(ns responsive-test
  "Test responsive layouts - root element should resize with window."
  (:require [hyprclj.core :as hypr]
            [hyprclj.dsl :refer [mount!]]
            [hyprclj.util :as util]))

(defn -main [& args]
  (println "Testing responsive layouts...")
  (println "The column should fill the window and grow when resized")

  ;; Create backend
  (hypr/create-backend!)

  ;; Create window
  (let [window (hypr/create-window
                {:title "Responsive Layout Test"
                 :size [600 400]
                 :on-close (fn [w]
                             (println "\nWindow closing...")
                             (util/exit-clean!))})]

    ;; Create UI with :grow true - should now fill the window!
    (mount! (hypr/root-element window)
            [:column {:gap 15
                      :margin 20
                      :grow true}  ; This should now work!
             [:text {:content "Responsive Layout Test"
                     :font-size 24}]
             [:text {:content "This column has :grow true"
                     :font-size 14}]
             [:text {:content "It should fill the entire window"
                     :font-size 14}]
             [:text {:content "Try resizing the window!"
                     :font-size 14}]
             [:button {:label "Test Button"
                       :size [200 50]
                       :on-click #(println "Button clicked!")}]])

    ;; Open and run
    (println "Opening window with responsive root element...")
    (hypr/open-window! window)
    (hypr/enter-loop!)))

(comment
  (-main)
  )
