(ns todo-app
  "Simple TODO app demonstrating list management and reactivity."
  (:require [hyprclj.core :as hypr]
            [hyprclj.dsl :refer [ratom compile-element]]
            [hyprclj.elements :as el]
            [hyprclj.simple-reactive :refer [reactive-mount!]]
            [hyprclj.util :as util]))

;; State
(def todos (ratom []))
(def next-id (atom 0))

;; Helper to generate todo text
(defn generate-todo-text []
  (let [tasks ["Buy groceries"
               "Write documentation"
               "Fix bugs"
               "Add features"
               "Review PRs"
               "Refactor code"
               "Update dependencies"
               "Write tests"
               "Deploy to production"
               "Celebrate success!"]]
    (str (rand-nth tasks) " #" @next-id)))

(defn add-todo! []
  (let [text (generate-todo-text)]
    (swap! next-id inc)
    (swap! todos conj {:id @next-id :text text :done false})
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
  (println "=== TODO App ===")
  (println "Demonstrating list management with reactivity!")
  (println "")

  ;; Create backend
  (hypr/create-backend!)

  ;; Create window
  (let [window (hypr/create-window
                {:title "Hyprclj TODO App"
                 :size [500 600]
                 :on-close (fn [w]
                             (println "\nClosing TODO app...")
                             (util/exit-clean!))})]

    (println "Window created")

    (let [root (hypr/root-element window)
          main-col (el/column-layout {:gap 15 :margin 20})]

      (el/add-child! root main-col)

      ;; Header (static)
      (el/add-child! main-col
        (compile-element
          [:text {:content "TODO List"
                  :font-size 28}]))

      ;; Counter (reactive - shows total)
      (let [counter-container (el/row-layout {:gap 10})]
        (el/add-child! main-col counter-container)

        (reactive-mount! counter-container [todos]
          (fn []
            [:text {:content (str "Total: " (count @todos) " | "
                                "Done: " (count (filter :done @todos)))
                    :font-size 14}])))

      ;; Add button (static)
      (el/add-child! main-col
        (compile-element
          [:button {:label "Add TODO"
                    :size [150 40]
                    :on-click add-todo!}]))

      ;; Separator
      (el/add-child! main-col
        (compile-element
          [:column {:size [-1 2] :margin [5 0]}]))

      ;; TODO list (reactive - entire list rebuilds)
      (let [list-container (el/column-layout {:gap 5})]
        (el/add-child! main-col list-container)

        (reactive-mount! list-container [todos]
          (fn []
            (if (empty? @todos)
              [:text {:content "No todos yet - click 'Add TODO' to start!"
                      :font-size 12}]
              ;; Build list dynamically
              (into [:column {:gap 3}]
                    (for [todo @todos]
                      [:row {:gap 10 :margin [5 0]}
                       ;; Done/Undone button
                       [:button {:label (if (:done todo) "✓" "○")
                                 :size [40 35]
                                 :on-click #(toggle-done! (:id todo))}]

                       ;; Todo text
                       [:text {:content (:text todo)
                               :font-size 14}]

                       ;; Spacer
                       ;; Delete button
                       [:button {:label "X"
                                 :size [40 35]
                                 :on-click #(remove-todo! (:id todo))}]]))))))

      ;; Instructions (static)
      (el/add-child! main-col
        (compile-element
          [:column {:gap 2 :margin [10 0]}]))

      (el/add-child! main-col
        (compile-element
          [:text {:content "Click ○ to mark done, X to delete"
                  :font-size 11}]))

      ;; Quit button
      (el/add-child! main-col
        (compile-element
          [util/make-quit-button {:size [150 40]}])))

    (println "TODO app UI mounted!")
    (println "Opening window...")

    ;; Open and run
    (hypr/open-window! window)
    (println "Window opened!")
    (println "Click 'Add TODO' to create tasks")
    (println "Click ○ to mark done, X to delete")
    (println "")

    (hypr/enter-loop!)

    (println "\nTODO app exited")))

(comment
  (-main)
  )
