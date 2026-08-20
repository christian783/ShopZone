#!/bin/sh
BASEDIR=$(cd "$(dirname "$0")" && pwd)
WRAPPER_JAR="$BASEDIR/.mvn/wrapper/maven-wrapper.jar"
exec java -classpath "$WRAPPER_JAR" "-Dmaven.multiModuleProjectDirectory=$BASEDIR" org.apache.maven.wrapper.MavenWrapperMain "$@"
