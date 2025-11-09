(ns nested-with-borders
  "Nested layouts with visual borders for debugging structure."
  (:require [hyprclj.core :as hypr]
            [hyprclj.elements :as elem]
            [hyprclj.util :as util]))

(defn bordered-v-box [props & children]
  "V-box with visible border for debugging"
  (let [{:keys [gap margin border-color]
         :or {gap 10 margin 5 border-color [255 0 0 255]}} props
        column (elem/column-layout (select-keys props [:gap :size]))]

    ;; Add border rectangle as first child
    (elem/add-child! column
      (elem/rectangle {:color [0 0 0 0]  ; Transparent
                       :border-color border-color
                       :border 2
                       :size (:size props)
                       :grow true}))

    ;; Add children
    (doseq [child children]
      (when child
        (elem/add-child! column child)))

    ;; Apply positioning
    (when (= (:position props) :absolute)
      (elem/set-position-mode! column 0)
      (elem/set-absolute-position! column 0 0))
    (when margin
      (elem/set-margin! column margin))

    column))

(defn bordered-h-box [props & children]
  "H-box with visible border for debugging"
  (let [{:keys [gap margin border-color]
         :or {gap 5 margin 5 border-color [0 255 0 255]}} props
        row (elem/row-layout (select-keys props [:gap :size]))]

    ;; Add border rectangle
    (elem/add-child! row
      (elem/rectangle {:color [0 0 0 0]
                       :border-color border-color
                       :border 2
                       :size (:size props)
                       :grow true}))

    ;; Add children
    (doseq [child children]
      (when child
        (elem/add-child! row child)))

    (when margin
      (elem/set-margin! row margin))

    row))

(defn -main [& args]
  (println "=== Nested Layouts with Debug Borders ===")

  (hypr/create-backend!)

  (let [w 700
        h 550
        window (hypr/create-window
                {:title "Nested with Borders"
                 :size [w h]
                 :on-close (fn [_] (util/exit-clean!))})]

    (let [root (hypr/root-element window)]

      ;; Root v-box (RED border) - absolute positioned
      (let [main-vbox (bordered-v-box {:gap 15 :margin 10 :position :absolute
                                       :border-color [255 0 0 255]}

                        ;; Header
                        (elem/text {:content "NESTED LAYOUT DEBUG" :font-size 24})

                        ;; Nested h-box (GREEN border)
                        (bordered-h-box {:gap 10 :border-color [0 255 0 255]}
                          (elem/button {:label "Btn 1" :size [80 30] :on-click #(println "1")})
                          (elem/button {:label "Btn 2" :size [80 30] :on-click #(println "2")})
                          (elem/button {:label "Btn 3" :size [80 30] :on-click #(println "3")}))

                        ;; Another nested v-box (BLUE border)
                        (bordered-v-box {:gap 5 :margin 10 :border-color [0 100 255 255]}
                          (elem/text {:content "Nested V-box (blue)" :font-size 14})
                          (elem/text {:content "- Item 1" :font-size 12})
                          (elem/text {:content "- Item 2" :font-size 12})))]

        (elem/add-child! root main-vbox)))

    (hypr/open-window! window)
    (hypr/enter-loop!)))

(comment
  (-main)
  )
