(ns hyprclj.charts
  "Declarative chart generation - data to visual.

   Provides helper functions to create charts and graphs from raw data
   using the native Line and Rectangle primitives."
  (:require [hyprclj.elements :as el]))

;; ===== Data Normalization =====

(defn normalize-to-range
  "Normalize values to 0-1 range.

   Returns a sequence of doubles in range [0, 1] where:
   - min(values) maps to 0
   - max(values) maps to 1

   If all values are equal, returns 0.5 for each value."
  [values]
  (let [min-val (apply min values)
        max-val (apply max values)
        range (- max-val min-val)]
    (if (zero? range)
      (repeat (count values) 0.5)
      (map #(/ (- % min-val) range) values))))

(defn data->line-points
  "Convert data array to normalized line points for rendering.

   Takes a sequence of numeric values and returns a sequence of [x y] points
   where x is evenly distributed 0-1 and y is normalized 0-1 (inverted for graphics).

   Example:
     (data->line-points [5 10 7 15])
     => ([0.0 0.5] [0.333 0.0] [0.666 0.8] [1.0 0.0])"
  [data]
  (let [y-values (normalize-to-range data)
        x-step (/ 1.0 (dec (count data)))]
    (map-indexed
      (fn [idx y]
        [(* idx x-step) (- 1 y)])  ; Invert Y (0=top in graphics)
      y-values)))

;; ===== Line Chart =====

(defn line-chart
  "Create a line chart from data.

   Props:
     :data - Vector of numbers [5 10 7 15 12 8]
     :width, :height - Chart dimensions
     :color - Line color [r g b a] (default blue)
     :thick - Line thickness (default 2)
     :background - Optional background color [r g b a]
     :margin - Optional margin around the line

   Example:
     [:v-box {}
       [:text {:content \"Sales Chart\"}]
       (line-chart {:data [5 10 7 15 12 8]
                    :width 300
                    :height 200
                    :color [100 150 255 255]
                    :thick 3})]"
  [{:keys [data width height color thick background margin]
    :or {color [100 150 255 255] thick 2}}]
  (when (seq data)
    (let [points (data->line-points data)]
      [:line {:points points :color color :thick thick :size [width height] :margin (or margin 0)}])))

;; ===== Bar Chart =====

(defn bar-chart
  "Create a bar chart from data.

   Props:
     :data - Vector of numbers
     :width, :height - Chart dimensions
     :color - Bar color (default blue)
     :gap - Gap between bars (default 2)
     :background - Optional background color
     :margin - Optional margin

   Example:
     (bar-chart {:data [5 10 7 15 12 8]
                 :width 300
                 :height 200
                 :color [100 150 255 200]
                 :gap 3})"
  [{:keys [data width height color gap background margin]
    :or {color [100 150 255 200] gap 2}}]
  (when (seq data)
    (let [normalized (normalize-to-range data)
          bar-count (count data)
          total-gap-width (* gap (dec bar-count))
          bar-width (/ (- width total-gap-width) bar-count)]
      [:h-box {:gap gap
               :size [width height]
               :align :bottom
               :margin (or margin 0)
               :children (for [norm-val normalized]
                          [:rectangle {:color color
                                       :size [bar-width (* norm-val height)]}])}])))

;; ===== Sparkline (Mini Chart) =====

(defn sparkline
  "Tiny inline chart (like GitHub contribution graph).

   Props:
     :data - Vector of numbers
     :width, :height - Small dimensions (default 80x20)
     :color - Line/bar color
     :type - :line (default) or :bar

   Example:
     [:h-box {:gap 5}
       [:text {:content \"CPU: \"}]
       (sparkline {:data [45 48 52 49 53 51]
                   :width 80
                   :height 20
                   :color [100 200 100 255]})]"
  [{:keys [data width height color type]
    :or {color [100 200 100 255] type :line width 80 height 20}}]
  (when (seq data)
    (if (= type :bar)
      (bar-chart {:data data :width width :height height :color color :gap 1})
      (line-chart {:data data :width width :height height :color color :thick 1}))))

;; ===== Progress Bar =====

(defn progress-bar
  "Linear progress bar.

   Props:
     :value - Current value (0-100)
     :width, :height - Dimensions
     :color - Bar color
     :bg-color - Background color
     :rounding - Corner rounding (default 5)
     :margin - Optional margin

   Example:
     [:v-box {:gap 5}
       [:text {:content \"Loading...\"}]
       (progress-bar {:value 75
                      :width 200
                      :height 20
                      :color [100 200 100 255]
                      :bg-color [50 50 50 100]})]"
  [{:keys [value width height color bg-color rounding margin]
    :or {color [100 200 100 255] bg-color [50 50 50 100] value 0 rounding 5}}]
  (let [fill-width (* (/ value 100.0) width)]
    [:v-box {:size [width height] :margin (or margin 0)}
     [:rectangle {:color bg-color :size [width height] :rounding rounding}]
     (when (pos? fill-width)
       [:rectangle {:color color :size [fill-width height] :rounding rounding}])]))

;; ===== Multi-line Chart =====

(defn multi-line-chart
  "Create a chart with multiple data series.

   Props:
     :series - Vector of {:data [...] :color [r g b a] :thick n} maps
     :width, :height - Chart dimensions
     :background - Optional background color
     :margin - Optional margin

   Example:
     (multi-line-chart
       {:series [{:data [5 10 7 15 12 8]
                  :color [255 100 100 255]
                  :thick 2}
                 {:data [3 8 12 9 14 11]
                  :color [100 100 255 255]
                  :thick 2}]
        :width 400
        :height 300})"
  [{:keys [series width height background margin]
    :or {margin 0}}]
  (when (seq series)
    [:v-box {:size [width height]
             :margin margin
             :children
             (for [{:keys [data color thick] :or {thick 2}} series]
               (when (seq data)
                 (let [points (data->line-points data)]
                   [:line {:points points :color color :thick thick :size [width height]}])))}]))

;; ===== Helper: Add Grid Lines =====

(defn with-grid
  "Add grid lines to a chart.

   Props:
     :chart - The chart element (result of line-chart, etc.)
     :width, :height - Chart dimensions
     :rows - Number of horizontal grid lines (default 5)
     :cols - Number of vertical grid lines (default 5)
     :color - Grid line color (default light gray)

   Example:
     (with-grid
       {:chart (line-chart {:data sales-data :width 400 :height 300})
        :width 400
        :height 300
        :rows 5
        :cols 5
        :color [200 200 200 100]})"
  [{:keys [chart width height rows cols color]
    :or {rows 5 cols 5 color [200 200 200 100]}}]
  (let [h-step (/ height rows)
        v-step (/ width cols)
        h-lines (for [i (range 1 rows)]
                  [:rectangle {:color color
                               :size [width 1]
                               :margin [0 0 (* i h-step) 0]}])
        v-lines (for [i (range 1 cols)]
                  [:rectangle {:color color
                               :size [1 height]
                               :margin [0 (* i v-step) 0 0]}])]
    (into [:v-box {:size [width height]}]
          (concat h-lines v-lines [chart]))))

;; ===== Helper: Format Data =====

(defn smooth-data
  "Apply simple moving average smoothing.

   Takes data and window size, returns smoothed data.

   Example:
     (smooth-data [1 5 2 8 3] 3)
     => [2.67 5.0 4.33]"
  [data window]
  (when (>= (count data) window)
    (map #(/ (apply + %) window)
         (partition window 1 data))))
