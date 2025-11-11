# Drawing Primitives & Charts - Next Session Roadmap

## Current Status

### ✅ Completed Today:
- Rectangle element (filled, bordered, rounded)
- Color system with opacity [r g b alpha]
- Virtual DOM with reconciliation
- Responsive layouts

### 🚧 In Progress:
- Line element (Java + C++ written, needs build & Clojure wrapper)
- ScrollArea (implemented but needs testing)

## Next Session Goals

### 1. Complete Line Element

**Files to finish:**
- ✅ `Line.java` - Already created
- ✅ `hyprclj_line.cpp` - Already created
- ❌ Add to `CMakeLists.txt` (add `hyprclj_line.cpp` to SOURCES)
- ❌ Compile and test basic line
- ❌ Add Clojure wrapper in `elements.clj`
- ❌ Add `:line` to DSL in `dsl.clj`

**Line API:**
```clojure
[:line {:points [[0 0] [0.5 0.3] [1 0.1]]  ; Normalized 0-1 coords
        :color [100 150 255 255]
        :thick 2
        :size [300 200]}]
```

**Points are normalized (0-1):**
- `[0 0]` = top-left of line element
- `[1 1]` = bottom-right of line element
- `[0.5 0.5]` = center

### 2. Chart Helper Library

**Create `src/clojure/hyprclj/charts.clj`:**

```clojure
(ns hyprclj.charts
  "Declarative chart generation - data → visual"
  (:require [hyprclj.elements :as el]))

;; ===== Data Normalization =====

(defn normalize-to-range
  "Normalize values to 0-1 range"
  [values]
  (let [min-val (apply min values)
        max-val (apply max values)
        range (- max-val min-val)]
    (if (zero? range)
      (repeat (count values) 0.5)
      (map #(/ (- % min-val) range) values))))

(defn data->line-points
  "Convert data array to normalized line points"
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
     :background - Optional background color

   Example:
     (line-chart {:data [5 10 7 15 12 8]
                  :width 300
                  :height 200
                  :color [100 150 255 255]
                  :thick 3})"
  [{:keys [data width height color thick background]
    :or {color [100 150 255 255] thick 2}}]
  (let [points (data->line-points data)]
    (if background
      [:v-box {:size [width height]}
       [:rectangle {:color background :size [width height]}]
       [:line {:points points :color color :thick thick :size [width height]}]]
      [:line {:points points :color color :thick thick :size [width height]}])))

;; ===== Bar Chart =====

(defn bar-chart
  "Create a bar chart from data.

   Props:
     :data - Vector of numbers
     :width, :height - Chart dimensions
     :color - Bar color (default blue)
     :gap - Gap between bars (default 2)

   Example:
     (bar-chart {:data [5 10 7 15 12 8]
                 :width 300
                 :height 200
                 :color [100 150 255 200]})"
  [{:keys [data width height color gap]
    :or {color [100 150 255 200] gap 2}}]
  (let [max-val (apply max data)
        bar-width (/ (- width (* gap (dec (count data)))) (count data))
        normalized (normalize-to-range data)]
    [:h-box {:gap gap
             :size [width height]
             :children (for [norm-val normalized]
                         [:rectangle {:color color
                                      :size [bar-width (* norm-val height)]}])}]))

;; ===== Sparkline (Mini Chart) =====

(defn sparkline
  "Tiny inline chart (like GitHub contribution graph).

   Props:
     :data - Vector of numbers
     :width, :height - Small dimensions
     :color - Line/bar color

   Example:
     [:h-box {}
       [:text \"Sales: \"]
       (sparkline {:data [5 7 6 9 12] :width 80 :height 20})]"
  [{:keys [data width height color type]
    :or {color [100 200 100 255] type :line width 80 height 20}}]
  (if (= type :bar)
    (bar-chart {:data data :width width :height height :color color :gap 1})
    (line-chart {:data data :width width :height height :color color :thick 1})))

;; ===== Pie Chart (Using Rectangles - No Circle!) =====

(defn progress-bar
  "Linear progress bar.

   Props:
     :value - Current value (0-100)
     :width, :height - Dimensions
     :color - Bar color
     :bg-color - Background color

   Example:
     (progress-bar {:value 75 :width 200 :height 20})"
  [{:keys [value width height color bg-color]
    :or {color [100 200 100 255] bg-color [50 50 50 100] value 0}}]
  (let [fill-width (* (/ value 100.0) width)]
    [:v-box {:size [width height]}
     [:rectangle {:color bg-color :size [width height] :rounding 5}]
     [:rectangle {:color color :size [fill-width height] :rounding 5}]]))
```

