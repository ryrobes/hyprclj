# Reconciliation Design for Hyprclj

## Current Approach: Full Remounting

### How It Works Now:

```clojure
(defn update-ui! []
  (mount! root-element (build-ui)))  ; ❌ Recreates everything

;; On button click:
(swap! counter inc)
(hypr/add-idle! #(update-ui!))
```

**Problems**:
- ❌ Destroys and recreates all elements
- ❌ Loses element state
- ❌ Inefficient for large UIs
- ❌ No animations or transitions
- ❌ Expensive JNI calls for unchanged elements

## Desired Approach: Reconciliation (Like React VDOM)

### How It Should Work:

```clojure
(def counter (ratom 0))

(defn my-component []
  [:column {}
   [:text (str "Count: " @counter)]  ; Only this should update
   [:button {:label "Static"}]])     ; Should stay untouched

;; When counter changes:
(swap! counter inc)
;; → Automatic reconciliation
;; → Only the text element updates
;; → Button untouched
```

## The Reconciliation Algorithm

### 1. Virtual DOM Representation

Track the rendered tree in memory:

```clojure
;; Store previous render
(def vdom-tree (atom nil))

;; Each node tracks:
{:type :text
 :props {:content "Count: 0" :font-size 24}
 :native-handle 0x12345  ; Pointer to native element
 :children []
 :key nil}
```

### 2. Re-render Phase

When atom changes:
```clojure
(defn render-component [component-fn]
  (let [old-tree @vdom-tree
        new-tree (compile-to-vdom (component-fn))]  ; New virtual tree
    (reconcile! root-element old-tree new-tree)
    (reset! vdom-tree new-tree)))
```

### 3. Reconciliation (Diffing)

Compare old and new trees:

```clojure
(defn reconcile! [parent old-node new-node]
  (cond
    ;; Same type, same key → Update props
    (and (= (:type old-node) (:type new-node))
         (= (:key old-node) (:key new-node)))
    (do
      (update-props! (:native-handle old-node)
                     (:props old-node)
                     (:props new-node))
      (reconcile-children! (:native-handle old-node)
                          (:children old-node)
                          (:children new-node)))

    ;; Different type → Replace entirely
    :else
    (do
      (remove-element! parent old-node)
      (create-and-add! parent new-node))))
```

### 4. Property Updates

Only update changed properties:

```clojure
(defn update-props! [element-handle old-props new-props]
  (let [changed-keys (keys (merge-with not= old-props new-props))]
    (doseq [k changed-keys]
      (case k
        :content (update-text-content! element-handle (new-props k))
        :font-size (update-font-size! element-handle (new-props k))
        :label (update-button-label! element-handle (new-props k))
        ;; ...
        ))))
```

### 5. Children Reconciliation

Diff and patch children lists:

```clojure
(defn reconcile-children! [parent old-children new-children]
  ;; For each position, reconcile or replace
  (doseq [i (range (max (count old-children) (count new-children)))]
    (cond
      ;; Both exist → reconcile
      (and (< i (count old-children))
           (< i (count new-children)))
      (reconcile! parent
                  (nth old-children i)
                  (nth new-children i))

      ;; Only new → add
      (< i (count new-children))
      (create-and-add! parent (nth new-children i))

      ;; Only old → remove
      :else
      (remove-element! parent (nth old-children i)))))
```

## Required Native Methods

We'd need to add these to update elements in-place:

### Text Updates:
```java
// Text.java
public void setContent(String content) {
    nativeSetContent(nativeHandle, content);
}

public void setFontSize(int size) {
    nativeSetFontSize(nativeHandle, size);
}
```

### Button Updates:
```java
// Button.java
public void setLabel(String label) {
    nativeSetLabel(nativeHandle, label);
}
```

### C++ Implementation:
```cpp
// hyprclj_text.cpp
JNIEXPORT void JNICALL
Java_org_hyprclj_bindings_Text_nativeSetContent(
    JNIEnv* env, jobject obj, jlong handle, jstring content) {

    auto textElement = /* cast from handle */;

    // Rebuild the text element with new content
    auto builder = textElement->rebuild();
    builder->text(/* new content */);
    auto newElement = builder->commence();

    // Replace in parent (complex!)
    // This is the tricky part with Hyprtoolkit
}
```

## Implementation Steps

### Phase 1: VDOM Structure

