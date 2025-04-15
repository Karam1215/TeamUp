import csv
import uuid
from datetime import datetime
import psycopg2

conn = psycopg2.connect(
    dbname="venue_db",
    user="postgres",
    password="postgres",
    host="localhost",
    port="5437"
)

cursor = conn.cursor()

with open("moscow_football_fields.csv", newline='', encoding='utf-8') as csvfile:
    reader = csv.reader(csvfile)

    # Skip the header row
    next(reader)

    for row in reader:
        # Ensure the row is not empty before processing
        if not row:
            continue

        venue_uuid = uuid.uuid4()
        name = row[1]
        address = row[2]
        region = "Москва"  # or extract from address

        # Handle latitude and longitude conversion errors
        try:
            latitude = float(row[3]) if row[3] and row[3] != 'lat' else None
        except ValueError:
            latitude = None
            print(f"Invalid latitude value in row {row[1]}: {row[3]}")

        try:
            longitude = float(row[4]) if row[4] else None
        except ValueError:
            longitude = None
            print(f"Invalid longitude value in row {row[1]}: {row[4]}")

        # Handle email field (if it doesn't have a valid email, set to NULL)
        email = row[5] if "@" in row[5] else None

        phone_number = row[6] if row[6] else None
        description = row[7] if row[7] else None

        # Set fixed opening and closing times
        opening_time = "08:00"
        closing_time = "23:00"

        created_at = datetime.now()

        try:
            cursor.execute("""
                INSERT INTO venues (
                    venue_id, name, address, region, latitude, longitude,
                    description, phone_number, email, opening_time, closing_time, created_at
                ) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
                ON CONFLICT (name) DO NOTHING
            """, (
                str(venue_uuid), name, address, region, latitude, longitude,
                description, phone_number, email, opening_time, closing_time, created_at
            ))
        except Exception as e:
            print(f"Error inserting row {row[1]}: {e}")

conn.commit()
cursor.close()
conn.close()
