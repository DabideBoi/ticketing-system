#!/usr/bin/env bash
# Runs the whole stack for local development: Postgres (docker), the Spring Boot
# backend, and the Next.js frontend. Ctrl+C stops the backend/frontend; Postgres
# is left running (stop it separately with `docker compose down`).
#
# Env overrides:
#   SKIP_DB=1            Skip Postgres and run the backend against in-memory H2 instead.
#   DOCKER_CMD="sudo docker"   Use if your docker requires sudo.
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

DOCKER_CMD="${DOCKER_CMD:-docker}"
SKIP_DB="${SKIP_DB:-0}"

if [ -f .env ]; then
  set -a
  source .env
  set +a
fi

PIDS=()

cleanup() {
  echo ""
  echo "==> Shutting down backend and frontend..."
  for pid in "${PIDS[@]:-}"; do
    kill "$pid" 2>/dev/null || true
  done
  wait 2>/dev/null || true
}
trap cleanup EXIT INT TERM

PROFILE="dev"

if [ "$SKIP_DB" != "1" ]; then
  echo "==> Starting Postgres (docker compose)..."
  $DOCKER_CMD compose up -d postgres

  echo "==> Waiting for Postgres to become healthy..."
  until [ "$($DOCKER_CMD inspect -f '{{.State.Health.Status}}' ticketing-postgres 2>/dev/null)" = "healthy" ]; do
    sleep 1
  done
  echo "==> Postgres is healthy."
  PROFILE="prod"
else
  echo "==> SKIP_DB=1 set — backend will use the in-memory H2 dev profile instead of Postgres."
fi

echo "==> Starting backend (Spring Boot, '${PROFILE}' profile) on http://localhost:8080 ..."
(
  cd backend
  SPRING_PROFILES_ACTIVE="$PROFILE" mvn -q spring-boot:run
) &
PIDS+=("$!")

echo "==> Starting frontend (Next.js dev server) on http://localhost:3000 ..."
(
  cd frontend
  npm run dev
) &
PIDS+=("$!")

echo ""
echo "Backend:  http://localhost:8080"
echo "Frontend: http://localhost:3000"
echo "Press Ctrl+C to stop the backend and frontend."
echo ""

wait
