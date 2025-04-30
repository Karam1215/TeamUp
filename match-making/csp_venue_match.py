from flask import Flask, request, jsonify
from sqlalchemy import create_engine, Column, String, Date, Time, TIMESTAMP, JSON, Integer, text
from sqlalchemy.dialects.postgresql import UUID
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker
from datetime import datetime, timedelta
import uuid
import faiss
import numpy as np
import logging
import threading

# Configuration
DB_URL = "postgresql+psycopg2://postgres:postgres@matchmaking-db:5432/matches_db"
engine = create_engine(DB_URL)
SessionLocal = sessionmaker(bind=engine)
Base = declarative_base()

app = Flask(__name__)

# Logging setup
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s',
    handlers=[
        logging.StreamHandler(),
        logging.FileHandler('matchmaking.log')
    ]
)
logger = logging.getLogger(__name__)

# Constants and Thread Safety
RANKING_WEIGHTS = {
    'beginner': 0,
    'medium': 1,
    'advanced': 2,
    'world-class': 3
}
faiss_index = faiss.IndexFlatL2(3)  # 3 dimensions: ranking, start_time, end_time
faiss_lock = threading.Lock()
vector_map = {}  # Maps team_id to index position


# Database Models
class MatchRequest(Base):
    __tablename__ = "match_requests"
    request_id = Column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4)
    team_id = Column(UUID(as_uuid=True), unique=True, nullable=False)
    ranking = Column(String(20), nullable=False)
    preferred_start_time = Column(Time, nullable=False)
    preferred_end_time = Column(Time, nullable=False)
    preferred_venues = Column(JSON, nullable=False, server_default='[]')
    preferred_day = Column(Date, nullable=False)
    team_size = Column(Integer, nullable=False)  # New team_size column
    status = Column(String(20), default='pending')
    created_at = Column(TIMESTAMP, server_default=text('CURRENT_TIMESTAMP'))


Base.metadata.create_all(bind=engine)


# Helper Functions
def encode_request_vector(req: MatchRequest):
    """Normalized feature vector [0-1 range]"""
    ranking_norm = RANKING_WEIGHTS[req.ranking] / 3.0
    start_minutes = req.preferred_start_time.hour * 60 + req.preferred_start_time.minute
    end_minutes = req.preferred_end_time.hour * 60 + req.preferred_end_time.minute
    return np.array([
        ranking_norm,
        start_minutes / 1440,  # 24*60
        end_minutes / 1440
    ], dtype='float32')


def rebuild_faiss_index():
    """Refresh FAISS index from database"""
    global faiss_index, vector_map
    session = SessionLocal()
    try:
        with faiss_lock:
            faiss_index.reset()
            vector_map.clear()

            requests = session.query(MatchRequest).filter_by(status='pending').all()
            if not requests:
                return

            vectors = np.array([encode_request_vector(req) for req in requests], dtype='float32')
            faiss_index.add(vectors)
            vector_map = {str(req.team_id): idx for idx, req in enumerate(requests)}
            logger.info(f"Rebuilt FAISS index with {len(requests)} vectors")
    finally:
        session.close()

_first_request_handled = False

# Initialization
@app.before_request
def initialize_system_on_first_request():
    global _first_request_handled
    if not _first_request_handled:
        with app.app_context():
            rebuild_faiss_index()
            logger.info("System initialized")
            _first_request_handled = True


# API Endpoints
@app.route("/api/v1/match/request-match", methods=["POST"])
def request_match():
    data = request.json
    session = SessionLocal()

    try:
        # Validation
        if data["ranking"] not in RANKING_WEIGHTS:
            return jsonify({"error": "Invalid ranking value"}), 400

        start_time = datetime.strptime(data["start_time"], "%H:%M").time()
        end_time = datetime.strptime(data["end_time"], "%H:%M").time()
        if start_time >= end_time:
            return jsonify({"error": "Invalid time range"}), 400

        # Ensure team_size is provided and is a valid number
        team_size = data.get("team_size")
        if not isinstance(team_size, int) or team_size <= 0:
            return jsonify({"error": "Invalid team size"}), 400

        team_uuid = uuid.UUID(data["team_id"])

        # Delete existing request
        existing = session.query(MatchRequest).filter_by(team_id=team_uuid).first()
        if existing:
            session.delete(existing)
            session.commit()
            rebuild_faiss_index()

        # Create new request
        new_request = MatchRequest(
            team_id=team_uuid,
            ranking=data["ranking"],
            preferred_start_time=start_time,
            preferred_end_time=end_time,
            preferred_day=datetime.strptime(data["preferred_day"], "%Y-%m-%d").date(),
            preferred_venues=data.get("preferred_venues", []),
            team_size=team_size  # Set the team_size
        )
        session.add(new_request)
        session.commit()
        rebuild_faiss_index()  # Refresh index

        return find_best_matches(new_request.team_id, session)

    except Exception as e:
        session.rollback()
        logger.error(f"Error: {str(e)}")
        return jsonify({"error": str(e)}), 400
    finally:
        session.close()


# Matchmaking Core
def find_best_matches(team_id, session):
    target_request = session.query(MatchRequest).filter_by(team_id=team_id).first()
    if not target_request:
        return jsonify({"error": "Team not found"}), 404

    # Get same-day candidates
    candidates = session.query(MatchRequest).filter(
        MatchRequest.status == 'pending',
        MatchRequest.team_id != team_id,
        MatchRequest.preferred_day == target_request.preferred_day
    ).all()

    if not candidates:
        return jsonify({"matches": []})

    # List of potential matches
    potential_matches = []

    # Function to calculate the ranking difference
    def get_ranking_difference(ranking1, ranking2):
        return abs(RANKING_WEIGHTS[ranking1] - RANKING_WEIGHTS[ranking2])

    # Iterate through each candidate and check for valid matches
    for candidate in candidates:
        # Calculate expanded time windows with 2-hour flexibility
        preferred_date = target_request.preferred_day

        target_start = datetime.combine(preferred_date, target_request.preferred_start_time)
        target_end = datetime.combine(preferred_date, target_request.preferred_end_time)
        target_earliest = target_start - timedelta(hours=2)
        target_latest = target_end + timedelta(hours=2)

        candidate_start = datetime.combine(preferred_date, candidate.preferred_start_time)
        candidate_end = datetime.combine(preferred_date, candidate.preferred_end_time)
        candidate_earliest = candidate_start - timedelta(hours=2)
        candidate_latest = candidate_end + timedelta(hours=2)

        # Check for overlap between expanded windows
        if not (target_earliest < candidate_latest and candidate_earliest < target_latest):
            continue

        # Venue check
        if not set(target_request.preferred_venues) & set(candidate.preferred_venues):
            continue

        # Match based on team_size
        if target_request.team_size != candidate.team_size:
            continue

        # Match based on ranking flexibility (allow difference of 1 rank)
        if get_ranking_difference(target_request.ranking, candidate.ranking) > 1:
            continue

        # Add to potential matches list
        potential_matches.append({
            "team_id": str(candidate.team_id),
            "ranking": candidate.ranking,
            "members": candidate.team_size,
            "start_time": candidate.preferred_start_time.strftime("%H:%M"),
            "end_time": candidate.preferred_end_time.strftime("%H:%M"),
            "day": candidate.preferred_day.strftime("%Y-%m-%d"),
            "venues": candidate.preferred_venues
        })

    session.commit()
    logger.info(f"Found {len(potential_matches)} potential matches for {team_id}")
    return jsonify({"matches": potential_matches})

if __name__ == '__main__':
    app.run(host="0.0.0.0", port=5000)

