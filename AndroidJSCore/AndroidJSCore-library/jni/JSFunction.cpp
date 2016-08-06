//
// JSFunction.cpp
// AndroidJSCore project
//
// https://github.com/ericwlange/AndroidJSCore/
//
// Created by Eric Lange
//
/*
 Copyright (c) 2014-2016 Eric Lange. All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:

 - Redistributions of source code must retain the above copyright notice, this
 list of conditions and the following disclaimer.

 - Redistributions in binary form must reproduce the above copyright notice,
 this list of conditions and the following disclaimer in the documentation
 and/or other materials provided with the distribution.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

#include "JSFunction.h"

JSClassDefinition JSFunction::JSFunctionClassDefinition() {
    JSClassDefinition definition = kJSClassDefinitionEmpty;
    definition.callAsFunction = StaticFunctionCallback;
    definition.callAsConstructor = StaticConstructorCallback;
    definition.hasInstance = StaticHasInstanceCallback;
    return definition;
}

JSFunction::JSFunction(JNIEnv* env, jobject thiz, JSContextRef ctx, JSStringRef name)
    : Instance(env, thiz, ctx, JSFunctionClassDefinition(), name)
{
}

JSFunction::~JSFunction() {
}

JSValueRef JSFunction::StaticFunctionCallback(JSContextRef ctx, JSObjectRef function, JSObjectRef thisObject,
        size_t argumentCount, const JSValueRef arguments[], JSValueRef* exception)
{
    JSFunction *thiz = (JSFunction *)getInstance(function);

    if (thiz) {
        return thiz->FunctionCallback(ctx,function,thisObject,argumentCount,arguments,exception);
    }
    return NULL;
}

JSObjectRef JSFunction::StaticConstructorCallback(JSContextRef ctx, JSObjectRef constructor,
        size_t argumentCount, const JSValueRef arguments[], JSValueRef* exception)
{
    JSFunction *thiz = (JSFunction *)getInstance(constructor);

    if (thiz) {
        return thiz->ConstructorCallback(ctx,constructor,argumentCount,arguments,exception);
    }
    return NULL;
}

bool JSFunction::StaticHasInstanceCallback(JSContextRef ctx, JSObjectRef constructor,
        JSValueRef possibleInstance, JSValueRef* exception)
{
    JSFunction *thiz = (JSFunction *)getInstance(constructor);

    if (thiz) {
        return thiz->HasInstanceCallback(ctx,constructor,possibleInstance,exception);
    }
    return false;
}

JSValueRef JSFunction::FunctionCallback(JSContextRef ctx, JSObjectRef function, JSObjectRef thisObject,
        size_t argumentCount, const JSValueRef arguments[], JSValueRef* exception)
{
    JNIEnv *env;
    int getEnvStat = jvm->GetEnv((void**)&env, JNI_VERSION_1_6);
    if (getEnvStat == JNI_EDETACHED) {
        jvm->AttachCurrentThread(&env, NULL);
    }

    jclass cls = env->GetObjectClass(thiz);
    jmethodID mid;
    do {
        mid = env->GetMethodID(cls,"functionCallback","(JJJ[JJ)J");
        if (!env->ExceptionCheck()) break;
        env->ExceptionClear();
        jclass super = env->GetSuperclass(cls);
        env->DeleteLocalRef(cls);
        if (super == NULL || env->ExceptionCheck()) {
            if (super != NULL) env->DeleteLocalRef(super);
            if (getEnvStat == JNI_EDETACHED) {
                jvm->DetachCurrentThread();
            }
            return NULL;
        }
        cls = super;
    } while (true);
    env->DeleteLocalRef(cls);
    jlongArray argsArr = env->NewLongArray(argumentCount);
    jlong* args = new jlong[argumentCount];
    for (size_t i=0; i<argumentCount; i++) {
        args[i] = (long) arguments[i];
    }
    env->SetLongArrayRegion(argsArr,0,argumentCount,args);

    long objret = env->CallLongMethod(thiz, mid, (jlong)ctx, (jlong)function, (jlong)thisObject,
            argsArr, (jlong)exception);

    delete args;
    env->DeleteLocalRef(argsArr);

    if (getEnvStat == JNI_EDETACHED) {
        jvm->DetachCurrentThread();
    }
    return (JSObjectRef)objret;
}

JSObjectRef JSFunction::ConstructorCallback(JSContextRef ctx, JSObjectRef constructor,
        size_t argumentCount, const JSValueRef arguments[], JSValueRef* exception)
{
    JNIEnv *env;
    int getEnvStat = jvm->GetEnv((void**)&env, JNI_VERSION_1_6);
    if (getEnvStat == JNI_EDETACHED) {
        jvm->AttachCurrentThread(&env, NULL);
    }
    jclass cls = env->GetObjectClass(thiz);
    jmethodID mid;
    do {
        mid = env->GetMethodID(cls,"constructorCallback","(JJ[JJ)J");
        if (!env->ExceptionCheck()) break;
        env->ExceptionClear();
        jclass super = env->GetSuperclass(cls);
        env->DeleteLocalRef(cls);
        if (super == NULL || env->ExceptionCheck()) {
            if (super != NULL) env->DeleteLocalRef(super);
            if (getEnvStat == JNI_EDETACHED) {
                jvm->DetachCurrentThread();
            }
            return NULL;
        }
        cls = super;
    } while (true);
    env->DeleteLocalRef(cls);
    jlongArray argsArr = env->NewLongArray(argumentCount);
    jlong* args = new jlong[argumentCount];
    for (size_t i=0; i<argumentCount; i++) {
        args[i] = (long) arguments[i];
    }
    env->SetLongArrayRegion(argsArr,0,argumentCount,args);

    long objret = env->CallLongMethod(thiz, mid, (jlong)ctx, (jlong)constructor,
            argsArr, (jlong)exception);

    delete args;
    env->DeleteLocalRef(argsArr);

    if (getEnvStat == JNI_EDETACHED) {
        jvm->DetachCurrentThread();
    }
    return (JSObjectRef)objret;
}

bool JSFunction::HasInstanceCallback(JSContextRef ctx, JSObjectRef constructor,
        JSValueRef possibleInstance, JSValueRef* exception)
{
    JNIEnv *env;
    int getEnvStat = jvm->GetEnv((void**)&env, JNI_VERSION_1_6);
    if (getEnvStat == JNI_EDETACHED) {
        jvm->AttachCurrentThread(&env, NULL);
    }
    jclass cls = env->GetObjectClass(thiz);
    jmethodID mid;
    do {
        mid = env->GetMethodID(cls,"hasInstanceCallback","(JJJJ)Z");
        if (!env->ExceptionCheck()) break;
        env->ExceptionClear();
        jclass super = env->GetSuperclass(cls);
        env->DeleteLocalRef(cls);
        if (super == NULL || env->ExceptionCheck()) {
            if (super != NULL) env->DeleteLocalRef(super);
            if (getEnvStat == JNI_EDETACHED) {
                jvm->DetachCurrentThread();
            }
            return NULL;
        }
        cls = super;
    } while (true);
    env->DeleteLocalRef(cls);

    bool ret = env->CallBooleanMethod(thiz, mid, (jlong)ctx, (jlong)constructor,
            (jlong)possibleInstance, (jlong)exception);

    if (getEnvStat == JNI_EDETACHED) {
        jvm->DetachCurrentThread();
    }
    return ret;
}
