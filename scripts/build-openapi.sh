#!/usr/bin/env bash
set -e

OUTPUT="build/generated/openapi/openapi.yaml"

echo "Linting OpenAPI..."
npx --yes @redocly/cli@2.58.2 lint \
  --lint-config error

echo "Bundling OpenAPI..."
mkdir -p "$(dirname "$OUTPUT")"

npx --yes @redocly/cli@2.58.2 bundle \
  docs/openapi/openapi.yaml \
  --output "$OUTPUT"

echo "OpenAPI built: $OUTPUT"
