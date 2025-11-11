(ns fonts-demo
  "Font family demonstration."
  (:require [hyprclj.core :as core]
            [hyprclj.dsl :refer [mount!]]
            [hyprclj.util :as util]))

(defn ui-component []
  [:v-box {:gap 20 :margin 20}
   [:text {:content "Font Family Support" :font-size 24}]

   [:v-box {:gap 10}
    [:text {:content "Default font (no family specified):"
            :font-size 12
            :color "#666666"}]
    [:text {:content "The quick brown fox jumps over the lazy dog"
            :font-size 16}]]

   [:v-box {:gap 10}
    [:text {:content "Sans-serif font family:"
            :font-size 12
            :color "#666666"}]
    [:text {:content "The quick brown fox jumps over the lazy dog"
            :font-size 16
            :font-family "sans-serif"}]]

   [:v-box {:gap 10}
    [:text {:content "Serif font family:"
            :font-size 12
            :color "#666666"}]
    [:text {:content "The quick brown fox jumps over the lazy dog"
            :font-size 16
            :font-family "serif"}]]

   [:v-box {:gap 10}
    [:text {:content "Monospace font family:"
            :font-size 12
            :color "#666666"}]
    [:text {:content "The quick brown fox jumps over the lazy dog"
            :font-size 16
            :font-family "monospace"}]]

   ;; Try some common specific fonts
   [:v-box {:gap 10}
    [:text {:content "DejaVu Sans:"
            :font-size 12
            :color "#666666"}]
    [:text {:content "The quick brown fox jumps over the lazy dog"
            :font-size 16
            :font-family "DejaVu Sans"}]]

   [:v-box {:gap 10}
    [:text {:content "Liberation Sans:"
            :font-size 12
            :color "#666666"}]
    [:text {:content "The quick brown fox jumps over the lazy dog"
            :font-size 16
            :font-family "Liberation Sans"}]]

   [:v-box {:gap 10}
    [:text {:content "Ubuntu:"
            :font-size 12
            :color "#666666"}]
    [:text {:content "The quick brown fox jumps over the lazy dog"
            :font-size 16
            :font-family "Ubuntu"}]]

   ;; Different sizes with same font
   [:v-box {:gap 10}
    [:text {:content "Various sizes (monospace):"
            :font-size 12
            :color "#666666"}]
    [:text {:content "Size 10" :font-size 10 :font-family "monospace"}]
    [:text {:content "Size 14" :font-size 14 :font-family "monospace"}]
    [:text {:content "Size 18" :font-size 18 :font-family "monospace"}]
    [:text {:content "Size 24" :font-size 24 :font-family "monospace"}]
    [:text {:content "Size 32" :font-size 32 :font-family "monospace"}]]])

(defn -main []
  (let [backend (core/create-backend!)
        window (core/create-window {:title "Font Families Demo"
                                    :on-close (fn [_] (util/exit-clean!))
                                    :size [600 800]})
        root (core/root-element window)]

    (mount! root (ui-component))
    (core/open-window! window)
    (core/enter-loop!)))
