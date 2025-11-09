package org.hyprclj.bindings;

import java.util.function.Consumer;

/**
 * Checkbox element.
 */
public class Checkbox extends Element {

    private Checkbox(long handle) {
        super(handle);
    }

    public static class Builder {
        private String label = "";
        private boolean checked = false;
        private Consumer<Boolean> onChange;

        public Builder label(String label) {
            this.label = label;
            return this;
        }

        public Builder checked(boolean checked) {
            this.checked = checked;
            return this;
        }

        public Builder onChange(Consumer<Boolean> callback) {
            this.onChange = callback;
            return this;
        }

        public Checkbox build() {
            long handle = nativeCreate(label, checked, onChange);
            if (handle == 0) {
                throw new RuntimeException("Failed to create checkbox");
            }
            return new Checkbox(handle);
        }

        private static native long nativeCreate(String label, boolean checked, Consumer<Boolean> callback);
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Get checked state.
     */
    public boolean isChecked() {
        return nativeGetChecked(nativeHandle);
    }

    /**
     * Set checked state.
     */
    public void setChecked(boolean checked) {
        nativeSetChecked(nativeHandle, checked);
    }

    private native boolean nativeGetChecked(long handle);
    private native void nativeSetChecked(long handle, boolean checked);

    static {
        System.loadLibrary("hyprclj");
    }
}
