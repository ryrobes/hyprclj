(ns responsive-complete
  "Complete responsive layout example with positioning control."
  (:require [hyprclj.core :as hypr]
            [hyprclj.dsl :refer [mount!]]
            [hyprclj.util :as util]))

(defn ui-absolute
  "Top-left positioned UI (natural size)"
  [[w h]]
  [:column {:gap 10 :margin 10}
   [:text {:content "Absolute Positioning" :font-size 24}]
   [:text {:content (str "Window: " w "x" h) :font-size 14}]
   [:text {:content "Positioned at (0, 0) top-left" :font-size 12}]
   [:text {:content "Natural size, not explicitly sized" :font-size 12}]
   [:button {:label "Click" :size [150 40] :on-click #(println "Clicked!")}]])

(defn ui-centered
  "Centered UI (explicit size)"
  [[w h]]
  [:column {:gap 10 :margin 10}
   [:text {:content "Centered Positioning" :font-size 24}]
   [:text {:content (str "Window: " w "x" h) :font-size 14}]
   [:text {:content "Auto-layout centered" :font-size 12}]
   [:text {:content "Explicitly sized to window dimensions" :font-size 12}]
   [:button {:label "Click" :size [150 40] :on-click #(println "Clicked!")}]])

(defn -main [& args]
  (let [mode (or (first args) "centered")
        use-absolute? (= mode "absolute")]
    (println "Mode:" mode "(absolute:" use-absolute? ")")

    (hypr/create-backend!)

    (let [w 700
          h 500
          window (hypr/create-window
                  {:title (str "Responsive: " mode)
                   :size [w h]
                   :on-close (fn [_] (util/exit-clean!))})]

      ;; Enable responsive with positioning option
      (hypr/enable-responsive-root! window
        (if use-absolute? ui-absolute ui-centered)
        (if use-absolute?
          {:position :absolute}  ; Top-left, natural size
          {}))                    ; Centered, explicit size

      (hypr/open-window! window)
      (hypr/enter-loop!))))

(comment
  (-main "centered")  ; Centered with explicit size
  (-main "absolute")  ; Top-left with natural size
  )
