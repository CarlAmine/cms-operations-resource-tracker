#!/usr/bin/env bash
# Seed script: hits the backend seed endpoint with admin credentials
set -e

BASE_URL=${BACKEND_URL:-http://localhost:8080}

echo "Seeding database via $BASE_URL ..."
curl -s -X POST "$BASE_URL/api/admin/seed" \
  -H "Content-Type: application/json" \
  -d '{"adminSecret": "seed-admin-2024"}' | jq .

echo "Seed complete."
