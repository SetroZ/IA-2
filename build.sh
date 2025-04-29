#!/bin/bash
echo "Building..."

mkdir -p out

# Check if lib directory exists and has JAR files
if [ ! -d "lib" ] || [ -z "$(ls -A lib 2>/dev/null)" ]; then
  echo "Warning: lib directory is empty or missing. Please download required JAR files as explained in README.md"
  echo "Continuing build without dependencies..."
  LIB_PATH="."
else
  LIB_PATH="lib/*"
fi

echo "Libraries in lib directory:"
ls -la lib/

# Compile all Java files with dependencies in classpath
find src/main/java -name "*.java" > sources.txt
javac -cp "lib/jfreechart-1.5.3.jar:lib/jcommon-1.0.24.jar:out" -d out @sources.txt
BUILD_RESULT=$?
rm sources.txt

if [ $BUILD_RESULT -ne 0 ]; then
  echo "Build failed!"
  exit 1
fi

# Copy resource files
mkdir -p out/resources
if [ -d "src/main/resources" ]; then
  cp -r src/main/resources/* out/resources 2>/dev/null || :
fi

echo "Build successful!"
echo "Running..."
# Run Main class
java -cp "lib/jfreechart-1.5.3.jar:lib/jcommon-1.0.24.jar:out" Main