import psycopg2
import random
import uuid
from datetime import datetime, timedelta
import json

# Connect to the match_database
conn = psycopg2.connect(
    dbname="matches_db",
    user="postgres",
    password="postgres",
    host="localhost",
    port="5439"
)
cur = conn.cursor()

# Connect to the venue_db
venue_conn = psycopg2.connect(
    dbname="venue_db",
    user="postgres",
    password="postgres",
    host="localhost",
    port="5437"
)
venue_cur = venue_conn.cursor()
venue_cur.execute("SELECT venue_id FROM venues;")
venue_uuids = [row[0] for row in venue_cur.fetchall()]

rankings = ['beginner', 'medium', 'advanced', 'world-class']

def random_time():
    start_time = datetime.combine(datetime.today(), datetime.min.time()) + timedelta(
        hours=random.randint(0, 22),
        minutes=random.randint(0, 59)
    )
    return start_time.time()

def random_preferred_venues():
    return random.sample(venue_uuids, random.randint(1, 5))

def random_day():
    return datetime.today() + timedelta(days=random.randint(0, 3))

for _ in range(400):
    team_id = uuid.uuid4()  # Create random UUID for team_id
    ranking = random.choice(rankings)
    preferred_start_date = random_day()
    preferred_start_time = random_time()
    preferred_end_time = (
        datetime.combine(preferred_start_date, preferred_start_time) +
        timedelta(hours=random.randint(1, 2))
    ).time()
    preferred_venues = random_preferred_venues()
    preferred_venues_json = json.dumps(preferred_venues)

    cur.execute("""
        INSERT INTO match_requests (
            request_id, team_id, ranking, preferred_start_time,
            preferred_end_time, preferred_venues, preferred_day
        )
        VALUES (%s, %s, %s, %s, %s, %s, %s);
    """, (
        str(uuid.uuid4()),
        str(team_id),  # Ensure team_id is a string
        ranking,
        preferred_start_time,
        preferred_end_time,
        preferred_venues_json,
        preferred_start_date.date()
    ))

conn.commit()
venue_cur.close()
venue_conn.close()
cur.close()
conn.close()

print("400 match requests have been successfully inserted into the match_database.")
