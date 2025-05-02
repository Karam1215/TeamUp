import logging
import threading
import uuid
import asyncpg
from flask import Flask, request, jsonify, abort
from sqlalchemy import create_engine, Column, String, Date, Time, TIMESTAMP, JSON, Integer, text
from sqlalchemy.dialects.postgresql import UUID
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker
from datetime import datetime, timedelta
import faiss
import json
import numpy as np
from flasgger import Swagger, swag_from

# Configuration
DB_URL = "postgresql+psycopg2://postgres:postgres@matchmaking-db:5432/matches_db"
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
    'PROVIDE_AUTOMATIC_OPTIONS': True  # Add this critical line
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
    'beginner': 0,
    'medium': 1,
    'advanced': 2,
    'world-class': 3
}
faiss_index = faiss.IndexFlatL2(3)  # 3 dimensions: ranking, start_time, end_time
faiss_lock = threading.Lock()
vector_map = {}  # Maps team_id to index position

class Match(Base):
    __tablename__ = "matches"
    match_id = Column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4)
    team1_id = Column(UUID(as_uuid=True), nullable=False)
    team2_id = Column(UUID(as_uuid=True), nullable=False)
    scheduled_time = Column(Date, nullable=False)
    venues_id = Column(JSON, nullable=False)
    status = Column(String(20), default='scheduled')
    created_at = Column(TIMESTAMP, server_default=text('CURRENT_TIMESTAMP'))

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
    team_size = Column(Integer, nullable=False)
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
    'description': 'Create a new match request',
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
                'preferred_day': {'type': 'string', 'format': 'date', 'example': '2024-05-20'},
                'preferred_venues': {'type': 'array', 'items': {'type': 'string'}, 'example': ['Stadium A']},
                'team_size': {'type': 'integer', 'example': 5}
            },
            'required': ['team_id', 'ranking', 'start_time', 'end_time', 'preferred_day', 'team_size']
        }
    }],
    'responses': {
        200: {
            'description': 'List of potential matches',
            'examples': {
                'application/json': {
                    'matches': [{
                        'request_id': '550e8400-e29b-41d4-a716-446655440000',
                        'team_id': '550e8400-e29b-41d4-a716-446655440001',
                        'ranking': 'medium',
                        'team_size': 5,
                        'preferred_start_time': '14:00',
                        'preferred_end_time': '16:00'
                    }]
                }
            }
        },
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
            team_size=team_size
        )
        session.add(new_request)
        session.commit()
        rebuild_faiss_index()

        return find_best_matches(new_request.team_id, session)

    except Exception as e:
        session.rollback()
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
                'request_id': {'type': 'string', 'format': 'uuid', 'example': '550e8400-e29b-41d4-a716-446655440000'},
                'team_b_id': {'type': 'string', 'format': 'uuid', 'example': '550e8400-e29b-41d4-a716-446655440001'}
            },
            'required': ['request_id', 'team_b_id']
        }
    }],
    'responses': {
        200: {'description': 'Match created successfully'},
        400: {'description': 'Invalid input or self-join attempt'},
        404: {'description': 'Match request or team not found'},
        500: {'description': 'Internal server error'}
    }
})
def join_match():
    data = request.get_json()
    session = SessionLocal()

    try:
        # Validate required fields
        if "request_id" not in data or "team_b_id" not in data:
            abort(400, description="Missing request_id or team_b_id in request")

        # 1. Get the match request of Team A
        try:
            request_id = uuid.UUID(data["request_id"])
            team_b_id = uuid.UUID(data["team_b_id"])
        except (TypeError, ValueError):
            abort(400, description="Invalid UUID format")

        request_row = session.query(MatchRequest).filter_by(
            request_id=request_id,
            status='pending'
        ).first()

        if not request_row:
            abort(404, description="Match request not found or already matched")

        # 2. Prevent self-join
        if request_row.team_id == team_b_id:
            abort(400, description="Team cannot join their own match request")

        # 3. Verify Team B exists (optional but recommended)
        team_b_exists = session.query(MatchRequest).filter_by(team_id=team_b_id).first()
        if not team_b_exists:
            abort(404, description="Team B has no active match request")

        # 4. Create match
        session.execute(text("""
            INSERT INTO matches (team1_id, team2_id, scheduled_time, venues_id, status)
            VALUES (:team1, :team2, :time, :venue, 'matched')
        """), {
            "team1": str(request_row.team_id),
            "team2": str(team_b_id),
            "time": request_row.preferred_day,
            "venue": json.dumps(request_row.preferred_venues)
        })

        # 5. Update statuses
        request_row.status = 'matched'
        session.query(MatchRequest).filter_by(team_id=team_b_id).delete()

        session.commit()
        rebuild_faiss_index()

        return jsonify({"message": "Match created successfully"})

    except Exception as e:
        session.rollback()
        logger.error(f"/join-match error: {e}")
        return jsonify({"error": str(e)}), 500
    finally:
        session.close()

