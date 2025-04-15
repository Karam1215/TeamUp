import uuid
from datetime import datetime, timedelta, time
from flask import Flask, request, jsonify
from sqlalchemy import create_engine, Column, String, Time, JSON, TIMESTAMP, Date, text, Integer
from sqlalchemy.dialects.postgresql import UUID
from sqlalchemy.orm import declarative_base, sessionmaker
from ortools.sat.python import cp_model
import os
import logging
from tabulate import tabulate
import requests

# Configuration
DB_URL = "postgresql+psycopg2://postgres:postgres@matchmaking-db:5432/matches_db"
VENUE_SERVICE_URL = "http://venue-service:8084"
MINIMUM_BOOKING_DURATION = timedelta(hours=1)

engine = create_engine(DB_URL)
SessionLocal = sessionmaker(bind=engine)
Base = declarative_base()

app = Flask(__name__)

logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s',
    handlers=[
        logging.StreamHandler(),
        logging.FileHandler('matchmaking.log')
    ]
)
logger = logging.getLogger(__name__)


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
    status = Column(String(20), default='pending')
    created_at = Column(TIMESTAMP, server_default=text('CURRENT_TIMESTAMP'))
    team_size = Column(Integer, nullable=False)  # Added column


class Match(Base):
    __tablename__ = "matches"
    match_id = Column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4)
    team1_id = Column(UUID(as_uuid=True), nullable=False)
    team2_id = Column(UUID(as_uuid=True), nullable=False)
    scheduled_time = Column(TIMESTAMP)
    venue_id = Column(UUID(as_uuid=True))
    field_id = Column(UUID(as_uuid=True))
    booking_id = Column(UUID(as_uuid=True))
    created_at = Column(TIMESTAMP, server_default=text('CURRENT_TIMESTAMP'))


Base.metadata.create_all(bind=engine)

# Constants
RANKING_WEIGHTS = {
    'beginner': 0,
    'medium': 1,
    'advanced': 2,
    'world-class': 3
}


# Helper Functions
def time_to_decimal(t: time) -> float:
    return t.hour + t.minute / 60


def round_to_nearest_hour(dt: datetime) -> datetime:
    return dt.replace(minute=0, second=0, microsecond=0) + timedelta(hours=1 if dt.minute >= 30 else 0)


def get_future_time() -> datetime:
    now = datetime.utcnow()
    future_time = now + timedelta(hours=2)
    return round_to_nearest_hour(future_time)


def log_team_table(requests):
    """Log team information in a table format"""
    team_data = []
    for i, req in enumerate(requests):
        team_data.append([
            i,
            str(req.team_id),
            req.ranking,
            req.preferred_start_time.strftime('%H:%M'),
            req.preferred_end_time.strftime('%H:%M'),
            req.preferred_day.strftime('%Y-%m-%d'),
            ', '.join(req.preferred_venues) if req.preferred_venues else 'None',
            req.team_size  # Added team size
        ])

    headers = ["Index", "Team ID", "Ranking", "Start Time", "End Time", "Day", "Preferred Venues", "Team Size"]
    logger.info("\nTeams in Matchmaking:\n" + tabulate(team_data, headers=headers, tablefmt="grid"))


def validate_time_overlap(req1, req2) -> bool:
    t1_start = time_to_decimal(req1.preferred_start_time)
    t1_end = time_to_decimal(req1.preferred_end_time)
    t2_start = time_to_decimal(req2.preferred_start_time)
    t2_end = time_to_decimal(req2.preferred_end_time)
    overlap = t1_start < t2_end and t2_start < t1_end
    logger.debug(
        f"Time overlap between {req1.team_id} ({t1_start}-{t1_end}) and {req2.team_id} ({t2_start}-{t2_end}): {overlap}")
    return overlap


def validate_rank(req1, req2) -> bool:
    rank_diff = abs(RANKING_WEIGHTS[req1.ranking] - RANKING_WEIGHTS[req2.ranking])
    valid = rank_diff <= 1
    logger.debug(
        f"Rank check between {req1.team_id} ({req1.ranking}) and {req2.team_id} ({req2.ranking}): {'Valid' if valid else 'Invalid'}")
    return valid


def calculate_overlapping_time(req1, req2):
    """Calculate the one-hour aligned overlapping time between two requests"""
    day = req1.preferred_day
    start1 = datetime.combine(day, req1.preferred_start_time)
    end1 = datetime.combine(day, req1.preferred_end_time)
    start2 = datetime.combine(day, req2.preferred_start_time)
    end2 = datetime.combine(day, req2.preferred_end_time)

    overlap_start = max(start1, start2)
    overlap_end = min(end1, end2)

    if overlap_start >= overlap_end:
        return None, None

    # Round up overlap_start to the next hour if minutes > 0
    if overlap_start.minute > 0 or overlap_start.second > 0 or overlap_start.microsecond > 0:
        overlap_start = overlap_start.replace(minute=0, second=0, microsecond=0) + timedelta(hours=1)

    overlap_end = overlap_start + timedelta(hours=1)

    if overlap_end <= min(end1, end2):
        return overlap_start, overlap_end

    return None, None


