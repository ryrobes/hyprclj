#!/bin/bash
# Quick script to run examples

set -e

EXAMPLE="${1:-simple}"  # Default to simple (demo has issues)

echo "Running example: $EXAMPLE"
echo "Make sure native library is built (cd native && ./build.sh)"
echo ""

cd "$(dirname "$0")"

# Check if library exists
if [ ! -f "resources/libhyprclj.so" ]; then
    echo "⚠️  Native library not found!"
    echo "Building native library..."
    cd native
    ./build.sh
    cd ..
fi

echo "Starting Clojure..."
clj -J-Djava.library.path=resources -J--enable-native-access=ALL-UNNAMED -M:examples -m "$EXAMPLE"
