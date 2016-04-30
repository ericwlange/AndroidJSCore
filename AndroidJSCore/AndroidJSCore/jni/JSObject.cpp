//
// JSObject.cpp
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
#include <map>

NATIVE(JSObject,jlong,make) (PARAMS, jlong ctx, jlong data) {
	JSContextWrapper *wrapper = (JSContextWrapper *)ctx;
	struct msg_t { JSContextRef ctx; void *data; JSObjectRef ret; };
	msg_t msg = { wrapper->context, (void *)data, NULL };
	wrapper->worker_q->sync([](void *msg) {
		msg_t *m = (msg_t *)msg;
		m->ret = JSObjectMake(m->ctx, (JSClassRef) NULL, m->data);
	}, &msg);
	return (long)msg.ret;
}

class Instance {
private:
	JavaVM *jvm;
	jobject thiz;
	JSObjectRef objRef;
	static std::map<JSObjectRef,Instance *> objMap;
	JSClassRef classRef;

	static void StaticFinalizeCallback(JSObjectRef object);
	void FinalizeCallback(JSObjectRef object);
public:
	Instance(JNIEnv *env, jobject thiz, JSContextWrapper *wrapper);
	virtual ~Instance();
	long getObjRef() { return (long) objRef; }
};
Instance::Instance(JNIEnv *env, jobject thiz, JSContextWrapper *wrapper) {
	env->GetJavaVM(&jvm);
	struct msg_t { JSContextRef ctx; JSClassRef classRef; JSObjectRef objRef; };
	msg_t msg = { wrapper->context, (JSClassRef) NULL, (JSObjectRef) NULL };
	wrapper->worker_q->sync([](void *msg) {
		msg_t *m = (msg_t *)msg;
		JSClassDefinition definition = kJSClassDefinitionEmpty;
		definition.finalize = StaticFinalizeCallback;
		m->classRef = JSClassCreate(&definition);
		m->objRef = JSObjectMake(m->ctx, m->classRef, NULL);
	}, &msg);
	this->thiz = env->NewGlobalRef(thiz);
	classRef = msg.classRef;
	objRef = msg.objRef;
	objMap[objRef] = this;
}
Instance::~Instance() {
	JSClassRelease(classRef);
	JNIEnv *env;
	jvm->AttachCurrentThread(&env, NULL);
	env->DeleteGlobalRef(thiz);
	objMap[objRef] = NULL;
	jvm->DetachCurrentThread();
}
std::map<JSObjectRef,Instance *> Instance::objMap = std::map<JSObjectRef,Instance *>();
void Instance::StaticFinalizeCallback(JSObjectRef object)
{
	Instance *thiz = objMap[object];
	if (thiz) {
		thiz->FinalizeCallback(object);
		delete thiz;
	}
}
void Instance::FinalizeCallback(JSObjectRef object)
{
	JNIEnv *env;
	jvm->AttachCurrentThread(&env, NULL);
	jclass cls = env->GetObjectClass(thiz);
	jmethodID mid;
	do {
		mid = env->GetMethodID(cls,"finalizeCallback","(J)V");
		if (!env->ExceptionCheck()) break;
		env->ExceptionClear();
		cls = env->GetSuperclass(env->GetObjectClass(thiz));
		if (cls == NULL || env->ExceptionCheck()) {
			return;
		}
	} while (true);
	env->CallVoidMethod(thiz, mid, (jlong)object);
	jvm->DetachCurrentThread();
}
NATIVE(JSObject,jlong,makeWithFinalizeCallback) (PARAMS, jlong ctx) {
	Instance *instance = new Instance(env, thiz, (JSContextWrapper *)ctx);
	return instance->getObjRef();
}

class Function {
	private:
		JavaVM *jvm;
		jobject thiz;
		JSObjectRef objRef;
		JSClassRef classRef;
		JSClassDefinition definition;

		static std::map<JSObjectRef,Function *> objMap;

		static JSValueRef StaticFunctionCallback(JSContextRef ctx, JSObjectRef function,
			 	JSObjectRef thisObject,size_t argumentCount, const JSValueRef arguments[],
			 	JSValueRef* exception);
		static JSObjectRef StaticConstructorCallback(JSContextRef ctx,
				JSObjectRef constructor,size_t argumentCount,const JSValueRef arguments[],
				JSValueRef* exception);

