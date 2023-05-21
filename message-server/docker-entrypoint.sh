#!/bin/bash
# print debug information
#set -ex

#########################################################
# Set JVM memory options if set as environment variables.
#########################################################
if [ -n "${JVM_XMS}" ]; then
  JAVA_OPTS="$JAVA_OPTS -Xms$JVM_XMS "
fi
if [ -n "$JVM_XMX" ]; then
  JAVA_OPTS="$JAVA_OPTS -Xmx$JVM_XMX "
fi
# shellcheck disable=SC1072
if [ -n "$JVM_GC" ]; then
  JAVA_OPTS="$JAVA_OPTS -XX:$JVM_GC"
fi
if [ -n "$JAVA_RANDOM" ]; then
  JAVA_OPTS="$JAVA_OPTS $JAVA_RANDOM"
fi
#########################################################
# set params
#########################################################

# shellcheck disable=SC1073
if [ -n  "$SERVER_PORT" ]; then
    PARAMS="$PARAMS --server.port=$SERVER_PORT "
fi
if [ -n  "$SPRING_PROFILES_ACTIVE" ]; then
    PARAMS="$PARAMS --spring.profiles.active=$SPRING_PROFILES_ACTIVE "
fi
if [ -n "$OTHER_PARAMS" ];  then
    PARAMS="$PARAMS $OTHER_PARAMS"
fi

exec  java $JAVA_OPTS -jar  app.jar $PARAMS
