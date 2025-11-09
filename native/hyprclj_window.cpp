#include <jni.h>
#include <hyprtoolkit/core/CoreMacros.hpp>  // Must be included first for HT_HIDDEN
#include <hyprtoolkit/window/Window.hpp>
#include <hyprtoolkit/core/Backend.hpp>
#include <hyprutils/math/Vector2D.hpp>
#include <string>

using namespace Hyprtoolkit;
using Hyprutils::Math::Vector2D;

extern JavaVM* g_jvm;
extern JNIEnv* getEnv();

extern "C" {

JNIEXPORT jlong JNICALL
Java_org_hyprclj_bindings_Window_00024Builder_nativeCreate(
    JNIEnv* env, jclass clazz,
    jstring title, jint width, jint height,
    jint minWidth, jint minHeight,
    jint maxWidth, jint maxHeight) {

    try {
        const char* titleChars = env->GetStringUTFChars(title, nullptr);
        std::string titleStr(titleChars);
        env->ReleaseStringUTFChars(title, titleChars);

        auto builder = CWindowBuilder::begin();

        builder->appTitle(std::move(titleStr));
        builder->preferredSize({(double)width, (double)height});

        if (minWidth > 0 && minHeight > 0) {
            builder->minSize({(double)minWidth, (double)minHeight});
        }
        if (maxWidth > 0 && maxHeight > 0) {
            builder->maxSize({(double)maxWidth, (double)maxHeight});
        }

        auto window = builder->commence();
        if (!window) {
            return 0;
        }

        return reinterpret_cast<jlong>(new Hyprutils::Memory::CSharedPointer<IWindow>(window));
    } catch (const std::exception& e) {
        return 0;
    }
}

JNIEXPORT jlong JNICALL
Java_org_hyprclj_bindings_Window_00024Builder_nativeSetCloseCallback(
    JNIEnv* env, jclass clazz, jlong handle, jobject callback) {

    auto window = *reinterpret_cast<Hyprutils::Memory::CSharedPointer<IWindow>*>(handle);
    if (!window) return 0;

    jobject globalCallback = env->NewGlobalRef(callback);

    // closeRequest is a Signal, use listen() to register callback
    // Store the listener to prevent it from being destroyed
    auto listener = window->m_events.closeRequest.listen([globalCallback]() {
        JNIEnv* env = getEnv();

        // Call the Java callback - Java will handle exit
        jclass runnableClass = env->FindClass("java/lang/Runnable");
        jmethodID runMethod = env->GetMethodID(runnableClass, "run", "()V");
        env->CallVoidMethod(globalCallback, runMethod);

        // Don't close the window here - System/exit will clean everything up
    });

    // Return the listener handle so it stays alive
    return reinterpret_cast<jlong>(new Hyprutils::Memory::CSharedPointer<Hyprutils::Signal::CSignalListener>(listener));
}

JNIEXPORT jlong JNICALL
Java_org_hyprclj_bindings_Window_nativeGetRootElement(
    JNIEnv* env, jobject obj, jlong handle) {

    auto window = *reinterpret_cast<Hyprutils::Memory::CSharedPointer<IWindow>*>(handle);
    if (!window || !window->m_rootElement) {
        return 0;
    }

    return reinterpret_cast<jlong>(new Hyprutils::Memory::CSharedPointer<IElement>(window->m_rootElement));
}

JNIEXPORT void JNICALL
Java_org_hyprclj_bindings_Window_nativeOpen(JNIEnv* env, jobject obj, jlong handle) {
    auto window = *reinterpret_cast<Hyprutils::Memory::CSharedPointer<IWindow>*>(handle);
    if (window) {
        window->open();
    }
}

JNIEXPORT void JNICALL
Java_org_hyprclj_bindings_Window_nativeClose(JNIEnv* env, jobject obj, jlong handle) {
    auto window = *reinterpret_cast<Hyprutils::Memory::CSharedPointer<IWindow>*>(handle);
    if (window) {
        window->close();
    }
}

JNIEXPORT jintArray JNICALL
Java_org_hyprclj_bindings_Window_nativeGetSize(JNIEnv* env, jobject obj, jlong handle) {
    auto window = *reinterpret_cast<Hyprutils::Memory::CSharedPointer<IWindow>*>(handle);
    if (!window) {
        return nullptr;
    }

    auto size = window->pixelSize();
    jintArray result = env->NewIntArray(2);
    jint sizeArr[2] = {(jint)size.x, (jint)size.y};
    env->SetIntArrayRegion(result, 0, 2, sizeArr);
    return result;
}

JNIEXPORT void JNICALL
Java_org_hyprclj_bindings_Window_nativeSetResizeCallback(
    JNIEnv* env, jobject obj, jlong handle, jobject listener) {

    auto window = *reinterpret_cast<Hyprutils::Memory::CSharedPointer<IWindow>*>(handle);
    if (!window) return;

    jobject globalListener = env->NewGlobalRef(listener);

    // The resized signal passes Vector2D size as data - use it directly!
    auto resizeListener = window->m_events.resized.listen([globalListener, window](const Vector2D& newSize) {
        JNIEnv* env = getEnv();

        // Use the size from the signal (this is the actual drawable size!)
        // Also get pixelSize() for comparison
        auto pixSize = window->pixelSize();
        auto scale = window->scale();

        // Debug: print both sizes and scale
        printf("[C++ RESIZE] Signal size = %d x %d, pixelSize = %d x %d, scale = %.2f\n",
               (int)newSize.x, (int)newSize.y,
               (int)pixSize.x, (int)pixSize.y,
               scale);
        fflush(stdout);

        // Find the ResizeListener class and call onResize
        jclass listenerClass = env->FindClass("org/hyprclj/bindings/Window$ResizeListener");
        jmethodID onResizeMethod = env->GetMethodID(listenerClass, "onResize", "(II)V");

        // Pass the size from the signal (the actual drawable size)
        env->CallVoidMethod(globalListener, onResizeMethod, (jint)newSize.x, (jint)newSize.y);
    });

    // Store listener to prevent it from being destroyed
    new Hyprutils::Memory::CSharedPointer<Hyprutils::Signal::CSignalListener>(resizeListener);
}

JNIEXPORT void JNICALL
Java_org_hyprclj_bindings_Window_nativeSetKeyboardCallback(
    JNIEnv* env, jobject obj, jlong handle, jobject listener) {

    auto window = *reinterpret_cast<Hyprutils::Memory::CSharedPointer<IWindow>*>(handle);
    if (!window) return;

    jobject globalListener = env->NewGlobalRef(listener);

    // Wire up keyboard event
    auto keyboardListener = window->m_events.keyboardKey.listen([globalListener](const Input::SKeyboardKeyEvent& event) {
        // Debug: Log that we received a keyboard event
        printf("[C++] Keyboard event: keysym=%d down=%d utf8='%s'\n",
               event.xkbKeysym, event.down, event.utf8.c_str());
        fflush(stdout);

        JNIEnv* env = getEnv();

        // Call Java listener with full event data
        jclass listenerClass = env->FindClass("org/hyprclj/bindings/Window$KeyboardListener");
        jmethodID onKeyMethod = env->GetMethodID(listenerClass, "onKey", "(IZLjava/lang/String;I)V");

        jstring utf8 = env->NewStringUTF(event.utf8.c_str());
        env->CallVoidMethod(globalListener, onKeyMethod,
                           (jint)event.xkbKeysym,
                           (jboolean)event.down,
                           utf8,
                           (jint)event.modMask);
        env->DeleteLocalRef(utf8);
    });

    // Store listener to prevent it from being destroyed
    // (We should track this properly, but for POC just leak it)
    new Hyprutils::Memory::CSharedPointer<Hyprutils::Signal::CSignalListener>(keyboardListener);
}

} // extern "C"

