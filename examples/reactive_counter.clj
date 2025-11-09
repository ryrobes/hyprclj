(ns reactive-counter
  "Example showing reactive state management."
  (:require [hyprclj.core :as hypr]
            [hyprclj.dsl :refer [ratom compile-element mount! defcomponent]]
            [hyprclj.util :as util]))

;; Global reactive state
(def counter (ratom 0))

;; Reactive component (accepts props map even if unused)
(defcomponent counter-display [_props]
  [:text {:content (str "Count: " @counter)
          :font-size 32}])

(defn -main [& args]
  (hypr/create-backend!)

  (let [window (hypr/create-window
                {:title "Reactive Counter"
                 :size [400 200]
                 :on-close (fn [w]
                             (println "\nWindow closing...")
                             (util/exit-clean!))})]

    ;; Mount reactive UI
    (mount! (hypr/root-element window)
            [:column {:gap 15 :margin 30}
             [counter-display]

             [:row {:gap 10}
              [:button {:label "Increment"
                        :size [100 40]
                        :on-click #(swap! counter inc)}]
              [:button {:label "Decrement"
                        :size [100 40]
                        :on-click #(swap! counter dec)}]
              [:button {:label "Reset"
                        :size [100 40]
                        :on-click #(reset! counter 0)}]]

             [:text {:content "Click buttons to update the counter"
                     :font-size 12}]])

    (hypr/open-window! window)
    (hypr/enter-loop!)))

(comment
  (-main)
  )
