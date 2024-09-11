#!/usr/bin/env bash

set -euo pipefail

#################################### README #####################################
# - This script may be run with environment variable `APP_RATE_LIMIT_MODE` having
# either of the following values: [auto|manual|off|remote].

# - Run this script with each of the above values and compare the results.
#   - For example, first run in `auto` mode, select different setups until a
#   setup leads to the system failing. Then try that same setup in `off` mode.
# - After running this script, run the related tests in your browser by opening
# http://localhost:3333/ (use whatever port you set for the message-client in the
# docker-compose.yml file).
#################################################################################

docker-compose down

# We got port already in use a couple of times.
# So we sleep between shutdown and startup.
sleep 3

docker-compose up -d --build --scale message-server=3 --scale message-client=1 --scale redis-cache=1

docker exec rate-limiter-tests-distributed-redis-cache-1 sh \
    -c 'redis-cli FLUSHALL && echo "SUCCESSFULLY FLUSHED CACHE" || echo "FAILED TO FLUSH CACHE"'
