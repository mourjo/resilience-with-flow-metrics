#!/bin/bash

# Define paths to each Maven project
APP1_DIR="$(pwd)/ls-server"
APP2_DIR="$(pwd)/nls-server"
APP3_DIR="$(pwd)/nls-client"
APP4_DIR="$(pwd)/ls-client"

mvn clean install -DskipTests

echo "Starting ls-server..."
cd $APP1_DIR || exit 1
mvn spring-boot:run > ls-server.log 2>&1 &
LS_SERVER_PID=$!

echo "Starting nls-server..."
cd $APP2_DIR || exit 1
mvn spring-boot:run > nls-server.log 2>&1 &
NLS_SERVER_PID=$!

sleep 5;

echo "Starting nls-client..."
cd $APP3_DIR || exit 1
mvn spring-boot:run > nls-client.log 2>&1 &
NLS_CLIENT_PID=$!

echo "Starting ls-client..."
cd $APP4_DIR || exit 1
mvn spring-boot:run > ls-client.log 2>&1 &
LS_CLIENT_PID=$!

cleanup() {
  echo "Received SIGINT, killing background processes..."
  kill $LS_SERVER_PID
  echo "Killed LS Server"
  kill $NLS_SERVER_PID
  echo "Killed NLS Server"
  kill $NLS_CLIENT_PID
  echo "Killed NLS Client"
  kill $LS_CLIENT_PID
  echo "Killed LS Client"
  exit 0
}

trap cleanup SIGINT

# Wait for all background processes to finish
wait $LS_SERVER_PID
wait $NLS_SERVER_PID
wait $NLS_CLIENT_PID
wait $LS_CLIENT_PID

echo "Finished all processes"
