(ns vdom-todo-v2
  "Full-featured TODO using VDOM - single atom, pure render."
  (:require [hyprclj.vdom :as vdom]
            [hyprclj.core :as hypr]
            [hyprclj.input :as input]
            [hyprclj.util :as util]))

;; ===== SINGLE APP STATE =====
(def app-state
  (atom {:todos []
         :input-text ""
         :editing-id nil
         :edit-text ""
         :next-id 0}))

;; ===== ACTIONS (Pure data transformations) =====
(defn add-todo! []
  (when (seq (:input-text @app-state))
    (swap! app-state
           (fn [s]
             (-> s
                 (update :next-id inc)
                 (update :todos conj {:id (:next-id s)
                                      :text (:input-text s)
                                      :done false})
                 (assoc :input-text ""))))))

(defn toggle-done! [id]
  (swap! app-state update :todos
         (fn [todos] (mapv (fn [t] (if (= (:id t) id) (update t :done not) t)) todos))))

(defn remove-todo! [id]
  (swap! app-state update :todos
         (fn [todos] (vec (remove #(= (:id %) id) todos)))))

(defn start-edit! [id text]
  (swap! app-state assoc :editing-id id :edit-text text))

(defn save-edit! []
  (when-let [id (:editing-id @app-state)]
    (swap! app-state
           (fn [s]
             (-> s
                 (update :todos (fn [todos]
                                  (mapv (fn [t]
                                          (if (= (:id t) id)
                                            (assoc t :text (:edit-text s))
                                            t))
                                        todos)))
                 (assoc :editing-id nil :edit-text ""))))))

(defn cancel-edit! []
  (swap! app-state assoc :editing-id nil :edit-text ""))

;; ===== PURE RENDER =====
(defn render-todo-app [state [w h]]
  (let [content-w (- w 50)
        btn-w (- content-w 115)
        colors [[255 120 120 200] [120 255 120 200] [120 180 255 200]
                [255 200 120 200] [255 120 255 200] [120 255 255 200]]
        separator [:rectangle {:color [0 0 0 0]
                               :border-color [120 150 180 80]
                               :border 1
                               :size [content-w 2]}]]

    [:v-box {:gap 12 :margin 25 :position :absolute}

     ;; Header
     [:text {:content "VDOM TODO App" :font-size 28}]

     ;; Stats
     [:h-box {:gap 2}
      [:text {:content "Status:" :font-size 12}]
      [:rectangle {:color [0 0 0 0] :size [10 1]}]
      [:text {:content (str (count (:todos state)) " tasks ("
                            (count (filter :done (:todos state))) " done)")
              :font-size 12}]]

     ;; Input
     [:h-box {:gap 2}
      [:text {:content "Add new task:" :font-size 12}]
      [:rectangle {:color [0 0 0 0] :size [10 1]}]
      [:text {:content (if (seq (:input-text state))
                         (str "> " (:input-text state) " _")
                         "(start typing)")
              :font-size 12}]]

     separator

     ;; Todo list (using :children prop for clean syntax!)
     [:v-box {:gap 5
              :children (for [todo (sort-by :id (:todos state))]
                          (let [id (:id todo)
                                editing? (= (:editing-id state) id)
                                done? (:done todo)
                                accent (nth colors (mod id (count colors)))
                                display-text (if done?
                                               (str "[DONE] " (:text todo))
                                               (:text todo))]
                            ^{:key id}
                            [:h-box {:gap 6}
                             [:rectangle {:color (if done? [100 100 100 120] accent)
                                          :rounding 3 :size [12 32]}]
                             [:checkbox {:checked done?
                                         :on-change (fn [_] (toggle-done! id))}]
                             (if editing?
                               [:text {:content (str "Editing: " (:edit-text state) " |")
                                       :font-size 13}]
                               [:button {:label display-text
                                         :size [btn-w 28]
                                         :on-click (fn [] (start-edit! id (:text todo)))}])
                             (if-not editing?
                               [:button {:label "X" :size [45 28] :on-click (fn [] (remove-todo! id))}]
                               [:text {:content "" :font-size 1}])]))}]

     (if (empty? (:todos state))
       [:text {:content "No tasks yet!" :font-size 13}]
       [:text {:content "" :font-size 1}])  ; Placeholder

     separator

     ;; Instructions
     [:v-box {:gap 2}
      [:text {:content "Type + Enter=add | Click=edit | Check=done" :font-size 9}]]]))

;; ===== MAIN =====
(defn -main [& args]
  (println "VDOM TODO App")
  (println "Pure data-driven declarative UI!\n")

  (hypr/create-backend!)

  (let [window (hypr/create-window {:title "VDOM TODO"
                                     :size [650 600]
                                     :on-close (fn [_] (util/exit-clean!))})]

    ;; Setup keyboard before mounting VDOM
    (input/setup-keyboard-handler! window
      (fn [keycode pressed? utf8 _]
        (when pressed?
          (if (:editing-id @app-state)
            ;; Edit mode
            (cond
              (= keycode 65293) (save-edit!)
              (= keycode 65307) (cancel-edit!)
              (= keycode 65288) (swap! app-state update :edit-text
                                       #(if (seq %) (subs % 0 (dec (count %))) %))
              (and (seq utf8) (not= utf8 ""))
              (swap! app-state update :edit-text str utf8))
            ;; Add mode
            (cond
              (= keycode 65293) (add-todo!)
              (= keycode 65288) (swap! app-state update :input-text
                                       #(if (seq %) (subs % 0 (dec (count %))) %))
              (and (seq utf8) (not= utf8 ""))
              (swap! app-state update :input-text str utf8))))))

    ;; Mount VDOM
    (let [root (hypr/root-element window)]
      (vdom/vdom-mount! root app-state render-todo-app window)
      (hypr/open-window! window)
      (hypr/enter-loop!))))

(comment (-main))
