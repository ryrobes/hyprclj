(ns auto-counter
  "Counter that auto-updates with component-level reactivity!"
  (:require [hyprclj.core :as hypr]
            [hyprclj.dsl :refer [ratom]]
            [hyprclj.reactive-component :refer [create-reactive-root]]
            [hyprclj.util :as util]))

;; State
(def counter (ratom 0))

(defn -main [& args]
  (println "=== Auto-Updating Counter ===")
  (println "This demonstrates AUTOMATIC UI updates!")
  (println "Click + or - and watch the UI update automatically")
  (println "")

  ;; Create backend
  (hypr/create-backend!)

  ;; Create window
  (let [window (hypr/create-window
                {:title "Auto-Updating Counter"
                 :size [500 400]
                 :on-close (fn [w]
                             (println "\nWindow closing...")
                             (util/exit-clean!))})]

    (println "Window created")

    ;; Create REACTIVE root - auto-updates when counter changes!
    (create-reactive-root window
      (fn []
        [:column {:gap 20 :margin 30}
         [:text {:content "Auto-Updating Counter"
                 :font-size 28}]

         [:text {:content "This UI updates automatically when you click buttons!"
                 :font-size 14}]

         ;; The counter value - updates automatically!
         [:text {:content (str "Count: " @counter)
                 :font-size 48}]

         ;; Buttons to change state
         [:row {:gap 10}
          [:button {:label "+"
                    :size [80 60]
                    :on-click (fn []
                                (println "Incrementing..." (swap! counter inc))
                                ;; NO MANUAL REMOUNTING NEEDED!
                                ;; UI updates automatically!
                                )}]

          [:button {:label "-"
                    :size [80 60]
                    :on-click (fn []
                                (println "Decrementing..." (swap! counter dec))
                                ;; UI auto-updates!
                                )}]

          [:button {:label "Reset"
                    :size [80 60]
                    :on-click (fn []
                                (println "Resetting..." (reset! counter 0))
                                ;; UI auto-updates!
                                )}]]

         [:text {:content "No manual remounting needed!"
                 :font-size 12}]

         [:text {:content "Component-level reactivity FTW!"
                 :font-size 12}]

         ;; Quit button
         [util/make-quit-button {:label "Quit" :size [150 50]}]]))

    (println "Reactive UI mounted - auto-updates enabled!")
    (println "Opening window...")

    ;; Open and run
    (hypr/open-window! window)
    (println "Window opened!")
    (println "Try clicking the buttons - watch the count update automatically!")
    (println "")

    (hypr/enter-loop!)

    (println "\nApplication exited")))

(comment
  (-main)
  )
