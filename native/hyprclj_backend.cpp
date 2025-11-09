#include <jni.h>
#include <hyprtoolkit/core/Backend.hpp>
#include <hyprutils/math/Vector2D.hpp>
#include <memory>
#include <functional>

using namespace Hyprtoolkit;
using Hyprutils::Math::Vector2D;

// Helper to store Java VM for callbacks
static JavaVM* g_jvm = nullptr;

// Get JNI env for current thread
JNIEnv* getEnv() {
    JNIEnv* env;
    g_jvm->AttachCurrentThread((void**)&env, nullptr);
    return env;
}

// Store the Java VM when library loads
extern "C" JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved) {
    g_jvm = vm;
    return JNI_VERSION_1_8;
}

// Backend implementation
extern "C" {

JNIEXPORT jlong JNICALL
Java_org_hyprclj_bindings_Backend_nativeCreate(JNIEnv* env, jclass clazz) {
    try {
        auto backend = IBackend::create();
        if (!backend) {
            return 0;
        }
        // Store as CSharedPointer
        return reinterpret_cast<jlong>(new Hyprutils::Memory::CSharedPointer<IBackend>(backend));
    } catch (const std::exception& e) {
        return 0;
    }
}

JNIEXPORT void JNICALL
Java_org_hyprclj_bindings_Backend_nativeEnterLoop(JNIEnv* env, jobject obj, jlong handle) {
    auto backend = *reinterpret_cast<Hyprutils::Memory::CSharedPointer<IBackend>*>(handle);
    if (backend) {
        backend->enterLoop();
    }
}

JNIEXPORT void JNICALL
Java_org_hyprclj_bindings_Backend_nativeAddTimer(
    JNIEnv* env, jobject obj, jlong handle, jint timeoutMs, jobject callback) {

    auto backend = *reinterpret_cast<Hyprutils::Memory::CSharedPointer<IBackend>*>(handle);
    if (!backend) return;

    // Create a global reference to the callback
    jobject globalCallback = env->NewGlobalRef(callback);

    backend->addTimer(std::chrono::milliseconds(timeoutMs),
                      [globalCallback](auto timer, void* data) {
                          JNIEnv* env = getEnv();
                          jclass runnableClass = env->FindClass("java/lang/Runnable");
                          jmethodID runMethod = env->GetMethodID(runnableClass, "run", "()V");
                          env->CallVoidMethod(globalCallback, runMethod);
                      },
                      nullptr, false);
}

JNIEXPORT void JNICALL
Java_org_hyprclj_bindings_Backend_nativeAddIdle(
    JNIEnv* env, jobject obj, jlong handle, jobject callback) {

    auto backend = *reinterpret_cast<Hyprutils::Memory::CSharedPointer<IBackend>*>(handle);
    if (!backend) return;

    jobject globalCallback = env->NewGlobalRef(callback);

    backend->addIdle([globalCallback]() {
        JNIEnv* env = getEnv();
        jclass runnableClass = env->FindClass("java/lang/Runnable");
        jmethodID runMethod = env->GetMethodID(runnableClass, "run", "()V");
        env->CallVoidMethod(globalCallback, runMethod);
    });
}

JNIEXPORT void JNICALL
Java_org_hyprclj_bindings_Backend_nativeDestroy(JNIEnv* env, jobject obj, jlong handle) {
    auto ptr = reinterpret_cast<Hyprutils::Memory::CSharedPointer<IBackend>*>(handle);
    if (ptr) {
        (*ptr)->destroy();
        delete ptr;
    }
}

} // extern "C"
