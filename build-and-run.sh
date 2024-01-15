#!/bin/bash
################ README ################
# - This script may be run with either of the following arguments:
# [auto|manual|off]

# - Run this script with each of the above arguments and compare the results.
#   - For example, first run in `auto` mode, select different setups until the
#   system fails for a setup. Then try that same setup in `off` mode.
# - After running this script, run the related tests in your browser by opening
# http://localhost:3333/ (use whatever port you set for the message-client in the
# docker-compose.yml file).
#
# The OUTPUT_DIR specified in this script refers to the dir within the container.
# To see the actual dir on you local machine, checkout how we map it in our docker-compose.yml
########################################

MVN_SETTING="$HOME/dev_looseboxes/.m2/settings.xml"

function cleanPackage() {

  cd "$1" || (echo "Dir not found: $1" && exit 1)

  mvn clean package -s $MVN_SETTING || (echo "mvn command failed for $1" && exit 1)

  cd .. || (echo "Could not change to parent dir of: $1" && exit 1)
}

cleanPackage "message-client"
cleanPackage "message-server"
cleanPackage "user-service"

DIR_INDEX=11

function run() {

  docker-compose down

  # We got port already in use a couple of times. So we sleep between shutdown and startup.
  sleep 3

  RATE_LIMIT_MODE="$1" \
      OUTPUT_DIR=/logs/tests/performance/"$DIR_INDEX" \
          docker-compose up -d --build --scale message-server=3 --scale message-client=1 --scale redis-cache=1

  docker exec rate-limiter-tests-distributed-redis-cache-1 sh \
      -c 'redis-cli FLUSHALL && echo "SUCCESSFULLY FLUSHED CACHE" || echo "FAILED TO FLUSH CACHE"'
}

# [auto|manual|off]
run "auto"
