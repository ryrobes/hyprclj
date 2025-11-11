(ns hyprclj.color
  "Color utilities for hex/string to RGBA vector conversion.")

(defn hex->int
  "Convert a hex string to integer."
  [hex-str]
  (Integer/parseInt hex-str 16))

(def parse-color
  "Parse color from hex string or vector to [r g b a] vector.

   Supported formats:
   - \"#RGB\" -> #RRGGBB (expands shorthand)
   - \"#RRGGBB\" -> [r g b 255]
   - \"#RRGGBBAA\" -> [r g b aa]
   - [r g b] -> [r g b 255]
   - [r g b a] -> [r g b a]

   Returns [r g b a] vector with values 0-255.

   Memoized for performance since colors are immutable."
  (memoize
    (fn [color]
      (cond
        ;; Already a vector - normalize to 4 elements
        (vector? color)
        (if (= 3 (count color))
          (conj color 255)  ; Add alpha if missing
          color)

        ;; Hex string
        (string? color)
        (let [hex (if (.startsWith color "#")
                    (subs color 1)
                    color)
              hex-len (count hex)]
          (case hex-len
            ;; #RGB -> #RRGGBB
            3 (let [r (hex->int (str (nth hex 0) (nth hex 0)))
                    g (hex->int (str (nth hex 1) (nth hex 1)))
                    b (hex->int (str (nth hex 2) (nth hex 2)))]
                [r g b 255])

            ;; #RRGGBB
            6 (let [r (hex->int (subs hex 0 2))
                    g (hex->int (subs hex 2 4))
                    b (hex->int (subs hex 4 6))]
                [r g b 255])

            ;; #RRGGBBAA
            8 (let [r (hex->int (subs hex 0 2))
                    g (hex->int (subs hex 2 4))
                    b (hex->int (subs hex 4 6))
                    a (hex->int (subs hex 6 8))]
                [r g b a])

            ;; Invalid format - return opaque white as fallback
            (do
              (println "Warning: Invalid hex color format:" color)
              [255 255 255 255])))

        ;; Unknown format - return opaque white as fallback
        :else
        (do
          (println "Warning: Unknown color format:" color)
          [255 255 255 255])))))

(defn normalize-color
  "Normalize any color input to [r g b a] vector.

   This is the main function to use for color conversion.
   Delegates to parse-color (which is memoized)."
  [color]
  (parse-color color))
