(ns hyprclj.input
  "Keyboard input and focus management."
  (:import [org.hyprclj.bindings Window$KeyboardListener]))

;; Global focus tracking
(defonce ^:private focused-element (atom nil))
(defonce ^:private focused-handler (atom nil))

;; Key code constants (from Linux input-event-codes.h)
(def KEY_ESC 1)
(def KEY_ENTER 28)
(def KEY_BACKSPACE 14)
(def KEY_SPACE 57)
(def KEY_A 30)
(def KEY_B 48)
(def KEY_C 46)
;; Add more as needed...

;; Shift/modifier keys
(def KEY_LEFTSHIFT 42)
(def KEY_RIGHTSHIFT 54)

;; Track shift state (public so text-input can access)
(defonce shift-pressed (atom false))

(defn keycode->char
  "Convert Linux keycode to character.
   Returns nil for non-printable keys."
  [keycode shift?]
  (let [code (int keycode)]
    (cond
      ;; Letters
      (and (>= code 30) (<= code 38))  ; A-I
      (let [ch (char (+ (- code 30) (int \a)))]
        (if shift? (Character/toUpperCase ch) ch))

      (and (>= code 16) (<= code 25))  ; Q-P
      (let [offset (- code 16)
            chars "qwertyuiop"
            ch (nth chars offset)]
        (if shift? (Character/toUpperCase ch) ch))

      (and (>= code 44) (<= code 50))  ; Z-M
      (let [offset (- code 44)
            chars "zxcvbnm"
            ch (nth chars offset)]
        (if shift? (Character/toUpperCase ch) ch))

      ;; Numbers
      (= code 2) (if shift? \! \1)
      (= code 3) (if shift? \@ \2)
      (= code 4) (if shift? \# \3)
      (= code 5) (if shift? \$ \4)
      (= code 6) (if shift? \% \5)
      (= code 7) (if shift? \^ \6)
      (= code 8) (if shift? \& \7)
      (= code 9) (if shift? \* \8)
      (= code 10) (if shift? \( \9)
      (= code 11) (if shift? \) \0)

      ;; Special chars
      (= code 57) \space
      (= code 12) (if shift? \_ \-)
      (= code 13) (if shift? \+ \=)
      (= code 51) (if shift? \< \,)
      (= code 52) (if shift? \> \.)
      (= code 53) (if shift? \? \/)

      ;; Default
      :else nil)))

(defn setup-keyboard-handler!
  "Set up keyboard event handling for a window.

   Args:
     window - Window instance
     handler-fn - Function called with (keycode pressed?) for each key event

   Example:
     (setup-keyboard-handler! window
       (fn [keycode pressed?]
         (println \"Key:\" keycode \"Pressed:\" pressed?)))"
  [window handler-fn]

  (.setKeyboardListener window
    (reify Window$KeyboardListener
      (onKey [_ keycode pressed utf8 modifiers]
        ;; Track shift state
        (when (or (= keycode KEY_LEFTSHIFT)
                  (= keycode KEY_RIGHTSHIFT))
          (reset! shift-pressed pressed))

        ;; Call handler with all data
        (handler-fn keycode pressed utf8 modifiers)))))

(defn focus!
  "Set focus to an element with a keyboard handler.

   Args:
     element - Element to focus (for tracking only)
     handler-fn - Function called with (keycode pressed?) for keyboard events

   Example:
     (focus! textbox-elem
       (fn [keycode pressed?]
         (when pressed?
           (handle-text-input keycode))))"
  [element handler-fn]
  (reset! focused-element element)
  (reset! focused-handler handler-fn))

(defn blur!
  "Remove focus from current element."
  []
  (reset! focused-element nil)
  (reset! focused-handler nil))

(defn is-focused?
  "Check if an element is currently focused."
  [element]
  (= @focused-element element))

(defn route-keyboard-to-focused!
  "Set up keyboard routing to focused element.
   Call this once per window after creation."
  [window]
  (setup-keyboard-handler! window
    (fn [keycode pressed]
      (when-let [handler @focused-handler]
        (handler keycode pressed)))))

(comment
  ;; Example usage:

  ;; Setup window keyboard routing
  (route-keyboard-to-focused! window)

  ;; When textbox is clicked, focus it
  :on-click (fn []
              (focus! textbox-elem
                (fn [keycode pressed?]
                  (when pressed?
                    (handle-text-input keycode)))))
  )
