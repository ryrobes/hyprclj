(ns todo-fixed
  "Fixed modern TODO - no emojis, clean layout."
  (:require [hyprclj.core :as hypr]
            [hyprclj.dsl :as dsl]
            [hyprclj.elements :as el]
            [hyprclj.input :as input]
            [hyprclj.keyed-reactive :refer [reactive-mount-keyed!]]
            [hyprclj.simple-reactive :refer [reactive-mount!]]
            [hyprclj.util :as util]))

;; State
(def todos (dsl/ratom []))
(def input-text (atom ""))
(def editing-id (atom nil))
(def edit-text (atom ""))
(def next-id (atom 0))

;; Actions
(defn start-edit! [id text]
  (reset! editing-id id)
  (reset! edit-text text)
  (println "Editing:" text))

(defn save-edit! []
  (when @editing-id
    (swap! todos (fn [ts]
                   (mapv (fn [t]
                           (if (= (:id t) @editing-id)
                             (assoc t :text @edit-text)
                             t))
                         ts)))
    (println "Saved:" @edit-text)
    (reset! editing-id nil)
    (reset! edit-text "")))

(defn cancel-edit! []
  (println "Cancelled")
  (reset! editing-id nil)
  (reset! edit-text ""))

(defn add-todo! []
  (when (seq @input-text)
    (swap! next-id inc)
    (swap! todos conj {:id @next-id :text @input-text :done false})
    (println "Added:" @input-text)
    (reset! input-text "")))

