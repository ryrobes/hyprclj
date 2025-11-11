(ns hex-colors-test
  "Test hex color support in DSL."
  (:require [hyprclj.core :as core]
            [hyprclj.dsl :refer [mount!]]
            [hyprclj.util :as util]))

(defn ui-component []
  [:v-box {:gap 15 :margin 20}
   [:text {:content "Hex Color Support Test" :font-size 20}]

   ;; Test basic hex colors
   [:v-box {:gap 10}
    [:text {:content "Basic Hex Colors (#RRGGBB):" :font-size 14}]
    [:rectangle {:color "#FF5733" :size [200 40]}]  ; Orange-red
    [:rectangle {:color "#33FF57" :size [200 40]}]  ; Green
    [:rectangle {:color "#3357FF" :size [200 40]}]] ; Blue

   ;; Test hex colors with alpha
   [:v-box {:gap 10}
    [:text {:content "Hex Colors with Alpha (#RRGGBBAA):" :font-size 14}]
    [:rectangle {:color "#FF573380" :size [200 40]}]  ; Semi-transparent orange-red
    [:rectangle {:color "#33FF5780" :size [200 40]}]  ; Semi-transparent green
    [:rectangle {:color "#3357FF80" :size [200 40]}]] ; Semi-transparent blue

   ;; Test shorthand hex
   [:v-box {:gap 10}
    [:text {:content "Shorthand Hex (#RGB):" :font-size 14}]
    [:rectangle {:color "#F53" :size [200 40]}]  ; #FF5533
    [:rectangle {:color "#3F5" :size [200 40]}]  ; #33FF55
    [:rectangle {:color "#35F" :size [200 40]}]] ; #3355FF

   ;; Test border colors
   [:v-box {:gap 10}
    [:text {:content "Hex Border Colors:" :font-size 14}]
    [:rectangle {:color "#00000000"           ; Transparent fill
                 :border-color "#FF5733"      ; Orange-red border
                 :border 3
                 :size [200 40]}]
    [:rectangle {:color "#FFFFFF80"           ; Semi-transparent white fill
                 :border-color "#3357FF"      ; Blue border
                 :border 3
                 :size [200 40]}]]

   ;; Test mixing hex and vector colors
   [:v-box {:gap 10}
    [:text {:content "Mixed Hex and Vector Colors:" :font-size 14}]
    [:rectangle {:color "#FF5733"             ; Hex
                 :size [200 40]}]
    [:rectangle {:color [51 255 87 255]       ; Vector (same green as above)
                 :size [200 40]}]
    [:rectangle {:color "#33FF57"             ; Hex (should match previous)
                 :size [200 40]}]]

   ;; Test text colors
   [:v-box {:gap 10}
    [:text {:content "Text with Hex Colors:" :font-size 14}]
    [:text {:content "Red Text" :color "#FF0000" :font-size 16}]
    [:text {:content "Green Text" :color "#00FF00" :font-size 16}]
    [:text {:content "Blue Text" :color "#0000FF" :font-size 16}]
    [:text {:content "Semi-transparent Purple" :color "#FF00FF80" :font-size 16}]]])

(defn -main []
  (let [backend (core/create-backend!)
        window (core/create-window {:title "Hex Color Support"
                                    :on-close (fn [_] (util/exit-clean!))
                                    :size [300 750]})
        root (core/root-element window)]

    (mount! root (ui-component))
    (core/open-window! window)
    (core/enter-loop!)))
