(ns fully-responsive
  "Fully responsive example that updates when window is resized."
  (:require [hyprclj.core :as hypr]
            [hyprclj.dsl :refer [mount!]]
            [hyprclj.util :as util]))

(defn ui-component
  "UI component that adapts to window size"
  [[w h]]
  [:column {:gap 15 :margin 20}
   [:text {:content "Fully Responsive Layout!"
           :font-size 24}]
   [:text {:content (str "Window size: " w " x " h)
           :font-size 16}]
   [:text {:content "Try resizing the window!"
           :font-size 14}]
   [:button {:label "Click me!"
             :size [150 40]
             :on-click #(println "Button clicked!")}]])

(defn -main [& args]
  (println "Testing fully responsive layout...")

  (hypr/create-backend!)

  (let [window-width 600
        window-height 400
        window (hypr/create-window
                {:title "Fully Responsive Test"
                 :size [window-width window-height]
                 :on-close (fn [w]
                             (println "\nWindow closing...")
                             (util/exit-clean!))})]

    ;; Initial mount with window size
    (mount! (hypr/root-element window)
            (ui-component [window-width window-height])
            [window-width window-height])

    (println "Mounted with initial size:" window-width "x" window-height)

    ;; Enable dynamic resizing
    (hypr/enable-responsive-root! window ui-component)
    (println "Enabled dynamic resize support")

    (hypr/open-window! window)
    (hypr/enter-loop!)))

(comment
  (-main)
  )
