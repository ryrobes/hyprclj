(ns hyprclj.core
  "Core functionality for Hyprtoolkit Clojure bindings.
   Provides backend and window management."
  (:require [hyprclj.elements :as elem])
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

(defn enable-responsive-root!
  "Enable automatic UI remounting when window is resized.

   Takes a component-fn that will be remounted with the new window size
   whenever the window is resized.

   Args:
     window - The window
     component-fn - A function that takes [width height] and returns a component spec
     opts - Optional map with:
            :position - :absolute to pin to top-left, :auto (default) for centered

   Example:
     (enable-responsive-root! window
       (fn [[w h]]
         [:column {:gap 10}
           [:text (str \"Window is \" w \"x\" h)]])
       {:position :absolute})"
  ([window]
   ;; Old signature for backwards compatibility - does nothing useful
   (println "[WARN] enable-responsive-root! called without component-fn")
   window)
  ([window component-fn]
   (enable-responsive-root! window component-fn {}))
  ([window component-fn opts]
   (let [root (root-element window)
         rendered-size (atom nil)
         pending-size (atom nil)
         ignore-resize? (atom false)]
     (.setResizeListener window
       (reify org.hyprclj.bindings.Window$ResizeListener
         (onResize [_ width height]
           (let [new-size [width height]]
             (cond
               ;; Currently ignoring - just update pending size
               @ignore-resize?
               (when (and (pos? width) (pos? height))
                 (println "[RESIZE] Queued:" width "x" height)
                 (reset! pending-size new-size))

               ;; Not ignoring - process resize
               (and (pos? width) (pos? height)
                    (not= new-size @rendered-size))
               (do
                 (println "[RESIZE]" width "x" height)
                 (reset! pending-size new-size)
                 (reset! ignore-resize? true)
                 (add-timer! 150
                   (fn []
                     (let [[w h] @pending-size]
                       (println "[REMOUNT] To" w "x" h)
                       (try
                         (require 'hyprclj.dsl)
                         (let [mount-fn (resolve 'hyprclj.dsl/mount!)]
                           (mount-fn root (component-fn [w h]) [w h] opts)
                           (reset! rendered-size [w h]))
                         (catch Exception e
                           (println "[ERROR]" (.getMessage e)))
                         (finally
                           (add-timer! 50
                             (fn []
                               (println "[RESIZE] Re-enabled")
                               (reset! ignore-resize? false)))))))))))))))
     window))

(defn open-window!
  "Open a window, making it visible."
  [window]
  (.open window)
  window)

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
