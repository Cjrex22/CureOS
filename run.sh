#!/bin/bash
# run.sh
# Make sure to place your sqlite-jdbc jar in the lib folder or update the classpath accordingly.

echo "Compiling CureOS source files..."
mkdir -p bin
javac -cp "lib/*:." src/*.java -d bin

if [ $? -eq 0 ]; then
    echo "Compilation successful. Starting CureOS..."
    java -cp "bin:lib/*:." Main
else
    echo "Compilation failed."
fi
