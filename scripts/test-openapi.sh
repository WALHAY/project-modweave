#!/usr/bin/env bash
set -e

OPENAPI="build/generated/openapi/openapi.yaml"
PORT=4010

echo "Starting OpenAPI mock..."

npx --yes @stoplight/prism-cli mock \
  -p "$PORT" \
  "$OPENAPI" &

PRISM_PID=$!

trap 'kill "$PRISM_PID" 2>/dev/null || true' EXIT

sleep 3

echo "Running Arazzo..."

npx --yes @redocly/cli@2.58.2 respect \
  docs/arazzo.yaml \
  --server "modweave=http://localhost:$PORT"
