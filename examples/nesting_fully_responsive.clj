(ns nesting-fully-responsive
  "Fully responsive nested layout that sizes to window."
  (:require [hyprclj.core :as hypr]
            [hyprclj.dsl :refer [mount!]]
            [hyprclj.util :as util]))

(defn ui-component [[w h]]
  (let [timestamp (System/currentTimeMillis)
        content-w (- w 40)  ; Account for margins
        
        content-h (- h 40)]
    
    [:rectangle {:color [50 50 50 255]
                :border-color [255 0 0 255]
                :border 3
                :rounding 5
                ;:size [(- w 40) 60]
                 }
    
    [:v-box {:gap 15
             :margin 20
             :position :absolute
             :size [content-w content-h]}  ; EXPLICIT size for responsive!
     
     [:text {:content "FULLY RESPONSIVE NESTING" :font-size 24}]
     [:text {:content (str "Window: " w "x" h " | Timestamp: " timestamp) :font-size 12}]
     [:text {:content "Main v-box sized to window - resize to see!" :font-size 10}]

     ;; Border separator
     [:rectangle {:color [0 0 0 0]
                  :border-color [255 255 255 100]
                  :border 1
                  :size [content-w 2]}]

     ;; H-box with buttons and nested v-box
     [:rectangle {:color [50 50 50 255]
                  :border-color [255 0 0 255]
                  :border 3
                  :rounding 5
                  :size [(- w 40) 60]
                  }
      [:h-box {:gap 10}
      [:button {:label "Button 1" :size [90 30] :on-click #(println "1")}]
      [:button {:label "Button 2" :size [90 30] :on-click #(println "2")}]

      ;; Nested v-box
      [:v-box {:gap 3}
       [:text {:content "Nested →" :font-size 11}]
       [:text {:content (str "W:" w) :font-size 9}]
       [:text {:content (str "H:" h) :font-size 9}]]]]

     ;; Border separator
     [:rectangle {:color [0 0 0 0]
                  :border-color [255 255 255 100]
                  :border 1
                  :size [content-w 2]
                  }]

     ;; Three columns with border separators
     [:h-box {:gap 15}
      [:v-box {:gap 5}
       [:text {:content "Column 1" :font-size 12}]
       [:button {:label "A1" :size [60 25] :on-click #(println "A1")}]
       [:button {:label "A2" :size [60 25] :on-click #(println "A2")}]]

      [:v-box {:gap 5}
       [:text {:content "Column 2" :font-size 12}]
       [:button {:label "B1" :size [60 25] :on-click #(println "B1")}]
       [:button {:label "B2" :size [60 25] :on-click #(println "B2")}]]

      [:v-box {:gap 5}
       [:text {:content "Column 3" :font-size 12}]
       [:button {:label "C1" :size [60 25] :on-click #(println "C1")}]
       [:button {:label "C2" :size [60 25] :on-click #(println "C2")}]]]

     ;; Bottom text
     [:text {:content "^ Resize window to see responsiveness! ^" :font-size 10}]]]))

(defn -main [& args]
  (println "=== Fully Responsive Nesting ===")
  (println "Resize window - sizes and timestamp should update!\n")

  (hypr/create-backend!)

  (let [w 700
        h 500
        window (hypr/create-window
                {:title "Fully Responsive Nesting"
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
