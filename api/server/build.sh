#!/bin/bash

set -e

echo "Building..."

export BASE_DIR=/
. /venv/bin/activate
./lint.sh

echo "done"
