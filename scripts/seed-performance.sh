#!/usr/bin/env bash
set -euo pipefail

script_dir="$(cd "$(dirname "$0")" && pwd)"
repo_root="$(cd "$script_dir/.." && pwd)"
cd "$repo_root"

if [[ "${APP_ENVIRONMENT:-development}" == "production" || "${SPRING_PROFILES_ACTIVE:-}" == *prod* ]]; then
  echo "Refusing to seed performance data in production." >&2
  exit 1
fi

env_file="${COMPOSE_ENV_FILE:-.env}"
if [[ ! -f "$env_file" ]]; then
  env_file=".env.example"
fi
if docker compose version >/dev/null 2>&1; then
  compose=(docker compose --env-file "$env_file")
elif command -v docker-compose >/dev/null 2>&1; then
  compose=(docker-compose --env-file "$env_file")
else
  echo "Docker Compose plugin not found. Install Docker Desktop or Docker Compose v2." >&2
  exit 1
fi
"${compose[@]}" exec -T postgres sh -c 'psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" --set ON_ERROR_STOP=1 -f -' < "$script_dir/seed-performance.sql"
