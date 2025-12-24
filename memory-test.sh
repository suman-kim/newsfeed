#!/bin/bash

echo "=== 뉴스피드 애플리케이션 메모리 모니터링 ==="

# JVM 최적화 옵션
export JAVA_OPTS="-Xmx2g -Xms1g -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+PrintGC -XX:+PrintGCDetails -XX:+PrintGCTimeStamps"

echo "JVM 옵션: $JAVA_OPTS"
echo "시작 시간: $(date)"

# 메모리 사용량 모니터링 함수
monitor_memory() {
    while true; do
        echo "$(date) - 메모리 사용량:"
        ps aux | grep java | grep -v grep | awk '{print "PID: " $2 ", CPU: " $3 "%, MEM: " $4 "%, RSS: " $6/1024 "MB"}'
        sleep 30
    done
}

# 백그라운드에서 메모리 모니터링 시작
monitor_memory &
MONITOR_PID=$!

# 애플리케이션 실행
java $JAVA_OPTS -jar build/libs/*.jar

# 모니터링 프로세스 종료
kill $MONITOR_PID 2>/dev/null

echo "애플리케이션 종료 시간: $(date)"
