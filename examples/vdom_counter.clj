(ns vdom-counter "Virtual DOM counter - pure data-driven UI"
  (:require [hyprclj.vdom :as vdom]
            [hyprclj.util :as util]))

;; ===== PURE DATA =====
(def app-state (atom {:count 0}))

;; ===== PURE RENDER FUNCTION =====
(defn render-app "Pure function: state + window size → hiccup"
  [state [w h]]
  [:v-box {:gap 15
           :margin 20
           :position :absolute}
   [:text {:content "VDOM Counter Demo" :font-size 24}]
   [:text {:content (str "Window: " w "x" h " | Pure data-driven!") :font-size 11}]
   [:rectangle {:color [0 0 0 0]
                :border-color [100 150 200 80]
                :border 1
                :size [(- w 50) 2]}]
   [:text {:content (str "Count: " (:count state)) :font-size 48}]
   [:h-box {:gap 10}
    [:button {:label "-"
              :size [60 40]
              :on-click (fn [] (swap! app-state update :count dec))}]  ; Just swap! app-state!
    [:button {:label "Reset"
              :size [80 40]
              :on-click (fn [] (swap! app-state assoc :count 0))}]
    [:button {:label "+"
              :size [60 40]
              :on-click (fn [] (swap! app-state update :count inc))}]]
   [:text {:content "Click buttons → app-state updates → UI auto-reconciles!" :font-size 10}]])

;; ===== MAIN =====
(defn -main [& args]
  (vdom/run-app!
    app-state
    render-app
    {:title "VDOM Counter"
     :size [500 400]
     :on-close (fn [_] (util/exit-clean!))}))

(comment (-main))
