(ns hyprclj.reactive-component
  "Component-level reactivity without full VDOM reconciliation.
   Components automatically remount when their dependencies change."
  (:require [hyprclj.elements :as el]
            [hyprclj.dsl :as dsl]))

;; Track reactive components
(defonce ^:private reactive-components (atom {}))

;; Dynamic var for tracking atom reads
(def ^:dynamic *tracking-derefs* nil)

(defn track-derefs
  "Execute f and track all atom derefs within it.
   Returns [result derefed-atoms]."
  [f]
  (let [derefed (atom #{})]
    (binding [*tracking-derefs* derefed]
      (let [result (f)]
        [result @derefed]))))

;; Override deref behavior when tracking
(defn deref-tracked [x]
  (when (and *tracking-derefs* (instance? clojure.lang.Atom x))
    (swap! *tracking-derefs* conj x))
  (clojure.core/deref x))

;; Monkey-patch deref for tracking (simple approach)
(defmacro with-atom-tracking [& body]
  `(let [original-deref# deref]
     (with-redefs [deref deref-tracked]
       ~@body)))

(defn reactive-component
  "Create a reactive component that auto-updates when dependencies change.

   Args:
     id          - Unique identifier for this component instance
     parent-elem - Native parent element to mount into
     component-fn - Function that returns Hiccup

   The component will:
   1. Track what atoms it reads
   2. Watch those atoms for changes
   3. Automatically remount when any atom changes

   Example:
     (reactive-component :counter
                        parent-element
                        (fn [] [:text (str \"Count: \" @counter)]))"
  [id parent-elem component-fn]

  (let [state (atom {:tree nil
                     :deps #{}
                     :native nil})]

    ;; Function to render and mount
    (letfn [(render-and-mount! []
              (let [[hiccup-tree new-deps] (binding [*tracking-derefs* (atom #{})]
                                             (let [result (component-fn)]
                                               [result @*tracking-derefs*]))]

                ;; Remove old watches
                (doseq [old-dep (:deps @state)]
                  (remove-watch old-dep id))

                ;; Set up new watches
                (doseq [new-dep new-deps]
                  (add-watch new-dep id
                    (fn [_ _ old-val new-val]
                      (when (not= old-val new-val)
                        ;; Dependency changed - remount!
                        (render-and-mount!)))))

                ;; Clear parent and remount
                (el/clear-children! parent-elem)
                (let [compiled (dsl/compile-element hiccup-tree)]
                  (when compiled
                    (el/add-child! parent-elem compiled))

                  ;; Update state
                  (swap! state assoc
                         :tree hiccup-tree
                         :deps new-deps
                         :native compiled))))]

      ;; Initial render
      (render-and-mount!)

      ;; Store component info
      (swap! reactive-components assoc id state)

      ;; Return cleanup function
      (fn cleanup! []
        ;; Remove watches
        (doseq [dep (:deps @state)]
          (remove-watch dep id))
        ;; Remove from registry
        (swap! reactive-components dissoc id)))))

(defn create-reactive-root
  "Create a reactive root component.

   This is a convenience wrapper for mounting a reactive component
   as the root of a window.

   Example:
     (create-reactive-root window
                          (fn []
                            [:column {}
                              [:text (str \"Count: \" @counter)]
                              [:button {:label \"+\"
                                       :on-click #(swap! counter inc)}]]))"
  [window component-fn]
  (reactive-component ::root
                     (-> window (.getRootElement))
                     component-fn))

;; Helper macro for reactive components
(defmacro defreactive
  "Define a reactive component function.

   Example:
     (defreactive my-counter [counter-atom]
       [:text (str \"Count: \" @counter-atom)])

   Usage:
     (reactive-component :my-counter parent (partial my-counter counter))"
  [name args & body]
  `(defn ~name ~args
     ~@body))

(comment
  ;; Example usage:

  (require '[hyprclj.core :as hypr])
  (require '[hyprclj.dsl :refer [ratom]])

  (def counter (ratom 0))

  ;; Create window
  (def window (hypr/create-window {:title "Auto-Update Demo"}))

  ;; Create reactive root - auto-updates when counter changes!
  (create-reactive-root window
    (fn []
      [:column {:gap 10}
       [:text (str "Count: " @counter)]
       [:button {:label "+"
                 :on-click #(swap! counter inc)}]]))

  ;; Now clicking the button automatically updates the UI!
  ;; No manual remounting needed!
  )
