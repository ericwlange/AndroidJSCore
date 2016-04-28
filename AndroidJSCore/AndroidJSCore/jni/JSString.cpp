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

// Create a set of worker threads for managing strings.  Two threads
// should be enough
static DispatchQueue string_q(2);

NATIVE(JSString,jlong,createWithCharacters) (PARAMS, jstring str)
{
	jboolean isCopy;
	struct msg_t { const jchar* chars; long len; long ret; };
	msg_t msg = { env->GetStringChars(str, &isCopy), env->GetStringLength(str), 0L };
	string_q.sync([](void *msg) {
		msg_t *m = (msg_t *)msg;
		m->ret = (long) JSStringCreateWithCharacters(m->chars, m->len );
	},&msg);
	env->ReleaseStringChars(str,msg.chars);
	return msg.ret;
}

NATIVE(JSString,jlong,createWithUTF8CString) (PARAMS, jstring str)
{
	struct msg_t { const char* string; long ret; };
	msg_t msg = { env->GetStringUTFChars(str, NULL), 0L };
	string_q.sync([](void *msg) {
		msg_t *m = (msg_t *)msg;
		m->ret = (long) JSStringCreateWithUTF8CString(m->string);
	},&msg);
	env->ReleaseStringUTFChars(str, msg.string);
	return msg.ret;
}

NATIVE(JSString,jlong,retain) (PARAMS, long strRef) {
	struct msg_t { JSStringRef strRef; long ret; };
	msg_t msg = { (JSStringRef) strRef, 0L };
	string_q.sync([](void *msg) {
		msg_t *m = (msg_t *)msg;
		m->ret = (long) JSStringRetain(m->strRef);
	},&msg);
	return msg.ret;
}

NATIVE(JSString,void,release) (PARAMS, long stringRef) {
	JSStringRef *msg = new JSStringRef((JSStringRef)stringRef);
	string_q.async([](void *msg) {
		JSStringRef *stringRef = (JSStringRef *)msg;
		JSStringRelease(*stringRef);
		delete stringRef;
	},msg);
}

NATIVE(JSString,jint,getLength) (PARAMS, long stringRef) {
	struct msg_t { JSStringRef strRef; int ret; };
	msg_t msg = { (JSStringRef) stringRef, 0 };
	string_q.sync([](void *msg) {
		msg_t *m = (msg_t *)msg;
		m->ret = (int) JSStringGetLength(m->strRef);
	},&msg);
	return msg.ret;
}

NATIVE(JSString,jstring,toString) (PARAMS, long stringRef) {
	struct msg_t { JSStringRef stringRef; char *buffer; jstring ret; };
	msg_t msg = { (JSStringRef) stringRef, NULL, 0 };
	string_q.sync([](void *msg) {
		msg_t *m = (msg_t *)msg;
		m->buffer = new char[JSStringGetMaximumUTF8CStringSize(m->stringRef)+1];
		JSStringGetUTF8CString(m->stringRef, m->buffer,
			JSStringGetMaximumUTF8CStringSize(m->stringRef)+1);
	},&msg);
	jstring ret = env->NewStringUTF(msg.buffer);
	delete msg.buffer;
	return ret;
}

NATIVE(JSString,jint,getMaximumUTF8CStringSize) (PARAMS, long stringRef) {
	struct msg_t { JSStringRef stringRef; int ret; };
	msg_t msg = { (JSStringRef) stringRef, 0 };
	string_q.sync([](void *msg) {
		msg_t *m = (msg_t *)msg;
		m->ret = (int) JSStringGetMaximumUTF8CStringSize(m->stringRef);
	},&msg);
	return msg.ret;
}

NATIVE(JSString,jboolean,isEqual) (PARAMS, long a, long b) {
	struct msg_t { JSStringRef a; JSStringRef b; bool ret; };
	msg_t msg = { (JSStringRef) a, (JSStringRef) b, false };
	string_q.sync([](void *msg) {
		msg_t *m = (msg_t *)msg;
		m->ret = JSStringIsEqual(m->a, m->b);
	}, &msg);
	return msg.ret;
}

NATIVE(JSString,jboolean,isEqualToUTF8CString) (PARAMS, long a, jstring b) {
	jboolean isCopy;
	struct msg_t { JSStringRef a; const char *string; bool ret; };
	msg_t msg = { (JSStringRef) a, env->GetStringUTFChars(b, &isCopy), false };
	string_q.sync([](void *msg) {
		msg_t *m = (msg_t *)msg;
		m->ret = JSStringIsEqualToUTF8CString(m->a, m->string);
	}, &msg);
	env->ReleaseStringUTFChars(b, msg.string);
	return msg.ret;
}


