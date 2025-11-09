(ns smooth-counter
  "Counter with minimal flicker using granular reactive components."
  (:require [hyprclj.core :as hypr]
            [hyprclj.dsl :refer [ratom compile-element]]
            [hyprclj.elements :as el]
            [hyprclj.simple-reactive :refer [reactive-mount!]]
            [hyprclj.util :as util]))

(def counter (ratom 0))

(defn -main [& args]
  (println "=== Smooth Counter ===")
  (println "Uses granular reactivity for minimal flicker!")
  (println "")

  ;; Create backend
  (hypr/create-backend!)

  ;; Create window
  (let [window (hypr/create-window
                {:title "Smooth Counter"
                 :size [450 350]
                 :on-close (fn [w]
                             (println "\nClosing...")
                             (util/exit-clean!))})]

    (println "Window created")

    ;; Build static structure first
    (let [root (hypr/root-element window)
          col (el/column-layout {:gap 20 :margin 30})]

      ;; Add column to root
      (el/add-child! root col)

      ;; Add static header
      (el/add-child! col
        (compile-element
          [:text {:content "Smooth Counter"
                  :font-size 24}]))

      ;; Add REACTIVE counter display only
      ;; This is the only part that updates!
      (let [counter-container (el/column-layout {:gap 5})]
        (el/add-child! col counter-container)

        ;; Only this tiny component remounts on change
        (reactive-mount! counter-container
                        [counter]
                        (fn []
                          [:text {:content (str "Count: " @counter)
                                  :font-size 48}])))

      ;; Add static buttons (never remount)
      (let [button-row (el/row-layout {:gap 10})]
        (el/add-child! col button-row)

        (el/add-child! button-row
          (compile-element
            [:button {:label "+"
                      :size [80 60]
                      :on-click (fn []
                                  (swap! counter inc)
                                  (println "Count:" @counter))}]))

        (el/add-child! button-row
          (compile-element
            [:button {:label "-"
                      :size [80 60]
                      :on-click (fn []
                                  (swap! counter dec)
                                  (println "Count:" @counter))}]))

        (el/add-child! button-row
          (compile-element
            [:button {:label "Reset"
                      :size [80 60]
                      :on-click (fn []
                                  (reset! counter 0)
                                  (println "Count:" @counter))}])))

      ;; Add static text
      (el/add-child! col
        (compile-element
          [:text {:content "Only the count number updates - minimal flicker!"
                  :font-size 12}]))

      ;; Add quit button
      (el/add-child! col
        (compile-element
          [util/make-quit-button {:size [150 50]}])))

    (println "UI mounted with granular reactivity!")
    (println "Opening window...")

    ;; Open and run
    (hypr/open-window! window)
    (println "Window opened!")
    (println "Click buttons - only the count text updates, everything else is static")
    (println "")

    (hypr/enter-loop!)

    (println "\nApplication exited")))

(comment
  (-main)
  )
