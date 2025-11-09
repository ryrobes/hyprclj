package org.hyprclj.bindings;

/**
 * ScrollArea - scrollable container element.
 */
public class ScrollArea extends Element {

    private ScrollArea(long handle) {
        super(handle);
    }

    /**
     * Get current scroll position.
     * @return [x, y] scroll offsets
     */
    public int[] getCurrentScroll() {
        return nativeGetCurrentScroll(nativeHandle);
    }

    /**
     * Set scroll position programmatically.
     * @param x X scroll offset
     * @param y Y scroll offset
     */
    public void setScroll(int x, int y) {
        nativeSetScroll(nativeHandle, x, y);
    }

    public static class Builder {
        private boolean scrollX = false;
        private boolean scrollY = true;  // Default to vertical scrolling
        private boolean blockUserScroll = false;
        private int width = -1;
        private int height = -1;

        public Builder scrollX(boolean enable) {
            this.scrollX = enable;
            return this;
        }

        public Builder scrollY(boolean enable) {
            this.scrollY = enable;
            return this;
        }

        public Builder blockUserScroll(boolean block) {
            this.blockUserScroll = block;
            return this;
        }

        public Builder size(int width, int height) {
            this.width = width;
            this.height = height;
            return this;
        }

        public ScrollArea build() {
            long handle = nativeCreate(scrollX, scrollY, blockUserScroll, width, height);
            if (handle == 0) {
                throw new RuntimeException("Failed to create scroll area");
            }
            return new ScrollArea(handle);
        }

        private static native long nativeCreate(boolean scrollX, boolean scrollY,
                                                boolean blockUserScroll,
                                                int width, int height);
    }

    public static Builder builder() {
        return new Builder();
    }

    // Native methods
    private native int[] nativeGetCurrentScroll(long handle);
    private native void nativeSetScroll(long handle, int x, int y);

    static {
        System.loadLibrary("hyprclj");
    }
}
