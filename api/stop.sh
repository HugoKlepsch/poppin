#!/bin/bash

set -e

echo "Stopping containers..."

docker kill poppindb poppinserver || true

echo "Deleting containers..."
docker rm poppindb poppinserver || true

echo "Deleting network..."
docker network prune -f

echo "done"
