//
// Created by mxc on 2021/12/28.
//

#ifndef JNIPROJECT_COMM_FUNCTION_H
#define JNIPROJECT_COMM_FUNCTION_H

#include <jni.h>
#include <string>

struct JniMethodInfo;

class AutoBuffer;

jvalue
JNU_CallMethodByName(JNIEnv *env, jobject obj, const char *name, const char *descriptor, ...);

jvalue
JNU_CallStaticMethodByName(JNIEnv *env, jclass clazz, const char *name, const char *descriptor,
                           ...);

jvalue JNU_CallStaticMethodByName(JNIEnv *env, const char *_class_name, const char *name,
                                  const char *descriptor, ...);

jvalue JNU_CallStaticMethodByMethodInfo(JNIEnv *env, JniMethodInfo _method_info, ...);

jvalue JNU_GetStaticField(JNIEnv *env, jclass clazz, const char *name, const char *sig);

jvalue JNU_GetField(JNIEnv *env, jobject obj, const char *name, const char *sig);

jvalue JNU_CallMethodByMethodInfo(JNIEnv *env, jobject obj, JniMethodInfo _method_info, ...);

jbyteArray JNU_Buffer2JbyteArray(JNIEnv *env, const AutoBuffer &ab);

jbyteArray JNU_buffer2JbyteArray(JNIEnv *env, const jbyteArray bytes, size_t _length);

wchar_t *JNU_Jstring2Wachar(JNIEnv *env, const jstring str);

void JNU_FreeWchar(JNIEnv *en, wchar_t *wchar);

jstring JNU_Wstring2Jstring(JNIEnv *env, const std::wstring &wstr);

jstring JNU_Wchar2Jstring(JNIEnv *env, wchar_t *wchar);

jstring JNU_Chars2Jstring(JNIEnv *env, const char *pat);

void JNU_FreeJstring(JNIEnv *env, jstring str);


#endif //JNIPROJECT_COMM_FUNCTION_H