		JSObjectRef ConstructorCallback(JSContextRef ctx, JSObjectRef constructor,
				size_t argumentCount, const JSValueRef arguments[], JSValueRef* exception);
		JSValueRef FunctionCallback(JSContextRef ctx, JSObjectRef function,
				JSObjectRef thisObject, size_t argumentCount,const JSValueRef arguments[],
				JSValueRef* exception);

	public:
		Function(JNIEnv *env, jobject thiz, JSContextWrapper *wrapper, JSStringRef name);
		virtual ~Function();
		long getObjRef() { return (long) objRef; }
		static void release(JSContextRef ctx, JSObjectRef function);
};
Function::Function(JNIEnv* env, jobject thiz, JSContextWrapper *wrapper,
	__attribute__((unused))JSStringRef name) {
	
	env->GetJavaVM(&jvm);
	definition = kJSClassDefinitionEmpty;
	definition.callAsFunction = StaticFunctionCallback;
	definition.callAsConstructor = StaticConstructorCallback;
	classRef = JSClassCreate(&definition);

	this->thiz = env->NewGlobalRef(thiz);
	struct msg_t { JSContextRef ctx; JSClassRef classRef; JSObjectRef objRef; };
	msg_t msg = { wrapper->context, classRef, (JSObjectRef) NULL };
	wrapper->worker_q->sync([](void *msg) {
		msg_t *m = (msg_t *)msg;
		m->objRef = JSObjectMake(m->ctx, m->classRef, NULL);
	}, &msg);
	objRef = msg.objRef;
	objMap[objRef] = this;
}
Function::~Function() {
	JSClassRelease(classRef);
	JNIEnv *env;
	jvm->AttachCurrentThread(&env, NULL);
	env->DeleteGlobalRef(thiz);
	jvm->DetachCurrentThread();
}
void Function::release(__attribute__((unused))JSContextRef ctx, JSObjectRef function) {
	Function *f = objMap[function];
	if (f) {
		delete f;
		objMap[function] = NULL;
	}
}
std::map<JSObjectRef,Function *> Function::objMap = std::map<JSObjectRef,Function *>();
JSValueRef Function::StaticFunctionCallback(JSContextRef ctx, JSObjectRef function, JSObjectRef thisObject,
		size_t argumentCount, const JSValueRef arguments[], JSValueRef* exception)
{
	Function *thiz = objMap[function];
	if (thiz) {
		return thiz->FunctionCallback(ctx,function,thisObject,argumentCount,arguments,exception);
	}
	return NULL;
}
JSObjectRef Function::StaticConstructorCallback(JSContextRef ctx, JSObjectRef constructor,
		size_t argumentCount, const JSValueRef arguments[], JSValueRef* exception)
{
	Function *thiz = objMap[constructor];
	if (thiz) {
		return thiz->ConstructorCallback(ctx,constructor,argumentCount,arguments,exception);
	}
	return NULL;
}
JSValueRef Function::FunctionCallback(JSContextRef ctx, JSObjectRef function, JSObjectRef thisObject,
		size_t argumentCount, const JSValueRef arguments[], JSValueRef* exception)
{
	JNIEnv *env;
	jvm->AttachCurrentThread(&env, NULL);
	jclass cls = env->GetObjectClass(thiz);
	jmethodID mid;
	do {
		mid = env->GetMethodID(cls,"functionCallback","(JJJ[JJ)J");
		if (!env->ExceptionCheck()) break;
		env->ExceptionClear();
		cls = env->GetSuperclass(env->GetObjectClass(thiz));
		if (cls == NULL || env->ExceptionCheck()) {
			return NULL;
		}
	} while (true);
	jlongArray argsArr = env->NewLongArray(argumentCount);
	jlong* args = new jlong[argumentCount];
	for (size_t i=0; i<argumentCount; i++) {
		args[i] = (long) arguments[i];
	}
	env->SetLongArrayRegion(argsArr,0,argumentCount,args);

	long objret = env->CallLongMethod(thiz, mid, (jlong)ctx, (jlong)function, (jlong)thisObject,
			argsArr, (jlong)exception);

	delete args;
	jvm->DetachCurrentThread();
	return (JSObjectRef)objret;
}
JSObjectRef Function::ConstructorCallback(JSContextRef ctx, JSObjectRef constructor,
		size_t argumentCount, const JSValueRef arguments[], JSValueRef* exception)
{
	JNIEnv *env;
	jvm->AttachCurrentThread(&env, NULL);
	jclass cls = env->GetObjectClass(thiz);
	jmethodID mid;
	do {
		mid = env->GetMethodID(cls,"constructorCallback","(JJ[JJ)J");
		if (!env->ExceptionCheck()) break;
		env->ExceptionClear();
		cls = env->GetSuperclass(env->GetObjectClass(thiz));
		if (cls == NULL || env->ExceptionCheck()) {
			return NULL;
		}
	} while (true);
	jlongArray argsArr = env->NewLongArray(argumentCount);
	jlong* args = new jlong[argumentCount];
	for (size_t i=0; i<argumentCount; i++) {
		args[i] = (long) arguments[i];
	}
	env->SetLongArrayRegion(argsArr,0,argumentCount,args);

	long objret = env->CallLongMethod(thiz, mid, (jlong)ctx, (jlong)constructor,
			argsArr, (jlong)exception);

	delete args;
	jvm->DetachCurrentThread();
	return (JSObjectRef)objret;
}

