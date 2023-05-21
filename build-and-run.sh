#!/bin/bash

MVN_SETTING="$HOME/dev_looseboxes/.m2/settings.xml"

function cleanPackage() {
  cd "$1" || (echo "Dir not found: $1" && exit 1)

  mvn clean package -s $MVN_SETTING || (echo "mvn command failed for $1" && exit 1)

  cd .. || (echo "Could not change to parent dir of: $1" && exit 1)
}

cleanPackage "message-client"
cleanPackage "message-server"
cleanPackage "user-service"

docker-compose down

# We got port already in use a couple of times. So we sleep between shutdown and startup.
sleep 3

# TODO - Set message-server=3
# rate-limit-mode=[auto|manual|off]
RATE_LIMIT_MODE=off \
    OUTPUT_DIR=/rate-limiter/logs/tests/performance/5 \
        docker-compose up -d --build --scale message-server=1 --scale message-client=1 --scale redis-cache=1

docker exec rate-limiter-tests-distributed-redis-cache-1 sh \
-c 'redis-cli FLUSHALL && echo "SUCCESSFULLY FLUSHED CACHE" || echo "FAILED TO FLUSH CACHE"'
