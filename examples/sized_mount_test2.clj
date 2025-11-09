(ns sized-mount-test2
  "Test the improved mount! with size merging."
  (:require [hyprclj.core :as hypr]
            [hyprclj.dsl :refer [mount!]]
            [hyprclj.util :as util]))

(defn -main [& args]
  (println "Testing improved mount! with size merging...")

  (hypr/create-backend!)

  (let [window-width 600
        window-height 400
        window (hypr/create-window
                {:title "Improved Sized Mount Test"
                 :size [window-width window-height]
                 :on-close (fn [w]
                             (println "\nWindow closing...")
                             (util/exit-clean!))})]

    ;; Mount with window size - this merges size into the root column
    (mount! (hypr/root-element window)
            [:column {:gap 15 :margin 20}  ; No :grow needed, size is set!
             [:text {:content "Improved Sized Mount Test"
                     :font-size 24}]
             [:text {:content (str "Column is sized to " window-width "x" window-height)
                     :font-size 14}]
             [:text {:content "Content should fill the window!"
                     :font-size 14}]
             [:button {:label "Click me!"
                       :size [150 40]
                       :on-click #(println "Button clicked!")}]]
            [window-width window-height])

    (println "Mounted with size merged into root column:" window-width "x" window-height)

    (hypr/open-window! window)
    (hypr/enter-loop!)))

(comment
  (-main)
  )
