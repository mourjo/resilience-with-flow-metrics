#!/bin/bash

# Define paths to each Maven project
APP1_DIR="/Users/mourjo/repos/monster-scale-2025/ls-server"
APP2_DIR="/Users/mourjo/repos/monster-scale-2025/nls-server"
APP3_DIR="/Users/mourjo/repos/monster-scale-2025/nls-client"
pwd
#mvn clean package -DskipTests

# Run app1 in the background and log output
echo "Starting ls-server..."
cd $APP1_DIR || exit 1
mvn spring-boot:run > ls-server.log 2>&1 &
LS_SERVER_PID=$!

# Run app2 in the background and log output
echo "Starting nls-server..."
cd $APP2_DIR || exit 1
mvn spring-boot:run > nls-server.log 2>&1 &
NLS_SERVER_PID=$!

# Run app3 in the background and log output
echo "Starting nls-client..."
cd $APP3_DIR || exit 1
mvn spring-boot:run > nls-client.log 2>&1 &
NLS_CLIENT_PID=$!

cleanup() {
  echo "Received SIGINT, killing background processes..."
  kill $LS_SERVER_PID
  echo "Killed LS Server"
  kill $NLS_SERVER_PID
  echo "Killed NLS Server"
  kill $NLS_CLIENT_PID
  echo "Killed NLS Client"
  exit 0
}

trap cleanup SIGINT

# Wait for all background processes to finish
wait $LS_SERVER_PID
wait $NLS_SERVER_PID
wait $NLS_CLIENT_PID

echo "Finished all processes"
