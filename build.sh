#!/bin/bash
# One-step build script for Hyprclj

set -e

echo "=== Building Hyprclj ==="
echo ""

# Step 1: Compile Java classes
echo "Step 1/2: Compiling Java classes..."
mkdir -p target/classes
javac -d target/classes -cp "$(clj -Spath)" src/java/org/hyprclj/bindings/*.java
echo "✓ Java classes compiled to target/classes/"
echo ""

# Step 2: Build native library
echo "Step 2/2: Building native library..."
cd native
./build.sh
cd ..
echo "✓ Native library built to resources/libhyprclj.so"
echo ""

echo "=== Build Complete! ==="
echo ""
echo "Run examples with:"
echo "  ./run_example.sh simple"
echo "  ./run_example.sh reactive_counter"
echo "  ./run_example.sh demo"
