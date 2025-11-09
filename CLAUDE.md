# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Hyprclj is a **native Wayland GUI framework** for Clojure using JNI bindings to Hyprtoolkit (C++). This enables building native desktop apps with a Reagent-style reactive DSL, compiled to native widgets (no webview/Electron).

**Tech Stack:**
- Clojure DSL → Java JNI bindings → C++ glue code → Hyprtoolkit C++ → Wayland protocol → GPU rendering

## Build Commands

**Full build (Java + native):**
```bash
./build.sh
```

**Java only:**
```bash
mkdir -p target/classes
javac -d target/classes -cp "$(clj -Spath)" src/java/org/hyprclj/bindings/*.java
```

**Native library only:**
```bash
cd native && ./build.sh && cd ..
```

**Run examples:**
```bash
./run_example.sh <example-name>
# Examples: simple, todo-fixed, best-practice-layout, nesting-fully-responsive
```

**Manual run:**
```bash
clj -J-Djava.library.path=resources -J--enable-native-access=ALL-UNNAMED -M:examples -m <namespace>
```

## Architecture Layers

### 1. C++ JNI Layer (`native/*.cpp`)
- **Purpose:** Bridge Java to Hyprtoolkit C++ API
- **Files:** `hyprclj_backend.cpp`, `hyprclj_window.cpp`, `hyprclj_element.cpp`, `hyprclj_button.cpp`, `hyprclj_text.cpp`, `hyprclj_layouts.cpp`, `hyprclj_textbox.cpp`, `hyprclj_checkbox.cpp`, `hyprclj_rectangle.cpp`
- **Key pattern:** Each file implements JNI native methods (e.g., `Java_org_hyprclj_bindings_Window_nativeCreate`)
- **Memory management:** Uses `CSharedPointer` from Hyprutils, stored as `jlong` handles

### 2. Java JNI Bindings (`src/java/org/hyprclj/bindings/*.java`)
- **Purpose:** Expose C++ API to JVM with Java types
- **Pattern:** Builder pattern for element creation (e.g., `Window.Builder`, `Button.Builder`)
- **Files:** `Backend.java`, `Window.java`, `Element.java`, `Button.java`, `Text.java`, `ColumnLayout.java`, `RowLayout.java`, `Textbox.java`, `Checkbox.java`, `Rectangle.java`
- **Key:** All use `native` method declarations loaded via `System.loadLibrary("hyprclj")`

### 3. Clojure Element Wrappers (`src/clojure/hyprclj/elements.clj`)
- **Purpose:** Idiomatic Clojure API over Java bindings
- **Functions:** `button`, `text`, `column-layout`, `row-layout`, `textbox`, `checkbox`, `rectangle`
- **Utilities:** `add-child!`, `set-margin!`, `set-grow!`, `set-size!`, `set-align!`, `set-position-mode!`, `set-absolute-position!`

### 4. Hiccup DSL (`src/clojure/hyprclj/dsl.clj`)
- **Purpose:** Declarative UI with Reagent/Re-com style syntax
- **Key function:** `compile-element` - Converts `[:tag props & children]` to native elements
- **Mounting:** `mount!` - Attaches compiled UI to window root
- **NEW: `:children` prop** - Supports `[:v-box {:children (for ...)}]` syntax

### 5. Layout System (`src/clojure/hyprclj/layout_system.clj`)
- **Purpose:** Advanced layout containers with positioning
- **Components:** `v-box`, `h-box` (with `:position`, `:background`, `:border` support)
- **Helper:** `apply-element-props!` - Applies positioning/alignment after creation
- **Colored buttons:** `colored-button` - Layers rectangle behind button (only works at root level)

### 6. Core (`src/clojure/hyprclj/core.clj`)
- **Backend:** `create-backend!`, `enter-loop!`, `add-timer!`, `add-idle!`
- **Windows:** `create-window`, `open-window!`, `close-window!`, `window-size`, `root-element`
- **CRITICAL:** `enable-responsive-root!` - Handles window resize events with loop protection

### 7. Reactive System
- **`simple_reactive.clj`:** Basic `reactive-mount!` (clears and rebuilds on change)
- **`keyed_reactive.clj`:** Performance-optimized `reactive-mount-keyed!` (reuses elements by key)
  - **WARNING:** Keyed reactive has ordering issues - elements aren't reordered, only reconciled by key
  - **Best for:** Stable lists that don't reorder
  - **Use simple reactive for:** Lists that need correct ordering after mutations

## Critical Implementation Details

### Responsive Layouts (`core.clj:105-165`, `hyprclj_window.cpp:119-154`)

**The Problem:** Window resize events trigger in rapid succession, and remounting UI can trigger more resize events (feedback loop).

**The Solution:**
1. **Use resize signal data, NOT `pixelSize()`** - Signal provides actual drawable area (accounts for HiDPI)
   - Example: Signal=692x685, pixelSize=865x856, scale=1.25
2. **Loop protection** - `ignore-resize?` flag blocks events during remount + 50ms cooldown
3. **Queuing** - `pending-size` atom captures rapid resize events, timer uses latest
4. **Debouncing** - 150ms delay before remounting

