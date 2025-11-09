(ns auto-simple
  "Simplest auto-updating counter example."
  (:require [hyprclj.core :as hypr]
            [hyprclj.dsl :refer [ratom compile-element]]
            [hyprclj.simple-reactive :refer [reactive-mount!]]
            [hyprclj.util :as util]))

(def counter (ratom 0))

(defn -main [& args]
  (println "=== Simple Auto-Updating Counter ===")
  (println "Click + or - buttons - UI updates automatically!")
  (println "")

  ;; Create backend
  (hypr/create-backend!)

  ;; Create window
  (let [window (hypr/create-window
                {:title "Auto Counter"
                 ;:size [450 350]
                 :on-close (fn [w]
                             (println "\nClosing...")
                             (util/exit-clean!))})]

    (println "Window created")

    ;; Mount reactive UI - auto-updates when counter changes!
    (reactive-mount! (hypr/root-element window)
                     [counter]  ; Watch this atom
                     (fn []
                       [:column {:gap 20 :margin 30 :grow true}
                        [:text {:content "Auto-Updating Counter"
                                :font-size 24}]

                       ;; This updates automatically!
                        [:text {:content (str "Count: " @counter)
                                :font-size 42}]

                        [:row {:gap 10  :grow true}
                         [:button {:label "+"
                                   :size [80 60]
                                   :on-click (fn []
                                               (swap! counter inc)
                                               (println "Clicked + (count now:" @counter ")"))}]

                         [:button {:label "-"
                                   :size [80 60]
                                   :on-click (fn []
                                               (swap! counter dec)
                                               (println "Clicked - (count now:" @counter ")"))}]

                         [:button {:label "Reset"
                                   :size [80 60]
                                   :on-click (fn []
                                               (reset! counter 0)
                                               (println "Reset (count now:" @counter ")"))}]]

                        [:text {:content "UI updates automatically - no manual code!"
                                :font-size 12}]

                        [util/make-quit-button {:size [150 50]}]]))

    (println "Reactive UI mounted!")
    (println "Opening window...")

    ;; Open and run
    (hypr/open-window! window)
    (println "Window opened - try clicking buttons!")
    (println "Watch the UI update automatically when you click!")
    (println "")

    (hypr/enter-loop!)

    (println "\nApplication exited")))

(comment
  (-main))
