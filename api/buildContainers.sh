#!/bin/bash

set -e
set -x

echo "Building..."

docker build -t poppindb:latest db/

docker build -t poppinserver:latest server/

echo "done"
