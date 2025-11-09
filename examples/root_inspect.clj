(ns root-inspect
  "Inspect root element properties."
  (:require [hyprclj.core :as hypr]
            [hyprclj.elements :as elem]
            [hyprclj.util :as util]))

(defn -main [& args]
  (println "Inspecting root element...")

  (hypr/create-backend!)

  (let [w 600
        h 400
        window (hypr/create-window
                {:title "Root Inspection"
                 :size [w h]
                 :on-close (fn [_] (util/exit-clean!))})]

    (let [root (hypr/root-element window)]
      (println "Root element:" root)
      (println "Root class:" (.getClass root))

      ;; Try to eliminate ALL spacing on root
      (elem/set-margin! root 0)
      (elem/set-position-mode! root 0)  ; Try absolute mode
      (elem/set-absolute-position! root 0 0)

      ;; Create a simple text element at the very edge
      (let [text1 (elem/text {:content "TOP-LEFT CORNER TEXT" :font-size 16})]
        ;; Set this text to absolute position too
        (elem/set-position-mode! text1 0)
        (elem/set-absolute-position! text1 0 0)
        (elem/set-margin! text1 0)
        (elem/add-child! root text1))

      ;; Add another text with offset
      (let [text2 (elem/text {:content "Offset (100, 100)" :font-size 16})]
        (elem/set-position-mode! text2 0)
        (elem/set-absolute-position! text2 100 100)
        (elem/set-margin! text2 0)
        (elem/add-child! root text2)))

    (hypr/open-window! window)
    (hypr/enter-loop!)))

(comment
  (-main)
  )