NATIVE(JSObject,jlong,makeFunctionWithCallback) (PARAMS, jlong ctx, jlong name) {
	Function *function = new Function(env,thiz, (JSContextWrapper*)ctx,
		(JSStringRef)name);
	return function->getObjRef();
}
NATIVE(JSObject,void,releaseFunctionWithCallback) (PARAMS, jlong ctx, jlong function) {
	ctx = (jlong) ((JSContextWrapper *)ctx)->context;
	Function::release((JSContextRef)ctx, (JSObjectRef)function);
}

NATIVE(JSObject,jobject,makeArray) (PARAMS, jlong ctx, jlongArray args) {
	JSValueRef exception = NULL;

	int i;
	jsize len = env->GetArrayLength(args);
	jlong *values = env->GetLongArrayElements(args, 0);
	JSValueRef* elements = new JSValueRef[len];
	for (i=0; i<len; i++) {
		elements[i] = (JSValueRef) values[i];
	}
	env->ReleaseLongArrayElements(args, values, 0);

	jclass ret = env->FindClass("org/liquidplayer/webkit/javascriptcore/JNIReturnObject");
	jmethodID cid = env->GetMethodID(ret,"<init>","()V");
	jobject out = env->NewObject(ret, cid);

	jfieldID fid = env->GetFieldID(ret , "reference", "J");

	JSContextWrapper *wrapper = (JSContextWrapper *)ctx;
	struct msg_t { JSContextRef ctx; size_t len; JSValueRef *elements;
		JSValueRef *exception; JSObjectRef objRef; };
	msg_t msg = { wrapper->context, (size_t)len, elements, &exception, (JSObjectRef)NULL};
	wrapper->worker_q->sync([](void *msg) {
		msg_t *m = (msg_t *)msg;
		m->objRef = JSObjectMakeArray(m->ctx, m->len, (m->len==0)?NULL:m->elements,
			m->exception);
	}, &msg);

	env->SetLongField( out, fid, (long)msg.objRef );

	fid = env->GetFieldID(ret , "exception", "J");
	env->SetLongField( out, fid, (long) exception);

	delete [] elements;
	return out;
}

