(ns positioned-responsive
  "Test with explicit positioning to eliminate gaps."
  (:require [hyprclj.core :as hypr]
            [hyprclj.dsl :refer [mount!]]
            [hyprclj.elements :as elem]
            [hyprclj.util :as util]))

(defn ui-component
  "UI component - column should fill entire window with NO gaps"
  [[w h]]
  [:column {:gap 15
            :align :left}  ; Explicitly align left
   [:text {:content "Positioned Responsive Test!"
           :font-size 24}]
   [:text {:content (str "Size: " w "x" h)
           :font-size 16}]
   [:text {:content "Should be flush left with no gap!"
           :font-size 14}]
   [:button {:label "Click me!"
             :size [150 40]
             :on-click #(println "Clicked!")}]])

(defn -main [& args]
  (println "Testing positioned responsive layout...")

  (hypr/create-backend!)

  (let [window-width 600
        window-height 400
        window (hypr/create-window
                {:title "Positioned Test"
                 :size [window-width window-height]
                 :on-close (fn [w]
                             (println "\nClosing...")
                             (util/exit-clean!))})]

    ;; Mount with size
    (let [root (hypr/root-element window)]
      ;; Try to configure the root element itself
      (println "Configuring root element...")
      (elem/set-grow! root true true)  ; Make root grow
      (elem/set-align! root :left)     ; Align left

      (mount! root
              (ui-component [window-width window-height])
              [window-width window-height]))

    (println "Mount complete, opening window...")

    ;; Enable resize
    (hypr/enable-responsive-root! window ui-component)

    (hypr/open-window! window)
    (hypr/enter-loop!)))

(comment
  (-main)
  )
