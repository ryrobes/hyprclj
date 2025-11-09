(ns proper-bordered-layout
  "Proper bordered containers using absolute positioning at root level."
  (:require [hyprclj.core :as hypr]
            [hyprclj.elements :as elem]
            [hyprclj.util :as util]))

(defn create-bordered-sections
  "Create bordered sections as separate root-level layers.
   Returns a sequence of elements to add to root."
  [w h]
  (let [section-w 500
        padding 10
        y-positions [80 200 320]]  ; Y positions for each section

    (concat
      ;; Title (no border)
      [(let [text (elem/text {:content "PROPER BORDERED LAYOUT" :font-size 24})]
         (elem/set-position-mode! text 0)
         (elem/set-absolute-position! text 20 20)
         text)]

      ;; Section 1: Red border with buttons
      ;; Background rectangle
      [(let [bg (elem/rectangle {:color [30 30 30 200]
                                 :border-color [255 0 0 255]
                                 :border 3
                                 :rounding 5
                                 :size [section-w 100]})]
         (elem/set-position-mode! bg 0)
         (elem/set-absolute-position! bg 50 (first y-positions))
         bg)]
      ;; Content on top
      [(let [content (elem/column-layout {:gap 5})]
         (elem/set-position-mode! content 0)
         (elem/set-absolute-position! content (+ 50 padding) (+ (first y-positions) padding))
         (elem/add-child! content (elem/text {:content "Red Section (Buttons)" :font-size 14}))
         (let [btn-row (elem/row-layout {:gap 5})]
           (elem/add-child! btn-row (elem/button {:label "Btn 1" :size [70 25] :on-click #(println "1")}))
           (elem/add-child! btn-row (elem/button {:label "Btn 2" :size [70 25] :on-click #(println "2")}))
           (elem/add-child! btn-row (elem/button {:label "Btn 3" :size [70 25] :on-click #(println "3")}))
           (elem/add-child! content btn-row))
         content)]

      ;; Section 2: Green border with text
      [(let [bg (elem/rectangle {:color [30 30 30 200]
                                 :border-color [0 255 0 255]
                                 :border 3
                                 :size [section-w 100]})]
         (elem/set-position-mode! bg 0)
         (elem/set-absolute-position! bg 50 (second y-positions))
         bg)]
      [(let [content (elem/column-layout {:gap 5})]
         (elem/set-position-mode! content 0)
         (elem/set-absolute-position! content (+ 50 padding) (+ (second y-positions) padding))
         (elem/add-child! content (elem/text {:content "Green Section (Text)" :font-size 14}))
         (elem/add-child! content (elem/text {:content "- Item 1" :font-size 12}))
         (elem/add-child! content (elem/text {:content "- Item 2" :font-size 12}))
         content)]

      ;; Section 3: Blue border with footer buttons
      [(let [bg (elem/rectangle {:color [30 30 30 200]
                                 :border-color [0 150 255 255]
                                 :border 3
                                 :size [section-w 60]})]
         (elem/set-position-mode! bg 0)
         (elem/set-absolute-position! bg 50 (nth y-positions 2))
         bg)]
      [(let [content (elem/row-layout {:gap 10})]
         (elem/set-position-mode! content 0)
         (elem/set-absolute-position! content (+ 50 padding) (+ (nth y-positions 2) padding))
         (elem/add-child! content (elem/button {:label "OK" :size [80 30] :on-click #(println "OK")}))
         (elem/add-child! content (elem/button {:label "Cancel" :size [80 30] :on-click #(println "Cancel")}))
         content)])))

(defn -main [& args]
  (println "=== Proper Bordered Layout ===")

  (hypr/create-backend!)

  (let [w 600
        h 500
        window (hypr/create-window
                {:title "Proper Bordered Layout"
                 :size [w h]
                 :on-close (fn [_] (util/exit-clean!))})]

    (let [root (hypr/root-element window)
          sections (create-bordered-sections w h)]

      ;; Add all sections to root
      (doseq [elem sections]
        (elem/add-child! root elem)))

    (hypr/open-window! window)
    (hypr/enter-loop!)))

(comment
  (-main)
  )
