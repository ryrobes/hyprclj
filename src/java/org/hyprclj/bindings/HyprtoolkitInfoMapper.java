package org.hyprclj.bindings;

import org.bytedeco.javacpp.*;
import org.bytedeco.javacpp.annotation.*;
import org.bytedeco.javacpp.tools.*;

/**
 * JavaCPP InfoMapper for hyprtoolkit.
 * This tells JavaCPP how to map C++ types to Java.
 */
@Properties(
    value = @Platform(
        include = {
            "hyprtoolkit/core/Backend.hpp",
            "hyprtoolkit/window/Window.hpp",
            "hyprtoolkit/element/Element.hpp",
            "hyprtoolkit/element/Button.hpp",
            "hyprtoolkit/element/Text.hpp",
            "hyprtoolkit/element/Rectangle.hpp",
            "hyprtoolkit/element/ColumnLayout.hpp",
            "hyprtoolkit/element/RowLayout.hpp"
        },
        link = "hyprtoolkit",
        compiler = "cpp17"
    ),
    target = "org.hyprclj.bindings.hyprtoolkit",
    global = "org.hyprclj.bindings.HyprtoolkitGlobal"
)
public class HyprtoolkitInfoMapper implements InfoMapper {

    public void map(InfoMap infoMap) {
        // Map std::string
        infoMap.put(new Info("std::string").annotations("@StdString").valueTypes("String").pointerTypes("@Cast(\"std::string*\") String"))
               .put(new Info("std::shared_ptr").annotations("@SharedPtr"));

        // Map Vector2D (common 2D vector type)
        infoMap.put(new Info("Vector2D").pointerTypes("Vector2D"));

        // Map smart pointers used by hyprtoolkit
        infoMap.put(new Info("Hyprutils::Memory::CSharedPointer").annotations("@SharedPtr"))
               .put(new Info("Hyprutils::Memory::CWeakPointer").annotations("@WeakPtr"))
               .put(new Info("Hyprutils::Memory::CUniquePointer").annotations("@UniquePtr"));

        // Map the main classes
        infoMap.put(new Info("Hyprtoolkit::IBackend").pointerTypes("IBackend"))
               .put(new Info("Hyprtoolkit::IWindow").pointerTypes("IWindow"))
               .put(new Info("Hyprtoolkit::CWindowBuilder").pointerTypes("CWindowBuilder"))
               .put(new Info("Hyprtoolkit::IElement").pointerTypes("IElement"))
               .put(new Info("Hyprtoolkit::CButtonElement").pointerTypes("CButtonElement"))
               .put(new Info("Hyprtoolkit::CButtonBuilder").pointerTypes("CButtonBuilder"))
               .put(new Info("Hyprtoolkit::CTextElement").pointerTypes("CTextElement"))
               .put(new Info("Hyprtoolkit::CTextBuilder").pointerTypes("CTextBuilder"))
               .put(new Info("Hyprtoolkit::CRectangleElement").pointerTypes("CRectangleElement"))
               .put(new Info("Hyprtoolkit::CRectangleBuilder").pointerTypes("CRectangleBuilder"))
               .put(new Info("Hyprtoolkit::CColumnLayoutElement").pointerTypes("CColumnLayoutElement"))
               .put(new Info("Hyprtoolkit::CColumnLayoutBuilder").pointerTypes("CColumnLayoutBuilder"))
               .put(new Info("Hyprtoolkit::CRowLayoutElement").pointerTypes("CRowLayoutElement"))
               .put(new Info("Hyprtoolkit::CRowLayoutBuilder").pointerTypes("CRowLayoutBuilder"));

        // Map enums
        infoMap.put(new Info("eWindowType").enumerate())
               .put(new Info("ePositioningMode").enumerate())
               .put(new Info("ePositionFlags").enumerate());

        // Map callback/signal types (these will need special handling)
        infoMap.put(new Info("Hyprutils::Signal::CSignal").skip());

        // Skip internal implementation details
        infoMap.put(new Info("SElementInternalData").skip())
               .put(new Info("SWindowInternalData").skip());
    }
}
