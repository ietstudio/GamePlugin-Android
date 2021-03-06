/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
#include <android/log.h>
/* Header for class com_joycastle_gameplugin_NativeUtil */

#ifndef _Included_com_joycastle_gameplugin_NativeUtil
#define _Included_com_joycastle_gameplugin_NativeUtil

#define  LOG_TAG    "main"
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)

#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     com_joycastle_gameplugin_NativeUtil
 * Method:    nativeInit
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_joycastle_gameplugin_NativeUtil_invokeCppMethod
  (JNIEnv *env, jclass type, jint responseId, jstring resData_)
{
    const char *resData = env->GetStringUTFChars(resData_, 0);
    LOGD("cpp nativeUtil: %s",resData);
    env->ReleaseStringUTFChars(resData_, resData);
}

#ifdef __cplusplus
}
#endif
#endif
