(ns nested-constrained
  "Nested layouts constrained to window size for visibility."
  (:require [hyprclj.core :as hypr]
            [hyprclj.dsl :refer [mount!]]
            [hyprclj.util :as util]))

(defn ui-component [[w h]]
  [:v-box {:gap 10
           :margin 20
           :position :absolute}  ; Root at top-left

   [:text {:content "NESTED LAYOUT (Constrained)" :font-size 24}]
   [:text {:content (str "Window: " w "x" h) :font-size 12}]

   ;; Horizontal button row with RED border for visibility
   [:v-box {:gap 5 :margin 5}
    [:rectangle {:color [0 0 0 0]
                 :border-color [255 0 0 255]
                 :border 2
                 :size [(- w 60) 100]}]  ; Constrained width!
    [:h-box {:gap 5}
     [:button {:label "Left" :size [80 30] :on-click #(println "Left")}]
     [:button {:label "Middle" :size [80 30] :on-click #(println "Middle")}]
     [:button {:label "Right" :size [80 30] :on-click #(println "Right")}]]]

   ;; Another section with GREEN border
   [:v-box {:gap 5 :margin 5}
    [:rectangle {:color [0 0 0 0]
                 :border-color [0 255 0 255]
                 :border 2
                 :size [(- w 60) 100]}]
    [:text {:content "Section 2 (Green border)" :font-size 14}]
    [:h-box {:gap 5}
     [:button {:label "Option 1" :size [100 30] :on-click #(println "1")}]
     [:button {:label "Option 2" :size [100 30] :on-click #(println "2")}]]]

   ;; Footer with BLUE border
   [:v-box {:gap 5 :margin 5}
    [:rectangle {:color [0 0 0 0]
                 :border-color [0 100 255 255]
                 :border 2
                 :size [(- w 60) 80]}]
    [:h-box {:gap 10}
     [:button {:label "OK" :size [80 35] :on-click #(println "OK")}]
     [:button {:label "Cancel" :size [80 35] :on-click #(println "Cancel")}]]]])

(defn -main [& args]
  (println "=== Nested Layout (Constrained & Responsive) ===")

  (hypr/create-backend!)

  (let [w 700
        h 550
        window (hypr/create-window
                {:title "Nested Constrained"
                 :size [w h]
                 :on-close (fn [_] (util/exit-clean!))})]

    ;; Enable responsive - borders will resize with window!
    (hypr/enable-responsive-root! window
      ui-component
      {:position :absolute})

    (hypr/open-window! window)
    (hypr/enter-loop!)))

(comment
  (-main)
  )
