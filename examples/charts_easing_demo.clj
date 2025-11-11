(ns charts-easing-demo
  "Eased chart animations using proper duration-based easing curves."
  (:require [hyprclj.core :as core]
            [hyprclj.dsl :refer [mount!]]
            [hyprclj.util :as util]
            [hyprclj.charts :as charts]
            [hyprclj.easing :as ease]
            [hyprclj.vdom :as vdom])
  (:import [java.util Random]))

(def random (Random.))

;; Animation config
(def animation-duration 200) ; ms for each animation
(def update-interval 150)     ; ~60fps

;; App state with animation tracking
(def app-state
  (atom {:sales (vec (repeatedly 10 #(+ 100.0 (.nextInt random 100))))
         :sales-target (vec (repeatedly 10 #(+ 100.0 (.nextInt random 100))))
         :sales-start (vec (repeatedly 10 #(+ 100.0 (.nextInt random 100))))
         :sales-anim-start 0

         :revenue (vec (repeatedly 10 #(+ 50.0 (.nextInt random 80))))
         :revenue-target (vec (repeatedly 10 #(+ 50.0 (.nextInt random 80))))
         :revenue-start (vec (repeatedly 10 #(+ 50.0 (.nextInt random 80))))
         :revenue-anim-start 0

         :cpu (vec (repeatedly 10 #(+ 40.0 (.nextInt random 20))))
         :cpu-target (vec (repeatedly 10 #(+ 40.0 (.nextInt random 20))))
         :cpu-start (vec (repeatedly 10 #(+ 40.0 (.nextInt random 20))))
         :cpu-anim-start 0

         :progress-1 0.0
         :progress-1-target 0.0
         :progress-1-start 0.0
         :progress-1-anim-start 0

         :progress-2 0.0
         :progress-2-target 0.0
         :progress-2-start 0.0
         :progress-2-anim-start 0

         :progress-3 0.0
         :progress-3-target 0.0
         :progress-3-start 0.0
         :progress-3-anim-start 0

         :tick 0
         :fps 0
         :last-update-time (System/currentTimeMillis)}))

(defn new-random-targets
  "Generate new random target values for all properties."
  [state now]
  (-> state
      ;; Sales (line chart) - new targets for all 10 points
      (assoc :sales-target (vec (repeatedly 10 #(+ 100.0 (.nextInt random 100)))))
      (assoc :sales-start (:sales state))
      (assoc :sales-anim-start now)

      ;; Revenue (bar chart)
      (assoc :revenue-target (vec (repeatedly 10 #(+ 50.0 (.nextInt random 80)))))
      (assoc :revenue-start (:revenue state))
      (assoc :revenue-anim-start now)

      ;; CPU (sparkline)
      (assoc :cpu-target (vec (repeatedly 10 #(+ 40.0 (.nextInt random 20)))))
      (assoc :cpu-start (:cpu state))
      (assoc :cpu-anim-start now)

      ;; Progress bars
      (assoc :progress-1-target (double (.nextInt random 100)))
      (assoc :progress-1-start (:progress-1 state))
      (assoc :progress-1-anim-start now)

      (assoc :progress-2-target (double (.nextInt random 100)))
      (assoc :progress-2-start (:progress-2 state))
      (assoc :progress-2-anim-start now)

      (assoc :progress-3-target (double (.nextInt random 100)))
      (assoc :progress-3-start (:progress-3 state))
      (assoc :progress-3-anim-start now)))

(defn animate-vector
  "Animate a vector of values using easing."
  [start-vec target-vec start-time current-time duration easing-fn]
  (vec (map (fn [start target]
              (ease/animate start target start-time current-time duration easing-fn))
            start-vec
            target-vec)))

(defn update-animations!
  "Update all animated values based on elapsed time."
  []
  (let [now (System/currentTimeMillis)
        new-state (swap! app-state
                         (fn [state]
                           (let [time-delta (- now (:last-update-time state))
                                 fps (if (pos? time-delta)
                                       (int (/ 1000.0 time-delta))
                                       0)
                                 ;; Set new targets every 100 ticks (with stagger)
                                 new-targets? (zero? (mod (:tick state) 100))]

                             (cond-> state
                               ;; Update FPS
                               true (assoc :fps fps)
                               true (assoc :last-update-time now)
                               true (update :tick inc)

                               ;; Generate new targets periodically
                               new-targets? (new-random-targets now)

                               ;; Animate sales data (ease-out-cubic for smooth deceleration)
                               true (assoc :sales
                                           (animate-vector (:sales-start state)
                                                           (:sales-target state)
                                                           (:sales-anim-start state)
                                                           now
                                                           animation-duration
                                                           ease/ease-out-cubic))

                               ;; Animate revenue data (ease-in-out-quad for balanced motion)
                               true (assoc :revenue
                                           (animate-vector (:revenue-start state)
                                                           (:revenue-target state)
                                                           (:revenue-anim-start state)
                                                           now
                                                           animation-duration
                                                           ease/ease-in-out-quad))

                               ;; Animate CPU data (ease-out-sine for gentle motion)
                               true (assoc :cpu
                                           (animate-vector (:cpu-start state)
                                                           (:cpu-target state)
                                                           (:cpu-anim-start state)
                                                           now
                                                           animation-duration
                                                           ease/ease-out-sine))

                               ;; Animate progress bars with different easing functions
                               ;; Progress 1: ease-out-back (slight overshoot)
                               true (assoc :progress-1
                                           (ease/animate (:progress-1-start state)
                                                         (:progress-1-target state)
                                                         (:progress-1-anim-start state)
                                                         now
                                                         animation-duration
                                                         ease/ease-out-back))

                               ;; Progress 2: ease-out-elastic (bouncy)
                               true (assoc :progress-2
                                           (ease/animate (:progress-2-start state)
                                                         (:progress-2-target state)
                                                         (:progress-2-anim-start state)
                                                         now
                                                         (* animation-duration 1.2) ; Slightly longer
                                                         ease/ease-out-elastic-soft))

                               ;; Progress 3: ease-in-out-cubic (smooth)
                               true (assoc :progress-3
                                           (ease/animate (:progress-3-start state)
                                                         (:progress-3-target state)
                                                         (:progress-3-anim-start state)
                                                         now
                                                         animation-duration
                                                         ease/ease-in-out-cubic))))))]
    (when (zero? (mod (:tick new-state) 30))
      (println "Tick:" (:tick new-state) "FPS:" (:fps new-state)))
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
      [:text {:content (str "Eased Charts (tick: " (:tick state) ")")
              :font-size 20}]
      [:rectangle {:color [0 0 0 0] :size [20 1]}]
      [:text {:content (str "FPS: " (:fps state))
              :font-size 16
              :color [100 255 100 255]}]]

     [:text {:content (str "Using duration-based easing (" animation-duration "ms animations)")
             :font-size 12
             :color [150 150 150 255]}]

     ;; Line chart
     [:v-box {:gap 5}
      [:text {:content "Sales Trend (ease-out-cubic)" :font-size 14}]
      (charts/line-chart {:data (mapv int (:sales state))
                          :width chart-w
                          :height chart-h-large
                          :color [100 150 255 255]
                          :thick 3})]

     ;; Bar chart
     [:v-box {:gap 5}
      [:text {:content "Revenue (ease-in-out-quad)" :font-size 14}]
      (charts/bar-chart {:data (mapv int (:revenue state))
                         :width chart-w
                         :height chart-h-med
                         :color [255 150 100 220]
                         :gap 4})]

     ;; Sparklines
     [:v-box {:gap 8}
      [:text {:content "CPU Usage (ease-out-sine)" :font-size 14}]
      [:h-box {:gap 10}
       [:text {:content "CPU:"}]
       (charts/sparkline {:data (mapv int (:cpu state))
                          :width 100
                          :height 25
                          :color [100 200 100 255]
                          :type :line})]]

     ;; Progress bars with different easing functions
     [:v-box {:gap 8}
      [:text {:content "Progress Bars (different easing curves)" :font-size 14}]
      [:h-box {:gap 5}
       [:text {:content "Back:"}]
       (charts/progress-bar {:value (int (:progress-1 state))
                             :width progress-w
                             :height 20
                             :color [100 200 100 255]
                             :bg-color [50 50 50 100]})]
      [:h-box {:gap 5}
       [:text {:content "Elastic:"}]
       (charts/progress-bar {:value (int (:progress-2 state))
                             :width progress-w
                             :height 20
                             :color [255 180 50 255]
                             :bg-color [50 50 50 100]})]
      [:h-box {:gap 5}
       [:text {:content "Cubic:"}]
       (charts/progress-bar {:value (int (:progress-3 state))
                             :width progress-w
                             :height 20
                             :color [100 150 255 255]
                             :bg-color [50 50 50 100]})]]

     ;; Multi-line chart
     [:v-box {:gap 5}
      [:text {:content "Comparison (multi-easing)" :font-size 14}]
      (charts/multi-line-chart
        {:series [{:data (mapv int (:sales state))
                   :color [100 150 255 255]
                   :thick 2}
                  {:data (mapv int (:revenue state))
                   :color [255 150 100 255]
                   :thick 2}]
         :width chart-w
         :height chart-h-large})]]))

(defn -main []
  (core/create-backend!)

  (let [window (core/create-window {:title "Eased Charts Demo"
                                    :on-close (fn [_] (util/exit-clean!))
                                    :size [480 950]})
        root (core/root-element window)]

    (vdom/vdom-mount! root app-state ui-component window)

    ;; ~60 FPS updates for smooth easing
    (letfn [(schedule-update! []
              (core/add-timer! update-interval (fn []
                                                 (update-animations!)
                                                 (schedule-update!))))]
      (schedule-update!))

    (core/open-window! window)
    (core/enter-loop!)))
