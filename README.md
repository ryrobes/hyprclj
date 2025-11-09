# Hyprclj

> Clojure bindings for [Hyprtoolkit](https://github.com/hyprwm/hyprtoolkit) - Build native Wayland GUI apps in Clojure!

Hyprclj provides idiomatic Clojure bindings to Hyprtoolkit, enabling you to write native Wayland applications using a Reagent-style reactive DSL.

## Features

- 🎨 **Hiccup-style DSL** - Familiar syntax for Clojure developers
- ⚛️ **Reactive State** - Reagent-like atoms and automatic UI updates
- 🚀 **Native Performance** - Direct bindings to C++ via JNI
- 🎯 **Wayland-Native** - Built for the modern Linux desktop
- 🧩 **Component System** - Composable, reusable UI components

## Status

✅ **POC Working!** - The simple example runs successfully and connects to Wayland!

⚠️ **Experimental** - This is a proof of concept. The demo example has issues with component definitions. See [CURRENT_STATUS.md](CURRENT_STATUS.md) for details.

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
- ✅ `simple` - Basic static UI
- ✅ `interactive-test` - Button click testing (verified working!)
- ✅ `reactive-counter` - Reactive state demo
- ✅ `counter-working` - Manual reactive updates with UI remounting

All examples support proper window closing with Hyprland's close command (`Mod+Shift+C`)!

See [USAGE.md](USAGE.md) for detailed usage instructions and [COMPLETE.md](COMPLETE.md) for full status.

## Quick Start

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

- `:text` - Text display
- `:button` - Clickable button
- `:column` - Vertical layout container
- `:row` - Horizontal layout container

### Common Props

**All elements:**
- `:margin` - `[top right bottom left]` or single number
- `:grow` - `true/false` - expand to fill space

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

## Limitations & TODOs

### Current Limitations:

- ✅ Windows, buttons, text, layouts
- ❌ Not all hyprtoolkit elements implemented yet (Checkbox, Slider, Image, etc.)
- ❌ Reactive updates don't fully re-render (need reconciliation)
- ❌ No hot-reload / component lifecycle management
- ❌ Limited styling options
- ❌ No animation API exposed

### TODO:

- [ ] Implement remaining element types
- [ ] Full reconciliation/diffing for reactive updates
- [ ] Component lifecycle hooks
- [ ] Better error handling
- [ ] Animation bindings
- [ ] Theming/palette API
- [ ] More comprehensive examples
- [ ] Tests
- [ ] Documentation
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
