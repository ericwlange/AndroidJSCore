//
// JSContext.cpp
// AndroidJSCore project
//
// https://github.com/ericwlange/AndroidJSCore/
//
// Created by Eric Lange
//
/*
 Copyright (c) 2014 Eric Lange. All rights reserved.

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

#include "JSJNI.h"
#include <pthread.h>
#include <stdio.h>
#include <unistd.h>
#include <android/log.h>

static int pfd[2];
static pthread_t thr;

static void *thread_func(void*)
{
    ssize_t rdsz;
    char buf[128];
    while((rdsz = read(pfd[0], buf, sizeof buf - 1)) > 0) {
        if(buf[rdsz - 1] == '\n') --rdsz;
        buf[rdsz - 1] = 0;  /* add null-terminator */
        __android_log_write(ANDROID_LOG_DEBUG, "JavaScriptCore", buf);
    }
    return 0;
}

NATIVE(JSContext,void,staticInit) (PARAMS) {
    /* make stdout line-buffered and stderr unbuffered */
    setvbuf(stdout, 0, _IOLBF, 0);
    setvbuf(stderr, 0, _IONBF, 0);

    /* create the pipe and redirect stdout and stderr */
    pipe(pfd);
    dup2(pfd[1], 1);
    dup2(pfd[1], 2);

    /* spawn the logging thread */
    if(pthread_create(&thr, 0, thread_func, 0) == -1)
        return; // fail silently
    pthread_detach(thr);
}

NATIVE(JSContextGroup,jlong,create) (PARAMS) {
    JSContextGroupRef group = JSContextGroupCreate();
    JSContextGroupRetain(group);
    return (long)group;
}

NATIVE(JSContextGroup,jlong,retain) (PARAMS,jlong group) {
    return (long) JSContextGroupRetain((JSContextGroupRef)group);
}

NATIVE(JSContextGroup,void,release) (PARAMS,jlong group) {
    JSContextGroupRelease((JSContextGroupRef) group);
}

NATIVE(JSContext,jlong,create) (PARAMS) {
    JSGlobalContextRef ref = JSGlobalContextCreate((JSClassRef) NULL);
    //JSGlobalContextRetain(ref);
    return (long)ref;
}

NATIVE(JSContext,jlong,createInGroup) (PARAMS,jlong group) {
    JSGlobalContextRef ref = JSGlobalContextCreateInGroup((JSContextGroupRef)group,
                            (JSClassRef) NULL);
    //JSGlobalContextRetain(ref);
    return (long)ref;
}

NATIVE(JSContext,jlong,retain) (PARAMS,jlong ctx) {
    return (jlong) JSGlobalContextRetain((JSGlobalContextRef) ctx);
}

NATIVE(JSContext,void,release) (PARAMS,jlong ctx) {
    JSGlobalContextRelease((JSGlobalContextRef) ctx);
}

NATIVE(JSContext,jlong,getGlobalObject) (PARAMS, jlong ctx) {
    JSObjectRef ref = JSContextGetGlobalObject((JSContextRef) ctx);
    JSValueProtect((JSContextRef)ctx,ref);
    return (long)ref;
}

NATIVE(JSContext,jlong,getGroup) (PARAMS, jlong ctx) {
    JSContextGroupRef group = JSContextGetGroup((JSContextRef) ctx);
    JSContextGroupRetain(group);
    return (long)group;
}

NATIVE(JSContext,jobject,evaluateScript) (PARAMS, jlong ctx, jlong script,
    jlong thisObject, jlong sourceURL, int startingLineNumber) {

    JSValueRef exception = NULL;

    jclass ret = env->FindClass("org/liquidplayer/webkit/javascriptcore/JSValue$JNIReturnObject");
    jmethodID cid = env->GetMethodID(ret,"<init>","()V");
    jobject out = env->NewObject(ret, cid);

    jfieldID fid = env->GetFieldID(ret , "reference", "J");

    JSValueRef value = JSEvaluateScript(
        (JSContextRef)ctx,
        (JSStringRef)script,
        (JSObjectRef)thisObject,
        (JSStringRef)sourceURL,
        startingLineNumber,
        &exception);
    JSValueProtect((JSContextRef)ctx, value);

    env->SetLongField( out, fid, (long)value);

    fid = env->GetFieldID(ret , "exception", "J");
    env->SetLongField( out, fid, (jlong) exception);

    return out;
}

NATIVE(JSContext,jobject,checkScriptSyntax) (PARAMS, jlong ctx, jlong script,
        jlong sourceURL, jint startingLineNumber) {

    JSValueRef exception = NULL;

    jclass ret = env->FindClass("org/liquidplayer/webkit/javascriptcore/JSValue$JNIReturnObject");
    jmethodID cid = env->GetMethodID(ret,"<init>","()V");
    jobject out = env->NewObject(ret, cid);

    jfieldID fid = env->GetFieldID(ret , "reference", "J");
    bool value = JSCheckScriptSyntax(
        (JSContextRef)ctx,
        (JSStringRef)script,
        (JSStringRef)sourceURL,
        startingLineNumber,
        &exception);
    env->SetBooleanField( out, fid, value );

    fid = env->GetFieldID(ret , "exception", "J");
    env->SetLongField( out, fid, (jlong) exception);

    return out;
}

NATIVE(JSContext,void,garbageCollect) (PARAMS, jlong ctx) {
    JSGarbageCollect((JSContextRef)ctx);
}

