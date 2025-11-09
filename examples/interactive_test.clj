(ns interactive-test
  "Interactive test to verify button clicks and close handling."
  (:require [hyprclj.core :as hypr]
            [hyprclj.dsl :refer [compile-element mount! ratom]]
            [hyprclj.util :as util]))

(def click-count (atom 0))

(defn -main [& args]
  (println "=== Interactive Test ===")
  (println "Click the buttons to test functionality")
  (println "Try closing the window with Hyprland's close command")
  (println "")

  ;; Create backend
  (hypr/create-backend!)

  ;; Create window with close handler
  (let [window (hypr/create-window
                {:title "Interactive Test"
                 :size [500 400]
                 :on-close (fn [w]
                             (println "\n✓ Window close via Hyprland - exiting...")
                             (util/exit-clean!))})]

    (println "Window created successfully")

    ;; Create UI with multiple interactive elements
    (mount! (hypr/root-element window)
            [:column {:gap 15 :margin 30}
             [:text {:content "Interactive Test"
                     :font-size 24}]

             [:text {:content "Click the buttons below:"
                     :font-size 14}]

             ;; Test buttons
             [:button {:label "Click Me!"
                       :size [200 50]
                       :on-click (fn []
                                   (swap! click-count inc)
                                   (println (str "✓ Button 1 clicked! Count: " @click-count)))}]

             [:button {:label "Also Click Me!"
                       :size [200 50]
                       :on-click (fn []
                                   (println "✓ Button 2 clicked!")
                                   (println "  Buttons are working!"))}]

             [:button {:label "Print State"
                       :size [200 50]
                       :on-click (fn []
                                   (println "✓ Button 3 clicked!")
                                   (println "  Current click count:" @click-count))}]

             [:text {:content "Check the console for output"
                     :font-size 12}]

             [util/make-quit-button {:label "Quit App" :size [200 50]}]

             [:text {:content "Use 'Quit App' button or Mod+Shift+C to exit"
                     :font-size 12}]])

    (println "UI mounted successfully")
    (println "Opening window...")

    ;; Open and run
    (hypr/open-window! window)
    (println "Window opened - should be visible now!")
    (println "Entering event loop...")
    (println "")

    (hypr/enter-loop!)

    (println "\nEvent loop exited - application closing")))

(comment
  (-main)
  )
