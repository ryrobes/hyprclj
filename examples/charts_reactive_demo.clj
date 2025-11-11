(ns charts-reactive-demo
  "Reactive charts demonstration - live updating charts."
  (:require [hyprclj.core :as core]
            [hyprclj.dsl :refer [mount!]]
            [hyprclj.util :as util]
            [hyprclj.charts :as charts]
            [hyprclj.vdom :as vdom])
  (:import [java.util Random]))

(def random (Random.))

;; App state with live data
(def app-state
  (atom {:sales (vec (repeatedly 10 #(+ 100 (.nextInt random 100))))
         :revenue (vec (repeatedly 10 #(+ 50 (.nextInt random 80))))
         :cpu (vec (repeatedly 10 #(+ 40 (.nextInt random 20))))
         :memory (vec (repeatedly 10 #(+ 55 (.nextInt random 25))))
         :progress-1 (.nextInt random 100)
         :progress-2 (.nextInt random 100)
         :progress-3 (.nextInt random 100)
         :tick 0
         :fps 0
         :last-update-time (System/currentTimeMillis)}))

(defn add-random-value
  "Add a new random value to a data series and keep last 10 values."
  [data-vec base variance]
  (vec (take-last 10 (conj data-vec (+ base (.nextInt random variance))))))

(defn update-data!
  "Update all data series with new random values and calculate FPS."
  []
  (let [now (System/currentTimeMillis)
        new-state (swap! app-state
                         (fn [state]
                           (let [time-delta (- now (:last-update-time state))
                                 fps (if (pos? time-delta)
                                       (int (/ 1000.0 time-delta))
                                       0)]
                             (-> state
                                 (update :sales add-random-value 100 100)
                                 (update :revenue add-random-value 50 80)
                                 (update :cpu add-random-value 40 20)
                                 (update :memory add-random-value 55 25)
                                 (assoc :progress-1 (.nextInt random 100))
                                 (assoc :progress-2 (.nextInt random 100))
                                 (assoc :progress-3 (.nextInt random 100))
                                 (update :tick inc)
                                 (assoc :fps fps)
                                 (assoc :last-update-time now)))))]
    (println "Updated! Tick:" (:tick new-state) "FPS:" (:fps new-state))
    new-state))

(defn ui-component [state [w h]]
  (let [content-w (- w 40)
        chart-w (max 200 (- content-w 20))
        chart-h-small (max 100 (int (* h 0.12)))
        chart-h-med (max 120 (int (* h 0.15)))
        chart-h-large (max 150 (int (* h 0.18)))
        progress-w (max 150 (int (* content-w 0.6)))]

    [:v-box {:position :absolute :gap 15 :margin 20}
     ;; Header with FPS counter
     [:h-box {:gap 10}
      [:text {:content (str "Live Charts (tick: " (:tick state) ")")
              :font-size 20}]
      [:rectangle {:color [0 0 0 0] :size [20 1]}]
      [:text {:content (str "FPS: " (:fps state))
              :font-size 16
              :color [100 255 100 255]}]]

     ;; Line chart
     [:v-box {:gap 5}
      [:text {:content "Sales Trend (Line Chart)" :font-size 14}]
      (charts/line-chart {:data (:sales state)
                          :width chart-w
                          :height chart-h-large
                          :color [100 150 255 255]
                          :thick 3})]

     ;; Bar chart
     [:v-box {:gap 5}
      [:text {:content "Revenue (Bar Chart)" :font-size 14}]
      (charts/bar-chart {:data (:revenue state)
                         :width chart-w
                         :height chart-h-med
                         :color [255 150 100 220]
                         :gap 4})]

     ;; Sparklines
     [:v-box {:gap 8}
      [:text {:content "System Metrics (Sparklines)" :font-size 14}]
      [:h-box {:gap 10}
       [:text {:content "CPU:"}]
       (charts/sparkline {:data (:cpu state)
                          :width 100
                          :height 25
                          :color [100 200 100 255]
                          :type :line})]
      [:h-box {:gap 10}
       [:text {:content "MEM:"}]
       (charts/sparkline {:data (:memory state)
                          :width 100
                          :height 25
                          :color [200 100 100 255]
                          :type :bar})]]

     ;; Progress bars
     [:v-box {:gap 8}
      [:text {:content "Progress Indicators" :font-size 14}]
      [:h-box {:gap 5}
       [:text {:content "Task 1:"}]
       (charts/progress-bar {:value (:progress-1 state)
                             :width progress-w
                             :height 20
                             :color [100 200 100 255]
                             :bg-color [50 50 50 100]})]
      [:h-box {:gap 5}
       [:text {:content "Task 2:"}]
       (charts/progress-bar {:value (:progress-2 state)
                             :width progress-w
                             :height 20
                             :color [255 180 50 255]
                             :bg-color [50 50 50 100]})]
      [:h-box {:gap 5}
       [:text {:content "Task 3:"}]
       (charts/progress-bar {:value (:progress-3 state)
                             :width progress-w
                             :height 20
                             :color [100 150 255 255]
                             :bg-color [50 50 50 100]})]]

     ;; Multi-line chart
     [:v-box {:gap 5}
      [:text {:content "Comparison (Multi-line)" :font-size 14}]
      (charts/multi-line-chart
        {:series [{:data (:sales state)
                   :color [100 150 255 255]
                   :thick 2}
                  {:data (:revenue state)
                   :color [255 150 100 255]
                   :thick 2}]
         :width chart-w
         :height chart-h-large})]]))

(defn -main []
  ;; Create backend first
  (core/create-backend!)

  ;; Create window
  (let [window (core/create-window {:title "Live Charts Demo"
                                    :on-close (fn [_] (util/exit-clean!))
                                    :size [480 950]})
        root (core/root-element window)]

    ;; Mount VDOM with reactive state
    (vdom/vdom-mount! root app-state ui-component window)

    ;; Set up repeating data update timer
    (letfn [(schedule-update! []
              (core/add-timer! 18 (fn []
                                      (update-data!)
                                      (schedule-update!))))]
      (schedule-update!))

    ;; Open window and start event loop
    (core/open-window! window)
    (core/enter-loop!)))
