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

#define SYNCHRONIZE(ctxRef,valueRef,t,f) \
	JSContextWrapper *wrapper = (JSContextWrapper *)ctxRef; \
	struct msg_t { JSContextRef ctxRef; JSValueRef valueRef; t ret; }; \
	msg_t msg = { wrapper->context, (JSValueRef)valueRef, (t)0 }; \
	wrapper->worker_q->sync([](void *msg) { \
		msg_t *m = (msg_t *)msg; \
		m->ret = f(m->ctxRef, m->valueRef); \
	},&msg); \
	return msg.ret


NATIVE(JSValue,jint,getType) (PARAMS, jlong ctxRef, jlong valueRef )
{
	SYNCHRONIZE(ctxRef,valueRef,int,JSValueGetType);
}

NATIVE(JSValue,jboolean,isUndefined) (PARAMS, jlong ctxRef, jlong valueRef)
{
	SYNCHRONIZE(ctxRef,valueRef,bool,JSValueIsUndefined);
}

NATIVE(JSValue,jboolean,isNull) (PARAMS, jlong ctxRef, jlong valueRef)
{
	SYNCHRONIZE(ctxRef,valueRef,bool,JSValueIsNull);
}

NATIVE(JSValue,jboolean,isBoolean) (PARAMS, jlong ctxRef, jlong valueRef)
{
	SYNCHRONIZE(ctxRef,valueRef,bool,JSValueIsBoolean);
}

NATIVE(JSValue,jboolean,isNumber) (PARAMS, jlong ctxRef, jlong valueRef)
{
	SYNCHRONIZE(ctxRef,valueRef,bool,JSValueIsNumber);
}

NATIVE(JSValue,jboolean,isString) (PARAMS, jlong ctxRef, jlong valueRef)
{
	SYNCHRONIZE(ctxRef,valueRef,bool,JSValueIsString);
}

NATIVE(JSValue,jboolean,isObject) (PARAMS, jlong ctxRef, jlong valueRef)
{
	SYNCHRONIZE(ctxRef,valueRef,bool,JSValueIsObject);
}

NATIVE(JSValue,jboolean,isArray) (PARAMS, jlong ctxRef, jlong valueRef)
{
	SYNCHRONIZE(ctxRef,valueRef,bool,JSValueIsArray);
}

NATIVE(JSValue,jboolean,isDate) (PARAMS, jlong ctxRef, jlong valueRef)
{
	SYNCHRONIZE(ctxRef,valueRef,bool,JSValueIsDate);
}

/* Comparing values */

NATIVE(JSValue,jobject,isEqual) (PARAMS, jlong ctxRef, jlong a, jlong b)
{
	JSValueRef exception = NULL;

	jclass ret = env->FindClass("org/liquidplayer/webkit/javascriptcore/JNIReturnObject");
	jmethodID cid = env->GetMethodID(ret,"<init>","()V");
	jobject out = env->NewObject(ret, cid);

	jfieldID fid = env->GetFieldID(ret , "bool", "Z");
	
	struct msg_t { JSContextRef ctxRef; JSValueRef a; JSValueRef b; JSValueRef *exception;
		bool ret; };
	JSContextWrapper *wrapper = (JSContextWrapper *)ctxRef;
	msg_t msg = { wrapper->context, (JSValueRef)a, (JSValueRef)b, &exception, false };
	wrapper->worker_q->sync([](void *msg) {
		msg_t *m = (msg_t *)msg;
		m->ret = JSValueIsEqual(m->ctxRef, m->a, m->b, m->exception);
	},&msg);
	
	env->SetBooleanField( out, fid, msg.ret);

	fid = env->GetFieldID(ret , "exception", "J");
	env->SetLongField( out, fid, (long) exception);

	return out;
}

