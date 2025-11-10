#include <jni.h>
#include <hyprtoolkit/element/Line.hpp>
#include <hyprtoolkit/palette/Color.hpp>
#include <hyprutils/math/Vector2D.hpp>
#include <vector>

using namespace Hyprtoolkit;
using Hyprutils::Math::Vector2D;

extern "C" {

JNIEXPORT jlong JNICALL
Java_org_hyprclj_bindings_Line_00024Builder_nativeCreate(
    JNIEnv* env, jclass clazz,
    jint r, jint g, jint b, jint a,
    jint thickness,
    jdoubleArray flatPoints,
    jint width, jint height) {

    try {
        auto builder = CLineBuilder::begin();

        // Set color
        builder->color([r, g, b, a]() {
            return CHyprColor{(float)r / 255.0f, (float)g / 255.0f,
                             (float)b / 255.0f, (float)a / 255.0f};
        });

        // Set thickness
        builder->thick(thickness);

        // Convert Java points array to vector<Vector2D>
        jsize len = env->GetArrayLength(flatPoints);
        jdouble* coords = env->GetDoubleArrayElements(flatPoints, nullptr);

        std::vector<Vector2D> points;
        for (int i = 0; i < len; i += 2) {
            points.push_back(Vector2D{coords[i], coords[i+1]});
        }

        env->ReleaseDoubleArrayElements(flatPoints, coords, 0);

        builder->points(std::move(points));

        // Set size if specified
        if (width > 0 && height > 0) {
            builder->size(CDynamicSize(CDynamicSize::HT_SIZE_ABSOLUTE,
                                      CDynamicSize::HT_SIZE_ABSOLUTE,
                                      Vector2D{(double)width, (double)height}));
        }

        auto line = builder->commence();
        if (!line) {
            return 0;
        }

        return reinterpret_cast<jlong>(new auto(line));
    } catch (const std::exception& e) {
        return 0;
    }
}

} // extern "C"
