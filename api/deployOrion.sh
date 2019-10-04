#!/bin/bash

echo "Pushing db"

docker save poppindb:latest | \
  bzip2 | \
  ssh hugo@blog.hugo-klepsch.tech 'bunzip2 | docker load'

echo "done"

echo "Pushing server"

docker save poppinserver:latest | \
  bzip2 | \
  ssh hugo@blog.hugo-klepsch.tech 'bunzip2 | docker load'

echo "done"

echo "Push start.sh"
cat start.sh | \
  bzip2 | \
  ssh hugo@blog.hugo-klepsch.tech \
    'bunzip2 > /home/hugo/poppin/start.sh && chmod u+x /home/hugo/poppin/start.sh'

echo "done"

echo "Push stop.sh"
cat stop.sh | \
  bzip2 | \
  ssh hugo@blog.hugo-klepsch.tech \
    'bunzip2 > /home/hugo/poppin/stop.sh && chmod u+x /home/hugo/poppin/stop.sh'

echo "done"
