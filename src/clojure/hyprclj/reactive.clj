(ns hyprclj.reactive
  "Reactive state management layer for Hyprclj.
   Provides Reagent-style reactivity with atoms and automatic UI updates."
  (:require [hyprclj.elements :as el]))

;; Track which components are watching which atoms
(defonce ^:private watchers (atom {}))

;; Track component state
(defonce ^:private component-cache (atom {}))

(defn- component-id
  "Generate a unique ID for a component instance."
  []
  (str (gensym "component-")))

(defn- track-reactive-read!
  "Track that the current component read from an atom."
  [atm component-id]
  (swap! watchers update atm (fnil conj #{}) component-id))

(defn reactive-atom?
  "Check if a value is a reactive atom."
  [x]
  (instance? clojure.lang.Atom x))

(defn cursor
  "Create a cursor into a nested atom structure.
   Like Reagent's cursor.

   Example:
     (def state (atom {:user {:name \"Alice\"}}))
     (def name-cursor (cursor state [:user :name]))
     @name-cursor ;; => \"Alice\"
     (reset! name-cursor \"Bob\") ;; updates the parent atom"
  ([atm path]
   (reify
     clojure.lang.IDeref
     (deref [_]
       (get-in @atm path))

     clojure.lang.IAtom
     (reset [_ new-value]
       (swap! atm assoc-in path new-value))
     (swap [_ f]
       (swap! atm update-in path f))
     (swap [_ f x]
       (swap! atm update-in path f x))
     (swap [_ f x y]
       (swap! atm update-in path f x y))
     (swap [_ f x y args]
       (apply swap! atm update-in path f x y args)))))

(defn track-derefs
  "Execute f and track all atom derefs within it.
   Returns [result derefed-atoms]."
  [f]
  (let [derefed (atom #{})]
    (with-redefs [deref (fn [x]
                          (when (reactive-atom? x)
                            (swap! derefed conj x))
                          (clojure.core/deref x))]
      (let [result (f)]
        [result @derefed]))))

(defn make-reactive
  "Wrap a component function to make it reactive.
   When any atom it derefs changes, the component will re-render.

   Example:
     (def counter (atom 0))
     (defn my-component []
       (make-reactive
         (fn []
           [:text {:content (str \"Count: \" @counter)}])))"
  [component-fn]
  (let [id (component-id)
        deps-atom (atom #{})
        current-element (atom nil)]

    ;; Function to render and track dependencies
    (fn render-reactive [& args]
      (let [[result new-deps] (track-derefs #(apply component-fn args))]

        ;; Update dependencies
        (let [old-deps @deps-atom]
          ;; Remove old watchers
          (doseq [dep old-deps]
            (swap! watchers update dep disj id))

          ;; Add new watchers
          (doseq [dep new-deps]
            (swap! watchers update dep (fnil conj #{}) id)
            ;; Set up watch to trigger re-render
            (add-watch dep id
                       (fn [_ _ old-val new-val]
                         (when (not= old-val new-val)
                           ;; Trigger re-render
                           ;; Note: This is simplified - real implementation would
                           ;; need to re-mount the component
                           (render-reactive args)))))

          (reset! deps-atom new-deps))

        ;; Store and return result
        (reset! current-element result)
        result))))

;; Ratom - Reactive atom (like Reagent's ratom)
(defn ratom
  "Create a reactive atom. Like a regular atom, but triggers UI updates.

   Example:
     (def count (ratom 0))
     (swap! count inc) ;; UI will update automatically"
  [initial-value]
  (atom initial-value))

;; Higher-order component for reactions
(defn reaction
  "Create a reactive computation that updates when dependencies change.
   Like Reagent's reaction.

   Example:
     (def count (ratom 0))
     (def doubled (reaction (fn [] (* 2 @count))))
     @doubled ;; => 0
     (swap! count inc)
     @doubled ;; => 2"
  [f]
  (let [value (atom nil)
        deps (atom #{})]

    ;; Initial computation
    (let [[result new-deps] (track-derefs f)]
      (reset! value result)
      (reset! deps new-deps)

      ;; Watch dependencies
      (doseq [dep new-deps]
        (add-watch dep (gensym "reaction-")
                   (fn [_ _ old-val new-val]
                     (when (not= old-val new-val)
                       (let [[new-result _] (track-derefs f)]
                         (reset! value new-result)))))))

    value))

(comment
  ;; Example usage:
  (def counter (ratom 0))
  (def doubled (reaction (fn [] (* 2 @counter))))

  @doubled ;; => 0
  (swap! counter inc)
  @doubled ;; => 2
  )
