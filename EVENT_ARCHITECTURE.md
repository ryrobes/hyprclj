# Event Architecture in Hyprclj

## The Big Picture

### Two Event Systems Working Together

```
┌──────────────────────────────────────┐
│   OS/Wayland Events (Input)          │
│   Mouse, Keyboard, Window            │
└────────────┬─────────────────────────┘
             ↓
      Event Loop (C++)
             ↓
      JNI Callbacks
             ↓
┌────────────▼─────────────────────────┐
│   Clojure Event Handlers             │ ← You write these
│   (swap! atom new-value)             │
└────────────┬─────────────────────────┘
             ↓
┌────────────▼─────────────────────────┐
│   Atom Watches (Pure Clojure)        │ ← Pure Clojure!
│   Detect changes, trigger updates    │
└────────────┬─────────────────────────┘
             ↓
      UI Re-renders
             ↓
      JNI Calls
             ↓
      Native GUI Updates
```

**Clean separation of concerns!** ✅

## Text Input - Level of Effort Analysis

### What's Needed (300-500 LOC, 4-8 hours)

#### 1. Window Keyboard Events (150 LOC - Medium)

**C++ (hyprclj_window.cpp)**:
```cpp
// Wire up keyboard signal
window->m_events.keyboardKey.listen(
    [globalCallback](Input::eKeyboardKey key, bool pressed) {
        // Call Java with key code
        callKeyboardHandler(globalCallback, (int)key, pressed);
    });
```

**Java (Window.java)**:
```java
public interface KeyboardListener {
    void onKey(int keyCode, boolean pressed);
}

private KeyboardListener keyboardListener;

public void setKeyboardListener(KeyboardListener listener) {
    this.keyboardListener = listener;
    nativeSetKeyboardCallback(nativeHandle, listener);
}
```

**Clojure (core.clj)**:
```clojure
(defn set-keyboard-handler! [window handler-fn]
  (.setKeyboardListener window
    (reify KeyboardListener
      (onKey [_ keyCode pressed]
        (handler-fn keyCode pressed)))))
```

#### 2. Focus Management (50 LOC - Easy)

```clojure
;; Track focused element
(defonce focused-element (atom nil))

(defn focus! [element]
  (reset! focused-element element))

;; Route keys to focused element
(set-keyboard-handler! window
  (fn [keyCode pressed]
    (when-let [elem @focused-element]
      (handle-key elem keyCode pressed))))
```

#### 3. Textbox Text Access (100 LOC - Medium-Hard)

**The Challenge**: Need to access Hyprtoolkit's internal textbox state.

**Option A - Via Submit Callback** (Easier):
```cpp
textboxElement->m_onSubmit = [globalCallback]() {
    // Get text from textbox
    std::string text = textboxElement->m_text;  // If public!
    // Pass to Java
    jstring jtext = env->NewStringUTF(text.c_str());
    env->CallVoidMethod(globalCallback, acceptMethod, jtext);
};
```

**Option B - Shadow State** (Pragmatic):
```clojure
;; Maintain text in Clojure
(def textbox-state (atom ""))

;; On keyboard event
(swap! textbox-state str (char keyCode))

;; Remount textbox with new text
;; (Hacky but works!)
```

#### 4. Character Mapping (100-200 LOC - Hard)

Map key codes to characters:
```clojure
(defn keycode->char [keyCode shift?]
  (case keyCode
    65 (if shift? \A \a)
    66 (if shift? \B \b)
    ;; ... 100+ mappings
    32 \space
    13 :enter
    8 :backspace
    ;; etc.
    ))
```

Or let Hyprtoolkit handle it (if exposed)!

### Simplified Approach (100-150 LOC, 1-2 hours)

**Just wire submit callback**:

1. When textbox created, wire `m_onSubmit`
2. On Enter key, Hyprtoolkit calls callback with current text
3. Clojure receives text, adds todo
4. Don't try to track every keystroke

**Result**: Textbox works for submit-on-enter, which covers 80% of use cases!

## What Can Be Clojure-Only

### ✅ Pure Clojure (No Event Loop Needed)

**State Management**:
```clojure
(def app-state (ratom {:todos [] :filter :all}))
(swap! app-state update :todos conj new-todo)
;; No event loop involved!
```

**Computed State**:
```clojure
(def visible-todos
  (reaction
    (filter (fn [t]
              (case (:filter @app-state)
                :all true
                :done (:done t)
                :active (not (:done t))))
            (:todos @app-state))))
;; Automatically recomputes!
```

**Business Logic**:
```clojure
(defn add-todo! [text]
  (swap! todos conj {:text text :done false}))

(defn complete-all! []
  (swap! todos
    (fn [ts] (mapv #(assoc % :done true) ts))))

;; All pure Clojure functions!
```

**Timed Events** (initiated from Clojure):
```clojure
;; Start a timer
(hypr/add-timer! 5000
  (fn []
    (swap! notifications conj "5 seconds elapsed")))

;; Uses event loop but initiated from Clojure
```

### ❌ Requires Event Loop

**User Input**:
- Mouse clicks → Must come from OS
- Keyboard → Must come from OS
- Touch/gestures → Must come from OS