def make_venue_booking_request(venue_ids, start_time, end_time, match_id):
    """Make a booking request to the venue service"""
    payload = {
        "venueIds": [str(v) for v in venue_ids],
        "startTime": start_time.isoformat(),
        "endTime": end_time.isoformat(),
        "matchId": str(match_id)
    }

    try:
        response = requests.post(
            f"{VENUE_SERVICE_URL}/api/v1/venue/booking-request",
            json=payload,
            timeout=5  # 5 second timeout
        )
        response.raise_for_status()
        return response.json()
    except requests.exceptions.RequestException as e:
        logger.error(f"Venue booking failed: {str(e)}")
        return None


# API Endpoints
@app.route('/health')
def health_check():
    return jsonify({'status': 'ok'})


@app.route('/match-requests', methods=['POST'])
def create_match_request():
    data = request.json
    session = SessionLocal()

    try:
        new_request = MatchRequest(
            team_id=uuid.UUID(data['team_id']),
            ranking=data['ranking'].lower(),
            preferred_start_time=datetime.strptime(data['preferred_start_time'], '%H:%M').time(),
            preferred_end_time=datetime.strptime(data['preferred_end_time'], '%H:%M').time(),
            preferred_venues=data.get('preferred_venues', []),
            preferred_day=datetime.strptime(data['preferred_day'], '%Y-%m-%d').date(),
            team_size=data['team_size']  # Added line
        )

        session.add(new_request)
        session.commit()
        logger.info(f"New match request created for team {data['team_id']}")
        return jsonify({'message': 'Request created successfully'}), 201

    except Exception as e:
        session.rollback()
        logger.error(f"Error creating match request: {str(e)}", exc_info=True)
        return jsonify({'error': str(e)}), 400
    finally:
        session.close()


