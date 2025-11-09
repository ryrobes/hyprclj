(ns todo-ultimate
  "Ultimate TODO app with Phase 2.5 keyed reconciliation.
   Per-item buttons that don't crash!"
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
    (println (if (:done todo) "✓ Completed:" "○ Uncompleted:") (:text todo))))

(defn -main [& args]
  (println "=== Ultimate TODO App ===")
  (println "Phase 2.5 Keyed Reconciliation - Per-item buttons work!")
  (println "")

  (hypr/create-backend!)

  (let [window (hypr/create-window
                {:title "Ultimate TODO"
                 :size [650 750]
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
        (dsl/compile-element
          [:text {:content "Ultimate TODO List"
                  :font-size 28}]))

      (el/add-child! main-col
        (dsl/compile-element
          [:text {:content "Phase 2.5: Keyed Reconciliation!"
                  :font-size 12}]))

      ;; Stats (reactive, simple)
      (let [stats-container (el/column-layout {})]
        (el/add-child! main-col stats-container)
        (reactive-mount! stats-container [todos]
          (fn []
            [:text {:content (str "Total: " (count @todos) " | "
                                "Done: " (count (filter :done @todos)) " | "
                                "Active: " (count (remove :done @todos)))
                    :font-size 13}])))

      ;; Input section
      (el/add-child! main-col
        (dsl/compile-element [:text {:content "Add new task:" :font-size 13}]))

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
        (dsl/compile-element [:column {:size [-1 2] :margin [8 0]}]))

      ;; TODO LIST with KEYED RECONCILIATION! ⭐
      (let [list-container (el/column-layout {:gap 6})]
        (el/add-child! main-col list-container)

        (reactive-mount-keyed! list-container [todos]
          (fn []
            (if (empty? @todos)
              [:column {}
               [:text {:content "No tasks yet!"
                       :font-size 14}]
               [:text {:content "Type something and press Enter to add your first task"
                       :font-size 11}]]
              ;; KEY INSIGHT: Use ^{:key} metadata for each item!
              (into [:column {:gap 4}]
                    (for [todo @todos]
                      ^{:key (:id todo)}  ; ← This keeps buttons stable!
                      [:row {:gap 10 :margin [3 0]}
                       ;; Toggle button - STABLE across updates!
                       [:button {:label (if (:done todo) "✓" "○")
                                 :size [40 35]
                                 :on-click (fn []
                                             (println "Toggling" (:id todo))
                                             (toggle-done! (:id todo)))}]

                       ;; Text
                       [:text {:content (:text todo)
                               :font-size 14}]

                       ;; Delete button - STABLE across updates!
                       [:button {:label "×"
                                 :size [40 35]
                                 :on-click (fn []
                                             (println "Deleting" (:id todo))
                                             (remove-todo! (:id todo)))}]]))))))

      ;; Instructions
      (el/add-child! main-col
        (dsl/compile-element
          [:column {:gap 2 :margin [8 0]}
           [:text {:content "How it works:" :font-size 12}]
           [:text {:content "• Type task, press Enter to add" :font-size 10}]
           [:text {:content "• Click ○/✓ to toggle (NO CRASH!)" :font-size 10}]
           [:text {:content "• Click × to delete" :font-size 10}]
           [:text {:content "• Keys keep buttons stable!" :font-size 10}]]))

      ;; Quit
      (el/add-child! main-col
        (dsl/compile-element [util/make-quit-button {:size [150 40]}])))

    (println "Ultimate TODO mounted with keyed reconciliation!")
    (println "Opening window...")

    (hypr/open-window! window)
    (println "Window opened!")
    (println "")
    (println "✨ This version uses Phase 2.5 keyed reconciliation!")
    (println "Add tasks, toggle them, delete them - NO CRASHES!")
    (println "Buttons are matched by key and reused across renders.")
    (println "")

    (hypr/enter-loop!)

    (println "\nUltimate TODO exited")))

(comment
  (-main)
  )
