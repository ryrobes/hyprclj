(ns hyprclj.text-input
  "High-level text input component with keyboard handling."
  (:require [hyprclj.elements :as el]
            [hyprclj.dsl :as dsl]
            [hyprclj.input :as input]
            [hyprclj.simple-reactive :refer [reactive-mount!]]))

(defn make-text-input
  "Create a text input component with full keyboard support.

   Args:
     parent      - Parent element to mount into
     text-atom   - Atom to store the text
     opts        - Options map

   Options:
     :placeholder - Placeholder text
     :on-submit   - Callback when Enter is pressed (receives text)
     :on-change   - Callback on every keystroke (receives text)
     :size        - [width height]
     :window      - Window instance (needed for keyboard events)

   Returns the native textbox element (for focus management)

   Example:
     (def input-text (atom \"\"))
     (make-text-input parent input-text
       {:placeholder \"Type here...\"
        :on-submit (fn [text] (println \"Submitted:\" text))
        :window window})"
  [parent text-atom {:keys [placeholder on-submit on-change size window]
                     :or {placeholder \"\" size [300 40]}}]

  (let [textbox-container (el/column-layout {})]

    ;; Add container to parent
    (el/add-child! parent textbox-container)

    ;; Create reactive textbox that updates when text-atom changes
    (reactive-mount! textbox-container [text-atom]
      (fn []
        (let [current-text @text-atom]
          [:textbox {:placeholder placeholder
                     :size size}])))

    ;; Set up keyboard handler for this input
    (when window
      (let [handle-key (fn [keycode pressed?]
                         (when pressed?
                           (cond
                             ;; Enter - submit
                             (= keycode input/KEY_ENTER)
                             (do
                               (when on-submit
                                 (on-submit @text-atom))
                               (println \"[Input] Submitted:\" @text-atom))

                             ;; Backspace - delete last char
                             (= keycode input/KEY_BACKSPACE)
                             (do
                               (swap! text-atom (fn [s]
                                                  (if (seq s)
                                                    (subs s 0 (dec (count s)))
                                                    s)))
                               (when on-change (on-change @text-atom)))

                             ;; Escape - blur/unfocus
                             (= keycode input/KEY_ESC)
                             (input/blur!)

                             ;; Regular character
                             :else
                             (when-let [ch (input/keycode->char keycode @input/shift-pressed)]
                               (swap! text-atom str ch)
                               (when on-change (on-change @text-atom))))))]

        ;; Make textbox focusable - focus on click
        (doto textbox-container
          (el/set-mouse-handlers
            (fn [_]
              (println \"[Input] Textbox focused\")
              (input/focus! textbox-container handle-key))
            nil nil))))

    textbox-container))

(defn setup-text-input-system!
  \"Initialize keyboard routing for a window.
   Call this once per window that uses text inputs.\"
  [window]
  (input/route-keyboard-to-focused! window))

(comment
  ;; Example usage:

  (def my-text (atom \"\"))

  ;; Setup window
  (setup-text-input-system! window)

  ;; Create input
  (make-text-input parent my-text
    {:placeholder \"Enter name...\"
     :on-submit (fn [text]
                  (println \"Name:\" text)
                  (reset! my-text \"\"))
     :window window})

  ;; Now:
  ;; 1. Click the textbox to focus
  ;; 2. Type characters
  ;; 3. Press Enter to submit
  ;; 4. Text appears in atom and triggers callback!
  )
