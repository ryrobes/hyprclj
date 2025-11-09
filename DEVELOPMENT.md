# Development Guide

This document provides detailed information for developers working on Hyprclj.

## Architecture Deep Dive

### JNI Bridge

The JNI layer bridges Java and C++. Here's how it works:

1. **Java classes** (`org.hyprclj.bindings.*`) declare `native` methods
2. **C++ implementations** (`native/*.cpp`) implement these methods
3. **JNI_OnLoad** captures the JavaVM pointer for callbacks
4. **Native handles** are passed as `long` (jlong) values containing C++ pointers

### Memory Management

- **C++ side**: Uses Hyprtoolkit's smart pointers (`CSharedPointer`)
- **Java side**: Stores raw pointers wrapped in `new auto(shared_ptr)`
- **Clojure side**: No manual memory management
- **Callbacks**: Use `NewGlobalRef` to prevent GC, need cleanup (TODO)

### Reactive System

Inspired by Reagent:

```clojure
(def state (ratom 0))        ; Create reactive atom
@state                        ; Deref tracks dependency
(swap! state inc)            ; Update triggers watchers
```

**How it works:**

1. `ratom` creates a regular Clojure atom
2. `make-reactive` wraps component functions
3. During render, `track-derefs` captures atom reads
4. `add-watch` is used to trigger re-renders
5. Re-renders call component function again

**Limitations:**

