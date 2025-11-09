(ns layered-borders
  "Test layering rectangles behind content for borders."
  (:require [hyprclj.core :as hypr]
            [hyprclj.elements :as elem]
            [hyprclj.util :as util]))

(defn bordered-section
  "Create a section with a border rectangle layered behind content.

   This uses a column with absolute-positioned children to layer them."
  [border-color content-w content-h & content-elements]
  (let [container (elem/column-layout {:size [content-w content-h]})]

    ;; Layer 1: Background rectangle (absolute at 0,0)
    (let [bg (elem/rectangle {:color [30 30 30 100]  ; Semi-transparent dark bg
                              :border-color border-color
                              :border 3
                              :size [content-w content-h]})]
      (elem/set-position-mode! bg 0)
      (elem/set-absolute-position! bg 0 0)
      (elem/add-child! container bg))

    ;; Layer 2: Content v-box (absolute at 10,10 for padding inside border)
    (let [content-vbox (elem/column-layout {:gap 5})]
      (elem/set-position-mode! content-vbox 0)
      (elem/set-absolute-position! content-vbox 10 10)  ; Padding from border
      (doseq [elem content-elements]
        (when elem
          (elem/add-child! content-vbox elem)))
      (elem/add-child! container content-vbox))

    container))

(defn -main [& args]
  (println "=== Layered Borders Test ===")

  (hypr/create-backend!)

  (let [w 700
        h 500
        window (hypr/create-window
                {:title "Layered Borders"
                 :size [w h]
                 :on-close (fn [_] (util/exit-clean!))})]

    (let [root (hypr/root-element window)
          main-col (elem/column-layout {:gap 15 :margin 20})]

      ;; Set absolute positioning on main column
      (elem/set-position-mode! main-col 0)
      (elem/set-absolute-position! main-col 0 0)

      ;; Add title
      (elem/add-child! main-col
        (elem/text {:content "LAYERED BORDERS" :font-size 24}))

      ;; Section 1: Red border with buttons
      (elem/add-child! main-col
        (bordered-section [255 0 0 255] 600 100
          (elem/text {:content "Red bordered section" :font-size 16})
          (let [h-row (elem/row-layout {:gap 10})]
            (elem/add-child! h-row (elem/button {:label "Button 1" :size [80 30] :on-click #(println "1")}))
            (elem/add-child! h-row (elem/button {:label "Button 2" :size [80 30] :on-click #(println "2")}))
            h-row)))

      ;; Section 2: Green border with text
      (elem/add-child! main-col
        (bordered-section [0 255 0 255] 600 80
          (elem/text {:content "Green bordered section" :font-size 16})
          (elem/text {:content "Content inside the border!" :font-size 12})))

      ;; Section 3: Blue border with buttons
      (elem/add-child! main-col
        (bordered-section [0 150 255 255] 600 60
          (let [h-row (elem/row-layout {:gap 10})]
            (elem/add-child! h-row (elem/button {:label "OK" :size [80 30] :on-click #(println "OK")}))
            (elem/add-child! h-row (elem/button {:label "Cancel" :size [80 30] :on-click #(println "Cancel")}))
            h-row)))

      (elem/add-child! root main-col))

    (hypr/open-window! window)
    (hypr/enter-loop!)))

(comment
  (-main)
  )