NATIVE(JSObject,jobject,makeDate) (PARAMS, jlong ctx, jlongArray args) {
	JSValueRef exception = NULL;

	int i;
	jsize len = env->GetArrayLength(args);
	jlong *values = env->GetLongArrayElements(args, 0);
	JSValueRef* elements = new JSValueRef[len];
	for (i=0; i<len; i++) {
		elements[i] = (JSValueRef) values[i];
	}
	env->ReleaseLongArrayElements(args, values, 0);

	jclass ret = env->FindClass("org/liquidplayer/webkit/javascriptcore/JNIReturnObject");
	jmethodID cid = env->GetMethodID(ret,"<init>","()V");
	jobject out = env->NewObject(ret, cid);

	jfieldID fid = env->GetFieldID(ret , "reference", "J");

	JSContextWrapper *wrapper = (JSContextWrapper *)ctx;
	struct msg_t { JSContextRef ctx; size_t len; JSValueRef *elements;
		JSValueRef *exception; JSObjectRef objRef; };
	msg_t msg = { wrapper->context, (size_t)len, elements, &exception, (JSObjectRef)NULL};
	wrapper->worker_q->sync([](void *msg) {
		msg_t *m = (msg_t *)msg;
		m->objRef = JSObjectMakeDate(m->ctx, m->len, (m->len==0)?NULL:m->elements,
			m->exception);
	}, &msg);
	env->SetLongField( out, fid, (long) msg.objRef );

	fid = env->GetFieldID(ret , "exception", "J");
	env->SetLongField( out, fid, (long) exception);

	delete [] elements;
	return out;
}

NATIVE(JSObject,jobject,makeError) (PARAMS, jlong ctx, jlongArray args) {
	JSValueRef exception = NULL;

	int i;
	jsize len = env->GetArrayLength(args);
	jlong *values = env->GetLongArrayElements(args, 0);
	JSValueRef* elements = new JSValueRef[len];
	for (i=0; i<len; i++) {
		elements[i] = (JSValueRef) values[i];
	}
	env->ReleaseLongArrayElements(args, values, 0);

	jclass ret = env->FindClass("org/liquidplayer/webkit/javascriptcore/JNIReturnObject");
	jmethodID cid = env->GetMethodID(ret,"<init>","()V");
	jobject out = env->NewObject(ret, cid);

	jfieldID fid = env->GetFieldID(ret , "reference", "J");

	JSContextWrapper *wrapper = (JSContextWrapper *)ctx;
	struct msg_t { JSContextRef ctx; size_t len; JSValueRef* elements;
		JSValueRef *exception; JSObjectRef objRef; };
	msg_t msg = { wrapper->context, (size_t)len, elements, &exception, (JSObjectRef)NULL};
	wrapper->worker_q->sync([](void *msg) {
		msg_t *m = (msg_t *)msg;
		m->objRef = JSObjectMakeError(m->ctx, m->len, (m->len==0)?NULL:m->elements,
			m->exception);
	}, &msg);
	env->SetLongField( out, fid, (long) msg.objRef );

	fid = env->GetFieldID(ret , "exception", "J");
	env->SetLongField( out, fid, (long) exception);

	delete elements;
	return out;
}

NATIVE(JSObject,jobject,makeRegExp) (PARAMS, jlong ctx, jlongArray args) {
	JSValueRef exception = NULL;

	int i;
	jsize len = env->GetArrayLength(args);
	jlong *values = env->GetLongArrayElements(args, 0);
	JSValueRef* elements = new JSValueRef[len];
	for (i=0; i<len; i++) {
		elements[i] = (JSValueRef) values[i];
	}
	env->ReleaseLongArrayElements(args, values, 0);

	jclass ret = env->FindClass("org/liquidplayer/webkit/javascriptcore/JNIReturnObject");
	jmethodID cid = env->GetMethodID(ret,"<init>","()V");
	jobject out = env->NewObject(ret, cid);

	jfieldID fid = env->GetFieldID(ret , "reference", "J");

	JSContextWrapper *wrapper = (JSContextWrapper *)ctx;
	struct msg_t { JSContextRef ctx; size_t len; JSValueRef* elements;
		JSValueRef *exception; JSObjectRef objRef; };
	msg_t msg = { wrapper->context, (size_t)len, elements, &exception, (JSObjectRef)NULL};
	wrapper->worker_q->sync([](void *msg) {
		msg_t *m = (msg_t *)msg;
		m->objRef = JSObjectMakeRegExp(m->ctx, m->len, (m->len==0)?NULL:m->elements,
			m->exception);
	}, &msg);
	env->SetLongField( out, fid, (long) msg.objRef );

	fid = env->GetFieldID(ret , "exception", "J");
	env->SetLongField( out, fid, (long) exception);

	delete elements;
	return out;
}

