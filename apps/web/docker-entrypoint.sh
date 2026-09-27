#!/bin/sh
set -eu

runtime_api_url="${NEXT_PUBLIC_API_URL:-http://localhost:8080}"
runtime_site_url="${SITE_URL:-${NEXT_PUBLIC_SITE_URL:-http://localhost:3000}}"

printf 'window.__NEWS_PLATFORM_CONFIG__={"apiBaseUrl":"%s","siteUrl":"%s"};\n' "$runtime_api_url" "$runtime_site_url" > /app/public/runtime-config.js
exec node /app/server.js
