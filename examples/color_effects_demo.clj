(ns color-effects-demo
  "Demonstration of color effects: fade/alpha and color manipulation."
  (:require [hyprclj.core :as core]
            [hyprclj.dsl :refer [mount!]]
            [hyprclj.color-fx :as fx]
            [hyprclj.util :as util]))

(defn ui-component []
  [:v-box {:gap 20 :margin 20}
   [:text {:content "Color Effects & Manipulation Demo" :font-size 24}]

   ;; Alpha/Fade Effects
   [:v-box {:gap 10}
    [:text {:content "Alpha/Fade Effects:" :font-size 16 :color [100 150 255 255]}]
    [:text {:content "100% Opacity (alpha=1.0)" :alpha 1.0 :font-size 14}]
    [:text {:content "75% Opacity (alpha=0.75)" :alpha 0.75 :font-size 14}]
    [:text {:content "50% Opacity (alpha=0.5)" :alpha 0.5 :font-size 14}]
    [:text {:content "25% Opacity (alpha=0.25)" :alpha 0.25 :font-size 14}]]

   ;; Rectangle with alpha
   [:v-box {:gap 10}
    [:text {:content "Rectangles with fade:" :font-size 16 :color [100 150 255 255]}]
    [:rectangle {:color [255 100 100 255] :alpha 1.0 :size [200 30]}]
    [:rectangle {:color [255 100 100 255] :alpha 0.7 :size [200 30]}]
    [:rectangle {:color [255 100 100 255] :alpha 0.4 :size [200 30]}]
    [:rectangle {:color [255 100 100 255] :alpha 0.1 :size [200 30]}]]

   ;; Color Manipulation - Brighten
   [:v-box {:gap 10}
    [:text {:content "Brighten (original → +20% → +40%):" :font-size 16 :color [100 150 255 255]}]
    (let [base-color [100 150 200 255]]
      [:h-box {:gap 5}
       [:rectangle {:color base-color :size [60 60] :rounding 5}]
       [:rectangle {:color (fx/brighten base-color 0.2) :size [60 60] :rounding 5}]
       [:rectangle {:color (fx/brighten base-color 0.4) :size [60 60] :rounding 5}]])]

   ;; Color Manipulation - Darken
   [:v-box {:gap 10}
    [:text {:content "Darken (original → -20% → -40%):" :font-size 16 :color [100 150 255 255]}]
    (let [base-color [200 100 100 255]]
      [:h-box {:gap 5}
       [:rectangle {:color base-color :size [60 60] :rounding 5}]
       [:rectangle {:color (fx/darken base-color 0.2) :size [60 60] :rounding 5}]
       [:rectangle {:color (fx/darken base-color 0.4) :size [60 60] :rounding 5}]])]

   ;; Color Mixing
   [:v-box {:gap 10}
    [:text {:content "Mix Red + Blue (0% → 50% → 100%):" :font-size 16 :color [100 150 255 255]}]
    (let [red [255 0 0 255]
          blue [0 0 255 255]]
      [:h-box {:gap 5}
       [:rectangle {:color (fx/mix red blue 0.0) :size [60 60] :rounding 5}]  ; All red
       [:rectangle {:color (fx/mix red blue 0.5) :size [60 60] :rounding 5}]  ; Purple
       [:rectangle {:color (fx/mix red blue 1.0) :size [60 60] :rounding 5}]])]  ; All blue

   ;; Grayscale
   [:v-box {:gap 10}
    [:text {:content "Grayscale Effect:" :font-size 16 :color [100 150 255 255]}]
    (let [colors [[255 100 50 255] [100 255 100 255] [100 100 255 255]]]
      [:h-box {:gap 5}
       [:v-box {:gap 5}
        [:text {:content "Original" :font-size 10}]
        (into [:h-box {:gap 3}]
              (for [c colors]
                [:rectangle {:color c :size [50 50] :rounding 3}]))]
       [:v-box {:gap 5}
        [:text {:content "Grayscale" :font-size 10}]
        (into [:h-box {:gap 3}]
              (for [c colors]
                [:rectangle {:color (fx/grayscale c) :size [50 50] :rounding 3}]))]])]

   ;; Invert Colors
   [:v-box {:gap 10}
    [:text {:content "Invert Colors:" :font-size 16 :color [100 150 255 255]}]
    (let [base [255 100 50 255]]
      [:h-box {:gap 5}
       [:v-box {:gap 3 :align :center}
        [:rectangle {:color base :size [60 60] :rounding 5}]
        [:text {:content "Original" :font-size 10}]]
       [:v-box {:gap 3 :align :center}
        [:rectangle {:color (fx/invert base) :size [60 60] :rounding 5}]
        [:text {:content "Inverted" :font-size 10}]]])]

   ;; Composite Effect - Faded + Brightened Text
   [:v-box {:gap 10}
    [:text {:content "Composite: Brightened + Faded:" :font-size 16 :color [100 150 255 255]}]
    (let [base-color [100 150 200 255]
          bright-color (fx/brighten base-color 0.3)]
      [:v-box {:gap 3}
       [:text {:content "Bright text at 100%" :color bright-color :alpha 1.0 :font-size 14}]
       [:text {:content "Bright text at 70%" :color bright-color :alpha 0.7 :font-size 14}]
       [:text {:content "Bright text at 40%" :color bright-color :alpha 0.4 :font-size 14}]])]])

(defn -main []
  (let [backend (core/create-backend!)
        window (core/create-window {:title "Color Effects Demo"
                                    :on-close (fn [_] (util/exit-clean!))
                                    :size [700 900]})
        root (core/root-element window)]

    (mount! root (ui-component))
    (core/open-window! window)
    (core/enter-loop!)))
