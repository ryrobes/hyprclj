(ns simple-clean
  "Simple example with clean quit button instead of window close."
  (:require [hyprclj.core :as hypr]
            [hyprclj.dsl :refer [compile-element mount!]]
            [hyprclj.util :as util]))

(defn -main [& args]
  ;; Create backend
  (hypr/create-backend!)

  ;; Create window WITHOUT close handler
  (let [window (hypr/create-window
                {:title "Simple Hyprclj App"
                 :size [400 300]})]

    ;; Create UI with a Quit button
    (mount! (hypr/root-element window)
            [:column {:gap 20 :margin 50}
             [:text {:content "Hello from Hyprclj!"
                     :font-size 24}]
             [:text {:content "This is a Clojure app running on Hyprtoolkit"
                     :font-size 14}]
             [:button {:label "Click me!"
                       :size [150 40]
                       :on-click #(println "Button clicked!")}]
             [util/make-quit-button {:label "Quit" :size [150 40]}]])

    ;; Open and run
    (hypr/open-window! window)
    (hypr/enter-loop!)))

(comment
  (-main)
  )