# Matchmaking Core
def find_best_matches(team_id, session):
    target_request = session.query(MatchRequest).filter_by(team_id=team_id).first()
    if not target_request:
        return jsonify({"error": "Team not found"}), 404

    target_vector = encode_request_vector(target_request)

    with faiss_lock:
        distances, indices = faiss_index.search(np.array([target_vector]), k=20)

    # Map team_id to distance
    team_distance_map = {}
    for idx, dist in zip(indices[0], distances[0]):
        if idx != -1:
            for team_id_str, vec_idx in vector_map.items():
                if vec_idx == idx:
                    team_distance_map[uuid.UUID(team_id_str)] = dist
                    break

    candidate_team_ids = list(team_distance_map.keys())

    candidates = session.query(MatchRequest).filter(
        MatchRequest.team_id.in_(candidate_team_ids),
        MatchRequest.status == 'pending',
        MatchRequest.team_id != target_request.team_id,
        MatchRequest.preferred_day == target_request.preferred_day,
        MatchRequest.team_size == target_request.team_size
    ).all()

    filtered_matches = []
    preferred_date = target_request.preferred_day

    for candidate in candidates:
        target_start = datetime.combine(preferred_date, target_request.preferred_start_time)
        target_end = datetime.combine(preferred_date, target_request.preferred_end_time)
        target_earliest = target_start - timedelta(hours=2)
        target_latest = target_end + timedelta(hours=2)

        candidate_start = datetime.combine(preferred_date, candidate.preferred_start_time)
        candidate_end = datetime.combine(preferred_date, candidate.preferred_end_time)
        candidate_earliest = candidate_start - timedelta(hours=2)
        candidate_latest = candidate_end + timedelta(hours=2)

        if not (target_earliest < candidate_latest and candidate_earliest < target_latest):
            continue

        if not set(target_request.preferred_venues) & set(candidate.preferred_venues):
            continue

        if abs(RANKING_WEIGHTS[target_request.ranking] - RANKING_WEIGHTS[candidate.ranking]) > 1:
            continue

        filtered_matches.append(candidate)

    # Sort by FAISS distance
    sorted_matches = sorted(filtered_matches, key=lambda c: team_distance_map.get(c.team_id, float('inf')))

    potential_matches = []
    for match in sorted_matches:
        potential_matches.append({
            "request_id": match.request_id,
            "team_id": match.team_id,
            "ranking": match.ranking,
            "team_size": match.team_size,
            "preferred_start_time": match.preferred_start_time.strftime("%H:%M"),
            "preferred_end_time": match.preferred_end_time.strftime("%H:%M"),
        })

    return jsonify({"matches": potential_matches})