NATIVE(JSObject,jobject,makeFunction) (PARAMS, jlong ctx, jlong name,
		jlongArray parameterNames, jlong body, jlong sourceURL, jint startingLineNumber) {
	JSContextWrapper *wrapper = (JSContextWrapper *)ctx;
	JSValueRef exception = NULL;

	int i;
	jsize len = env->GetArrayLength(parameterNames);
	jlong *parameters = env->GetLongArrayElements(parameterNames, 0);
	JSStringRef* parameterNameArr = new JSStringRef[len];
	for (i=0; i<len; i++) {
		parameterNameArr[i] = (JSStringRef) parameters[i];
	}
	env->ReleaseLongArrayElements(parameterNames, parameters, 0);

	jclass ret = env->FindClass("org/liquidplayer/webkit/javascriptcore/JNIReturnObject");
	jmethodID cid = env->GetMethodID(ret,"<init>","()V");
	jobject out = env->NewObject(ret, cid);

	jfieldID fid = env->GetFieldID(ret , "reference", "J");

	struct msg_t {
		JSContextRef ctx;
		JSStringRef  name;
		unsigned     len;
		JSStringRef* parameterNameArr;
		JSStringRef  body;
		JSStringRef  sourceURL;
		int          startingLineNumber;
		JSValueRef*  exception;
		long         lval;
	};
	msg_t msg = {
		wrapper->context,
		(JSStringRef)name,
		(unsigned)len,
		(len==0)?NULL:parameterNameArr,
		(JSStringRef) body,
		(JSStringRef) sourceURL,
		(int)startingLineNumber,
		&exception,
		0L
    };
	wrapper->dispatch_q->sync([](void *msg){
		msg_t *m = (msg_t *)msg;
		m->lval = (long) JSObjectMakeFunction(
			m->ctx, m->name, m->len, m->parameterNameArr,
			m->body, m->sourceURL, m->startingLineNumber, m->exception);
	}, &msg);
	env->SetLongField( out, fid, msg.lval);

	fid = env->GetFieldID(ret , "exception", "J");
	env->SetLongField( out, fid, (long) exception);

	delete parameterNameArr;
	return out;
}

NATIVE(JSObject,jlong,getPrototype) (PARAMS, jlong ctx, jlong object) {
	JSContextWrapper *wrapper = (JSContextWrapper *)ctx;
	struct msg_t { JSContextRef ctx; JSObjectRef object; JSValueRef ret; };
	msg_t msg = { wrapper->context, (JSObjectRef)object, (JSValueRef) NULL};
	wrapper->worker_q->sync([](void *msg) {
		msg_t *m = (msg_t *)msg;
		m->ret = JSObjectGetPrototype(m->ctx, m->object);
	}, &msg);
	return (long) msg.ret;
}

NATIVE(JSObject,void,setPrototype) (PARAMS, jlong ctx, jlong object, jlong value) {
	JSContextWrapper *wrapper = (JSContextWrapper *)ctx;
	struct msg_t { JSContextRef ctx; JSObjectRef object; JSValueRef value; };
    msg_t msg = { wrapper->context, (JSObjectRef) object, (JSValueRef)value };
	wrapper->dispatch_q->sync([](void *msg){
		msg_t *m = (msg_t *)msg;
		JSObjectSetPrototype(m->ctx, m->object, m->value);
	}, &msg);
}

NATIVE(JSObject,jboolean,hasProperty) (PARAMS, jlong ctx, jlong object, jlong propertyName) {
	JSContextWrapper *wrapper = (JSContextWrapper *)ctx;
	struct msg_t { JSContextRef ctx; JSObjectRef object; JSStringRef propertyName;
		bool ret; };
    msg_t msg = { wrapper->context, (JSObjectRef) object, (JSStringRef)propertyName,
    	false };
	wrapper->worker_q->sync([](void *msg){
		msg_t *m = (msg_t *)msg;
		m->ret = JSObjectHasProperty(m->ctx, m->object, m->propertyName);
	}, &msg);
	return msg.ret;
}

