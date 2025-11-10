(ns charts-demo
  "Comprehensive charts demonstration - showcases all chart types."
  (:require [hyprclj.core :as core]
            [hyprclj.dsl :refer [mount!]]
            [hyprclj.util :as util]
            [hyprclj.charts :as charts]))

;; Sample data
(def sales-data [100 120 110 150 145 160 155 170 165 180])
(def revenue-data [50 75 60 90 85 100 95 110 105 120])
(def cpu-usage [45 48 52 49 53 51 55 52 50 48])
(def memory-usage [60 62 65 63 68 70 72 69 71 73])
(def temperature-data [20 22 21 25 28 30 29 31 33 32])

(defn ui-component []
  [:v-box {:gap 15 :margin 20}
   ;; Title
   [:text {:content "Native Charts & Graphs Demo" :font-size 20}]

   ;; Line chart
   [:v-box {:gap 5}
    [:text {:content "Sales Trend (Line Chart)" :font-size 14}]
    (charts/line-chart {:data sales-data
                        :width 400
                        :height 150
                        :color [100 150 255 255]
                        :thick 3})]

   ;; Bar chart
   [:v-box {:gap 5}
    [:text {:content "Monthly Revenue (Bar Chart)" :font-size 14}]
    (charts/bar-chart {:data revenue-data
                       :width 400
                       :height 120
                       :color [255 150 100 220]
                       :gap 4})]

   ;; Sparklines
   [:v-box {:gap 8}
    [:text {:content "System Metrics (Sparklines)" :font-size 14}]
    [:h-box {:gap 10}
     [:text {:content "CPU:"}]
     (charts/sparkline {:data cpu-usage
                        :width 100
                        :height 25
                        :color [100 200 100 255]
                        :type :line})]
    [:h-box {:gap 10}
     [:text {:content "MEM:"}]
     (charts/sparkline {:data memory-usage
                        :width 100
                        :height 25
                        :color [200 100 100 255]
                        :type :bar})]]

   ;; Progress bars
   [:v-box {:gap 8}
    [:text {:content "Progress Indicators" :font-size 14}]
    [:h-box {:gap 5}
     [:text {:content "Task 1:"}]
     (charts/progress-bar {:value 75
                           :width 200
                           :height 20
                           :color [100 200 100 255]
                           :bg-color [50 50 50 100]})]
    [:h-box {:gap 5}
     [:text {:content "Task 2:"}]
     (charts/progress-bar {:value 45
                           :width 200
                           :height 20
                           :color [255 180 50 255]
                           :bg-color [50 50 50 100]})]
    [:h-box {:gap 5}
     [:text {:content "Task 3:"}]
     (charts/progress-bar {:value 90
                           :width 200
                           :height 20
                           :color [100 150 255 255]
                           :bg-color [50 50 50 100]})]]

   ;; Multi-line chart
   [:v-box {:gap 5}
    [:text {:content "Comparison (Multi-line)" :font-size 14}]
    (charts/multi-line-chart
      {:series [{:data sales-data
                 :color [100 150 255 255]
                 :thick 2}
                {:data revenue-data
                 :color [255 150 100 255]
                 :thick 2}]
       :width 400
       :height 150})]])

(defn -main []
  (let [backend (core/create-backend!)
        window (core/create-window {:title "Charts Demo" 
                                    :on-close (fn [_] (util/exit-clean!))
                                    :size [480 950]})]

    (core/open-window! window)
    (mount! (core/root-element window) (ui-component))

    (core/enter-loop!)))
