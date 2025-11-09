(ns todo-inline
  "TODO with TRUE inline editing - edit happens in place!"
  (:require [hyprclj.core :as hypr]
            [hyprclj.dsl :refer [ratom]]
            [hyprclj.elements :as el]
            [hyprclj.input :as input]
            [hyprclj.keyed-reactive :refer [reactive-mount-keyed!]]
            [hyprclj.simple-reactive :refer [reactive-mount!]]
            [hyprclj.util :as util]
            [hyprclj.dsl :as dsl]))

;; State
(def todos (ratom []))
(def input-text (atom ""))
(def editing-id (atom nil))
(def edit-text (atom ""))
(def next-id (atom 0))

(defn start-edit! [id text]
  (reset! editing-id id)
  (reset! edit-text text)
  (println "✏ Editing TODO #" id))

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
    (println "✓ Added:" @input-text)
    (reset! input-text "")))

(defn remove-todo! [id]
  (let [todo (first (filter #(= (:id %) id) @todos))]
    (swap! todos (fn [ts] (vec (remove #(= (:id %) id) ts))))
    (println "× Deleted:" (:text todo))))

(defn toggle-done! [id]
  (swap! todos (fn [ts]
                 (mapv (fn [t]
                         (if (= (:id t) id)
                           (update t :done not)
                           t))
                       ts)))
  (let [todo (first (filter #(= (:id %) id) @todos))]
    (println (if (:done todo) "✓ Done:" "○ Active:") (:text todo))))

(defn -main [& args]
  (println "=== TODO with TRUE Inline Editing ===")
  (println "Edit happens right in the list!")
  (println "")

  (hypr/create-backend!)

  (let [window (hypr/create-window
                {:title "Inline Edit TODO"
                 :size [700 800]
                 :on-close (fn [w] (util/exit-clean!))})]

    ;; Keyboard: route to edit-text if editing, else input-text
    (input/setup-keyboard-handler! window
                                   (fn [keycode pressed? utf8 modifiers]
                                     (when pressed?
                                       (if @editing-id
            ;; EDIT MODE
                                         (cond
                                           (= keycode 65293) (save-edit!)
                                           (= keycode 65307) (cancel-edit!)
                                           (= keycode 65288) (swap! edit-text (fn [s] (if (seq s) (subs s 0 (dec (count s))) s)))
                                           (and (seq utf8) (not= utf8 "")) (swap! edit-text str utf8))
            ;; ADD MODE
                                         (cond
                                           (= keycode 65293) (add-todo!)
                                           (= keycode 65288) (swap! input-text (fn [s] (if (seq s) (subs s 0 (dec (count s))) s)))
                                           (and (seq utf8) (not= utf8 "")) (swap! input-text str utf8))))))

    (let [root (hypr/root-element window)]

      ;; Use DSL to mount a growing column that fills the window!
      (dsl/mount! root
                  [:column {:gap 15 :margin 20 :grow true}  ; ← :grow true fills window!

      ;; Header
                   (el/add-child! main-col
                                  (dsl/compile-element [:text {:content "Inline Edit TODO" :font-size 26}]))

      ;; Stats
                   (let [stats-container (el/column-layout {})]
                     (el/add-child! main-col stats-container)
                     (reactive-mount! stats-container [todos editing-id]
                                      (fn []
                                        (if @editing-id
                                          [:text {:content (str "✏ Editing mode | " (count @todos) " total")
                                                  :font-size 13}]
                                          [:text {:content (str "📋 " (count @todos) " tasks (" (count (filter :done @todos)) " done)")
                                                  :font-size 13}]))))

      ;; Add input (only show when NOT editing)
                   (let [add-container (el/column-layout {:gap 3})]
                     (el/add-child! main-col add-container)
                     (reactive-mount! add-container [input-text editing-id]
                                      (fn []
                                        (if @editing-id
                                          [:text {:content "(editing below - Enter to save, Esc to cancel)"
                                                  :font-size 11}]
                                          [:column {:gap 2}
                                           [:text {:content "New task:" :font-size 11}]
                                           [:text {:content (if (seq @input-text)
                                                              (str "» " @input-text " _")
                                                              "(type here)")
                                                   :font-size 15}]]))))

      ;; Separator
                   (el/add-child! main-col
                                  (dsl/compile-element [:column {:size [-1 2] :margin [5 0]}]))

      ;; TODO list with INLINE editing!
                   (let [list-container (el/column-layout {:gap 6})]
                     (el/add-child! main-col list-container)

                     (reactive-mount-keyed! list-container [todos editing-id edit-text]
                                            (fn []
                                              (if (empty? @todos)
                                                [:column {} [:text {:content "No tasks!" :font-size 14}]]
                                                (into [:column {:gap 5}]
                                                      (for [todo @todos]
                                                        (let [is-editing? (= @editing-id (:id todo))]
                                                          ^{:key (:id todo)}
                                                          [:row {:gap 10 :margin [3 0]}
                         ;; Checkbox
                                                           [:checkbox {:checked (:done todo)
                                                                       :on-change (fn [_] (toggle-done! (:id todo)))}]

                         ;; Text OR Edit field - RIGHT HERE!
                                                           (if is-editing?
                           ;; EDITING: Show edit text with cursor
                                                             [:text {:content (str "✏ " @edit-text " |")
                                                                     :font-size 14}]
                           ;; NORMAL: Show task text (clickable)
                                                             [:button {:label (:text todo)
                                                                       :size [450 30]
                                                                       :on-click (fn []
                                                                                   (start-edit! (:id todo) (:text todo)))}])

                         ;; Delete (disabled while editing this item)
                                                           (if is-editing?
                                                             [:text {:content " " :font-size 14}]  ; Spacer
                                                             [:button {:label "×"
                                                                       :size [35 30]
                                                                       :on-click (fn []
                                                                                   (remove-todo! (:id todo)))}])])))))))

      ;; Instructions
                   (el/add-child! main-col
                                  (dsl/compile-element
                                   [:column {:gap 2 :margin [8 0]}
                                    [:text {:content "💡 Instructions:" :font-size 12}]
                                    [:text {:content "• Type → Enter to add task" :font-size 10}]
                                    [:text {:content "• Click task → edit IN PLACE" :font-size 10}]
                                    [:text {:content "• Enter → save | Esc → cancel" :font-size 10}]
                                    [:text {:content "• Checkbox → toggle done" :font-size 10}]]))

      ;; Quit
                   (el/add-child! main-col
                                  (dsl/compile-element [util/make-quit-button {:size [150 40]}]))])

      (println "Inline editing TODO mounted!")
      (hypr/open-window! window)
      (println "Window opened - try inline editing!")
      (println "")

      (hypr/enter-loop!))))

(comment (-main))
