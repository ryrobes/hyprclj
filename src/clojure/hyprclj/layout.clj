(ns hyprclj.layout
  "Re-com style layout components and utilities."
  (:require [hyprclj.elements :as el]))

;; Re-com style layout components

(defn v-box
  "Vertical box layout (column).

   Re-com style props:
     :gap         - Space between children (px)
     :margin      - Outer margin (px or [v h] or [t r b l])
     :padding     - Same as margin
     :size        - [width height] or :auto
     :width       - Width (overrides :size)
     :height      - Height (overrides :size)
     :min-width   - Minimum width
     :min-height  - Minimum height
     :align       - Cross-axis alignment: :start :center :end
     :justify     - Main-axis distribution: :start :center :end :between :around
     :children    - Child elements (or pass as varargs)

   Example:
     (v-box {:gap 10 :margin 20 :align :center}
       child1 child2 child3)

     (v-box {:gap 5 :padding [10 20]} [child1 child2])"
  [{:keys [gap margin padding size width height min-width min-height
           align justify children grow]
    :or {gap 0}
    :as opts}
   & child-elements]

  (let [actual-children (or children child-elements)
        w (or width (when (vector? size) (first size)))
        h (or height (when (vector? size) (second size)))
        final-size (when (or w h) [(or w -1) (or h -1)])
        m (or padding margin)]

    (el/column-layout
      (cond-> {:gap gap}
        final-size (assoc :size final-size)
        m (assoc :margin m)
        grow (assoc :grow grow)
        align (assoc :align align)))))

(defn h-box
  "Horizontal box layout (row).

   Props same as v-box but horizontal.

   Example:
     (h-box {:gap 10 :align :center}
       button1 button2 button3)"
  [{:keys [gap margin padding size width height align justify children grow]
    :or {gap 0}
    :as opts}
   & child-elements]

  (let [actual-children (or children child-elements)
        w (or width (when (vector? size) (first size)))
        h (or height (when (vector? size) (second size)))
        final-size (when (or w h) [(or w -1) (or h -1)])
        m (or padding margin)]

    (el/row-layout
      (cond-> {:gap gap}
        final-size (assoc :size final-size)
        m (assoc :margin m)
        grow (assoc :grow grow)
        align (assoc :align align)))))

(defn box
  "Generic container box.

   Direction determined by :direction :horizontal or :vertical (default)"
  [{:keys [direction] :or {direction :vertical} :as opts} & children]
  (if (= direction :horizontal)
    (apply h-box opts children)
    (apply v-box opts children)))

(defn gap
  "Fixed-size spacer.

   Example:
     (gap :size 20)  ; 20px spacer
     (gap :width 50 :height 10)"
  [{:keys [size width height] :or {size 10}}]
  (let [w (or width size)
        h (or height size)]
    (el/column-layout {:size [w h]})))

(defn spacer
  "Flexible spacer that grows to fill available space.

   Example:
     [:column {}
       [:text \"Top\"]
       (spacer)  ; Pushes content to edges
       [:text \"Bottom\"]]"
  ([] (spacer {}))
  ([{:keys [size] :or {size 1}}]
   (doto (el/column-layout {:size [size size]})
     (el/set-grow! true true))))  ; Grow both directions

(defn line
  "Horizontal or vertical line (separator).

   Example:
     (line :horizontal)
     (line :vertical :size 2 :width 100)"
  [direction & {:keys [size width height color]
                :or {size 1}}]
  (let [[w h] (case direction
                :horizontal [(or width -1) size]
                :vertical [size (or height -1)])]
    ;; For POC, use a column as a line
    ;; Could use Rectangle element for colored lines
    (el/column-layout {:size [w h]})))

;; Helper functions for common layouts

(defn centered
  "Center content both horizontally and vertically.

   Example:
     (centered [:text \"I'm centered!\"])"
  [child]
  (doto (el/column-layout {:grow true})
    (el/set-align! "center")))

(defn title-bar
  "Common title bar layout.

   Example:
     (title-bar {:left title-text
                 :right close-button})"
  [{:keys [left center right]}]
  (el/row-layout
    {:gap 10 :margin 10}))

(defn content-with-sidebar
  "Two-column layout with sidebar.

   Example:
     (content-with-sidebar
       {:sidebar sidebar-content
        :main main-content
        :sidebar-width 200})"
  [{:keys [sidebar main sidebar-width sidebar-position]
    :or {sidebar-width 200 sidebar-position :left}}]
  (let [row (el/row-layout {:gap 0})]
    (when (= sidebar-position :left)
      (el/add-child! row (el/column-layout {:size [sidebar-width -1]})))
    (el/add-child! row (doto (el/column-layout {}) (el/set-grow! true)))
    (when (= sidebar-position :right)
      (el/add-child! row (el/column-layout {:size [sidebar-width -1]})))
    row))

;; Export as layout namespace conveniences
(def v-split content-with-sidebar)  ; Alias

(comment
  ;; Example usage - v-box and h-box for layouts
  )
