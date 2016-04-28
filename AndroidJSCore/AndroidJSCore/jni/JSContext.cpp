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
	return (long) JSContextGroupCreate();
}

NATIVE(JSContextGroup,jlong,retain) (PARAMS,jlong group) {
	return (long) JSContextGroupRetain((JSContextGroupRef)group);
}

NATIVE(JSContextGroup,void,release) (PARAMS,jlong group) {
	JSContextGroupRelease((JSContextGroupRef) group);
}

NATIVE(JSContext,void,finalizeContext) (PARAMS,jlong ctx) {
	JSContextWrapper *wrapper = (JSContextWrapper *)ctx;
	delete wrapper;	
}

NATIVE(JSContext,jlong,create) (PARAMS) {
    JSContextWrapper *wrapper = new JSContextWrapper();

	struct msg_t {
		JSContextRef ref;
	};
	msg_t msg;
	wrapper->dispatch_q->sync([](void *msg){
		((msg_t*)msg)->ref = JSGlobalContextCreate((JSClassRef) NULL);
	}, &msg);
	wrapper->context = msg.ref;
	return (long) wrapper;
}

NATIVE(JSContext,jlong,createInGroup) (PARAMS,jlong group) {
	JSContextWrapper *wrapper = new JSContextWrapper();

	struct msg_t {
		JSContextGroupRef group;
		JSContextRef ref;
	};
	msg_t msg = {(JSContextGroupRef)group, 0};
	wrapper->dispatch_q->sync([](void *msg){
		((msg_t*)msg)->ref = JSGlobalContextCreateInGroup(((msg_t*)msg)->group,
			(JSClassRef) NULL);
	}, &msg);
	wrapper->context = msg.ref;
	return (long) wrapper;
}

NATIVE(JSContext,jlong,retain) (PARAMS,jlong ctx) {
	JSContextWrapper *wrapper = (JSContextWrapper *)ctx;
	return (long) JSGlobalContextRetain((JSGlobalContextRef) wrapper->context);
}

NATIVE(JSContext,void,release) (PARAMS,jlong ctx) {
	JSContextWrapper *wrapper = (JSContextWrapper *)ctx;
	JSGlobalContextRelease((JSGlobalContextRef) wrapper->context);
}

NATIVE(JSContext,jlong,getGlobalObject) (PARAMS, jlong ctx) {
	JSContextWrapper *wrapper = (JSContextWrapper *)ctx;
	return (long) JSContextGetGlobalObject((JSContextRef) wrapper->context);
}

NATIVE(JSContext,jlong,getGroup) (PARAMS, jlong ctx) {
	JSContextWrapper *wrapper = (JSContextWrapper *)ctx;
	return (long)JSContextGetGroup((JSContextRef) wrapper->context);
}

NATIVE(JSContext,jobject,evaluateScript) (PARAMS, jlong ctx, jlong script,
	jlong thisObject, jlong sourceURL, int startingLineNumber) {

	JSContextWrapper *wrapper = (JSContextWrapper *)ctx;
	JSValueRef exception = NULL;

	jclass ret = env->FindClass("org/liquidplayer/webkit/javascriptcore/JNIReturnObject");
	jmethodID cid = env->GetMethodID(ret,"<init>","()V");
	jobject out = env->NewObject(ret, cid);

	jfieldID fid = env->GetFieldID(ret , "reference", "J");

	struct msg_t {
		JSContextRef ctx;
		JSStringRef  script;
		JSObjectRef  thisObject;
		JSStringRef  sourceURL;
		int          startingLineNumber;
		JSValueRef*  exception;
		long         lval;
	};
	msg_t msg = {
		wrapper->context,
		(JSStringRef)script,
		(JSObjectRef)thisObject,
		(JSStringRef)sourceURL,
		startingLineNumber,
		&exception,
		0L
        };
	wrapper->dispatch_q->sync([](void *msg){
		msg_t *m = (msg_t *)msg;
		m->lval = (long) JSEvaluateScript(m->ctx, m->script,
			m->thisObject, m->sourceURL, m->startingLineNumber, m->exception);
	}, &msg);

	env->SetLongField( out, fid, msg.lval);

	fid = env->GetFieldID(ret , "exception", "J");
	env->SetLongField( out, fid, (long) exception);

	return out;
}

NATIVE(JSContext,jobject,checkScriptSyntax) (PARAMS, jlong ctx, jlong script,
		jlong sourceURL, int startingLineNumber) {

	JSContextWrapper *wrapper = (JSContextWrapper *)ctx;
	JSValueRef exception = NULL;

	jclass ret = env->FindClass("org/liquidplayer/webkit/javascriptcore/JNIReturnObject");
	jmethodID cid = env->GetMethodID(ret,"<init>","()V");
	jobject out = env->NewObject(ret, cid);

	jfieldID fid = env->GetFieldID(ret , "reference", "J");
	struct msg_t {
		JSContextRef ctx;
		JSStringRef  script;
		JSStringRef  sourceURL;
		int          startingLineNumber;
		JSValueRef*  exception;
		long         lval;
	};
	msg_t msg = {
		wrapper->context,
		(JSStringRef)script,
		(JSStringRef)sourceURL,
		startingLineNumber,
		&exception,
		0L
        };
	wrapper->worker_q->sync([](void *msg){
		msg_t *m = (msg_t *)msg;
		m->lval = (long) JSCheckScriptSyntax(m->ctx, m->script,
			m->sourceURL, m->startingLineNumber, m->exception);
	}, &msg);
	env->SetLongField( out, fid, (long) msg.lval );

	fid = env->GetFieldID(ret , "exception", "J");
	env->SetLongField( out, fid, (long) exception);

	return out;
}

NATIVE(JSContext,void,garbageCollect) (PARAMS, jlong ctx) {
	JSContextWrapper *wrapper = (JSContextWrapper *)ctx;
	struct msg_t {
		JSContextRef ctx;
	};
	msg_t *msg = new msg_t;
        msg->ctx = wrapper->context;
	wrapper->worker_q->async([](void *msg){
		JSGarbageCollect(((msg_t*)msg)->ctx);
		delete (msg_t*)msg;
	}, msg);
}

