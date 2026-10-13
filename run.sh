#!/bin/sh
cd "$(dirname "$0")" || exit 1
java -cp "out" main.Main
