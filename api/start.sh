#!/bin/bash

set -e

./stop.sh

echo "Pruning old docker network..."
docker network prune -f

echo "Creating docker network..."
docker network create poppin

echo "Starting db container..."
docker run -d -p 5432:5432 --net poppin --name poppindb poppindb:latest

echo "Starting server container..."
docker run --env-file server/env.env -d -p 1221:80 --net poppin --name poppinserver poppinserver:latest
set +x

echo "To see logs of db, type 'docker logs -f poppindb'"
echo "To see logs of server, type 'docker logs -f poppinserver'"
echo "View website at http://localhost:1221"

