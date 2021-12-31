#include <jni.h>
#include <string>
#include "jni/comm_function.h"
#include "jni/var_cache.h"

extern "C" JNIEXPORT jstring JNICALL
Java_com_mxc_jniproject_MainActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}