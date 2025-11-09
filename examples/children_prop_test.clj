(ns children-prop-test
  "Test :children prop syntax for programmatic UI generation."
  (:require [hyprclj.core :as hypr]
            [hyprclj.dsl :refer [mount!]]
            [hyprclj.util :as util]))

(defn ui-component [[w h]]
  (let [items ["Apple" "Banana" "Cherry" "Date"]
        ;; Generate children programmatically
        item-children (for [item items]
                        [:text {:content (str "- " item) :font-size 12}])]

    [:v-box {:gap 15
             :margin 20
             :position :absolute}

     [:text {:content ":children PROP TEST" :font-size 24}]

     ;; OLD style: using (into [:v-box {}] ...)
     [:v-box {:gap 5}
      [:text {:content "OLD Style (into [:v-box {}] ...):" :font-size 12}]
      (into [:v-box {:gap 3}]
            (for [item items]
              [:text {:content (str "• " item) :font-size 11}]))]

     ;; NEW style: using :children prop!
     [:v-box {:gap 5}
      [:text {:content "NEW Style {:children [...]}:" :font-size 12}]
      [:v-box {:gap 3
               :children item-children}]]  ; Pass as prop!

     ;; Another example with h-box
     [:v-box {:gap 5}
      [:text {:content "H-box with :children:" :font-size 12}]
      [:h-box {:gap 8
               :children (for [item items]
                           [:button {:label item
                                     :size [80 28]
                                     :on-click #(println "Clicked:" item)}])}]]

     [:text {:content "Both OLD and NEW styles render the same!" :font-size 10}]]))

(defn -main [& args]
  (println "\n=== :children Prop Test ===")
  (println "Shows two syntax styles for programmatic UI:\n")
  (println "OLD: (into [:v-box {}] (for ...))")
  (println "NEW: [:v-box {:children (for ...)}]\n")

  (hypr/create-backend!)

  (let [w 600
        h 500
        window (hypr/create-window
                {:title ":children Prop Test"
                 :size [w h]
                 :on-close (fn [_] (util/exit-clean!))})]

    (hypr/enable-responsive-root! window
      ui-component
      {:position :absolute})

    (hypr/open-window! window)
    (hypr/enter-loop!)))

(comment (-main))
