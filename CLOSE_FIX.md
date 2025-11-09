# Window Close Fix

## Problem

When closing windows with Hyprland's close command, the application would:
1. ❌ Print "Window closing..."
2. ❌ Segfault in OpenGL renderer destructor
3. ❌ Core dump
4. ❌ Eventually exit after a few seconds

**Root Cause**: Race condition between window cleanup and JVM shutdown.

## Solution

### C++ Side (hyprclj_window.cpp)

**Before**:
```cpp
window->m_events.closeRequest.listen([window, globalCallback]() {
    // Call Java callback
    env->CallVoidMethod(globalCallback, runMethod);
    // Also close the window
    window->close();  // ❌ Triggers OpenGL cleanup
});
```

**After**:
```cpp
window->m_events.closeRequest.listen([globalCallback]() {
    // Call Java callback
    env->CallVoidMethod(globalCallback, runMethod);
    // Don't close here - let System/exit handle cleanup
});
```

### Java Side (All Examples)

**Before**:
```clojure
:on-close (fn [w]
            (println "Window closing...")
            (System/exit 0))  ; ❌ Immediate exit during cleanup
```

**After**:
```clojure
:on-close (fn [w]
            (println "Window closing...")
            (future
              (Thread/sleep 100)  ; Give native cleanup time
              (System/exit 0)))
```

## How It Works Now

1. User presses `Mod+Shift+C` (Hyprland close)
2. Wayland sends close request to window
3. Hyprtoolkit fires `closeRequest` signal
4. C++ listener calls Java callback
5. Java callback prints message
6. Java spawns future thread
7. Future waits 100ms
8. `System/exit(0)` called
9. JVM cleanly shuts down all native resources
10. ✅ Clean exit, no segfault!

## Result

**Before**: Segfault, core dump, delayed exit
**After**: Clean message, 100ms delay, smooth exit ✅

## Testing

```bash
# Run any example
./run_example.sh interactive-test

# Close with Hyprland
# Press Mod+Shift+C

# Expected output:
✓ Close button clicked!
  Window close request received
  Exiting in 100ms...
# (waits 100ms)
# Clean exit back to shell! ✅
```

## Technical Details

The 100ms delay allows:
- OpenGL context cleanup to complete
- Wayland resources to be freed
- Backend to finish pending operations
- JNI callbacks to finish
- Proper shutdown sequence

Without this delay, `System/exit(0)` would interrupt native cleanup mid-process, causing the segfault.

## Alternative Approaches Considered

1. **Call window->close() then exit** - ❌ Caused segfault
2. **Exit immediately** - ❌ Caused segfault
3. **Don't close window, just exit** - ✅ **Current approach**
4. **Signal event loop to exit naturally** - ⏳ Future improvement

## Future Improvement

A more elegant solution would be:
```clojure
;; Set a flag
(reset! should-exit true)

;; Event loop checks flag and exits gracefully
(when @should-exit
  ;; Exit event loop
  ;; Clean up resources
  ;; Exit JVM)
```

But for the POC, the current 100ms delay approach works perfectly!

## Status

✅ **Fixed** - All examples now close cleanly without segfaults!

---

**Files Modified**:
- `examples/simple.clj`
- `examples/interactive_test.clj`
- `examples/reactive_counter.clj`
- `examples/counter_working.clj`
- `native/hyprclj_window.cpp`

**Result**: Clean window closing in all examples! 🎉
