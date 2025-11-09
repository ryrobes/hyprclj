(ns clean-responsive
  "Clean responsive example - let first resize event do initial render."
  (:require [hyprclj.core :as hypr]
            [hyprclj.dsl :refer [mount!]]
            [hyprclj.util :as util]))

(defn ui-component
  "Simple centered UI"
  [[w h]]
  [:column {:gap 15 :margin 10}
   [:text {:content "Clean Responsive Test" :font-size 24}]
   [:text {:content (str "Size: " w " x " h) :font-size 16}]
   [:text {:content "Resize count will show in console" :font-size 14}]
   [:button {:label "Click" :size [120 40] :on-click #(println "Clicked!")}]])

(defn -main [& args]
  (println "\n=== Clean Responsive Test ===")
  (println "Window will be empty briefly, then render on first resize event\n")

  (hypr/create-backend!)

  (let [w 700
        h 500
        window (hypr/create-window
                {:title "Clean Responsive"
                 :size [w h]
                 :on-close (fn [_] (util/exit-clean!))})]

    ;; Enable responsive rendering - will mount on first resize event
    (hypr/enable-responsive-root! window ui-component)

    (println "Opening window - waiting for resize event...")
    (hypr/open-window! window)
    (hypr/enter-loop!)))

(comment
  (-main)
  )
