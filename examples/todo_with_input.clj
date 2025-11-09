(ns todo-with-input
  "TODO app with text input field."
  (:require [hyprclj.core :as hypr]
            [hyprclj.dsl :refer [ratom compile-element]]
            [hyprclj.elements :as el]
            [hyprclj.simple-reactive :refer [reactive-mount!]]
            [hyprclj.util :as util]))

;; State
(def todos (ratom []))
(def input-text (ratom ""))
(def next-id (atom 0))

(defn add-todo! [text]
  (when (seq text)
    (swap! next-id inc)
    (swap! todos conj {:id @next-id :text text :done false})
    (reset! input-text "")
    (println "Added:" text)))

(defn remove-todo! [id]
  (swap! todos (fn [ts] (vec (remove #(= (:id %) id) ts))))
  (println "Removed todo #" id))

(defn toggle-done! [id]
  (swap! todos (fn [ts]
                 (mapv (fn [t]
                         (if (= (:id t) id)
                           (update t :done not)
                           t))
                       ts)))
  (println "Toggled todo #" id))

(defn -main [& args]
  (println "=== TODO App with Input ===")
  (println "Features text input field (visual only for POC)")
  (println "")

  ;; Create backend
  (hypr/create-backend!)

  ;; Create window
  (let [window (hypr/create-window
                {:title "TODO App"
                 :size [550 650]
                 :on-close (fn [w]
                             (println "\nClosing...")
                             (util/exit-clean!))})]

    (println "Window created")

    (let [root (hypr/root-element window)
          main-col (el/column-layout {:gap 15 :margin 20})]

      (el/add-child! root main-col)

      ;; Header
      (el/add-child! main-col
        (compile-element
          [:text {:content "TODO List with Input"
                  :font-size 26}]))

      ;; Counter (reactive)
      (let [counter-container (el/row-layout {:gap 10})]
        (el/add-child! main-col counter-container)

        (reactive-mount! counter-container [todos]
          (fn []
            [:text {:content (str "Total: " (count @todos) " | "
                                "Done: " (count (filter :done @todos)))
                    :font-size 14}])))

      ;; Input section
      (el/add-child! main-col
        (compile-element
          [:column {:gap 5}]))

      (el/add-child! main-col
        (compile-element
          [:text {:content "Enter new task:"
                  :font-size 12}]))

      ;; Textbox + Add button row
      (let [input-row (el/row-layout {:gap 10})]
        (el/add-child! main-col input-row)

        ;; Textbox - renders but input events not fully wired yet
        (el/add-child! input-row
          (compile-element
            [:textbox {:placeholder "Type task here..."
                       :size [350 40]}]))

        ;; For POC: Use predefined tasks instead
        (el/add-child! input-row
          (compile-element
            [:button {:label "Add Task"
                      :size [120 40]
                      :on-click (fn []
                                  (let [tasks ["Buy groceries"
                                              "Write code"
                                              "Review PRs"
                                              "Fix bugs"
                                              "Deploy app"]]
                                    (add-todo! (rand-nth tasks))))}])))

      (el/add-child! main-col
        (compile-element
          [:text {:content "(Textbox shows but input events need wiring - clicking Add adds random tasks)"
                  :font-size 10}]))

      ;; Separator
      (el/add-child! main-col
        (compile-element
          [:column {:size [-1 2] :margin [5 0]}]))

      ;; TODO list (reactive)
      (let [list-container (el/column-layout {:gap 5})]
        (el/add-child! main-col list-container)

        (reactive-mount! list-container [todos]
          (fn []
            (if (empty? @todos)
              [:text {:content "No todos - click 'Add Task' to create one!"
                      :font-size 12}]
              (into [:column {:gap 3}]
                    (for [todo @todos]
                      [:row {:gap 10 :margin [3 0]}
                       [:button {:label (if (:done todo) "✓" "○")
                                 :size [35 30]
                                 :on-click #(toggle-done! (:id todo))}]
                       [:text {:content (:text todo)
                               :font-size 13}]
                       [:button {:label "X"
                                 :size [35 30]
                                 :on-click #(remove-todo! (:id todo))}]]))))))

      ;; Instructions
      (el/add-child! main-col
        (compile-element
          [:text {:content "○/✓ = toggle done | X = delete"
                  :font-size 11}]))

      ;; Quit button
      (el/add-child! main-col
        (compile-element
          [util/make-quit-button {:size [150 40]}])))

    (println "TODO app UI mounted with textbox!")
    (println "Opening window...")

    ;; Open and run
    (hypr/open-window! window)
    (println "Window opened!")
    (println "Click 'Add Task' to create todos (textbox visual only)")
    (println "")

    (hypr/enter-loop!)

    (println "\nTODO app exited")))

(comment
  (-main)
  )
