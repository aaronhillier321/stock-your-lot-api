#!/usr/bin/env bash
# Start Cloud SQL Auth Proxy to connect to GCP PostgreSQL from your machine.
#
# Required: set these env vars (or edit the defaults below).
#   CLOUD_SQL_PROJECT_ID   - GCP project ID
#   CLOUD_SQL_REGION       - e.g. us-west1
#   CLOUD_SQL_INSTANCE     - instance name (e.g. stock-your-lot-db)
#
# Optional:
#   CLOUD_SQL_PORT         - local port (default 5433)
#   CLOUD_SQL_PRIVATE_IP   - set to "true" if instance has only private IP (no public IP)
#                            For private IP you must be on a network that can reach the VPC (VPN or run proxy from a VM in the VPC).
#
# Then run: ./scripts/start-cloud-sql-proxy.sh
# Connect with: psql "host=127.0.0.1 port=${CLOUD_SQL_PORT:-5432} user=YOUR_USER dbname=YOUR_DB sslmode=disable"

set -e

PROJECT_ID="stock-your-lot"
REGION="us-west1"
INSTANCE="stock-your-lot"
PORT="${CLOUD_SQL_PORT:-5433}"
USE_PRIVATE_IP="${CLOUD_SQL_PRIVATE_IP:-false}"

if [[ -z "$PROJECT_ID" || -z "$REGION" || -z "$INSTANCE" ]]; then
  echo "Set CLOUD_SQL_PROJECT_ID, CLOUD_SQL_REGION, and CLOUD_SQL_INSTANCE."
  echo "Example:"
  echo "  export CLOUD_SQL_PROJECT_ID=my-project"
  echo "  export CLOUD_SQL_REGION=us-west1"
  echo "  export CLOUD_SQL_INSTANCE=stock-your-lot-db"
  echo "  ./scripts/start-cloud-sql-proxy.sh"
  exit 1
fi

CONNECTION_NAME="${PROJECT_ID}:${REGION}:${INSTANCE}"

if ! command -v cloud-sql-proxy &> /dev/null; then
  echo "cloud-sql-proxy not found. Install it:"
  echo "  brew install cloud-sql-proxy"
  echo "Or see: https://cloud.google.com/sql/docs/postgres/connect-auth-proxy#install"
  exit 1
fi

echo "Starting Cloud SQL Auth Proxy -> $CONNECTION_NAME on port $PORT (private-ip=$USE_PRIVATE_IP)"
echo "Connect with: host=127.0.0.1 port=$PORT (use your DB user and database name)"
echo "Press Ctrl+C to stop."
# Force project so ADC/quota project doesn't use a different project (e.g. mindful-app-1)
export GOOGLE_CLOUD_PROJECT="$PROJECT_ID"

PRIVATE_IP_FLAG=""
if [[ "$USE_PRIVATE_IP" == "true" || "$USE_PRIVATE_IP" == "1" ]]; then
  PRIVATE_IP_FLAG="--private-ip"
fi

exec cloud-sql-proxy $PRIVATE_IP_FLAG --port="$PORT" "$CONNECTION_NAME"
