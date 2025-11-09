# Hyprclj Setup & Running Guide

## ✅ Build Status

All components have been successfully built and configured!

### What's Working:
- ✅ Native library compiled (`resources/libhyprclj.so`)
- ✅ Java classes compiled (`target/classes`)
- ✅ JNI integration functional
- ✅ Hyprtoolkit backend initializes
- ✅ Wayland connection established
- ✅ Graphics system detected

## Quick Start

### 1. One-Time Setup

```bash
# Compile Java classes (needed once, or after Java code changes)
mkdir -p target/classes
javac -d target/classes -cp "$(clj -Spath)" src/java/org/hyprclj/bindings/*.java

# Build native library (needed once, or after C++ code changes)
cd native && ./build.sh && cd ..
```

### 2. Run Examples

```bash
# Use the helper script
./run_example.sh simple
./run_example.sh reactive_counter
./run_example.sh demo

# Or run directly
clj -J-Djava.library.path=resources -J--enable-native-access=ALL-UNNAMED -M:examples -m simple
```

## Project Structure

```
hyprclj/
├── deps.edn                    # Clojure dependencies
├── src/
│   ├── clojure/hyprclj/        # Clojure source
│   └── java/org/hyprclj/       # Java JNI bindings
├── target/classes/             # Compiled Java classes (generated)
├── native/                     # C++ JNI implementation
│   └── build/                  # Build artifacts (generated)
├── resources/
│   └── libhyprclj.so          # Native library (generated)
└── examples/                   # Example apps
```

## Build Process

### Compile Java (when .java files change):

```bash
javac -d target/classes -cp "$(clj -Spath)" src/java/org/hyprclj/bindings/*.java
```

### Build Native Library (when .cpp files change):

```bash
cd native
./build.sh
cd ..
```

## Classpath Configuration

The `deps.edn` is configured with:

```clojure
{:paths ["src/clojure" "target/classes" "resources"]

 :aliases
 {:examples {:extra-paths ["examples"]}}}
```

- `src/clojure` - Clojure source code
- `target/classes` - Compiled Java classes
- `resources` - Contains `libhyprclj.so`
- `examples` - Example applications (via `:examples` alias)

## JVM Options

When running, we need these JVM flags:

- `-Djava.library.path=resources` - Tell JVM where to find `libhyprclj.so`
- `--enable-native-access=ALL-UNNAMED` - Allow JNI calls (Java 21+)

## Troubleshooting

### "Could not locate X.clj"
→ Use `-M:examples` to include examples directory on classpath

### "ClassNotFoundException: org.hyprclj.bindings.Backend"
→ Run `javac` to compile Java classes to `target/classes/`

### "UnsatisfiedLinkError: no hyprclj in java.library.path"
→ Use `-Djava.library.path=resources` JVM option

### "error while loading shared libraries: libhyprtoolkit.so"
→ Install hyprtoolkit system-wide or set `LD_LIBRARY_PATH`

### Compilation errors in native code
→ Check API_COMPATIBILITY_NOTES.md for known issues with hyprtoolkit versions

## Development Workflow

### Making Changes to Clojure Code:
Just edit and re-run - no rebuild needed!

### Making Changes to Java Code:
```bash
javac -d target/classes -cp "$(clj -Spath)" src/java/org/hyprclj/bindings/*.java
# Then re-run
```

### Making Changes to C++ Code:
```bash
cd native && ./build.sh && cd ..
# Then re-run
```

## Verification

Test that everything is working:

```bash
# 1. Check library exists
ls -lh resources/libhyprclj.so

# 2. Check Java classes compiled
ls -R target/classes/org/hyprclj/bindings/

# 3. Try running
./run_example.sh simple
```

Expected output should show:
- "Creating an Aquamarine backend!"
- "Connected to a wayland compositor"
- Various Wayland initialization messages

## What's Next

The POC successfully:
1. ✅ Compiles native JNI library
2. ✅ Loads Java bindings
3. ✅ Initializes Hyprtoolkit backend
4. ✅ Connects to Wayland

To actually see a window, the example code may need adjustment depending on how Hyprtoolkit 0.2.1 renders. Check the logs for any errors after "Connected to a wayland compositor".

## Files Generated During Build

- `target/classes/**/*.class` - Compiled Java bytecode
- `native/build/` - CMake build artifacts
- `resources/libhyprclj.so` - Native JNI library
- `.cpcache/` - Clojure dependency cache

These can be cleaned with:
```bash
rm -rf target/classes native/build .cpcache
```

## Success Indicators

When running an example, you should see:
1. No ClassNotFoundException
2. No UnsatisfiedLinkError
3. "[HT] DEBUG: Starting the Aquamarine backend!"
4. "[HT] DEBUG: Connected to a wayland compositor"

This means the full stack is working: Clojure → Java → JNI → C++ → Hyprtoolkit → Wayland! 🎉
