(ns line-test
  "Test the Line drawing primitive."
  (:require [hyprclj.core :as core]
            [hyprclj.dsl :refer [mount!]]
            [hyprclj.elements :as el]))

(defn ui-component []
  [:v-box {:gap 20 :margin 20}
   [:text {:content "Line Element Test" :font-size 18}]

   ;; Simple diagonal line
   [:v-box {:gap 5}
    [:text {:content "Diagonal line:" :font-size 14}]
    [:line {:points [[0 0] [1 1]]
            :color [255 100 100 255]
            :thick 2
            :size [200 200]}]]

   ;; Zigzag line
   [:v-box {:gap 5}
    [:text {:content "Zigzag:" :font-size 14}]
    [:line {:points [[0 0.5] [0.25 0] [0.5 0.5] [0.75 0] [1 0.5]]
            :color [100 150 255 255]
            :thick 3
            :size [300 100]}]]

   ;; Wave-like curve
   [:v-box {:gap 5}
    [:text {:content "Wave:" :font-size 14}]
    [:line {:points [[0 0.5] [0.2 0.3] [0.4 0.5] [0.6 0.7] [0.8 0.5] [1 0.5]]
            :color [100 255 100 255]
            :thick 2
            :size [300 150]}]]])

(defn -main []
  (let [backend (core/create-backend!)
        window (core/create-window {:title "Line Test" :size [400 600]})]

    (core/open-window! window)
    (mount! (core/root-element window) (ui-component))

    (core/enter-loop!)))
