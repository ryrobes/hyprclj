(ns hardcoded-absolute
  "Hardcoded absolute positioning test."
  (:require [hyprclj.core :as hypr]
            [hyprclj.dsl :refer [mount!]]
            [hyprclj.util :as util]))

(defn -main [& args]
  (println "Hardcoded absolute positioning test")

  (hypr/create-backend!)

  (let [w 600
        h 400
        window (hypr/create-window
                {:title "Hardcoded Absolute"
                 :size [w h]
                 :on-close (fn [_] (util/exit-clean!))})]

    (let [root (hypr/root-element window)]
      (println "Mounting with HARDCODED {:position :absolute}")

      (mount! root
              [:column {:gap 10 :margin 10}
               [:text {:content "ABSOLUTE POSITIONING" :font-size 24}]
               [:text {:content "Should be TOP-LEFT!" :font-size 16}]
               [:button {:label "Click" :size [120 40] :on-click #(println "Clicked!")}]]
              [w h]
              {:position :absolute}))  ; HARDCODED!

    (println "Opening window...")
    (hypr/open-window! window)
    (hypr/enter-loop!)))

(comment
  (-main)
  )
