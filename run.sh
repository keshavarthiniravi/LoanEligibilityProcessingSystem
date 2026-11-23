#!/bin/sh
cd "$(dirname "$0")"
mkdir -p out
javac -d out $(find src -name "*.java") || { echo "Compilation failed. Install JDK 17+."; exit 1; }
java -cp out com.loansystem.Main
