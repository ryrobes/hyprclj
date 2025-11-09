(ns final-position-test
  "Final test showing centered vs absolute positioning."
  (:require [hyprclj.core :as hypr]
            [hyprclj.dsl :refer [mount!]]
            [hyprclj.util :as util]))

(defn ui-component
  "Simple UI"
  [[w h] mode]
  [:column {:gap 10 :margin 10}
   [:text {:content (str "POSITION MODE: " mode) :font-size 24}]
   [:text {:content (str "Window: " w "x" h) :font-size 16}]
   [:text {:content (if (= mode :absolute)
                      "Should be TOP-LEFT (absolute)"
                      "Should be CENTERED (auto-layout)")
           :font-size 14}]
   [:button {:label "Click" :size [150 40] :on-click #(println "Clicked!")}]])

(defn -main [& args]
  (let [mode-str (or (first args) "centered")
        mode (keyword mode-str)]
    (println "Testing position mode:" mode)

    (hypr/create-backend!)

    (let [w 600
          h 400
          window (hypr/create-window
                  {:title (str "Position: " mode-str)
                   :size [w h]
                   :on-close (fn [_] (util/exit-clean!))})]

      ;; Enable responsive with position option
      (hypr/enable-responsive-root! window
        (fn [[w h]] (ui-component [w h] mode))
        {:position mode})  ; Pass mode here!

      (hypr/open-window! window)
      (hypr/enter-loop!))))

(comment
  (-main "centered")  ; Default auto-layout (centered)
  (-main "absolute")  ; Absolute positioning (top-left)
  )
