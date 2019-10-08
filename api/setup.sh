#!/bin/bash

export BASE_DIR=$(pwd)

if [[ ! -f "${BASE_DIR}/setup.sh" ]];
then
  echo "~~~~~~"
  echo "FAILED"
  echo "~~~~~~"
  echo "Please source setup.sh from the api/ directory"
  printf "\tcd api && source setup.sh\n"
  return 1
fi

cp git-hook-src/pre-push ../.git/hooks/pre-push

python3 -m venv venv

. venv/bin/activate

pip install -r server/requirements.txt

