(ns zero-margin-test
  "Test with explicit zero margin on everything."
  (:require [hyprclj.core :as hypr]
            [hyprclj.dsl :refer [mount!]]
            [hyprclj.elements :as elem]
            [hyprclj.util :as util]))

(defn ui-component
  "UI with EXPLICIT zero margin and left alignment"
  [[w h]]
  [:column {:gap 15
            :margin 0     ; EXPLICIT zero margin!
            :align :left} ; EXPLICIT left alignment!
   [:text {:content "Zero Margin + Left Align Test"
           :font-size 24
           :margin 0}]
   [:text {:content (str "Size: " w "x" h)
           :font-size 16
           :margin 0}]
   [:text {:content "Column has :align :left"
           :font-size 14
           :margin 0}]
   [:button {:label "Click"
             :size [150 40]
             :margin 0
             :on-click #(println "Clicked!")}]])

(defn -main [& args]
  (println "Testing with explicit zero margins...")

  (hypr/create-backend!)

  (let [window-width 600
        window-height 400
        window (hypr/create-window
                {:title "Zero Margin Test"
                 :size [window-width window-height]
                 :on-close (fn [w]
                             (util/exit-clean!))})]

    ;; Configure root element
    (let [root (hypr/root-element window)]
      (println "Configuring root element...")
      (elem/set-margin! root 0)
      ;; Try to make root align its children to top-left
      (elem/set-align! root :left)

      (mount! root
              (ui-component [window-width window-height])
              [window-width window-height])

      ;; Also configure the mounted column after mounting
      (println "Getting first child (column) to configure it...")
      ;; The column should be the first child of root
      )

    (println "Mount complete")

    ;; Enable resize
    (hypr/enable-responsive-root! window ui-component)

    (hypr/open-window! window)
    (hypr/enter-loop!)))

(comment
  (-main)
  )
