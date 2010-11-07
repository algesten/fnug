#!/bin/sh

export MAVEN_OPTS="-agentlib:jdwp=transport=dt_socket,address=8000,server=y,suspend=y $MAVEN_OPTS"
mvn jetty:run
