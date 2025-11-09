(ns todo-responsive
  "Modern responsive TODO with optimized reactivity."
  (:require [hyprclj.core :as hypr]
            [hyprclj.dsl :as dsl]
            [hyprclj.elements :as el]
            [hyprclj.input :as input]
            [hyprclj.keyed-reactive :refer [reactive-mount-keyed!]]
            [hyprclj.simple-reactive :refer [reactive-mount!]]
            [hyprclj.util :as util]))

;; ===== State =====
(def todos (dsl/ratom []))
(def input-text (atom ""))
(def editing-id (atom nil))
(def edit-text (atom ""))
(def next-id (atom 0))

;; ===== Actions (same as before) =====
(defn start-edit! [id text]
  (reset! editing-id id)
  (reset! edit-text text))

(defn save-edit! []
  (when @editing-id
    (swap! todos (fn [ts]
                   (mapv (fn [t]
                           (if (= (:id t) @editing-id)
                             (assoc t :text @edit-text)
                             t))
                         ts)))
    (reset! editing-id nil)
    (reset! edit-text "")))

(defn cancel-edit! []
  (reset! editing-id nil)
  (reset! edit-text ""))

(defn add-todo! []
  (when (seq @input-text)
    (swap! next-id inc)
    (swap! todos conj {:id @next-id :text @input-text :done false})
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

;; ===== UI Helpers =====
(defn h-separator [w]
  [:rectangle {:color [0 0 0 0]
               :border-color [100 150 200 80]
               :border 1
               :size [w 2]}])

;; ===== Main UI Setup (Imperative with reactive sections) =====
(defn setup-ui! [root window-width window-height]
  (let [content-w (- window-width 50)
        item-w (- content-w 20)]

    ;; Create main column with absolute positioning
    (let [main-col (el/column-layout {:gap 12})]
      (el/set-position-mode! main-col 0)
      (el/set-absolute-position! main-col 25 25)
      (el/set-margin! main-col 0)

      ;; === STATIC: Header ===
      (el/add-child! main-col
        (dsl/compile-element [:text {:content "📝 Modern TODO" :font-size 28}]))

      (el/add-child! main-col
        (dsl/compile-element (h-separator content-w)))

      ;; === DYNAMIC: Stats (reactive) ===
      (let [stats-container (el/column-layout {:gap 5})]
        (el/add-child! main-col stats-container)
        (reactive-mount! stats-container [todos editing-id]
          (fn []
            (if @editing-id
              [:text {:content (str "✏️  Editing mode | " (count @todos) " total")
                      :font-size 13}]
              [:text {:content (str "📋 " (count @todos) " tasks (" (count (filter :done @todos)) " done)")
                      :font-size 13}]))))

      ;; === DYNAMIC: Input area (reactive) ===
      (let [input-container (el/column-layout {:gap 5})]
        (el/add-child! main-col input-container)
        (reactive-mount! input-container [input-text editing-id]
          (fn []
            (if @editing-id
              [:text {:content "(editing below - Enter=save, Esc=cancel)"
                      :font-size 11}]
              [:v-box {:gap 3}
               [:text {:content "New task:" :font-size 11}]
               [:text {:content (if (seq @input-text)
                                  (str "» " @input-text " _")
                                  "(type to add task)")
                       :font-size 14}]]))))

      (el/add-child! main-col
        (dsl/compile-element (h-separator content-w)))

      ;; === DYNAMIC: Todo list (keyed reactive for performance) ===
      (let [list-container (el/column-layout {:gap 6})]
        (el/add-child! main-col list-container)
        (reactive-mount-keyed! list-container [todos editing-id edit-text]
          (fn []
            (if (empty? @todos)
              [:text {:content "No tasks yet!" :font-size 14}]
              (into [:v-box {:gap 5}]
                    (for [todo @todos]
                      (let [is-editing? (= @editing-id (:id todo))]
                        ^{:key (:id todo)}
                        [:h-box {:gap 8}
                         ;; Checkbox
                         [:checkbox {:checked (:done todo)
                                     :on-change (fn [_] (toggle-done! (:id todo)))}]

                         ;; Text or edit field
                         (if is-editing?
                           [:text {:content (str "✏️  " @edit-text " |")
                                   :font-size 13}]
                           [:button {:label (:text todo)
                                     :size [(max 300 (- item-w 80)) 28]
                                     :on-click (fn [] (start-edit! (:id todo) (:text todo)))}])

                         ;; Delete button
                         (if is-editing?
                           [:text {:content "" :font-size 1}]  ; Spacer
                           [:button {:label "×"
                                     :size [30 28]
                                     :on-click (fn [] (remove-todo! (:id todo)))}])])))))))

      (el/add-child! main-col
        (dsl/compile-element (h-separator content-w)))

      ;; === STATIC: Instructions ===
      (el/add-child! main-col
        (dsl/compile-element
          [:v-box {:gap 3}
           [:text {:content "💡 Instructions:" :font-size 11}]
           [:text {:content "Type → Enter=add | Click→edit→Enter/Esc | ☑=done" :font-size 9}]]))

      ;; Add main column to root
      (el/add-child! root main-col))))

;; ===== Main =====
(defn -main [& args]
  (println "\n=== Modern Responsive TODO ===\n")

  (hypr/create-backend!)

  (let [w 700
        h 650
        window (hypr/create-window
                {:title "Modern TODO"
                 :size [w h]
                 :on-close (fn [_] (util/exit-clean!))})]

    ;; Keyboard handler
    (input/setup-keyboard-handler! window
      (fn [keycode pressed? utf8 modifiers]
        (when pressed?
          (if @editing-id
            (cond
              (= keycode 65293) (save-edit!)
              (= keycode 65307) (cancel-edit!)
              (= keycode 65288) (swap! edit-text #(if (seq %) (subs % 0 (dec (count %))) %))
              (and (seq utf8) (not= utf8 "")) (swap! edit-text str utf8))
            (cond
              (= keycode 65293) (add-todo!)
              (= keycode 65288) (swap! input-text #(if (seq %) (subs % 0 (dec (count %))) %))
              (and (seq utf8) (not= utf8 "")) (swap! input-text str utf8))))))

    ;; Initial setup
    (let [root (hypr/root-element window)]
      (setup-ui! root w h))

    (hypr/open-window! window)
    (hypr/enter-loop!)))

(comment
  (-main)
  )
