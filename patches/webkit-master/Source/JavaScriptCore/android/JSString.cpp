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

NATIVE(JSString,jlong,createWithCharacters) (PARAMS, jstring str)
{
	jboolean isCopy;
	const jchar* chars = env->GetStringChars(str, &isCopy);
	long ret = (long) JSStringCreateWithCharacters(chars, env->GetStringLength(str));
	env->ReleaseStringChars(str,chars);
	return ret;
}

NATIVE(JSString,jlong,createWithUTF8CString) (PARAMS, jstring str)
{
	const char* string = env->GetStringUTFChars(str, NULL);
	long ret = (long) JSStringCreateWithUTF8CString(string);
	env->ReleaseStringUTFChars(str, string);
	return ret;
}

NATIVE(JSString,jlong,retain) (PARAMS, long strRef) {
	return (long) JSStringRetain((JSStringRef) strRef);
}

NATIVE(JSString,void,release) (PARAMS, long stringRef) {
	JSStringRelease((JSStringRef) stringRef);
}

NATIVE(JSString,jint,getLength) (PARAMS, long stringRef) {
	return (int) JSStringGetLength((JSStringRef) stringRef);
}

NATIVE(JSString,jstring,toString) (PARAMS, long stringRef) {
	char buffer[JSStringGetMaximumUTF8CStringSize((JSStringRef) stringRef)+1];
	JSStringGetUTF8CString((JSStringRef) stringRef, buffer, sizeof buffer);
	return env->NewStringUTF(buffer);
}

NATIVE(JSString,jint,getMaximumUTF8CStringSize) (PARAMS, long stringRef) {
	return (int) JSStringGetMaximumUTF8CStringSize((JSStringRef) stringRef);
}

NATIVE(JSString,jboolean,isEqual) (PARAMS, long a, long b) {
	return JSStringIsEqual((JSStringRef) a, (JSStringRef) b);
}

NATIVE(JSString,jboolean,isEqualToUTF8CString) (PARAMS, long a, jstring b) {
	jboolean isCopy;
	const char* string = env->GetStringUTFChars(b, &isCopy);
	return JSStringIsEqualToUTF8CString((JSStringRef) a, string);
}


