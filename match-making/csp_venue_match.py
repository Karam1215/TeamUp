import logging
import threading
import uuid
from flask import Flask, request, jsonify, abort
from sqlalchemy import create_engine, Column, String, Date, Time, TIMESTAMP, JSON, Integer, text, CheckConstraint
from sqlalchemy.dialects.postgresql import UUID, JSONB
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker
from datetime import datetime, timedelta
import faiss
import json
import numpy as np
import requests
from flasgger import Swagger, swag_from

DB_URL = "postgresql+psycopg2://postgres:postgres@matchmaking-db:5432/matches_db"
CHAT_SERVICE_URL = "http://chat-service:8090/api/v1/chat/create"
engine = create_engine(DB_URL)
SessionLocal = sessionmaker(bind=engine)
Base = declarative_base()

app = Flask(__name__)
app.config.update({
    'SWAGGER': {
        'title': 'Matchmaking API',
        'uiversion': 3,
        'specs_route': '/api-docs/',
        'specs': [{
            'endpoint': 'apispec_1',
            'route': '/apispec_1.json',
            'rule_filter': lambda rule: True,
            'model_filter': lambda tag: True,
        }],
        'static_url_path': '/flasgger_static',
        'swagger_ui': True
    },
    'PROVIDE_AUTOMATIC_OPTIONS': True
})
swagger = Swagger(app)

# Logging setup
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s',
    handlers=[
        logging.StreamHandler(),
        logging.FileHandler('../matchmaking.log')
    ]
)
logger = logging.getLogger(__name__)

# Constants and Thread Safety
RANKING_WEIGHTS = {
    'BEGINNER': 0,
    'MEDIUM': 1,
    'ADVANCED': 2,
    'WORLD_CLASS': 3
}
faiss_index = faiss.IndexFlatL2(3)  # 3 dimensions: ranking, start_time, end_time
faiss_lock = threading.Lock()
vector_map = {}  # Maps team_id to index position

# Database Models
class MatchRequest(Base):
    __tablename__ = "match_requests"
    request_id = Column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4)
    team_id = Column(UUID(as_uuid=True), nullable=False)
    ranking = Column(String(50), nullable=False)
    start_time = Column(Time, nullable=False)
    end_time = Column(Time, nullable=False)
    team_size = Column(Integer, nullable=False)
    preferred_venues = Column(JSONB, nullable=False, server_default='[]')
    day = Column(Date, nullable=False)
    status = Column(String(20), server_default='pending', nullable=False)
    created_at = Column(TIMESTAMP, server_default=text('CURRENT_TIMESTAMP'))

    __table_args__ = (
        CheckConstraint(ranking.in_(['beginner', 'medium', 'advanced', 'world-class'])),
        CheckConstraint(team_size >= 1),
        CheckConstraint(status.in_(['pending', 'matched', 'expired'])),
    )

class Match(Base):
    __tablename__ = "matches"
    match_id = Column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4)
    creator_team_id = Column(UUID(as_uuid=True), nullable=False)
    joined_team_id = Column(UUID(as_uuid=True), nullable=False)
    start_time = Column(Time, nullable=False)
    end_time = Column(Time, nullable=False)
    day = Column(Date, nullable=False)
    venues_id = Column(JSONB, nullable=False, server_default='[]')
    status = Column(String(20), server_default='matched', nullable=False)
    created_at = Column(TIMESTAMP, server_default=text('CURRENT_TIMESTAMP'))
    updated_at = Column(TIMESTAMP)

    __table_args__ = (
        CheckConstraint(status.in_(['matched', 'expired', 'canceled'])),
    )

Base.metadata.create_all(bind=engine)

