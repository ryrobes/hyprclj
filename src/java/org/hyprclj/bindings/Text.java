package org.hyprclj.bindings;

/**
 * Text element.
 */
public class Text extends Element {

    private Text(long handle) {
        super(handle);
    }

    public static class Builder {
        private String content = "";
        private int fontSize = 12;
        private String fontFamily = "";
        private int r = 255, g = 255, b = 255, a = 255;  // White by default
        private String align = "left";

        public Builder content(String content) {
            this.content = content;
            return this;
        }

        public Builder fontSize(int size) {
            this.fontSize = size;
            return this;
        }

        public Builder fontFamily(String family) {
            this.fontFamily = family;
            return this;
        }

        public Builder color(int r, int g, int b, int a) {
            this.r = r;
            this.g = g;
            this.b = b;
            this.a = a;
            return this;
        }

        public Builder color(int r, int g, int b) {
            return color(r, g, b, 255);
        }

        public Builder align(String align) {
            this.align = align;
            return this;
        }

        public Text build() {
            long handle = nativeCreate(
                content, fontSize, fontFamily,
                r, g, b, a, align
            );
            if (handle == 0) {
                throw new RuntimeException("Failed to create text element");
            }
            return new Text(handle);
        }

        private static native long nativeCreate(
            String content, int fontSize, String fontFamily,
            int r, int g, int b, int a, String align
        );
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Update the text content.
     */
    public void setContent(String content) {
        nativeSetContent(nativeHandle, content);
    }

    /**
     * Update the font size.
     */
    public void setFontSize(int size) {
        nativeSetFontSize(nativeHandle, size);
    }

    private native void nativeSetContent(long handle, String content);
    private native void nativeSetFontSize(long handle, int size);

    static {
        System.loadLibrary("hyprclj");
    }
}
