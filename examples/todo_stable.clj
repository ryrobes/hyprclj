(ns todo-stable
  "Stable TODO app with granular reactivity to prevent button crashes."
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
  (println "=== Stable TODO App ===")
  (println "Uses static buttons to prevent crashes!")
  (println "")

  ;; Create backend
  (hypr/create-backend!)

  ;; Create window
  (let [window (hypr/create-window
                {:title "Stable TODO App"
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
          main-col (el/column-layout {:gap 15 :margin 20})]

      (el/add-child! root main-col)

      ;; Header
      (el/add-child! main-col
        (compile-element
          [:text {:content "Stable TODO List"
                  :font-size 28}]))

      ;; Stats (reactive but safe - just text)
      (let [stats-container (el/column-layout {})]
        (el/add-child! main-col stats-container)
        (reactive-mount! stats-container [todos]
          (fn []
            [:text {:content (str "Total: " (count @todos) " | Done: " (count (filter :done @todos)))
                    :font-size 13}])))

      ;; Input display (reactive)
      (let [input-display (el/column-layout {:gap 3})]
        (el/add-child! main-col input-display)
        (el/add-child! main-col
          (compile-element [:text {:content "Type task and press Enter:" :font-size 12}]))
        (reactive-mount! input-display [input-text]
          (fn []
            [:text {:content (if (seq @input-text)
                              (str "» " @input-text)
                              "(start typing...)")
                    :font-size 18}])))

      ;; Separator
      (el/add-child! main-col
        (compile-element [:column {:size [-1 2] :margin [5 0]}]))

      ;; TODO list - JUST TEXT, NO BUTTONS IN REACTIVE PART
      (let [list-container (el/column-layout {:gap 8})]
        (el/add-child! main-col list-container)

        ;; Reactive: Just display the todo items as numbered list
        (reactive-mount! list-container [todos]
          (fn []
            (if (empty? @todos)
              [:text {:content "No tasks - type and press Enter!"
                      :font-size 14}]
              (into [:column {:gap 2}]
                    (for [[idx todo] (map-indexed vector @todos)]
                      [:text {:content (str (inc idx) ". "
                                          (if (:done todo) "[✓] " "[ ] ")
                                          (:text todo))
                              :font-size 14}]))))))

      ;; Static control buttons (separate from reactive list)
      (el/add-child! main-col
        (compile-element [:text {:content "Controls:" :font-size 12}]))

      (let [controls-row (el/row-layout {:gap 10})]
        (el/add-child! main-col controls-row)

        ;; Toggle first item
        (el/add-child! controls-row
          (compile-element
            [:button {:label "Toggle #1"
                      :size [100 35]
                      :on-click (fn []
                                  (when (seq @todos)
                                    (toggle-done! (:id (first @todos)))))}]))

        ;; Delete first item
        (el/add-child! controls-row
          (compile-element
            [:button {:label "Delete #1"
                      :size [100 35]
                      :on-click (fn []
                                  (when (seq @todos)
                                    (remove-todo! (:id (first @todos)))))}]))

        ;; Clear all
        (el/add-child! controls-row
          (compile-element
            [:button {:label "Clear All"
                      :size [100 35]
                      :on-click (fn []
                                  (reset! todos [])
                                  (println "Cleared all todos"))}])))

      (el/add-child! main-col
        (compile-element
          [:text {:content "↑ Buttons are static - no crashes!"
                  :font-size 10}]))

      ;; Quit
      (el/add-child! main-col
        (compile-element [util/make-quit-button {:size [150 40]}])))

    (println "Stable TODO UI mounted!")
    (println "Opening window...")

    ;; Open and run
    (hypr/open-window! window)
    (println "Window opened!")
    (println "")
    (println "How to use:")
    (println "  • Type task, press Enter to add")
    (println "  • Click 'Toggle #1' to mark first item done")
    (println "  • Click 'Delete #1' to delete first item")
    (println "  • No crashes! Buttons are static")
    (println "")

    (hypr/enter-loop!)

    (println "\nStable TODO exited")))

(comment
  (-main)
  )
