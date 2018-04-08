# IMachineApp
This Android application uses the CIEngine module to automatically manage the photos and images on the device. Then you will be able to manage the result according to your criteria, by moving or erasing images as you want.


## Requirements
You need the OpenCV Android SDK downloaded on your machine to compile the app.

## How to compile
Clone this project and open it in Android Studio. Edit the gradle.properties file and set the OpenCVAndroidSDK variable to the full path to OpenCV Android SDK you downloaded on your system. Compile, run and enjoy the working Android, OpenCV Java and OpenCV C++ integration.

## How to use
Add your C++ files to src/main/cpp and Header files to src/main/cpp/include. Edit app/CMakeLists.txt, adding a line for your library before target_link_libraries like so: add_library(library-name SHARED src/main/cpp/library-source.cpp)

Make sure the following line appears in the app/CMakeLists.txt file, otherwise your header files will not be found. include_directories(src/main/cpp/include)

Finally, add your library to the target_link_libraries clause: target_link_libraries(library-name ${OpenCV_LIBS})

If you wish to call a C++ method from Java, define the native method in java (public native void methodName(java arguments)) and then:

 - use the javah tool (for Android Studio integration see Step 4 in HujiaweiBujidao's blog) on the java file which will generate a nice header file in the jni directory (here is an example for such a file)

 - OR determine the C++ method name by yourself by combining the package, activity and method name

