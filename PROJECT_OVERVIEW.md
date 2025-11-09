# Hyprclj - Project Overview

## What We Built

A complete **proof-of-concept** for Clojure bindings to Hyprtoolkit, enabling native Wayland GUI development in Clojure with a Reagent-style reactive DSL.

## File Structure

```
hyprclj/
├── deps.edn                          # Clojure dependencies
├── .gitignore                        # Git ignore rules
│
├── README.md                         # Main documentation
├── TUTORIAL.md                       # Step-by-step tutorial
├── DEVELOPMENT.md                    # Developer guide
├── PROJECT_OVERVIEW.md              # This file
│
├── src/
│   ├── java/org/hyprclj/bindings/   # JNI Java bindings (8 files)
│   │   ├── HyprtoolkitInfoMapper.java    # JavaCPP config
│   │   ├── Backend.java                   # Backend wrapper
│   │   ├── Window.java                    # Window wrapper
│   │   ├── Element.java                   # Base element
│   │   ├── Button.java                    # Button element
│   │   ├── Text.java                      # Text element
│   │   ├── ColumnLayout.java              # Vertical layout
│   │   └── RowLayout.java                 # Horizontal layout
│   │
│   └── clojure/hyprclj/             # Clojure source (4 files)
│       ├── core.clj                  # Backend & window management
│       ├── elements.clj              # Element constructors
│       ├── reactive.clj              # Reactive state (ratom, etc.)
│       └── dsl.clj                   # Hiccup-style DSL
│
├── native/                          # C++ JNI implementation
│   ├── CMakeLists.txt               # Build configuration
│   ├── build.sh                     # Build script
│   ├── hyprclj_backend.cpp          # Backend JNI impl
│   ├── hyprclj_window.cpp           # Window JNI impl
│   ├── hyprclj_element.cpp          # Element JNI impl
│   ├── hyprclj_button.cpp           # Button JNI impl
│   ├── hyprclj_text.cpp             # Text JNI impl
│   └── hyprclj_layouts.cpp          # Layouts JNI impl
│
├── examples/                        # Example applications
│   ├── simple.clj                   # Basic static UI
│   ├── reactive_counter.clj         # Reactive state demo
│   └── demo.clj                     # Full-featured demo
│
├── test/                            # Tests (TODO)
├── resources/                       # Native library output
└── run_example.sh                   # Helper script

```

## Architecture Layers

### Layer 7: User Code (Clojure DSL)
```clojure
[:column {:gap 10}
  [:text "Hello"]
  [:button {:on-click handler}]]
```

### Layer 6: DSL Compiler (`hyprclj.dsl`)
- Hiccup → Element compilation
- Component system
- `mount!`, `defcomponent`

### Layer 5: Reactive Layer (`hyprclj.reactive`)
- `ratom` - Reactive atoms
- `reaction` - Derived values
- `cursor` - Nested atom access
- Dependency tracking

### Layer 4: Element Wrappers (`hyprclj.elements`)
- Idiomatic Clojure functions
- `button`, `text`, `column-layout`, `row-layout`
- Helper functions for manipulation

### Layer 3: Core (`hyprclj.core`)
- Backend lifecycle
- Window management
- Event loop
- Timer system

### Layer 2: Java Bindings (`org.hyprclj.bindings`)
- JNI bridge classes
- Builder pattern wrappers
- Native method declarations

### Layer 1: C++ JNI Glue (`native/*.cpp`)
- JNI implementations
- C++ ↔ Java marshalling
- Callback handling
- Memory management

### Layer 0: Hyprtoolkit
- Native C++ GUI library
- Wayland integration
- Rendering engine

## Key Features Implemented

### ✅ Core Infrastructure
- [x] JNI bindings setup
- [x] Backend initialization
- [x] Window creation and management
- [x] Event loop integration
- [x] Timer system

### ✅ UI Elements
- [x] Text labels
- [x] Buttons with click handlers
- [x] Column layout (vertical)
- [x] Row layout (horizontal)
- [x] Element hierarchy management
- [x] Margin and grow properties

### ✅ Reactive System
- [x] Reactive atoms (`ratom`)
- [x] Reactions (derived values)
- [x] Cursors (nested access)
- [x] Dependency tracking
- [x] Watch-based updates

### ✅ DSL
- [x] Hiccup-style syntax
- [x] Component system
- [x] Props handling
- [x] Children composition
- [x] Dynamic rendering

### ✅ Documentation
- [x] README with quickstart
- [x] Tutorial for beginners
- [x] Development guide
- [x] Three example apps

## What's NOT Implemented

### Missing UI Elements
- [ ] Checkbox
- [ ] Slider
- [ ] Textbox (input)
- [ ] Image
- [ ] Combobox
- [ ] Scroll area
- [ ] Spinbox

