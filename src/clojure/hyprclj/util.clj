(ns hyprclj.util
  "Utility functions for Hyprclj.")

(defn exit-clean!
  "Exit the application cleanly without triggering native cleanup crashes.
   Uses kill -9 to avoid OpenGL context cleanup issues during JVM shutdown."
  []
  (let [pid (-> (java.lang.ProcessHandle/current) (.pid))]
    (future
      (Thread/sleep 50)  ; Brief delay for message printing
      (.exec (Runtime/getRuntime)
             (into-array String ["kill" "-9" (str pid)])))))

(defn make-quit-button
  "Create a standard quit button that exits cleanly.

   Options:
     :label - Button label (default: \"Quit\")
     :size  - Button size (default: [100 40])

   Example:
     (make-quit-button {:label \"Exit\" :size [150 50]})"
  ([]
   (make-quit-button {}))
  ([{:keys [label size] :or {label "Quit" size [100 40]}}]
   [:button {:label label
             :size size
             :on-click (fn []
                         (println "\nExiting...")
                         (exit-clean!))}]))
