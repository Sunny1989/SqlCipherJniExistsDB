#include <jni.h>
#include <string>

extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_sqlcipherjniexistsdb_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "TestKey";
    return env->NewStringUTF(hello.c_str());
}
