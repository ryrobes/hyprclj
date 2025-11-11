package org.hyprclj.bindings;

/**
 * Rectangle element for backgrounds and borders.
 */
public class Rectangle extends Element {

    private Rectangle(long handle) {
        super(handle);
    }

    public static class Builder {
        private int r = 255, g = 255, b = 255, a = 255;  // Default white
        private int borderR = 0, borderG = 0, borderB = 0, borderA = 255;  // Default black border
        private int borderThickness = 0;
        private int rounding = 0;
        private int width = -1;
        private int height = -1;
        private float alpha = 1.0f;  // Separate opacity multiplier (0.0-1.0)

        public Builder color(int r, int g, int b, int a) {
            this.r = r;
            this.g = g;
            this.b = b;
            this.a = a;
            return this;
        }

        public Builder borderColor(int r, int g, int b, int a) {
            this.borderR = r;
            this.borderG = g;
            this.borderB = b;
            this.borderA = a;
            return this;
        }

        public Builder borderThickness(int thickness) {
            this.borderThickness = thickness;
            return this;
        }

        public Builder rounding(int rounding) {
            this.rounding = rounding;
            return this;
        }

        public Builder size(int width, int height) {
            this.width = width;
            this.height = height;
            return this;
        }

        public Builder alpha(float alpha) {
            this.alpha = Math.max(0.0f, Math.min(1.0f, alpha));
            return this;
        }

        public Rectangle build() {
            long handle = nativeCreate(
                r, g, b, a,
                borderR, borderG, borderB, borderA,
                borderThickness, rounding,
                width, height, alpha
            );
            if (handle == 0) {
                throw new RuntimeException("Failed to create rectangle");
            }
            return new Rectangle(handle);
        }

        private static native long nativeCreate(
            int r, int g, int b, int a,
            int borderR, int borderG, int borderB, int borderA,
            int borderThickness, int rounding,
            int width, int height, float alpha
        );
    }

    public static Builder builder() {
        return new Builder();
    }

    static {
        System.loadLibrary("hyprclj");
    }
}
