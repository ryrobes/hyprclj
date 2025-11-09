# Reducing Flicker in Reactive Updates

## The Problem

When reactive components update, they clear all children and rebuild, causing visible flicker:

```clojure
(defn remount! []
  (clear-children! parent)  ; ← Everything disappears
  (add-child! parent new)   ; ← Then reappears = flicker!
)
```

## Solutions

### Solution 1: Double-Buffering ✅ (Implemented)

**Add new first, then remove old:**

```clojure
(defn remount! []
  ;; Build new element first
  (let [new-elem (compile-element (component-fn))]
    ;; Add new (now we have both old and new)
    (add-child! parent new-elem)
    ;; Remove old
    (when @current-element
      (remove-child! parent @current-element))
    ;; Track new
    (reset! current-element new-elem)))
```

**Benefit**: No blank frame between old and new!

### Solution 2: Granular Reactivity ⭐ **BEST**

**Only make the changing parts reactive:**

```clojure
;; Bad - entire component remounts
(reactive-mount! parent [counter]
  (fn []
    [:column {}
      [:text "Header"]           ; ← Recreated
      [:text (str @counter)]     ; ← This changes
      [:button {:label "OK"}]])) ; ← Recreated

;; Good - only counter text remounts!
(let [col (column-layout {})]
  (add-child! col (text "Header"))  ; Static, never changes

  ;; Only this remounts
  (reactive-mount! col [counter]
    (fn []
      [:text (str @counter)]))

  (add-child! col (button "OK")))   ; Static, never changes
```

**Benefit**: Minimal DOM churn, almost no flicker!

### Solution 3: True VDOM (Future)

With full reconciliation:
```clojure
;; Diff detects only :content prop changed
old: [:text {:content "Count: 0" :font-size 48}]
new: [:text {:content "Count: 1" :font-size 48}]

;; Calls native update directly (no element recreation)
(update-text-content! handle "Count: 1")
```

**Benefit**: Zero flicker, maximum performance!

**Downside**: Complex implementation, Hyprtoolkit limitations

## Example: smooth_counter.clj

Demonstrates granular reactivity:

```clojure
;; Static structure
(let [col (column-layout)]

  (add-child! col (text "Title"))  ; Never remounts

  ;; ONLY the counter text is reactive
  (reactive-mount! (column-layout)  ; Container for just the number
                  [counter]
                  (fn [] [:text (str @counter)]))

  (add-child! col button-row))  ; Never remounts
```

**Result**: When counter changes, only 1 text element remounts instead of entire UI!

## Comparison

| Approach | Elements Recreated | Flicker | Code Complexity |
|----------|-------------------|---------|-----------------|
| Manual full remount | All (~10) | ❌ High | Low |
| Component reactivity | Component (~5) | ⚠️ Medium | Low |
| **Double-buffering** | Component (~5) | **✅ Low** | **Low** |
| **Granular reactivity** | **Changed only (~1)** | **✅ Minimal** | **Medium** |
| Full VDOM | Changed only (~1) | ✅ None | High |

## Recommendations

### For POC/Simple Apps:
Use **double-buffering** (current implementation) ✅
- Minimal code changes
- Significant flicker reduction
- Good enough!

### For Production Apps:
Use **granular reactivity** pattern ⭐
- Separate static and dynamic parts
- Each reactive part is small
- Almost no flicker
- Clean architecture

Example:
```clojure
(defn my-app []
  (let [root (column-layout)]

    ;; Header (static)
    (add-static-header! root)

    ;; Counter (reactive)
    (add-reactive-counter! root counter-atom)

    ;; Status (reactive)
    (add-reactive-status! root status-atom)

    ;; Footer (static)
    (add-static-footer! root)

    root))
```

### For Maximum Performance:
Implement **full VDOM reconciliation** (Phase 3)
- Element-level diffing
- Native property updates
- Zero unnecessary recreation
- No flicker at all

## Quick Fix for Existing Code

**Before**:
```clojure
(reactive-mount! parent [counter]
  (fn []
    [:column {}
      [:text "Score"]
      [:text (str @counter)]  ; Only this changes!
      [:button "OK"]]))
```

**After** (less flicker):
```clojure
;; Build static structure
(let [col (column-layout)]
  (add-child! parent col)
  (add-child! col (text "Score"))

  ;; Only counter is reactive
  (reactive-mount! col [counter]
    (fn [] [:text (str @counter)]))

  (add-child! col (button "OK")))
```

**Result**: Only 1 element remounts instead of 3!

## Current Status

✅ **Double-buffering implemented** in `simple_reactive.clj`
✅ **Granular pattern demonstrated** in `smooth_counter.clj`
✅ Flicker significantly reduced

**For POC, this is excellent!** The double-buffering should make updates much smoother.

## Try It

```bash
# With double-buffering (improved)
./run_example.sh auto-simple

# With granular reactivity (minimal flicker)
./run_example.sh smooth-counter
```

Click the buttons and compare the smoothness!

---

**Bottom line**: Flicker is greatly reduced with double-buffering, and can be minimized further with granular reactivity patterns. For full elimination, would need Phase 3 (VDOM), but current approach is totally acceptable! ✅