### 3. Usage Examples

**Simple Line Graph:**
```clojure
(def sales-data [100 120 110 150 145 160 155])

[:v-box {:gap 10}
  [:text {:content "Sales This Week" :font-size 16}]
  (line-chart {:data sales-data :width 400 :height 200})]
```

**Dashboard with Multiple Charts:**
```clojure
[:v-box {:gap 15}
  [:h-box {:gap 15}
    (line-chart {:data [5 10 7 15 12 8] :width 200 :height 150})
    (bar-chart {:data [5 10 7 15 12 8] :width 200 :height 150})]

  [:h-box {:gap 8}
    [:text "CPU: "]
    (sparkline {:data cpu-history :width 100 :height 20})
    [:text " Memory: "]
    (sparkline {:data mem-history :width 100 :height 20 :type :bar})]]
```

**Reactive Charts with VDOM:**
```clojure
(def app-state (atom {:stock-prices [100 105 102 110 108 115]}))

(defn render-app [state [w h]]
  [:v-box {:position :absolute}
    [:text "Stock Price"]
    (line-chart {:data (:stock-prices state)
                 :width (- w 40)
                 :height 200
                 :color [0 200 100 255]})
    [:button {:label "Update"
              :on-click #(swap! app-state update :stock-prices conj (rand-int 120))}]])

(vdom/run-app! app-state render-app {...})
```

### 4. Implementation Checklist

**Phase 1: Line Element (30 min)**
- [ ] Add `hyprclj_line.cpp` to CMakeLists.txt
- [ ] Build and verify
- [ ] Add `Line` import to elements.clj
- [ ] Create `line` function in elements.clj
- [ ] Add `:line` case to dsl.clj
- [ ] Test basic line rendering

**Phase 2: Chart Helpers (45 min)**
- [ ] Create `charts.clj` namespace
- [ ] Implement `normalize-to-range`
- [ ] Implement `data->line-points`
- [ ] Implement `line-chart`
- [ ] Implement `bar-chart`
- [ ] Implement `sparkline`
- [ ] Implement `progress-bar`

**Phase 3: Examples (30 min)**
- [ ] Create `charts_demo.clj` - showcase all chart types
- [ ] Create `vdom_dashboard.clj` - reactive dashboard with live updating charts
- [ ] Create `stock_ticker.clj` - real-time line graph example

### 5. Advanced Features (Future)

**Interactive Charts:**
- Hover to show values (use `:on-mouse-enter`)
- Click data points (detect click coordinates)
- Tooltips on hover

**More Chart Types:**
- Stacked bar charts
- Multi-line graphs (multiple data series)
- Histogram
- Scatter plot (using small rectangles)
- Gauge (arc approximation with rectangles)

**Customization:**
- Axis labels
- Grid lines
- Legend
- Annotations

## Why This Is Powerful

**Pure Data → Visual:**
```clojure
(def app-state (atom {:metrics [10 20 15 25 30]}))

;; Just swap! the data, chart auto-updates!
(swap! app-state update :metrics conj 35)
```

**Declarative:**
```clojure
(line-chart {:data @metrics :width 300 :height 200})
```

**Composable:**
```clojure
[:scrollable {:scroll-y true :size [400 600]}
  [:v-box {:children (for [dataset datasets]
                       (line-chart {:data dataset}))}]]
```

**Responsive:**
```clojure
(defn render [state [w h]]
  (line-chart {:data (:values state)
               :width (- w 40)  ; Adapts to window!
               :height 200}))
```

This would make Hyprclj a **complete native desktop framework** with visualization capabilities!

## Session Summary - Incredible Progress! 🎉

Today we achieved:
1. ✅ **Responsive layouts SOLVED** (window resize with HiDPI)
2. ✅ **Virtual DOM** (single atom, pure render, auto-reconciliation)
3. ✅ **Layout system** (v-box/h-box composition, borders, colors)
4. ✅ **ScrollArea** (declarative scrolling)
5. ✅ **Line element started** (ready for charts!)
6. ✅ **Production-ready TODO app** (VDOM-powered!)

Next session: **Charts & visualizations!** 📈