NATIVE(JSValue,jboolean,isStrictEqual) (PARAMS, jlong ctxRef, jlong a, jlong b)
{
	struct msg_t { JSContextRef ctxRef; JSValueRef a; JSValueRef b; bool ret; };
	JSContextWrapper *wrapper = (JSContextWrapper *)ctxRef;
	msg_t msg = { wrapper->context, (JSValueRef)a, (JSValueRef)b, false };
	wrapper->worker_q->sync([](void *msg) {
		msg_t *m = (msg_t *)msg;
		m->ret = JSValueIsStrictEqual(m->ctxRef, m->a, m->b);
	},&msg);
	return msg.ret;
}

NATIVE(JSValue,jobject,isInstanceOfConstructor) (PARAMS, jlong ctxRef, jlong valueRef,
	jlong constructor)
{
	JSValueRef exception = NULL;

	jclass ret = env->FindClass("org/liquidplayer/webkit/javascriptcore/JNIReturnObject");
	jmethodID cid = env->GetMethodID(ret,"<init>","()V");
	jobject out = env->NewObject(ret, cid);

	jfieldID fid = env->GetFieldID(ret ,"bool", "Z");

	struct msg_t { JSContextRef ctxRef; JSValueRef valueRef; JSObjectRef constructor;
		JSValueRef *exception; bool ret; };
	JSContextWrapper *wrapper = (JSContextWrapper *)ctxRef;
	msg_t msg = { wrapper->context, (JSValueRef)valueRef, (JSObjectRef)constructor, 
		&exception, false };
	wrapper->worker_q->sync([](void *msg) {
		msg_t *m = (msg_t *)msg;
		m->ret = JSValueIsInstanceOfConstructor(m->ctxRef, m->valueRef, m->constructor,
			m->exception);
	},&msg);

	env->SetBooleanField( out, fid, msg.ret);

	fid = env->GetFieldID(ret , "exception", "J");
	env->SetLongField( out, fid, (long)exception);

	return out;
}

/* Creating values */

NATIVE(JSValue,jlong,makeUndefined) (PARAMS, jlong ctx)
{
	JSContextWrapper *wrapper = (JSContextWrapper *)ctx;
	long lval = (long) wrapper->context;
	wrapper->worker_q->sync([](void *lval) {
		*((long *)lval) = (long) JSValueMakeUndefined((JSContextRef) *((long*)lval));
	},&lval);
	return lval;
}

NATIVE(JSValue,jlong,makeNull) (PARAMS, jlong ctx)
{
	JSContextWrapper *wrapper = (JSContextWrapper *)ctx;
	long lval = (long) wrapper->context;
	wrapper->worker_q->sync([](void *lval) {
		*((long *)lval) = (long) JSValueMakeNull((JSContextRef) *((long*)lval));
	},&lval);
	return lval;
}

NATIVE(JSValue,jlong,makeBoolean) (PARAMS, jlong ctx, jboolean boolean)
{
	JSContextWrapper *wrapper = (JSContextWrapper *)ctx;
	struct msg_t { JSContextRef ctx; bool boolean; JSValueRef ret; };
	msg_t msg = { wrapper->context, (bool)boolean, NULL };
	wrapper->worker_q->sync([](void *msg) {
		msg_t *m = (msg_t *)msg;
		m->ret = JSValueMakeBoolean(m->ctx, m->boolean);
	},&msg);
	return (long) msg.ret;
}

NATIVE(JSValue,jlong,makeNumber) (PARAMS, jlong ctx, jdouble number)
{
	JSContextWrapper *wrapper = (JSContextWrapper *)ctx;
	struct msg_t { JSContextRef ctx; double number; JSValueRef ret; };
	msg_t msg = { wrapper->context, (double)number, NULL };
	wrapper->worker_q->sync([](void *msg) {
		msg_t *m = (msg_t *)msg;
		m->ret = JSValueMakeNumber(m->ctx, m->number);
	},&msg);
	return (long) msg.ret;
}

NATIVE(JSValue,jlong,makeString) (PARAMS, jlong ctx, jlong stringRef)
{
	JSContextWrapper *wrapper = (JSContextWrapper *)ctx;
	struct msg_t { JSContextRef ctx; JSStringRef stringRef; JSValueRef ret; };
	msg_t msg = { wrapper->context, (JSStringRef)stringRef, NULL };
	wrapper->worker_q->sync([](void *msg) {
		msg_t *m = (msg_t *)msg;
		m->ret = JSValueMakeString(m->ctx, m->stringRef);
	},&msg);
	return (long) msg.ret;
}

