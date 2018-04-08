#include "jni.h"
#include "native-lib.h"
#include <string>
#include <opencv2/core.hpp>
#include <opencv2/opencv.hpp>
#include <android/log.h>

using namespace cv;
using namespace std;

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

int GetCluster(){
    //	srand((unsigned)time(0));
    int i;
    i = (rand()%10)+1;
    return i ;
}


//void //jobjectArray JNICALL
//Java_com_example_fran_imachineapp_MainActivity_imgProcess2(JNIEnv *env, jobject ,vector<String> images) {
//    int size = images.size();
//    for (int i=0;i<size;i++){
//        //Hacer algo
//    }
//}
//vector< tuple<String,int>> processingImages(vector<String> imagesPath){
//    vector< tuple<String,int>> results;
//    for (int i=0;i<imagesPath.size();i++){
//        Mat img = imread(imagesPath[i].c_str());
//        int conv;
//        jint reVal;
//        conv = toGray(img,img);
//        reVal = (jint)conv;
//        int cluster = GetCluster();
//        tuple<String,int> result (imagesPath[i].c_str(),cluster);
//        results.push_back(result);
//    }
//    return results;
//};

vector<String> processingImages(vector<String> imagesPath){
    vector<String> results;
    for (int i=0;i<imagesPath.size();i++){
        clock_t start = clock();
        Mat img = imread(imagesPath[i]);
        __android_log_print(ANDROID_LOG_VERBOSE, "Read image", "time = %0f ms", ((clock()-start) / (double)(CLOCKS_PER_SEC / 1000)));
        int conv;
        jint reVal;
        //Para devolver la imagen de manera correcta (en caso de querer rescatarla con algun proceso en java)
        //Se debe definir de la siguiente manera:
        //jlong imageGray;
        //Mat& mGray = *(Mat*) imageGray;
        //conv = toGray(mRgb,mGray);
        start = clock();
        conv = toGray(img,img);
        __android_log_print(ANDROID_LOG_VERBOSE, "Processing image", "time = %0f ms", ((clock()-start) / (double)(CLOCKS_PER_SEC / 1000)));
        //De esta manera podriamos devolver la imagen convertida
        reVal = (jint)conv;
        //return reVal;
        int cluster = GetCluster();
        stringstream ss;
        ss<<cluster;
        string str = ss.str();
        String result = imagesPath[i] + "->" + str;
        results.push_back(result);
    }
    return results;
};

jobjectArray //jobjectArray JNICALL
Java_com_example_fran_imachineapp_Working_imgProcess2(JNIEnv *env, jobject ,jobjectArray stringArray) {
    clock_t start = clock();
    int stringCount = env->GetArrayLength(stringArray);
//    vector< tuple<String,int>> results;
    vector<String> results;
    vector<String> imagesPath;
    for (int i=0; i<stringCount; i++) {
        jstring string = (jstring) (env->GetObjectArrayElement(stringArray, i));
        if (env->ExceptionCheck()) continue;
        const char *rawString = env->GetStringUTFChars(string, 0);
        env->DeleteLocalRef(string);
        imagesPath.push_back((String &&) rawString);
        //vector<string,int> a = new vector<string,int>;
        //a = getClusters(vector<String> imagesPath);
        //getClusters(vector<String> imagesPath, a);
//        int cluster = GetCluster();
//        mMap[rawString] = cluster;

        //cluster 1: 3 images
        //cluster 2: 2 images
    }
    results = processingImages(imagesPath);
    jobjectArray ret;

    ret= (jobjectArray)env->NewObjectArray((int)results.size(),env->FindClass("java/lang/String"),env->NewStringUTF(""));

    for (int i=0; i<results.size(); i++){
        env->SetObjectArrayElement(ret,i,env->NewStringUTF(results[i].c_str()));
    }
    __android_log_print(ANDROID_LOG_VERBOSE, "Process all images", "time = %0f ms", ((clock()-start) / (double)(CLOCKS_PER_SEC / 1000)));
    return(ret);

}


