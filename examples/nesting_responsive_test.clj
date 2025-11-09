(ns nesting-responsive-test
  "Test that nested layouts are responsive - add timestamp to verify."
  (:require [hyprclj.core :as hypr]
            [hyprclj.dsl :refer [mount!]]
            [hyprclj.util :as util]))

(defn ui-component [[w h]]
  (let [timestamp (System/currentTimeMillis)]
    [:v-box {:gap 15
             :margin 20
             :position :absolute}

     [:text {:content "RESPONSIVE NESTING TEST" :font-size 24}]
     [:text {:content (str "Window: " w "x" h " | Render: " timestamp) :font-size 12}]
     [:text {:content "^ Timestamp changes on resize = it's working!" :font-size 10}]

     ;; H-box with buttons and nested v-box
     [:h-box {:gap 10}
      [:button {:label "Btn 1" :size [80 30] :on-click #(println "1")}]
      [:button {:label "Btn 2" :size [80 30] :on-click #(println "2")}]

      ;; Nested v-box (NO rectangles inside!)
      [:v-box {:gap 3}
       [:text {:content "Nested V →" :font-size 11}]
       [:text {:content (str "W:" w) :font-size 9}]
       [:text {:content (str "H:" h) :font-size 9}]]]

     ;; Three columns
     [:h-box {:gap 15}
      [:v-box {:gap 5}
       [:text {:content "Col 1" :font-size 12}]
       [:button {:label "A1" :size [60 25] :on-click #(println "A1")}]
       [:button {:label "A2" :size [60 25] :on-click #(println "A2")}]]

      [:v-box {:gap 5}
       [:text {:content "Col 2" :font-size 12}]
       [:button {:label "B1" :size [60 25] :on-click #(println "B1")}]
       [:button {:label "B2" :size [60 25] :on-click #(println "B2")}]]

      [:v-box {:gap 5}
       [:text {:content "Col 3" :font-size 12}]
       [:button {:label "C1" :size [60 25] :on-click #(println "C1")}]
       [:button {:label "C2" :size [60 25] :on-click #(println "C2")}]]]

     [:text {:content (str "Render timestamp: " timestamp " (changes on resize)") :font-size 10}]]))

(defn -main [& args]
  (println "=== Responsive Nesting Test ===")
  (println "Resize the window - timestamp should update!\n")

  (hypr/create-backend!)

  (let [w 700
        h 400
        window (hypr/create-window
                {:title "Responsive Nesting Test"
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
