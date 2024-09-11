#!/usr/bin/env bash

set -euo pipefail

MVN_SETTINGS_FILE=${MVN_SETTINGS_FILE:-~/dev_looseboxes/.m2/settings.xml}
DEBUG=${DEBUG:-false}

cd .. || exit 1

export JAVA_HOME=~/.sdkman/candidates/java/17

if [ "${DEBUG}" = "true" ] || [ "$DEBUG" = true ]; then
    DEBUG="-X -e"
    echo "MVN_SETTINGS_FILE=$MVN_SETTINGS_FILE"
    echo "DEBUG=$DEBUG"
    echo "JAVA_HOME=$JAVA_HOME"
else
    DEBUG=
fi

# shellcheck disable=SC2086
# We disable the need for double quotes here, as using double quotes caused errors.
mvn clean verify -s "$MVN_SETTINGS_FILE" $DEBUG
echo "Build SUCCESSFUL"
