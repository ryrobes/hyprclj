(ns nested-layout-demo
  "Demonstrate nested layouts with declarative positioning and alignment."
  (:require [hyprclj.core :as hypr]
            [hyprclj.dsl :refer [mount!]]
            [hyprclj.util :as util]))

(defn ui-component [[w h]]
  [:v-box {:gap 15
           :margin 20
           :position :absolute}  ; Root container at top-left

   ;; Header row
   [:h-box {:gap 10}
    [:text {:content "NESTED LAYOUT DEMO" :font-size 24}]
    [:button {:label "X" :size [30 30] :on-click #(println "Close")}]]

   ;; Info row
   [:text {:content (str "Window: " w "x" h " | Nested v-box and h-box") :font-size 12}]

   ;; Content area with nested layouts
   [:v-box {:gap 10 :margin 10}
    [:text {:content "Vertical section with nested horizontal rows:" :font-size 14}]

    ;; Nested h-box #1
    [:h-box {:gap 5}
     [:button {:label "Left" :size [80 30] :on-click #(println "Left")}]
     [:button {:label "Middle" :size [80 30] :on-click #(println "Middle")}]
     [:button {:label "Right" :size [80 30] :on-click #(println "Right")}]]

    ;; Nested h-box #2
    [:h-box {:gap 5}
     [:text {:content "Label:" :font-size 12}]
     [:button {:label "Option 1" :size [100 30] :on-click #(println "1")}]
     [:button {:label "Option 2" :size [100 30] :on-click #(println "2")}]]

    ;; Another nested v-box
    [:v-box {:gap 5 :margin 10}
     [:text {:content "Nested v-box inside v-box:" :font-size 12}]
     [:text {:content "- Line 1" :font-size 10}]
     [:text {:content "- Line 2" :font-size 10}]
     [:text {:content "- Line 3" :font-size 10}]]]

   ;; Footer
   [:h-box {:gap 10}
    [:button {:label "OK" :size [80 35] :on-click #(println "OK")}]
    [:button {:label "Cancel" :size [80 35] :on-click #(println "Cancel")}]]])

(defn -main [& args]
  (println "=== Nested Layout Demo ===")
  (println "Shows v-box and h-box nesting with declarative positioning\n")

  (hypr/create-backend!)

  (let [w 700
        h 550
        window (hypr/create-window
                {:title "Nested Layout Demo"
                 :size [w h]
                 :on-close (fn [_] (util/exit-clean!))})]

    ;; Enable responsive with absolute positioning
    (hypr/enable-responsive-root! window
      ui-component
      {:position :absolute})

    (hypr/open-window! window)
    (hypr/enter-loop!)))

(comment
  (-main)
  )
