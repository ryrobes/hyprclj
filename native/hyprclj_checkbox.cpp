#include <jni.h>
#include <hyprtoolkit/element/Checkbox.hpp>
#include <hyprutils/math/Vector2D.hpp>
#include <string>

using namespace Hyprtoolkit;
using Hyprutils::Math::Vector2D;

extern JavaVM* g_jvm;
extern JNIEnv* getEnv();

extern "C" {

JNIEXPORT jlong JNICALL
Java_org_hyprclj_bindings_Checkbox_00024Builder_nativeCreate(
    JNIEnv* env, jclass clazz, jstring label, jboolean checked, jobject callback) {

    try {
        const char* labelChars = env->GetStringUTFChars(label, nullptr);
        std::string labelStr(labelChars);
        env->ReleaseStringUTFChars(label, labelChars);

        auto builder = CCheckboxBuilder::begin();
        builder->toggled(checked);

        // Wire up onToggled callback if provided
        if (callback != nullptr) {
            jobject globalCallback = env->NewGlobalRef(callback);

            builder->onToggled([globalCallback](Hyprutils::Memory::CSharedPointer<CCheckboxElement> self, bool toggled) {
                JNIEnv* env = getEnv();

                // Call Java Consumer with the new state
                jclass consumerClass = env->FindClass("java/util/function/Consumer");
                jmethodID acceptMethod = env->GetMethodID(consumerClass, "accept", "(Ljava/lang/Object;)V");

                // Box the boolean
                jclass boolClass = env->FindClass("java/lang/Boolean");
                jmethodID valueOfMethod = env->GetStaticMethodID(boolClass, "valueOf", "(Z)Ljava/lang/Boolean;");
                jobject boxedBool = env->CallStaticObjectMethod(boolClass, valueOfMethod, (jboolean)toggled);

                env->CallVoidMethod(globalCallback, acceptMethod, boxedBool);
            });
        }

        auto checkbox = builder->commence();
        if (!checkbox) {
            return 0;
        }

        return reinterpret_cast<jlong>(new Hyprutils::Memory::CSharedPointer<IElement>(checkbox));
    } catch (const std::exception& e) {
        return 0;
    }
}

JNIEXPORT jboolean JNICALL
Java_org_hyprclj_bindings_Checkbox_nativeGetChecked(
    JNIEnv* env, jobject obj, jlong handle) {

    // For POC, getting checkbox state not implemented
    return false;
}

JNIEXPORT void JNICALL
Java_org_hyprclj_bindings_Checkbox_nativeSetChecked(
    JNIEnv* env, jobject obj, jlong handle, jboolean checked) {

    // For POC, setting checkbox state not fully implemented
    // Would require rebuilding the checkbox
}

} // extern "C"
