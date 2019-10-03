#!/bin/bash

set -e

echo "Stopping containers..."
docker kill poppinserver || true

echo "Deleting containers..."
docker rm poppinserver || true

sleep 1

echo "Starting server container..."
docker run --env-file server/env.env -d -p 1221:80 --net poppin --name poppinserver poppinserver:latest
set +x

echo "To see logs of server, type 'docker logs -f poppinserver'"
echo "View website at http://localhost:1221"
