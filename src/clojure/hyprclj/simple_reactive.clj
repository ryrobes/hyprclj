(ns hyprclj.simple-reactive
  "Simplified component-level reactivity with explicit dependencies."
  (:require [hyprclj.elements :as el]
            [hyprclj.dsl :as dsl]
            [hyprclj.core :as hypr]))

(defn reactive-mount!
  "Mount a component that auto-updates when atoms change.

   Args:
     parent-elem  - Native parent element
     atoms-vec    - Vector of atoms to watch
     component-fn - Function that returns Hiccup (will be called with no args)

   Example:
     (reactive-mount! parent
                     [counter status]  ; Watches these
                     (fn []
                       [:text (str \"Count: \" @counter)]))"
  [parent-elem atoms-vec component-fn]

  (let [watch-id (gensym "reactive-watch-")
        current-element (atom nil)]  ; Track current mounted element

    ;; Function to remount with minimal flicker
    (letfn [(remount! []
              ;; Schedule remount on idle to avoid conflicts with event handlers
              (hypr/add-idle!
                (fn []
                  ;; Build new UI first (before clearing old!)
                  (let [hiccup (component-fn)
                        new-compiled (dsl/compile-element hiccup)
                        old-element @current-element]

                    (when new-compiled
                      ;; Add new element first
                      (el/add-child! parent-elem new-compiled)

                      ;; Then remove old element (double-buffering)
                      (when old-element
                        (el/remove-child! parent-elem old-element))

                      ;; Track new element
                      (reset! current-element new-compiled))))))]

      ;; Initial mount
      (remount!)

      ;; Watch all provided atoms
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
  ;; Example:
  (def counter (atom 0))

  (reactive-mount! parent-element
                  [counter]  ; Watch counter
                  (fn []
                    [:text (str "Count: " @counter)]))

  ;; Now when you do:
  (swap! counter inc)
  ;; The UI auto-updates!
  )
