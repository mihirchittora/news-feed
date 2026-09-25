#!/usr/bin/env bash
set -euo pipefail

api_url="${API_URL:-http://localhost:8080}"
smoke_dir="$(mktemp -d)"
trap 'rm -rf "$smoke_dir"' EXIT

smoke_email="milestone8-smoke-$(date +%s)-$$@example.invalid"
smoke_password='SmokePass123!'

health_status="$(curl -sS -D "$smoke_dir/health.headers" -o "$smoke_dir/health.json" -w '%{http_code}' "$api_url/actuator/health/readiness")"
[ "$health_status" = '200' ]
grep -qi '^X-Request-Id:' "$smoke_dir/health.headers"

register_status="$(curl -sS -o "$smoke_dir/register.json" -w '%{http_code}' \
  -H 'Content-Type: application/json' \
  -d "{\"name\":\"Milestone Smoke\",\"email\":\"$smoke_email\",\"password\":\"$smoke_password\"}" \
  "$api_url/api/v1/auth/register")"
[ "$register_status" = '201' ] || [ "$register_status" = '409' ]

login_status="$(curl -sS -o "$smoke_dir/login.json" -w '%{http_code}' \
  -H 'Content-Type: application/json' \
  -d "{\"email\":\"$smoke_email\",\"password\":\"$smoke_password\"}" \
  "$api_url/api/v1/auth/login")"
[ "$login_status" = '200' ]
token="$(jq -er '.accessToken' "$smoke_dir/login.json")"

me_status="$(curl -sS -o "$smoke_dir/me.json" -w '%{http_code}' \
  -H "Authorization: Bearer $token" "$api_url/api/v1/users/me")"
[ "$me_status" = '200' ]

dashboard_status="$(curl -sS -o "$smoke_dir/dashboard.json" -w '%{http_code}' \
  -H "Authorization: Bearer $token" "$api_url/api/v1/admin/dashboard")"
[ "$dashboard_status" = '403' ]

validation_status="$(curl -sS -D "$smoke_dir/validation.headers" -o "$smoke_dir/validation.json" -w '%{http_code}' \
  -H 'Content-Type: application/json' \
  -H 'X-Request-Id: smoke-validation' \
  -d '{"email":"not-an-email","password":"x"}' \
  "$api_url/api/v1/auth/login")"
[ "$validation_status" = '400' ]
jq -e '.code == "VALIDATION_ERROR" and .requestId == "smoke-validation"' "$smoke_dir/validation.json" >/dev/null

printf 'critical API smoke passed: health=%s register=%s login=%s me=%s user_dashboard=%s validation=%s\n' \
  "$health_status" "$register_status" "$login_status" "$me_status" "$dashboard_status" "$validation_status"
