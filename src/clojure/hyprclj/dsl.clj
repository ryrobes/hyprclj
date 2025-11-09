(ns hyprclj.dsl
  "Hiccup-style DSL for declarative UI construction.
   Provides a Reagent-like syntax for building UIs."
  (:require [hyprclj.elements :as el]
            [hyprclj.reactive :as r]
            [hyprclj.layout :as layout]))

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
                             [{} (vec args)])]

      (cond
        ;; Custom component from registry
        (and (keyword? tag) (@components tag))
        (let [component-fn (@components tag)]
          (compile-element (apply component-fn props (vec children))))

        ;; Component function
        (fn? tag)
        (compile-element (apply tag props (vec children)))

        ;; Built-in elements
        :else
        (let [element (case tag
                        :button (el/button props)
                        :text (el/text props)
                        :textbox (el/textbox props)
                        :checkbox (el/checkbox props)
                        :column (el/column-layout props)
                        :row (el/row-layout props)
                        ;; Re-com style layout components
                        :v-box (layout/v-box props)
                        :h-box (layout/h-box props)
                        :box (layout/box props)
                        :gap (layout/gap props)
                        :spacer (layout/spacer props)
                        :line (layout/line (:direction props :horizontal) props)
                        ;; Default: try as text
                        (el/text {:content (str tag)}))]

          ;; Add children
          (when (seq children)
            (compile-children element children))

          element)))))

(defn mount!
  "Mount a component spec into a parent element.

   Args:
     parent - Parent element (usually window root)
     component - Hiccup spec or component function

   Example:
     (mount! (root-element window)
             [:column {}
               [:text \"Hello\"]
               [:button {:label \"Click me!\"}]])"
  [parent component]
  (el/clear-children! parent)
  (let [compiled (if (fn? component)
                   (compile-element (component))
                   (compile-element component))]
    (when compiled
      (el/add-child! parent compiled)))
  parent)

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
