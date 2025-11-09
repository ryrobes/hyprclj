(ns layout-patterns
  "Demonstrate different layout patterns controlled by props."
  (:require [hyprclj.core :as hypr]
            [hyprclj.dsl :refer [mount!]]
            [hyprclj.util :as util]))

(defn ui-top-left
  "Top-left aligned, natural sizing"
  [[w h]]
  [:column {:gap 15
            :margin 10
            :align :left}  ; Column children aligned left
   [:text {:content "Pattern: Top-Left Aligned" :font-size 20}]
   [:text {:content (str "Window: " w "x" h) :font-size 14}]
   [:button {:label "Click" :size [120 40] :on-click #(println "Clicked!")}]])

(defn ui-centered
  "Centered both H and V"
  [[w h]]
  [:column {:gap 15
            :margin 0
            :align :center}  ; Center children horizontally IN the column
   [:text {:content "Pattern: Centered" :font-size 20}]
   [:text {:content (str "Window: " w "x" h) :font-size 14}]
   [:button {:label "Click" :size [120 40] :on-click #(println "Clicked!")}]])

(defn ui-fill-window
  "Content expands to fill window"
  [[w h]]
  [:column {:gap 15
            :margin 20}
   [:text {:content "Pattern: Fill Window" :font-size 20 :grow true}]
   [:text {:content (str "Window: " w "x" h) :font-size 14 :grow true}]
   [:text {:content "Text has :grow true" :font-size 12 :grow true}]
   [:button {:label "Click" :size [120 40] :on-click #(println "Clicked!")}]])

(defn -main [& args]
  (let [pattern (or (first args) "centered")]
    (println "Testing layout pattern:" pattern)

    (hypr/create-backend!)

    (let [w 700
          h 500
          window (hypr/create-window
                  {:title (str "Layout Pattern: " pattern)
                   :size [w h]
                   :on-close (fn [_] (util/exit-clean!))})]

      ;; Choose pattern
      (let [ui-fn (case pattern
                    "top-left" ui-top-left
                    "centered" ui-centered
                    "fill" ui-fill-window
                    ui-centered)]

        ;; Enable dynamic resize - will render on first resize event after open
        (hypr/enable-responsive-root! window ui-fn))

      (println "Opening window...")
      (hypr/open-window! window)
      (hypr/enter-loop!))))

(comment
  ;; Test different patterns:
  (-main "top-left")
  (-main "centered")
  (-main "fill")
  )
