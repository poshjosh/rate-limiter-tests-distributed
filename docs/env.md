# Supported environment variables

```dotenv
# [auto|manual|off|remote]
APP_RATE_LIMIT_MODE=remote

# Connect from within a docker container to the host machine
APP_RATE_LIMITER_SERVICE_URL=http://host.docker.internal:8080

# Refers to the dir within the container. To see the actual dir on your
# local machine, checkout how we map it in our docker-compose.yml
APP_OUTPUT_DIR=/output

# JVM
JVM_XMS=[OPTIONAL]
JVM_XMX=[OPTIONAL]
JVM_GC=[OPTIONAL]

# Spring
SERVER_PORT="[OPTIONAL, default=5555]"
SPRING_REDIS_*=[OPTIONAL]
```
