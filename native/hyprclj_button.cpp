#include <jni.h>
#include <hyprtoolkit/element/Button.hpp>
#include <hyprutils/math/Vector2D.hpp>
#include <string>

using namespace Hyprtoolkit;
using Hyprutils::Math::Vector2D;

extern JavaVM* g_jvm;
extern JNIEnv* getEnv();

extern "C" {

JNIEXPORT jlong JNICALL
Java_org_hyprclj_bindings_Button_00024Builder_nativeCreate(
    JNIEnv* env, jclass clazz,
    jstring label, jint width, jint height,
    jboolean noBorder, jboolean noBg, jint fontSize) {

    try {
        const char* labelChars = env->GetStringUTFChars(label, nullptr);
        std::string labelStr(labelChars);
        env->ReleaseStringUTFChars(label, labelChars);

        auto builder = CButtonBuilder::begin();
        builder->label(std::move(labelStr));
        builder->fontSize(CFontSize(CFontSize::HT_FONT_ABSOLUTE, (float)fontSize));

        if (noBorder) {
            builder->noBorder(true);
        }
        if (noBg) {
            builder->noBg(true);
        }

        if (width > 0 && height > 0) {
            builder->size(CDynamicSize(CDynamicSize::HT_SIZE_ABSOLUTE,
                                      CDynamicSize::HT_SIZE_ABSOLUTE,
                                      Vector2D{(double)width, (double)height}));
        }

        auto button = builder->commence();
        if (!button) {
            return 0;
        }

        return reinterpret_cast<jlong>(new auto(button));
    } catch (const std::exception& e) {
        return 0;
    }
}

JNIEXPORT void JNICALL
Java_org_hyprclj_bindings_Button_00024Builder_nativeSetClickCallback(
    JNIEnv* env, jclass clazz, jlong handle, jobject callback) {

    auto button = *reinterpret_cast<Hyprutils::Memory::CSharedPointer<IElement>*>(handle);
    if (!button) return;

    jobject globalCallback = env->NewGlobalRef(callback);

    button->setReceivesMouse(true);
    button->setMouseButton([globalCallback](Input::eMouseButton btn, bool pressed) {
        if (pressed && btn == Input::MOUSE_BUTTON_LEFT) {
            JNIEnv* env = getEnv();
            jclass runnableClass = env->FindClass("java/lang/Runnable");
            jmethodID runMethod = env->GetMethodID(runnableClass, "run", "()V");
            env->CallVoidMethod(globalCallback, runMethod);
        }
    });
}

JNIEXPORT void JNICALL
Java_org_hyprclj_bindings_Button_00024Builder_nativeSetRightClickCallback(
    JNIEnv* env, jclass clazz, jlong handle, jobject callback) {

    auto button = *reinterpret_cast<Hyprutils::Memory::CSharedPointer<IElement>*>(handle);
    if (!button) return;

    jobject globalCallback = env->NewGlobalRef(callback);

    button->setReceivesMouse(true);
    button->setMouseButton([globalCallback](Input::eMouseButton btn, bool pressed) {
        if (pressed && btn == Input::MOUSE_BUTTON_RIGHT) {
            JNIEnv* env = getEnv();
            jclass runnableClass = env->FindClass("java/lang/Runnable");
            jmethodID runMethod = env->GetMethodID(runnableClass, "run", "()V");
            env->CallVoidMethod(globalCallback, runMethod);
        }
    });
}

JNIEXPORT void JNICALL
Java_org_hyprclj_bindings_Button_nativeSetLabel(
    JNIEnv* env, jobject obj, jlong handle, jstring label) {

    auto element = *reinterpret_cast<Hyprutils::Memory::CSharedPointer<IElement>*>(handle);
    if (!element) return;

    // Note: Dynamic label updates not fully implemented in this POC
    // Would require rebuilding the element and replacing it in parent
}

} // extern "C"
