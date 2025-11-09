(ns todo-edit
  "TODO app with inline editing!"
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
(def editing-id (atom nil))      ; ID of TODO being edited
(def edit-text (atom ""))        ; Text being edited
(def next-id (atom 0))

(defn start-edit! [id text]
  "Start editing a TODO item."
  (reset! editing-id id)
  (reset! edit-text text)
  (println "✏ Editing TODO #" id))

(defn save-edit! []
  "Save the edited TODO."
  (when @editing-id
    (swap! todos (fn [ts]
                   (mapv (fn [t]
                           (if (= (:id t) @editing-id)
                             (assoc t :text @edit-text)
                             t))
                         ts)))
    (println "💾 Saved edit for #" @editing-id)
    (reset! editing-id nil)
    (reset! edit-text "")))

(defn cancel-edit! []
  "Cancel editing."
  (println "❌ Cancelled edit")
  (reset! editing-id nil)
  (reset! edit-text ""))

(defn add-todo! []
  (when (seq @input-text)
    (swap! next-id inc)
    (swap! todos conj {:id @next-id
                       :text @input-text
                       :done false})
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
    (println (if (:done todo) "✓ Completed:" "○ Uncompleted:") (:text todo))))

(defn -main [& args]
  (println "=== TODO with Inline Editing ===")
  (println "Click task text to edit, Enter to save, Esc to cancel!")
  (println "")

  (hypr/create-backend!)

  (let [window (hypr/create-window
                {:title "TODO with Editing"
                 :size [700 800]
                 :on-close (fn [w]
                             (println "\nClosing...")
                             (util/exit-clean!))})]

    ;; Keyboard handler
    (input/setup-keyboard-handler! window
      (fn [keycode pressed? utf8 modifiers]
        (when pressed?
          (cond
            ;; If editing, handle edit keys
            @editing-id
            (cond
              (= keycode 65293) (save-edit!)         ; Enter - save
              (= keycode 65307) (cancel-edit!)       ; Esc - cancel
              (= keycode 65288) (swap! edit-text (fn [s] (if (seq s) (subs s 0 (dec (count s))) s)))  ; Backspace
              (and (seq utf8) (not= utf8 "")) (swap! edit-text str utf8))  ; Type

            ;; Not editing - add new TODO
            :else
            (cond
              (= keycode 65293) (add-todo!)
              (= keycode 65288) (swap! input-text (fn [s] (if (seq s) (subs s 0 (dec (count s))) s)))
              (and (seq utf8) (not= utf8 "")) (swap! input-text str utf8))))))

    (let [root (hypr/root-element window)
          main-col (el/column-layout {:gap 15 :margin 20})]

      (el/add-child! root main-col)

      ;; Header
      (el/add-child! main-col
        (dsl/compile-element
          [:text {:content "TODO with Inline Editing"
                  :font-size 26}]))

      ;; Stats
      (let [stats-container (el/column-layout {})]
        (el/add-child! main-col stats-container)
        (reactive-mount! stats-container [todos]
          (fn []
            [:text {:content (str "📋 " (count @todos) " tasks ("
                                (count (filter :done @todos)) " done)")
                    :font-size 14}])))

      ;; Mode indicator (reactive)
      (let [mode-container (el/column-layout {})]
        (el/add-child! main-col mode-container)
        (reactive-mount! mode-container [editing-id]
          (fn []
            (if @editing-id
              [:text {:content "✏ EDIT MODE - Enter to save, Esc to cancel"
                      :font-size 12}]
              [:text {:content "➕ ADD MODE - Type new task"
                      :font-size 12}]))))

      ;; Input section (reactive)
      (let [input-display (el/column-layout {})]
        (el/add-child! main-col input-display)
        (reactive-mount! input-display [input-text editing-id edit-text]
          (fn []
            (if @editing-id
              [:text {:content (str "✏ Editing: " @edit-text " _")
                      :font-size 16}]
              [:text {:content (if (seq @input-text)
                                (str "» " @input-text " _")
                                "(type to add task)")
                      :font-size 16}]))))

      ;; Separator
      (el/add-child! main-col
        (dsl/compile-element [:column {:size [-1 2] :margin [8 0]}]))

      ;; TODO list with inline editing!
      (let [list-container (el/column-layout {:gap 6})]
        (el/add-child! main-col list-container)

        (reactive-mount-keyed! list-container [todos editing-id edit-text]
          (fn []
            (if (empty? @todos)
              [:column {}
               [:text {:content "No tasks - type and press Enter!"
                       :font-size 14}]]
              (into [:column {:gap 5}]
                    (for [todo @todos]
                      (let [is-editing? (= @editing-id (:id todo))]
                        ^{:key (:id todo)}
                        [:row {:gap 10 :margin [3 0]}
                         ;; Checkbox
                         [:checkbox {:checked (:done todo)
                                     :on-change (fn [checked?]
                                                  (toggle-done! (:id todo)))}]

                         ;; Task text - clickable to edit!
                         [:button {:label (if is-editing?
                                           (str "✏ " @edit-text)
                                           (:text todo))
                                   :size [400 35]
                                   :on-click (fn []
                                               (when-not @editing-id
                                                 (start-edit! (:id todo) (:text todo))))}]

                         ;; Delete button
                         [:button {:label "×"
                                   :size [35 30]
                                   :on-click (fn []
                                               (remove-todo! (:id todo)))}]])))))))

      ;; Instructions
      (el/add-child! main-col
        (dsl/compile-element
          [:column {:gap 2 :margin [8 0]}
           [:text {:content "💡 How to use:" :font-size 12}]
           [:text {:content "• Type task, press Enter to add" :font-size 10}]
           [:text {:content "• Click task text to edit it" :font-size 10}]
           [:text {:content "• While editing: Enter saves, Esc cancels" :font-size 10}]
           [:text {:content "• Click checkbox to mark done" :font-size 10}]
           [:text {:content "• Click × to delete" :font-size 10}]]))

      ;; Quit
      (el/add-child! main-col
        (dsl/compile-element [util/make-quit-button {:size [150 40]}])))

    (println "TODO with inline editing mounted!")
    (println "Opening window...")

    (hypr/open-window! window)
    (println "Window opened!")
    (println "")
    (println "✨ NEW FEATURE: Inline Editing!")
    (println "  • Click any task text to edit it")
    (println "  • Type to change text")
    (println "  • Press Enter to save")
    (println "  • Press Esc to cancel")
    (println "")

    (hypr/enter-loop!)

    (println "\nTODO edit exited")))

(comment
  (-main)
  )
