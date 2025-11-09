# Hyprclj Usage Guide

## Running Examples

All examples are now fully functional and can be closed properly!

### Available Examples

```bash
# Simple static UI
./run_example.sh simple

# Interactive button testing
./run_example.sh interactive-test

# Reactive state demo
./run_example.sh reactive-counter

# Manual reactive updates
./run_example.sh counter-working
```

## Window Controls

### Closing Windows

All examples now support proper window closing:

**Method 1: Hyprland Close Command**
- Press `Mod+Shift+C` (or your configured close binding)
- Window closes and application exits cleanly
- Console shows close message and returns to prompt ✅

**Method 2: Force Kill** (if needed)
```bash
# Kill all running examples
pkill -f "clojure.main -m"

# Kill specific example
pkill -f "clojure.main -m simple"
```

### Expected Behavior

When you close a window:
1. Console prints: `Window closing...` or `✓ Window close requested`
2. Window disappears from screen
3. JVM exits cleanly
4. **Shell prompt returns** ✅

## Testing Features

### Button Clicks (interactive-test)

**What to test:**
1. Click "Click Me!" button
   - Console shows: `✓ Button 1 clicked! Count: 1`
   - Count increments with each click

2. Click "Also Click Me!" button
   - Console shows: `✓ Button 2 clicked!`

3. Click "Print State" button
   - Console shows current click count

**Expected**: All buttons respond immediately, console output appears

### Reactive Updates (counter-working)

**What to test:**
1. Click `+` button
   - Console shows: `Incremented! Count: 1`
   - Console shows: `Updating UI with count: 1`
   - **UI updates** to show new count ✅

2. Click `-` button
   - Count decreases
   - UI refreshes

3. Click `Reset`
   - Count returns to 0
   - UI refreshes

**Expected**: UI visually updates after each click showing new counter value

### Static UI (simple, reactive-counter)

- Text displays correctly
- Buttons render
- Buttons are clickable (may not do anything visible)
- Window can be closed properly

## Keyboard Shortcuts

### In Hyprland:

- `Mod+Shift+C` - Close focused window
- `Mod+Q` - Close window (if configured)
- Or use your configured window close binding

### In Terminal:

- `Ctrl+C` - Kill the Clojure process (emergency)
- `pkill -f hyprclj` - Kill all hyprclj processes

## Monitoring Windows

### Check what's running:

```bash
# See all Hyprclj windows
hyprctl clients | grep -B2 -A5 "hyprclj\|Simple\|Interactive\|Counter"

# See Java processes
ps aux | grep -i "[j]ava.*clojure.main"

# See window count
hyprctl clients | grep -c "hyprtoolkit-app"
```

### Focus a window:

```bash
hyprctl dispatch focuswindow "Interactive Test"
hyprctl dispatch focuswindow "Simple Hyprclj App"
```

## Troubleshooting

### Window won't close

**If close doesn't work:**
```bash
# Force kill by PID (from hyprctl clients)
kill <PID>

# Or kill all Clojure processes
pkill -f "clojure.main"
```

### Console hangs after close

**Fixed!** All examples now call `System/exit(0)` which:
- Closes the window
- Exits the JVM
- Returns console prompt

If still hanging, press `Ctrl+C` to force exit.

### Multiple windows open

Each time you run an example, a new window opens. To close all:

```bash
# Kill all Java/Clojure processes
pkill -f java
pkill -f clojure
```

## Development Workflow

### Making Changes:

**Clojure code changes:**
- Just edit and re-run
- No rebuild needed
- Clear cache if needed: `rm -rf .cpcache`

**Java code changes:**
```bash
javac -d target/classes -cp "$(clj -Spath)" src/java/org/hyprclj/bindings/*.java
# Then re-run
```

**C++ code changes:**
```bash
cd native && ./build.sh && cd ..
# Then re-run
```

### Quick Edit-Test Loop:

```bash
# Edit Clojure code
vim examples/simple.clj

# Clear cache and run
rm -rf .cpcache && ./run_example.sh simple

# Test changes
# Close with Mod+Shift+C
# Repeat
```

## Best Practices

### For Clean Exits:

Always include a close handler:
```clojure
(hypr/create-window
  {:title "My App"
   :on-close (fn [w]
               (println "Cleaning up...")
               (System/exit 0))})
```

### For Multiple Windows:

Track windows and close all:
```clojure
(def windows (atom []))

;; On close
:on-close (fn [w]
            (doseq [win @windows]
              (hypr/close-window! win))
            (System/exit 0))
```

### For Graceful Shutdown:

```clojure
:on-close (fn [w]
            ;; Save state
            (save-app-state! @app-state)
            ;; Cleanup resources
            (cleanup!)
            ;; Exit
            (System/exit 0))
```

## Example Session

```bash
$ ./run_example.sh interactive-test
Running example: interactive-test
Starting Clojure...
Creating an Aquamarine backend!
[HT] DEBUG: Connected to a wayland compositor: Hyprland
Window created successfully
UI mounted successfully
Window opened - should be visible now!
Entering event loop...

# Click buttons in the GUI
✓ Button 1 clicked! Count: 1
✓ Button 2 clicked!
✓ Button 3 clicked!
  Current click count: 1

# Press Mod+Shift+C to close
✓ Close button clicked!
  Window close request received
  Exiting...
$  # Clean exit back to prompt!
```

## Summary

All examples now:
- ✅ Render windows on screen
- ✅ Display UI correctly
- ✅ Handle button clicks
- ✅ Print console output
- ✅ Close with Hyprland commands
- ✅ Exit cleanly to shell prompt

**Everything works!** 🎉

---

## Quick Commands

```bash
# Run any example
./run_example.sh <name>

# See all windows
hyprctl clients | grep hyprtoolkit

# Kill all
pkill -f clojure

# Clean rebuild
rm -rf .cpcache target/classes native/build
./build.sh
```

**Happy Clojure GUI development!** 🚀
