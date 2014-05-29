//
// JSObject.cpp
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
#include <android/log.h>
#include <stdio.h>
#include <map>

NATIVE(JSObject,jlong,make) (PARAMS, jlong ctx, jlong data) {
	return (long) JSObjectMake((JSContextRef) ctx, (JSClassRef) NULL, (void*)data);
}

class Instance {
private:
	JNIEnv* env;
	jobject thiz;
	JSObjectRef objRef;
	static std::map<JSObjectRef,Instance *> objMap;
	JSClassRef classRef;

	static void StaticFinalizeCallback(JSObjectRef object);
	void FinalizeCallback(JSObjectRef object);
public:
	Instance(JNIEnv *env, jobject thiz, JSContextRef ctx);
	virtual ~Instance();
	long getObjRef() { return (long) objRef; }
};
Instance::Instance(JNIEnv *env, jobject thiz, JSContextRef ctx) : env(env) {
	JSClassDefinition definition = kJSClassDefinitionEmpty;
	definition.finalize = StaticFinalizeCallback;
	classRef = JSClassCreate(&definition);
	this->thiz = env->NewGlobalRef(thiz);
	objRef = JSObjectMake((JSContextRef) ctx, (JSClassRef) NULL, NULL);
	objMap[objRef] = this;
}
Instance::~Instance() {
	JSClassRelease(classRef);
	env->DeleteGlobalRef(thiz);
	objMap[objRef] = NULL;
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
	}
NATIVE(JSObject,jlong,makeWithFinalizeCallback) (PARAMS, jlong ctx) {
	Instance *instance = new Instance(env,thiz, (JSContextRef)ctx);
	return instance->getObjRef();
}

class Function {
	private:
		JNIEnv* env;
		jobject thiz;
		JSObjectRef objRef;
		JSClassRef classRef;
		JSClassDefinition definition;

		static std::map<JSObjectRef,Function *> objMap;

		static JSValueRef StaticFunctionCallback(JSContextRef ctx, JSObjectRef function, JSObjectRef thisObject,
				size_t argumentCount, const JSValueRef arguments[], JSValueRef* exception);
		static JSObjectRef StaticConstructorCallback(JSContextRef ctx, JSObjectRef constructor,
				size_t argumentCount, const JSValueRef arguments[], JSValueRef* exception);

		JSObjectRef ConstructorCallback(JSContextRef ctx, JSObjectRef constructor,
				size_t argumentCount, const JSValueRef arguments[], JSValueRef* exception);
		JSValueRef FunctionCallback(JSContextRef ctx, JSObjectRef function, JSObjectRef thisObject,
				size_t argumentCount, const JSValueRef arguments[], JSValueRef* exception);

	public:
		Function(JNIEnv *env, jobject thiz, JSContextRef ctx, JSStringRef name);
		virtual ~Function();
		long getObjRef() { return (long) objRef; }
		static void release(JSContextRef ctx, JSObjectRef function);
};
Function::Function(JNIEnv* env, jobject thiz, JSContextRef ctx, JSStringRef name) : env(env) {
	definition = kJSClassDefinitionEmpty;
	definition.callAsFunction = StaticFunctionCallback;
	definition.callAsConstructor = StaticConstructorCallback;
	classRef = JSClassCreate(&definition);

	this->thiz = env->NewGlobalRef(thiz);
	objRef = JSObjectMake(ctx, classRef, NULL);
	objMap[objRef] = this;
}
Function::~Function() {
	JSClassRelease(classRef);
	env->DeleteGlobalRef(thiz);
}
void Function::release(JSContextRef ctx, JSObjectRef function) {
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
	for (int i=0; i<argumentCount; i++) {
		args[i] = (long) arguments[i];
	}
	env->SetLongArrayRegion(argsArr,0,argumentCount,args);

	long objret = env->CallLongMethod(thiz, mid, (jlong)ctx, (jlong)function, (jlong)thisObject,
			argsArr, (jlong)exception);

	delete args;
	return (JSObjectRef)objret;
}
JSObjectRef Function::ConstructorCallback(JSContextRef ctx, JSObjectRef constructor,
		size_t argumentCount, const JSValueRef arguments[], JSValueRef* exception)
{
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
	for (int i=0; i<argumentCount; i++) {
		args[i] = (long) arguments[i];
	}
	env->SetLongArrayRegion(argsArr,0,argumentCount,args);

	long objret = env->CallLongMethod(thiz, mid, (jlong)ctx, (jlong)constructor,
			argsArr, (jlong)exception);

	delete args;
	return (JSObjectRef)objret;
}

NATIVE(JSObject,jlong,makeFunctionWithCallback) (PARAMS, jlong ctx, jlong name) {
	Function *function = new Function(env,thiz, (JSContextRef)ctx, (JSStringRef)name);
	return function->getObjRef();
}
NATIVE(JSObject,void,releaseFunctionWithCallback) (PARAMS, jlong ctx, jlong function) {
	Function::release((JSContextRef)ctx, (JSObjectRef)function);
}