```clojure
(ns hyprclj.vdom
  "Virtual DOM for efficient reconciliation.")

(defrecord VNode [type props children key native-handle])

(defn hiccup->vdom
  "Convert Hiccup to VNode tree"
  [spec]
  (when (vector? spec)
    (let [[tag & args] spec
          [props children] (if (map? (first args))
                             [(first args) (rest args)]
                             [{} args])]
      (->VNode tag
               props
               (mapv hiccup->vdom children)
               (:key props)
               nil))))  ; Native handle added during mount
```

### Phase 2: Smart Mounting

```clojure
(defn mount-vnode!
  "Mount a VNode and store native handle"
  [parent vnode]
  (let [native-elem (create-element (:type vnode) (:props vnode))]
    (add-child! parent native-elem)
    (assoc vnode :native-handle native-elem)))
```

### Phase 3: Diff Algorithm

```clojure
(defn diff-props [old-props new-props]
  "Return map of changed properties"
  (let [all-keys (set (concat (keys old-props) (keys new-props)))]
    (into {}
          (for [k all-keys
                :when (not= (old-props k) (new-props k))]
            [k (new-props k)]))))

(defn can-reuse? [old-vnode new-vnode]
  "Can we update old-vnode instead of replacing it?"
  (and (= (:type old-vnode) (:type new-vnode))
       (= (:key old-vnode) (:key new-vnode))))
```

### Phase 4: Reconciliation

```clojure
(defn reconcile-vnode! [parent old-vnode new-vnode]
  (cond
    ;; Reusable → update
    (can-reuse? old-vnode new-vnode)
    (let [changed-props (diff-props (:props old-vnode)
                                   (:props new-vnode))]
      ;; Update native element properties
      (update-element-props! (:native-handle old-vnode) changed-props)

      ;; Reconcile children
      (reconcile-children! (:native-handle old-vnode)
                          (:children old-vnode)
                          (:children new-vnode))

      ;; Return updated vnode
      (assoc new-vnode :native-handle (:native-handle old-vnode)))

    ;; Not reusable → replace
    :else
    (do
      (remove-element! parent (:native-handle old-vnode))
      (mount-vnode! parent new-vnode))))
```

### Phase 5: Reactive Integration

```clojure
(defn make-reactive-component [component-fn]
  (let [vdom-atom (atom nil)
        deps-atom (atom #{})]

    (fn render []
      ;; Track what atoms are read
      (let [[result new-deps] (track-derefs component-fn)]

        ;; Set up watches on new dependencies
        (doseq [dep new-deps]
          (add-watch dep ::reconcile
            (fn [_ _ _ _]
              ;; When atom changes, reconcile
              (let [old-vdom @vdom-atom
                    new-vdom (hiccup->vdom (component-fn))]
                (reconcile-vnode! root-element old-vdom new-vdom)
                (reset! vdom-atom new-vdom)))))

        ;; Store vdom
        (reset! vdom-atom (hiccup->vdom result))
        (reset! deps-atom new-deps)

        result))))
```

## Example: Before and After

### Without Reconciliation (Current):

```clojure
(def counter (ratom 0))

(defn ui []
  [:column {}
   [:text (str "Count: " @counter)]
   [:button {:label "Big Complex Button"}]
   [:column {}
     [:text "Child 1"]
     [:text "Child 2"]]])

;; On counter change:
(swap! counter inc)
(mount! root (ui))  ; Recreates 5 elements!
```

**Cost**: Create 5 new native elements, destroy 5 old ones = 10 JNI calls

### With Reconciliation (Goal):

```clojure
(def counter (ratom 0))

;; Automatic reconciliation on atom change
(swap! counter inc)
;; → Diff detects only text content changed
;; → Calls nativeSetContent on text element
;; → Other 4 elements untouched
```

**Cost**: 1 JNI call to update text content!

## Implementation Complexity

### Easy Parts ✅:
- Hiccup → VDOM conversion
- Tree structure tracking
- Diff algorithm (standard algorithm)
- Watch-based triggering

### Hard Parts ⚠️:
- **In-place element updates** - Hyprtoolkit uses builder pattern, hard to update
- **Element replacement** - Need to maintain position in parent
- **Key-based reconciliation** - For efficient list updates
- **Component lifecycle** - Mount, update, unmount hooks

