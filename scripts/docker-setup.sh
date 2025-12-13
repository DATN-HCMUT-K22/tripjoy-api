# Start all
docker-compose up -d

# Verify
docker ps
docker exec -it tripjoy-redis redis-cli -a TripJoySecurePass2025 ping
docker exec -it tripjoy-postgis psql -U postgres -d tripjoy

# Stop all
docker-compose down