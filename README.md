# Hyprclj

> Clojure bindings for [Hyprtoolkit](https://github.com/hyprwm/hyprtoolkit) - Build native Wayland GUI apps in Clojure!

Hyprclj provides idiomatic Clojure bindings to Hyprtoolkit, enabling you to write native Wayland applications using a Reagent-style reactive DSL.

## Features

- 🎨 **Hiccup-style DSL** - Familiar syntax for Clojure developers
- ⚛️ **Reactive State** - Reagent-like atoms and automatic UI updates
- 🚀 **Native Performance** - Direct JNI bindings to C++ (no webview!)
- 🎯 **Wayland-Native** - Built for the modern Linux desktop
- 🧩 **Composable Layouts** - Flexbox-style v-box/h-box composition
- 📐 **Responsive Layouts** - Windows resize correctly with HiDPI support
- 🎨 **Styled Components** - Colored backgrounds, borders, rounded corners
- ⚡ **Optimized Reactivity** - Partial updates (only changed sections repaint)

## Status

✅ **Responsive Layouts Working!** - Window resizing now works correctly!

✅ **Production-Ready Features:**
- Responsive window layouts with proper sizing
- Nested v-box/h-box composition (flexbox-style)
- Colored rectangles for backgrounds and borders
- Optimized partial repainting for reactive UIs
- Full keyboard and mouse input support
- Text input with inline editing

⚠️ **Experimental** - This is an active POC. Some advanced features still in development.

## Requirements

- **OS**: Linux with Wayland
- **JDK**: 11 or higher
- **Clojure**: 1.11+
- **Hyprtoolkit**: 0.2.4+ (must be installed on your system)
- **Build tools**: CMake 3.20+, g++ with C++23 support

## Installation

### 1. Install Hyprtoolkit

First, install hyprtoolkit on your system:

```bash
# Using your package manager, or build from source:
git clone https://github.com/hyprwm/hyprtoolkit
cd hyprtoolkit
cmake -B build -DCMAKE_INSTALL_PREFIX=/usr
cmake --build build
sudo cmake --install build
```

### 2. Build Project

```bash
# Compile Java classes
mkdir -p target/classes
javac -d target/classes -cp "$(clj -Spath)" src/java/org/hyprclj/bindings/*.java

# Build native library
cd native && ./build.sh && cd ..
```

This will compile the JNI bridge and place `libhyprclj.so` in the `resources/` directory.

### 3. Run Examples

```bash
# Use the helper script (defaults to 'simple')
./run_example.sh

# Or specify example
./run_example.sh simple

# Or run manually
clj -J-Djava.library.path=resources -J--enable-native-access=ALL-UNNAMED -M:examples -m simple
```

**Working Examples**:

**Basic Examples:**
- ✅ `simple` - Basic static UI
- ✅ `interactive-test` - Button click testing
- ✅ `reactive-counter` - Reactive state demo

**Responsive Layout Examples:**
- ✅ `test-absolute-final` - Absolute positioning (top-left)
- ✅ `test-centered-final` - Auto-layout positioning (centered)
- ✅ `nesting-fully-responsive` - Nested v-box/h-box layouts
- ✅ `best-practice-layout` - Complete app with border separators

**Advanced Examples:**
- ✅ `todo-fixed` - Full TODO app with colors, inline editing, optimized reactivity
- ✅ `colored-buttons-test` - Colored button demonstration
- ✅ `children-prop-test` - :children prop for programmatic UI

All examples support proper window closing with Hyprland's close command (`Mod+Shift+C`)!

## Quick Start

### Responsive Layout (NEW!)

```clojure
(ns my-app
  (:require [hyprclj.core :as hypr]
            [hyprclj.dsl :refer [mount!]]
            [hyprclj.util :as util]))

(defn ui-component
  "UI component that adapts to window size"
  [[w h]]  ; Receives [width height] for responsiveness!
  [:v-box {:gap 15 :margin 20 :position :absolute}
   [:text {:content "My Responsive App" :font-size 24}]
   [:text {:content (str "Window: " w "x" h) :font-size 12}]

   ;; Border separator (resizes with window!)
   [:rectangle {:color [0 0 0 0]
                :border-color [100 150 200 80]
                :border 1
                :size [(- w 50) 2]}]

   [:h-box {:gap 10}
    [:button {:label "Click" :size [100 30] :on-click #(println "Hi!")}]
    [:button {:label "Me" :size [100 30] :on-click #(println "Hello!")}]]])

(defn -main []
  (hypr/create-backend!)
  (let [window (hypr/create-window {:title "App" :size [700 500]
                                     :on-close (fn [_] (util/exit-clean!))})]
    ;; Enable responsive resizing!
    (hypr/enable-responsive-root! window ui-component {:position :absolute})
    (hypr/open-window! window)
    (hypr/enter-loop!)))
```

### Simple Static UI

```clojure
(ns my-app
  (:require [hyprclj.core :as hypr]
            [hyprclj.dsl :refer [mount!]]))

(defn -main []
  (hypr/create-backend!)
  (let [window (hypr/create-window {:title "My App" :size [400 300]})]
    (mount! (hypr/root-element window)
            [:column {:gap 10 :margin 20}
             [:text {:content "Hello, Hyprland!"
                     :font-size 24}]
             [:button {:label "Click me!"
                       :on-click #(println "Clicked!")}]])
    (hypr/open-window! window)
    (hypr/enter-loop!)))
```

### Reactive UI with State

```clojure
(ns my-app
  (:require [hyprclj.core :as hypr]
            [hyprclj.dsl :refer [ratom mount! defcomponent]]))

(def counter (ratom 0))

(defcomponent counter-view []
  [:column {:gap 10}
   [:text {:content (str "Count: " @counter)}]
   [:button {:label "Increment"
             :on-click #(swap! counter inc)}]])

(defn -main []
  (hypr/create-backend!)
  (let [window (hypr/create-window {:title "Counter"})]
    (mount! (hypr/root-element window) [counter-view])
    (hypr/open-window! window)
    (hypr/enter-loop!)))
```

## Architecture

```
┌──────────────────────────────────┐
│  Clojure DSL (Hiccup-style)     │  ← You write here
├──────────────────────────────────┤
│  Reactive Layer (ratom, etc.)   │
├──────────────────────────────────┤
│  Clojure Element Wrappers        │
├──────────────────────────────────┤
│  Java Bindings (JNI)             │
├──────────────────────────────────┤
│  C++ JNI Glue Code               │
├──────────────────────────────────┤
│  Hyprtoolkit C++ Library         │
└──────────────────────────────────┘
```

### Layers:

1. **DSL Layer** (`hyprclj.dsl`) - Hiccup-style syntax
2. **Reactive Layer** (`hyprclj.reactive`) - Atoms, reactions, watches
3. **Elements Layer** (`hyprclj.elements`) - Idiomatic Clojure wrappers
4. **Core Layer** (`hyprclj.core`) - Backend and window management
5. **Java Bindings** (`org.hyprclj.bindings`) - JNI bridge classes
6. **C++ Glue** (`native/*.cpp`) - JNI implementation
7. **Hyprtoolkit** - Native C++ GUI library

## API Overview

### Core (`hyprclj.core`)

- `create-backend!` - Initialize the backend
- `create-window` - Create a window
- `open-window!` / `close-window!` - Window lifecycle
- `enter-loop!` - Start event loop
- `add-timer!` - Schedule timers
- `root-element` - Get window's root element

### Elements (`hyprclj.elements`)

- `button` - Create button
- `text` - Create text label
- `column-layout` - Vertical layout
- `row-layout` - Horizontal layout
- `add-child!` / `remove-child!` - Manage hierarchy
- `set-margin!` / `set-grow!` - Layout properties

### DSL (`hyprclj.dsl`)

- `mount!` - Mount UI into window
- `compile-element` - Compile Hiccup to elements
- `defcomponent` - Define components
- `ratom` - Reactive atom
- `reaction` - Derived reactive values
- `cursor` - Nested atom cursor

### Hiccup Syntax

```clojure
[:element-type props? & children]

;; Examples:
[:text "Hello"]
[:text {:font-size 24} "Hello"]
[:button {:label "Click" :on-click handler}]
[:column {:gap 10}
  [:text "Child 1"]
  [:text "Child 2"]]
```

### Supported Elements

**Basic Elements:**
- `:text` - Text display
- `:button` - Clickable button
- `:textbox` - Text input field
- `:checkbox` - Checkbox with label
- `:rectangle` - Colored rectangles for backgrounds/borders

**Layout Containers:**
- `:column` - Vertical layout (basic)
- `:row` - Horizontal layout (basic)
- `:v-box` - Vertical box with positioning support (NEW!)
- `:h-box` - Horizontal box with positioning support (NEW!)
- `:colored-button` - Button with colored background (NEW!)

### Common Props

**All elements:**
- `:margin` - `[top right bottom left]` or single number
- `:grow` - `true/false` - expand to fill space
- `:size` - `[width height]` - explicit sizing
- `:position` - `:absolute` (top-left) or `:auto` (centered, default)

**Layout boxes (v-box/h-box):**
- `:gap` - Spacing between children
- `:children` - Vector of child elements (programmatic generation)
- `:background` - Background color `[r g b alpha]`
- `:border` - Border thickness
- `:border-color` - Border color `[r g b alpha]`
- `:rounding` - Corner rounding in pixels

**Text:**
- `:content` - Text string
- `:font-size` - Size in pixels
- `:font-family` - Font name
- `:color` - `[r g b a]` (0-255)
- `:align` - `"left"/"center"/"right"`

**Button:**
- `:label` - Button text
- `:size` - `[width height]`
- `:font-size` - Font size
- `:no-border` - Remove border
- `:no-bg` - Remove background
- `:on-click` - Click handler function
- `:on-right-click` - Right-click handler

**Layouts:**
- `:gap` - Spacing between children
- `:size` - `[width height]`

## Examples

See the `examples/` directory:

- `simple.clj` - Basic static UI
- `reactive_counter.clj` - Reactive state demo
- `demo.clj` - Full-featured demo app

## Development

### Project Structure

```
hyprclj/
├── src/
│   ├── java/                 # Java JNI bindings
│   │   └── org/hyprclj/bindings/
│   └── clojure/              # Clojure source
│       └── hyprclj/
├── native/                   # C++ JNI implementation
│   ├── *.cpp                 # Implementation files
│   ├── CMakeLists.txt        # Build config
│   └── build.sh              # Build script
├── examples/                 # Example applications
├── test/                     # Tests
└── deps.edn                  # Dependencies

```

### Building

```bash
# Build native library
cd native && ./build.sh

# Generate Java headers (if needed)
javac -h native src/java/org/hyprclj/bindings/*.java

# Run REPL
clj -M:dev

# Run tests
clj -M:test
```

## Recent Improvements

### ✅ Responsive Layouts (FIXED!)
- Window resize events properly handled
- Correct drawable area sizing (accounts for HiDPI scaling)
- Loop protection prevents infinite remount cycles
- Supports both absolute and auto-layout positioning

### ✅ Layout System
- Flexbox-style v-box/h-box composition
- Deep nesting support
- Border separators with colors and opacity
- Colored backgrounds via Rectangle element
- :children prop for clean programmatic UI generation

### ✅ Styling & Visual Design
- Rectangle element with colors, borders, rounding
- Semi-transparent colors via alpha channel [r g b alpha]
- Colored button backgrounds (via layering)
- Border separators between sections

## Current Limitations

- ⚠️ **Background layering** - Only works at root level (not inside v-box/h-box)
- ⚠️ **Button colors** - Require layering workaround (colored-button)
- ⚠️ **Sized rectangles in containers** - Can clip content if not careful
- ❌ Not all Hyprtoolkit elements implemented (Slider, Image, etc.)
- ❌ No animation API exposed yet
- ❌ No hot-reload / component lifecycle

## TODO

- [ ] Implement remaining element types (Slider, Image, etc.)
- [ ] Better reconciliation for keyed reactive lists
- [ ] Component lifecycle hooks
- [ ] Animation bindings
- [ ] Theming/palette API
- [ ] Tests
- [ ] Package as library

## Contributing

This is a POC! Contributions, ideas, and feedback are welcome.

## License

To be determined (likely follows Hyprtoolkit's BSD-3-Clause)

## Credits

- [Hyprtoolkit](https://github.com/hyprwm/hyprtoolkit) - The underlying GUI library
- [Reagent](https://reagent-project.github.io/) - Inspiration for reactive model
- [Re-com](https://github.com/day8/re-com) - Inspiration for component DSL

---

**Built with 💜 for the Hyprland community**
