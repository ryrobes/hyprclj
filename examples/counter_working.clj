(ns counter-working
  "Working counter example with manual updates via timers."
  (:require [hyprclj.core :as hypr]
            [hyprclj.dsl :refer [mount! ratom]]
            [hyprclj.util :as util]))

(def counter (ratom 0))
(def window-atom (atom nil))

(declare update-ui!)

(defn build-ui []
  "Build the UI tree with current state"
  [:column {:gap 20 :margin 30}
   [:text {:content "Reactive Counter Demo"
           :font-size 24}]

   [:text {:content (str "Count: " @counter)
           :font-size 32}]

   [:row {:gap 10}
    [:button {:label "+"
              :size [80 50]
              :on-click (fn []
                          (swap! counter inc)
                          (println "Incremented! Count:" @counter)
                          ;; Schedule UI update
                          (hypr/add-idle! #(update-ui!)))}]

    [:button {:label "-"
              :size [80 50]
              :on-click (fn []
                          (swap! counter dec)
                          (println "Decremented! Count:" @counter)
                          (hypr/add-idle! #(update-ui!)))}]

    [:button {:label "Reset"
              :size [80 50]
              :on-click (fn []
                          (reset! counter 0)
                          (println "Reset! Count:" @counter)
                          (hypr/add-idle! #(update-ui!)))}]]

   [:text {:content "Click buttons to update counter"
           :font-size 12}]

   [:text {:content "Updates happen via remounting (no reconciliation yet)"
           :font-size 10}]])

(defn update-ui! []
  "Remount the UI with current state"
  (when-let [window @window-atom]
    (println "  Updating UI with count:" @counter)
    (mount! (hypr/root-element window) (build-ui))))

(defn -main [& args]
  (println "=== Working Counter Example ===")
  (println "This demonstrates:")
  (println "  - Button clicks work")
  (println "  - Atoms can be updated")
  (println "  - UI can be remounted with new data")
  (println "")

  ;; Create backend
  (hypr/create-backend!)

  ;; Create window
  (let [window (hypr/create-window
                {:title "Working Counter"
                 :size [450 350]
                 :on-close (fn [w]
                             (println "\n✓ Window close requested - exiting!")
                             (util/exit-clean!))})]

    (reset! window-atom window)
    (println "Window created")

    ;; Mount initial UI
    (mount! (hypr/root-element window) (build-ui))
    (println "UI mounted with initial count:" @counter)

    ;; Open and run
    (hypr/open-window! window)
    (println "Window opened - try clicking the buttons!")
    (println "")

    (hypr/enter-loop!)

    (println "\nApplication exited")))

(comment
  (-main)
  )
