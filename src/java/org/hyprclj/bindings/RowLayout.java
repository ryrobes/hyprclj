package org.hyprclj.bindings;

/**
 * Horizontal row layout.
 */
public class RowLayout extends Element {

    private RowLayout(long handle) {
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

        public RowLayout build() {
            long handle = nativeCreate(gap, width, height);
            if (handle == 0) {
                throw new RuntimeException("Failed to create row layout");
            }
            return new RowLayout(handle);
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
