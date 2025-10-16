#!/bin/bash
set -e

# Wait until PostgreSQL accepts connections
until pg_isready -U postgres > /dev/null 2>&1; do
  echo "Waiting for PostgreSQL to start..."
  sleep 2
done

echo "✅ PostgreSQL is up. Creating databases..."
psql -U postgres -d postgres -f /docker-entrypoint-initdb.d/00-create-databases.sql

echo "✅ Populating scholarspace_users..."
psql -U postgres -d scholarspace_users -f /docker-entrypoint-initdb.d/01-users-dump.sql

echo "✅ Populating scholarspace_institutions..."
psql -U postgres -d scholarspace_institutions -f /docker-entrypoint-initdb.d/02-institutions-dump.sql

echo "✅ Populating scholarspace_courses..."
psql -U postgres -d scholarspace_courses -f /docker-entrypoint-initdb.d/03-courses-dump.sql

echo "✅ All databases have been initialized successfully!"