NATIVE(JSObject,jobject,makeArray) (PARAMS, jlong ctx, jlongArray args) {
	JSValueRef exception = NULL;

	int i, sum = 0;
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
	env->SetLongField( out, fid, (long) JSObjectMakeArray((JSContextRef) ctx, (size_t)len, (len==0)?NULL:elements, &exception));

	fid = env->GetFieldID(ret , "exception", "J");
	env->SetLongField( out, fid, (long) exception);

	delete elements;
	return out;
}

NATIVE(JSObject,jobject,makeDate) (PARAMS, jlong ctx, jlongArray args) {
	JSValueRef exception = NULL;

	int i, sum = 0;
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
	env->SetLongField( out, fid, (long) JSObjectMakeDate((JSContextRef) ctx, (size_t)len, (len==0)?NULL:elements, &exception));

	fid = env->GetFieldID(ret , "exception", "J");
	env->SetLongField( out, fid, (long) exception);

	delete elements;
	return out;
}

NATIVE(JSObject,jobject,makeError) (PARAMS, jlong ctx, jlongArray args) {
	JSValueRef exception = NULL;

	int i, sum = 0;
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
	env->SetLongField( out, fid, (long) JSObjectMakeError((JSContextRef) ctx, (size_t)len, (len==0)?NULL:elements, &exception));

	fid = env->GetFieldID(ret , "exception", "J");
	env->SetLongField( out, fid, (long) exception);

	delete elements;
	return out;
}

NATIVE(JSObject,jobject,makeRegExp) (PARAMS, jlong ctx, jlongArray args) {
	JSValueRef exception = NULL;

	int i, sum = 0;
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
	env->SetLongField( out, fid, (long) JSObjectMakeRegExp((JSContextRef) ctx, (size_t)len, (len==0)?NULL:elements, &exception));

	fid = env->GetFieldID(ret , "exception", "J");
	env->SetLongField( out, fid, (long) exception);

	delete elements;
	return out;
}

NATIVE(JSObject,jobject,makeFunction) (PARAMS, jlong ctx, jlong name, jlongArray parameterNames,
		jlong body, jlong sourceURL, jint startingLineNumber) {
	JSValueRef exception = NULL;

	int i, sum = 0;
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
	env->SetLongField( out, fid,
			(long) JSObjectMakeFunction(
					(JSContextRef) ctx,
					(JSStringRef) name,
					(unsigned)len,
					(len==0)?NULL:parameterNameArr,
					(JSStringRef) body,
					(JSStringRef) sourceURL,
					(int)startingLineNumber,
					&exception));

	fid = env->GetFieldID(ret , "exception", "J");
	env->SetLongField( out, fid, (long) exception);

	delete parameterNameArr;
	return out;
}

NATIVE(JSObject,jlong,getPrototype) (PARAMS, jlong ctx, jlong object) {
	return (long) JSObjectGetPrototype((JSContextRef) ctx, (JSObjectRef) object);
}

NATIVE(JSObject,void,setPrototype) (PARAMS, jlong ctx, jlong object, jlong value) {
	JSObjectSetPrototype((JSContextRef) ctx, (JSObjectRef) object, (JSValueRef) value);
}

NATIVE(JSObject,jboolean,hasProperty) (PARAMS, jlong ctx, jlong object, jlong propertyName) {
	return JSObjectHasProperty((JSContextRef) ctx, (JSObjectRef) object, (JSStringRef) propertyName);
}

NATIVE(JSObject,jobject,getProperty) (PARAMS, jlong ctx, jlong object, jlong propertyName) {
	JSValueRef exception = NULL;

	jclass ret = env->FindClass("org/liquidplayer/webkit/javascriptcore/JNIReturnObject");
	jmethodID cid = env->GetMethodID(ret,"<init>","()V");
	jobject out = env->NewObject(ret, cid);

	jfieldID fid = env->GetFieldID(ret , "reference", "J");
	env->SetLongField( out, fid, (long) JSObjectGetProperty((JSContextRef) ctx, (JSObjectRef)object, (JSStringRef)propertyName,
			&exception));

	fid = env->GetFieldID(ret , "exception", "J");
	env->SetLongField( out, fid, (long) exception);

	return out;
}

NATIVE(JSObject,jobject,setProperty) (PARAMS, jlong ctx, jlong object, jlong propertyName, jlong value, jint attributes) {
	JSValueRef exception = NULL;

	jclass ret = env->FindClass("org/liquidplayer/webkit/javascriptcore/JNIReturnObject");
	jmethodID cid = env->GetMethodID(ret,"<init>","()V");
	jobject out = env->NewObject(ret, cid);

	JSObjectSetProperty((JSContextRef) ctx, (JSObjectRef) object, (JSStringRef) propertyName,
			(JSValueRef) value, (JSPropertyAttributes) attributes, &exception);
	jfieldID fid = env->GetFieldID(ret , "exception", "J");
	env->SetLongField( out, fid, (long) exception);

	return out;
}

