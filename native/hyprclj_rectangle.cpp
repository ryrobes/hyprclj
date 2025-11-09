#include <jni.h>
#include <hyprtoolkit/element/Rectangle.hpp>
#include <hyprtoolkit/palette/Color.hpp>
#include <hyprutils/math/Vector2D.hpp>

using namespace Hyprtoolkit;
using Hyprutils::Math::Vector2D;

extern "C" {

JNIEXPORT jlong JNICALL
Java_org_hyprclj_bindings_Rectangle_00024Builder_nativeCreate(
    JNIEnv* env, jclass clazz,
    jint r, jint g, jint b, jint a,
    jint borderR, jint borderG, jint borderB, jint borderA,
    jint borderThickness, jint rounding,
    jint width, jint height) {

    try {
        auto builder = CRectangleBuilder::begin();

        // Set background color
        builder->color([r, g, b, a]() {
            return CHyprColor{(float)r / 255.0f, (float)g / 255.0f,
                             (float)b / 255.0f, (float)a / 255.0f};
        });

        // Set border color if thickness > 0
        if (borderThickness > 0) {
            builder->borderColor([borderR, borderG, borderB, borderA]() {
                return CHyprColor{(float)borderR / 255.0f, (float)borderG / 255.0f,
                                 (float)borderB / 255.0f, (float)borderA / 255.0f};
            });
            builder->borderThickness(borderThickness);
        }

        // Set rounding
        if (rounding > 0) {
            builder->rounding(rounding);
        }

        // Set size if specified
        if (width > 0 && height > 0) {
            builder->size(CDynamicSize(CDynamicSize::HT_SIZE_ABSOLUTE,
                                      CDynamicSize::HT_SIZE_ABSOLUTE,
                                      Vector2D{(double)width, (double)height}));
        }

        auto rect = builder->commence();
        if (!rect) {
            return 0;
        }

        return reinterpret_cast<jlong>(new auto(rect));
    } catch (const std::exception& e) {
        return 0;
    }
}

} // extern "C"
