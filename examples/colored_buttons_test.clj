(ns colored-buttons-test
  "Test colored buttons using layering technique."
  (:require [hyprclj.core :as hypr]
            [hyprclj.dsl :refer [mount!]]
            [hyprclj.util :as util]))

(defn ui-component [[w h]]
  [:v-box {:gap 15
           :margin 20
           :position :absolute
           :size [(- w 40) (- h 40)]}

   [:text {:content "COLORED BUTTONS TEST" :font-size 24}]
   [:text {:content "Using :colored-button with :bg-color prop!" :font-size 12}]

   ;; Row of colored buttons
   [:h-box {:gap 10}
    [:colored-button {:label "Red Button"
                      :size [120 35]
                      :bg-color [255 100 100 255]
                      :on-click #(println "Red clicked!")}]

    [:colored-button {:label "Green"
                      :size [100 35]
                      :bg-color [100 255 100 255]
                      :on-click #(println "Green clicked!")}]

    [:colored-button {:label "Blue"
                      :size [100 35]
                      :bg-color [100 150 255 255]
                      :on-click #(println "Blue clicked!")}]]

   ;; Row with borders and rounding
   [:h-box {:gap 10}
    [:colored-button {:label "Orange + Border"
                      :size [150 35]
                      :bg-color [255 180 100 255]
                      :border-color [200 100 0 255]
                      :border 2
                      :rounding 8
                      :on-click #(println "Orange!")}]

    [:colored-button {:label "Purple"
                      :size [120 35]
                      :bg-color [200 100 255 255]
                      :rounding 15
                      :on-click #(println "Purple!")}]]

   ;; Semi-transparent buttons
   [:h-box {:gap 10}
    [:colored-button {:label "Semi Red"
                      :size [110 35]
                      :bg-color [255 100 100 180]  ; Alpha=180
                      :on-click #(println "Semi-red!")}]

    [:colored-button {:label "Semi Blue"
                      :size [110 35]
                      :bg-color [100 150 255 180]
                      :on-click #(println "Semi-blue!")}]]

   [:text {:content "Mix of opaque and semi-transparent backgrounds!" :font-size 10}]])

(defn -main [& args]
  (println "\n=== Colored Buttons Test ===")
  (println "Features:")
  (println "  - :bg-color for button backgrounds")
  (println "  - :border-color and :border for borders")
  (println "  - :rounding for rounded corners")
  (println "  - Semi-transparency via alpha channel\n")

  (hypr/create-backend!)

  (let [w 650
        h 400
        window (hypr/create-window
                {:title "Colored Buttons"
                 :size [w h]
                 :on-close (fn [_] (util/exit-clean!))})]

    (hypr/enable-responsive-root! window
      ui-component
      {:position :absolute})

    (hypr/open-window! window)
    (hypr/enter-loop!)))

(comment (-main))
