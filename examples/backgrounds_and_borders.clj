(ns backgrounds-and-borders
  "Demonstrate :background and :border props on v-box/h-box."
  (:require [hyprclj.core :as hypr]
            [hyprclj.dsl :refer [mount!]]
            [hyprclj.util :as util]))

(defn ui-component [[w h]]
  (let [content-w (- w 40)]
    [:v-box {:gap 15
             :margin 20
             :position :absolute
             :size [(- w 40) (- h 40)]}

     [:text {:content "BACKGROUNDS & BORDERS" :font-size 24}]
     [:text {:content (str "Window: " w "x" h) :font-size 12}]

     ;; Section 1: V-box with semi-transparent background and border
     [:v-box {:gap 10
              :size [content-w 100]
              :background [50 50 50 200]      ; Semi-transparent dark (alpha=200)
              :border-color [255 0 0 255]     ; Red border
              :border 3
              :rounding 5}
      [:text {:content "Red border, semi-transparent bg (a=200)" :font-size 14}]
      [:text {:content "Opacity is the 4th value: [r g b a]" :font-size 11}]]

     ;; Section 2: H-box with background, no border
     [:h-box {:gap 10
              :size [content-w 80]
              :background [0 100 150 180]     ; Blue-ish, semi-transparent
              :rounding 8}
      [:button {:label "Button 1" :size [90 30] :on-click #(println "1")}]
      [:button {:label "Button 2" :size [90 30] :on-click #(println "2")}]
      [:button {:label "Button 3" :size [90 30] :on-click #(println "3")}]]

     ;; Section 3: V-box with border, transparent background
     [:v-box {:gap 5
              :size [content-w 100]
              :background [0 0 0 0]            ; Fully transparent (alpha=0)
              :border-color [0 255 0 255]      ; Green border
              :border 2
              :rounding 10}
      [:text {:content "Green border, transparent bg" :font-size 14}]
      [:text {:content "Background alpha=0 = invisible" :font-size 11}]]

     ;; Section 4: Nested boxes with backgrounds
     [:h-box {:gap 10}
      [:v-box {:gap 5
               :size [150 80]
               :background [100 50 50 150]    ; Red-ish
               :border-color [255 100 100 255]
               :border 2}
       [:text {:content "Box 1" :font-size 12}]
       [:button {:label "A" :size [50 25] :on-click #(println "A")}]]

      [:v-box {:gap 5
               :size [150 80]
               :background [50 100 50 150]    ; Green-ish
               :border-color [100 255 100 255]
               :border 2}
       [:text {:content "Box 2" :font-size 12}]
       [:button {:label "B" :size [50 25] :on-click #(println "B")}]]

      [:v-box {:gap 5
               :size [150 80]
               :background [50 50 100 150]    ; Blue-ish
               :border-color [100 100 255 255]
               :border 2}
       [:text {:content "Box 3" :font-size 12}]
       [:button {:label "C" :size [50 25] :on-click #(println "C")}]]]

     [:text {:content "All backgrounds use semi-transparent colors [r g b alpha]" :font-size 10}]]))

(defn -main [& args]
  (println "=== Backgrounds & Borders ===")
  (println "Colors format: [r g b alpha] where alpha 0-255")
  (println "Props: :background, :border, :border-color, :rounding\n")

  (hypr/create-backend!)

  (let [w 700
        h 600
        window (hypr/create-window
                {:title "Backgrounds & Borders"
                 :size [w h]
                 :on-close (fn [_] (util/exit-clean!))})]

    (hypr/enable-responsive-root! window
      ui-component
      {:position :absolute})

    (hypr/open-window! window)
    (hypr/enter-loop!)))

(comment
  (-main)
  )
