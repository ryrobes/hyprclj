(ns todo-checkbox
  "TODO app with real checkboxes using Phase 2.5 keyed reconciliation."
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
  (println "=== TODO with Checkboxes ===")
  (println "Real Hyprtoolkit checkboxes + keyed reconciliation!")
  (println "")

  (hypr/create-backend!)

  (let [window (hypr/create-window
                {:title "TODO with Checkboxes"
                 :size [650 750]
                 :on-close (fn [w]
                             (println "\nClosing...")
                             (util/exit-clean!))})]

    (println "Window created")

    ;; Keyboard for text input
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
          [:text {:content "TODO List with Checkboxes"
                  :font-size 26}]))

      ;; Stats
      (let [stats-container (el/column-layout {})]
        (el/add-child! main-col stats-container)
        (reactive-mount! stats-container [todos]
          (fn []
            [:text {:content (str "📋 " (count @todos) " tasks ("
                                (count (filter :done @todos)) " done)")
                    :font-size 14}])))

      ;; Input
      (el/add-child! main-col
        (dsl/compile-element [:text {:content "New task:" :font-size 12}]))

      (let [input-display (el/column-layout {})]
        (el/add-child! main-col input-display)
        (reactive-mount! input-display [input-text]
          (fn []
            [:text {:content (if (seq @input-text)
                              (str "» " @input-text " _")
                              "(type here)")
                    :font-size 16}])))

      ;; Separator
      (el/add-child! main-col
        (dsl/compile-element [:column {:size [-1 2] :margin [8 0]}]))

      ;; TODO list with REAL CHECKBOXES!
      (let [list-container (el/column-layout {:gap 6})]
        (el/add-child! main-col list-container)

        (reactive-mount-keyed! list-container [todos]
          (fn []
            (if (empty? @todos)
              [:column {}
               [:text {:content "No tasks - type and press Enter!"
                       :font-size 14}]]
              (into [:column {:gap 5}]
                    (for [todo @todos]
                      ^{:key (:id todo)}  ; ← Keyed reconciliation!
                      [:row {:gap 10 :margin [3 0]}
                       ;; Real Hyprtoolkit checkbox!
                       [:checkbox {:checked (:done todo)
                                   :on-change (fn [checked?]
                                                (toggle-done! (:id todo)))}]

                       ;; Task text
                       [:text {:content (:text todo)
                               :font-size 14}]

                       ;; Delete button
                       [:button {:label "×"
                                 :size [35 30]
                                 :on-click (fn []
                                             (remove-todo! (:id todo)))}]]))))))

      ;; Instructions
      (el/add-child! main-col
        (dsl/compile-element
          [:column {:gap 2 :margin [8 0]}
           [:text {:content "💡 Tips:" :font-size 12}]
           [:text {:content "• Type task, press Enter" :font-size 10}]
           [:text {:content "• Click checkbox to mark done" :font-size 10}]
           [:text {:content "• Click × to delete" :font-size 10}]
           [:text {:content "• Uses keyed reconciliation!" :font-size 10}]]))

      ;; Quit
      (el/add-child! main-col
        (dsl/compile-element [util/make-quit-button {:size [150 40]}])))

    (println "TODO with checkboxes mounted!")
    (println "Opening window...")

    (hypr/open-window! window)
    (println "Window opened!")
    (println "")

    (hypr/enter-loop!)

    (println "\nTODO checkbox exited")))

(comment
  (-main)
  )