### Hyprtoolkit Challenge:

Most Hyprtoolkit elements use the builder pattern and don't support updates:

```cpp
// Can't do this directly:
textElement->setText("new text");  // ❌ No such method

// Must do this:
auto builder = textElement->rebuild();
builder->text("new text");
auto newElement = builder->commence();
// Then replace in parent!
```

This makes in-place updates complex.

## Workarounds

### Option 1: Smart Remounting

Only remount changed subtrees:

```clojure
(defn smart-update! [path component-fn]
  ;; Only remount the element at 'path'
  (let [target-elem (get-element-at-path root path)]
    (mount! target-elem (component-fn))))

;; Usage:
[:column {}
  [:text {:key :counter} ...]  ; This subtree
  [:button {:key :static} ...]] ; This stays

;; Update just the counter text:
(smart-update! [:counter] #(counter-text))
```

### Option 2: Keyed Elements

Track elements by key and only update those:

```clojure
{:elements {
  :counter-text #<native-handle>
  :button1 #<native-handle>}}

;; Update by key
(update-element! :counter-text
                 {:content "Count: 5"})
```

### Option 3: Manual Updates (Current)

Accept the full remount but optimize:

```clojure
;; Group related UI
(defn counter-display []
  [:text (str "Count: " @counter)])

(defn static-ui []
  [:column {}
    [counter-display]  ; Only this remounts
    static-button      ; Already mounted
    static-column])
```

## Recommended Approach for POC Enhancement

### Step 1: Add Update Methods (Easy)

Add native methods to update common properties:

```java
// Text.java
public void updateContent(String content) {
    // For POC, could just track content and remount this element only
}

// Button.java
public void updateLabel(String label) {
    // Same approach
}
```

### Step 2: Selective Remounting (Medium)

Instead of full tree remount, remount only changed components:

```clojure
(defn render-component [component-id component-fn parent]
  (let [old-tree (get @component-cache component-id)
        new-tree (component-fn)]
    (when (not= old-tree new-tree)
      ;; Only remount if changed
      (clear-children! parent)
      (mount! parent new-tree)
      (swap! component-cache assoc component-id new-tree))))
```

### Step 3: Property-Level Updates (Hard)

Implement true VDOM diffing:

```clojure
(defn diff-and-patch! [old-vnode new-vnode]
  (let [prop-changes (diff-props old-vnode new-vnode)]
    (doseq [[prop-key new-val] prop-changes]
      (update-native-prop! (:handle old-vnode) prop-key new-val))))
```

## Practical POC Enhancement

For a working POC improvement without full VDOM:

```clojure
(ns hyprclj.smart-update
  "Smart selective updates without full reconciliation")

(defonce component-trees (atom {}))

(defn reactive-component
  "Component that only remounts when its dependencies change"
  [id component-fn parent]

  ;; Track dependencies
  (let [[tree deps] (track-derefs component-fn)]

    ;; Watch dependencies
    (doseq [dep deps]
      (add-watch dep id
        (fn [_ _ old new]
          (when (not= old new)
            ;; Remount just this component
            (let [new-tree (component-fn)]
              (when (not= tree new-tree)
                (clear-children! parent)
                (mount! parent new-tree)))))))

    ;; Initial mount
    (mount! parent tree)
    (swap! component-trees assoc id tree)))
```

### Usage:

```clojure
;; Break UI into reactive components
(reactive-component :counter counter-display counter-parent)
(reactive-component :static static-ui static-parent)

;; Now when counter changes, only :counter component remounts!
```

## Example Implementation

Here's what a basic reconciliation could look like:

