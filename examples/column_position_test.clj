(ns column-position-test
  "Test setting position flags on the column itself."
  (:require [hyprclj.core :as hypr]
            [hyprclj.dsl :refer [mount!]]
            [hyprclj.elements :as elem]
            [hyprclj.util :as util]))

(defn -main [& args]
  (let [position (or (first args) "left")]
    (println "Testing column position:" position)

    (hypr/create-backend!)

    (let [w 600
          h 400
          window (hypr/create-window
                  {:title (str "Column Position: " position)
                   :size [w h]
                   :on-close (fn [_] (util/exit-clean!))})]

      ;; Mount the UI
      (let [root (hypr/root-element window)
            column (elem/column-layout {:gap 10 :size [w h]})]

        ;; Add content to column
        (elem/add-child! column (elem/text {:content (str "POSITION: " position) :font-size 20}))
        (elem/add-child! column (elem/text {:content (str "Size: " w "x" h) :font-size 14}))
        (elem/add-child! column (elem/text {:content "Testing position flags on column" :font-size 12}))
        (elem/add-child! column (elem/button {:label "Click" :size [120 40] :on-click #(println "Clicked!")}))

        ;; Set position flags ON THE COLUMN
        (case position
          "left" (elem/set-align! column :left)
          "right" (elem/set-align! column :right)
          "top" (elem/set-align! column :top)
          "bottom" (elem/set-align! column :bottom)
          "topleft" (do
                      (elem/set-align! column :left)
                      (elem/set-align! column :top))
          "absolute" (do
                       (elem/set-position-mode! column 0)  ; Absolute mode
                       (elem/set-absolute-position! column 0 0))
          nil)

        ;; Add column to root
        (elem/add-child! root column))

      (hypr/open-window! window)
      (hypr/enter-loop!))))

(comment
  (-main "left")
  (-main "right")
  (-main "topleft")
  (-main "absolute")
  )
