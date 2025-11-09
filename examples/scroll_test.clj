(ns scroll-test
  "Test ScrollArea element - scrollable long list."
  (:require [hyprclj.core :as hypr]
            [hyprclj.elements :as elem]
            [hyprclj.util :as util])
  (:import [org.hyprclj.bindings ScrollArea]))

(defn -main [& args]
  (println "=== ScrollArea Test ===")

  (hypr/create-backend!)

  (let [w 500
        h 400
        window (hypr/create-window {:title "Scroll Test"
                                     :size [w h]
                                     :on-close (fn [_] (util/exit-clean!))})]

    (let [root (hypr/root-element window)]

      ;; Create scroll area
      (let [scroll (-> (ScrollArea/builder)
                      (.scrollX false)
                      (.scrollY true)
                      (.size 450 350)
                      (.build))]

        ;; Position it
        (elem/set-position-mode! scroll 0)
        (elem/set-absolute-position! scroll 25 25)

        ;; Add content to scroll area (long list!)
        (let [content-col (elem/column-layout {:gap 8})]
          ;; Add many items to make it scrollable
          (doseq [i (range 50)]
            (elem/add-child! content-col
              (elem/text {:content (str "Item " i " - This is a scrollable list!")
                          :font-size 13})))

          ;; Add content column to scroll area
          (elem/add-child! scroll content-col))

        ;; Add scroll area to root
        (elem/add-child! root scroll)))

    (hypr/open-window! window)
    (println "Scroll area created! Use mouse wheel to scroll.")
    (hypr/enter-loop!)))

(comment (-main))