```clojure
(ns hyprclj.reconcile
  "Basic reconciliation without full VDOM")

(defrecord VNode [type key props children native-ref])

(defn same-node? [old new]
  (and (= (:type old) (:type new))
       (= (:key old) (:key new))))

(defn reconcile-node! [parent-native old new]
  (cond
    ;; Nil cases
    (and (nil? old) new)
    (create-and-mount! parent-native new)

    (and old (nil? new))
    (remove-from-parent! parent-native (:native-ref old))

    (and (nil? old) (nil? new))
    nil

    ;; Same node → update
    (same-node? old new)
    (let [native-ref (:native-ref old)]
      ;; Update changed properties
      (update-if-changed! native-ref :content old new)
      (update-if-changed! native-ref :font-size old new)
      (update-if-changed! native-ref :label old new)

      ;; Reconcile children
      (reconcile-children! native-ref
                          (:children old)
                          (:children new))

      ;; Return updated node
      (assoc new :native-ref native-ref))

    ;; Different node → replace
    :else
    (do
      (remove-from-parent! parent-native (:native-ref old))
      (create-and-mount! parent-native new))))

(defn update-if-changed! [element-ref prop old new]
  (when (not= (get-in old [:props prop])
              (get-in new [:props prop]))
    (case prop
      :content (el/set-text-content! element-ref
                                     (get-in new [:props prop]))
      :label (el/set-button-label! element-ref
                                  (get-in new [:props prop]))
      ;; Add more as needed
      nil)))
```

## What's Needed

### 1. Native Update Methods ⚠️

The biggest challenge: Hyprtoolkit doesn't support in-place updates well.

**Current**:
```cpp
// Can't just update text
textElement->setText("new");  // ❌ Doesn't exist
```

**Need**:
```cpp
// Would need to rebuild
auto builder = textElement->rebuild();
builder->text("new text");
auto newElem = builder->commence();
// Replace in parent - complex!
```

**Workaround**: For POC, could remount just that element:
```clojure
(defn update-text! [text-elem content]
  (let [parent (get-parent text-elem)]
    (remove-child! parent text-elem)
    (add-child! parent (el/text {:content content}))))
```

### 2. Parent Tracking 📝

Need to track each element's parent:

```clojure
(defonce element-parents (atom {}))  ; element → parent

(defn add-child-tracked! [parent child]
  (add-child! parent child)
  (swap! element-parents assoc child parent))
```

### 3. Keys for Lists 🔑

Support React-style keys for efficient list reconciliation:

```clojure
[:column {}
  (for [item items]
    ^{:key (:id item)}
    [:text (:name item)])]
```

## Comparison to React/Reagent

| Feature | React | Reagent | Hyprclj (Current) | Hyprclj (With Reconciliation) |
|---------|-------|---------|-------------------|------------------------------|
| VDOM | ✅ | ✅ | ❌ | ✅ |
| Diffing | ✅ | ✅ | ❌ | ✅ |
| Minimal updates | ✅ | ✅ | ❌ | ✅ |
| Component state | ✅ | ✅ | ❌ | ✅ |
| Keys | ✅ | ✅ | ❌ | ✅ |
| Lifecycle hooks | ✅ | ✅ | ❌ | ✅ |
| Auto re-render | ✅ | ✅ | ❌ Manual | ✅ Auto |

## Effort Estimate

### Basic Reconciliation (200-300 LOC):
- VDOM structure ✅ Easy
- Diff algorithm ✅ Easy
- Property tracking ✅ Easy
- Integration with reactive ⚠️ Medium

### Full Reconciliation (500-800 LOC):
- Everything above
- Children reconciliation ⚠️ Medium
- Key-based matching ⚠️ Medium
- In-place updates ⚠️ Hard (Hyprtoolkit limitation)
- Component lifecycle ✅ Medium
- Performance optimization ⚠️ Medium

### Production-Ready (1500+ LOC):
- Everything above
- Edge cases handled
- Performance optimized
- Comprehensive tests
- Documentation
- Error handling

## Quick Win: Component-Level Granularity

Without full VDOM, you can get 80% of the benefit:

```clojure
(defn counter-app []
  [:column {}
    [reactive-counter]     ; Auto-updates when counter changes
    [static-sidebar]       ; Never remounts
    [reactive-status]])    ; Auto-updates when status changes

;; Each reactive-X is a separate reactive component
;; Changes to counter only remount reactive-counter
;; Much better than remounting everything!
```

**Effort**: ~50-100 LOC, much simpler than full reconciliation!

## Recommendation

For Hyprclj POC → Production:

**Phase 1 (Now)**: Manual remounting ✅ DONE
**Phase 2 (Easy Win)**: Component-level reactivity (50 LOC)
**Phase 3 (Medium)**: Basic reconciliation (300 LOC)
**Phase 4 (Advanced)**: Full VDOM with keys (800 LOC)

**Start with Phase 2** - biggest bang for buck!

---

Would you like me to implement Phase 2 (component-level reactivity)? It would give you automatic updates without the complexity of full VDOM!