(defn remove-todo! [id]
  (let [todo (first (filter #(= (:id %) id) @todos))]
    (swap! todos (fn [ts] (vec (remove #(= (:id %) id) ts))))
    (println "Deleted:" (:text todo))))

(defn toggle-done! [id]
  (swap! todos (fn [ts]
                 (mapv (fn [t]
                         (if (= (:id t) id)
                           (update t :done not)
                           t))
                       ts))))

;; Separator helper
(defn make-separator [w]
  (dsl/compile-element
    [:rectangle {:color [0 0 0 0]
                 :border-color [120 150 180 80]
                 :border 1
                 :size [w 2]}]))

;; Main UI setup
(defn -main [& args]
  (println "\n=== Modern TODO App ===\n")

  (hypr/create-backend!)

  (let [w 650
        h 600
        window (hypr/create-window
                {:title "Modern TODO"
                 :size [w h]
                 :on-close (fn [_] (util/exit-clean!))})]

    ;; Keyboard handler
    (input/setup-keyboard-handler! window
      (fn [keycode pressed? utf8 _]
        (when pressed?
          (if @editing-id
            (cond
              (= keycode 65293) (save-edit!)
              (= keycode 65307) (cancel-edit!)
              (= keycode 65288) (swap! edit-text #(if (seq %) (subs % 0 (dec (count %))) %))
              (and (seq utf8) (not= utf8 "")) (swap! edit-text str utf8))
            (cond
              (= keycode 65293) (add-todo!)
              (= keycode 65288) (swap! input-text #(if (seq %) (subs % 0 (dec (count %))) %))
              (and (seq utf8) (not= utf8 "")) (swap! input-text str utf8))))))

    (let [root (hypr/root-element window)
          content-w (- w 60)
          item-w (- content-w 40)
          main-col (el/column-layout {:gap 15})]  ; Increased gap!

      [:rectangle {:gap 1
                   :size [800 800]
                   :background [50 100 50 150]    ; Green-ish
                   :border-color [100 255 100 255]
                   :border 5}

       ;; Position main column absolutely
       (el/set-position-mode! main-col 0)
       (el/set-absolute-position! main-col 30 30)

       ;; STATIC: Title
       (el/add-child! main-col
                      (dsl/compile-element [:text {:content "TODO App" :font-size 28}]))

       (el/add-child! main-col (make-separator content-w))

       ;; DYNAMIC: Stats and Input - use reactive containers WITHOUT backgrounds
       (let [stats-c (el/column-layout {:gap 3})]
         (el/add-child! main-col stats-c)
         (reactive-mount! stats-c [todos]
                          (fn []
                            [:h-box {:gap 2}
                             [:text {:content "Status:" :font-size 12}]
                             [:rectangle {:color [0 0 0 0] :size [10 1]}]  ; Spacer
                             [:text {:content (str (count @todos) " tasks (" (count (filter :done @todos)) " done)")
                                     :font-size 12}]])))

       (let [input-c (el/column-layout {:gap 3})]
         (el/add-child! main-col input-c)
         (reactive-mount! input-c [input-text]
                          (fn []
                            [:h-box {:gap 2}
                             [:text {:content "Add new task:" :font-size 12}]
                             [:rectangle {:color [0 0 0 0] :size [10 1]}]  ; Spacer
                             [:text {:content (if (seq @input-text)
                                                (str "> " @input-text " _")
                                                "(start typing)")
                                     :font-size 12}]])))

       (el/add-child! main-col (make-separator content-w))

       ;; DYNAMIC: Todo list (simple reactive for correct ordering!)
       (let [list-c (el/column-layout {:gap 6 :size [content-w 300]})]
         (el/add-child! main-col list-c)
         (reactive-mount! list-c [todos editing-id edit-text]  ; Use simple reactive, not keyed!
                          (fn []
                            (if (empty? @todos)
                              [:text {:content "No tasks yet!" :font-size 13}]
                              (into [:v-box {:gap 5}]
                                    ;; Sort by ID to maintain creation order (stable sorting)
                                    (for [todo (sort-by :id @todos)]
                                      (let [editing? (= @editing-id (:id todo))
                                            ;; Generate VIBRANT varied colors from ID
                                            id (:id todo)
                                            colors [[255 120 120 200]   ; Pastel Red
                                                    [120 255 120 200]   ; Pastel Green
                                                    [120 180 255 200]   ; Pastel Blue
                                                    [255 200 120 200]   ; Pastel Orange
                                                    [255 120 255 200]   ; Pastel Magenta
                                                    [120 255 255 200]]  ; Pastel Cyan
                                            accent-color (nth colors (mod id (count colors)))
                                            done? (:done todo)
                                            ;; Button text with visual indicator for done items
                                            display-text (if done?
                                                           (str "[DONE] " (:text todo))
                                                           (:text todo))
                                            ;; Button width - leave room for accent(12) + checkbox(30) + delete(45) + gaps(25)
                                            btn-w (- content-w 115)]
                                        ^{:key (:id todo)}
                                        [:h-box {:gap 6}
                                         ;; Wide colored accent bar
                                         [:rectangle {:color (if done?
                                                               [100 100 100 120]  ; Gray when done
                                                               accent-color)  ; Colored when active
                                                      :rounding 3
                                                      :size [12 32]}]
                                         [:checkbox {:checked done?
                                                     :on-change (fn [checked?]
                                                                  (println "Checkbox clicked! ID:" (:id todo) "Text:" (:text todo))
                                                                  (toggle-done! (:id todo)))}]
                                         (if editing?
                                           [:text {:content (str "Editing: " @edit-text " |") :font-size 13}]
                                           [:button {:label display-text
                                                     :size [btn-w 28]
                                                     :on-click (fn [] (start-edit! (:id todo) (:text todo)))}])
                                         (when-not editing?
                                           [:button {:label "X"
                                                     :size [45 28]
                                                     :on-click (fn [] (remove-todo! (:id todo)))}])])))))))

      ;(el/add-child! main-col (make-separator content-w))

       ;; STATIC: Instructions
       (let [instr-c (el/column-layout {:gap 4 :size [content-w 80]})]
        ;(el/add-child! main-col instr-c)

        ;(el/add-child! instr-c (dsl/compile-element [:text {:content "How to use:" :font-size 11}]))
        ;(el/add-child! instr-c (dsl/compile-element [:text {:content "- Type and press Enter to add task" :font-size 9}]))
        ;(el/add-child! instr-c (dsl/compile-element [:text {:content "- Click task to edit inline" :font-size 9}]))
        ;(el/add-child! instr-c (dsl/compile-element [:text {:content "- Check box to mark done" :font-size 9}]))
         )

      ;(el/add-child! main-col (make-separator content-w))

       ;; DEBUG: Show raw todos atom data (NO background - it clips content!)
       (let [debug-c (el/column-layout {:gap 3})]  ; Auto-size!
         (el/add-child! main-col debug-c)
         ;; Border separator above
         (el/add-child! debug-c (make-separator content-w))
         ;; Reactive debug text
         (reactive-mount! debug-c [todos]
                          (fn []
            ;[:rectangle {:color [255 255 255 0] :border 2 :size [600 300]}  ; Transparent background to avoid clipping
                            [:v-box {:gap 5 :size [600 100]
                      ;:background [50 100 50 150]    ; Green-ish
                      ;:border-color [100 255 100 255]
                      ;:color [0 0 0 255]
                      ;:border 2
             ;[:text {:content "DEBUG: Raw todos atom (updates live!):" :font-size 9}]
             ;[:text {:content (pr-str @todos) :font-size 7}]
                                     :children
                                     (vec
                                      (for [t @todos]
                                        [:text {:content (pr-str t) :font-size 7}]))}])))

       ;; Add main column to root
       (el/add-child! root main-col)])

    (hypr/open-window! window)
    (println "TODO app ready! Start typing to add tasks.")
    (hypr/enter-loop!)))

(comment (-main))
