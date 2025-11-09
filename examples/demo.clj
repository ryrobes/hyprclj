(ns demo
  "Demo application showcasing Hyprclj capabilities."
  (:require [hyprclj.core :as hypr]
            [hyprclj.dsl :refer [defcomponent ratom compile-element mount!]]))

;; Application state
(def counter (ratom 0))
(def text-input (ratom ""))
(def todos (ratom []))

;; Counter component
(defcomponent counter-view []
  [:column {:gap 10 :margin 20}
   [:text {:content "Counter Demo"
           :font-size 20}]
   [:text {:content (str "Count: " @counter)
           :font-size 16}]
   [:row {:gap 5}
    [:button {:label "+"
              :size [50 40]
              :on-click #(swap! counter inc)}]
    [:button {:label "-"
              :size [50 40]
              :on-click #(swap! counter dec)}]
    [:button {:label "Reset"
              :size [80 40]
              :on-click #(reset! counter 0)}]]])

;; Todo item component
(defcomponent todo-item [{:keys [text index]}]
  [:row {:gap 10 :margin [5 0]}
   [:text {:content (str (inc index) ". " text)
           :grow true}]
   [:button {:label "X"
             :size [30 30]
             :no-bg true
             :on-click #(swap! todos
                               (fn [ts]
                                 (vec (concat (subvec ts 0 index)
                                            (subvec ts (inc index))))))}]])

;; Todo list component
(defcomponent todo-list []
  [:column {:gap 5 :margin 20}
   [:text {:content "Todo List"
           :font-size 20}]
   [:text {:content (str "Total: " (count @todos))
           :font-size 14}]

   ;; Todo items
   (into [:column {:gap 2}]
         (map-indexed
          (fn [idx todo]
            [todo-item {:text todo :index idx}])
          @todos))

   ;; Add button
   [:button {:label "Add Todo"
             :size [120 40]
             :margin [10 0 0 0]
             :on-click #(swap! todos conj (str "Task " (inc (count @todos))))}]])

;; Timer component
(defcomponent timer-demo []
  (let [elapsed (ratom 0)]
    ;; Set up timer (runs every second)
    (hypr/add-timer! 1000
                     (fn []
                       (swap! elapsed inc)
                       ;; Re-schedule
                       (hypr/add-timer! 1000 #(swap! elapsed inc))))

    (fn []
      [:column {:gap 10 :margin 20}
       [:text {:content "Timer Demo"
               :font-size 20}]
       [:text {:content (str "Elapsed: " @elapsed " seconds")
               :font-size 16}]
       [:button {:label "Reset"
                 :size [80 40]
                 :on-click #(reset! elapsed 0)}]])))

;; Main app layout
(defcomponent app []
  [:column {:gap 20 :margin 20}
   [:text {:content "🚀 Hyprclj Demo"
           :font-size 28}]
   [:text {:content "Clojure bindings for Hyprtoolkit"
           :font-size 14}]

   ;; Divider
   [:column {:size [-1 2] :margin [10 0]}]

   ;; Counter section
   [counter-view]

   ;; Divider
   [:column {:size [-1 2] :margin [10 0]}]

   ;; Todo list section
   [todo-list]

   ;; Divider
   [:column {:size [-1 2] :margin [10 0]}]

   ;; Timer section
   [timer-demo]])

;; Run the application
(defn -main [& args]
  (println "Starting Hyprclj Demo...")

  ;; Create backend
  (hypr/create-backend!)

  ;; Create window
  (let [window (hypr/create-window
                {:title "Hyprclj Demo"
                 :size [600 800]
                 :on-close (fn [w]
                             (println "Window closing...")
                             (hypr/close-window! w))})]

    ;; Mount the app
    (mount! (hypr/root-element window) (app))

    ;; Open window
    (hypr/open-window! window)

    ;; Enter event loop
    (println "Entering event loop...")
    (hypr/enter-loop!))

  (println "Application exited."))

(comment
  ;; To run from REPL:
  (-main)
  )