NATIVE(JSObject,jobject,getProperty) (PARAMS, jlong ctx, jlong object,
	jlong propertyName) {

	JSValueRef exception = NULL;

	jclass ret = env->FindClass("org/liquidplayer/webkit/javascriptcore/JNIReturnObject");
	jmethodID cid = env->GetMethodID(ret,"<init>","()V");
	jobject out = env->NewObject(ret, cid);

	jfieldID fid = env->GetFieldID(ret , "reference", "J");

	JSContextWrapper *wrapper = (JSContextWrapper *)ctx;
	struct msg_t { JSContextRef ctx; JSObjectRef object; JSStringRef propertyName;
		JSValueRef *exception; JSValueRef ret; };
    msg_t msg = { wrapper->context, (JSObjectRef) object, (JSStringRef)propertyName,
    	&exception, (JSValueRef)NULL };
	wrapper->worker_q->sync([](void *msg){
		msg_t *m = (msg_t *)msg;
		m->ret = JSObjectGetProperty(m->ctx, m->object, m->propertyName, m->exception);
	}, &msg);

	env->SetLongField( out, fid, (long) msg.ret);

	fid = env->GetFieldID(ret , "exception", "J");
	env->SetLongField( out, fid, (long) exception);

	return out;
}

NATIVE(JSObject,jobject,setProperty) (PARAMS, jlong ctx, jlong object, jlong propertyName,
	jlong value, jint attributes) {
	
	JSValueRef exception = NULL;

	jclass ret = env->FindClass("org/liquidplayer/webkit/javascriptcore/JNIReturnObject");
	jmethodID cid = env->GetMethodID(ret,"<init>","()V");
	jobject out = env->NewObject(ret, cid);

	JSContextWrapper *wrapper = (JSContextWrapper *)ctx;
	struct msg_t { JSContextRef ctx; JSObjectRef object; JSStringRef propertyName;
		JSValueRef value; JSPropertyAttributes attributes; JSValueRef* exception; };
    msg_t msg = { wrapper->context, (JSObjectRef) object, (JSStringRef) propertyName,
		(JSValueRef)value, (JSPropertyAttributes)attributes, &exception };
	wrapper->dispatch_q->sync([](void *msg){
		msg_t *m = (msg_t *)msg;
		JSObjectSetProperty(m->ctx, m->object, m->propertyName,
			m->value, m->attributes, m->exception);
	}, &msg);
	
	jfieldID fid = env->GetFieldID(ret , "exception", "J");
	env->SetLongField( out, fid, (long) exception);

	return out;
}

NATIVE(JSObject,jobject,deleteProperty) (PARAMS, jlong ctx, jlong object,
	jlong propertyName) {
	
	JSValueRef exception = NULL;

	jclass ret = env->FindClass("org/liquidplayer/webkit/javascriptcore/JNIReturnObject");
	jmethodID cid = env->GetMethodID(ret,"<init>","()V");
	jobject out = env->NewObject(ret, cid);

	jfieldID fid = env->GetFieldID(ret ,"bool", "Z");

	JSContextWrapper *wrapper = (JSContextWrapper *)ctx;
	struct msg_t { JSContextRef ctx; JSObjectRef object; JSStringRef propertyName;
		JSValueRef* exception; bool bval; };
        msg_t msg = { wrapper->context, (JSObjectRef) object, (JSStringRef) propertyName,
		&exception, false };
	wrapper->dispatch_q->sync([](void *msg){
		msg_t *m = (msg_t *)msg;
		m->bval = (bool) JSObjectDeleteProperty(m->ctx, m->object,
			m->propertyName, m->exception);
	}, &msg);
	env->SetBooleanField( out, fid, msg.bval);

	fid = env->GetFieldID(ret , "exception", "J");
	env->SetLongField( out, fid, (long) exception);

	return out;
}

