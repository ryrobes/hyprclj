(ns todo-smart
  "Smart TODO app with stable per-item components."
  (:require [hyprclj.core :as hypr]
            [hyprclj.dsl :refer [ratom compile-element]]
            [hyprclj.elements :as el]
            [hyprclj.input :as input]
            [hyprclj.simple-reactive :refer [reactive-mount!]]
            [hyprclj.util :as util]))

;; State
(def todos (ratom []))
(def input-text (atom ""))
(def next-id (atom 0))

;; Track UI elements for each todo
(def todo-elements (atom {}))  ; {todo-id -> {:row element :text element}}

(defn add-todo! []
  (when (seq @input-text)
    (swap! next-id inc)
    (swap! todos conj {:id @next-id
                       :text @input-text
                       :done false})
    (println "Added TODO:" @input-text)
    (reset! input-text "")))

(defn remove-todo! [id]
  (swap! todos (fn [ts] (vec (remove #(= (:id %) id) ts))))
  ;; Don't need to manually remove UI - list rebuild will handle it
  (println "Removed TODO #" id))

(defn toggle-done! [id]
  (swap! todos (fn [ts]
                 (mapv (fn [t]
                         (if (= (:id t) id)
                           (update t :done not)
                           t))
                       ts)))
  (let [todo (first (filter #(= (:id %) id) @todos))]
    (println (if (:done todo) "Completed:" "Uncompleted:") (:text todo))))

(defn make-todo-item
  "Create a single TODO item with stable buttons.
   Returns the row element."
  [todo]
  (let [row (el/row-layout {:gap 10 :margin [3 0]})
        text-container (el/column-layout {})]

    ;; Toggle button (static, stable)
    (el/add-child! row
      (compile-element
        [:button {:label (if (:done todo) "✓" "○")
                  :size [35 30]
                  :on-click (fn []
                              (println "Toggle button clicked for" (:id todo))
                              (toggle-done! (:id todo)))}]))

    ;; Text (will be reactive)
    (el/add-child! row text-container)

    ;; Make just the text reactive to todo changes
    (reactive-mount! text-container [todos]
      (fn []
        (if-let [current-todo (first (filter #(= (:id %) (:id todo)) @todos))]
          [:text {:content (:text current-todo)
                  :font-size 13}]
          [:text {:content "(deleted)" :font-size 13}])))

    ;; Delete button (static, stable)
    (el/add-child! row
      (compile-element
        [:button {:label "×"
                  :size [35 30]
                  :on-click (fn []
                              (println "Delete button clicked for" (:id todo))
                              (remove-todo! (:id todo)))}]))

    row))

(defn rebuild-todo-list!
  "Rebuild the entire TODO list when items are added/removed."
  [list-container]
  (println "Rebuilding TODO list...")
  (el/clear-children! list-container)

  (if (empty? @todos)
    (el/add-child! list-container
      (compile-element
        [:text {:content "No tasks - type and press Enter!"
                :font-size 14}]))
    (doseq [todo @todos]
      (let [item-row (make-todo-item todo)]
        (el/add-child! list-container item-row)))))

(defn -main [& args]
  (println "=== Smart TODO App ===")
  (println "Per-item stable buttons - no crashes!")
  (println "")

  (hypr/create-backend!)

  (let [window (hypr/create-window
                {:title "Smart TODO App"
                 :size [600 700]
                 :on-close (fn [w]
                             (println "\nClosing...")
                             (util/exit-clean!))})]

    (println "Window created")

    ;; Keyboard handler
    (input/setup-keyboard-handler! window
      (fn [keycode pressed? utf8 modifiers]
        (when pressed?
          (cond
            (= keycode 65293) (add-todo!)
            (= keycode 65288) (swap! input-text (fn [s] (if (seq s) (subs s 0 (dec (count s))) s)))
            (and (seq utf8) (not= utf8 "")) (swap! input-text str utf8)))))

    (let [root (hypr/root-element window)
          main-col (el/column-layout {:gap 15 :margin 20})
          list-container (el/column-layout {:gap 5})]

      (el/add-child! root main-col)

      ;; Header
      (el/add-child! main-col
        (compile-element
          [:text {:content "Smart TODO List"
                  :font-size 28}]))

      ;; Stats (reactive)
      (let [stats-container (el/column-layout {})]
        (el/add-child! main-col stats-container)
        (reactive-mount! stats-container [todos]
          (fn []
            [:text {:content (str "Total: " (count @todos) " | Done: " (count (filter :done @todos)))
                    :font-size 13}])))

      ;; Input display
      (el/add-child! main-col
        (compile-element [:text {:content "Add task:" :font-size 12}]))

      (let [input-display (el/column-layout {})]
        (el/add-child! main-col input-display)
        (reactive-mount! input-display [input-text]
          (fn []
            [:text {:content (if (seq @input-text)
                              (str "» " @input-text)
                              "(type and press Enter)")
                    :font-size 16}])))

      ;; Separator
      (el/add-child! main-col
        (compile-element [:column {:size [-1 2] :margin [5 0]}]))

      ;; List container
      (el/add-child! main-col list-container)

      ;; Initial list build
      (rebuild-todo-list! list-container)

      ;; Watch for add/remove (not toggle!)
      (add-watch todos ::list-rebuild
        (let [last-count (atom (count @todos))]
          (fn [_ _ old-todos new-todos]
            (let [old-count (count old-todos)
                  new-count (count new-todos)]
              ;; Only rebuild if count changed (add/delete), not if just toggled
              (when (not= old-count new-count)
                (hypr/add-idle!
                  (fn []
                    (rebuild-todo-list! list-container))))))))

      ;; Instructions
      (el/add-child! main-col
        (compile-element
          [:column {:gap 2 :margin [5 0]}
           [:text {:content "• Type task, press Enter to add" :font-size 11}]
           [:text {:content "• Click ○/✓ to toggle done" :font-size 11}]
           [:text {:content "• Click × to delete" :font-size 11}]
           [:text {:content "• Buttons are stable - no crashes!" :font-size 11}]]))

      ;; Quit
      (el/add-child! main-col
        (compile-element [util/make-quit-button {:size [150 40]}])))

    (println "Smart TODO UI mounted!")
    (println "Opening window...")

    (hypr/open-window! window)
    (println "Window opened!")
    (println "")
    (println "This version has stable per-item buttons!")
    (println "Toggle and delete should work without crashes.")
    (println "")

    (hypr/enter-loop!)

    (println "\nSmart TODO exited")))

(comment
  (-main)
  )