NATIVE(JSObject,jobject,deleteProperty) (PARAMS, jlong ctx, jlong object, jlong propertyName) {
	JSValueRef exception = NULL;

	jclass ret = env->FindClass("org/liquidplayer/webkit/javascriptcore/JNIReturnObject");
	jmethodID cid = env->GetMethodID(ret,"<init>","()V");
	jobject out = env->NewObject(ret, cid);

	jfieldID fid = env->GetFieldID(ret ,"bool", "Z");
	env->SetBooleanField( out, fid, (bool) JSObjectDeleteProperty((JSContextRef) ctx, (JSObjectRef)object, (JSStringRef)propertyName,
			&exception));

	fid = env->GetFieldID(ret , "exception", "J");
	env->SetLongField( out, fid, (long) exception);

	return out;
}

NATIVE(JSObject,jobject,getPropertyAtIndex) (PARAMS, jlong ctx, jlong object, jint propertyIndex) {
	JSValueRef exception = NULL;

	jclass ret = env->FindClass("org/liquidplayer/webkit/javascriptcore/JNIReturnObject");
	jmethodID cid = env->GetMethodID(ret,"<init>","()V");
	jobject out = env->NewObject(ret, cid);

	jfieldID fid = env->GetFieldID(ret , "reference", "J");
	env->SetLongField( out, fid, (long) JSObjectGetPropertyAtIndex((JSContextRef) ctx, (JSObjectRef)object,
			(unsigned)propertyIndex, &exception));

	fid = env->GetFieldID(ret , "exception", "J");
	env->SetLongField( out, fid, (long) exception);

	return out;
}

NATIVE(JSObject,jobject,setPropertyAtIndex) (PARAMS, jlong ctx, jlong object, jint propertyIndex, jlong value) {
	JSValueRef exception = NULL;

	jclass ret = env->FindClass("org/liquidplayer/webkit/javascriptcore/JNIReturnObject");
	jmethodID cid = env->GetMethodID(ret,"<init>","()V");
	jobject out = env->NewObject(ret, cid);

	jfieldID fid = env->GetFieldID(ret , "reference", "J");
	JSObjectSetPropertyAtIndex((JSContextRef) ctx, (JSObjectRef)object, (unsigned)propertyIndex,
			(JSValueRef)value, &exception);

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
	return JSObjectIsFunction((JSContextRef) ctx, (JSObjectRef) object);
}

NATIVE(JSObject,jobject,callAsFunction) (PARAMS, jlong ctx, jlong object, jlong thisObject, jlongArray args) {
	JSValueRef exception = NULL;

	int i, sum = 0;
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
	env->SetLongField( out, fid, (long) JSObjectCallAsFunction((JSContextRef) ctx, (JSObjectRef)object,
			(JSObjectRef)thisObject, (size_t)len, (len==0)?NULL:elements, &exception));

	fid = env->GetFieldID(ret , "exception", "J");
	env->SetLongField( out, fid, (long) exception);

	delete elements;
	return out;
}

NATIVE(JSObject,jboolean,isConstructor) (PARAMS, jlong ctx, jlong object) {
	return JSObjectIsConstructor((JSContextRef) ctx, (JSObjectRef) object);
}

NATIVE(JSObject,jobject,callAsConstructor) (PARAMS, jlong ctx, jlong object, jlongArray args) {
	JSValueRef exception = NULL;

	int i, sum = 0;
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
	env->SetLongField( out, fid, (long) JSObjectCallAsConstructor((JSContextRef) ctx, (JSObjectRef)object,
			(size_t)len, (len==0)?NULL:elements, &exception));

	fid = env->GetFieldID(ret , "exception", "J");
	env->SetLongField( out, fid, (long) exception);

	delete elements;
	return out;
}

NATIVE(JSObject,jlongArray,getPropertyNames) (PARAMS,jlong ctx, jlong object) {
	JSPropertyNameArrayRef propertyNameArray = JSObjectCopyPropertyNames((JSContextRef) ctx, (JSObjectRef) object);

	size_t count = JSPropertyNameArrayGetCount(propertyNameArray);
	jlongArray retArray = env->NewLongArray(count);
	jlong* stringRefs = new jlong[count];
	for (size_t i=0; i<count; i++) {
		stringRefs[i] = (long) JSPropertyNameArrayGetNameAtIndex(propertyNameArray, i);
	}
	env->SetLongArrayRegion(retArray,0,count,stringRefs);

	JSPropertyNameArrayRelease(propertyNameArray);
	return retArray;
}
