(ns position-test
  "Test positioning the root container itself within the window."
  (:require [hyprclj.core :as hypr]
            [hyprclj.dsl :refer [mount!]]
            [hyprclj.elements :as elem]
            [hyprclj.util :as util]))

(defn ui-component
  "Simple UI"
  [[w h] position]
  [:column {:gap 10}
   [:text {:content (str "POSITION TEST: " position) :font-size 20}]
   [:text {:content (str "Window: " w "x" h) :font-size 14}]
   [:text {:content "Where is this column positioned?" :font-size 12}]
   [:button {:label "Click" :size [120 40] :on-click #(println "Clicked!")}]])

(defn -main [& args]
  (let [position (or (first args) "auto")]
    (println "Testing position:" position)

    (hypr/create-backend!)

    (let [w 600
          h 400
          window (hypr/create-window
                  {:title (str "Position Test: " position)
                   :size [w h]
                   :on-close (fn [_] (util/exit-clean!))})]

      (hypr/enable-responsive-root! window
        (fn [[w h]]
          (let [col (ui-component [w h] position)]
            col)))

      ;; After opening, try to configure the root or the column
      (let [root (hypr/root-element window)]
        (case position
          "left" (elem/set-align! root :left)
          "right" (elem/set-align! root :right)
          "top" (elem/set-align! root :top)
          "bottom" (elem/set-align! root :bottom)
          "center" (elem/set-align! root :center)
          "topleft" (do
                      (elem/set-align! root :left)
                      (elem/set-align! root :top))
          nil))

      (hypr/open-window! window)
      (hypr/enter-loop!))))

(comment
  (-main "left")
  (-main "right")
  (-main "center")
  (-main "top")
  (-main "bottom")
  (-main "topleft")
  )
