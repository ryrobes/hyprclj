(ns hyprclj.dsl
  "Hiccup-style DSL for declarative UI construction.
   Provides a Reagent-like syntax for building UIs."
  (:require [hyprclj.elements :as el]
            [hyprclj.reactive :as r]
            [hyprclj.layout :as layout]
            [hyprclj.layout-system :as ls]))

;; Component registry
(defonce ^:private components (atom {}))

(defn register-component!
  "Register a custom component.

   Example:
     (register-component! :my-button
       (fn [props & children]
         [:button (merge {:label \"Custom\"} props)]))"
  [tag handler]
  (swap! components assoc tag handler))

(defn component?
  "Check if a value is a component function."
  [x]
  (or (keyword? x)
      (fn? x)))

(declare compile-element)

(defn- compile-children
  "Compile a sequence of child elements."
  [parent children]
  (doseq [child children]
    (when child
      (cond
        ;; Nested component
        (vector? child)
        (when-let [compiled (compile-element child)]
          (el/add-child! parent compiled))

        ;; String content (wrap in text)
        (string? child)
        (el/add-child! parent (el/text {:content child}))

        ;; Already compiled element
        (instance? org.hyprclj.bindings.Element child)
        (el/add-child! parent child)

        ;; Reactive atom - deref and use value
        (r/reactive-atom? child)
        (when-let [compiled (compile-element @child)]
          (el/add-child! parent compiled)))))
  parent)

(defn compile-element
  "Compile a Hiccup-style element specification into a native element.

   Format: [tag props? & children]
   - tag: keyword (:button, :text, :column, etc.) or component function
   - props: optional map of properties
   - children: child elements

   Examples:
     [:text \"Hello\"]
     [:text {:font-size 24} \"Hello\"]
     [:button {:label \"Click\" :on-click #(println \"clicked\")}]
     [:column {:gap 10}
       [:text \"Line 1\"]
       [:text \"Line 2\"]]"
  [spec]
  (when (vector? spec)
    (let [[tag & args] spec
          ;; Check if first item after tag is a props map
          [props children] (if (and (seq args) (map? (first args)))
                             [(first args) (vec (clojure.core/rest args))]
                             [{} (vec args)])
          ;; Support :children prop - if present, use it instead of rest args
          final-children (if (:children props)
                          (:children props)
                          children)
          ;; Remove :children from props before passing to element constructors
          final-props (dissoc props :children)]

      (cond
        ;; Custom component from registry
        (and (keyword? tag) (@components tag))
        (let [component-fn (@components tag)]
          (compile-element (apply component-fn final-props (vec final-children))))

        ;; Component function
        (fn? tag)
        (compile-element (apply tag final-props (vec final-children)))

        ;; Built-in elements
        :else
        (let [element (case tag
                        :button (el/button final-props)
                        :colored-button (ls/colored-button final-props)
                        :text (el/text final-props)
                        :textbox (el/textbox final-props)
                        :checkbox (el/checkbox final-props)
                        :rectangle (el/rectangle final-props)
                        :scroll-area (el/scroll-area final-props)
                        :scrollable (el/scroll-area final-props)  ; Alias
                        :column (el/column-layout final-props)
                        :row (el/row-layout final-props)
                        ;; NEW Re-com style layout with positioning support
                        :v-box (ls/v-box final-props)
                        :h-box (ls/h-box final-props)
                        :box (ls/box final-props)
                        ;; OLD re-com compatibility
                        :v-box-old (layout/v-box final-props)
                        :h-box-old (layout/h-box final-props)
                        :box-old (layout/box final-props)
                        :gap (layout/gap final-props)
                        :spacer (layout/spacer final-props)
                        :line (layout/line (:direction final-props :horizontal) final-props)
                        ;; Default: try as text
                        (el/text {:content (str tag)}))]

          ;; Add children
          (when (seq final-children)
            (compile-children element final-children))

          element)))))

(defn mount!
  "Mount a component spec into a parent element.

   If window-size is provided as [width height], applies size to the root
   layout element to enable responsive layouts.

   Args:
     parent - Parent element (usually window root)
     component - Hiccup spec or component function
     window-size - Optional [width height] to set on root element
     opts - Optional map with:
            :position-mode - :auto (default, root centers content) or :absolute (pin to 0,0)

   Examples:
     (mount! (root-element window) [:column {} [:text \"Hello\"]])
     (mount! (root-element window) [:column {} [:text \"Hello\"]] [800 600])
     (mount! (root-element window) [:column {} [:text \"Hello\"]] [800 600] {:position-mode :absolute})"
  ([parent component]
   (el/clear-children! parent)
   (let [compiled (if (fn? component)
                    (compile-element (component))
                    (compile-element component))]
     (when compiled
       (el/add-child! parent compiled)))
   parent)
  ([parent component window-size]
   (mount! parent component window-size {}))
  ([parent component window-size opts]
   (el/clear-children! parent)
   (if (and window-size (vector? component))
     ;; Merge window size into the root component's props
     (let [[tag props-or-child & rest-args] component
           [w h] window-size
           ;; Check if second element is a props map
           has-props? (map? props-or-child)
           props (if has-props? props-or-child {})
           children (if has-props? rest-args (cons props-or-child rest-args))
           ;; Merge size into props (but NOT for absolute positioning!)
           new-props (if (= (:position opts) :absolute)
                       props  ; Don't set size for absolute - let it be natural
                       (assoc props :size [w h]))
           new-component (vec (cons tag (cons new-props children)))]
       (let [compiled (compile-element new-component)]
         (when compiled
           (if (= (:position opts) :absolute)
             ;; Absolute positioning: pin to (0, 0) with natural size
             (do
               (el/set-position-mode! compiled 0)
               (el/set-absolute-position! compiled 0 0)
               (el/set-margin! compiled 0)
               (el/add-child! parent compiled))
             ;; Auto positioning: centered with explicit size
             (el/add-child! parent compiled)))))
     ;; No window-size or not a vector, mount normally
     (let [compiled (if (fn? component)
                      (compile-element (component))
                      (compile-element component))]
       (when compiled
         (el/add-child! parent compiled))))
   parent))

(defn remount!
  "Re-mount a component, useful for reactive updates.

   Args:
     parent - Parent element
     component-fn - Function that returns component spec

   This is typically called automatically by the reactive system."
  [parent component-fn]
  (mount! parent (component-fn)))

;; Convenience macro for defining components
(defmacro defcomponent
  "Define a UI component.

   Example:
     (defcomponent greeting [props]
       [:text {:content (str \"Hello, \" (:name props))}])

     Usage: [greeting {:name \"Alice\"}]"
  [name args & body]
  `(defn ~name ~args
     ~@body))

;; Reactive component wrapper
(defn reactive
  "Make a component reactive to atom changes.

   Example:
     (def counter (atom 0))
     (reactive
       (fn []
         [:text {:content (str \"Count: \" @counter)}]))"
  [component-fn]
  (r/make-reactive component-fn))

;; Example component helpers
(defn spacer
  "Create a flexible spacer that grows to fill space.

   Example:
     [:column {}
       [:text \"Top\"]
       [spacer]
       [:text \"Bottom\"]]"
  ([]
   (spacer {}))
  ([props]
   (let [col (el/column-layout (merge {:size [1 1]} props))]
     (el/set-grow! col true)
     col)))

;; Re-export for convenience
(def ratom r/ratom)
(def reaction r/reaction)
(def cursor r/cursor)

(comment
  ;; Example usage:

  ;; Simple static UI
  [:column {:gap 10}
   [:text {:content "Hello, Hyprland!"
           :font-size 24}]
   [:button {:label "Click me"
             :on-click #(println "Clicked!")}]]

  ;; Reactive UI
  (def counter (ratom 0))

  [:column {:gap 10}
   [:text {:content (str "Count: " @counter)}]
   [:button {:label "Increment"
             :on-click #(swap! counter inc)}]]

  ;; Custom component
  (defcomponent counter-display [{:keys [count]}]
    [:text {:content (str "Count: " count)
            :font-size 18}])

  [:column {}
   [counter-display {:count @counter}]
   [:button {:label "+" :on-click #(swap! counter inc)}]]
  )
