(ns test-centered-final
  "HARDCODED test - centered positioning."
  (:require [hyprclj.core :as hypr]
            [hyprclj.dsl :refer [mount!]]
            [hyprclj.util :as util]))

(defn ui-component [[w h]]
  [:column {:gap 10 :margin 10}
   [:text {:content "CENTERED MODE" :font-size 24}]
   [:text {:content (str "Window: " w "x" h) :font-size 14}]
   [:text {:content "Should be CENTERED" :font-size 12}]
   [:button {:label "Click" :size [120 40] :on-click #(println "Clicked!")}]])

(defn -main [& args]
  (println "HARDCODED CENTERED MODE TEST")

  (hypr/create-backend!)

  (let [w 600
        h 400
        window (hypr/create-window
                {:title "CENTERED MODE"
                 :size [w h]
                 :on-close (fn [_] (util/exit-clean!))})]

    ;; HARDCODED centered (empty opts)
    (hypr/enable-responsive-root! window
      ui-component
      {})  ; HARDCODED empty opts = centered!

    (hypr/open-window! window)
    (hypr/enter-loop!)))
