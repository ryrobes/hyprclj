# Full VDOM Reconciliation - Roadmap

## Current Status (Phase 2)

✅ **Component-level reactivity**
- Entire component remounts on atom change
- Works but destroys buttons → crashes
- ~50 LOC implementation

## The Gap to Full VDOM

### Phase 2.5: Keyed Reconciliation ⭐ **RECOMMENDED**

**Effort**: 150-250 LOC, 2-4 hours
**Complexity**: Medium
**Benefit**: Solves 90% of practical issues

#### What It Does:

```clojure
;; Add keys to list items (like Reagent!)
(for [todo @todos]
  ^{:key (:id todo)}  ; ← Key metadata
  [:row {}
    [:button {:on-click #(toggle! (:id todo))}]
    [:text (:text todo)]])

;; On update:
;; - Matches old/new by key
;; - Reuses unchanged rows (buttons stay stable!)
;; - Only creates/destroys for add/delete
;; - Result: NO CRASHES! ✅
```

#### Implementation:

```clojure
(defn reconcile-keyed-children [parent old-children new-children]
  (let [old-by-key (into {} (map (fn [c] [(:key c) c]) old-children))
        new-by-key (into {} (map (fn [c] [(:key c) c]) new-children))
        old-keys (set (keys old-by-key))
        new-keys (set (keys new-by-key))]

    ;; Remove deleted items
    (doseq [k (clojure.set/difference old-keys new-keys)]
      (remove-child! parent (:native (old-by-key k))))

    ;; Add new items
    (doseq [k (clojure.set/difference new-keys old-keys)]
      (let [compiled (compile-element (new-by-key k))]
        (add-child! parent compiled)))

    ;; Existing items are REUSED (stable buttons!)
    ;; Just update their text children if needed
    ))
```

**Result:**
- Buttons never destroyed during toggle ✅
- No crashes ✅
- Can have per-item buttons ✅
- Still simple DSL ✅

### Phase 3: Full VDOM

**Effort**: 500-800 LOC, 1-2 weeks
**Complexity**: High
**Benefit**: Maximum performance, full React-like behavior

#### What It Adds:

- Property-level diffing
- Minimal DOM mutations
- Component lifecycle hooks
- shouldComponentUpdate
- Performance optimizations

**The Hyprtoolkit Challenge:**

Can't update most properties in-place:
```cpp
// Desired:
button->setLabel("new label");  // ❌ Doesn't exist

// Reality:
auto builder = button->rebuild();
builder->label("new label");
auto newButton = builder->commence();
// Replace in parent
```

So even with VDOM, we often rebuild elements. The benefit is **knowing which ones to rebuild**.

## Can We Keep DSL Simple?

### YES! No DSL changes needed! ✅

**Current Hiccup:**
```clojure
[:column {:gap 10}
  [:text "Hello"]
  [:button {:on-click handler}]]
```

**With VDOM (only adds keys for lists):**
```clojure
[:column {:gap 10}
  [:text "Hello"]
  [:button {:on-click handler}]

  ;; For dynamic lists:
  (for [item items]
    ^{:key (:id item)}  ; ← Only addition!
    [:text (:name item)])]
```

**That's it!** Reagent already does this, users are familiar!

## Recommended Path Forward

### Now → Phase 2.5 (Keyed Reconciliation)

**Priority: HIGH** ⭐

**Why:**
- Solves button crash problem ✅
- Enables practical list UIs ✅
- Relatively simple to implement ✅
- Uses familiar key pattern ✅

**Implementation Plan:**

1. **Extract keys from metadata** (~30 LOC)
```clojure
(defn get-key [vnode]
  (or (:key (meta vnode))
      (hash vnode)))  ; Auto-key if not provided
```

2. **Track previous render by key** (~40 LOC)
```clojure
(defonce element-registry (atom {}))  ; {key -> native-element}
```

3. **Reconcile children** (~80 LOC)
```clojure
(defn reconcile-children [parent old-keys new-vnodes]
  ;; Match by key, reuse elements
  ;; Add new, remove deleted
  )
```

4. **Integrate with reactive-mount!** (~50 LOC)
```clojure
(defn reactive-mount-keyed! [parent atoms-vec component-fn]
  ;; Like reactive-mount! but with key reconciliation
  )
```

**Total: ~200 LOC**

### Later → Phase 3 (Full VDOM)

**Priority: LOW** (Nice-to-have, not essential)

**Why wait:**
- Phase 2.5 solves practical issues
- Hyprtoolkit doesn't support in-place updates well
- High complexity for marginal benefit
- Can add later without DSL changes

## Code Example: Phase 2.5

```clojure
;; In todo app:
(reactive-mount-keyed! list-container [todos]
  (fn []
    (into [:column {}]
          (for [todo @todos]
            ^{:key (:id todo)}  ; ← Key for reconciliation
            [:row {}
              [:button {:label (if (:done todo) "✓" "○")
                        :on-click #(toggle! (:id todo))}]  ; Stable!
              [:text (:text todo)]]))))

;; When toggle is called:
;; 1. todos atom changes
;; 2. Component re-renders
;; 3. Reconciler matches by :id
;; 4. REUSES the row and buttons (stable!)
;; 5. Only updates text if changed
;; 6. NO CRASH! ✅
```

## Complexity Breakdown

### Phase 2.5: Keyed Lists

| Task | LOC | Complexity | Benefit |
|------|-----|------------|---------|
| Key extraction | 30 | Easy | Foundation |
| Element registry | 40 | Easy | Track elements |
| Reconcile algorithm | 80 | Medium | Core logic |
| Integration | 50 | Easy | Wire it up |
| **Total** | **200** | **Medium** | **High** |

