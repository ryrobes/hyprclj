(ns sized-mount-test
  "Test using mount! with window size parameter."
  (:require [hyprclj.core :as hypr]
            [hyprclj.dsl :refer [mount!]]
            [hyprclj.util :as util]))

(defn -main [& args]
  (println "Testing mount! with window-size parameter...")

  (hypr/create-backend!)

  (let [window-width 600
        window-height 400
        window (hypr/create-window
                {:title "Sized Mount Test"
                 :size [window-width window-height]
                 :on-close (fn [w]
                             (println "\nWindow closing...")
                             (util/exit-clean!))})]

    ;; Mount with window size - this creates a wrapper
    (mount! (hypr/root-element window)
            [:column {:gap 15 :margin 20 :grow true}
             [:text {:content "Sized Mount Test"
                     :font-size 24}]
             [:text {:content "Content should fill the window!"
                     :font-size 14}]
             [:text {:content "The column has :grow true"
                     :font-size 14}]
             [:button {:label "Click me!"
                       :size [150 40]
                       :on-click #(println "Button clicked!")}]]
            [window-width window-height])  ; Pass window size here!

    (println "Mounted with wrapper size:" window-width "x" window-height)

    (hypr/open-window! window)
    (hypr/enter-loop!)))

(comment
  (-main)
  )
