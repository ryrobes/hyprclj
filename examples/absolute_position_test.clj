(ns absolute-position-test
  "Test absolute positioning set BEFORE adding to parent."
  (:require [hyprclj.core :as hypr]
            [hyprclj.elements :as elem]
            [hyprclj.util :as util]))

(defn -main [& args]
  (println "Testing absolute positioning...")

  (hypr/create-backend!)

  (let [w 600
        h 400
        window (hypr/create-window
                {:title "Absolute Position Test"
                 :size [w h]
                 :on-close (fn [_] (util/exit-clean!))})]

    (let [root (hypr/root-element window)
          ;; Create column
          column (elem/column-layout {:gap 10 :size [w h]})]

      ;; Add content
      (elem/add-child! column (elem/text {:content "ABSOLUTE POSITION TEST" :font-size 20}))
      (elem/add-child! column (elem/text {:content "Should be at (0, 0) top-left" :font-size 14}))
      (elem/add-child! column (elem/text {:content (str "Window: " w "x" h) :font-size 12}))
      (elem/add-child! column (elem/button {:label "Click" :size [120 40] :on-click #(println "Clicked!")}))

      ;; CRITICAL: Set position mode and position BEFORE adding to parent!
      (println "Setting column to absolute position (0, 0)")
      (elem/set-position-mode! column 0)         ; 0 = absolute
      (elem/set-absolute-position! column 0 0)   ; Position at (0, 0)

      ;; Also try setting NO margin to ensure that's not the offset
      (elem/set-margin! column 0)

      ;; Now add to root
      (println "Adding column to root")
      (elem/add-child! root column))

    (hypr/open-window! window)
    (hypr/enter-loop!)))

(comment
  (-main)
  )
