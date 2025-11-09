(ns test-absolute-final
  "HARDCODED test - absolute positioning."
  (:require [hyprclj.core :as hypr]
            [hyprclj.dsl :refer [mount!]]
            [hyprclj.util :as util]))

(defn ui-component [[w h]]
  [:column {:gap 10 :margin 10}
   [:text {:content "ABSOLUTE MODE" :font-size 24}]
   [:text {:content (str "Window: " w "x" h) :font-size 14}]
   [:text {:content "Should be TOP-LEFT" :font-size 12}]
   [:button {:label "Click" :size [120 40] :on-click #(println "Clicked!")}]])

(defn -main [& args]
  (println "HARDCODED ABSOLUTE MODE TEST")

  (hypr/create-backend!)

  (let [w 600
        h 400
        window (hypr/create-window
                {:title "ABSOLUTE MODE"
                 :size [w h]
                 :on-close (fn [_] (util/exit-clean!))})]

    ;; HARDCODED :absolute
    (hypr/enable-responsive-root! window
      ui-component
      {:position :absolute})  ; HARDCODED!

    (hypr/open-window! window)
    (hypr/enter-loop!)))
