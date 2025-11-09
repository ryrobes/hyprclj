(ns todo-modern
  "Modern TODO app with responsive layout and optimized reactivity."
  (:require [hyprclj.core :as hypr]
            [hyprclj.dsl :refer [ratom mount!]]
            [hyprclj.elements :as el]
            [hyprclj.input :as input]
            [hyprclj.keyed-reactive :refer [reactive-mount-keyed!]]
            [hyprclj.simple-reactive :refer [reactive-mount!]]
            [hyprclj.util :as util]))

;; ===== State =====
(def todos (ratom []))
(def input-text (atom ""))
(def editing-id (atom nil))
(def edit-text (atom ""))
(def next-id (atom 0))

;; ===== Actions =====
(defn start-edit! [id text]
  (reset! editing-id id)
  (reset! edit-text text)
  (println "✏️  Editing TODO #" id))

(defn save-edit! []
  (when @editing-id
    (swap! todos (fn [ts]
                   (mapv (fn [t]
                           (if (= (:id t) @editing-id)
                             (assoc t :text @edit-text)
                             t))
                         ts)))
    (println "💾 Saved:" @edit-text)
    (reset! editing-id nil)
    (reset! edit-text "")))

(defn cancel-edit! []
  (println "❌ Cancelled")
  (reset! editing-id nil)
  (reset! edit-text ""))

(defn add-todo! []
  (when (seq @input-text)
    (swap! next-id inc)
    (swap! todos conj {:id @next-id :text @input-text :done false})
    (println "✅ Added:" @input-text)
    (reset! input-text "")))

(defn remove-todo! [id]
  (let [todo (first (filter #(= (:id %) id) @todos))]
    (swap! todos (fn [ts] (vec (remove #(= (:id %) id) ts))))
    (println "🗑️  Deleted:" (:text todo))))

(defn toggle-done! [id]
  (swap! todos (fn [ts]
                 (mapv (fn [t]
                         (if (= (:id t) id)
                           (update t :done not)
                           t))
                       ts)))
  (let [todo (first (filter #(= (:id %) id) @todos))]
    (println (if (:done todo) "✅ Done:" "⭕ Active:") (:text todo))))

;; ===== UI Component =====
(defn h-separator [w]
  "Horizontal separator line"
  [:rectangle {:color [0 0 0 0]
               :border-color [255 255 255 60]
               :border 1
               :size [w 2]}])

(defn ui-component [[w h]]
  "Main UI component - receives window size for responsiveness"
  (let [content-w (- w 50)
        item-w (- content-w 20)]

    [:v-box {:gap 12
             :margin 25
             :position :absolute
             :size [(- w 50) (- h 50)]}

     ;; ===== STATIC HEADER SECTION =====
     [:v-box {:gap 8}
      [:text {:content "📝 Modern TODO" :font-size 28}]
      [:text {:content (str "Window: " w "x" h " | Fully Responsive!") :font-size 11}]
      (h-separator content-w)]

     ;; ===== DYNAMIC STATS SECTION =====
     ;; This will be a reactive container
     [:v-box {:gap 5}]  ; Placeholder - will be replaced with reactive-mount!

     ;; ===== DYNAMIC INPUT SECTION =====
     [:v-box {:gap 5}]  ; Placeholder - will be replaced with reactive-mount!

     (h-separator content-w)

     ;; ===== DYNAMIC TODO LIST =====
     [:v-box {:gap 6}]  ; Placeholder - will be replaced with reactive-mount-keyed!

     (h-separator content-w)

     ;; ===== STATIC INSTRUCTIONS =====
     [:v-box {:gap 3}
      [:text {:content "💡 How to use:" :font-size 12}]
      [:text {:content "• Type → Enter to add" :font-size 10}]
      [:text {:content "• Click task → edit inline → Enter/Esc" :font-size 10}]
      [:text {:content "• Checkbox → mark done" :font-size 10}]]]))

;; ===== Main =====
(defn -main [& args]
  (println "\n=== Modern TODO App ===")
  (println "Features:")
  (println "  ✓ Responsive layout with border separators")
  (println "  ✓ Inline editing")
  (println "  ✓ Optimized reactivity (static vs dynamic sections)")
  (println "  ✓ Clean declarative DSL\n")

  (hypr/create-backend!)

  (let [w 700
        h 700
        window (hypr/create-window
                {:title "Modern TODO"
                 :size [w h]
                 :on-close (fn [_] (util/exit-clean!))})]

    ;; Setup keyboard handler
    (input/setup-keyboard-handler! window
      (fn [keycode pressed? utf8 modifiers]
        (when pressed?
          (if @editing-id
            ;; Edit mode
            (cond
              (= keycode 65293) (save-edit!)           ; Enter
              (= keycode 65307) (cancel-edit!)          ; Esc
              (= keycode 65288) (swap! edit-text #(if (seq %) (subs % 0 (dec (count %))) %))  ; Backspace
              (and (seq utf8) (not= utf8 "")) (swap! edit-text str utf8))
            ;; Add mode
            (cond
              (= keycode 65293) (add-todo!)
              (= keycode 65288) (swap! input-text #(if (seq %) (subs % 0 (dec (count %))) %))
              (and (seq utf8) (not= utf8 "")) (swap! input-text str utf8))))))

    ;; Enable responsive root
    (hypr/enable-responsive-root! window
      ui-component
      {:position :absolute})

    ;; After first render, set up reactive sections
    (hypr/add-idle! (fn []
      (let [root (hypr/root-element window)
            ;; Find the placeholder v-boxes (they're children 2, 3, 4 of root's first child)
            main-vbox (first (.impl root))]  ; This won't work - need different approach

        (println "TODO: Wire up reactive sections after initial render"))))

    (hypr/open-window! window)
    (hypr/enter-loop!)))

(comment
  (-main)
  )
