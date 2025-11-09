package org.hyprclj.bindings;

import java.util.function.Consumer;

/**
 * Button element.
 */
public class Button extends Element {

    private Button(long handle) {
        super(handle);
    }

    public static class Builder {
        private String label = "";
        private int width = -1;  // -1 means auto
        private int height = -1;
        private boolean noBorder = false;
        private boolean noBg = false;
        private int fontSize = 12;
        private Consumer<Button> onClick;
        private Consumer<Button> onRightClick;

        public Builder label(String label) {
            this.label = label;
            return this;
        }

        public Builder size(int width, int height) {
            this.width = width;
            this.height = height;
            return this;
        }

        public Builder noBorder(boolean value) {
            this.noBorder = value;
            return this;
        }

        public Builder noBg(boolean value) {
            this.noBg = value;
            return this;
        }

        public Builder fontSize(int size) {
            this.fontSize = size;
            return this;
        }

        public Builder onClick(Consumer<Button> callback) {
            this.onClick = callback;
            return this;
        }

        public Builder onRightClick(Consumer<Button> callback) {
            this.onRightClick = callback;
            return this;
        }

        public Button build() {
            long handle = nativeCreate(
                label, width, height,
                noBorder, noBg, fontSize
            );
            if (handle == 0) {
                throw new RuntimeException("Failed to create button");
            }
            Button button = new Button(handle);

            if (onClick != null) {
                nativeSetClickCallback(handle, () -> onClick.accept(button));
            }
            if (onRightClick != null) {
                nativeSetRightClickCallback(handle, () -> onRightClick.accept(button));
            }

            return button;
        }

        private static native long nativeCreate(
            String label, int width, int height,
            boolean noBorder, boolean noBg, int fontSize
        );
        private static native void nativeSetClickCallback(long handle, Runnable callback);
        private static native void nativeSetRightClickCallback(long handle, Runnable callback);
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Update the button label.
     */
    public void setLabel(String label) {
        nativeSetLabel(nativeHandle, label);
    }

    private native void nativeSetLabel(long handle, String label);

    static {
        System.loadLibrary("hyprclj");
    }
}