@app.route("/api/v1/match/get-matches/<team_id>", methods=["GET"])
@swag_from({
    'tags': ['Matches'],
    'description': 'Get all matches for a specific team',
    'parameters': [{
        'in': 'path',
        'name': 'team_id',
        'type': 'string',
        'format': 'uuid',
        'required': True,
        'example': '550e8400-e29b-41d4-a716-446655440000'
    }],
    'responses': {
        200: {
            'description': 'List of matches',
            'schema': {
                'type': 'object',
                'properties': {
                    'matches': {
                        'type': 'array',
                        'items': {
                            'type': 'object',
                            'properties': {
                                'match_id': {'type': 'string', 'format': 'uuid'},
                                'team1_id': {'type': 'string', 'format': 'uuid'},
                                'team2_id': {'type': 'string', 'format': 'uuid'},
                                'scheduled_time': {'type': 'string', 'format': 'date'},
                                'venues': {'type': 'array', 'items': {'type': 'string'}},
                                'status': {'type': 'string'},
                                'created_at': {'type': 'string', 'format': 'date-time'}
                            }
                        }
                    }
                }
            }
        },
        400: {'description': 'Invalid team ID format'},
        500: {'description': 'Internal server error'}
    }
})
def get_matches(team_id):
    session = SessionLocal()
    try:
        # Validate team_id UUID format
        try:
            team_uuid = uuid.UUID(team_id)
        except ValueError:
            abort(400, description="Invalid team ID format")

        # Query matches where the team is either team1 or team2
        matches = session.execute(text("""
            SELECT * FROM matches 
            WHERE team1_id = :team_id OR team2_id = :team_id
            ORDER BY scheduled_time DESC
        """), {"team_id": str(team_uuid)}).fetchall()

        # Convert matches to JSON-serializable format
        result = []
        for match in matches:
            result.append({
                "match_id": str(match[0]),
                "team1_id": str(match[1]),
                "team2_id": str(match[2]),
                "scheduled_time": match[3].isoformat(),
                "venues": match[4],
                "status": match[5],
                "created_at": match[6].isoformat() if match[6] else None
            })

        return jsonify({"matches": result})

    except Exception as e:
        logger.error(f"Error getting matches: {str(e)}")
        return jsonify({"error": "Internal server error"}), 500
    finally:
        session.close()

if __name__ == '__main__':
    app.run(host="0.0.0.0", port=5000)
import logging
import threading
import uuid
import asyncpg
from flask import Flask, request, jsonify, abort
from sqlalchemy import create_engine, Column, String, Date, Time, TIMESTAMP, JSON, Integer, text
from sqlalchemy.dialects.postgresql import UUID
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker
from datetime import datetime, timedelta
import faiss
import json
import numpy as np
from flasgger import Swagger, swag_from

# Configuration
DB_URL = "postgresql+psycopg2://postgres:postgres@matchmaking-db:5432/matches_db"
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
    'PROVIDE_AUTOMATIC_OPTIONS': True  # Add this critical line
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
    'beginner': 0,
    'medium': 1,
    'advanced': 2,
    'world-class': 3
}
faiss_index = faiss.IndexFlatL2(3)  # 3 dimensions: ranking, start_time, end_time
faiss_lock = threading.Lock()
vector_map = {}  # Maps team_id to index position

