(ns declarative-layout-test
  "Test declarative positioning via :position prop in hiccup."
  (:require [hyprclj.core :as hypr]
            [hyprclj.dsl :refer [mount!]]
            [hyprclj.util :as util]))

(defn ui-absolute [[w h]]
  "Layout with :position :absolute in the hiccup DSL"
  [:v-box {:gap 10
           :margin 10
           :position :absolute}  ; Declarative positioning!
   [:text {:content "DECLARATIVE ABSOLUTE" :font-size 24}]
   [:text {:content (str "Window: " w "x" h) :font-size 14}]
   [:text {:content "Position set via :position :absolute prop!" :font-size 12}]
   [:button {:label "Click" :size [120 40] :on-click #(println "Clicked!")}]])

(defn ui-centered [[w h]]
  "Layout without :position (defaults to centered)"
  [:v-box {:gap 10
           :margin 10}  ; No :position = centered
   [:text {:content "DECLARATIVE CENTERED" :font-size 24}]
   [:text {:content (str "Window: " w "x" h) :font-size 14}]
   [:text {:content "No :position prop = auto-layout (centered)" :font-size 12}]
   [:button {:label "Click" :size [120 40] :on-click #(println "Clicked!")}]])

(defn -main [& args]
  (println "=== Declarative Layout Test ===\n")

  (hypr/create-backend!)

  ;; Test absolute
  (let [w1 600 h1 300
        win1 (hypr/create-window {:title "Declarative ABSOLUTE" :size [w1 h1] :on-close (fn [_] (util/exit-clean!))})]
    (hypr/enable-responsive-root! win1 ui-absolute {:position :absolute})
    (hypr/open-window! win1))

  ;; Test centered
  (let [w2 600 h2 300
        win2 (hypr/create-window {:title "Declarative CENTERED" :size [w2 h2] :on-close (fn [_] (util/exit-clean!))})]
    (hypr/enable-responsive-root! win2 ui-centered {})
    (hypr/open-window! win2))

  (hypr/enter-loop!))

(comment
  (-main)
  )
