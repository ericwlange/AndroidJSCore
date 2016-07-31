//
// JSValue.cpp
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

#include "JSJNI.h"

NATIVE(JSValue,jint,getType) (PARAMS, jlong ctxRef, jlong valueRef )
{
    return (jint) JSValueGetType((JSContextRef)ctxRef, (JSValueRef)valueRef);
}

NATIVE(JSValue,jboolean,isUndefined) (PARAMS, jlong ctxRef, jlong valueRef)
{
    return (jboolean) JSValueIsUndefined((JSContextRef)ctxRef, (JSValueRef)valueRef);
}

NATIVE(JSValue,jboolean,isNull) (PARAMS, jlong ctxRef, jlong valueRef)
{
    return (jboolean) JSValueIsNull((JSContextRef)ctxRef, (JSValueRef)valueRef);
}

NATIVE(JSValue,jboolean,isBoolean) (PARAMS, jlong ctxRef, jlong valueRef)
{
    return (jboolean) JSValueIsBoolean((JSContextRef)ctxRef, (JSValueRef)valueRef);
}

NATIVE(JSValue,jboolean,isNumber) (PARAMS, jlong ctxRef, jlong valueRef)
{
    return (jboolean) JSValueIsNumber((JSContextRef)ctxRef, (JSValueRef)valueRef);
}

NATIVE(JSValue,jboolean,isString) (PARAMS, jlong ctxRef, jlong valueRef)
{
    return (jboolean) JSValueIsString((JSContextRef)ctxRef, (JSValueRef)valueRef);
}

NATIVE(JSValue,jboolean,isObject) (PARAMS, jlong ctxRef, jlong valueRef)
{
    return (jboolean) JSValueIsObject((JSContextRef)ctxRef, (JSValueRef)valueRef);
}

NATIVE(JSValue,jboolean,isArray) (PARAMS, jlong ctxRef, jlong valueRef)
{
    return (jboolean) JSValueIsArray((JSContextRef)ctxRef, (JSValueRef)valueRef);
}

NATIVE(JSValue,jboolean,isDate) (PARAMS, jlong ctxRef, jlong valueRef)
{
    return (jboolean) JSValueIsDate((JSContextRef)ctxRef, (JSValueRef)valueRef);
}

/* Comparing values */

NATIVE(JSValue,jobject,isEqual) (PARAMS, jlong ctxRef, jlong a, jlong b)
{
    JSValueRef exception = NULL;

    jclass ret = env->FindClass("org/liquidplayer/webkit/javascriptcore/JSValue$JNIReturnObject");
    jmethodID cid = env->GetMethodID(ret,"<init>","()V");
    jobject out = env->NewObject(ret, cid);

    jfieldID fid = env->GetFieldID(ret ,"bool", "Z");

    bool bret = JSValueIsEqual((JSContextRef) ctxRef, (JSValueRef)a, (JSValueRef)b,
         &exception);

    env->SetBooleanField( out, fid, bret);

    fid = env->GetFieldID(ret , "exception", "J");
    env->SetLongField( out, fid, (long)exception);

    return out;
}

NATIVE(JSValue,jboolean,isStrictEqual) (PARAMS, jlong ctxRef, jlong a, jlong b)
{
    return (jboolean) JSValueIsStrictEqual((JSContextRef)ctxRef, (JSValueRef)a, (JSValueRef)b);
}

NATIVE(JSValue,jobject,isInstanceOfConstructor) (PARAMS, jlong ctxRef, jlong valueRef,
    jlong constructor)
{
    JSValueRef exception = NULL;

    jclass ret = env->FindClass("org/liquidplayer/webkit/javascriptcore/JSValue$JNIReturnObject");
    jmethodID cid = env->GetMethodID(ret,"<init>","()V");
    jobject out = env->NewObject(ret, cid);

    jfieldID fid = env->GetFieldID(ret ,"bool", "Z");

    bool bret = JSValueIsInstanceOfConstructor((JSContextRef) ctxRef, (JSValueRef)valueRef,
            (JSObjectRef)constructor, &exception);

    env->SetBooleanField( out, fid, bret);

    fid = env->GetFieldID(ret , "exception", "J");
    env->SetLongField( out, fid, (long)exception);

    return out;
}

/* Creating values */

NATIVE(JSValue,jlong,makeUndefined) (PARAMS, jlong ctx)
{
    JSValueRef value = JSValueMakeUndefined((JSContextRef) ctx);
    JSValueProtect((JSContextRef) ctx, value);
    return (long)value;
}

NATIVE(JSValue,jlong,makeNull) (PARAMS, jlong ctx)
{
    JSValueRef value = JSValueMakeNull((JSContextRef) ctx);
    JSValueProtect((JSContextRef) ctx, value);
    return (long)value;
}

NATIVE(JSValue,jlong,makeBoolean) (PARAMS, jlong ctx, jboolean boolean)
{
    JSValueRef value = JSValueMakeBoolean((JSContextRef) ctx, (bool) boolean);
    JSValueProtect((JSContextRef) ctx, value);
    return (long)value;
}

