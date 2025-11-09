#!/bin/bash
set -e

# Build script for hyprclj native library

echo "Building hyprclj native library..."

# Create build directory
mkdir -p build
cd build

# Configure with CMake
cmake ..

# Build
make -j$(nproc)

echo "Build complete! Library should be in ../resources/"
