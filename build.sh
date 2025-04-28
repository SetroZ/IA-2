#!/bin/bash
echo "Building..."

mkdir -p out

# Compile all Java files
find src/main/java -name "*.java" > sources.txt
javac -d out @sources.txt
rm sources.txt

# Copy resource files
cp -r src/main/resources/* out/resources

echo "Running..."
java -cp out Main