**Window Events**:
- Resize → Compositor decides
- Close → User/Compositor initiates
- Focus → Window manager decides

**System Events**:
- File watching → OS notification
- Network → Socket events
- IPC → External messages

**Can't Fake These!** They originate outside your app.

## The Perfect Hybrid Model (What We Have!)

### Event Flow:

```
External Event (Mouse click)
        ↓
Event Loop detects
        ↓
Hyprtoolkit dispatches to element
        ↓
JNI callback fires
        ↓
Clojure handler executes:
    (swap! counter inc)  ← Pure Clojure!
        ↓
Atom watch fires (Pure Clojure!)
        ↓
Reactive component remounts
        ↓
JNI calls to rebuild UI
        ↓
Native rendering
```

**External events enter via event loop, then it's all Clojure!** ✅

## What We Need vs What We Have

### Currently Wired ✅:

| Event Type | Status | Usage |
|------------|--------|-------|
| Mouse click | ✅ Working | Buttons, interactive elements |
| Mouse enter | ✅ Wired (untested) | Hover effects |
| Mouse leave | ✅ Wired (untested) | Hover effects |
| Window close | ✅ Working | App exit |
| Timers | ✅ Working | Scheduled tasks |

### Not Yet Wired ❌:

| Event Type | Effort | Needed For |
|------------|--------|------------|
| Keyboard keys | Medium | Text input, shortcuts |
| Window resize | Easy | Responsive layouts |
| Window focus | Easy | Focus indicators |
| Mouse move | Easy | Cursors, tooltips |
| Scroll | Medium | Scroll areas |
| Drag & drop | Hard | File uploads, reordering |

### For POC:

**We have enough!** ✅
- Mouse clicks cover most interaction
- Atoms handle all state
- Reactive system works
- Can build real apps!

**Text input would be nice** but not critical for POC.

## Recommendation: Priority Order

### Now (POC Complete):
- ✅ Mouse clicks
- ✅ Atoms & reactivity
- ✅ Basic UI elements

### Phase 2.5 (Easy Wins):
1. **Mouse enter/leave** - Test what we have
2. **Window resize** - Expose existing event
3. **Textbox submit** - Just wire the callback (~100 LOC)

### Phase 3 (Full Input):
4. **Keyboard events** - Full wire-up
5. **Text input** - Live character input
6. **Focus management** - Proper focus tracking

### Phase 4 (Advanced):
7. Scroll events
8. Drag & drop
9. Touch input

## Text Input - Quick Win Approach

**Skip full keyboard handling, just wire submit:**

```cpp
// hyprclj_textbox.cpp (100 LOC)
JNIEXPORT void JNICALL
Java_org_hyprclj_bindings_Textbox_nativeSetSubmitCallback(...) {
    auto textbox = std::dynamic_pointer_cast<CTextboxElement>(element);

    textbox->m_onSubmit = [globalCallback, textbox]() {
        // Get current text
        std::string text = textbox->m_text;  // Hope this is public!

        // Call Java
        JNIEnv* env = getEnv();
        jclass consumerClass = env->FindClass("java/util/function/Consumer");
        jmethodID acceptMethod = env->GetMethodID(consumerClass, "accept", "(Ljava/lang/Object;)V");
        jstring jtext = env->NewStringUTF(text.c_str());
        env->CallVoidMethod(globalCallback, acceptMethod, jtext);
    };
}
```

**Then in Clojure**:
```clojure
[:textbox {:placeholder "Enter task..."
           :on-submit (fn [text]
                        (add-todo! text))}]

;; User types in textbox, presses Enter
;; → on-submit fires with text
;; → Add todo!
;; Works! ✅
```

**Effort**: ~2 hours to wire up submit callback properly

## Bottom Line

### What You Can Do Now (Clojure-Only):

```clojure
;; All pure Clojure - no event loop knowledge needed!
(def state (ratom {}))

(swap! state assoc :key val)        ; Update state
(def derived (reaction (f @state))) ; Computed values
(reactive-mount! parent [state] ui) ; Auto-updates

;; Build entire apps with just atoms and reactive components!
```

### What Needs Event Loop:

Only **external input** - and we've already wired the most important (mouse clicks)!

### For Full Text Input:

**Effort**: 300-500 LOC, 4-8 hours
**Benefit**: Text input fields work like native inputs
**Priority**: Medium (nice-to-have, not critical for POC)

### My Recommendation:

**For This POC**:
- ✅ What we have is **excellent**
- ✅ Demonstrates all core concepts
- ✅ Buttons work, reactivity works, apps work
- 🎯 **Ship it as-is!**

**For v2** (if you want text input):
- Wire up textbox submit callback (~100 LOC, 1-2 hours)
- Good first enhancement after POC
- Makes TODO app feel more polished

**The architecture is perfect** - clean separation between event loop (external events) and Clojure (state & reactivity). You can build complex apps right now with what we have!

---

**TL;DR**:
- **Clojure-only**: State management, reactive updates, business logic ✅
- **Event loop**: User input, window events (mouse clicks wired! ✅)
- **Text input**: 300-500 LOC to fully wire, but POC is complete without it!
