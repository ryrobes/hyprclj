(ns hyprclj.color-fx
  "Color manipulation and effects utilities.

   Provides functions for manipulating colors: brighten, darken, mix, and opacity adjustments."
  (:require [hyprclj.color :as color]))

(defn brighten
  "Brighten a color by a coefficient.

   Args:
     color - [r g b a] vector or hex string
     coeff - Brightness coefficient (0.0-1.0, default 0.2)
             0.2 = 20% brighter

   Returns: [r g b a] vector with adjusted brightness

   Example:
     (brighten [100 150 200 255] 0.3)  ; Make 30% brighter
     (brighten \"#FF5733\" 0.2)         ; Make 20% brighter"
  ([color] (brighten color 0.2))
  ([color coeff]
   (let [[r g b a] (color/normalize-color color)
         factor (+ 1.0 coeff)]
     [(min 255 (int (* r factor)))
      (min 255 (int (* g factor)))
      (min 255 (int (* b factor)))
      a])))

(defn darken
  "Darken a color by a coefficient.

   Args:
     color - [r g b a] vector or hex string
     coeff - Darkness coefficient (0.0-1.0, default 0.2)
             0.2 = 20% darker

   Returns: [r g b a] vector with reduced brightness

   Example:
     (darken [100 150 200 255] 0.3)  ; Make 30% darker
     (darken \"#FF5733\" 0.2)         ; Make 20% darker"
  ([color] (darken color 0.2))
  ([color coeff]
   (let [[r g b a] (color/normalize-color color)
         factor (- 1.0 coeff)]
     [(max 0 (int (* r factor)))
      (max 0 (int (* g factor)))
      (max 0 (int (* b factor)))
      a])))

(defn mix
  "Mix two colors together.

   Args:
     color1 - First color [r g b a] or hex string
     color2 - Second color [r g b a] or hex string
     ratio  - Mix ratio (0.0-1.0, default 0.5)
              0.0 = all color1
              0.5 = 50/50 mix
              1.0 = all color2

   Returns: [r g b a] vector with blended colors

   Example:
     (mix [255 0 0 255] [0 0 255 255] 0.5)  ; Purple (50% red + 50% blue)
     (mix \"#FF0000\" \"#0000FF\" 0.3)        ; More red than blue"
  ([color1 color2] (mix color1 color2 0.5))
  ([color1 color2 ratio]
   (let [[r1 g1 b1 a1] (color/normalize-color color1)
         [r2 g2 b2 a2] (color/normalize-color color2)
         r1-ratio (- 1.0 ratio)]
     [(int (+ (* r1 r1-ratio) (* r2 ratio)))
      (int (+ (* g1 r1-ratio) (* g2 ratio)))
      (int (+ (* b1 r1-ratio) (* b2 ratio)))
      (int (+ (* a1 r1-ratio) (* a2 ratio)))])))

(defn fade
  "Adjust the opacity/alpha of a color.

   This multiplies the existing alpha channel by the fade factor.

   Args:
     color - [r g b a] vector or hex string
     alpha - Opacity multiplier (0.0-1.0)
             0.0 = fully transparent
             0.5 = 50% opacity
             1.0 = fully opaque

   Returns: [r g b a] vector with adjusted alpha

   Example:
     (fade [255 0 0 255] 0.5)   ; Red at 50% opacity
     (fade \"#FF5733\" 0.3)      ; Semi-transparent orange"
  [color alpha]
  (let [[r g b a] (color/normalize-color color)]
    [r g b (int (* a alpha))]))

(defn set-alpha
  "Set the alpha channel to a specific value (0-255).

   This replaces the alpha channel completely.

   Args:
     color - [r g b a] vector or hex string
     alpha - New alpha value (0-255)

   Returns: [r g b a] vector with new alpha

   Example:
     (set-alpha [255 0 0 255] 128)  ; Red at ~50% opacity
     (set-alpha \"#FF5733\" 200)     ; Orange with alpha=200"
  [color alpha]
  (let [[r g b _] (color/normalize-color color)]
    [r g b (max 0 (min 255 alpha))]))

(defn grayscale
  "Convert a color to grayscale.

   Uses the luminosity method: 0.299*R + 0.587*G + 0.114*B

   Args:
     color - [r g b a] vector or hex string

   Returns: [r g b a] vector in grayscale

   Example:
     (grayscale [255 100 50 255])  ; Orange to gray"
  [color]
  (let [[r g b a] (color/normalize-color color)
        gray (int (+ (* r 0.299) (* g 0.587) (* b 0.114)))]
    [gray gray gray a]))

(defn invert
  "Invert a color (negative).

   Args:
     color - [r g b a] vector or hex string

   Returns: [r g b a] vector with inverted RGB values

   Example:
     (invert [255 0 0 255])  ; Red -> Cyan"
  [color]
  (let [[r g b a] (color/normalize-color color)]
    [(- 255 r) (- 255 g) (- 255 b) a]))

(comment
  ;; Example usage:

  ;; Brighten and darken
  (brighten [100 150 200 255] 0.3)  ; => [130 195 255 255]
  (darken [100 150 200 255] 0.3)    ; => [70 105 140 255]

  ;; Mix colors
  (mix [255 0 0 255] [0 0 255 255])  ; => [127 0 127 255] (purple)

  ;; Fade/opacity
  (fade [255 0 0 255] 0.5)  ; => [255 0 0 127]

  ;; Grayscale
  (grayscale [255 100 50 255])  ; => [132 132 132 255]
  )
