#include <jni.h>
#include <string>
#include <opencv2/core.hpp>
#include <opencv2/opencv.hpp>

using namespace cv;
using namespace std;

extern "C" {


jstring
Java_com_example_fran_imachineapp_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}


int toGray(Mat img, Mat& gray){
    cvtColor(img,gray,CV_BGR2GRAY);
    if (gray.rows == img.rows and gray.cols == img.cols){
        return 1;
    }else{
        return 0;
    }
}

jint
Java_com_example_fran_imachineapp_MainActivity_imgProcess(JNIEnv *env, jclass , jlong inputMat, jlong imageGray) {
    Mat& mRgb = *(Mat*) inputMat;
    Mat& mGray = *(Mat*) imageGray;
    int conv;
    jint reVal;
    conv = toGray(mRgb,mGray);
    reVal = (jint)conv;
    return  reVal;
}

}
