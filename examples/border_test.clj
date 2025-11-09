(ns border-test
  "Test rectangles for borders and backgrounds."
  (:require [hyprclj.core :as hypr]
            [hyprclj.elements :as elem]
            [hyprclj.util :as util]))

(defn -main [& args]
  (println "Testing rectangles for borders/backgrounds")

  (hypr/create-backend!)

  (let [w 600
        h 400
        window (hypr/create-window
                {:title "Border Test"
                 :size [w h]
                 :on-close (fn [_] (util/exit-clean!))})]

    (let [root (hypr/root-element window)
          ;; Main column
          column (elem/column-layout {:gap 15 :margin 20 :size [w h]})]

      ;; Set absolute positioning
      (elem/set-position-mode! column 0)
      (elem/set-absolute-position! column 0 0)

      ;; Add title
      (elem/add-child! column
        (elem/text {:content "BORDER & BACKGROUND TEST" :font-size 24}))

      ;; Red border rectangle
      (elem/add-child! column
        (elem/rectangle {:color [50 50 50 255]
                         :border-color [255 0 0 255]
                         :border 3
                         :rounding 5
                         :size [500 60]}))

      ;; Blue background rectangle
      (elem/add-child! column
        (elem/rectangle {:color [0 100 200 255]
                         :size [500 60]}))

      ;; Green border, rounded
      (elem/add-child! column
        (elem/rectangle {:color [40 40 40 255]
                         :border-color [0 255 0 255]
                         :border 2
                         :rounding 15
                         :size [500 60]}))

      ;; Add to root
      (elem/add-child! root column))

    (hypr/open-window! window)
    (hypr/enter-loop!)))

(comment
  (-main)
  )
