(ns hyprclj.keyed-reactive
  "Phase 2.5: Keyed reconciliation for stable list items.
   Like React - matches elements by key to avoid destroying and recreating them."
  (:require [hyprclj.elements :as el]
            [hyprclj.dsl :as dsl]
            [hyprclj.core :as hypr]
            [clojure.set :as set]))

(defn extract-key
  "Extract key from Hiccup element metadata or generate one.

   Examples:
     ^{:key 123} [:row ...] → 123
     [:row ...] → (hash [:row ...])"
  [hiccup]
  (or (:key (meta hiccup))
      (hash hiccup)))

(defrecord VNode [key hiccup native-element])

(defn compile-vnode
  "Compile Hiccup to VNode with key and native element."
  [hiccup]
  (->VNode (extract-key hiccup)
           hiccup
           (dsl/compile-element hiccup)))

(defn reconcile-children
  "Reconcile old and new children by key.

   Strategy:
   - Match old/new by key
   - Reuse matched elements (stable!)
   - Create new elements for added keys
   - Remove elements for deleted keys

   Returns vector of new VNodes."
  [parent old-vnodes new-hiccup-list]

  (let [;; Build map of old vnodes by key
        old-by-key (into {} (map (fn [v] [(:key v) v]) old-vnodes))

        ;; Extract keys from new hiccup (don't compile yet!)
        new-hiccup-with-keys (mapv (fn [h] {:key (extract-key h) :hiccup h})
                                   new-hiccup-list)

        ;; Determine changes
        old-keys (set (keys old-by-key))
        new-keys (set (map :key new-hiccup-with-keys))

        deleted-keys (set/difference old-keys new-keys)
        added-keys (set/difference new-keys old-keys)
        kept-keys (set/intersection old-keys new-keys)]

    ;; Remove deleted elements
    (doseq [k deleted-keys]
      (when-let [old-vnode (old-by-key k)]
        (when (:native-element old-vnode)
          (el/remove-child! parent (:native-element old-vnode)))))

    ;; Compile and add ONLY new elements
    (let [added-hiccup (filter #(contains? added-keys (:key %)) new-hiccup-with-keys)
          added-vnodes (mapv (fn [{:keys [key hiccup]}]
                               (->VNode key hiccup (dsl/compile-element hiccup)))
                             added-hiccup)]

      ;; Add new elements to parent
      (doseq [vnode added-vnodes]
        (when (:native-element vnode)
          (el/add-child! parent (:native-element vnode))))

      ;; Build result: mix of reused (kept) and new (added)
      (let [added-by-key (into {} (map (fn [v] [(:key v) v]) added-vnodes))

            result-vnodes
            (mapv (fn [{:keys [key hiccup]}]
                    (if (contains? kept-keys key)
                      ;; Check if hiccup changed for kept items
                      (let [old-vnode (old-by-key key)
                            old-hiccup (:hiccup old-vnode)]
                        (if (= old-hiccup hiccup)
                          ;; Unchanged - reuse completely
                          old-vnode
                          ;; Changed - need to update!
                          ;; Rebuild and replace (double-buffering to avoid flicker)
                          (let [new-elem (dsl/compile-element hiccup)
                                old-elem (:native-element old-vnode)]
                            ;; Add new first, then remove old (double-buffer)
                            (when new-elem
                              (el/add-child! parent new-elem))
                            (when old-elem
                              (el/remove-child! parent old-elem))
                            ;; Return updated vnode
                            (->VNode key hiccup new-elem))))
                      ;; Use newly added element
                      (added-by-key key)))
                  new-hiccup-with-keys)]

        result-vnodes))))

(defn reactive-mount-keyed!
  "Mount a component with keyed reconciliation.

   Like reactive-mount! but uses keys to match elements,
   avoiding unnecessary destruction and recreation.

   Args:
     parent-elem  - Parent element to mount into
     atoms-vec    - Vector of atoms to watch
     component-fn - Function returning Hiccup (must return container with children)

   The component-fn should return something like:
     [:column {}
       (for [item @items]
         ^{:key (:id item)}  ; ← Key metadata!
         [:row {} ...])]

   On update:
   - Matches children by key
   - Reuses elements with same key
   - Only creates/destroys for add/delete
   - Buttons stay stable! No crashes!

   Example:
     (reactive-mount-keyed! list-container [todos]
       (fn []
         (into [:column {}]
               (for [todo @todos]
                 ^{:key (:id todo)}
                 [:row {}
                   [:button {:on-click #(toggle! (:id todo))}]
                   [:text (:text todo)]]))))"
  [parent-elem atoms-vec component-fn]

  (let [watch-id (gensym "keyed-watch-")
        current-vnodes (atom [])]

    ;; Function to remount with keyed reconciliation
    (letfn [(remount! []
              (hypr/add-idle!
                (fn []
                  (let [hiccup (component-fn)
                        ;; Extract children from container
                        ;; Expecting [:column {} child1 child2 ...]
                        children (if (and (vector? hiccup)
                                        (keyword? (first hiccup)))
                                   (drop 2 hiccup)  ; Skip tag and props
                                   [hiccup])

                        ;; Reconcile children by key
                        new-vnodes (reconcile-children parent-elem
                                                      @current-vnodes
                                                      (vec children))]

                    ;; Store new vnodes
                    (reset! current-vnodes new-vnodes)))))]

      ;; Initial mount
      (remount!)

      ;; Watch atoms for changes
      (doseq [atm atoms-vec]
        (add-watch atm watch-id
          (fn [_ _ old-val new-val]
            (when (not= old-val new-val)
              (remount!)))))

      ;; Return cleanup function
      (fn cleanup! []
        (doseq [atm atoms-vec]
          (remove-watch atm watch-id))))))

(comment
  ;; Example usage:

  (def todos (ratom [{:id 1 :text "Task 1"}
                     {:id 2 :text "Task 2"}]))

  (reactive-mount-keyed! parent [todos]
    (fn []
      (into [:column {}]
            (for [todo @todos]
              ^{:key (:id todo)}  ; ← Important!
              [:row {}
                [:button {:label "Done"
                          :on-click #(toggle! (:id todo))}]  ; Stable!
                [:text (:text todo)]]))))

  ;; Now when you toggle:
  ;; - todos atom changes
  ;; - Remount triggered
  ;; - Reconciler matches by key
  ;; - Reuses the row element
  ;; - Button not destroyed!
  ;; - NO CRASH! ✅
  )