**Time: 2-4 hours** for a working implementation

### Phase 3: Full VDOM

| Task | LOC | Complexity | Benefit |
|------|-----|------------|---------|
| Everything above | 200 | - | - |
| Property diffing | 100 | Medium | Optimization |
| Update strategies | 150 | Hard | Hyprtoolkit workarounds |
| Lifecycle hooks | 100 | Medium | React parity |
| shouldUpdate | 50 | Easy | Performance |
| Edge cases | 200 | Hard | Robustness |
| **Total** | **800** | **High** | **Medium** |

**Time: 1-2 weeks**

## The Pattern (Reagent-Style)

### How Reagent Does It:

```clojure
;; Reagent tracks components
(defn todo-item [todo]
  [:li {:key (:id todo)}  ; ← Key for reconciliation
    [:input {:type "checkbox"
             :checked (:done todo)
             :on-change #(toggle! (:id todo))}]
    [:span (:text todo)]])

(defn todo-list []
  [:ul
    (for [todo @todos]
      [todo-item todo])])  ; ← Each gets tracked separately

;; On update:
;; - React diffs by key
;; - Reuses matching components
;; - Updates changed props only
```

### How We'd Do It (Same Pattern!):

```clojure
;; Exact same API!
(defn todo-item [todo]
  ^{:key (:id todo)}  ; ← Key
  [:row {}
    [:button {:label (if (:done todo) "✓" "○")
              :on-click #(toggle! (:id todo))}]
    [:text (:text todo)]])

(defn todo-list []
  (reactive-mount-keyed! parent [todos]
    (fn []
      (into [:column {}]
            (map todo-item @todos)))))

;; Same behavior as Reagent! ✅
```

**No DSL changes needed!** Just use keys and keyed mounting.

## Practical Implementation

Let me show you what Phase 2.5 would look like:

```clojure
(ns hyprclj.keyed-reactive
  "Keyed reconciliation for stable list items.")

(defrecord VNode [type key props children native-ref])

(defn extract-key [hiccup]
  "Get key from metadata or generate one"
  (or (:key (meta hiccup))
      (hash hiccup)))

(defn compile-with-key [hiccup]
  "Compile hiccup to VNode with key tracking"
  (let [compiled (compile-element hiccup)
        key (extract-key hiccup)]
    {:key key
     :hiccup hiccup
     :native compiled}))

(defn reconcile-keyed-list [parent old-nodes new-hiccup-list]
  (let [old-by-key (into {} (map (fn [n] [(:key n) n]) old-nodes))
        new-compiled (map compile-with-key new-hiccup-list)
        new-by-key (into {} (map (fn [n] [(:key n) n]) new-compiled))

        old-keys (set (keys old-by-key))
        new-keys (set (keys new-by-key))

        deleted (clojure.set/difference old-keys new-keys)
        added (clojure.set/difference new-keys old-keys)
        kept (clojure.set/intersection old-keys new-keys)]

    ;; Remove deleted
    (doseq [k deleted]
      (remove-child! parent (:native (old-by-key k))))

    ;; Add new
    (doseq [k added]
      (add-child! parent (:native (new-by-key k))))

    ;; Kept elements are REUSED (stable!)
    ;; Could optionally update their children here

    ;; Return new nodes for next render
    (vals new-by-key)))

(defn reactive-mount-keyed! [parent atoms-vec component-fn]
  (let [current-nodes (atom [])]

    (letfn [(remount! []
              (hypr/add-idle!
                (fn []
                  (let [new-hiccup (component-fn)
                        ;; Extract children from wrapper
                        children (if (and (vector? new-hiccup)
                                        (= (first new-hiccup) :column))
                                   (drop 2 new-hiccup)  ; Skip :column {}
                                   [new-hiccup])
                        new-nodes (reconcile-keyed-list parent @current-nodes children)]
                    (reset! current-nodes new-nodes)))))]

      ;; Initial mount
      (remount!)

      ;; Watch atoms
      (doseq [atm atoms-vec]
        (add-watch atm (gensym)
          (fn [_ _ old new]
            (when (not= old new)
              (remount!))))))))
```

**Usage (unchanged DSL!):**
```clojure
(reactive-mount-keyed! list-container [todos]
  (fn []
    (into [:column {}]
          (for [todo @todos]
            ^{:key (:id todo)}
            [:row {}
              [:button {:on-click #(toggle! (:id todo))}]
              [:text (:text todo)]]))))
```

**Result:**
- Buttons stable across toggles ✅
- No crashes ✅
- DSL unchanged ✅
- ~200 LOC ✅

## Bottom Line

### Phase 2.5 (Keyed): ~200 LOC, 2-4 hours
- **Automatic with keys** ✅
- **DSL stays simple** (just add ^{:key}) ✅
- **Solves practical issues** ✅
- **Like Reagent!** ✅

### Phase 3 (Full VDOM): ~800 LOC, 1-2 weeks
- Property diffing
- Full React parity
- Marginal benefit given Hyprtoolkit limitations

## My Recommendation

**Implement Phase 2.5 NOW:**
- Small effort
- Big practical benefit
- Enables fully functional TODO app
- Familiar pattern from Reagent
- No DSL complications

**Skip Phase 3 FOR NOW:**
- High complexity
- Limited benefit (Hyprtoolkit can't update in-place anyway)
- Can add later if needed
- Phase 2.5 is "good enough"

**Want me to implement Phase 2.5 keyed reconciliation?** It would make the TODO app fully functional with per-item buttons that don't crash!
