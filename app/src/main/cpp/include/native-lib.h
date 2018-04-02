#include <jni.h>

#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     eu_otselo_sensibleopencv_MainActivity
 * Method:    imgProcess2
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring Java_com_example_fran_imachineapp_MainActivity_stringFromJNI
	(JNIEnv *env, jobject /* this */);

JNIEXPORT jstring Java_com_example_fran_imachineapp_MainActivity_stringFromJNI
	(JNIEnv *env, jobject /* this */);

JNIEXPORT jint Java_com_example_fran_imachineapp_MainActivity_imgProcess
	(JNIEnv *env, jclass , jlong inputMat, jlong imageGray);

JNIEXPORT jobjectArray Java_com_example_fran_imachineapp_Working_imgProcess2
	(JNIEnv *env, jobject ,jobjectArray stringArray);

#ifdef __cplusplus
}
#endif
