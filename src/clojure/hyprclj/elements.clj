(ns hyprclj.elements
  "UI element constructors and utilities."
  (:import [org.hyprclj.bindings Element Button Text ColumnLayout RowLayout Textbox Checkbox Rectangle ScrollArea]))

;; Element utilities
(defn add-child!
  "Add a child element to a parent."
  [parent child]
  (.addChild parent child)
  parent)

(defn remove-child!
  "Remove a child element from a parent."
  [parent child]
  (.removeChild parent child)
  parent)

(defn clear-children!
  "Remove all children from an element."
  [element]
  (.clearChildren element)
  element)

(defn set-margin!
  "Set margin around an element.
   Can be called with:
     (set-margin! el 10)              ; all sides
     (set-margin! el 10 20)           ; vertical horizontal
     (set-margin! el 10 20 30 40)     ; top right bottom left"
  ([element all]
   (set-margin! element all all all all))
  ([element vertical horizontal]
   (set-margin! element vertical horizontal vertical horizontal))
  ([element top right bottom left]
   (.setMargin element top right bottom left)
   element))

(defn set-grow!
  "Set whether element should grow to fill space.
   Can be called with:
     (set-grow! el true)              ; Both directions
     (set-grow! el true false)        ; H and V separately"
  ([element grow?]
   (.setGrow element grow?)
   element)
  ([element grow-h grow-v]
   (.setGrow element grow-h grow-v)
   element))

(defn set-align!
  "Set alignment of element.
   Options: :center, :left, :right, :top, :bottom, :hcenter, :vcenter"
  [element align]
  (when align
    (.setAlign element (name align)))
  element)

(defn set-size!
  "Set the size of an element.
   Can be called with:
     (set-size! el 400 300)           ; width height
     (set-size! el [400 300])         ; [width height]
   Use -1 for auto-sizing in either dimension."
  ([element size-vec]
   (let [[w h] size-vec]
     (set-size! element w h)))
  ([element width height]
   (.setSize element width height)
   element))

(defn set-position-mode!
  "Set position mode for an element.
   Modes:
     0 = absolute positioning
     1 = auto (default, layout-based)"
  [element mode]
  (.setPositionMode element mode)
  element)

(defn set-absolute-position!
  "Set absolute position offset from parent.
   Usage:
     (set-absolute-position! el 10 20)  ; x y
     (set-absolute-position! el [10 20]) ; [x y]"
  ([element pos-vec]
   (let [[x y] pos-vec]
     (set-absolute-position! element x y)))
  ([element x y]
   (.setAbsolutePosition element x y)
   element))

;; Button
(defn button
  "Create a button element.

   Options:
     :label        - Button text
     :size         - [width height]
     :no-border    - Remove border (boolean)
     :no-bg        - Remove background (boolean)
     :font-size    - Font size in pixels
     :on-click     - Click handler function
     :on-right-click - Right-click handler
     :margin       - Margin (single int or [t r b l])
     :grow         - Whether to grow (boolean)

   Example:
     (button {:label \"Click me!\"
              :on-click #(println \"Clicked!\")
              :size [150 40]})"
  [{:keys [label size no-border no-bg font-size on-click on-right-click
           margin grow]
    :or {label "" font-size 12}}]
  (let [builder (Button/builder)]
    (.label builder label)
    (.fontSize builder font-size)
    (when no-border (.noBorder builder true))
    (when no-bg (.noBg builder true))
    (when size
      (let [[w h] size]
        (.size builder w h)))
    (when on-click
      (.onClick builder (fn [btn] (on-click))))
    (when on-right-click
      (.onRightClick builder (fn [btn] (on-right-click))))
    (let [btn (.build builder)]
      (when margin
        (if (vector? margin)
          (apply set-margin! btn margin)
          (set-margin! btn margin)))
      (when grow
        (set-grow! btn grow))
      btn)))

