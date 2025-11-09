(ns text-input-demo
  "Demo of keyboard-powered text input."
  (:require [hyprclj.core :as hypr]
            [hyprclj.dsl :refer [ratom compile-element]]
            [hyprclj.elements :as el]
            [hyprclj.input :as input]
            [hyprclj.simple-reactive :refer [reactive-mount!]]
            [hyprclj.util :as util]))

;; State
(def input-text (atom ""))
(def submitted-items (atom []))

(defn submit-text! []
  (when (seq @input-text)
    (swap! submitted-items conj @input-text)
    (println "Submitted:" @input-text)
    (reset! input-text "")))

(defn -main [& args]
  (println "=== Text Input Demo ===")
  (println "Full keyboard input support!")
  (println "")

  ;; Create backend
  (hypr/create-backend!)

  ;; Create window
  (let [window (hypr/create-window
                {:title "Text Input Demo"
                 :size [550 500]
                 :on-close (fn [w]
                             (println "\nClosing...")
                             (util/exit-clean!))})]

    (println "Window created")

    ;; Enable keyboard routing
    (input/route-keyboard-to-focused! window)
    (println "Keyboard routing enabled")

    (let [root (hypr/root-element window)
          main-col (el/column-layout {:gap 15 :margin 20})]

      (el/add-child! root main-col)

      ;; Header
      (el/add-child! main-col
        (compile-element
          [:text {:content "Text Input Demo"
                  :font-size 26}]))

      (el/add-child! main-col
        (compile-element
          [:text {:content "Click the input field and type!"
                  :font-size 13}]))

      ;; Input field with reactive display
      (let [input-row (el/row-layout {:gap 10})]
        (el/add-child! main-col input-row)

        ;; Show what's being typed (reactive)
        (let [input-display (el/column-layout {:gap 2})]
          (el/add-child! input-row input-display)

          (reactive-mount! input-display [input-text]
            (fn []
              [:column {:gap 2}
               [:text {:content (str "Input: " (if (seq @input-text)
                                                 @input-text
                                                 "(empty)"))
                       :font-size 14}]
               [:textbox {:placeholder "Click here and type..."
                          :size [350 40]}]])))

        ;; Add button
        (el/add-child! input-row
          (compile-element
            [:button {:label "Add"
                      :size [80 40]
                      :on-click submit-text!}])))

      ;; Instructions
      (el/add-child! main-col
        (compile-element
          [:text {:content "Click textbox, type text, press Enter or click Add"
                  :font-size 11}]))

      (el/add-child! main-col
        (compile-element
          [:text {:content "Keys: Letters, Numbers, Space, Backspace, Enter, Esc"
                  :font-size 10}]))

      ;; Separator
      (el/add-child! main-col
        (compile-element
          [:column {:size [-1 2] :margin [5 0]}]))

      ;; Submitted items list (reactive)
      (let [list-container (el/column-layout {:gap 5})]
        (el/add-child! main-col list-container)

        (reactive-mount! list-container [submitted-items]
          (fn []
            (if (empty? @submitted-items)
              [:text {:content "Submitted items will appear here"
                      :font-size 12}]
              (into [:column {:gap 3}]
                    (for [[idx item] (map-indexed vector @submitted-items)]
                      [:text {:content (str (inc idx) ". " item)
                              :font-size 13}]))))))

      ;; Set up keyboard handler for window
      ;; Route keys to input when textbox area is clicked
      (let [textbox-elem (first (filter #(instance? org.hyprclj.bindings.Element %)
                                        [input-display]))]  ; Get the input container

        ;; Focus on click
        (.setMouseHandlers textbox-elem
          (fn [_]  ; onClick
            (println "[Input] Focused on textbox")
            (input/focus! textbox-elem
              (fn [keycode pressed?]
                (when pressed?
                  (cond
                    ;; Enter - submit
                    (= keycode input/KEY_ENTER)
                    (submit-text!)

                    ;; Backspace - delete last char
                    (= keycode input/KEY_BACKSPACE)
                    (swap! input-text (fn [s]
                                        (if (seq s)
                                          (subs s 0 (dec (count s)))
                                          s)))

                    ;; Escape - blur
                    (= keycode input/KEY_ESC)
                    (do
                      (input/blur!)
                      (println "[Input] Blurred"))

                    ;; Regular character
                    :else
                    (when-let [ch (input/keycode->char keycode @input/shift-pressed)]
                      (swap! input-text str ch)))))))
          nil  ; onEnter
          nil)) ; onLeave

      ;; Quit button
      (el/add-child! main-col
        (compile-element
          [util/make-quit-button {:size [150 40]}])))

    (println "Text input demo mounted!")
    (println "Opening window...")

    ;; Open and run
    (hypr/open-window! window)
    (println "Window opened!")
    (println "Instructions:")
    (println "  1. Click the input field to focus")
    (println "  2. Type text (letters, numbers, space)")
    (println "  3. Press Backspace to delete")
    (println "  4. Press Enter to submit")
    (println "  5. Press Esc to unfocus")
    (println "")

    (hypr/enter-loop!)

    (println "\nText input demo exited")))

(comment
  (-main)
  )
