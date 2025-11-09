#include <jni.h>
#include <hyprtoolkit/element/ColumnLayout.hpp>
#include <hyprtoolkit/element/RowLayout.hpp>
#include <hyprutils/math/Vector2D.hpp>

using namespace Hyprtoolkit;
using Hyprutils::Math::Vector2D;

extern "C" {

// ColumnLayout
JNIEXPORT jlong JNICALL
Java_org_hyprclj_bindings_ColumnLayout_00024Builder_nativeCreate(
    JNIEnv* env, jclass clazz, jint gap, jint width, jint height) {

    try {
        auto builder = CColumnLayoutBuilder::begin();
        builder->gap(gap);

        if (width > 0 && height > 0) {
            builder->size(CDynamicSize(CDynamicSize::HT_SIZE_ABSOLUTE,
                                      CDynamicSize::HT_SIZE_ABSOLUTE,
                                      Vector2D{(double)width, (double)height}));
        }

        auto layout = builder->commence();
        if (!layout) {
            return 0;
        }

        return reinterpret_cast<jlong>(new auto(layout));
    } catch (const std::exception& e) {
        return 0;
    }
}

// RowLayout
JNIEXPORT jlong JNICALL
Java_org_hyprclj_bindings_RowLayout_00024Builder_nativeCreate(
    JNIEnv* env, jclass clazz, jint gap, jint width, jint height) {

    try {
        auto builder = CRowLayoutBuilder::begin();
        builder->gap(gap);

        if (width > 0 && height > 0) {
            builder->size(CDynamicSize(CDynamicSize::HT_SIZE_ABSOLUTE,
                                      CDynamicSize::HT_SIZE_ABSOLUTE,
                                      Vector2D{(double)width, (double)height}));
        }

        auto layout = builder->commence();
        if (!layout) {
            return 0;
        }

        return reinterpret_cast<jlong>(new auto(layout));
    } catch (const std::exception& e) {
        return 0;
    }
}

} // extern "C"