;; Text
(defn text
  "Create a text element.

   Options:
     :content      - Text content
     :font-size    - Font size in pixels
     :font-family  - Font family name
     :color        - [r g b a] or [r g b] (0-255)
     :align        - Text alignment (\"left\", \"center\", \"right\")
     :margin       - Margin
     :grow         - Whether to grow

   Example:
     (text {:content \"Hello, World!\"
            :font-size 24
            :color [255 255 255]})"
  [{:keys [content font-size font-family color align margin grow]
    :or {content "" font-size 12 align "left"}}]
  (let [builder (Text/builder)
        [r g b a] (or color [255 255 255 255])]
    (.content builder content)
    (.fontSize builder font-size)
    (when font-family
      (.fontFamily builder font-family))
    (.color builder r g b (or a 255))
    (.align builder align)
    (let [txt (.build builder)]
      (when margin
        (if (vector? margin)
          (apply set-margin! txt margin)
          (set-margin! txt margin)))
      (when grow
        (set-grow! txt grow))
      txt)))

;; Layouts
(defn column-layout
  "Create a vertical column layout.

   Options:
     :gap    - Gap between children in pixels
     :size   - [width height]
     :margin - Margin
     :grow   - Whether to grow (boolean or [h v])
     :align  - Alignment (:center, :left, :right, etc.)

   Example:
     (column-layout {:gap 10 :align :center})"
  [{:keys [gap size margin grow align]
    :or {gap 0}}]
  (let [builder (ColumnLayout/builder)
        _ (.gap builder gap)
        _ (when size
            (let [[w h] size]
              (.size builder w h)))
        layout (.build builder)]
    (when margin
      (if (vector? margin)
        (apply set-margin! layout margin)
        (set-margin! layout margin)))
    (when grow
      (if (vector? grow)
        (apply set-grow! layout grow)
        (set-grow! layout grow)))
    (when align
      (set-align! layout align))
    layout))

(defn row-layout
  "Create a horizontal row layout.

   Options:
     :gap    - Gap between children in pixels
     :size   - [width height]
     :margin - Margin
     :grow   - Whether to grow (boolean or [h v])
     :align  - Alignment (:center, :top, :bottom, etc.)

   Example:
     (row-layout {:gap 5 :align :center})"
  [{:keys [gap size margin grow align]
    :or {gap 0}}]
  (let [builder (RowLayout/builder)
        _ (.gap builder gap)
        _ (when size
            (let [[w h] size]
              (.size builder w h)))
        layout (.build builder)]
    (when margin
      (if (vector? margin)
        (apply set-margin! layout margin)
        (set-margin! layout margin)))
    (when grow
      (if (vector? grow)
        (apply set-grow! layout grow)
        (set-grow! layout grow)))
    (when align
      (set-align! layout align))
    layout))

;; Textbox
(defn textbox
  "Create a text input field.

   Options:
     :placeholder  - Placeholder text
     :initial-text - Initial text content
     :size         - [width height]
     :on-submit    - Handler called when Enter is pressed (receives text)
     :on-change    - Handler called when text changes (receives text)
     :margin       - Margin
     :grow         - Whether to grow

   Example:
     (textbox {:placeholder \"Enter task...\"
               :on-submit (fn [text] (println \"Submitted:\" text))
               :size [300 40]})"
  [{:keys [placeholder initial-text size on-submit on-change margin grow]
    :or {placeholder "" initial-text ""}}]
  (let [builder (Textbox/builder)]
    (.placeholder builder placeholder)
    (.initialText builder initial-text)
    (when size
      (let [[w h] size]
        (.size builder w h)))
    (when on-submit
      (.onSubmit builder on-submit))
    (when on-change
      (.onChange builder on-change))
    (let [tb (.build builder)]
      (when margin
        (if (vector? margin)
          (apply set-margin! tb margin)
          (set-margin! tb margin)))
      (when grow
        (set-grow! tb grow))
      tb)))

;; Checkbox
(defn checkbox
  "Create a checkbox element.

   Options:
     :label     - Label text (optional)
     :checked   - Initial checked state
     :on-change - Handler called when toggled (receives new state)
     :margin    - Margin
     :grow      - Whether to grow

   Example:
     (checkbox {:label \"Done\"
                :checked false
                :on-change (fn [checked?] (println \"Toggled:\" checked?))})"
  [{:keys [label checked on-change margin grow]
    :or {label "" checked false}}]
  (let [builder (Checkbox/builder)]
    (.label builder label)
    (.checked builder checked)
    (when on-change
      (.onChange builder on-change))
    (let [cb (.build builder)]
      (when margin
        (if (vector? margin)
          (apply set-margin! cb margin)
          (set-margin! cb margin)))
      (when grow
        (set-grow! cb grow))
      cb)))

;; Rectangle
(defn rectangle
  "Create a rectangle element (for backgrounds/borders).

   Options:
     :color        - [r g b a] or [r g b] background color (0-255)
     :border-color - [r g b a] or [r g b] border color
     :border       - Border thickness in pixels
     :rounding     - Corner rounding in pixels
     :size         - [width height]
     :margin       - Margin
     :grow         - Whether to grow

   Example:
     (rectangle {:color [100 100 200 255]
                 :border-color [255 0 0 255]
                 :border 2
                 :size [100 100]})"
  [{:keys [color border-color border rounding size margin grow]
    :or {color [255 255 255 255] border 0 rounding 0}}]
  (let [builder (Rectangle/builder)
        [r g b a] (if (= 3 (count color))
                    (conj (vec color) 255)
                    color)
        [br bg bb ba] (if border-color
                        (if (= 3 (count border-color))
                          (conj (vec border-color) 255)
                          border-color)
                        [0 0 0 255])]
    (.color builder r g b a)
    (when (and border-color (pos? border))
      (.borderColor builder br bg bb ba)
      (.borderThickness builder border))
    (when (pos? rounding)
      (.rounding builder rounding))
    (when size
      (let [[w h] size]
        (.size builder w h)))
    (let [rect (.build builder)]
      (when margin
        (if (vector? margin)
          (apply set-margin! rect margin)
          (set-margin! rect margin)))
      (when grow
        (set-grow! rect grow))
      rect)))

(defn scroll-area
  "Create a scrollable area (container).

   Options:
     :scroll-x - Enable horizontal scrolling (default false)
     :scroll-y - Enable vertical scrolling (default true)
     :block-scroll - Disable user scrolling (default false)
     :size - [width height] required!
     :margin - Margin
     :grow - Whether to grow

   Example:
     (scroll-area {:scroll-y true :size [400 300]})"
  [{:keys [scroll-x scroll-y block-scroll size margin grow]
    :or {scroll-x false scroll-y true block-scroll false}}]
  (let [builder (ScrollArea/builder)]
    (.scrollX builder scroll-x)
    (.scrollY builder scroll-y)
    (when block-scroll
      (.blockUserScroll builder true))
    (when size
      (let [[w h] size]
        (.size builder w h)))
    (let [scroll (.build builder)]
      (when margin
        (if (vector? margin)
          (apply set-margin! scroll margin)
          (set-margin! scroll margin)))
      (when grow
        (set-grow! scroll grow))
      scroll)))

;; Helper to add multiple children
(defn add-children!
  "Add multiple children to a parent element."
  [parent & children]
  (doseq [child children]
    (when child
      (add-child! parent child)))
  parent)