@app.route('/run-matchmaking', methods=['POST'])
def run_matchmaking():
    session = SessionLocal()
    try:
        requests = session.query(MatchRequest).filter_by(status='pending').order_by(MatchRequest.preferred_day).all()
        logger.info(f"\n{'=' * 50}\nStarting matchmaking with {len(requests)} pending requests\n{'=' * 50}")

        # Log all teams in a table format
        log_team_table(requests)

        if len(requests) < 2:
            logger.info("Not enough pending requests (need at least 2)")
            return jsonify({'message': 'Not enough pending requests'}), 200

        pair_venues = {}
        valid_pairs = []
        potential_pairs = []

        logger.info("\nEvaluating potential pairs:")
        for i in range(len(requests)):
            for j in range(i + 1, len(requests)):
                req1 = requests[i]
                req2 = requests[j]
                pair_key = (i, j)

                # Log basic pair info
                pair_info = {
                    "Team1": str(req1.team_id),
                    "Team2": str(req2.team_id),
                    "Same Day": req1.preferred_day == req2.preferred_day,
                    "Time Overlap": False,
                    "Common Venues": 0,
                    "Rank Compatible": False,
                    "Team Size Match": False,  # Added key
                    "Valid": False
                }

                if req1.preferred_day != req2.preferred_day:
                    logger.debug(f"Pair {i}-{j}: Different days ({req1.preferred_day} vs {req2.preferred_day})")
                    potential_pairs.append(pair_info)
                    continue

                time_overlap = validate_time_overlap(req1, req2)
                pair_info["Time Overlap"] = time_overlap
                if not time_overlap:
                    logger.debug(f"Pair {i}-{j}: No time overlap")
                    potential_pairs.append(pair_info)
                    continue

                common_venues = list(set(req1.preferred_venues) & set(req2.preferred_venues))
                pair_info["Common Venues"] = len(common_venues)
                if not common_venues:
                    logger.debug(f"Pair {i}-{j}: No common venues")
                    potential_pairs.append(pair_info)
                    continue

                rank_compatible = validate_rank(req1, req2)
                pair_info["Rank Compatible"] = rank_compatible
                if not rank_compatible:
                    logger.debug(f"Pair {i}-{j}: Rank mismatch ({req1.ranking} vs {req2.ranking})")
                    potential_pairs.append(pair_info)
                    continue

                # Check team sizes match
                team_size_match = req1.team_size == req2.team_size
                pair_info["Team Size Match"] = team_size_match
                if not team_size_match:
                    logger.debug(f"Pair {i}-{j}: Team size mismatch ({req1.team_size} vs {req2.team_size})")
                    potential_pairs.append(pair_info)
                    continue

                # All checks passed, mark as valid
                pair_info["Valid"] = True
                valid_pairs.append(pair_key)
                pair_venues[pair_key] = common_venues
                logger.info(f"Valid pair found: {req1.team_id} & {req2.team_id} (venues: {common_venues})")
                potential_pairs.append(pair_info)

        # Log potential pairs in a table
        logger.info("\nPotential Pairs Evaluation:\n" +
                   tabulate([p.values() for p in potential_pairs],
                            headers=potential_pairs[0].keys() if potential_pairs else [],
                            tablefmt="grid"))

        model = cp_model.CpModel()
        solver = cp_model.CpSolver()

        pair_vars = {pair: model.NewBoolVar(f'pair_{pair[0]}_{pair[1]}') for pair in valid_pairs}

        # Add constraints
        for team_idx in range(len(requests)):
            involved_pairs = [
                var for pair, var in pair_vars.items()
                if team_idx in pair
            ]
            if involved_pairs:
                model.Add(sum(involved_pairs) <= 1)

        model.Maximize(sum(pair_vars.values()))

        logger.info("\nSolving constraint programming model...")
        status = solver.Solve(model)
        logger.info(f"Solver status: {status} (OPTIMAL={cp_model.OPTIMAL})")

        matches = []
        if status == cp_model.OPTIMAL:
            logger.info(f"\nFound {solver.ObjectiveValue()} valid matches:")
            for pair, var in pair_vars.items():
                if solver.Value(var):
                    i, j = pair
                    req1 = requests[i]
                    req2 = requests[j]
                    common_venues = pair_venues[pair]

                    # Calculate overlapping time
                    scheduled_start, scheduled_end = calculate_overlapping_time(req1, req2)
                    if not scheduled_start:
                        logger.info("No overlapping time window, skipping pair")
                        continue

                    # Ensure minimum booking duration
                    if (scheduled_end - scheduled_start) < MINIMUM_BOOKING_DURATION:
                        logger.info("Overlap too short, skipping pair")
                        continue

                    # Generate match ID first
                    match_id = uuid.uuid4()

                    # Try to book a venue
                    booking_response = make_venue_booking_request(
                        common_venues,
                        scheduled_start,
                        scheduled_end,
                        match_id
                    )

                    if not booking_response or not booking_response.get('success'):
                        logger.error(f"Failed to book venue for match {match_id}")
                        continue

                    # Create match if booking succeeded
                    new_match = Match(
                        match_id=match_id,
                        team1_id=req1.team_id,
                        team2_id=req2.team_id,
                        scheduled_time=scheduled_start,
                        venue_id=uuid.UUID(booking_response['venueId']),
                        field_id=uuid.UUID(booking_response['fieldId']),
                        booking_id=uuid.UUID(booking_response['bookingId'])
                    )

                    matches.append(new_match)
                    logger.info(
                        f"Matched: {req1.team_id} vs {req2.team_id} | "
                        f"Venue: {booking_response['venueId']} | "
                        f"Field: {booking_response['fieldId']} | "
                        f"Time: {scheduled_start}"
                    )

                    # Update request statuses
                    session.query(MatchRequest).filter(
                        MatchRequest.team_id.in_([req1.team_id, req2.team_id])
                    ).update({'status': 'matched'}, synchronize_session=False)

            session.bulk_save_objects(matches)
            session.commit()

            # Log final matches in a table
            match_data = []
            for match in matches:
                match_data.append([
                    str(match.team1_id),
                    str(match.team2_id),
                    str(match.venue_id),
                    str(match.field_id),
                    match.scheduled_time.isoformat()
                ])

            if match_data:
                logger.info("\nCreated Matches:\n" +
                           tabulate(match_data,
                                    headers=["Team 1", "Team 2", "Venue ID", "Field ID", "Scheduled Time"],
                                    tablefmt="grid"))

            return jsonify({
                'message': f'Created {len(matches)} matches',
                'matches': [{
                    'match_id': str(m.match_id),
                    'team1': str(m.team1_id),
                    'team2': str(m.team2_id),
                    'venue_id': str(m.venue_id),
                    'field_id': str(m.field_id),
                    'booking_id': str(m.booking_id),
                    'time': m.scheduled_time.isoformat()
                } for m in matches]
            }), 200
        else:
            logger.info("No valid matches found by the solver")
            return jsonify({'message': 'No valid matches found'}), 200

    except Exception as e:
        session.rollback()
        logger.error(f"Error during matchmaking: {str(e)}", exc_info=True)
        return jsonify({'error': str(e)}), 500
    finally:
        session.close()
        logger.info("\nMatchmaking process completed\n" + "=" * 50 + "\n")


# Error Handlers
@app.errorhandler(requests.exceptions.RequestException)
def handle_venue_service_error(e):
    logger.error(f"Venue service communication failed: {str(e)}")
    return jsonify({
        'error': 'Venue service unavailable',
        'details': str(e)
    }), 503


if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)