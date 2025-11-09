(ns vdom-todo-simple
  "Simplified VDOM TODO to debug crash."
  (:require [hyprclj.vdom :as vdom]
            [hyprclj.core :as hypr]
            [hyprclj.util :as util]))

;; Simple state
(def app-state (atom {:count 0}))

;; Minimal render
(defn render-app [state [w h]]
  [:v-box {:gap 15 :margin 20 :position :absolute}
   [:text {:content "Minimal VDOM TODO" :font-size 24}]
   [:text {:content (str "Count: " (:count state)) :font-size 16}]
   [:button {:label "+"
             :size [60 30]
             :on-click #(swap! app-state update :count inc)}]])

(defn -main [& args]
  (println "Starting...")

  (try
    (vdom/run-app!
      app-state
      render-app
      {:title "VDOM TODO Simple"
       :size [500 300]
       :on-close (fn [_] (util/exit-clean!))})
    (catch Exception e
      (println "Error:" (.getMessage e))
      (.printStackTrace e))))

(comment (-main))
