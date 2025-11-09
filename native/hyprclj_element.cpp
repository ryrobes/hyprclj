#include <jni.h>
#include <hyprtoolkit/element/Element.hpp>
#include <hyprtoolkit/element/Button.hpp>
#include <hyprtoolkit/element/Text.hpp>
#include <hyprtoolkit/element/ColumnLayout.hpp>
#include <hyprtoolkit/element/RowLayout.hpp>
#include <hyprutils/math/Vector2D.hpp>

using namespace Hyprtoolkit;
using Hyprutils::Math::Vector2D;

extern JavaVM* g_jvm;
extern JNIEnv* getEnv();

extern "C" {

// Element base class
JNIEXPORT void JNICALL
Java_org_hyprclj_bindings_Element_nativeAddChild(
    JNIEnv* env, jobject obj, jlong handle, jlong childHandle) {

    auto element = *reinterpret_cast<Hyprutils::Memory::CSharedPointer<IElement>*>(handle);
    auto child = *reinterpret_cast<Hyprutils::Memory::CSharedPointer<IElement>*>(childHandle);

    if (element && child) {
        element->addChild(child);
    }
}

JNIEXPORT void JNICALL
Java_org_hyprclj_bindings_Element_nativeRemoveChild(
    JNIEnv* env, jobject obj, jlong handle, jlong childHandle) {

    auto element = *reinterpret_cast<Hyprutils::Memory::CSharedPointer<IElement>*>(handle);
    auto child = *reinterpret_cast<Hyprutils::Memory::CSharedPointer<IElement>*>(childHandle);

    if (element && child) {
        element->removeChild(child);
    }
}

JNIEXPORT void JNICALL
Java_org_hyprclj_bindings_Element_nativeClearChildren(
    JNIEnv* env, jobject obj, jlong handle) {

    auto element = *reinterpret_cast<Hyprutils::Memory::CSharedPointer<IElement>*>(handle);
    if (element) {
        element->clearChildren();
    }
}

JNIEXPORT void JNICALL
Java_org_hyprclj_bindings_Element_nativeSetMargin(
    JNIEnv* env, jobject obj, jlong handle,
    jint top, jint right, jint bottom, jint left) {

    auto element = *reinterpret_cast<Hyprutils::Memory::CSharedPointer<IElement>*>(handle);
    if (element) {
        // setMargin only takes a single float in hyprtoolkit
        // Use the average or just top for simplicity
        element->setMargin((float)top);
    }
}

JNIEXPORT void JNICALL
Java_org_hyprclj_bindings_Element_nativeSetGrow(
    JNIEnv* env, jobject obj, jlong handle, jboolean grow) {

    auto element = *reinterpret_cast<Hyprutils::Memory::CSharedPointer<IElement>*>(handle);
    if (element) {
        element->setGrow(grow);
    }
}

JNIEXPORT void JNICALL
Java_org_hyprclj_bindings_Element_nativeSetMouseClick(
    JNIEnv* env, jobject obj, jlong handle, jobject callback) {

    auto element = *reinterpret_cast<Hyprutils::Memory::CSharedPointer<IElement>*>(handle);
    if (!element) return;

    element->setReceivesMouse(true);
    jobject globalCallback = env->NewGlobalRef(callback);

    element->setMouseButton([globalCallback](Input::eMouseButton button, bool pressed) {
        if (!pressed) return;  // Only fire on press

        JNIEnv* env = getEnv();
        jclass consumerClass = env->FindClass("java/util/function/Consumer");
        jmethodID acceptMethod = env->GetMethodID(consumerClass, "accept", "(Ljava/lang/Object;)V");

        // Create MouseEvent object
        jclass mouseEventClass = env->FindClass("org/hyprclj/bindings/Element$MouseEvent");
        jmethodID constructor = env->GetMethodID(mouseEventClass, "<init>", "(DDI)V");
        jobject mouseEvent = env->NewObject(mouseEventClass, constructor, 0.0, 0.0, (jint)button);

        env->CallVoidMethod(globalCallback, acceptMethod, mouseEvent);
    });
}

JNIEXPORT void JNICALL
Java_org_hyprclj_bindings_Element_nativeSetMouseEnter(
    JNIEnv* env, jobject obj, jlong handle, jobject callback) {

    auto element = *reinterpret_cast<Hyprutils::Memory::CSharedPointer<IElement>*>(handle);
    if (!element) return;

    element->setReceivesMouse(true);
    jobject globalCallback = env->NewGlobalRef(callback);

    element->setMouseEnter([globalCallback](const Vector2D& pos) {
        JNIEnv* env = getEnv();
        jclass consumerClass = env->FindClass("java/util/function/Consumer");
        jmethodID acceptMethod = env->GetMethodID(consumerClass, "accept", "(Ljava/lang/Object;)V");

        jclass mouseEventClass = env->FindClass("org/hyprclj/bindings/Element$MouseEvent");
        jmethodID constructor = env->GetMethodID(mouseEventClass, "<init>", "(DDI)V");
        jobject mouseEvent = env->NewObject(mouseEventClass, constructor, pos.x, pos.y, 0);

        env->CallVoidMethod(globalCallback, acceptMethod, mouseEvent);
    });
}

JNIEXPORT void JNICALL
Java_org_hyprclj_bindings_Element_nativeSetMouseLeave(
    JNIEnv* env, jobject obj, jlong handle, jobject callback) {

    auto element = *reinterpret_cast<Hyprutils::Memory::CSharedPointer<IElement>*>(handle);
    if (!element) return;

    element->setReceivesMouse(true);
    jobject globalCallback = env->NewGlobalRef(callback);

    element->setMouseLeave([globalCallback]() {
        JNIEnv* env = getEnv();
        jclass consumerClass = env->FindClass("java/util/function/Consumer");
        jmethodID acceptMethod = env->GetMethodID(consumerClass, "accept", "(Ljava/lang/Object;)V");

        jclass mouseEventClass = env->FindClass("org/hyprclj/bindings/Element$MouseEvent");
        jmethodID constructor = env->GetMethodID(mouseEventClass, "<init>", "(DDI)V");
        jobject mouseEvent = env->NewObject(mouseEventClass, constructor, 0.0, 0.0, 0);

        env->CallVoidMethod(globalCallback, acceptMethod, mouseEvent);
    });
}

JNIEXPORT void JNICALL
Java_org_hyprclj_bindings_Element_nativeSetGrowBoth(
    JNIEnv* env, jobject obj, jlong handle, jboolean growH, jboolean growV) {

    auto element = *reinterpret_cast<Hyprutils::Memory::CSharedPointer<IElement>*>(handle);
    if (element) {
        element->setGrow(growH, growV);
    }
}

JNIEXPORT void JNICALL
Java_org_hyprclj_bindings_Element_nativeSetAlign(
    JNIEnv* env, jobject obj, jlong handle, jstring align) {

    auto element = *reinterpret_cast<Hyprutils::Memory::CSharedPointer<IElement>*>(handle);
    if (!element) return;

    const char* alignChars = env->GetStringUTFChars(align, nullptr);
    std::string alignStr(alignChars);
    env->ReleaseStringUTFChars(align, alignChars);

    // Map string to position flags
    if (alignStr == "center") {
        element->setPositionFlag(IElement::HT_POSITION_FLAG_CENTER, true);
    } else if (alignStr == "hcenter") {
        element->setPositionFlag(IElement::HT_POSITION_FLAG_HCENTER, true);
    } else if (alignStr == "vcenter") {
        element->setPositionFlag(IElement::HT_POSITION_FLAG_VCENTER, true);
    } else if (alignStr == "left") {
        element->setPositionFlag(IElement::HT_POSITION_FLAG_LEFT, true);
    } else if (alignStr == "right") {
        element->setPositionFlag(IElement::HT_POSITION_FLAG_RIGHT, true);
    } else if (alignStr == "top") {
        element->setPositionFlag(IElement::HT_POSITION_FLAG_TOP, true);
    } else if (alignStr == "bottom") {
        element->setPositionFlag(IElement::HT_POSITION_FLAG_BOTTOM, true);
    }
}

} // extern "C"

