(ns border-test-responsive
  "Responsive border test with window-sized containers."
  (:require [hyprclj.core :as hypr]
            [hyprclj.dsl :refer [mount!]]
            [hyprclj.util :as util]))

(defn ui-component [[w h]]
  [:v-box {:gap 15
           :margin 20
           :position :absolute}  ; Top-left positioned

   [:text {:content "RESPONSIVE BORDER TEST" :font-size 24}]
   [:text {:content (str "Window: " w "x" h " - Borders grow with window!") :font-size 14}]

   ;; Red border rectangle - grows with window
   [:rectangle {:color [50 50 50 255]
                :border-color [255 0 0 255]
                :border 3
                :rounding 5
                :size [(- w 40) 60]}]  ; Width = window width - margins

   ;; Blue background rectangle
   [:rectangle {:color [0 100 200 255]
                :size [(- w 40) 60]}]

   ;; Green border, rounded
   [:rectangle {:color [40 40 40 255]
                :border-color [0 255 0 255]
                :border 2
                :rounding 15
                :size [(- w 40) 60]}]

   [:text {:content "^ Borders resize with window ^" :font-size 12}]])

(defn -main [& args]
  (println "=== Responsive Border Test ===")

  (hypr/create-backend!)

  (let [w 600
        h 400
        window (hypr/create-window
                {:title "Responsive Borders"
                 :size [w h]
                 :on-close (fn [_] (util/exit-clean!))})]

    ;; Enable responsive rendering
    (hypr/enable-responsive-root! window
      ui-component
      {:position :absolute})

    (hypr/open-window! window)
    (hypr/enter-loop!)))

(comment
  (-main)
  )
