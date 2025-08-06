#ifndef LLAMA_JNI_H
#define LLAMA_JNI_H

#include <jni.h>

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT jboolean JNICALL
Java_com_example_tastydiet_llm_LlamaManager_initModel(JNIEnv *env, jobject thiz, jstring path);

JNIEXPORT jstring JNICALL
Java_com_example_tastydiet_llm_LlamaManager_generateResponse(JNIEnv *env, jobject thiz, jstring prompt, jint maxTokens);

JNIEXPORT jboolean JNICALL
Java_com_example_tastydiet_llm_LlamaManager_isModelLoaded(JNIEnv *env, jobject thiz);

JNIEXPORT jstring JNICALL
Java_com_example_tastydiet_llm_LlamaManager_getModelInfo(JNIEnv *env, jobject thiz);

JNIEXPORT void JNICALL
Java_com_example_tastydiet_llm_LlamaManager_cleanup(JNIEnv *env, jobject thiz);

#ifdef __cplusplus
}
#endif

#endif // LLAMA_JNI_H 