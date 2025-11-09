# Layout & Positioning Guide

## Hyprtoolkit Layout System

### Available Options:

**Position Modes**:
- `HT_POSITION_ABSOLUTE` - Fixed positioning
- `HT_POSITION_AUTO` - Layout-driven (default)

**Position Flags** (Alignment):
- `HT_POSITION_FLAG_HCENTER` - Horizontal center
- `HT_POSITION_FLAG_VCENTER` - Vertical center
- `HT_POSITION_FLAG_CENTER` - Both centered
- `HT_POSITION_FLAG_LEFT` - Align left
- `HT_POSITION_FLAG_RIGHT` - Align right
- `HT_POSITION_FLAG_TOP` - Align top
- `HT_POSITION_FLAG_BOTTOM` - Align bottom

**Sizing**:
- `setGrow(bool)` - Expand to fill space
- `setGrow(bool h, bool v)` - Grow horizontal/vertical
- `preferredSize`, `minimumSize`, `maximumSize`

**Spacing**:
- `setMargin(float)` - Uniform margin
- `gap` on layouts - Space between children

## Re-com Style API Design

### h-box / v-box (We have these!)

```clojure
;; v-box (column)
[:column {:gap 10          ; Space between children
          :margin 20       ; Outer padding
          :grow true       ; Expand to fill
          :align :center   ; ← NEW: Align children
          :justify :start  ; ← NEW: Justify distribution
          :size [300 400]} ; Fixed size
  children...]

;; h-box (row)
[:row {:gap 5
       :align :center
       :justify :space-between}
  children...]
```

### Alignment Options (Like re-com!)

```clojure
:align #{:start :center :end :stretch}  ; Cross-axis alignment
:justify #{:start :center :end :space-between :space-around}  ; Main axis

;; For v-box (column):
:align → horizontal alignment of children
:justify → vertical distribution

;; For h-box (row):
:align → vertical alignment of children
:justify → horizontal distribution
```

### Size Options

```clojure
:size [width height]        ; Fixed size
:min-size [w h]            ; Minimum
:max-size [w h]            ; Maximum
:grow true                 ; Expand to fill
:grow-h true :grow-v false ; Selective growth
```

### Spacing

```clojure
:gap 10                  ; Between children
:margin 20               ; Outer (all sides)
:margin [10 20]          ; [vertical horizontal]
:margin [10 20 10 20]    ; [top right bottom left]
:padding 10              ; Inner spacing (same as gap for layouts)
```

## Implementation Plan

### 1. Expose Positioning Methods

Add to Element.java:
```java
public void setAlignment(String align) {
    // Map :center → HT_POSITION_FLAG_CENTER etc.
}

public void setPositioning(String mode) {
    // :absolute or :auto
}
```

### 2. Enhanced Layout Props

Support re-com style props in column-layout/row-layout:

```clojure
(defn column-layout [{:keys [gap margin align justify grow size]}]
  (let [layout (create-column gap size)]
    (when align (set-alignment! layout align))
    (when justify (set-justify! layout justify))
    (when grow (set-grow! layout grow))
    layout))
```

### 3. Helper Components (Like re-com!)

```clojure
(defn box [opts & children]
  "Flexible box with full layout control"
  [:column (merge {:gap 0} opts) children])

(defn h-box [opts & children]
  "Horizontal box"
  [:row (merge {:gap 5} opts) children])

(defn v-box [opts & children]
  "Vertical box"
  [:column (merge {:gap 5} opts) children])

(defn spacer
  "Flexible spacer (like re-com/gap)"
  ([] (spacer {}))
  ([{:keys [size]}]
   [:column {:size (or size [1 1]) :grow true}]))

(defn gap [size]
  "Fixed gap (like re-com/gap)"
  [:column {:size [size size]}])
```

## Usage Examples

### Centered Content

```clojure
[:column {:align :center     ; Center children horizontally
          :justify :center   ; Center children vertically
          :grow true}        ; Fill parent
  [:text "Centered!"]]
```

### Header/Content/Footer

```clojure
[:column {:gap 0 :grow true}
  ;; Header (fixed)
  [:row {:margin 10}
    [:text "Header"]]

  ;; Content (grows to fill)
  [:column {:grow true :margin 20}
    content...]

  ;; Footer (fixed)
  [:row {:margin 10}
    [:text "Footer"]]]
```

### Toolbar (Space Between)

```clojure
[:row {:justify :space-between :margin 10}
  [:text "Left"]
  [:text "Right"]]
```

### Grid-like Layout

```clojure
[:column {:gap 10}
  [:row {:gap 10}
    [:button {:size [100 40]}]
    [:button {:size [100 40]}]
    [:button {:size [100 40]}]]
  [:row {:gap 10}
    [:button {:size [100 40]}]
    [:button {:size [100 40]}]
    [:button {:size [100 40]}]]]
```

## What's Missing vs What We Have

### We Have ✅:
- Column and Row layouts
- Gap (spacing between children)
- Margin (outer padding)
- Grow (expand to fill)
- Fixed sizes

### Missing (Easy to Add):
- Alignment flags (:center, :left, :right)
- Justify/distribution
- Helper components (box, spacer, gap)
- Better margin API (4-value support)

### Effort to Add:
- ~100 LOC for alignment support
- ~50 LOC for helper components
- ~50 LOC for enhanced props

**Total: ~200 LOC, 1-2 hours**

Would make layouts much more flexible and re-com-like!
