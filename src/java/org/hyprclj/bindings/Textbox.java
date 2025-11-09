package org.hyprclj.bindings;

import java.util.function.Consumer;

/**
 * Text input field.
 */
public class Textbox extends Element {

    private Textbox(long handle) {
        super(handle);
    }

    public static class Builder {
        private String placeholder = "";
        private String initialText = "";
        private int width = 200;
        private int height = 40;
        private Consumer<String> onSubmit;
        private Consumer<String> onChange;

        public Builder placeholder(String placeholder) {
            this.placeholder = placeholder;
            return this;
        }

        public Builder initialText(String text) {
            this.initialText = text;
            return this;
        }

        public Builder size(int width, int height) {
            this.width = width;
            this.height = height;
            return this;
        }

        public Builder onSubmit(Consumer<String> callback) {
            this.onSubmit = callback;
            return this;
        }

        public Builder onChange(Consumer<String> callback) {
            this.onChange = callback;
            return this;
        }

        public Textbox build() {
            long handle = nativeCreate(
                placeholder, initialText, width, height
            );
            if (handle == 0) {
                throw new RuntimeException("Failed to create textbox");
            }
            Textbox textbox = new Textbox(handle);

            if (onSubmit != null) {
                nativeSetSubmitCallback(handle, onSubmit);
            }
            if (onChange != null) {
                nativeSetChangeCallback(handle, onChange);
            }

            return textbox;
        }

        private static native long nativeCreate(
            String placeholder, String initialText, int width, int height
        );
        private static native void nativeSetSubmitCallback(long handle, Consumer<String> callback);
        private static native void nativeSetChangeCallback(long handle, Consumer<String> callback);
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Get the current text content.
     */
    public String getText() {
        return nativeGetText(nativeHandle);
    }

    /**
     * Set the text content.
     */
    public void setText(String text) {
        nativeSetText(nativeHandle, text);
    }

    /**
     * Clear the text.
     */
    public void clear() {
        setText("");
    }

    private native String nativeGetText(long handle);
    private native void nativeSetText(long handle, String text);

    static {
        System.loadLibrary("hyprclj");
    }
}
