(ns best-practice-layout
  "Best-practice responsive layout with border separators."
  (:require [hyprclj.core :as hypr]
            [hyprclj.dsl :refer [mount!]]
            [hyprclj.util :as util]))

;; Helper: Horizontal separator line
(defn h-separator [w color]
  [:rectangle {:color [0 0 0 0]
               :border-color color
               :border 1
               :size [w 2]}])

;; Helper: Section with separator above
(defn section [w title & content]
  [:v-box {:gap 8}
   (h-separator w [255 255 255 80])  ; Subtle white separator
   [:text {:content title :font-size 14}]
   content])

(defn ui-component [[w h]]
  (let [timestamp (System/currentTimeMillis)
        content-w (- w 40)]

    [:v-box {:gap 12
             :margin 20
             :position :absolute
             :size [(- w 40) (- h 40)]}  ; Sized to window

     ;; Header
     [:text {:content "BEST-PRACTICE LAYOUT" :font-size 24}]
     [:text {:content (str "Window: " w "x" h " | TS: " timestamp) :font-size 11}]

     ;; Section 1: Toolbar with buttons
     (section content-w "Toolbar"
       [:h-box {:gap 8}
        [:button {:label "New" :size [70 28] :on-click #(println "New")}]
        [:button {:label "Open" :size [70 28] :on-click #(println "Open")}]
        [:button {:label "Save" :size [70 28] :on-click #(println "Save")}]
        [:button {:label "Export" :size [70 28] :on-click #(println "Export")}]])

     ;; Section 2: Content area with nested columns
     (section content-w "Content Area (Nested Columns)"
       [:h-box {:gap 15}
        ;; Column 1
        [:v-box {:gap 5}
         [:text {:content "Files" :font-size 12}]
         (h-separator 120 [100 100 255 150])  ; Blue separator
         [:text {:content "• doc1.txt" :font-size 10}]
         [:text {:content "• doc2.txt" :font-size 10}]
         [:text {:content "• doc3.txt" :font-size 10}]]

        ;; Column 2
        [:v-box {:gap 5}
         [:text {:content "Actions" :font-size 12}]
         (h-separator 120 [100 255 100 150])  ; Green separator
         [:button {:label "Edit" :size [80 25] :on-click #(println "Edit")}]
         [:button {:label "Delete" :size [80 25] :on-click #(println "Delete")}]
         [:button {:label "Share" :size [80 25] :on-click #(println "Share")}]]

        ;; Column 3
        [:v-box {:gap 5}
         [:text {:content "Info" :font-size 12}]
         (h-separator 120 [255 100 100 150])  ; Red separator
         [:text {:content "Size: 2.4 MB" :font-size 10}]
         [:text {:content "Type: Text" :font-size 10}]
         [:text {:content "Modified: Today" :font-size 10}]]])

     ;; Section 3: Settings form
     (section content-w "Settings"
       [:v-box {:gap 8}
        ;; Setting row 1
        [:h-box {:gap 10}
         [:text {:content "Theme:" :font-size 11}]
         [:button {:label "Light" :size [60 25] :on-click #(println "Light")}]
         [:button {:label "Dark" :size [60 25] :on-click #(println "Dark")}]]

        ;; Setting row 2
        [:h-box {:gap 10}
         [:text {:content "Size:" :font-size 11}]
         [:button {:label "Small" :size [60 25] :on-click #(println "Small")}]
         [:button {:label "Medium" :size [60 25] :on-click #(println "Medium")}]
         [:button {:label "Large" :size [60 25] :on-click #(println "Large")}]]])

     ;; Footer
     (section content-w "Footer"
       [:h-box {:gap 10}
        [:button {:label "Apply" :size [80 30] :on-click #(println "Apply")}]
        [:button {:label "Reset" :size [80 30] :on-click #(println "Reset")}]
        [:button {:label "Close" :size [80 30] :on-click #(println "Close")}]])

     [:text {:content "^ All separators resize | Fully composable! ^" :font-size 9}]]))

(defn -main [& args]
  (println "=== Best-Practice Layout ===")
  (println "Features:")
  (println "  ✓ Border separators between sections")
  (println "  ✓ Fully responsive (resize to test!)")
  (println "  ✓ Deep nesting (v-box > h-box > v-box)")
  (println "  ✓ Timestamp updates on resize")
  (println "  ✓ Composable helpers (section, h-separator)\n")

  (hypr/create-backend!)

  (let [w 750
        h 650
        window (hypr/create-window
                {:title "Best-Practice Layout"
                 :size [w h]
                 :on-close (fn [_] (util/exit-clean!))})]

    (hypr/enable-responsive-root! window
      ui-component
      {:position :absolute})

    (hypr/open-window! window)
    (hypr/enter-loop!)))

(comment
  (-main)
  )
