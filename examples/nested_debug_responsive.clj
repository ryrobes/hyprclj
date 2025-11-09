(ns nested-debug-responsive
  "Responsive nested layouts with debug borders that fit in viewport."
  (:require [hyprclj.core :as hypr]
            [hyprclj.dsl :refer [mount!]]
            [hyprclj.util :as util]))

(defn ui-component [[w h]]
  (let [content-w (- w 60)  ; Leave margins
        section-h 80]       ; Fixed height per section

    [:v-box {:gap 10
             :margin 20
             :position :absolute}

     [:text {:content "NESTED WITH DEBUG BORDERS" :font-size 20}]
     [:text {:content (str "Window: " w "x" h " | Red=v-box, Green=h-box") :font-size 11}]

     ;; Section 1: H-box with GREEN border
     [:v-box {:gap 0}
      [:rectangle {:color [0 0 0 0]
                   :border-color [0 255 0 255]
                   :border 2
                   :size [content-w section-h]}]
      [:h-box {:gap 5}
       [:button {:label "Btn 1" :size [70 25] :on-click #(println "1")}]
       [:button {:label "Btn 2" :size [70 25] :on-click #(println "2")}]
       [:button {:label "Btn 3" :size [70 25] :on-click #(println "3")}]]]

     ;; Section 2: V-box with BLUE border
     [:v-box {:gap 0}
      [:rectangle {:color [0 0 0 0]
                   :border-color [0 150 255 255]
                   :border 2
                   :size [content-w section-h]}]
      [:text {:content "Nested vertical (blue)" :font-size 12}]
      [:text {:content "- Line 1" :font-size 10}]
      [:text {:content "- Line 2" :font-size 10}]]

     ;; Section 3: H-box footer with YELLOW border
     [:v-box {:gap 0}
      [:rectangle {:color [0 0 0 0]
                   :border-color [255 255 0 255]
                   :border 2
                   :size [content-w section-h]}]
      [:h-box {:gap 10}
       [:button {:label "OK" :size [80 30] :on-click #(println "OK")}]
       [:button {:label "Cancel" :size [80 30] :on-click #(println "Cancel")}]]]

     [:text {:content "^ All borders resize with window ^" :font-size 10}]]))

(defn -main [& args]
  (println "=== Nested Debug Responsive ===")

  (hypr/create-backend!)

  (let [w 700
        h 550
        window (hypr/create-window
                {:title "Nested Debug Responsive"
                 :size [w h]
                 :on-close (fn [_] (util/exit-clean!))})]

    ;; Enable responsive
    (hypr/enable-responsive-root! window
      ui-component
      {:position :absolute})

    (hypr/open-window! window)
    (hypr/enter-loop!)))

(comment
  (-main)
  )
