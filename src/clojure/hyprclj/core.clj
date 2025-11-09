(ns hyprclj.core
  "Core functionality for Hyprtoolkit Clojure bindings.
   Provides backend and window management."
  (:import [org.hyprclj.bindings Backend Window]))

;; Backend management
(defonce ^:private backend-atom (atom nil))

(defn create-backend!
  "Create and initialize the Hyprtoolkit backend.
   There can only be one backend per process."
  []
  (when-not @backend-atom
    (reset! backend-atom (Backend/create)))
  @backend-atom)

(defn get-backend
  "Get the current backend instance."
  []
  (or @backend-atom
      (throw (ex-info "Backend not initialized. Call create-backend! first." {}))))

(defn add-timer!
  "Add a timer that fires after the specified milliseconds.

   Args:
     timeout-ms - Timeout in milliseconds
     callback   - Function to call when timer fires

   Example:
     (add-timer! 1000 #(println \"One second elapsed\"))"
  [timeout-ms callback]
  (.addTimer (get-backend) timeout-ms callback))

(defn add-idle!
  "Add a callback that runs after pending events.

   Args:
     callback - Function to call when idle

   Example:
     (add-idle! #(println \"System is idle\"))"
  [callback]
  (.addIdle (get-backend) callback))

(defn enter-loop!
  "Enter the event loop. This blocks until the application exits.

   This should be called after setting up your windows and UI."
  []
  (.enterLoop (get-backend)))

;; Window management
(defn create-window
  "Create a new window.

   Options map:
     :title      - Window title (string)
     :size       - [width height] in pixels
     :min-size   - [width height] minimum size
     :max-size   - [width height] maximum size
     :on-close   - Function called when window close is requested

   Example:
     (create-window {:title \"My App\"
                     :size [800 600]
                     :on-close #(println \"Window closing\")})"
  [{:keys [title size min-size max-size on-close]
    :or {title "Hyprclj Window"
         size [640 480]}}]
  (let [builder (Window/builder)]
    (when title
      (.title builder title))
    (when size
      (let [[w h] size]
        (.size builder w h)))
    (when min-size
      (let [[w h] min-size]
        (.minSize builder w h)))
    (when max-size
      (let [[w h] max-size]
        (.maxSize builder w h)))
    (when on-close
      (.onClose builder on-close))
    (.build builder)))

(defn open-window!
  "Open a window, making it visible."
  [window]
  (.open window)
  window)

(defn close-window!
  "Close a window."
  [window]
  (.close window))

(defn window-size
  "Get the current window size as [width height]."
  [window]
  (vec (.getSize window)))

(defn root-element
  "Get the root element of a window.
   All UI elements should be added as children of the root."
  [window]
  (.getRootElement window))

;; Convenience function for running an app
(defn run-app!
  "Create a backend, window, and enter the event loop.

   Options:
     :title      - Window title
     :size       - [width height]
     :root       - Function that returns the root UI component
     :on-close   - Cleanup function

   Example:
     (run-app! {:title \"My App\"
                :size [800 600]
                :root (fn [] [column {} [text \"Hello\"]])})"
  [{:keys [title size root on-close] :as opts}]
  (create-backend!)
  (let [window (create-window opts)]
    (when root
      ;; Root will be set up by the DSL layer
      (println "Window created. Use DSL to set up UI."))
    (open-window! window)
    (enter-loop!)))
