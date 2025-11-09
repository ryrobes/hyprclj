# Hyprclj Tutorial

Let's build a simple TODO app to learn Hyprclj!

## Step 1: Hello World

Create `hello.clj`:

```clojure
(ns hello
  (:require [hyprclj.core :as hypr]
            [hyprclj.dsl :refer [mount!]]))

(defn -main []
  ;; 1. Create backend (once per app)
  (hypr/create-backend!)

  ;; 2. Create window
  (let [window (hypr/create-window
                {:title "Hello Hyprclj"
                 :size [400 200]})]

    ;; 3. Mount UI
    (mount! (hypr/root-element window)
            [:text {:content "Hello, World!"
                    :font-size 32}])

    ;; 4. Open and run
    (hypr/open-window! window)
    (hypr/enter-loop!)))
```

Run it:
```bash
clj -M -m hello
```

You should see a window with "Hello, World!"

## Step 2: Adding a Button

Let's add interactivity:

```clojure
(mount! (hypr/root-element window)
        [:column {:gap 20 :margin 30}
         [:text {:content "Hello, World!"
                 :font-size 32}]
         [:button {:label "Click me!"
                   :size [150 50]
                   :on-click #(println "Button clicked!")}]])
```

**What's new:**

- `:column` - Layout container that stacks children vertically
- `:gap` - Spacing between children
- `:margin` - Padding around the column
- `:on-click` - Event handler

## Step 3: Adding State

Now let's make it reactive with an atom:

```clojure
(ns hello
  (:require [hyprclj.core :as hypr]
            [hyprclj.dsl :refer [mount! ratom]]))

;; Reactive state
(def click-count (ratom 0))

(defn -main []
  (hypr/create-backend!)
  (let [window (hypr/create-window {:title "Counter"})]

    (mount! (hypr/root-element window)
            [:column {:gap 20 :margin 30}
             [:text {:content (str "Clicks: " @click-count)
                     :font-size 32}]
             [:button {:label "Click me!"
                       :size [150 50]
                       :on-click #(swap! click-count inc)}]])

    (hypr/open-window! window)
    (hypr/enter-loop!)))
```

**What's new:**

- `ratom` - Reactive atom that triggers UI updates
- `@click-count` - Dereference to read value
- `swap!` - Update atom value

**Note**: Full reactivity requires more work (reconciliation). For now, the initial render uses the atom value.

## Step 4: Components

Let's extract components:

```clojure
(ns hello
  (:require [hyprclj.core :as hypr]
            [hyprclj.dsl :refer [mount! ratom defcomponent]]))

(def click-count (ratom 0))

;; Define a component
(defcomponent counter-display []
  [:text {:content (str "Clicks: " @click-count)
          :font-size 32}])

(defcomponent click-button []
  [:button {:label "Click me!"
            :size [150 50]
            :on-click #(swap! click-count inc)}])

(defn -main []
  (hypr/create-backend!)
  (let [window (hypr/create-window {:title "Counter"})]

    ;; Use components with [component-name]
    (mount! (hypr/root-element window)
            [:column {:gap 20 :margin 30}
             [counter-display]
             [click-button]])

    (hypr/open-window! window)
    (hypr/enter-loop!)))
```

**What's new:**

- `defcomponent` - Define reusable UI components
- `[counter-display]` - Use component in Hiccup

## Step 5: Props

Components can accept properties:

```clojure
(defcomponent labeled-button [{:keys [label on-click]}]
  [:button {:label label
            :size [150 50]
            :on-click on-click}])

;; Usage:
[labeled-button {:label "Click me!"
                 :on-click #(swap! click-count inc)}]
```

## Step 6: TODO App

Let's build something useful:

```clojure
(ns todo-app
  (:require [hyprclj.core :as hypr]
            [hyprclj.dsl :refer [mount! ratom defcomponent]]))

;; State
(def todos (ratom []))
(def input-text (ratom ""))

;; Components
(defcomponent todo-item [{:keys [text index]}]
  [:row {:gap 10 :margin [5 0]}
   [:text {:content (str (inc index) ". " text)}]
   [:button {:label "✗"
             :size [30 30]
             :on-click #(swap! todos
                               (fn [ts]
                                 (vec (concat (subvec ts 0 index)
                                            (subvec ts (inc index))))))}]])

(defcomponent todo-list []
  [:column {:gap 10 :margin 30}
   [:text {:content "My TODO List"
           :font-size 24}]

   ;; List of todos
   (into [:column {:gap 5}]
         (map-indexed
          (fn [idx todo]
            [todo-item {:text todo :index idx}])
          @todos))

   ;; Add button
   [:button {:label "Add TODO"
             :size [120 40]
             :margin [10 0 0 0]
             :on-click #(when (seq @input-text)
                          (swap! todos conj @input-text)
                          (reset! input-text ""))}]])

(defn -main []
  (hypr/create-backend!)
  (let [window (hypr/create-window
                {:title "TODO App"
                 :size [400 500]})]

    (mount! (hypr/root-element window)
            [todo-list])

    (hypr/open-window! window)
    (hypr/enter-loop!)))
```

**What's new:**

- Multiple atoms for different state
- Dynamic list rendering with `map-indexed`
- `into` to build dynamic Hiccup
- Nested component composition

## Step 7: Timers

Add a timer:

```clojure
(def elapsed (ratom 0))

;; In -main, after creating backend:
(hypr/add-timer! 1000
                 (fn []
                   (swap! elapsed inc)
                   ;; Re-schedule
                   (hypr/add-timer! 1000 #(swap! elapsed inc))))

;; Display in UI:
[:text {:content (str "Elapsed: " @elapsed "s")}]
```

## Best Practices

### 1. Component Design

✅ **Good** - Small, focused components:
```clojure
(defcomponent header [{:keys [title]}]
  [:text {:content title :font-size 24}])

(defcomponent app []
  [:column {}
   [header {:title "My App"}]
   [content]
   [footer]])
```

❌ **Bad** - Monolithic components:
```clojure
(defcomponent app []
  [:column {}
   [:text {...}]
   [:column {...}
    [:row {...}
     ;; 100 lines of nested UI...
     ]]])
```

### 2. State Management

✅ **Good** - Separate state:
```clojure
(def ui-state (ratom {:count 0 :input ""}))
(def app-data (ratom {:users [] :posts []}))
```

❌ **Bad** - Mixed concerns:
```clojure
(def everything (ratom {:count 0 :users [] :window-visible true}))
```

### 3. Event Handlers

✅ **Good** - Named functions:
```clojure
(defn handle-submit! [data]
  (swap! todos conj data))

[:button {:on-click #(handle-submit! @input)}]
```

❌ **Bad** - Inline logic:
```clojure
[:button {:on-click #(swap! todos conj
                            (do
                              (println "Adding...")
                              (process-input @input)))}]
```

## Next Steps

- Check out `examples/demo.clj` for more features
- Read `DEVELOPMENT.md` to understand internals
- Try adding new element types
- Experiment with layouts

## Common Patterns

### Modal Dialog

```clojure
(def show-modal? (ratom false))

(defcomponent modal [{:keys [title content]}]
  (when @show-modal?
    [:column {:margin 50}
     [:text {:content title :font-size 20}]
     [:text {:content content}]
     [:button {:label "Close"
               :on-click #(reset! show-modal? false)}]]))
```

### Form

```clojure
(def form-data (ratom {:name "" :email ""}))

(defcomponent form []
  [:column {:gap 10}
   [:text "Name:"]
   ;; Note: textbox not implemented yet
   [:text "Email:"]
   [:button {:label "Submit"
             :on-click #(submit-form! @form-data)}]])
```

### List with Selection

```clojure
(def selected-idx (ratom nil))

(defcomponent selectable-item [{:keys [text index]}]
  [:button {:label text
            :no-border (not= index @selected-idx)
            :on-click #(reset! selected-idx index)}])
```

## Troubleshooting

**Q: UI doesn't update when atom changes**

A: Full reactivity requires reconciliation (not yet implemented). For now, only initial render uses atom values. Use `remount!` to manually update.

**Q: Window doesn't appear**

A: Make sure you called `open-window!` before `enter-loop!`.

**Q: Crash on exit**

A: This is normal during development. The JVM sometimes doesn't clean up native resources gracefully.

**Q: Element not rendering**

A: Check that:
- Element is added to parent
- Parent is added to window root
- `mount!` was called

## Resources

- [README.md](README.md) - Main documentation
- [examples/](examples/) - Example applications
- [DEVELOPMENT.md](DEVELOPMENT.md) - Developer guide
- [Hyprtoolkit](https://github.com/hyprwm/hyprtoolkit) - Underlying library
