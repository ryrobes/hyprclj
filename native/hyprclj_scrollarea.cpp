#include <jni.h>
#include <hyprtoolkit/element/ScrollArea.hpp>
#include <hyprutils/math/Vector2D.hpp>

using namespace Hyprtoolkit;
using Hyprutils::Math::Vector2D;

extern "C" {

JNIEXPORT jlong JNICALL
Java_org_hyprclj_bindings_ScrollArea_00024Builder_nativeCreate(
    JNIEnv* env, jclass clazz,
    jboolean scrollX, jboolean scrollY, jboolean blockUserScroll,
    jint width, jint height) {

    try {
        auto builder = CScrollAreaBuilder::begin();

        builder->scrollX(scrollX);
        builder->scrollY(scrollY);

        if (blockUserScroll) {
            builder->blockUserScroll(true);
        }

        // Set size if specified
        if (width > 0 && height > 0) {
            builder->size(CDynamicSize(CDynamicSize::HT_SIZE_ABSOLUTE,
                                      CDynamicSize::HT_SIZE_ABSOLUTE,
                                      Vector2D{(double)width, (double)height}));
        }

        auto scrollArea = builder->commence();
        if (!scrollArea) {
            return 0;
        }

        return reinterpret_cast<jlong>(new auto(scrollArea));
    } catch (const std::exception& e) {
        return 0;
    }
}

JNIEXPORT jintArray JNICALL
Java_org_hyprclj_bindings_ScrollArea_nativeGetCurrentScroll(
    JNIEnv* env, jobject obj, jlong handle) {

    auto scrollArea = *reinterpret_cast<Hyprutils::Memory::CSharedPointer<CScrollAreaElement>*>(handle);
    if (!scrollArea) return nullptr;

    auto scroll = scrollArea->getCurrentScroll();

    jintArray result = env->NewIntArray(2);
    jint coords[2] = {(jint)scroll.x, (jint)scroll.y};
    env->SetIntArrayRegion(result, 0, 2, coords);

    return result;
}

JNIEXPORT void JNICALL
Java_org_hyprclj_bindings_ScrollArea_nativeSetScroll(
    JNIEnv* env, jobject obj, jlong handle, jint x, jint y) {

    auto scrollArea = *reinterpret_cast<Hyprutils::Memory::CSharedPointer<CScrollAreaElement>*>(handle);
    if (!scrollArea) return;

    scrollArea->setScroll(Vector2D{(double)x, (double)y});
}

} // extern "C"
