#!/bin/bash

# JVM 최적화 옵션 설정
export JAVA_OPTS="-Xmx2g -Xms1g -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/tmp/"

echo "Starting Newsfeed Application with JVM optimization..."
echo "JVM Options: $JAVA_OPTS"

# Spring Boot 애플리케이션 실행
java $JAVA_OPTS -jar build/libs/*.jar