/* Converting to and from JSON formatted strings */

NATIVE(JSValue,jlong,makeFromJSONString) (PARAMS, jlong ctx, jlong stringRef)
{
	JSContextWrapper *wrapper = (JSContextWrapper *)ctx;
	struct msg_t {
		JSContextRef ctx;
		JSStringRef  stringRef;
		JSValueRef   valueRef;
	};
	msg_t msg = {
		wrapper->context,
		(JSStringRef)stringRef,
		(JSValueRef)0
        };
	wrapper->worker_q->sync([](void *msg){
		msg_t *m = (msg_t *)msg;
		m->valueRef = JSValueMakeFromJSONString(m->ctx, m->stringRef);
	}, &msg);
	return (long) msg.valueRef;
}

NATIVE(JSValue,jobject,createJSONString) (PARAMS, jlong ctxRef, jlong valueRef, jint indent)
{
	JSValueRef exception = NULL;

	jclass ret = env->FindClass("org/liquidplayer/webkit/javascriptcore/JNIReturnObject");
	jmethodID cid = env->GetMethodID(ret,"<init>","()V");
	jobject out = env->NewObject(ret, cid);

	jfieldID fid = env->GetFieldID(ret , "reference", "J");
	JSContextWrapper *wrapper = (JSContextWrapper *)ctxRef;
	struct msg_t {
		JSContextRef ctxRef;
		JSValueRef   valueRef;
		unsigned     indent;
		JSValueRef*  exception;
		long         lval;
	};
	msg_t msg = {
		wrapper->context,
		(JSValueRef)valueRef,
		(unsigned)indent,
		&exception,
		0L
        };
	wrapper->worker_q->sync([](void *msg){
		msg_t *m = (msg_t *)msg;
		m->lval = (long) JSValueCreateJSONString(m->ctxRef,
			m->valueRef, m->indent, m->exception);
	}, &msg);

	env->SetLongField( out, fid, msg.lval);

	fid = env->GetFieldID(ret , "exception", "J");
	env->SetLongField( out, fid, (long)exception);

	return out;
}

/* Converting to primitive values */

NATIVE(JSValue,jboolean,toBoolean) (PARAMS, jlong ctx, jlong valueRef)
{
	JSContextWrapper *wrapper = (JSContextWrapper *)ctx;
	struct msg_t { JSContextRef ctx; JSValueRef valueRef; bool ret; };
	msg_t msg = { wrapper->context, (JSValueRef)valueRef, false };
	wrapper->worker_q->sync([](void *msg) {
		msg_t *m = (msg_t *)msg;
		m->ret = JSValueToBoolean(m->ctx, m->valueRef);
	},&msg);
	return msg.ret;
}

NATIVE(JSValue,jobject,toNumber) (PARAMS, jlong ctxRef, jlong valueRef)
{
	JSValueRef exception = NULL;

	jclass ret = env->FindClass("org/liquidplayer/webkit/javascriptcore/JNIReturnObject");
	jmethodID cid = env->GetMethodID(ret,"<init>","()V");
	jobject out = env->NewObject(ret, cid);

	jfieldID fid = env->GetFieldID(ret , "number", "D");

	JSContextWrapper *wrapper = (JSContextWrapper *)ctxRef;
	struct msg_t { JSContextRef ctx; JSValueRef valueRef; JSValueRef *exception;
		double ret; };
	msg_t msg = { wrapper->context, (JSValueRef)valueRef, &exception, 0.0 };
	wrapper->worker_q->sync([](void *msg) {
		msg_t *m = (msg_t *)msg;
		m->ret = JSValueToNumber(m->ctx, m->valueRef, m->exception);
	},&msg);

	env->SetDoubleField( out, fid, msg.ret);

	fid = env->GetFieldID(ret , "exception", "J");
	env->SetLongField( out, fid, (long) exception);

	return out;
}

