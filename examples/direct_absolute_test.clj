(ns direct-absolute-test
  "Direct test using mount! with :position :absolute option."
  (:require [hyprclj.core :as hypr]
            [hyprclj.dsl :refer [mount!]]
            [hyprclj.util :as util]))

(defn -main [& args]
  (let [mode (or (first args) "centered")]
    (println "Direct test - mode:" mode)

    (hypr/create-backend!)

    (let [w 600
          h 400
          window (hypr/create-window
                  {:title (str "Direct Test: " mode)
                   :size [w h]
                   :on-close (fn [_] (util/exit-clean!))})]

      ;; Directly mount with positioning option
      (let [root (hypr/root-element window)
            opts (if (= mode "absolute")
                   {:position :absolute}
                   {})]

        (println "Mounting with opts:" opts)

        (mount! root
                [:column {:gap 10 :margin 10}
                 [:text {:content (str "MODE: " mode) :font-size 24}]
                 [:text {:content "Check console for [mount!] debug" :font-size 14}]
                 [:button {:label "Click" :size [120 40] :on-click #(println "Clicked!")}]]
                [w h]
                opts))  ; Pass opts here!

      (println "Opening window...")
      (hypr/open-window! window)
      (hypr/enter-loop!))))

(comment
  (-main "centered")
  (-main "absolute")
  )
