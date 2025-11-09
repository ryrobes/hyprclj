(ns todo-beautiful
  "Beautifully laid-out TODO with inline editing - pure declarative DSL!"
  (:require [hyprclj.core :as hypr]
            [hyprclj.dsl :refer [ratom mount!]]
            [hyprclj.input :as input]
            [hyprclj.keyed-reactive :refer [reactive-mount-keyed!]]
            [hyprclj.simple-reactive :refer [reactive-mount!]]
            [hyprclj.util :as util]))

;; State
(def todos (ratom []))
(def input-text (atom ""))
(def editing-id (atom nil))
(def edit-text (atom ""))
(def next-id (atom 0))

(defn start-edit! [id text]
  (reset! editing-id id)
  (reset! edit-text text)
  (println "✏ Editing:" text))

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
  (reset! editing-id nil)
  (reset! edit-text ""))

(defn add-todo! []
  (when (seq @input-text)
    (swap! next-id inc)
    (swap! todos conj {:id @next-id :text @input-text :done false})
    (println "✓ Added:" @input-text)
    (reset! input-text "")))

(defn remove-todo! [id]
  (swap! todos (fn [ts] (vec (remove #(= (:id %) id) ts)))))

(defn toggle-done! [id]
  (swap! todos (fn [ts]
                 (mapv (fn [t]
                         (if (= (:id t) id)
                           (update t :done not)
                           t))
                       ts))))

(defn -main [& args]
  (println "=== Beautiful TODO ===")

  (hypr/create-backend!)

  (let [window (hypr/create-window
                {:title "Beautiful TODO"
                 :grow true
                 ;:size [1200 1200]
                 :on-close (fn [w] (util/exit-clean!))})]

    ;; Keyboard
    (input/setup-keyboard-handler! window
                                   (fn [keycode pressed? utf8 modifiers]
                                     (when pressed?
                                       (if @editing-id
                                         (cond
                                           (= keycode 65293) (save-edit!)
                                           (= keycode 65307) (cancel-edit!)
                                           (= keycode 65288) (swap! edit-text (fn [s] (if (seq s) (subs s 0 (dec (count s))) s)))
                                           (and (seq utf8) (not= utf8 "")) (swap! edit-text str utf8))
                                         (cond
                                           (= keycode 65293) (add-todo!)
                                           (= keycode 65288) (swap! input-text (fn [s] (if (seq s) (subs s 0 (dec (count s))) s)))
                                           (and (seq utf8) (not= utf8 "")) (swap! input-text str utf8))))))

    ;; Build UI imperatively but with reactive components
    (let [root (hypr/root-element window)
          [win-w win-h] (hypr/window-size window)]

      (println "DEBUG: Window size:" win-w "x" win-h)
      (println "DEBUG: Root element:" root)

      (let [main-col (hyprclj.elements/column-layout {:gap 15
                                                       :margin 20
                                                       :size [win-w win-h]})]
        (println "DEBUG: Main column created with size:" [win-w win-h])

        (hyprclj.elements/add-child! root main-col)
        (println "DEBUG: Main column added to root")

      ;; Header - static
      (hyprclj.elements/add-child! main-col
                                   (hyprclj.dsl/compile-element
                                    [:text {:content "Beautiful TODO" :font-size 28}]))

      ;; Stats - reactive
      (let [stats (hyprclj.elements/column-layout {})]
        (hyprclj.elements/add-child! main-col stats)
        (reactive-mount! stats [todos editing-id]
                         (fn []
                           [:text {:content (if @editing-id
                                              "✏ EDIT MODE"
                                              (str (count @todos) " tasks (" (count (filter :done @todos)) " done)"))
                                   :font-size 13}])))

      ;; Input display - reactive
      (let [input-display (hyprclj.elements/column-layout {:gap 3 :grow true})]
        (hyprclj.elements/add-child! main-col input-display)
        (reactive-mount! input-display [input-text editing-id edit-text]
                         (fn []
                           (if @editing-id
                             [:text {:content (str "Editing: " @edit-text " |") :font-size 15}]
                             [:text {:content (if (seq @input-text)
                                                (str "» " @input-text " _")
                                                "Type to add task...")
                                     :font-size 15}]))))

      ;; Separator
      (hyprclj.elements/add-child! main-col
                                   (hyprclj.dsl/compile-element [:column {:size [-1 2] :margin [10 0]}]))

      ;; TODO list with keyed reconciliation
      (let [list (hyprclj.elements/column-layout {:gap 6 :grow true})]
        (hyprclj.elements/add-child! main-col list)

        (reactive-mount-keyed! list [todos editing-id edit-text]
                               (fn []
                                 (if (empty? @todos)
                                   [:column {} [:text {:content "No tasks!" :font-size 14}]]
                                   (into [:column {:gap 5 :grow true}]
                                         (for [todo @todos]
                                           (let [is-editing? (= @editing-id (:id todo))]
                                             ^{:key (:id todo)}
                                             [:row {:gap 10}
                                              [:checkbox {:checked (:done todo)
                                                          :on-change (fn [_] (toggle-done! (:id todo)))}]
                                              (if is-editing?
                                                [:text {:content (str "✏ " @edit-text " |") :font-size 14}]
                                                [:button {:label (:text todo)
                                                          :size [450 30]
                                                          :on-click (fn [] (start-edit! (:id todo) (:text todo)))}])
                                              (if is-editing?
                                                [:text {:content " "}]
                                                [:button {:label "×" :size [35 30]
                                                          :on-click (fn [] (remove-todo! (:id todo)))}])])))))))

        ;; Footer with quit
        (hyprclj.elements/add-child! main-col
                                     (hyprclj.dsl/compile-element
                                      [:column {:gap 3}
                                       [:text {:content "Click task to edit | Enter=save | Esc=cancel" :font-size 10}]
                                       [util/make-quit-button {:size [150 40]}]]))

        (println "DEBUG: All UI elements added")))

    (println "DEBUG: Opening window...")
    (hypr/open-window! window)
    (println "DEBUG: Window opened, entering event loop")
    (hypr/enter-loop!)))

(comment (-main))
