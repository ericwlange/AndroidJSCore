//
// Instance.h
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

#ifndef ANDROIDJSCORE_INSTANCE_H
#define ANDROIDJSCORE_INSTANCE_H

#include "JSJNI.h"
#include <map>
#include <mutex>

class Instance {
public:
    Instance(JNIEnv *env, jobject thiz, JSContextRef ctx,
        JSClassDefinition def = kJSClassDefinitionEmpty, JSStringRef name = NULL);
    virtual ~Instance();
    virtual long getObjRef() { return (long) objRef; }
    static Instance* getInstance(JSObjectRef objref);

protected:
    JavaVM *jvm;
    jobject thiz;

private:
    JSObjectRef objRef;
    JSClassRef classRef;
    JSClassDefinition definition;

    static std::map<JSObjectRef,Instance *> objMap;
    static std::mutex mutex;

    static void StaticFinalizeCallback(JSObjectRef object);
};

#endif //ANDROIDJSCORE_INSTANCE_H