NATIVE(JSObject,jobject,getPropertyAtIndex) (PARAMS, jlong ctx, jlong object,
	jint propertyIndex) {
	
	JSValueRef exception = NULL;

	jclass ret = env->FindClass("org/liquidplayer/webkit/javascriptcore/JNIReturnObject");
	jmethodID cid = env->GetMethodID(ret,"<init>","()V");
	jobject out = env->NewObject(ret, cid);

	jfieldID fid = env->GetFieldID(ret , "reference", "J");
	
	JSContextWrapper *wrapper = (JSContextWrapper *)ctx;
	struct msg_t { JSContextRef ctx; JSObjectRef object; unsigned propertyIndex;
		JSValueRef *exception; JSValueRef ret; };
    msg_t msg = { wrapper->context, (JSObjectRef) object, (unsigned)propertyIndex,
    	&exception, (JSValueRef)NULL };
	wrapper->worker_q->sync([](void *msg){
		msg_t *m = (msg_t *)msg;
		m->ret = JSObjectGetPropertyAtIndex(m->ctx, m->object, m->propertyIndex,
			m->exception);
	}, &msg);

	env->SetLongField( out, fid, (long) msg.ret );

	fid = env->GetFieldID(ret , "exception", "J");
	env->SetLongField( out, fid, (long) exception);

	return out;
}

NATIVE(JSObject,jobject,setPropertyAtIndex) (PARAMS, jlong ctx, jlong object,
	jint propertyIndex, jlong value) {

	JSValueRef exception = NULL;

	jclass ret = env->FindClass("org/liquidplayer/webkit/javascriptcore/JNIReturnObject");
	jmethodID cid = env->GetMethodID(ret,"<init>","()V");
	jobject out = env->NewObject(ret, cid);

	jfieldID fid = env->GetFieldID(ret , "reference", "J");

	JSContextWrapper *wrapper = (JSContextWrapper *)ctx;
	struct msg_t { JSContextRef ctx; JSObjectRef object; unsigned propertyIndex;
		JSValueRef value; JSValueRef* exception; };
    msg_t msg = { wrapper->context, (JSObjectRef) object, (unsigned) propertyIndex,
		(JSValueRef)value, &exception };
	wrapper->dispatch_q->sync([](void *msg){
		msg_t *m = (msg_t *)msg;
		JSObjectSetPropertyAtIndex(m->ctx, m->object, m->propertyIndex,
			m->value, m->exception);
	}, &msg);

	fid = env->GetFieldID(ret , "exception", "J");
	env->SetLongField( out, fid, (long) exception);

	return out;
}

NATIVE(JSObject,jlong,getPrivate) (PARAMS, jlong object) {
	return (long) JSObjectGetPrivate((JSObjectRef) object);
}

NATIVE(JSObject,jboolean,setPrivate) (PARAMS, jlong object, jlong data) {
	return JSObjectSetPrivate((JSObjectRef) object, (void*) data);
}

NATIVE(JSObject,jboolean,isFunction) (PARAMS, jlong ctx, jlong object) {
	JSContextWrapper *wrapper = (JSContextWrapper *)ctx;
	struct msg_t { JSContextRef ctx; JSObjectRef object; bool ret; };
    msg_t msg = { wrapper->context, (JSObjectRef) object, false };
	wrapper->worker_q->sync([](void *msg){
		msg_t *m = (msg_t *)msg;
		m->ret = JSObjectIsFunction(m->ctx, m->object);
	}, &msg);
	return msg.ret;
}

NATIVE(JSObject,jobject,callAsFunction) (PARAMS, jlong ctx, jlong object,
	jlong thisObject, jlongArray args) {
	JSValueRef exception = NULL;

	int i;
	jsize len = env->GetArrayLength(args);
	jlong *values = env->GetLongArrayElements(args, 0);
	JSValueRef* elements = new JSValueRef[len];
	for (i=0; i<len; i++) {
		elements[i] = (JSValueRef) values[i];
	}
	env->ReleaseLongArrayElements(args, values, 0);

	jclass ret = env->FindClass("org/liquidplayer/webkit/javascriptcore/JNIReturnObject");
	jmethodID cid = env->GetMethodID(ret,"<init>","()V");
	jobject out = env->NewObject(ret, cid);

	jfieldID fid = env->GetFieldID(ret , "reference", "J");

	JSContextWrapper *wrapper = (JSContextWrapper *)ctx;
	struct msg_t{JSContextRef ctx; JSObjectRef object; JSObjectRef thisObject; size_t len;
		JSValueRef* elements; JSValueRef* exception; long lval; };
        msg_t msg = { wrapper->context, (JSObjectRef) object, (JSObjectRef) thisObject,
		(size_t)len, (len==0)?NULL:elements, &exception, 0L };
	wrapper->dispatch_q->sync([](void *msg){
		msg_t *m = (msg_t *)msg;
		m->lval = (long) JSObjectCallAsFunction(m->ctx, m->object,
			m->thisObject, m->len, m->elements, m->exception);
	}, &msg);
	env->SetLongField( out, fid, msg.lval);

	fid = env->GetFieldID(ret , "exception", "J");
	env->SetLongField( out, fid, (long) exception);

	delete elements;
	return out;
}

