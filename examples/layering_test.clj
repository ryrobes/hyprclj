(ns layering-test
  "Test if absolute positioning creates layers."
  (:require [hyprclj.core :as hypr]
            [hyprclj.elements :as elem]
            [hyprclj.util :as util]))

(defn -main [& args]
  (println "Testing absolute positioning layering...")

  (hypr/create-backend!)

  (let [w 600
        h 400
        window (hypr/create-window
                {:title "Layering Test"
                 :size [w h]
                 :on-close (fn [_] (util/exit-clean!))})]

    (let [root (hypr/root-element window)]

      ;; Layer 1: Big red rectangle (background)
      (let [bg (elem/rectangle {:color [200 50 50 255]
                                :size [300 200]})]
        (elem/set-position-mode! bg 0)
        (elem/set-absolute-position! bg 50 50)  ; Positioned at 50,50
        (elem/add-child! root bg))

      ;; Layer 2: Text on top of rectangle
      (let [text (elem/text {:content "LAYERED TEXT" :font-size 24})]
        (elem/set-position-mode! text 0)
        (elem/set-absolute-position! text 100 100)  ; On top at 100,100
        (elem/add-child! root text))

      ;; Layer 3: Button on top
      (let [btn (elem/button {:label "Layered Button" :size [150 40] :on-click #(println "Clicked!")})]
        (elem/set-position-mode! btn 0)
        (elem/set-absolute-position! btn 100 150)
        (elem/add-child! root btn)))

    (hypr/open-window! window)
    (hypr/enter-loop!)))

(comment
  (-main)
  )
