package org.hyprclj.bindings;

import java.util.function.Consumer;

/**
 * Base class for all UI elements.
 */
public class Element {
    protected long nativeHandle;

    protected Element(long handle) {
        this.nativeHandle = handle;
    }

    /**
     * Add a child element.
     */
    public void addChild(Element child) {
        nativeAddChild(nativeHandle, child.nativeHandle);
    }

    /**
     * Remove a child element.
     */
    public void removeChild(Element child) {
        nativeRemoveChild(nativeHandle, child.nativeHandle);
    }

    /**
     * Clear all children.
     */
    public void clearChildren() {
        nativeClearChildren(nativeHandle);
    }

    /**
     * Set margin around the element.
     */
    public void setMargin(int top, int right, int bottom, int left) {
        nativeSetMargin(nativeHandle, top, right, bottom, left);
    }

    /**
     * Set whether this element should grow to fill available space.
     */
    public void setGrow(boolean grow) {
        nativeSetGrow(nativeHandle, grow);
    }

    /**
     * Set horizontal and vertical grow separately.
     */
    public void setGrow(boolean growH, boolean growV) {
        nativeSetGrowBoth(nativeHandle, growH, growV);
    }

    /**
     * Set alignment using position flags.
     * @param align "center", "left", "right", "top", "bottom", "hcenter", "vcenter"
     */
    public void setAlign(String align) {
        nativeSetAlign(nativeHandle, align);
    }

    /**
     * Set mouse event handlers.
     */
    public void setMouseHandlers(
        Consumer<MouseEvent> onClick,
        Consumer<MouseEvent> onEnter,
        Consumer<MouseEvent> onLeave
    ) {
        if (onClick != null) {
            nativeSetMouseClick(nativeHandle, onClick);
        }
        if (onEnter != null) {
            nativeSetMouseEnter(nativeHandle, onEnter);
        }
        if (onLeave != null) {
            nativeSetMouseLeave(nativeHandle, onLeave);
        }
    }

    public long getNativeHandle() {
        return nativeHandle;
    }

    // Native methods
    private native void nativeAddChild(long handle, long childHandle);
    private native void nativeRemoveChild(long handle, long childHandle);
    private native void nativeClearChildren(long handle);
    private native void nativeSetMargin(long handle, int top, int right, int bottom, int left);
    private native void nativeSetGrow(long handle, boolean grow);
    private native void nativeSetGrowBoth(long handle, boolean growH, boolean growV);
    private native void nativeSetAlign(long handle, String align);
    private native void nativeSetMouseClick(long handle, Consumer<MouseEvent> callback);
    private native void nativeSetMouseEnter(long handle, Consumer<MouseEvent> callback);
    private native void nativeSetMouseLeave(long handle, Consumer<MouseEvent> callback);

    static {
        System.loadLibrary("hyprclj");
    }

    /**
     * Mouse event data.
     */
    public static class MouseEvent {
        public final double x;
        public final double y;
        public final int button;

        public MouseEvent(double x, double y, int button) {
            this.x = x;
            this.y = y;
            this.button = button;
        }
    }
}
