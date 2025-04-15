import psycopg2
import folium

# Connect to the PostgreSQL database
conn = psycopg2.connect(
    dbname="venue_db",
    user="postgres",
    password="postgres",
    host="localhost",
    port="5437"
)

# Create a cursor object
cur = conn.cursor()

# Execute a query to fetch venue data
cur.execute("""
    SELECT name, address, latitude, longitude
    FROM venues;
""")

# Fetch all rows from the executed query
venues = cur.fetchall()

# Create a map centered around Moscow
m = folium.Map(location=[55.7558, 37.6176], zoom_start=12)

# Add markers for each venue from the query results
for venue in venues:
    name, address, latitude, longitude, = venue
    folium.Marker(
        location=[latitude, longitude],
        popup=f'<strong>{name}</strong><br>{address}',
        tooltip=name,
    ).add_to(m)

# Save the map as an HTML file
m.save("venues_map.html")

# Close the cursor and the connection
cur.close()
conn.close()

print("Map has been created and saved as 'venues_map.html'")