NATIVE(JSObject,jboolean,isConstructor) (PARAMS, jlong ctx, jlong object) {
	JSContextWrapper *wrapper = (JSContextWrapper *)ctx;
	struct msg_t { JSContextRef ctx; JSObjectRef object; bool ret; };
    msg_t msg = { wrapper->context, (JSObjectRef) object, false };
	wrapper->worker_q->sync([](void *msg){
		msg_t *m = (msg_t *)msg;
		m->ret = JSObjectIsConstructor(m->ctx, m->object);
	}, &msg);
	return msg.ret;
}

NATIVE(JSObject,jobject,callAsConstructor) (PARAMS, jlong ctx, jlong object,
	jlongArray args) {

	JSValueRef exception = NULL;

	int i;
	jsize len = env->GetArrayLength(args);
	jlong *values = env->GetLongArrayElements(args, 0);
	JSValueRef* elements = new JSValueRef[len];
	for (i=0; i<len; i++) {
		elements[i] = (JSValueRef) values[i];
	}
	env->ReleaseLongArrayElements(args, values, 0);

	jclass ret = env->FindClass("org/liquidplayer/webkit/javascriptcore/JNIReturnObject");
	jmethodID cid = env->GetMethodID(ret,"<init>","()V");
	jobject out = env->NewObject(ret, cid);

	jfieldID fid = env->GetFieldID(ret , "reference", "J");

	JSContextWrapper *wrapper = (JSContextWrapper *)ctx;
	struct msg_t { JSContextRef ctx; JSObjectRef object; size_t len;
		JSValueRef* elements; JSValueRef* exception; long lval; };
        msg_t msg = { wrapper->context, (JSObjectRef) object,
		(size_t)len, (len==0)?NULL:elements, &exception, 0L };
	wrapper->dispatch_q->sync([](void *msg){
		msg_t *m = (msg_t *)msg;
		m->lval = (long) JSObjectCallAsConstructor(m->ctx, m->object,
			m->len, m->elements, m->exception);
	}, &msg);
	env->SetLongField( out, fid, msg.lval);

	fid = env->GetFieldID(ret , "exception", "J");
	env->SetLongField( out, fid, (long) exception);

	delete elements;
	return out;
}

NATIVE(JSObject,jlong,copyPropertyNames) (PARAMS, jlong ctx, jlong object) {
	JSContextWrapper *wrapper = (JSContextWrapper *)ctx;
	struct msg_t { JSContextRef ctx; JSObjectRef object; JSPropertyNameArrayRef ret; };
    msg_t msg = { wrapper->context, (JSObjectRef) object, (JSPropertyNameArrayRef)NULL };
	wrapper->worker_q->sync([](void *msg){
		msg_t *m = (msg_t *)msg;
		m->ret = JSObjectCopyPropertyNames(m->ctx, m->object);
	}, &msg);
	return (jlong)msg.ret;
}

NATIVE(JSObject,jlongArray,getPropertyNames) (PARAMS,jlong propertyNameArray) {
	size_t count = JSPropertyNameArrayGetCount((JSPropertyNameArrayRef)propertyNameArray);
	jlongArray retArray = env->NewLongArray(count);
	jlong* stringRefs = new jlong[count];
	for (size_t i=0; i<count; i++) {
		stringRefs[i] = (long) JSPropertyNameArrayGetNameAtIndex(
 			(JSPropertyNameArrayRef)propertyNameArray, i);
	}
	env->SetLongArrayRegion(retArray,0,count,stringRefs);

	return retArray;
}

NATIVE(JSObject,void,releasePropertyNames) (PARAMS, jlong propertyNameArray) {
	JSPropertyNameArrayRelease((JSPropertyNameArrayRef)propertyNameArray);
}
