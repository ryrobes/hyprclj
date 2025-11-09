package org.hyprclj.bindings;

import java.util.function.Consumer;

/**
 * Wrapper for Hyprtoolkit IWindow.
 * Represents a window that can contain UI elements.
 */
public class Window {
    private long nativeHandle;
    private Element rootElement;
    private long closeListenerHandle;  // Store close listener to prevent GC

    private Window(long handle) {
        this.nativeHandle = handle;
    }

    /**
     * Builder for creating windows.
     */
    public static class Builder {
        private String title = "Hyprclj Window";
        private int width = 640;
        private int height = 480;
        private int minWidth = -1;
        private int minHeight = -1;
        private int maxWidth = -1;
        private int maxHeight = -1;
        private Consumer<Window> onClose;

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder size(int width, int height) {
            this.width = width;
            this.height = height;
            return this;
        }

        public Builder minSize(int width, int height) {
            this.minWidth = width;
            this.minHeight = height;
            return this;
        }

        public Builder maxSize(int width, int height) {
            this.maxWidth = width;
            this.maxHeight = height;
            return this;
        }

        public Builder onClose(Consumer<Window> callback) {
            this.onClose = callback;
            return this;
        }

        public Window build() {
            long handle = nativeCreate(
                title, width, height,
                minWidth, minHeight,
                maxWidth, maxHeight
            );
            if (handle == 0) {
                throw new RuntimeException("Failed to create window");
            }
            Window window = new Window(handle);
            if (onClose != null) {
                window.closeListenerHandle = nativeSetCloseCallback(handle, () -> onClose.accept(window));
            }
            return window;
        }

        private static native long nativeCreate(
            String title, int width, int height,
            int minWidth, int minHeight,
            int maxWidth, int maxHeight
        );
        private static native long nativeSetCloseCallback(long handle, Runnable callback);
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Get the root element of the window.
     */
    public Element getRootElement() {
        if (rootElement == null) {
            long elementHandle = nativeGetRootElement(nativeHandle);
            rootElement = new Element(elementHandle);
        }
        return rootElement;
    }

    /**
     * Open the window.
     */
    public void open() {
        nativeOpen(nativeHandle);
    }

    /**
     * Close the window.
     */
    public void close() {
        nativeClose(nativeHandle);
    }

    /**
     * Get the window size in pixels.
     */
    public int[] getSize() {
        return nativeGetSize(nativeHandle);
    }

    public long getNativeHandle() {
        return nativeHandle;
    }

    /**
     * Interface for resize event handling.
     */
    public interface ResizeListener {
        void onResize(int width, int height);
    }

    /**
     * Interface for keyboard event handling.
     */
    public interface KeyboardListener {
        void onKey(int keyCode, boolean pressed, String utf8, int modifiers);
    }

    /**
     * Set resize event listener for this window.
     */
    public void setResizeListener(ResizeListener listener) {
        nativeSetResizeCallback(nativeHandle, listener);
    }

    /**
     * Set keyboard event listener for this window.
     */
    public void setKeyboardListener(KeyboardListener listener) {
        nativeSetKeyboardCallback(nativeHandle, listener);
    }

    // Native methods
    private native long nativeGetRootElement(long handle);
    private native void nativeOpen(long handle);
    private native void nativeClose(long handle);
    private native int[] nativeGetSize(long handle);
    private native void nativeSetResizeCallback(long handle, ResizeListener listener);
    private native void nativeSetKeyboardCallback(long handle, KeyboardListener listener);

    static {
        System.loadLibrary("hyprclj");
    }
}
