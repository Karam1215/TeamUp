import psycopg2
import uuid
from datetime import datetime

# Connect to the PostgreSQL database
conn = psycopg2.connect(
    dbname="venue_db",
    user="postgres",
    password="postgres",
    host="localhost",
    port="5437"
)

cursor = conn.cursor()

def add_field_for_each_venue():
    # Retrieve all venues from the 'venues' table
    cursor.execute("SELECT venue_id, name FROM venues")
    venues = cursor.fetchall()

    for venue in venues:
        venue_id = venue[0]
        venue_name = venue[1]
        print(f"Processing venue: {venue_name}")

        # Define field data for field_1 (this could be dynamic or from your CSV, hardcoded for now)
        field_name = "Field_2"
        price_per_hour = 500.00  # Example value
        capacity = 10  # Example value
        created_at = datetime.now()

        try:
            # Insert field data for this venue
            cursor.execute("""
                INSERT INTO fields (
                    venue_id, name, price_per_hour, capacity, created_at
                ) VALUES (%s, %s, %s, %s, %s)
            """, (
                venue_id, field_name, price_per_hour, capacity, created_at
            ))

            print(f"Successfully added field: {field_name} for venue: {venue_name}")

        except Exception as e:
            print(f"Error inserting field for venue {venue_name}: {e}")
            conn.rollback()  # Rollback if there's an error

    conn.commit()

# Call the function to add fields for each venue
add_field_for_each_venue()

# Close the cursor and connection
cursor.close()
conn.close()
