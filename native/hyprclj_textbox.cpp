#include <jni.h>
#include <hyprtoolkit/element/Textbox.hpp>
#include <hyprutils/math/Vector2D.hpp>
#include <string>

using namespace Hyprtoolkit;
using Hyprutils::Math::Vector2D;

extern JavaVM* g_jvm;
extern JNIEnv* getEnv();

extern "C" {

JNIEXPORT jlong JNICALL
Java_org_hyprclj_bindings_Textbox_00024Builder_nativeCreate(
    JNIEnv* env, jclass clazz,
    jstring placeholder, jstring initialText,
    jint width, jint height) {

    try {
        const char* placeholderChars = env->GetStringUTFChars(placeholder, nullptr);
        std::string placeholderStr(placeholderChars);
        env->ReleaseStringUTFChars(placeholder, placeholderChars);

        const char* initialChars = env->GetStringUTFChars(initialText, nullptr);
        std::string initialStr(initialChars);
        env->ReleaseStringUTFChars(initialText, initialChars);

        auto builder = CTextboxBuilder::begin();

        if (!placeholderStr.empty()) {
            builder->placeholder(std::move(placeholderStr));
        }

        // Note: Initial text might use a different method or property
        // Skipping for POC - can be set after creation if needed

        if (width > 0 && height > 0) {
            builder->size(CDynamicSize(CDynamicSize::HT_SIZE_ABSOLUTE,
                                      CDynamicSize::HT_SIZE_ABSOLUTE,
                                      Vector2D{(double)width, (double)height}));
        }

        auto textbox = builder->commence();
        if (!textbox) {
            return 0;
        }

        return reinterpret_cast<jlong>(new Hyprutils::Memory::CSharedPointer<IElement>(textbox));
    } catch (const std::exception& e) {
        return 0;
    }
}

JNIEXPORT void JNICALL
Java_org_hyprclj_bindings_Textbox_00024Builder_nativeSetSubmitCallback(
    JNIEnv* env, jclass clazz, jlong handle, jobject callback) {

    auto textbox = *reinterpret_cast<Hyprutils::Memory::CSharedPointer<IElement>*>(handle);
    if (!textbox) return;

    jobject globalCallback = env->NewGlobalRef(callback);

    // For POC, submit callback is not fully implemented
    // Would need to access textbox internal state and wire up submit event
    // Stub for now
}

JNIEXPORT void JNICALL
Java_org_hyprclj_bindings_Textbox_00024Builder_nativeSetChangeCallback(
    JNIEnv* env, jclass clazz, jlong handle, jobject callback) {

    auto textbox = *reinterpret_cast<Hyprutils::Memory::CSharedPointer<IElement>*>(handle);
    if (!textbox) return;

    jobject globalCallback = env->NewGlobalRef(callback);

    // Change callbacks would need Hyprtoolkit support
    // For POC, we'll skip this
}

JNIEXPORT jstring JNICALL
Java_org_hyprclj_bindings_Textbox_nativeGetText(
    JNIEnv* env, jobject obj, jlong handle) {

    // For POC, getting text from textbox is not implemented
    // Would need access to internal textbox state
    // Return empty string for now
    return env->NewStringUTF("");
}

JNIEXPORT void JNICALL
Java_org_hyprclj_bindings_Textbox_nativeSetText(
    JNIEnv* env, jobject obj, jlong handle, jstring text) {

    // For POC, setting text on textbox is not implemented
    // Would require rebuilding the textbox element
    // Stub for now
}

} // extern "C"
