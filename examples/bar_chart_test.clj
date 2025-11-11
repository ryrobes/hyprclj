(ns bar-chart-test
  "Debug bar chart rendering."
  (:require [hyprclj.core :as core]
            [hyprclj.dsl :refer [mount!]]
            [hyprclj.charts :as charts]))

(defn ui-component []
  [:v-box {:gap 20 :margin 20}
   [:text {:content "Bar Chart Debug" :font-size 18}]

   ;; Simple manual bars test
   [:v-box {:gap 5}
    [:text {:content "Manual bars (should work):" :font-size 14}]
    [:h-box {:gap 5 :size [300 100] :align :bottom}
     [:rectangle {:color [255 100 100 255] :size [25 80]}]
     [:rectangle {:color [100 255 100 255] :size [25 40]}]
     [:rectangle {:color [100 100 255 255] :size [25 60]}]
     [:rectangle {:color [255 255 100 255] :size [25 90]}]
     [:rectangle {:color [255 100 255 255] :size [25 50]}]]]

   ;; Bar chart from library
   [:v-box {:gap 5}
    [:text {:content "Bar chart (library):" :font-size 14}]
    (charts/bar-chart {:data [8 4 6 9 5]
                       :width 300
                       :height 100
                       :color [100 150 255 220]
                       :gap 5})]

   ;; Try with :children explicitly
   [:v-box {:gap 5}
    [:text {:content "Explicit children test:" :font-size 14}]
    [:h-box {:gap 5
             :size [300 100]
             :align :bottom
             :children [[:rectangle {:color [255 100 100 255] :size [25 80]}]
                        [:rectangle {:color [100 255 100 255] :size [25 40]}]
                        [:rectangle {:color [100 100 255 255] :size [25 60]}]
                        [:rectangle {:color [255 255 100 255] :size [25 90]}]
                        [:rectangle {:color [255 100 255 255] :size [25 50]}]]}]]])

(defn -main []
  (let [backend (core/create-backend!)
        window (core/create-window {:title "Bar Chart Test" :size [380 450]})]

    (core/open-window! window)
    (mount! (core/root-element window) (ui-component))

    (core/enter-loop!)))
