(ns keyboard-test
  "Simple keyboard event testing."
  (:require [hyprclj.core :as hypr]
            [hyprclj.dsl :refer [ratom compile-element]]
            [hyprclj.elements :as el]
            [hyprclj.input :as input]
            [hyprclj.simple-reactive :refer [reactive-mount!]]
            [hyprclj.util :as util]))

;; State
(def typed-text (atom ""))
(def key-log (atom []))

(defn -main [& args]
  (println "=== Keyboard Test ===")
  (println "Testing full keyboard input!")
  (println "")

  ;; Create backend
  (hypr/create-backend!)

  ;; Create window
  (let [window (hypr/create-window
                {:title "Keyboard Test"
                 :size [600 500]
                 :on-close (fn [w]
                             (println "\nClosing...")
                             (util/exit-clean!))})]

    (println "Window created")

    ;; Set up keyboard handler
    (println "Setting up keyboard handler...")
    (input/setup-keyboard-handler! window
      (fn [keycode pressed? utf8 modifiers]
        (println (format "[Clojure] Key event: %d | pressed=%s | UTF8: '%s' | Mods: %d"
                        keycode pressed? utf8 modifiers))
        (when pressed?  ; Only on key down
          (println (format "[Clojure] Processing key: %d | UTF8: '%s'" keycode utf8))

          ;; Log key
          (swap! key-log conj {:key keycode :char utf8 :mods modifiers})

          (cond
            ;; Enter (Return) - keycode 65293 in XKB
            (= keycode 65293)
            (do
              (println "========================================")
              (println "ENTER pressed! Full text was:")
              (println (str "  \"" @typed-text "\""))
              (println "========================================"))

            ;; Backspace - keycode 65288
            (= keycode 65288)
            (swap! typed-text (fn [s]
                                (if (seq s)
                                  (subs s 0 (dec (count s)))
                                  s)))

            ;; Regular printable character
            (and (seq utf8) (not= utf8 ""))
            (swap! typed-text str utf8)))))

    (let [root (hypr/root-element window)
          main-col (el/column-layout {:gap 15 :margin 20})]

      (el/add-child! root main-col)

      ;; Header
      (el/add-child! main-col
        (compile-element
          [:text {:content "Keyboard Input Test"
                  :font-size 26}]))

      (el/add-child! main-col
        (compile-element
          [:text {:content "Type anywhere in this window!"
                  :font-size 14}]))

      ;; Display typed text (reactive)
      (let [text-display (el/column-layout {:gap 5})]
        (el/add-child! main-col text-display)

        (reactive-mount! text-display [typed-text]
          (fn []
            [:column {:gap 5}
             [:text {:content "You typed:"
                     :font-size 13}]
             [:text {:content (if (seq @typed-text)
                               (str "\"" @typed-text "\"")
                               "(nothing yet - start typing!)")
                     :font-size 20}]])))

      ;; Instructions
      (el/add-child! main-col
        (compile-element
          [:column {:gap 3}
           [:text {:content "Instructions:"
                   :font-size 13}]
           [:text {:content "- Type letters, numbers, punctuation"
                   :font-size 11}]
           [:text {:content "- Press Backspace to delete"
                   :font-size 11}]
           [:text {:content "- Press Enter to see it logged"
                   :font-size 11}]
           [:text {:content "- Check console for detailed key events"
                   :font-size 11}]]))

      ;; Clear button
      (el/add-child! main-col
        (compile-element
          [:button {:label "Clear Text"
                    :size [150 40]
                    :on-click (fn []
                                (reset! typed-text "")
                                (println "Cleared!"))}]))

      ;; Quit button
      (el/add-child! main-col
        (compile-element
          [util/make-quit-button {:size [150 40]}])))

    (println "Keyboard test UI mounted!")
    (println "Opening window...")

    ;; Open and run
    (hypr/open-window! window)
    (println "Window opened!")
    (println "Start typing - keyboard events will be logged!")
    (println "")

    (hypr/enter-loop!)

    (println "\nKeyboard test exited")))

(comment
  (-main)
  )
