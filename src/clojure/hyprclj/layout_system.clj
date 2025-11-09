(ns hyprclj.layout-system
  "Re-com style layout system with alignment and justification."
  (:require [hyprclj.elements :as elem]))

;; Helper to apply post-compile properties
(defn apply-element-props!
  "Apply properties to a compiled element.

   Supported props:
     :position - :absolute to pin to (0, 0)
     :align - Alignment flags (:left, :right, :center, :top, :bottom, etc.)"
  [element props]
  (when element
    ;; Apply positioning
    (when (= (:position props) :absolute)
      (elem/set-position-mode! element 0)
      (elem/set-absolute-position! element 0 0))

    ;; Apply alignment
    (when-let [align (:align props)]
      (elem/set-align! element align))

    element))

;; v-box: Creates column layout, returns it for children to be added via compile-children
(defn v-box
  "Vertical layout box (column).

   Use in DSL as:
     [:v-box {:gap 10 :align :center :position :absolute :padding 10}
       [:text \"Child 1\"]
       [:text \"Child 2\"]]

   Props:
     :gap - Spacing between children
     :align - Child alignment: :left (default), :center, :right
     :position - :absolute to pin to top-left
     :padding - Internal padding (adds margin to all children)
     :background - Background color [r g b a]
     :border - Border thickness with :border-color
     :size, :margin, :grow - Standard element props

   Note: :background/:border create a rectangle as the first child"
  [props]
  (let [column (elem/column-layout (select-keys props [:gap :size :margin :grow]))]
    ;; Apply positioning and alignment to the column itself
    (apply-element-props! column props)

    ;; Add background/border rectangle as first child if specified
    (when (or (:background props) (:border props))
      (let [bg-rect (elem/rectangle
                      {:color (or (:background props) [0 0 0 0])
                       :border-color (:border-color props)
                       :border (or (:border props) 0)
                       :rounding (or (:rounding props) 0)
                       :size (:size props)})]  ; NO :grow - let content have space!
        (elem/add-child! column bg-rect)))

    column))

(defn h-box
  "Horizontal layout box (row).

   Use in DSL as:
     [:h-box {:gap 5 :align :center :background [50 50 50 255] :border 2}
       [:text \"Child 1\"]
       [:text \"Child 2\"]]

   Props:
     :gap - Spacing between children
     :align - Child alignment: :top (default), :center, :bottom
     :position - :absolute to pin to top-left
     :padding - Internal padding (adds margin to all children)
     :background - Background color [r g b a]
     :border - Border thickness with :border-color
     :size, :margin, :grow - Standard element props

   Note: :background/:border create a rectangle as the first child"
  [props]
  (let [row (elem/row-layout (select-keys props [:gap :size :margin :grow]))]
    ;; Apply positioning and alignment to the row itself
    (apply-element-props! row props)

    ;; Add background/border rectangle as first child if specified
    (when (or (:background props) (:border props))
      (let [bg-rect (elem/rectangle
                      {:color (or (:background props) [0 0 0 0])
                       :border-color (:border-color props)
                       :border (or (:border props) 0)
                       :rounding (or (:rounding props) 0)
                       :size (:size props)})]  ; NO :grow
        (elem/add-child! row bg-rect)))

    row))

(defn box
  "Generic box - chooses v-box or h-box based on :direction.

   Props:
     :direction - :vertical (default) or :horizontal
     (all other props from v-box/h-box)"
  [{:keys [direction] :or {direction :vertical} :as props}]
  (if (= direction :horizontal)
    (h-box props)
    (v-box props)))

;; colored-button: Button with colored background via layering trick
(defn colored-button
  "Create a button with a colored background.

   Props:
     :label, :size, :on-click - Standard button props
     :bg-color - Background color [r g b a]
     :border-color - Optional border color
     :border - Border thickness (default 0)
     :rounding - Corner rounding (default 5)

   Example:
     (colored-button {:label \"Click\" :size [100 30]
                      :bg-color [100 150 255 200]
                      :on-click #(println \"Hi\")})"
  [{:keys [bg-color border-color border rounding] :as props}]
  (if bg-color
    ;; Create container with background rectangle + button
    (let [container (elem/column-layout {:size (:size props)})
          [w h] (:size props)]
      ;; Layer 1: Background rectangle (absolute 0,0)
      (when bg-color
        (let [bg (elem/rectangle {:color bg-color
                                  :border-color border-color
                                  :border (or border 0)
                                  :rounding (or rounding 5)
                                  :size [w h]})]
          (elem/set-position-mode! bg 0)
          (elem/set-absolute-position! bg 0 0)
          (elem/add-child! container bg)))

      ;; Layer 2: Button on top (absolute 0,0, no-bg)
      (let [btn (elem/button (assoc props
                                    :no-bg true   ; Transparent background
                                    :no-border true))]  ; No border, use rectangle's
        (elem/set-position-mode! btn 0)
        (elem/set-absolute-position! btn 0 0)
        (elem/add-child! container btn))

      container)
    ;; No color - just regular button
    (elem/button props)))

;; bordered-box: Wraps content in a border
(defn bordered-box
  "Wrap a box with a visual border for debugging.

   Props:
     :border-color - [r g b a] or [r g b] (default red)
     :border - Border thickness (default 2)
     :bg-color - Background color [r g b a] (optional)
     :rounding - Corner rounding (default 0)
     :gap, :align, :position, :size, :margin - Standard box props

   Returns a column containing a background rectangle and the content."
  [{:keys [border-color border bg-color rounding gap align position size margin]
    :or {border-color [255 0 0 255] border 2 rounding 0 gap 0}}]
  ;; Create a rectangle as background with border
  (elem/rectangle {:color (or bg-color [0 0 0 0])  ; Transparent if no bg
                   :border-color border-color
                   :border border
                   :rounding rounding
                   :size size
                   :margin margin
                   :grow true}))