NATIVE(JSValue,jobject,toStringCopy) (PARAMS, jlong ctxRef, jlong valueRef)
{
	JSContextWrapper *wrapper = (JSContextWrapper *)ctxRef;
	JSValueRef exception = NULL;

	jclass ret = env->FindClass("org/liquidplayer/webkit/javascriptcore/JNIReturnObject");
	jmethodID cid = env->GetMethodID(ret,"<init>","()V");
	jobject out = env->NewObject(ret, cid);

	jfieldID fid = env->GetFieldID(ret , "reference", "J");
	struct msg_t { JSContextRef ctxRef; JSValueRef valueRef; JSValueRef* exception;
		long lval; };
    msg_t msg = { wrapper->context, (JSValueRef)valueRef, &exception, 0L };
	wrapper->worker_q->sync([](void *msg) {
		msg_t *m = (msg_t *)msg;
		m->lval = (long) JSValueToStringCopy(m->ctxRef, m->valueRef, m->exception);
	}, &msg);
	env->SetLongField( out, fid, msg.lval);

	fid = env->GetFieldID(ret , "exception", "J");
	env->SetLongField( out, fid, (long) exception);

	return out;
}

NATIVE(JSValue,jobject,toObject) (PARAMS, jlong ctxRef, jlong valueRef)
{
	JSValueRef exception = NULL;

	jclass ret = env->FindClass("org/liquidplayer/webkit/javascriptcore/JNIReturnObject");
	jmethodID cid = env->GetMethodID(ret,"<init>","()V");
	jobject out = env->NewObject(ret, cid);

	jfieldID fid = env->GetFieldID(ret , "reference", "J");
	
	JSContextWrapper *wrapper = (JSContextWrapper *)ctxRef;
	struct msg_t { JSContextRef ctxRef; JSValueRef valueRef; JSValueRef *exception;
		JSObjectRef ret; };
	msg_t msg = { wrapper->context, (JSValueRef)valueRef, &exception, NULL };
	wrapper->worker_q->sync([](void *msg) {
		msg_t *m = (msg_t *)msg;
		m->ret = JSValueToObject(m->ctxRef, m->valueRef, m->exception);
	},&msg);

	env->SetLongField( out, fid, (long) msg.ret);

	fid = env->GetFieldID(ret , "exception", "J");
	env->SetLongField( out, fid, (long) exception);

	return out;
}

/* Garbage collection */

NATIVE(JSValue,void,protect) (PARAMS, jlong ctxRef, jlong valueRef)
{
	JSValueProtect( ((JSContextWrapper*)ctxRef)->context, (JSValueRef)valueRef );
/*
	JSContextWrapper *wrapper = (JSContextWrapper *)ctxRef;
	struct msg_t { JSContextRef ctxRef; JSValueRef valueRef; };
	msg_t *msg = new msg_t;
	msg->ctxRef = wrapper->context;
	msg->valueRef = (JSValueRef)valueRef;
	wrapper->worker_q->async([](void *msg) {
		msg_t *m = (msg_t *)msg;
		JSValueProtect(m->ctxRef, m->valueRef);
		delete m;
	},msg);
*/
}

NATIVE(JSValue,void,unprotect) (PARAMS, jlong ctxRef, jlong valueRef)
{
	JSContextWrapper *wrapper = (JSContextWrapper *)ctxRef;
	struct msg_t { JSContextRef ctxRef; JSValueRef valueRef; };
	msg_t *msg = new msg_t;
	msg->ctxRef = wrapper->context;
	msg->valueRef = (JSValueRef)valueRef;
	wrapper->worker_q->async([](void *msg) {
		msg_t *m = (msg_t *)msg;
		JSValueUnprotect(m->ctxRef, m->valueRef);
		delete m;
	},msg);
}

NATIVE(JSValue,void,setException) (PARAMS, jlong valueRef, jlong exceptionRefRef)
{
	JSValueRef *exception = (JSValueRef *)exceptionRefRef;
	*exception = (JSValueRef)valueRef;
}
