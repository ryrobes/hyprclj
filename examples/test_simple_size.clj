(ns test-simple-size
  "Test with explicit window-sized wrapper."
  (:require [hyprclj.core :as hypr]
            [hyprclj.elements :as elem]
            [hyprclj.util :as util]))

(defn -main [& args]
  (println "Testing with explicit wrapper size...")

  (hypr/create-backend!)

  (let [window-width 600
        window-height 400
        window (hypr/create-window
                {:title "Test Simple Size"
                 :size [window-width window-height]
                 :on-close (fn [w]
                             (println "\nWindow closing...")
                             (util/exit-clean!))})
        root (hypr/root-element window)]

    (println "Creating wrapper with size:" window-width "x" window-height)

    ;; Create a full-sized wrapper column using the known window dimensions
    (let [wrapper (elem/column-layout {:size [window-width window-height] :gap 15 :margin 20})]
      ;; Add content to wrapper
      (elem/add-child! wrapper (elem/text {:content "Test with sized wrapper" :font-size 24}))
      (elem/add-child! wrapper (elem/text {:content (str "Wrapper is " window-width "x" window-height) :font-size 14}))
      (elem/add-child! wrapper (elem/button {:label "Click me!" :size [150 40] :on-click #(println "Clicked!")}))

      ;; Add wrapper to root
      (elem/add-child! root wrapper))

    (hypr/open-window! window)
    (hypr/enter-loop!)))

(comment
  (-main)
  )