# Helper Functions
def encode_request_vector(req: MatchRequest):
    """Normalized feature vector [0-1 range]"""
    ranking_norm = RANKING_WEIGHTS[req.ranking] / 3.0
    start_minutes = req.start_time.hour * 60 + req.start_time.minute
    end_minutes = req.end_time.hour * 60 + req.end_time.minute
    return np.array([
        ranking_norm,
        start_minutes / 1440,
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
@swag_from({
    'tags': ['Match Requests'],
    'description': 'Search for potential match without storing the request',
    'parameters': [{
        'in': 'body',
        'name': 'body',
        'required': True,
        'schema': {
            'type': 'object',
            'properties': {
                'team_id': {'type': 'string', 'format': 'uuid', 'example': '550e8400-e29b-41d4-a716-446655440000'},
                'ranking': {'type': 'string', 'enum': ['beginner', 'medium', 'advanced', 'world-class'], 'example': 'medium'},
                'start_time': {'type': 'string', 'format': 'HH:MM', 'example': '14:00'},
                'end_time': {'type': 'string', 'format': 'HH:MM', 'example': '16:00'},
                'day': {'type': 'string', 'format': 'date', 'example': '2024-05-20'},
                'preferred_venues': {'type': 'array', 'items': {'type': 'string'}, 'example': ['Stadium A']},
                'team_size': {'type': 'integer', 'example': 5}
            },
            'required': ['team_id', 'ranking', 'start_time', 'end_time', 'day', 'team_size']
        }
    }],
    'responses': {
        200: {'description': 'List of potential matches'},
        400: {'description': 'Invalid input'},
        500: {'description': 'Internal server error'}
    }
})
def request_match():
    data = request.json
    session = SessionLocal()

    try:
        if data["ranking"] not in RANKING_WEIGHTS:
            return jsonify({"error": "Invalid ranking value"}), 400

        start_time = datetime.strptime(data["start_time"], "%H:%M").time()
        end_time = datetime.strptime(data["end_time"], "%H:%M").time()
        if start_time >= end_time:
            return jsonify({"error": "Invalid time range"}), 400

        team_size = data.get("team_size")
        if not isinstance(team_size, int) or team_size <= 0:
            return jsonify({"error": "Invalid team size"}), 400

        team_uuid = uuid.UUID(data["team_id"])

        # Create temporary request object
        temp_request = MatchRequest(
            team_id=team_uuid,
            ranking=data["ranking"],
            start_time=start_time,
            end_time=end_time,
            day=datetime.strptime(data["day"], "%Y-%m-%d").date(),
            preferred_venues=data.get("preferred_venues", []),
            team_size=team_size
        )

        return find_best_matches(temp_request.team_id, session, temp_request)

    except Exception as e:
        logger.error(f"Error: {str(e)}")
        return jsonify({"error": str(e)}), 400
    finally:
        session.close()

@app.route("/api/v1/match/join-match", methods=["POST"])
@swag_from({
    'tags': ['Matches'],
    'description': 'Join an existing match request',
    'parameters': [{
        'in': 'body',
        'name': 'body',
        'required': True,
        'schema': {
            'type': 'object',
            'properties': {
                'request_id': {'type': 'string', 'format': 'uuid'},
                'team_b_id': {'type': 'string', 'format': 'uuid'}
            },
            'required': ['request_id', 'team_b_id']
        }
    }],
    'responses': {
        200: {'description': 'Match created successfully'},
        400: {'description': 'Invalid input'},
        404: {'description': 'Not found'},
        500: {'description': 'Internal server error'}
    }
})
def join_match():
    data = request.get_json()
    session = SessionLocal()

    try:
        request_id = uuid.UUID(data["request_id"])
        team_b_id = uuid.UUID(data["team_b_id"])

        request_row = session.query(MatchRequest).filter_by(
            request_id=request_id,
            status='pending'
        ).first()

        if not request_row:
            abort(404, description="Match request not found")

        if request_row.team_id == team_b_id:
            abort(400, description="Cannot join own request")

        # Create match
        result = session.execute(text("""
            INSERT INTO matches (
                creator_team_id, joined_team_id, start_time, end_time, day, venues_id
            )
            VALUES (
                :creator, :joined, :start_time, :end_time, :day, :venues
            )
            RETURNING match_id
        """), {
            "creator": str(request_row.team_id),
            "joined": str(team_b_id),
            "start_time": request_row.start_time,
            "end_time": request_row.end_time,
            "day": request_row.day,
            "venues": json.dumps(request_row.preferred_venues)
        })

        match_id = result.scalar()  # fetch match_id here

        # Update statuses
        request_row.status = 'matched'
        session.query(MatchRequest).filter_by(team_id=team_b_id).update({'status': 'matched'})
        session.commit()
        rebuild_faiss_index()

        # Send a request to the chat service to create a chat room for the match
        chat_room_data = {
            'match_id': str(match_id),
            'team_a_id': str(request_row.team_id),
            'team_b_id': str(team_b_id),
            'day': str(request_row.day),
            'start_time':str(request_row.start_time),
            'end_time':str(request_row.end_time),
        }

        # Send the chat room creation request
        print('sending post req')
        chat_response = requests.post(CHAT_SERVICE_URL, json=chat_room_data)

        if chat_response.status_code != 200:
            logger.error(f"Failed to create chat room: {chat_response.text}")
            return jsonify({"error": "Match created but failed to create chat room"}), 500

        return jsonify({"message": "Match created successfully and chat room created"})

    except Exception as e:
        session.rollback()
        logger.error(f"/join-match error: {e}")
        return jsonify({"error": str(e)}), 500
    finally:
        session.close()

def find_best_matches(team_id, session, temp_request):
    with faiss_lock:
        if faiss_index.ntotal == 0:
            return jsonify([])

        # Get all match requests from DB (since we need day + venue filtering)
        all_requests = session.query(MatchRequest).filter(
            MatchRequest.status == 'pending',
            MatchRequest.team_id != team_id,
            MatchRequest.day == temp_request.day
        ).all()

        if not all_requests:
            return jsonify([])

        # Filter by overlapping venues
        filtered_requests = [
            r for r in all_requests
            if set(r.preferred_venues) & set(temp_request.preferred_venues)
        ]

        if not filtered_requests:
            return jsonify([])

        # Encode their vectors
        vectors = np.array([encode_request_vector(r) for r in filtered_requests], dtype='float32')

        # Encode query vector
        query_vector = encode_request_vector(temp_request).reshape(1, -1)

        # Build a temporary FAISS index for filtered vectors
        temp_index = faiss.IndexFlatL2(3)
        temp_index.add(vectors)

        # Search for top 10 similar
        D, I = temp_index.search(query_vector, k=min(10, len(filtered_requests)))

        # Return match info sorted by similarity
        matches = []
        for dist, idx in zip(D[0], I[0]):
            r = filtered_requests[idx]
            matches.append({
                'request_id': str(r.request_id),
                'team_id': str(r.team_id),
                'ranking': r.ranking,
                'start_time': str(r.start_time),
                'end_time': str(r.end_time),
                'preferred_venues': r.preferred_venues,
                'day': str(r.day),
                'team_size': r.team_size,
                'similarity_score': float(1 / (1 + dist))  # Optional score
            })

        return jsonify(matches)

@app.route("/api/v1/match/get-matches/<team_id>", methods=["GET"])
@swag_from({
    'tags': ['Matches'],
    'description': 'Get all matches for a team',
    'parameters': [{
        'in': 'path',
        'name': 'team_id',
        'type': 'string',
        'required': True
    }],
    'responses': {
        200: {'description': 'List of matches'},
        400: {'description': 'Invalid UUID'},
        500: {'description': 'Server error'}
    }
})
def get_matches(team_id):
    session = SessionLocal()
    try:
        team_uuid = uuid.UUID(team_id)
    except ValueError:
        abort(400, description="Invalid team ID format")

    try:
        matches = session.execute(text("""
            SELECT * FROM matches 
            WHERE creator_team_id = :team_id OR joined_team_id = :team_id
            ORDER BY day DESC, start_time DESC
        """), {"team_id": str(team_uuid)}).fetchall()

        result = []
        for match in matches:
            result.append({
                "match_id": str(match[0]),
                "creator_team_id": str(match[1]),
                "joined_team_id": str(match[2]),
                "start_time": match[3].strftime("%H:%M"),
                "end_time": match[4].strftime("%H:%M"),
                "day": match[5].isoformat(),
                "venues_id": match[6],
                "status": match[7],
                "created_at": match[8].isoformat()
            })

        return jsonify({"matches": result})

    except Exception as e:
        logger.error(f"Error getting matches: {str(e)}")
        return jsonify({"error": "Internal server error"}), 500
    finally:
        session.close()

@app.route("/api/v1/match/create-match-request", methods=["POST"])
@swag_from({
    'tags': ['Match Requests'],
    'description': 'Create a new match request',
    'parameters': [{
        'in': 'body',
        'name': 'body',
        'required': True,
        'schema': {
            'type': 'object',
            'properties': {
                'team_id': {'type': 'string', 'format': 'uuid'},
                'ranking': {'type': 'string'},
                'start_time': {'type': 'string'},
                'end_time': {'type': 'string'},
                'day': {'type': 'string'},
                'preferred_venues': {'type': 'array'},
                'team_size': {'type': 'integer'}
            },
            'required': ['team_id', 'ranking', 'start_time', 'end_time', 'day', 'team_size']
        }
    }],
    'responses': {
        201: {'description': 'Request created'},
        400: {'description': 'Invalid input or overlapping request'},
        409: {'description': 'Conflicting existing request'}
    }
})
def create_match_request():
    data = request.json
    session = SessionLocal()
    try:
        # Parse input data
        team_uuid = uuid.UUID(data["team_id"])
        start_time = datetime.strptime(data["start_time"], "%H:%M").time()
        end_time = datetime.strptime(data["end_time"], "%H:%M").time()
        day = datetime.strptime(data["day"], "%Y-%m-%d").date()
        team_size = data["team_size"]

        # Check for existing overlapping requests
        existing_conflict = session.query(MatchRequest).filter(
            MatchRequest.team_id == team_uuid,
            MatchRequest.day == day,
            MatchRequest.status.in_(['pending', 'matched']),
            MatchRequest.start_time < end_time,
            MatchRequest.end_time > start_time
        ).first()

        if existing_conflict:
            return jsonify({
                "error": "Team already has an active or pending match request with overlapping times on this day"
            }), 409

        # Validate team size
        if not isinstance(team_size, int) or team_size < 1:
            return jsonify({"error": "Invalid team size"}), 400

        # Create new request
        new_request = MatchRequest(
            team_id=team_uuid,
            ranking=data["ranking"],
            start_time=start_time,
            end_time=end_time,
            day=day,
            preferred_venues=data.get("preferred_venues", []),
            team_size=team_size
        )

        session.add(new_request)
        session.commit()
        rebuild_faiss_index()

        return jsonify({"message": "Match request created"}), 201

    except ValueError as e:
        session.rollback()
        logger.error(f"Validation error: {str(e)}")
        return jsonify({"error": "Invalid input format"}), 400
    except Exception as e:
        session.rollback()
        logger.error(f"Create request error: {str(e)}")
        return jsonify({"error": str(e)}), 400
    finally:
        session.close()

if __name__ == '__main__':
    app.run(host="0.0.0.0", port=5000)