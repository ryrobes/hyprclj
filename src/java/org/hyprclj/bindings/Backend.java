package org.hyprclj.bindings;

/**
 * Wrapper for Hyprtoolkit IBackend.
 * Manages the event loop, timers, and system resources.
 */
public class Backend {
    private long nativeHandle; // C++ pointer
    private static Backend instance;

    private Backend(long handle) {
        this.nativeHandle = handle;
    }

    /**
     * Create the backend instance. Only one per process.
     */
    public static synchronized Backend create() {
        if (instance == null) {
            long handle = nativeCreate();
            if (handle == 0) {
                throw new RuntimeException("Failed to create backend");
            }
            instance = new Backend(handle);
        }
        return instance;
    }

    /**
     * Enter the event loop. Blocks until the application exits.
     */
    public void enterLoop() {
        nativeEnterLoop(nativeHandle);
    }

    /**
     * Add a timer that fires after the specified milliseconds.
     * @param timeoutMs Timeout in milliseconds
     * @param callback Callback to invoke
     */
    public void addTimer(int timeoutMs, Runnable callback) {
        nativeAddTimer(nativeHandle, timeoutMs, callback);
    }

    /**
     * Add an idle callback that runs after pending events.
     * @param callback Callback to invoke
     */
    public void addIdle(Runnable callback) {
        nativeAddIdle(nativeHandle, callback);
    }

    /**
     * Get the native handle (for internal use).
     */
    public long getNativeHandle() {
        return nativeHandle;
    }

    /**
     * Destroy the backend and cleanup resources.
     */
    public void destroy() {
        if (nativeHandle != 0) {
            nativeDestroy(nativeHandle);
            nativeHandle = 0;
            instance = null;
        }
    }

    @Override
    protected void finalize() throws Throwable {
        destroy();
        super.finalize();
    }

    // Native methods
    private static native long nativeCreate();
    private native void nativeEnterLoop(long handle);
    private native void nativeAddTimer(long handle, int timeoutMs, Runnable callback);
    private native void nativeAddIdle(long handle, Runnable callback);
    private native void nativeDestroy(long handle);

    // Load native library
    static {
        System.loadLibrary("hyprclj");
    }
}
