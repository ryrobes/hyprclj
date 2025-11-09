package org.hyprclj.bindings;

/**
 * Vertical column layout.
 */
public class ColumnLayout extends Element {

    private ColumnLayout(long handle) {
        super(handle);
    }

    public static class Builder {
        private int gap = 0;
        private int width = -1;  // -1 means auto
        private int height = -1;

        public Builder gap(int gap) {
            this.gap = gap;
            return this;
        }

        public Builder size(int width, int height) {
            this.width = width;
            this.height = height;
            return this;
        }

        public ColumnLayout build() {
            long handle = nativeCreate(gap, width, height);
            if (handle == 0) {
                throw new RuntimeException("Failed to create column layout");
            }
            return new ColumnLayout(handle);
        }

        private static native long nativeCreate(int gap, int width, int height);
    }

    public static Builder builder() {
        return new Builder();
    }

    static {
        System.loadLibrary("hyprclj");
    }
}
