(ns sizing-diagnostic
  "Diagnose how sizing actually works in Hyprtoolkit."
  (:require [hyprclj.core :as hypr]
            [hyprclj.elements :as elem]
            [hyprclj.util :as util]))

(defn -main [& args]
  (println "=== Sizing Diagnostic ===\n")

  (hypr/create-backend!)

  (let [w 700
        h 400
        window (hypr/create-window
                {:title "Sizing Diagnostic"
                 :size [w h]
                 :on-close (fn [_] (util/exit-clean!))})]

    (let [root (hypr/root-element window)]

      ;; Test 1: H-box with explicit size, containing two columns
      (let [test-row (elem/row-layout {:gap 20 :size [600 100]})]
        (elem/set-position-mode! test-row 0)
        (elem/set-absolute-position! test-row 50 50)

        ;; Column 1: With explicit size
        (let [col1 (elem/column-layout {:gap 5 :size [250 80]})]
          ;; Add background to visualize the column's bounds
          (elem/add-child! col1
            (elem/rectangle {:color [100 50 50 100]
                             :size [250 80]}))
          (elem/add-child! col1
            (elem/text {:content "Column 1: Sized [250x80]" :font-size 12}))
          (elem/add-child! col1
            (elem/text {:content "Has red background" :font-size 10}))
          (elem/add-child! test-row col1))

        ;; Column 2: With explicit size
        (let [col2 (elem/column-layout {:gap 5 :size [250 80]})]
          ;; Add background to visualize
          (elem/add-child! col2
            (elem/rectangle {:color [50 100 50 100]
                             :size [250 80]}))
          (elem/add-child! col2
            (elem/text {:content "Column 2: Sized [250x80]" :font-size 12}))
          (elem/add-child! col2
            (elem/text {:content "Has green background" :font-size 10}))
          (elem/add-child! test-row col2))

        (elem/add-child! root test-row))

      ;; Test 2: Text with explicit button sizes for reference
      (let [ref-text (elem/text {:content "Reference: Buttons have explicit sizes below" :font-size 12})]
        (elem/set-position-mode! ref-text 0)
        (elem/set-absolute-position! ref-text 50 180)
        (elem/add-child! root ref-text))

      (let [btn-row (elem/row-layout {:gap 10})]
        (elem/set-position-mode! btn-row 0)
        (elem/set-absolute-position! btn-row 50 210)
        (elem/add-child! btn-row (elem/button {:label "Button [100x30]" :size [100 30] :on-click #(println "1")}))
        (elem/add-child! btn-row (elem/button {:label "Button [150x30]" :size [150 30] :on-click #(println "2")}))
        (elem/add-child! btn-row (elem/button {:label "Button [200x30]" :size [200 30] :on-click #(println "3")}))
        (elem/add-child! root btn-row))

      ;; Test 3: Info
      (let [info (elem/text {:content "Question: Do the columns display their backgrounds fully?" :font-size 11})]
        (elem/set-position-mode! info 0)
        (elem/set-absolute-position! info 50 270)
        (elem/add-child! root info)))

    (hypr/open-window! window)
    (hypr/enter-loop!)))

(comment (-main))
