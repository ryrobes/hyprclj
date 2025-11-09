(ns flexbox-composition
  "Flexbox-like composition with v-box/h-box - borders as visual separators."
  (:require [hyprclj.core :as hypr]
            [hyprclj.dsl :refer [mount!]]
            [hyprclj.util :as util]))

(defn ui-component [[w h]]
  [:v-box {:gap 15
           :margin 20
           :position :absolute}

   ;; Header
   [:text {:content "FLEXBOX-STYLE COMPOSITION" :font-size 24}]
   [:text {:content (str "Window: " w "x" h " | Auto-layout composition") :font-size 12}]

   ;; Section 1: Horizontal button row with border separator
   [:v-box {:gap 5}
    [:rectangle {:color [0 0 0 0]
                 :border-color [255 0 0 255]
                 :border 2
                 :size [(- w 50) 2]}]  ; Horizontal line separator
    [:text {:content "Button Row" :font-size 14}]
    [:h-box {:gap 5}
     [:button {:label "Action 1" :size [90 30] :on-click #(println "1")}]
     [:button {:label "Action 2" :size [90 30] :on-click #(println "2")}]
     [:button {:label "Action 3" :size [90 30] :on-click #(println "3")}]

     [:v-box {:gap 5}
      ;; [:rectangle {:color [0 0 0 0]
      ;;              :border-color [255 0 0 255]
      ;;              :border 2
      ;;              ;:size [(- w 50) 2]
      ;;              }]  ; Horizontal line separator
      [:text {:content "Button Row" :font-size 14}]
      [:h-box {:gap 5}
       [:button {:label "Action 1a" :size [90 30] :on-click #(println "1")}]
       [:button {:label "Action 2a" :size [90 30] :on-click #(println "2")}]
       [:button {:label "Action 3a" :size [90 30] :on-click #(println "3")}]]]]]

   ;; Section 2: Nested layouts
   [:v-box {:gap 5}
    [:rectangle {:color [0 0 0 0]
                 :border-color [0 255 0 255]
                 :border 2
                 :size [(- w 50) 2]}]
    [:text {:content "Nested Content" :font-size 14}]
    [:v-box {:gap 3 :margin 10}
     [:text {:content "• Nested item 1" :font-size 11}]
     [:text {:content "• Nested item 2" :font-size 11}]
     [:text {:content "• Nested item 3" :font-size 11}]]]

   ;; Section 3: Form-like layout
   [:v-box {:gap 5}
    [:rectangle {:color [0 0 0 0]
                 :border-color [0 150 255 255]
                 :border 2
                 :size [(- w 50) 2]}]
    [:text {:content "Form Section" :font-size 14}]
    [:h-box {:gap 10}
     [:text {:content "Label:" :font-size 12}]
     [:button {:label "Option A" :size [80 25] :on-click #(println "A")}]
     [:button {:label "Option B" :size [80 25] :on-click #(println "B")}]]]

   ;; Footer
   [:v-box {:gap 5}
    [:rectangle {:color [0 0 0 0]
                 :border-color [255 255 255 128]
                 :border 1
                 :size [(- w 50) 2]}]
    [:h-box {:gap 10}
     [:button {:label "OK" :size [80 35] :on-click #(println "OK")}]
     [:button {:label "Cancel" :size [80 35] :on-click #(println "Cancel")}]]]

   [:text {:content "^ Pure auto-layout composition with border separators ^" :font-size 10}]])

(defn -main [& args]
  (println "=== Flexbox-Style Composition ===")
  (println "Using v-box/h-box with borders as separators\n")

  (hypr/create-backend!)

  (let [w 650
        h 600
        window (hypr/create-window
                {:title "Flexbox Composition"
                 :size [w h]
                 :on-close (fn [_] (util/exit-clean!))})]

    ;; Fully responsive!
    (hypr/enable-responsive-root! window
      ui-component
      {:position :absolute})

    (hypr/open-window! window)
    (hypr/enter-loop!)))

(comment
  (-main)
  )
