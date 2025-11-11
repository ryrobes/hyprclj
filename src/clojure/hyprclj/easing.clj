(ns hyprclj.easing
  "Easing functions for smooth animations.

   All easing functions take a progress value t in range [0, 1]
   and return an eased value in range [0, 1].

   Naming convention:
   - ease-in: slow start, fast end
   - ease-out: fast start, slow end
   - ease-in-out: slow start and end, fast middle")

;; ===== Linear =====

(defn linear
  "No easing, linear interpolation."
  [t]
  t)

;; ===== Quadratic =====

(defn ease-in-quad
  "Quadratic easing in - accelerating from zero velocity."
  [t]
  (* t t))

(defn ease-out-quad
  "Quadratic easing out - decelerating to zero velocity."
  [t]
  (- (* t (- t 2.0))))

(defn ease-in-out-quad
  "Quadratic easing in/out - acceleration until halfway, then deceleration."
  [t]
  (if (< t 0.5)
    (* 2.0 t t)
    (+ (* -2.0 t t) (* 4.0 t) -1.0)))

;; ===== Cubic =====

(defn ease-in-cubic
  "Cubic easing in - accelerating from zero velocity."
  [t]
  (* t t t))

(defn ease-out-cubic
  "Cubic easing out - decelerating to zero velocity."
  [t]
  (let [t1 (dec t)]
    (inc (* t1 t1 t1))))

(defn ease-in-out-cubic
  "Cubic easing in/out."
  [t]
  (if (< t 0.5)
    (* 4.0 t t t)
    (let [t2 (- (* 2.0 t) 2.0)]
      (+ 1.0 (* 0.5 t2 t2 t2)))))

;; ===== Quartic =====

(defn ease-in-quart
  "Quartic easing in."
  [t]
  (* t t t t))

(defn ease-out-quart
  "Quartic easing out."
  [t]
  (let [t1 (dec t)]
    (- 1.0 (* t1 t1 t1 t1))))

(defn ease-in-out-quart
  "Quartic easing in/out."
  [t]
  (if (< t 0.5)
    (* 8.0 t t t t)
    (let [t2 (- (* 2.0 t) 2.0)]
      (- 1.0 (* 0.5 t2 t2 t2 t2)))))

;; ===== Sine =====

(defn ease-in-sine
  "Sinusoidal easing in."
  [t]
  (- 1.0 (Math/cos (* t Math/PI 0.5))))

(defn ease-out-sine
  "Sinusoidal easing out."
  [t]
  (Math/sin (* t Math/PI 0.5)))

(defn ease-in-out-sine
  "Sinusoidal easing in/out."
  [t]
  (* -0.5 (dec (Math/cos (* Math/PI t)))))

;; ===== Exponential =====

(defn ease-in-expo
  "Exponential easing in."
  [t]
  (if (zero? t)
    0.0
    (Math/pow 2.0 (* 10.0 (- t 1.0)))))

(defn ease-out-expo
  "Exponential easing out."
  [t]
  (if (= t 1.0)
    1.0
    (- 1.0 (Math/pow 2.0 (* -10.0 t)))))

(defn ease-in-out-expo
  "Exponential easing in/out."
  [t]
  (cond
    (zero? t) 0.0
    (= t 1.0) 1.0
    (< t 0.5) (* 0.5 (Math/pow 2.0 (* 20.0 t -10.0)))
    :else (+ 1.0 (* -0.5 (Math/pow 2.0 (+ (* -20.0 t) 10.0))))))

;; ===== Circular =====

(defn ease-in-circ
  "Circular easing in."
  [t]
  (- 1.0 (Math/sqrt (- 1.0 (* t t)))))

(defn ease-out-circ
  "Circular easing out."
  [t]
  (let [t1 (dec t)]
    (Math/sqrt (- 1.0 (* t1 t1)))))

(defn ease-in-out-circ
  "Circular easing in/out."
  [t]
  (if (< t 0.5)
    (* -0.5 (dec (Math/sqrt (- 1.0 (* 4.0 t t)))))
    (let [t2 (- (* 2.0 t) 2.0)]
      (* 0.5 (inc (Math/sqrt (- 1.0 (* t2 t2))))))))

;; ===== Back (overshoot) =====

(defn ease-in-back
  "Back easing in - slight back movement before forward."
  [t]
  (let [c1 1.70158
        c3 (inc c1)]
    (* c3 t t t (- (* c1 t t)))))

(defn ease-out-back
  "Back easing out - overshoot then settle."
  [t]
  (let [c1 1.70158
        c3 (inc c1)
        t1 (dec t)]
    (inc (* c3 t1 t1 t1 (* c1 t1 t1)))))

(defn ease-in-out-back
  "Back easing in/out."
  [t]
  (let [c1 1.70158
        c2 (* c1 1.525)]
    (if (< t 0.5)
      (let [t2 (* 2.0 t)]
        (* 0.5 (* t2 t2 (- (* (inc c2) t2) c2))))
      (let [t2 (- (* 2.0 t) 2.0)]
        (* 0.5 (+ 2.0 (* t2 t2 (+ (* (inc c2) t2) c2))))))))

;; ===== Elastic =====

(defn ease-out-elastic
  "Elastic easing out - bouncy overshoot effect."
  [t]
  (let [c4 (/ (* 2.0 Math/PI) 3.0)]
    (cond
      (zero? t) 0.0
      (= t 1.0) 1.0
      :else (+ 1.0 (* (Math/pow 2.0 (* -10.0 t))
                      (Math/sin (/ (* (- (* t 10.0) 0.75) 2.0 Math/PI) 3.0)))))))

(defn ease-out-elastic-soft
  "Softer elastic easing out."
  [t]
  (let [c4 (/ (* 2.0 Math/PI) 4.0)]
    (cond
      (zero? t) 0.0
      (= t 1.0) 1.0
      :else (+ 1.0 (* (Math/pow 2.0 (* -8.0 t))
                      (Math/sin (/ (* (- (* t 10.0) 0.75) 2.0 Math/PI) 4.0)))))))

;; ===== Interpolation Helper =====

(defn interpolate
  "Interpolate between start and end values using an easing function.

   Args:
     start - Starting value
     end - Target value
     progress - Progress in range [0, 1]
     easing-fn - Easing function (default: ease-out-cubic)

   Returns:
     Interpolated value between start and end"
  ([start end progress]
   (interpolate start end progress ease-out-cubic))
  ([start end progress easing-fn]
   (let [t (max 0.0 (min 1.0 progress))  ; Clamp to [0, 1]
         eased (easing-fn t)]
     (+ start (* (- end start) eased)))))

;; ===== Duration-based Animation Helper =====

(defn animate
  "Calculate animated value based on elapsed time.

   Args:
     start-value - Starting value
     target-value - Target value
     start-time - Animation start time (millis)
     current-time - Current time (millis)
     duration-ms - Animation duration in milliseconds
     easing-fn - Easing function (default: ease-out-cubic)

   Returns:
     Interpolated value, or target-value if animation is complete"
  ([start-value target-value start-time current-time duration-ms]
   (animate start-value target-value start-time current-time duration-ms ease-out-cubic))
  ([start-value target-value start-time current-time duration-ms easing-fn]
   (let [elapsed (- current-time start-time)
         progress (min 1.0 (/ elapsed (double duration-ms)))]
     (if (>= progress 1.0)
       target-value
       (interpolate start-value target-value progress easing-fn)))))
