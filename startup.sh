#!/bin/sh

set -x
set -e

JAR=skylight-$1-0.0.1-SNAPSHOT-jar-with-dependencies.jar

echo "Starting $JAR"
java -jar /app/$JAR

