#!/bin/bash

docker-compose down

# We got port already in use a couple of times. So we sleep between shutdown and startup.
# Have not checked if this actually fixes the problem.
sleep 3

docker-compose up -d --build --scale message-server=3 --scale message-client=1 --scale redis-cache=1

docker exec rate-limiter-tests-distributed-redis-cache-1 sh \
-c 'redis-cli FLUSHALL && echo "SUCCESSFULLY FLUSHED CACHE" || echo "FAILED TO FLUSH CACHE"'
