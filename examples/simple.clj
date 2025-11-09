(ns simple
  "Simplest possible Hyprclj example."
  (:require [hyprclj.core :as hypr]
            [hyprclj.dsl :refer [compile-element mount!]]
            [hyprclj.util :as util]))

(defn -main [& args]
  ;; Create backend
  (hypr/create-backend!)

  ;; Create window
  (let [window (hypr/create-window
                {:title "Simple Hyprclj App"
                 :size [400 300]
                 :on-close (fn [w]
                             (println "\nWindow closing...")
                             (util/exit-clean!))})]

    ;; Create UI: just a centered text and button
    (mount! (hypr/root-element window)
            [:column {:gap 20 :margin 50}
             [:text {:content "Hello from Hyprclj!"
                     :font-size 24}]
             [:text {:content "This is a Clojure app running on Hyprtoolkit"
                     :font-size 14}]
             [:button {:label "Click me!"
                       :size [150 40]
                       :on-click #(println "Button clicked!")}]])

    ;; Open and run
    (hypr/open-window! window)
    (hypr/enter-loop!)))

(comment
  (-main)
  )
