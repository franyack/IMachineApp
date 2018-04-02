cmake_minimum_required(VERSION 3.4.1)

set(CMAKE_VERBOSE_MAKEFILE on)
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=gnu++11")

set(OpenCV_DIR ${OPENCV_ANDROID_SDK}/sdk/native/jni)
find_package (OpenCV REQUIRED)

add_library( native-lib SHARED src/main/cpp/native-lib.cpp )

include_directories(src/main/cpp/include)

target_link_libraries(native-lib ${OpenCV_LIBS})