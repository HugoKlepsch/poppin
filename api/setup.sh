#!/bin/bash

export BASE_DIR=$(pwd)

cp git-hook-src/pre-push ../.git/hooks/pre-push

python3 -m venv venv

. venv/bin/activate

pip install -r server/requirements.txt

