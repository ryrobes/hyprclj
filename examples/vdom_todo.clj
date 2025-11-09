(ns vdom-todo
  "TODO app using Virtual DOM - radically simplified!"
  (:require [hyprclj.vdom :as vdom]
            [hyprclj.core :as hypr]
            [hyprclj.input :as input]
            [hyprclj.util :as util]))

;; ===== PURE DATA (Single atom!) =====
(def app-state
  (atom {:todos []
         :input-text ""
         :editing-id nil
         :edit-text ""
         :next-id 0}))

;; ===== ACTIONS (Just swap! app-state) =====
(defn add-todo! []
  (swap! app-state
         (fn [state]
           (if (seq (:input-text state))
             (-> state
                 (update :next-id inc)
                 (update :todos conj {:id (:next-id state)
                                      :text (:input-text state)
                                      :done false})
                 (assoc :input-text ""))
             state))))

(defn toggle-done! [id]
  (swap! app-state update :todos
         (fn [todos]
           (mapv (fn [t]
                   (if (= (:id t) id)
                     (update t :done not)
                     t))
                 todos))))

(defn remove-todo! [id]
  (swap! app-state update :todos
         (fn [todos]
           (vec (remove #(= (:id %) id) todos)))))

(defn start-edit! [id text]
  (swap! app-state assoc :editing-id id :edit-text text))

(defn save-edit! []
  (swap! app-state
         (fn [state]
           (if-let [id (:editing-id state)]
             (-> state
                 (update :todos (fn [todos]
                                  (mapv (fn [t]
                                          (if (= (:id t) id)
                                            (assoc t :text (:edit-text state))
                                            t))
                                        todos)))
                 (assoc :editing-id nil :edit-text ""))
             state))))

(defn cancel-edit! []
  (swap! app-state assoc :editing-id nil :edit-text ""))

;; ===== PURE RENDER FUNCTION =====
(defn render-app
  "Pure function: state → UI

   No refs! No manual reactive mounts! Just data → hiccup!"
  [state [w h]]
  (let [content-w (- w 50)
        btn-w (- content-w 115)
        colors [[255 120 120 200] [120 255 120 200] [120 180 255 200]
                [255 200 120 200] [255 120 255 200] [120 255 255 200]]]

    [:v-box {:gap 12
             :margin 25
             :position :absolute}

     ;; Header
     [:text {:content "VDOM TODO" :font-size 28}]

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

     [:rectangle {:color [0 0 0 0] :border-color [120 150 180 80]
                  :border 1 :size [content-w 2]}]

     ;; Todo list - automatically reconciled!
     [:v-box {:gap 5
              :children (for [todo (sort-by :id (:todos state))]
                          (let [editing? (= (:editing-id state) (:id todo))
                                id (:id todo)
                                done? (:done todo)
                                accent (nth colors (mod id (count colors)))
                                display-text (if done?
                                               (str "[DONE] " (:text todo))
                                               (:text todo))]
                            ^{:key id}  ; User-provided key for stable identity!
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
                               [:button {:label "X"
                                         :size [45 28]
                                         :on-click (fn [] (remove-todo! id))}]
                               [:text {:content "" :font-size 1}])]))}]  ; Placeholder instead of nil!

     [:rectangle {:color [0 0 0 0] :border-color [120 150 180 80]
                  :border 1 :size [content-w 2]}]

     ;; Instructions
     [:v-box {:gap 2}
      [:text {:content "Type → Enter=add | Click→edit | Check=done" :font-size 9}]]

     ;; Debug (pure data!)
     [:v-box {:gap 2}
      [:text {:content "DEBUG: App State" :font-size 8}]
      [:text {:content (pr-str state) :font-size 7}]]]))

;; ===== MAIN =====
(defn -main [& args]
  (println "\n=== VDOM TODO ===")
  (println "Fully data-driven declarative UI!")
  (println "- Single app-state atom")
  (println "- Pure render function")
  (println "- Automatic reconciliation")
  (println "- No manual reactive-mount! calls!\n")

  ;; Setup keyboard handler (uses app-state directly!)
  (let [window (hypr/create-window {:title "VDOM TODO"
                                     :size [650 600]
                                     :on-close (fn [_] (util/exit-clean!))})]

    (input/setup-keyboard-handler! window
      (fn [keycode pressed? utf8 _]
        (when pressed?
          (if (:editing-id @app-state)
            (cond
              (= keycode 65293) (save-edit!)
              (= keycode 65307) (cancel-edit!)
              (= keycode 65288) (swap! app-state update :edit-text
                                       #(if (seq %) (subs % 0 (dec (count %))) %))
              (and (seq utf8) (not= utf8 ""))
              (swap! app-state update :edit-text str utf8))
            (cond
              (= keycode 65293) (add-todo!)
              (= keycode 65288) (swap! app-state update :input-text
                                       #(if (seq %) (subs % 0 (dec (count %))) %))
              (and (seq utf8) (not= utf8 ""))
              (swap! app-state update :input-text str utf8))))))

    ;; Mount VDOM and run!
    (hypr/create-backend!)
    (let [root (hypr/root-element window)]
      (vdom/vdom-mount! root app-state render-app window)
      (hypr/open-window! window)
      (hypr/enter-loop!))))

(comment (-main))
