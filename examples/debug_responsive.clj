(ns debug-responsive
  "Debug version with borders and better error handling."
  (:require [hyprclj.core :as hypr]
            [hyprclj.dsl :refer [mount!]]
            [hyprclj.elements :as elem]
            [hyprclj.util :as util]))

(defn add-debug-border!
  "Add a visible border to an element for debugging"
  [element color]
  ;; Note: Hyprtoolkit doesn't have borders, but we can use background colors
  ;; to visualize layout. For now, just return the element
  element)

(defn ui-component
  "UI component that adapts to window size - NO MARGIN to avoid offset"
  [[w h]]
  [:column {:gap 15}  ; REMOVED :margin to avoid right offset!
   [:text {:content "Debug Responsive Layout!"
           :font-size 24}]
   [:text {:content (str "Window size: " w " x " h)
           :font-size 16}]
   [:text {:content "No margin - should be flush left!"
           :font-size 14}]
   [:text {:content "Try resizing the window!"
           :font-size 12}]
   [:button {:label "Click me!"
             :size [150 40]
             :on-click #(println "Button clicked!")}]])

(defn -main [& args]
  (println "Testing debug responsive layout...")

  (hypr/create-backend!)

  (let [window-width 600
        window-height 400
        window (hypr/create-window
                {:title "Debug Responsive Test"
                 :size [window-width window-height]
                 :on-close (fn [w]
                             (println "\nWindow closing...")
                             (util/exit-clean!))})]

    (println "Initial window dims:" window-width "x" window-height)

    ;; Initial mount with window size
    (mount! (hypr/root-element window)
            (ui-component [window-width window-height])
            [window-width window-height])

    (println "Initial mount complete")

    ;; Enable dynamic resizing with better filtering
    (let [last-size (atom [window-width window-height])]
      (hypr/enable-responsive-root! window
        (fn [[w h]]
          (println "UI component called with size:" w "x" h)
          ;; Guard against invalid sizes - use last good size if invalid
          (if (and (pos? w) (pos? h))
            (do
              (reset! last-size [w h])
              (ui-component [w h]))
            (do
              (println "Invalid size, using last good size:" @last-size)
              (ui-component @last-size))))))

    (println "Enabled dynamic resize support")

    (hypr/open-window! window)
    (hypr/enter-loop!)))

(comment
  (-main)
  )
