#!/usr/bin/env bash
# Reset: wipe docker volumes and restart cleanly
set -e

echo "Stopping services..."
docker compose down -v

echo "Rebuilding and starting..."
docker compose up -d --build

echo "Reset complete. Services are starting up."
