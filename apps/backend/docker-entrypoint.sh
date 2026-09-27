#!/bin/sh
set -eu

media_dir="/data/media"
mkdir -p "$media_dir"
chown -R newsplatform:newsplatform "$media_dir"

exec setpriv --reuid=newsplatform --regid=newsplatform --init-groups \
  /opt/java/openjdk/bin/java ${JAVA_OPTS:-} -jar /app/app.jar