### Missing Features
- [ ] Full reactive reconciliation (VDOM-like diffing)
- [ ] Component lifecycle hooks
- [ ] Animation API
- [ ] Theming/palette access
- [ ] Keyboard event handling
- [ ] Advanced layout options
- [ ] Hot reload
- [ ] Error boundaries

### Quality & Polish
- [ ] Automated tests
- [ ] Memory leak prevention
- [ ] Callback cleanup
- [ ] Better error messages
- [ ] Performance optimization
- [ ] Production packaging

## Lines of Code

```
Java:        ~600 lines (8 files)
C++:         ~700 lines (6 files)
Clojure:     ~800 lines (4 files)
Examples:    ~300 lines (3 files)
Docs:        ~1500 lines (4 files)
─────────────────────────────
Total:       ~3900 lines
```

## How to Use

### 1. Install Hyprtoolkit
```bash
# From source or package manager
```

### 2. Build Native Library
```bash
cd native
./build.sh
```

### 3. Run Examples
```bash
./run_example.sh simple
./run_example.sh reactive_counter
./run_example.sh demo
```

### 4. Write Your App
```clojure
(ns my-app
  (:require [hyprclj.core :as hypr]
            [hyprclj.dsl :refer [mount! ratom]]))

(def state (ratom 0))

(defn -main []
  (hypr/create-backend!)
  (let [window (hypr/create-window {:title "My App"})]
    (mount! (hypr/root-element window)
            [:column {:gap 10}
             [:text (str "Value: " @state)]
             [:button {:label "+" :on-click #(swap! state inc)}]])
    (hypr/open-window! window)
    (hypr/enter-loop!)))
```

## Next Steps for Production

### High Priority
1. **Implement reconciliation** - VDOM-like diffing for efficient updates
2. **Add remaining elements** - Checkbox, Textbox, Slider, etc.
3. **Memory management** - Cleanup callbacks, prevent leaks
4. **Error handling** - Better error messages, recovery

### Medium Priority
5. **Component lifecycle** - Mount, update, unmount hooks
6. **Animation API** - Expose Hyprtoolkit's animation system
7. **Testing** - Unit tests, integration tests
8. **Performance** - Profile and optimize hot paths

### Nice to Have
9. **Hot reload** - Development workflow improvement
10. **Theming** - Access to Hyprtoolkit palette system
11. **Dev tools** - Component inspector, state viewer
12. **Packaging** - Publish as library

## Design Decisions

### Why JNI Instead of JavaCPP Generation?
- **Pro**: More control, clearer code
- **Con**: More manual work
- **Decision**: JNI for POC, could migrate to JavaCPP later

### Why Reagent-style Instead of Om/Fulcro?
- **Pro**: Simpler, more familiar to most Clojure devs
- **Con**: Less structured for large apps
- **Decision**: Reagent style, can layer on more structure later

### Why Not FFI (JNA/JNR)?
- **Pro**: Would be simpler than JNI
- **Con**: Harder to handle C++ objects, callbacks
- **Decision**: JNI is more powerful for C++ interop

## Performance Notes

### JNI Overhead
- Each JNI call: ~10-100ns overhead
- Not a concern for UI (human timescale)
- Could batch updates if needed

### Reactive Updates
- Current: Re-render entire component tree
- Needed: Reconciliation/diffing
- Impact: Acceptable for small UIs, limiting for large

### Memory
- Java/Clojure objects: GC managed
- Native objects: Manual management needed
- Callbacks: Need cleanup (TODO)

## Comparison to Alternatives

### vs. Seesaw (Swing bindings)
- **Hyprclj**: Native, modern, Wayland
- **Seesaw**: Mature, cross-platform, dated UI

### vs. Membrane (CLJS + Native)
- **Hyprclj**: JVM, simpler stack
- **Membrane**: More experimental, different philosophy

### vs. Electron/Tauri + CLJS
- **Hyprclj**: Native, lower memory, Wayland-specific
- **Electron**: Cross-platform, huge ecosystem, heavy

### vs. cljfx (JavaFX bindings)
- **Hyprclj**: Wayland-native, modern
- **cljfx**: Mature, cross-platform, JavaFX

## Credits & Inspiration

- **Hyprtoolkit** - The underlying GUI library
- **Reagent** - Reactive model inspiration
- **Re-com** - Component DSL inspiration
- **Hiccup** - Syntax inspiration
- **React** - Component paradigm

## License

TBD (likely BSD-3-Clause to match Hyprtoolkit)

---

**Status**: POC Complete ✅
**Date**: 2025-11-08
**Ready for**: Experimentation, feedback, enhancement
