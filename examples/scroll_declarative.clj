(ns scroll-declarative
  "Scrollable area using declarative hiccup DSL."
  (:require [hyprclj.core :as hypr]
            [hyprclj.dsl :refer [mount!]]
            [hyprclj.util :as util]))

(defn ui-component [[w h]]
  [:v-box {:gap 15
           :margin 20
           :position :absolute}

   [:text {:content "Declarative ScrollArea" :font-size 24}]
   [:text {:content "Use mouse wheel to scroll!" :font-size 12}]

   ;; Scrollable area with declarative syntax!
   [:scrollable {:scroll-y true
                 :size [450 300]}
    [:v-box {:gap 8
             :children (for [i (range 50)]
                         [:h-box {:gap 10}
                          [:text {:content (str "Item " i) :font-size 13}]
                          [:button {:label "Click"
                                    :size [60 25]
                                    :on-click #(println "Clicked item" i)}]])}]]

   [:text {:content (str "50 items in scroll area | Window: " w "x" h) :font-size 10}]])

(defn -main [& args]
  (println "=== Declarative ScrollArea ===\n")

  (hypr/create-backend!)

  (let [w 550
        h 500
        window (hypr/create-window {:title "Declarative Scroll"
                                     :size [w h]
                                     :on-close (fn [_] (util/exit-clean!))})]

    ;; Use enable-responsive-root for responsive layout!
    (hypr/enable-responsive-root! window
      ui-component
      {:position :absolute})

    (hypr/open-window! window)
    (hypr/enter-loop!)))

(comment (-main))
