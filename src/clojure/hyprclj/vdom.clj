(ns hyprclj.vdom
  "Virtual DOM - Data-driven declarative UI with automatic reconciliation.

   Single atom, pure render function, automatic efficient updates.
   Like Reagent/Re-frame but for native Wayland GUIs!"
  (:require [hyprclj.elements :as el]
            [hyprclj.dsl :as dsl]
            [hyprclj.core :as hypr]
            [clojure.set :as set]))

;; ===== VNode (Virtual Node) =====

(defrecord VNode [key path hiccup native-element child-vnodes])

;; Helper: Check if hiccup is a container element
(defn container-element? [hiccup]
  (when (vector? hiccup)
    (let [tag (first hiccup)]
      (contains? #{:v-box :h-box :column :row :box} tag))))

;; Helper: Extract children from hiccup (handles props, :children prop)
(defn extract-children [hiccup]
  (let [[_ & args] hiccup]
    (if (map? (first args))
      ;; Has props map
      (if (:children (first args))
        (:children (first args))  ; Use :children prop!
        (vec (rest args)))         ; Use rest args
      ;; No props
      (vec args))))

;; Debug: Print tree structure
(defn print-vnode-tree
  "Print the vnode tree for debugging"
  ([vnodes] (print-vnode-tree vnodes 0))
  ([vnodes depth]
   (doseq [vnode vnodes]
     (let [indent (apply str (repeat depth "  "))
           tag (when (vector? (:hiccup vnode)) (first (:hiccup vnode)))
           children (:child-vnodes vnode)]
       (println (str indent "- key:" (:key vnode) " tag:" tag
                     " children:" (if children (count children) "nil")))
       (when (seq children)
         (print-vnode-tree children (inc depth)))))))

(defn make-vnode
  "Create a VNode with auto-generated key if not provided."
  [hiccup path]
  (let [user-key (:key (meta hiccup))
        auto-key (or user-key (hash [path (first hiccup)]))  ; Deterministic from path + tag
        native-elem (dsl/compile-element hiccup)]
    (->VNode auto-key path hiccup native-elem nil)))

;; ===== Reconciliation =====

(defn reconcile!
  "Reconcile old and new hiccup trees.

   Strategy:
   - Match by key (user-provided or auto-generated)
   - Reuse unchanged elements
   - Rebuild changed elements
   - Add new, queue old for removal (triple buffer)
   - IMPORTANT: Fully rebuild container if children reordered (accept cost)"
  ([parent old-vnodes new-hiccup-list path]
   (reconcile! parent old-vnodes new-hiccup-list path nil))
  ([parent old-vnodes new-hiccup-list path pending-cleanup]

  (let [;; Build map of old vnodes by key
        old-by-key (into {} (map (fn [v] [(:key v) v]) old-vnodes))

        ;; Create vnodes for new hiccup (with auto-keys)
        new-vnodes-with-keys (mapv (fn [idx hiccup]
                                     (let [child-path (conj path idx)
                                           user-key (:key (meta hiccup))
                                           auto-key (or user-key (hash [child-path (first hiccup)]))]
                                       {:key auto-key
                                        :path child-path
                                        :hiccup hiccup}))
                                   (range)
                                   new-hiccup-list)

        ;; Determine changes
        old-keys (set (keys old-by-key))
        new-keys (set (map :key new-vnodes-with-keys))
        new-key-order (mapv :key new-vnodes-with-keys)
        old-key-order (mapv :key old-vnodes)

        deleted-keys (set/difference old-keys new-keys)
        added-keys (set/difference new-keys old-keys)
        kept-keys (set/intersection old-keys new-keys)

        ;; Check if order changed (even for kept items)
        order-changed? (not= (filter kept-keys new-key-order)
                            (filter kept-keys old-key-order))]

    ;; (println "[superDOM] Reconcile:" (count old-vnodes) "old →" (count new-vnodes-with-keys) "new"
    ;;          "| Added:" (count added-keys) "Deleted:" (count deleted-keys) "Kept:" (count kept-keys))

    ;; If order changed, do full rebuild (simpler than complex reordering)
    (if (and order-changed? (seq kept-keys))
      (do
        (println "[VDOM] ⚠️  Order changed → FULL REBUILD")
        ;; Remove all old elements
        (doseq [vnode old-vnodes]
          (when (:native-element vnode)
            (el/remove-child! parent (:native-element vnode))))
        ;; Create all new elements
        (mapv (fn [{:keys [key path hiccup]}]
                (println "[VDOM]   ✨ Create key" key)
                (let [native-elem (dsl/compile-element hiccup)]
                  (when native-elem
                    (el/add-child! parent native-elem))
                  ;; Track empty child-vnodes for containers (filled on first update)
                  (->VNode key path hiccup native-elem (when (container-element? hiccup) []))))
              new-vnodes-with-keys))

      ;; Order unchanged - smart reconcile
      (do
        ;; Remove deleted (queue for triple buffer if provided)
        (doseq [k deleted-keys]
          (println "[VDOM]   🗑️  Delete key" k)
          (when-let [old-vnode (old-by-key k)]
            (when (:native-element old-vnode)
              (if pending-cleanup
                (swap! pending-cleanup conj (:native-element old-vnode))
                (el/remove-child! parent (:native-element old-vnode))))))

        ;; Process each new item
        (mapv (fn [{:keys [key path hiccup]}]
                (if (contains? kept-keys key)
                  ;; Kept - check if changed
                  (let [old-vnode (old-by-key key)
                        old-hiccup (:hiccup old-vnode)]
                    (if (= old-hiccup hiccup)
                      ;; Unchanged - reuse!
                      (do
                        (println "[VDOM]   ♻️  Reuse key" key)
                        old-vnode)
                      ;; Changed - rebuild with delayed removal (anti-flicker)
                      (do
                        (println "[superDOM]    Rebuild key" key)
                        (let [old-elem (:native-element old-vnode)
                              new-elem (dsl/compile-element hiccup)]
                          ;; DOUBLE BUFFER: Add new, then async remove old
                          (when new-elem (el/add-child! parent new-elem))
                          (when old-elem
                            (hypr/add-idle! #(el/remove-child! parent old-elem)))
                          (->VNode key path hiccup new-elem nil)))))
                  ;; Added - create new
                  ;; NOTE: dsl/compile-element already adds children, so we don't reconcile!
                  ;; We only track that this is a new container for future reconciliations.
                  (do
                    (println "[VDOM]   ✨ Add key" key)
                    (let [native-elem (dsl/compile-element hiccup)]
                      (when native-elem
                        (el/add-child! parent native-elem))
                      ;; For containers, track empty child-vnodes (will be filled on first update)
                      (->VNode key path hiccup native-elem (when (container-element? hiccup) []))))))
              new-vnodes-with-keys))))))

;; ===== Main VDOM Mount =====

(defn vdom-mount!
  "Mount a virtual DOM - single state atom, pure render function.

   The Reagent way: Just data → UI!

   Args:
     parent - Root element
     app-state - Single atom containing all app state
     render-fn - Pure function: (fn [state [width height]] hiccup)
     window - Window (for resize handling)

   Example:
     (def app-state (atom {:count 0}))

     (vdom-mount! root app-state
       (fn [state [w h]]
         [:v-box {:position :absolute}
           [:text (str \"Count: \" (:count state))]
           [:button {:label \"+\" :on-click #(swap! app-state update :count inc)}]])
       window)"
  ([parent app-state render-fn window]
   (let [current-vnodes (atom [])
         pending-cleanup (atom [])  ; Elements to remove on next update (triple buffer!)
         window-size (atom [700 500])]  ; Default, updated on resize

     ;; Watch app-state for data changes
     (add-watch app-state :vdom-reconcile
       (fn [_ _ old-state new-state]
         (when (not= old-state new-state)
           ;(println "[VDOM]  State changed" (keys (filter (fn [[k v]] (not= (get old-state k) v)) new-state)))
           ;; TRIPLE BUFFER: Clean up elements from PREVIOUS update first
           (when (seq @pending-cleanup)
             ;(println "[VDOM] 🧹 Cleanup" (count @pending-cleanup) "old elements from previous update")
             (doseq [elem @pending-cleanup]
               (el/remove-child! parent elem))
             (reset! pending-cleanup []))
           ;; Now reconcile (will queue new elements for cleanup)
           (let [new-hiccup (render-fn new-state @window-size)
                 new-vnodes (reconcile! parent @current-vnodes [new-hiccup] [] pending-cleanup)]
             (reset! current-vnodes new-vnodes)))))

     ;; Enable window resize support (directly reconcile, don't use enable-responsive-root!)
     (.setResizeListener window
       (reify org.hyprclj.bindings.Window$ResizeListener
         (onResize [_ width height]
           (when (and (pos? width) (pos? height))
             (let [new-size [width height]]
               (when (not= new-size @window-size)
                 (println "[VDOM] 📐 Window resized to" width "x" height)
                 (reset! window-size new-size)
                 ;; Full re-render on resize (all dimensions change!)
                 (let [new-hiccup (render-fn @app-state new-size)
                       new-vnodes (reconcile! parent @current-vnodes [new-hiccup] [])]
                   (reset! current-vnodes new-vnodes))))))))

     ;; Initial render
     (println "[VDOM] 🎬 Initial render with size:" @window-size)
     (let [initial-hiccup (render-fn @app-state @window-size)
           initial-vnodes (reconcile! parent @current-vnodes [initial-hiccup] [])]
       (println "[VDOM] ✅ Initial render complete -" (count initial-vnodes) "root vnodes")
       (println "[VDOM] 🌳 VNode tree structure:")
       (print-vnode-tree initial-vnodes)
       (reset! current-vnodes initial-vnodes))

     ;; Return cleanup function
     (fn cleanup! []
       (remove-watch app-state :vdom-reconcile)
       (remove-watch window-size :vdom-resize)))))

;; ===== Simplified API =====

(defn run-app!
  "Run a virtual DOM app - the simplest API!

   Just provide:
   - Initial state (atom)
   - Pure render function
   - Window config

   Example:
     (run-app!
       (atom {:count 0})
       (fn [state [w h]]
         [:v-box {:position :absolute}
           [:text (str (:count state))]
           [:button {:label \"+\" :on-click #(swap! *app-state* update :count inc)}]])
       {:title \"Counter\" :size [400 300]})"
  [app-state render-fn window-opts]
  (hypr/create-backend!)
  (let [window (hypr/create-window (merge {:title "Hyprclj App"
                                            :size [700 500]}
                                           window-opts))
        root (hypr/root-element window)]

    ;; Mount virtual DOM
    (vdom-mount! root app-state render-fn window)

    ;; Open and run
    (hypr/open-window! window)
    (hypr/enter-loop!)))
