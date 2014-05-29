//
// JSContext.java
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
package org.liquidplayer.webkit.javascriptcore;

import java.util.HashMap;
import java.util.Map;

public class JSContext extends JSObject {

	protected Long ctx;
	
	public JSContext() {
		ctx = create();
		context = this;
		valueRef = getGlobalObject(ctx);
	}
	public JSContext(JSContextGroup inGroup) {
		ctx = createInGroup(inGroup.groupRef());
		context = this;
		valueRef = getGlobalObject(ctx);
	}
	public JSContext(Class<?> iface) throws JSException {
		ctx = create();
		context = this;
		valueRef = getGlobalObject(ctx);
		this.initJSInterface(this, iface, null);
	}
	public JSContext(JSContextGroup inGroup, Class<?> iface) throws JSException {
		ctx = createInGroup(inGroup.groupRef());
		context = this;
		valueRef = getGlobalObject(ctx);
		this.initJSInterface(this, iface, null);
	}
	@Override
	protected void finalize() throws Throwable {
		if (ctx!=null) release(ctx);
		super.finalize();
	}
	
	public JSContextGroup getGroup() {
		Long g = getGroup(ctx);
		if (g==null || g==0) return null;
		return new JSContextGroup(g);
	}
	
	public Long ctxRef() {
		return ctx;
	}
	
	public JSValue evaluateScript(String script, JSObject thiz,
			String sourceURL, int startingLineNumber) throws JSException {
		JNIReturnObject jni = evaluateScript(ctx, new JSString(script).stringRef(),
				(thiz==null)?0L:thiz.valueRef(), (sourceURL==null)?0L:new JSString(sourceURL).stringRef(),
				startingLineNumber);
		if (jni.exception!=0) {
			throw (new JSException(new JSValue(jni.exception,this)));
		}
		return new JSValue(jni.reference,this);
	}
	
	public JSValue evaluateScript(String script, JSObject thiz) throws JSException {
		return evaluateScript(script,thiz,null,0);
	}
	public JSValue evaluateScript(String script) throws JSException {
		return evaluateScript(script,null,null,0);
	}
	
	private Map<Long,JSObject> objects = new HashMap<Long,JSObject>();

	public void persistObject(JSObject obj) {
		objects.put(obj.valueRef(), obj);
	}
	public void finalizeObject(JSObject obj) {
		objects.remove(obj.valueRef());
	}
	public JSObject getObjectFromRef(long objRef) {
		JSObject obj = objects.get(objRef);
		if (obj==null) {
			obj = new JSObject(objRef, this);
		}
		return obj;
	}
	
	protected native long create();
	protected native long createInGroup(long group);
	protected native long retain(long ctx);
	protected native long release(long ctx);
	protected native long getGroup(long ctx);
	protected native long getGlobalObject(long ctx);
	protected native JNIReturnObject evaluateScript(long ctx, long script, long thisObject, long sourceURL, int startingLineNumber);
	protected native JNIReturnObject checkScriptSyntax(long ctx, long script, long sourceURL, int startingLineNumber);
	protected native void garbageCollect(long ctx);

}