**C++ side (`hyprclj_window.cpp:129`):**
```cpp
window->m_events.resized.listen([globalListener, window](const Vector2D& newSize) {
    // Use newSize (signal data), NOT window->pixelSize()!
```

### Positioning Modes

**Two positioning strategies:**

1. **Auto-layout (default):** Root element centers children
   - Used when: Omit `:position` or use `:position :auto`
   - Behavior: Content centered horizontally, top-aligned vertically
   - Size: Must set explicit `:size [w h]` for responsive layouts

2. **Absolute positioning:** Pin to (0, 0) top-left
   - Used when: `:position :absolute`
   - Implementation: `set-position-mode! 0` + `set-absolute-position! 0 0`
   - Size: Natural sizing (don't set explicit size, conflicts with absolute)

**In `mount!` (`dsl.clj:139-164`):**
```clojure
(if (= (:position opts) :absolute)
  props  ; Don't set size for absolute
  (assoc props :size [w h]))  ; Set size for centered
```

### Layering Constraints

**CRITICAL:** Absolute positioning layering **only works on root's direct children**, NOT inside v-box/h-box containers!

**Works:**
```clojure
(let [root (root-element window)]
  (add-child! root bg-rect)     ; Layer 1
  (add-child! root text-elem))  ; Layer 2 (on top)
```

**Doesn't work:**
```clojure
[:v-box {}
  [:rectangle ...]  ; Stacks as sibling
  [:text ...]]      ; Not layered, just stacked vertically
```

**Workaround for backgrounds:** Use thin rectangle separators between sections instead of backgrounds.

### Sizing Behavior

**Auto-sizing:**
- Text, buttons: Size to content
- Containers without `:size`: Collapse to minimum

**Explicit sizing:**
- Required for: Reactive containers, rectangles, nested layouts
- Format: `:size [width height]` or `:size [width -1]` for auto-height

**Rectangle clipping issue:**
- Sized rectangles in sized containers stack vertically
- Rectangle takes up its height, pushing content below container bounds
- Solution: Don't use background rectangles in constrained containers

### Reactive Rendering

**Two modes:**

1. **`reactive-mount!`** - Full rebuild on change
   - Use for: Small lists, correct ordering critical
   - Performance: O(n) rebuild every time

2. **`reactive-mount-keyed!`** - Reconciles by key
   - Use for: Large stable lists (performance)
   - **BUG:** Doesn't handle reordering - elements matched by key but not repositioned
   - **Workaround:** Use `sort-by` to ensure stable order, or use `reactive-mount!`

### Color Format

All colors use `[r g b alpha]` where values are 0-255:
- `[255 0 0 255]` - Opaque red
- `[100 150 255 120]` - Semi-transparent blue (alpha=120/255 ≈ 47%)
- `[0 0 0 0]` - Fully transparent (invisible)

## Common Patterns

### Responsive Window

```clojure
(defn ui-component [[w h]]
  [:v-box {:position :absolute :gap 15 :margin 20}
   [:text {:content (str "Size: " w "x" h)}]
   [:rectangle {:border-color [100 150 200 80] :size [(- w 50) 2]}]])

(enable-responsive-root! window ui-component {:position :absolute})
```

### Programmatic Children

```clojure
;; NEW clean syntax
[:v-box {:gap 5
         :children (for [item items]
                     [:text {:content item}])}]

;; OLD verbose syntax (still works)
(into [:v-box {:gap 5}]
      (for [item items]
        [:text {:content item}]))
```

### Border Separators

```clojure
[:rectangle {:color [0 0 0 0]           ; Transparent
             :border-color [r g b alpha]
             :border 1
             :size [width 2]}]           ; Thin horizontal line
```

## When Adding New Elements

1. **Add Java binding** in `src/java/org/hyprclj/bindings/ElementName.java`
   - Extend `Element` class
   - Use Builder pattern
   - Declare `native` methods

2. **Implement C++ JNI** in `native/hyprclj_elementname.cpp`
   - Include headers: `<hyprtoolkit/element/ElementName.hpp>`
   - Implement `Java_org_hyprclj_bindings_ElementName_...` functions
   - Use `CSharedPointer` for memory management

3. **Add to CMakeLists.txt** in `native/CMakeLists.txt`
   - Add source file to `SOURCES` list

4. **Add Clojure wrapper** in `src/clojure/hyprclj/elements.clj`
   - Constructor function with props destructuring
   - Apply margin/grow/size helpers if needed

5. **Register in DSL** in `src/clojure/hyprclj/dsl.clj`
   - Add case in `compile-element`: `:element-name (el/element-name final-props)`

6. **Build and test:**
   ```bash
   ./build.sh
   ./run_example.sh <test-example>
   ```

## Known Issues & Gotchas

- **HiDPI:** Always use resize signal data, not `pixelSize()` (off by scale factor)
- **Emojis:** Can cause encoding issues in text - avoid or test carefully
- **Button sizing:** Buttons MUST have explicit `:size [w h]`, don't auto-size
- **Container sizing:** Containers without size collapse - set explicit sizes for predictable layouts
- **Keyed reactive:** Has ordering bugs - prefer `reactive-mount!` for lists that reorder
- **Window size before open:** `window-size` returns `[0 0]` before `open-window!` - use known dimensions
