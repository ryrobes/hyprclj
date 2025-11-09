cmake_minimum_required(VERSION 3.20)
project(hyprclj)

set(CMAKE_CXX_STANDARD 23)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Find JNI
find_package(JNI REQUIRED)
include_directories(${JNI_INCLUDE_DIRS})

# Find hyprtoolkit
find_package(PkgConfig REQUIRED)
pkg_check_modules(HYPRTOOLKIT REQUIRED hyprtoolkit)
pkg_check_modules(PIXMAN REQUIRED pixman-1)
pkg_check_modules(HYPRUTILS REQUIRED hyprutils)
pkg_check_modules(LIBDRM REQUIRED libdrm)

include_directories(${HYPRTOOLKIT_INCLUDE_DIRS})
include_directories(${PIXMAN_INCLUDE_DIRS})
include_directories(${HYPRUTILS_INCLUDE_DIRS})
include_directories(${LIBDRM_INCLUDE_DIRS})
link_directories(${HYPRTOOLKIT_LIBRARY_DIRS})

# Source files
set(SOURCES
    hyprclj_backend.cpp
    hyprclj_window.cpp
    hyprclj_element.cpp
    hyprclj_button.cpp
    hyprclj_text.cpp
    hyprclj_layouts.cpp
    hyprclj_textbox.cpp
    hyprclj_checkbox.cpp
    hyprclj_rectangle.cpp
    hyprclj_scrollarea.cpp
)

# Create shared library
add_library(hyprclj SHARED ${SOURCES})

# Link libraries
target_link_libraries(hyprclj
    ${JNI_LIBRARIES}
    ${HYPRTOOLKIT_LIBRARIES}
    ${PIXMAN_LIBRARIES}
    ${HYPRUTILS_LIBRARIES}
    ${LIBDRM_LIBRARIES}
)

# Set output directory
set_target_properties(hyprclj PROPERTIES
    LIBRARY_OUTPUT_DIRECTORY ${CMAKE_SOURCE_DIR}/../resources
)

# Install rules
install(TARGETS hyprclj
    LIBRARY DESTINATION lib
)