NATIVE(JSValue,jlong,makeNumber) (PARAMS, jlong ctx, jdouble number)
{
    JSValueRef value = JSValueMakeNumber((JSContextRef) ctx, (double) number);
    JSValueProtect((JSContextRef) ctx, value);
    return (long)value;
}

NATIVE(JSValue,jlong,makeString) (PARAMS, jlong ctx, jlong stringRef)
{
    JSValueRef value = JSValueMakeString((JSContextRef) ctx, (JSStringRef) stringRef);
    JSValueProtect((JSContextRef) ctx, value);
    return (long)value;
}

/* Converting to and from JSON formatted strings */

NATIVE(JSValue,jlong,makeFromJSONString) (PARAMS, jlong ctx, jlong stringRef)
{
    JSValueRef value = JSValueMakeFromJSONString((JSContextRef) ctx, (JSStringRef) stringRef);
    JSValueProtect((JSContextRef) ctx, value);
    return (long)value;
}

NATIVE(JSValue,jobject,createJSONString) (PARAMS, jlong ctxRef, jlong valueRef, jint indent)
{
    JSValueRef exception = NULL;

    jclass ret = env->FindClass("org/liquidplayer/webkit/javascriptcore/JSValue$JNIReturnObject");
    jmethodID cid = env->GetMethodID(ret,"<init>","()V");
    jobject out = env->NewObject(ret, cid);

    jfieldID fid = env->GetFieldID(ret , "reference", "J");
    JSStringRef value = JSValueCreateJSONString(
        (JSContextRef)ctxRef,
        (JSValueRef)valueRef,
        (unsigned)indent,
        &exception);
    if (value)
        value = JSStringRetain(value);

    env->SetLongField( out, fid, (long)value);

    fid = env->GetFieldID(ret , "exception", "J");
    env->SetLongField( out, fid, (long)exception);

    return out;
}

/* Converting to primitive values */

NATIVE(JSValue,jboolean,toBoolean) (PARAMS, jlong ctx, jlong valueRef)
{
    return (jboolean) JSValueToBoolean((JSContextRef)ctx, (JSValueRef)valueRef);
}

NATIVE(JSValue,jobject,toNumber) (PARAMS, jlong ctxRef, jlong valueRef)
{
    JSValueRef exception = NULL;

    jclass ret = env->FindClass("org/liquidplayer/webkit/javascriptcore/JSValue$JNIReturnObject");
    jmethodID cid = env->GetMethodID(ret,"<init>","()V");
    jobject out = env->NewObject(ret, cid);

    jfieldID fid = env->GetFieldID(ret , "number", "D");

    jdouble dret = JSValueToNumber((JSContextRef)ctxRef, (JSValueRef)valueRef, &exception);

    env->SetDoubleField( out, fid, dret);

    fid = env->GetFieldID(ret , "exception", "J");
    env->SetLongField( out, fid, (long) exception);

    return out;
}

NATIVE(JSValue,jobject,toStringCopy) (PARAMS, jlong ctxRef, jlong valueRef)
{
    JSValueRef exception = NULL;

    jclass ret = env->FindClass("org/liquidplayer/webkit/javascriptcore/JSValue$JNIReturnObject");
    jmethodID cid = env->GetMethodID(ret,"<init>","()V");
    jobject out = env->NewObject(ret, cid);

    jfieldID fid = env->GetFieldID(ret , "reference", "J");

    JSStringRef string = JSValueToStringCopy((JSContextRef)ctxRef, (JSValueRef)valueRef, &exception);
    if (string)
        string = JSStringRetain(string);

    env->SetLongField( out, fid, (long)string);

    fid = env->GetFieldID(ret , "exception", "J");
    env->SetLongField( out, fid, (long) exception);

    return out;
}

NATIVE(JSValue,jobject,toObject) (PARAMS, jlong ctxRef, jlong valueRef)
{
    JSValueRef exception = NULL;

    jclass ret = env->FindClass("org/liquidplayer/webkit/javascriptcore/JSValue$JNIReturnObject");
    jmethodID cid = env->GetMethodID(ret,"<init>","()V");
    jobject out = env->NewObject(ret, cid);

    jfieldID fid = env->GetFieldID(ret , "reference", "J");

    JSObjectRef value = JSValueToObject((JSContextRef)ctxRef, (JSValueRef)valueRef, &exception);
    JSValueProtect((JSContextRef)ctxRef, value);

    env->SetLongField( out, fid, (long)value);

    fid = env->GetFieldID(ret , "exception", "J");
    env->SetLongField( out, fid, (long) exception);

    return out;
}

/* Garbage collection */

NATIVE(JSValue,void,protect) (PARAMS, jlong ctxRef, jlong valueRef)
{
    JSValueProtect((JSContextRef)ctxRef, (JSValueRef)valueRef );
}

NATIVE(JSValue,void,unprotect) (PARAMS, jlong ctxRef, jlong valueRef)
{
    JSValueUnprotect((JSContextRef)ctxRef, (JSValueRef)valueRef);
}

NATIVE(JSValue,void,setException) (PARAMS, jlong valueRef, jlong exceptionRefRef)
{
    JSValueRef *exception = (JSValueRef *)exceptionRefRef;
    *exception = (JSValueRef)valueRef;
}
