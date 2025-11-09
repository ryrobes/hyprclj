(ns todo-full
  "Fully functional TODO app with real text input!"
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

(defn -main [& args]
  (println "=== Fully Functional TODO App ===")
  (println "With real text input and keyboard support!")
  (println "")

  ;; Create backend
  (hypr/create-backend!)

  ;; Create window
  (let [window (hypr/create-window
                {:title "TODO App"
                 :size [600 700]
                 :on-close (fn [w]
                             (println "\nClosing TODO app...")
                             (util/exit-clean!))})]

    (println "Window created")

    ;; Set up keyboard handler for text input
    (println "Setting up keyboard handler...")
    (input/setup-keyboard-handler! window
      (fn [keycode pressed? utf8 modifiers]
        (when pressed?
          (cond
            ;; Enter - submit todo
            (= keycode 65293)
            (add-todo!)

            ;; Backspace
            (= keycode 65288)
            (swap! input-text (fn [s]
                                (if (seq s)
                                  (subs s 0 (dec (count s)))
                                  s)))

            ;; Regular printable character
            (and (seq utf8) (not= utf8 ""))
            (swap! input-text str utf8)))))

    (let [root (hypr/root-element window)
          main-col (el/column-layout {:gap 15 :margin 20})]

      (el/add-child! root main-col)

      ;; Header
      (el/add-child! main-col
        (compile-element
          [:text {:content "TODO List"
                  :font-size 28}]))

      ;; Stats (reactive)
      (let [stats-container (el/row-layout {:gap 10})]
        (el/add-child! main-col stats-container)

        (reactive-mount! stats-container [todos]
          (fn []
            [:text {:content (str "Total: " (count @todos) " | "
                                "Done: " (count (filter :done @todos)) " | "
                                "Remaining: " (count (remove :done @todos)))
                    :font-size 13}])))

      ;; Separator
      (el/add-child! main-col
        (compile-element
          [:column {:size [-1 2] :margin [5 0]}]))

      ;; Input section label
      (el/add-child! main-col
        (compile-element
          [:text {:content "Add New Task:"
                  :font-size 14}]))

      ;; Input field (reactive - shows what you're typing)
      (let [input-container (el/column-layout {:gap 5})]
        (el/add-child! main-col input-container)

        (reactive-mount! input-container [input-text]
          (fn []
            [:column {:gap 3}
             ;; Show current input
             [:text {:content (if (seq @input-text)
                               (str "» " @input-text)
                               "(Type your task and press Enter)")
                     :font-size 16}]
             ;; Visual textbox (click to \"focus\")
             [:textbox {:placeholder "Type task here, press Enter to add..."
                        :size [500 40]}]])))

      (el/add-child! main-col
        (compile-element
          [:text {:content "↑ Type anywhere and press Enter to add"
                  :font-size 11}]))

      ;; Separator
      (el/add-child! main-col
        (compile-element
          [:column {:size [-1 2] :margin [10 0]}]))

      ;; TODO list (reactive)
      (let [list-container (el/column-layout {:gap 5})]
        (el/add-child! main-col list-container)

        (reactive-mount! list-container [todos]
          (fn []
            (if (empty? @todos)
              [:text {:content "No tasks yet - start typing and press Enter!"
                      :font-size 14}]
              (into [:column {:gap 4}]
                    (for [todo @todos]
                      [:row {:gap 10 :margin [3 0]}
                       ;; Done/Undone button
                       [:button {:label (if (:done todo) "✓" "○")
                                 :size [40 35]
                                 :on-click #(toggle-done! (:id todo))}]

                       ;; Todo text (strikethrough effect via font for done items)
                       [:text {:content (:text todo)
                               :font-size 14}]

                       ;; Delete button
                       [:button {:label "×"
                                 :size [40 35]
                                 :on-click #(remove-todo! (:id todo))}]]))))))

      ;; Instructions
      (el/add-child! main-col
        (compile-element
          [:column {:gap 2 :margin [10 0]}
           [:text {:content "Instructions:"
                   :font-size 12}]
           [:text {:content "• Type task and press Enter to add"
                   :font-size 11}]
           [:text {:content "• Click ○ to mark done (becomes ✓)"
                   :font-size 11}]
           [:text {:content "• Click × to delete task"
                   :font-size 11}]
           [:text {:content "• Click Quit to exit"
                   :font-size 11}]]))

      ;; Quit button
      (el/add-child! main-col
        (compile-element
          [util/make-quit-button {:size [150 40]}])))

    (println "TODO app UI mounted!")
    (println "Opening window...")

    ;; Open and run
    (hypr/open-window! window)
    (println "Window opened!")
    (println "")
    (println "Ready to use:")
    (println "  1. Type your task")
    (println "  2. Press Enter to add it")
    (println "  3. Click ○/✓ to toggle done")
    (println "  4. Click × to delete")
    (println "")

    (hypr/enter-loop!)

    (println "\nTODO app exited")))

(comment
  (-main)
  )
