(ns clean-nesting
  "Clean example of v-box and h-box nesting with proper sizing."
  (:require [hyprclj.core :as hypr]
            [hyprclj.dsl :refer [mount!]]
            [hyprclj.util :as util]))

(defn ui-component [[w h]]
  [:v-box {:gap 15
           :margin 20
           :position :absolute}

   [:text {:content "CLEAN NESTING EXAMPLE" :font-size 24}]
   [:text {:content (str "Window: " w "x" h) :font-size 12}]

   ;; Horizontal row with BUTTONS and a nested VERTICAL section
   [:h-box {:gap 10}
    ;; Left side: buttons
    [:button {:label "Left Btn" :size [100 40] :on-click #(println "Left")}]
    [:button {:label "Middle Btn" :size [100 40] :on-click #(println "Middle")}]

    ;; Right side: NESTED v-box with multiple items
    [:v-box {:gap 3}  ; V-box INSIDE the h-box!
     [:text {:content "Nested V-Box →" :font-size 11}]
     [:text {:content "Line 1" :font-size 10}]
     [:text {:content "Line 2" :font-size 10}]]]

   ;; Another example: H-box with nested V-boxes
   [:h-box {:gap 15}
    ;; Column 1
    [:v-box {:gap 5}
     [:text {:content "Column 1" :font-size 14}]
     [:button {:label "A1" :size [60 25] :on-click #(println "A1")}]
     [:button {:label "A2" :size [60 25] :on-click #(println "A2")}]]

    ;; Column 2
    [:v-box {:gap 5}
     [:text {:content "Column 2" :font-size 14}]
     [:button {:label "B1" :size [60 25] :on-click #(println "B1")}]
     [:button {:label "B2" :size [60 25] :on-click #(println "B2")}]]

    ;; Column 3
    [:v-box {:gap 5}
     [:text {:content "Column 3" :font-size 14}]
     [:button {:label "C1" :size [60 25] :on-click #(println "C1")}]
     [:button {:label "C2" :size [60 25] :on-click #(println "C2")}]]]

   [:text {:content "^ Multiple nesting levels working! ^" :font-size 10}]])

(defn -main [& args]
  (println "=== Clean Nesting Test ===")

  (hypr/create-backend!)

  (let [w 700
        h 450
        window (hypr/create-window
                {:title "Clean Nesting"
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