- Currently re-renders don't diff/patch the UI tree
- Need to implement reconciliation (like React's VDOM)
- Watchers accumulate (need cleanup)

### Component Compilation

Hiccup specs are compiled to native elements:

```clojure
[:button {:label "Hi"}]
     ↓
(button {:label "Hi"})
     ↓
Button.builder().label("Hi").build()
     ↓
nativeCreate(...) in C++
     ↓
CButtonBuilder::begin()->label("Hi")->commence()
```

## Building from Source

### Prerequisites

```bash
# Ubuntu/Debian
sudo apt install build-essential cmake pkg-config default-jdk

# Arch
sudo pacman -S base-devel cmake jdk-openjdk

# Install hyprtoolkit (see main README)
```

### Build Steps

```bash
# 1. Build native library
cd native
mkdir -p build
cd build
cmake ..
make -j$(nproc)

# Library will be in ../resources/libhyprclj.so

# 2. Generate JNI headers (if you modify Java files)
cd ../../src/java
javac -h ../../native org/hyprclj/bindings/*.java

# 3. Test
cd ../..
clj -M -m simple
```

### Debugging Native Code

```bash
# Build with debug symbols
cd native/build
cmake -DCMAKE_BUILD_TYPE=Debug ..
make

# Run with GDB
gdb --args java -cp target:src/clojure clojure.main -m simple

# Or attach to running JVM
# Find PID: jps
# Attach: gdb -p <PID>
```

### Common Issues

**Library not found:**
```
UnsatisfiedLinkError: no hyprclj in java.library.path
```
Solution: Ensure `libhyprclj.so` is in `resources/` directory.

**Hyprtoolkit not found:**
```
error while loading shared libraries: libhyprtoolkit.so
```
Solution: Install hyprtoolkit or set `LD_LIBRARY_PATH`.

**JNI method not found:**
```
java.lang.UnsatisfiedLinkError: ... (Native method)
```
Solution: Regenerate headers and rebuild native library.

## Adding New Elements

To add a new element type (e.g., Checkbox):

### 1. Java Binding

Create `src/java/org/hyprclj/bindings/Checkbox.java`:

```java
public class Checkbox extends Element {
    private Checkbox(long handle) {
        super(handle);
    }

    public static class Builder {
        private String label = "";
        private boolean checked = false;
        // ... other fields

        public Builder label(String label) {
            this.label = label;
            return this;
        }

        public Checkbox build() {
            long handle = nativeCreate(label, checked);
            return new Checkbox(handle);
        }

        private static native long nativeCreate(String label, boolean checked);
    }

    public static Builder builder() {
        return new Builder();
    }

    public void setChecked(boolean checked) {
        nativeSetChecked(nativeHandle, checked);
    }

    private native void nativeSetChecked(long handle, boolean checked);
}
```

### 2. C++ Implementation

Create `native/hyprclj_checkbox.cpp`:

```cpp
#include <jni.h>
#include <hyprtoolkit/element/Checkbox.hpp>

using namespace Hyprtoolkit;

extern "C" {

JNIEXPORT jlong JNICALL
Java_org_hyprclj_bindings_Checkbox_00024Builder_nativeCreate(
    JNIEnv* env, jclass clazz, jstring label, jboolean checked) {

    const char* labelChars = env->GetStringUTFChars(label, nullptr);
    std::string labelStr(labelChars);
    env->ReleaseStringUTFChars(label, labelChars);

    auto builder = CCheckboxBuilder::begin();
    builder->label(std::move(labelStr));
    builder->checked(checked);

    auto checkbox = builder->commence();
    return reinterpret_cast<jlong>(new auto(checkbox));
}

JNIEXPORT void JNICALL
Java_org_hyprclj_bindings_Checkbox_nativeSetChecked(
    JNIEnv* env, jobject obj, jlong handle, jboolean checked) {

    auto checkbox = std::dynamic_pointer_cast<CCheckboxElement>(
        *reinterpret_cast<std::shared_ptr<IElement>*>(handle)
    );
    // Use rebuild() to update the checkbox state
    // ...
}

}
```

### 3. Update CMakeLists.txt

Add to `native/CMakeLists.txt`:

```cmake
set(SOURCES
    # ... existing sources
    hyprclj_checkbox.cpp
)
```

### 4. Clojure Wrapper

Add to `src/clojure/hyprclj/elements.clj`:

```clojure
(defn checkbox
  "Create a checkbox element.

   Options:
     :label    - Label text
     :checked  - Initial checked state
     :on-change - Change handler"
  [{:keys [label checked on-change] :or {label "" checked false}}]
  (let [builder (Checkbox/builder)]
    (.label builder label)
    ;; Set up change callback...
    (.build builder)))
```

### 5. Add to DSL

Update `src/clojure/hyprclj/dsl.clj`:

```clojure
(defn compile-element [spec]
  ;; ...
  (case tag
    :button (el/button props)
    :text (el/text props)
    :checkbox (el/checkbox props)  ; Add this
    ;; ...
  ))
```

### 6. Test

```clojure
;; In REPL or example file
[:checkbox {:label "Enable feature"
            :checked true
            :on-change #(println "Changed!")}]
```

## Testing

### Manual Testing

```bash
clj -M -m simple
clj -M -m demo
```

### Automated Testing (TODO)

```clojure
(ns hyprclj.core-test
  (:require [clojure.test :refer :all]
            [hyprclj.core :as hypr]))

(deftest backend-creation
  (testing "Backend can be created"
    (let [backend (hypr/create-backend!)]
      (is (some? backend)))))
```

## Performance Considerations

### JNI Overhead

- Each JNI call has overhead (~10-100ns)
- Batch operations when possible
- Avoid chatty JNI (many small calls)

### Reactive Updates

- Currently re-renders entire component tree
- Need to implement reconciliation
- Consider memoization for expensive components

### Memory Leaks

- Global refs in callbacks need cleanup
- Track and cleanup watches
- Consider weak references

## Future Enhancements

### 1. Reconciliation

Implement VDOM-like diffing:

```clojure
(defn reconcile! [old-tree new-tree element]
  ;; Compare old and new trees
  ;; Only update changed elements
  ;; Reuse unchanged elements
  )
```

### 2. Component Lifecycle

```clojure
(defcomponent my-component
  :mount (fn [props] ...)
  :update (fn [old-props new-props] ...)
  :unmount (fn [] ...))
```

### 3. Animation API

Expose Hyprtoolkit's animation system:

```clojure
(animate! element
  {:property :opacity
   :from 0.0
   :to 1.0
   :duration 300
   :easing :ease-in-out})
```

### 4. Hot Reload

Integrate with tools.namespace:

```clojure
(defn reload! []
  (require 'hyprclj.dsl :reload)
  (remount-all-components!))
```

## Resources

- [JNI Specification](https://docs.oracle.com/javase/8/docs/technotes/guides/jni/)
- [Hyprtoolkit Docs](https://github.com/hyprwm/hyprtoolkit)
- [Reagent Rationale](https://github.com/reagent-project/reagent/blob/master/doc/WhenDoComponentsUpdate.md)
- [Re-frame Architecture](https://github.com/day8/re-frame/blob/master/docs/README.md)
