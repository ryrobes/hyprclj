(ns charts-smooth-demo
  "Smooth animated charts using momentum/easing for buttery animations."
  (:require [hyprclj.core :as core]
            [hyprclj.dsl :refer [mount!]]
            [hyprclj.util :as util]
            [hyprclj.charts :as charts]
            [hyprclj.vdom :as vdom])
  (:import [java.util Random]))

(def random (Random.))

;; App state with live data
(def app-state
  (atom {:sales (vec (repeatedly 10 #(+ 100.0 (.nextInt random 100))))
         :revenue (vec (repeatedly 10 #(+ 50.0 (.nextInt random 80))))
         :cpu (vec (repeatedly 10 #(+ 40.0 (.nextInt random 20))))
         :memory (vec (repeatedly 10 #(+ 55.0 (.nextInt random 25))))
         :progress-1 (double (.nextInt random 100))
         :progress-2 (double (.nextInt random 100))
         :progress-3 (double (.nextInt random 100))
         ;; Target values for smooth transitions
         :progress-1-target (double (.nextInt random 100))
         :progress-2-target (double (.nextInt random 100))
         :progress-3-target (double (.nextInt random 100))
         :tick 0
         :fps 0
         :last-update-time (System/currentTimeMillis)}))

(defn add-smooth-value
  "Add smoothly changing value using exponential moving average.
   Higher momentum (0.7-0.9) = smoother but slower transitions."
  [data-vec base variance momentum]
  (let [last-val (or (last data-vec) (double base))
        target (+ base (.nextInt random variance))
        ;; Exponential smoothing: new = old * momentum + target * (1 - momentum)
        new-val (+ (* last-val momentum) (* target (- 0.5 momentum)))]
    (vec (take-last 150 (conj data-vec new-val)))))

(defn smooth-toward
  "Smoothly interpolate current value toward target."
  [current target momentum]
  (+ (* current momentum) (* target (- 1.0 momentum))))

(defn update-data!
  "Update with smooth transitions using momentum/easing."
  []
  (let [now (System/currentTimeMillis)
        new-state (swap! app-state
                         (fn [state]
                           (let [time-delta (- now (:last-update-time state))
                                 fps (if (pos? time-delta)
                                       (int (/ 1000.0 time-delta))
                                       0)
                                 ;; Occasionally set new random targets for progress bars
                                 new-targets? (zero? (mod (:tick state) 30))]
                             (-> state
                                 ;; Smooth data series with high momentum (0.75) for very smooth transitions
                                 (update :sales add-smooth-value 100 100 0.75)
                                 (update :revenue add-smooth-value 50 80 0.75)
                                 (update :cpu add-smooth-value 40 20 0.8)
                                 (update :memory add-smooth-value 55 25 0.8)
                                 ;; Update progress bar targets occasionally
                                 (update :progress-1-target (fn [t] (if new-targets? (double (.nextInt random 100)) t)))
                                 (update :progress-2-target (fn [t] (if new-targets? (double (.nextInt random 100)) t)))
                                 (update :progress-3-target (fn [t] (if new-targets? (double (.nextInt random 100)) t)))
                                 ;; Smooth progress bars toward their targets
                                 (update :progress-1 smooth-toward (:progress-1-target state) 0.85)
                                 (update :progress-2 smooth-toward (:progress-2-target state) 0.85)
                                 (update :progress-3 smooth-toward (:progress-3-target state) 0.85)
                                 (update :tick inc)
                                 (assoc :fps fps)
                                 (assoc :last-update-time now)))))]
    (when (zero? (mod (:tick new-state) 20))
      (println "Tick:" (:tick new-state) "FPS:" (:fps new-state)))
    new-state))

(defn ui-component [state [w h]]
  (let [content-w (- w 40)
        chart-w (max 200 (- content-w 20))
        chart-h-small (max 100 (int (* h 0.12)))
        chart-h-med (max 120 (int (* h 0.15)))
        chart-h-large (max 150 (int (* h 0.18)))
        progress-w (max 750 (int (* content-w 0.45)))]

    [:v-box {:position :absolute :gap 15 :margin 20}
     ;; Header with FPS counterFMEM
     [:h-box {:gap 10}
      ;; [:text {:content (str "Smooth Charts (tick: " (:tick state) ")")
      ;;         :font-size 20}]
      [:rectangle {:color [0 0 0 0] :size [20 1]}]
      ;; [:text {:content (str "FPS: " (:fps state))
      ;;         :font-size 16
      ;;         :color [100 255 100 255]}]
      ]

     [:text {:content "HYPRCLJ"
             :font-size 75
             :font-family "Outrun Future"
             :color "#FF1493"}]
     
          [:text {:content "...like a butter?"
             :font-size 35
             :font-family "Outrun Future"
                  :opacity 0.4
             :color "#39FF14"}]

     ;; Line chart
     [:v-box {:gap 5}
      ;[:text {:content "Sales Trend (momentum: 0.75)" :font-size 14}]
      (charts/line-chart {:data (mapv int (:sales state))
                          :width chart-w
                          :margin 20
                          :height chart-h-large
                          :color "#FF00FF" ;[100 150 255 200]
                          :thick 2})]

     ;; Bar chart
     [:v-box {:gap 5}
      ;;[:text {:content "Revenue (momentum: 0.75)" :font-size 14}]
      (charts/bar-chart {:data (vec (take-last 25 (mapv int (:revenue state))))
                         :width chart-w
                         :height chart-h-med
                         :color "#00FFFF" ;[233 150 99 200]
                         :gap 1})
      
      (charts/bar-chart {:data (vec (take-last 45  (mapv int (:revenue state))))
                         :width chart-w
                         :height chart-h-med
                         :color "#7D00FF"
                         :gap 4})

      ]

     ;; Sparklines
     [:v-box {:gap 8}
      ;[:text {:content "System Metrics (momentum: 0.8)" :font-size 14}]
      [:h-box {:gap 10}
       [:text {:content "CPU  " :font-family "Outrun Future"  :font-size 20 :color "#FF4500"}]
       (charts/sparkline {:data (mapv int (:cpu state))
                          :width 600
                          :height 35
                          :thick 2
                          :color "#FF4500"
                          :type :line})]
      ;; [:v-box {:gap 10}
      ;;  [:text {:content "MEM:"}]
      ;;  (charts/sparkline {:data (mapv int (:memory state))
      ;;                     :width 100
      ;;                     :height 25
      ;;                     :color [200 100 100 255]
      ;;                     :type :bar})]
      ]

     ;; Progress bars with smooth easing
     [:v-box {:gap 8}
      ;[:text {:content "Progress (eased, targets change every 30 ticks)" :font-size 14}]
      [:h-box {:gap 5}
       [:text {:content "Task 1    " :font-family "Outrun Future"  :font-size 23 :color "#4169E1"}]
       (charts/progress-bar {:value (int (:progress-1 state))
                             :width progress-w
                             :height 45
                             :color "#4169E1"
                             :bg-color [50 50 50 100]})]
      [:h-box {:gap 5 :align "center"}
       [:text {:content "Task 2    " :font-family "Outrun Future"  :font-size 23 :color "#FF00AA"}]
       (charts/progress-bar {:value (int (:progress-2 state))
                             :width progress-w
                             :height 40
                             :color "#FF00AA"
                             :bg-color [50 50 50 100]})]
      [:h-box {:gap 5}
       [:text {:content "Task 3    " :font-family "Outrun Future" :font-size 23 :color "#1A1A2E"}]
       (charts/progress-bar {:value (int (:progress-3 state))
                             :width progress-w
                             :height 40
                             :color "#1A1A2E"
                             :bg-color [50 50 50 100]})]]

     ;; Multi-line chart
     [:v-box {:gap 5}
      ;;[:text {:content "Comparison (smooth multi-line)" :font-size 14}]
      (charts/multi-line-chart
        {:series [{:data (mapv int (:sales state))
                   :color [255, 215, 0, 255]
                   :thick 2}
                  {:data (mapv int (:revenue state))
                   :color [255 150 100 255]
                   :thick 2}]
         :width chart-w
         :height chart-h-large})]]))

(defn -main []
  (core/create-backend!)

  (let [window (core/create-window {:title "Smooth Charts Demo"
                                    :on-close (fn [_] (util/exit-clean!))
                                    :size [480 950]})
        root (core/root-element window)]

    (vdom/vdom-mount! root app-state ui-component window)

    ;; 60 FPS target (16.67ms) for ultra-smooth animations
    (letfn [(schedule-update! []
              (core/add-timer! 60 (fn []
                                    (update-data!)
                                    (schedule-update!))))]
      (schedule-update!))

    (core/open-window! window)
    (core/enter-loop!)))
