#!/bin/sh
set -eu

for database in $(printf '%s' "$POSTGRES_MULTIPLE_DATABASES" | tr ',' ' '); do
  echo "Creating database: $database"
  psql --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" --set ON_ERROR_STOP=1 \
    --command "CREATE DATABASE \"$database\""
done

