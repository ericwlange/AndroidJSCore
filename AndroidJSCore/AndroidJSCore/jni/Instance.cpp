//
// Instance.cpp
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

#include "Instance.h"

Instance::Instance(JNIEnv *env, jobject thiz, JSContextRef ctx,
        JSClassDefinition def, JSStringRef name)
{
    env->GetJavaVM(&jvm);
    definition = def;
    definition.finalize = StaticFinalizeCallback;
    classRef = JSClassCreate(&definition);
    objRef = JSObjectMake(ctx, classRef, name);
    JSValueProtect(ctx, objRef);
    this->thiz = env->NewWeakGlobalRef(thiz);

    mutex.lock();
    objMap[objRef] = this;
    mutex.unlock();
}

Instance::~Instance()
{
    JSClassRelease(classRef);
    JNIEnv *env;
    int getEnvStat = jvm->GetEnv((void**)&env, JNI_VERSION_1_6);
    if (getEnvStat == JNI_EDETACHED) {
        jvm->AttachCurrentThread(&env, NULL);
    }
    env->DeleteWeakGlobalRef(thiz);

    mutex.lock();
    objMap.erase(objRef);
    mutex.unlock();

    if (getEnvStat == JNI_EDETACHED) {
        jvm->DetachCurrentThread();
    }
}

Instance* Instance::getInstance(JSObjectRef objref)
{
    Instance *inst = NULL;
    mutex.lock();
    inst = objMap[objref];
    mutex.unlock();
    return inst;
}

std::map<JSObjectRef,Instance *> Instance::objMap = std::map<JSObjectRef,Instance *>();
std::mutex Instance::mutex;

void Instance::StaticFinalizeCallback(JSObjectRef object)
{
    Instance *thiz = getInstance(object);

    if (thiz) {
        delete thiz;
    }
}