class Match(Base):
    __tablename__ = "matches"
    match_id = Column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4)
    team1_id = Column(UUID(as_uuid=True), nullable=False)
    team2_id = Column(UUID(as_uuid=True), nullable=False)
    scheduled_time = Column(Date, nullable=False)
    venues_id = Column(JSON, nullable=False)
    status = Column(String(20), default='scheduled')
    created_at = Column(TIMESTAMP, server_default=text('CURRENT_TIMESTAMP'))

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
    team_size = Column(Integer, nullable=False)
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
    'description': 'Create a new match request',
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
                'preferred_day': {'type': 'string', 'format': 'date', 'example': '2024-05-20'},
                'preferred_venues': {'type': 'array', 'items': {'type': 'string'}, 'example': ['Stadium A']},
                'team_size': {'type': 'integer', 'example': 5}
            },
            'required': ['team_id', 'ranking', 'start_time', 'end_time', 'preferred_day', 'team_size']
        }
    }],
    'responses': {
        200: {
            'description': 'List of potential matches',
            'examples': {
                'application/json': {
                    'matches': [{
                        'request_id': '550e8400-e29b-41d4-a716-446655440000',
                        'team_id': '550e8400-e29b-41d4-a716-446655440001',
                        'ranking': 'medium',
                        'team_size': 5,
                        'preferred_start_time': '14:00',
                        'preferred_end_time': '16:00'
                    }]
                }
            }
        },
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
            team_size=team_size
        )
        session.add(new_request)
        session.commit()
        rebuild_faiss_index()

        return find_best_matches(new_request.team_id, session)

    except Exception as e:
        session.rollback()
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
                'request_id': {'type': 'string', 'format': 'uuid', 'example': '550e8400-e29b-41d4-a716-446655440000'},
                'team_b_id': {'type': 'string', 'format': 'uuid', 'example': '550e8400-e29b-41d4-a716-446655440001'}
            },
            'required': ['request_id', 'team_b_id']
        }
    }],
    'responses': {
        200: {'description': 'Match created successfully'},
        400: {'description': 'Invalid input or self-join attempt'},
        404: {'description': 'Match request or team not found'},
        500: {'description': 'Internal server error'}
    }
})
def join_match():
    data = request.get_json()
    session = SessionLocal()

    try:
        # Validate required fields
        if "request_id" not in data or "team_b_id" not in data:
            abort(400, description="Missing request_id or team_b_id in request")

        # 1. Get the match request of Team A
        try:
            request_id = uuid.UUID(data["request_id"])
            team_b_id = uuid.UUID(data["team_b_id"])
        except (TypeError, ValueError):
            abort(400, description="Invalid UUID format")

        request_row = session.query(MatchRequest).filter_by(
            request_id=request_id,
            status='pending'
        ).first()

        if not request_row:
            abort(404, description="Match request not found or already matched")

        # 2. Prevent self-join
        if request_row.team_id == team_b_id:
            abort(400, description="Team cannot join their own match request")

        # 3. Verify Team B exists (optional but recommended)
        team_b_exists = session.query(MatchRequest).filter_by(team_id=team_b_id).first()
        if not team_b_exists:
            abort(404, description="Team B has no active match request")

        # 4. Create match
        session.execute(text("""
            INSERT INTO matches (team1_id, team2_id, scheduled_time, venues_id, status)
            VALUES (:team1, :team2, :time, :venue, 'matched')
        """), {
            "team1": str(request_row.team_id),
            "team2": str(team_b_id),
            "time": request_row.preferred_day,
            "venue": json.dumps(request_row.preferred_venues)
        })

        # 5. Update statuses
        request_row.status = 'matched'
        session.query(MatchRequest).filter_by(team_id=team_b_id).delete()

        session.commit()
        rebuild_faiss_index()

        return jsonify({"message": "Match created successfully"})

    except Exception as e:
        session.rollback()
        logger.error(f"/join-match error: {e}")
        return jsonify({"error": str(e)}), 500
    finally:
        session.close()

