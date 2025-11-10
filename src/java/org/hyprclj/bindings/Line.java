package org.hyprclj.bindings;

/**
 * Line element - multi-point connected line (like SVG polyline).
 */
public class Line extends Element {

    private Line(long handle) {
        super(handle);
    }

    public static class Builder {
        private int r = 255, g = 255, b = 255, a = 255;  // Default white
        private int thickness = 1;
        private double[][] points = new double[0][0];  // [x,y] pairs
        private int width = -1;
        private int height = -1;

        public Builder color(int r, int g, int b, int a) {
            this.r = r;
            this.g = g;
            this.b = b;
            this.a = a;
            return this;
        }

        public Builder thick(int thickness) {
            this.thickness = thickness;
            return this;
        }

        /**
         * Set line points as array of [x, y] coordinates.
         * Points are in normalized 0-1 range relative to size.
         */
        public Builder points(double[][] points) {
            this.points = points;
            return this;
        }

        public Builder size(int width, int height) {
            this.width = width;
            this.height = height;
            return this;
        }

        public Line build() {
            // Flatten points for native call
            double[] flatPoints = new double[points.length * 2];
            for (int i = 0; i < points.length; i++) {
                flatPoints[i * 2] = points[i][0];
                flatPoints[i * 2 + 1] = points[i][1];
            }

            long handle = nativeCreate(r, g, b, a, thickness, flatPoints, width, height);
            if (handle == 0) {
                throw new RuntimeException("Failed to create line");
            }
            return new Line(handle);
        }

        private static native long nativeCreate(
            int r, int g, int b, int a,
            int thickness,
            double[] flatPoints,  // [x1, y1, x2, y2, ...]
            int width, int height
        );
    }

    public static Builder builder() {
        return new Builder();
    }

    static {
        System.loadLibrary("hyprclj");
    }
}
