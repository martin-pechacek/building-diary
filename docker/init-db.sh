#!/bin/bash
set -e
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL
    CREATE USER photos_user WITH PASSWORD '123456!';
    CREATE DATABASE photos OWNER photos_user;
    CREATE USER notifications_user WITH PASSWORD 'notifications_pass';
    CREATE DATABASE notifications OWNER notifications_user;
EOSQL
