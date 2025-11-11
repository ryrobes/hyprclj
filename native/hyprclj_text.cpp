#include <jni.h>
#include <hyprtoolkit/element/Text.hpp>
#include <hyprutils/math/Vector2D.hpp>
#include <string>

using namespace Hyprtoolkit;
using Hyprutils::Math::Vector2D;

extern JavaVM* g_jvm;
extern JNIEnv* getEnv();

extern "C" {

JNIEXPORT jlong JNICALL
Java_org_hyprclj_bindings_Text_00024Builder_nativeCreate(
    JNIEnv* env, jclass clazz,
    jstring content, jint fontSize, jstring fontFamily,
    jint r, jint g, jint b, jint a, jstring align, jfloat alpha) {

    try {
        const char* contentChars = env->GetStringUTFChars(content, nullptr);
        std::string contentStr(contentChars);
        env->ReleaseStringUTFChars(content, contentChars);

        const char* fontFamilyChars = env->GetStringUTFChars(fontFamily, nullptr);
        std::string fontFamilyStr(fontFamilyChars);
        env->ReleaseStringUTFChars(fontFamily, fontFamilyChars);

        const char* alignChars = env->GetStringUTFChars(align, nullptr);
        std::string alignStr(alignChars);
        env->ReleaseStringUTFChars(align, alignChars);

        auto builder = CTextBuilder::begin();
        builder->text(std::move(contentStr));
        builder->fontSize(CFontSize(CFontSize::HT_FONT_ABSOLUTE, (float)fontSize));

        if (!fontFamilyStr.empty()) {
            builder->fontFamily(std::move(fontFamilyStr));
        }

        // Set color as a function returning CHyprColor
        builder->color([r, g, b, a]() {
            return CHyprColor((float)r/255.0f, (float)g/255.0f, (float)b/255.0f, (float)a/255.0f);
        });

        // Set separate alpha multiplier for fade effects
        builder->a(alpha);

        auto text = builder->commence();
        if (!text) {
            return 0;
        }

        return reinterpret_cast<jlong>(new auto(text));
    } catch (const std::exception& e) {
        return 0;
    }
}

JNIEXPORT void JNICALL
Java_org_hyprclj_bindings_Text_nativeSetContent(
    JNIEnv* env, jobject obj, jlong handle, jstring content) {

    auto element = *reinterpret_cast<Hyprutils::Memory::CSharedPointer<IElement>*>(handle);
    if (!element) return;

    const char* contentChars = env->GetStringUTFChars(content, nullptr);
    std::string contentStr(contentChars);
    env->ReleaseStringUTFChars(content, contentChars);

    // Note: Updating text content dynamically may require rebuilding the element
    // This is a simplified stub
    // Note: This is simplified
}

JNIEXPORT void JNICALL
Java_org_hyprclj_bindings_Text_nativeSetFontSize(
    JNIEnv* env, jobject obj, jlong handle, jint fontSize) {

    auto element = *reinterpret_cast<Hyprutils::Memory::CSharedPointer<IElement>*>(handle);
    if (!element) return;

    // Note: Updating font size dynamically may require rebuilding the element
    // This is a simplified stub
}

} // extern "C"
