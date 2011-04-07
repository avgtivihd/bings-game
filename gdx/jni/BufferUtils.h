/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class com_badlogic_gdx_utils_BufferUtils */

#ifndef _Included_com_badlogic_gdx_utils_BufferUtils
#define _Included_com_badlogic_gdx_utils_BufferUtils
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     com_badlogic_gdx_utils_BufferUtils
 * Method:    copyJni
 * Signature: ([FLjava/nio/Buffer;II)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_utils_BufferUtils_copyJni___3FLjava_nio_Buffer_2II
  (JNIEnv *, jclass, jfloatArray, jobject, jint, jint);

/*
 * Class:     com_badlogic_gdx_utils_BufferUtils
 * Method:    copyJni
 * Signature: (Ljava/nio/Buffer;Ljava/nio/Buffer;I)V
 */
JNIEXPORT void JNICALL Java_com_badlogic_gdx_utils_BufferUtils_copyJni__Ljava_nio_Buffer_2Ljava_nio_Buffer_2I
  (JNIEnv *, jclass, jobject, jobject, jint);

/*
 * Class:     com_badlogic_gdx_utils_BufferUtils
 * Method:    int2float
 * Signature: (I)F
 */
JNIEXPORT jfloat JNICALL Java_com_badlogic_gdx_utils_BufferUtils_int2float
  (JNIEnv *, jclass, jint);

/*
 * Class:     com_badlogic_gdx_utils_BufferUtils
 * Method:    float2int
 * Signature: (F)I
 */
JNIEXPORT jint JNICALL Java_com_badlogic_gdx_utils_BufferUtils_float2int
  (JNIEnv *, jclass, jfloat);

/*
 * Class:     com_badlogic_gdx_utils_BufferUtils
 * Method:    bitEqual
 * Signature: (IF)Z
 */
JNIEXPORT jboolean JNICALL Java_com_badlogic_gdx_utils_BufferUtils_bitEqual
  (JNIEnv *, jclass, jint, jfloat);

#ifdef __cplusplus
}
#endif
#endif
