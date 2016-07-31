//
// JSString.cpp
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

NATIVE(JSValue_00024JSString,jlong,createWithCharacters) (PARAMS, jstring str)
{
    const jchar *chars = env->GetStringChars(str, NULL);
    JSStringRef string = JSStringRetain(JSStringCreateWithCharacters(chars,
        env->GetStringLength(str)));
    env->ReleaseStringChars(str,chars);
    return (long)string;
}

NATIVE(JSValue_00024JSString,jlong,createWithUTF8CString) (PARAMS, jstring str)
{
    const char *string = env->GetStringUTFChars(str, NULL);
    JSStringRef ret = JSStringRetain(JSStringCreateWithUTF8CString(string));
    env->ReleaseStringUTFChars(str, string);
    return (long)ret;
}

NATIVE(JSValue_00024JSString,jlong,retain) (PARAMS, jlong strRef) {
    return (jlong) JSStringRetain((JSStringRef)strRef);
}

NATIVE(JSValue_00024JSString,void,release) (PARAMS, jlong stringRef) {
    JSStringRelease((JSStringRef)stringRef);
}

NATIVE(JSValue_00024JSString,jint,getLength) (PARAMS, jlong stringRef) {
    return (jint) JSStringGetLength((JSStringRef)stringRef);
}

NATIVE(JSValue_00024JSString,jstring,toString) (PARAMS, jlong stringRef) {
    char *buffer = new char[JSStringGetMaximumUTF8CStringSize((JSStringRef)stringRef)+1];
    JSStringGetUTF8CString((JSStringRef)stringRef, buffer,
        JSStringGetMaximumUTF8CStringSize((JSStringRef)stringRef)+1);
    jstring ret = env->NewStringUTF(buffer);
    delete buffer;
    return ret;
}

NATIVE(JSValue_00024JSString,jint,getMaximumUTF8CStringSize) (PARAMS, jlong stringRef) {
    return (jint) JSStringGetMaximumUTF8CStringSize((JSStringRef)stringRef);
}

NATIVE(JSValue_00024JSString,jboolean,isEqual) (PARAMS, jlong a, jlong b) {
    return (jboolean) JSStringIsEqual((JSStringRef)a, (JSStringRef)b);
}

NATIVE(JSValue_00024JSString,jboolean,isEqualToUTF8CString) (PARAMS, jlong a, jstring b) {
    const char *string = env->GetStringUTFChars(b, NULL);
    jboolean ret = JSStringIsEqualToUTF8CString((JSStringRef)a, string);
    env->ReleaseStringUTFChars(b, string);
    return ret;
}


