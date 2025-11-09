(ns alignment-test
  "Test different alignment options to verify they work."
  (:require [hyprclj.core :as hypr]
            [hyprclj.dsl :refer [mount!]]
            [hyprclj.elements :as elem]
            [hyprclj.util :as util]))

(defn ui-left-aligned
  "Column with left-aligned children"
  [[w h]]
  [:column {:gap 10}
   [:text {:content "LEFT ALIGNED CHILDREN" :font-size 20}]
   [:text {:content "Short" :font-size 14}]
   [:text {:content "Medium length text" :font-size 14}]
   [:text {:content "This is a much longer text that should be left-aligned" :font-size 14}]
   [:button {:label "Left Button" :size [150 40] :on-click #(println "Clicked!")}]])

(defn ui-center-aligned
  "Column with center-aligned children"
  [[w h]]
  [:column {:gap 10 :align :center}
   [:text {:content "CENTER ALIGNED CHILDREN" :font-size 20}]
   [:text {:content "Short" :font-size 14}]
   [:text {:content "Medium length text" :font-size 14}]
   [:text {:content "This is a much longer text that should be centered" :font-size 14}]
   [:button {:label "Center Button" :size [150 40] :on-click #(println "Clicked!")}]])

(defn ui-right-aligned
  "Column with right-aligned children"
  [[w h]]
  [:column {:gap 10 :align :right}
   [:text {:content "RIGHT ALIGNED CHILDREN" :font-size 20}]
   [:text {:content "Short" :font-size 14}]
   [:text {:content "Medium length text" :font-size 14}]
   [:text {:content "This is a much longer text that should be right-aligned" :font-size 14}]
   [:button {:label "Right Button" :size [150 40] :on-click #(println "Clicked!")}]])

(defn -main [& args]
  (let [alignment (or (first args) "center")]
    (println "Testing alignment:" alignment)

    (hypr/create-backend!)

    (let [w 600
          h 400
          window (hypr/create-window
                  {:title (str "Alignment Test: " alignment)
                   :size [w h]
                   :on-close (fn [_] (util/exit-clean!))})]

      (let [ui-fn (case alignment
                    "left" ui-left-aligned
                    "center" ui-center-aligned
                    "right" ui-right-aligned
                    ui-center-aligned)]

        ;; Enable responsive
        (hypr/enable-responsive-root! window ui-fn))

      (hypr/open-window! window)
      (hypr/enter-loop!))))

(comment
  (-main "left")
  (-main "center")
  (-main "right")
  )
