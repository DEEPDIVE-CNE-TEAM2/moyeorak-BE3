#!/bin/bash

APP_NAME="moyeorak-0.0.1-SNAPSHOT.jar"
APP_DIR="/opt/moyeorak-BE2"
APP_PATH="$APP_DIR/build/libs/$APP_NAME"

cd $APP_DIR || exit

echo ">>> 최신 코드 가져오기 (git pull)"
git pull origin dev || { echo "git pull 실패"; exit 1; }

echo ">>> 프로젝트 빌드 시작"
./gradlew build || { echo "빌드 실패"; exit 1; }

echo ">>> 현재 실행중인 애플리케이션 PID 확인"
PID=$(ps -ef | grep "$APP_NAME" | grep -v grep | awk '{print $2}')

if [ -n "$PID" ]; then
    echo ">>> 실행중인 프로세스 종료 (PID: $PID)"
    kill -9 "$PID"
else
    echo ">>> 실행중인 프로세스 없음"
fi

echo ">>> 애플리케이션 실행 시작"
nohup java -jar "$APP_PATH" > nohup.out 2>&1 &

echo ">>> 배포 완료 (PID: $!)"
tail -f nohup.out