# Matchmaking Core
def find_best_matches(team_id, session):
    target_request = session.query(MatchRequest).filter_by(team_id=team_id).first()
    if not target_request:
        return jsonify({"error": "Team not found"}), 404

    target_vector = encode_request_vector(target_request)

    with faiss_lock:
        distances, indices = faiss_index.search(np.array([target_vector]), k=20)

    # Map team_id to distance
    team_distance_map = {}
    for idx, dist in zip(indices[0], distances[0]):
        if idx != -1:
            for team_id_str, vec_idx in vector_map.items():
                if vec_idx == idx:
                    team_distance_map[uuid.UUID(team_id_str)] = dist
                    break

    candidate_team_ids = list(team_distance_map.keys())

    candidates = session.query(MatchRequest).filter(
        MatchRequest.team_id.in_(candidate_team_ids),
        MatchRequest.status == 'pending',
        MatchRequest.team_id != target_request.team_id,
        MatchRequest.preferred_day == target_request.preferred_day,
        MatchRequest.team_size == target_request.team_size
    ).all()

    filtered_matches = []
    preferred_date = target_request.preferred_day

    for candidate in candidates:
        target_start = datetime.combine(preferred_date, target_request.preferred_start_time)
        target_end = datetime.combine(preferred_date, target_request.preferred_end_time)
        target_earliest = target_start - timedelta(hours=2)
        target_latest = target_end + timedelta(hours=2)

        candidate_start = datetime.combine(preferred_date, candidate.preferred_start_time)
        candidate_end = datetime.combine(preferred_date, candidate.preferred_end_time)
        candidate_earliest = candidate_start - timedelta(hours=2)
        candidate_latest = candidate_end + timedelta(hours=2)

        if not (target_earliest < candidate_latest and candidate_earliest < target_latest):
            continue

        if not set(target_request.preferred_venues) & set(candidate.preferred_venues):
            continue

        if abs(RANKING_WEIGHTS[target_request.ranking] - RANKING_WEIGHTS[candidate.ranking]) > 1:
            continue

        filtered_matches.append(candidate)

    # Sort by FAISS distance
    sorted_matches = sorted(filtered_matches, key=lambda c: team_distance_map.get(c.team_id, float('inf')))

    potential_matches = []
    for match in sorted_matches:
        potential_matches.append({
            "request_id": match.request_id,
            "team_id": match.team_id,
            "ranking": match.ranking,
            "team_size": match.team_size,
            "preferred_start_time": match.preferred_start_time.strftime("%H:%M"),
            "preferred_end_time": match.preferred_end_time.strftime("%H:%M"),
        })

    return jsonify({"matches": potential_matches})


@app.route("/api/v1/match/get-matches/<team_id>", methods=["GET"])
@swag_from({
    'tags': ['Matches'],
    'description': 'Get all matches for a specific team',
    'parameters': [{
        'in': 'path',
        'name': 'team_id',
        'type': 'string',
        'format': 'uuid',
        'required': True,
        'example': '550e8400-e29b-41d4-a716-446655440000'
    }],
    'responses': {
        200: {
            'description': 'List of matches',
            'schema': {
                'type': 'object',
                'properties': {
                    'matches': {
                        'type': 'array',
                        'items': {
                            'type': 'object',
                            'properties': {
                                'match_id': {'type': 'string', 'format': 'uuid'},
                                'team1_id': {'type': 'string', 'format': 'uuid'},
                                'team2_id': {'type': 'string', 'format': 'uuid'},
                                'scheduled_time': {'type': 'string', 'format': 'date'},
                                'venues': {'type': 'array', 'items': {'type': 'string'}},
                                'status': {'type': 'string'},
                                'created_at': {'type': 'string', 'format': 'date-time'}
                            }
                        }
                    }
                }
            }
        },
        400: {'description': 'Invalid team ID format'},
        500: {'description': 'Internal server error'}
    }
})
def get_matches(team_id):
    session = SessionLocal()
    try:
        # Validate team_id UUID format
        try:
            team_uuid = uuid.UUID(team_id)
        except ValueError:
            abort(400, description="Invalid team ID format")

        # Query matches where the team is either team1 or team2
        matches = session.execute(text("""
            SELECT * FROM matches 
            WHERE team1_id = :team_id OR team2_id = :team_id
            ORDER BY scheduled_time DESC
        """), {"team_id": str(team_uuid)}).fetchall()

        # Convert matches to JSON-serializable format
        result = []
        for match in matches:
            result.append({
                "match_id": str(match[0]),
                "team1_id": str(match[1]),
                "team2_id": str(match[2]),
                "scheduled_time": match[3].isoformat(),
                "venues": match[4],
                "status": match[5],
                "created_at": match[6].isoformat() if match[6] else None
            })

        return jsonify({"matches": result})

    except Exception as e:
        logger.error(f"Error getting matches: {str(e)}")
        return jsonify({"error": "Internal server error"}), 500
    finally:
        session.close()

if __name__ == '__main__':
    app.run(host="0.0.0.0", port=5000)