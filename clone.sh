#!/bin/bash

dir="$( cd "$( dirname "$0" )" && pwd )"
jar=`ls "$dir"/target/config-cloner-*-jar-with-dependencies.jar 2> /dev/null` || true
if [ ! -f "$jar" ]; then
  echo "Project not build, building it now..." >&2
  mvn clean package -B -DskipTests=true -f $dir/pom.xml > /dev/null
  jar=`ls "$dir"/target/config-cloner-*-jar-with-dependencies.jar`
fi

if [[ "$JAVA_OPTS" != *"javax.net.ssl.trustStore"* && -f "$dir/cacerts" ]]; then
  JAVA_OPTS="$JAVA_OPTS -Djavax.net.ssl.trustStore=$dir/cacerts"
fi

java $JAVA_OPTS -jar $jar "$@"
