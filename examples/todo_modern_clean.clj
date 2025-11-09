(ns todo-modern-clean
  "Modern TODO - clean refactor with new responsive layout system."
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

;; ===== Actions =====
(defn start-edit! [id text] (reset! editing-id id) (reset! edit-text text))
(defn save-edit! []
  (when @editing-id
    (swap! todos (fn [ts] (mapv (fn [t] (if (= (:id t) @editing-id) (assoc t :text @edit-text) t)) ts)))
    (reset! editing-id nil) (reset! edit-text "")))
(defn cancel-edit! [] (reset! editing-id nil) (reset! edit-text ""))
(defn add-todo! []
  (when (seq @input-text)
    (swap! next-id inc)
    (swap! todos conj {:id @next-id :text @input-text :done false})
    (reset! input-text "")))
(defn remove-todo! [id] (swap! todos (fn [ts] (vec (remove #(= (:id %) id) ts)))))
(defn toggle-done! [id] (swap! todos (fn [ts] (mapv (fn [t] (if (= (:id t) id) (update t :done not) t)) ts))))

;; ===== UI Setup (runs once per window size) =====
(defn setup-ui! [root w h]
  (let [content-w (- w 50)
        item-w (- content-w 20)
        h-sep (fn [] (dsl/compile-element
                       [:rectangle {:color [0 0 0 0]
                                    :border-color [100 150 200 60]
                                    :border 1
                                    :size [content-w 2]}]))]

    ;; Main column - absolutely positioned
    (let [main-col (el/column-layout {:gap 12})]
      (el/set-position-mode! main-col 0)
      (el/set-absolute-position! main-col 25 25)

      ;; STATIC: Header
      (el/add-child! main-col (dsl/compile-element [:text {:content "📝 Modern TODO" :font-size 28}]))
      (el/add-child! main-col (dsl/compile-element [:text {:content (str "Window: " w "x" h) :font-size 11}]))
      (el/add-child! main-col (h-sep))

      ;; DYNAMIC: Stats
      (let [stats (el/column-layout {})]
        (el/add-child! main-col stats)
        (reactive-mount! stats [todos editing-id]
          (fn []
            (if @editing-id
              [:text {:content (str "✏️  Editing | " (count @todos) " total") :font-size 13}]
              [:text {:content (str "📋 " (count @todos) " tasks (" (count (filter :done @todos)) " done)") :font-size 13}]))))

      ;; DYNAMIC: Input
      (let [input-c (el/column-layout {})]
        (el/add-child! main-col input-c)
        (reactive-mount! input-c [input-text editing-id]
          (fn []
            (if @editing-id
              [:text {:content "(editing below - Enter/Esc)" :font-size 10}]
              [:v-box {:gap 3}
               [:text {:content "Add task:" :font-size 11}]
               [:text {:content (if (seq @input-text) (str "» " @input-text " _") "(type here)") :font-size 14}]]))))

      (el/add-child! main-col (h-sep))

      ;; DYNAMIC: Todo list (keyed for performance!)
      (let [list-c (el/column-layout {:gap 6})]
        (el/add-child! main-col list-c)
        (reactive-mount-keyed! list-c [todos editing-id edit-text]
          (fn []
            (if (empty? @todos)
              [:text {:content "No tasks!" :font-size 14}]
              (into [:v-box {:gap 5}]
                    (for [todo @todos]
                      (let [editing? (= @editing-id (:id todo))]
                        ^{:key (:id todo)}
                        [:h-box {:gap 8}
                         [:checkbox {:checked (:done todo) :on-change (fn [_] (toggle-done! (:id todo)))}]
                         (if editing?
                           [:text {:content (str "✏️  " @edit-text " |") :font-size 13}]
                           [:button {:label (:text todo) :size [item-w 28] :on-click (fn [] (start-edit! (:id todo) (:text todo)))}])
                         (if editing?
                           [:text {:content "" :font-size 1}]
                           [:button {:label "×" :size [30 28] :on-click (fn [] (remove-todo! (:id todo)))}])])))))))

      (el/add-child! main-col (h-sep))

      ;; STATIC: Instructions
      (el/add-child! main-col
        (dsl/compile-element
          [:v-box {:gap 2}
           [:text {:content "💡 Type→Enter=add | Click→edit | ☑=done" :font-size 10}]]))

      ;; Add to root
      (el/add-child! root main-col))))

;; ===== Main =====
(defn -main [& args]
  (println "\n=== Modern Responsive TODO ===")
  (println "Optimized: Only reactive sections repaint!\n")

  (hypr/create-backend!)

  (let [w 650
        h 600
        window (hypr/create-window
                {:title "Modern TODO"
                 :size [w h]
                 :on-close (fn [_] (util/exit-clean!))})]

    ;; Keyboard
    (input/setup-keyboard-handler! window
      (fn [keycode pressed? utf8 _]
        (when pressed?
          (if @editing-id
            (cond (= keycode 65293) (save-edit!)
                  (= keycode 65307) (cancel-edit!)
                  (= keycode 65288) (swap! edit-text #(if (seq %) (subs % 0 (dec (count %))) %))
                  (and (seq utf8) (not= utf8 "")) (swap! edit-text str utf8))
            (cond (= keycode 65293) (add-todo!)
                  (= keycode 65288) (swap! input-text #(if (seq %) (subs % 0 (dec (count %))) %))
                  (and (seq utf8) (not= utf8 "")) (swap! input-text str utf8))))))

    ;; Setup UI once
    (setup-ui! (hypr/root-element window) w h)

    (hypr/open-window! window)
    (hypr/enter-loop!)))

(comment (-main))
