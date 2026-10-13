#!/bin/sh
cd "$(dirname "$0")" || exit 1
mkdir -p out
find src -name "*.java" > sources.txt
javac -encoding UTF-8 -d out @sources.txt
result=$?
rm -f sources.txt
if [ "$result" -ne 0 ]; then
  echo "Build failed"
  exit 1
fi
echo "Build successful"
