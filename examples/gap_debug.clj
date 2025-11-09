(ns gap-debug
  "Debug the left gap issue."
  (:require [hyprclj.core :as hypr]
            [hyprclj.elements :as elem]
            [hyprclj.util :as util]))

(defn -main [& args]
  (println "Gap debug test...")

  (hypr/create-backend!)

  (let [window-width 600
        window-height 400
        window (hypr/create-window
                {:title "Gap Debug"
                 :size [window-width window-height]
                 :on-close (fn [w]
                             (util/exit-clean!))})]

    ;; Directly create and configure elements without mount!
    (let [root (hypr/root-element window)]
      (println "Root element:" root)

      ;; Create a simple text element - NO column wrapper
      (let [text1 (elem/text {:content "Line 1 - Direct child of root"
                              :font-size 20})]
        (elem/add-child! root text1))

      (let [text2 (elem/text {:content "Line 2 - Also direct child"
                              :font-size 20})]
        (elem/add-child! root text2))

      (let [text3 (elem/text {:content "Line 3 - Should be flush left?"
                              :font-size 20})]
        (elem/add-child! root text3)))

    (hypr/open-window! window)
    (hypr/enter-loop!)))

(comment
  (-main)
  )
