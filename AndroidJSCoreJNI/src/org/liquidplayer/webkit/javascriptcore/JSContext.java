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

/**
 * Wraps a JavaScriptCore context 
 */
public class JSContext extends JSObject {

	protected Long ctx;
	
	/**
	 * Creates a new JavaScript context
	 */
	public JSContext() {
		ctx = create();
		context = this;
		valueRef = getGlobalObject(ctx);
		protect(context,valueRef);
	}
	/**
	 * Creates a new JavaScript context in the context group 'inGroup'.
	 * @param inGroup  The context group to create the context in
	 */
	public JSContext(JSContextGroup inGroup) {
		ctx = createInGroup(inGroup.groupRef());
		context = this;
		valueRef = getGlobalObject(ctx);
		protect(context,valueRef);
	}
	/**
	 * Creates a JavaScript context, and defines the global object with interface 'iface'.  This
	 * object must implement 'iface'.  The methods in 'iface' will be exposed to the JavaScript environment.
	 * @param iface  The interface to expose to JavaScript
	 * @throws JSException
	 */
	public JSContext(Class<?> iface) throws JSException {
		ctx = create();
		context = this;
		valueRef = getGlobalObject(ctx);
		this.initJSInterface(this, iface, null);
	}
	/**
	 * Creates a JavaScript context in context group 'inGroup', and defines the global object
	 * with interface 'iface'.  This object must implement 'iface'.  The methods in 'iface' will 
	 * be exposed to the JavaScript environment.
	 * @param inGroup  The context group to create the context in
	 * @param iface  The interface to expose to JavaScript
	 * @throws JSException
	 */
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
	
	/**
	 * Gets the context group to which this context belongs.
	 * @return  The context group to which this context belongs
	 */
	public JSContextGroup getGroup() {
		Long g = getGroup(ctx);
		if (g==null || g==0) return null;
		return new JSContextGroup(g);
	}
	
	/**
	 * Gets the JavaScriptCore context reference
	 * @return  the JavaScriptCore context reference
	 */
	public Long ctxRef() {
		return ctx;
	}
	
	/**
	 * Executes a the JavaScript code in 'script' in this context
	 * @param script  The code to execute
	 * @param thiz  The 'this' object
	 * @param sourceURL  The URI of the source file, only used for reporting in stack trace (optional)
	 * @param startingLineNumber  The beginning line number, only used for reporting in stack trace (optional)
	 * @return  The return value returned by 'script'
	 * @throws JSException
	 */
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
	
	/**
	 * Executes a the JavaScript code in 'script' in this context
	 * @param script  The code to execute
	 * @param thiz  The 'this' object
	 * @return  The return value returned by 'script'
	 * @throws JSException
	 */
	public JSValue evaluateScript(String script, JSObject thiz) throws JSException {
		return evaluateScript(script,thiz,null,0);
	}
	/**
	 * Executes a the JavaScript code in 'script' in this context
	 * @param script  The code to execute
	 * @return  The return value returned by 'script'
	 * @throws JSException
	 */
	public JSValue evaluateScript(String script) throws JSException {
		return evaluateScript(script,null,null,0);
	}
	
	private Map<Long,JSObject> objects = new HashMap<Long,JSObject>();

	/**
	 * Keeps a reference to an object in this context.  This is used so that only one
	 * Java object instance wrapping a JavaScript object is maintained at any time.  This way,
	 * local variables in the Java object will stay wrapped around all returns of the same
	 * instance.  This is handled by JSObject, and should not need to be called by clients.
	 * @param obj  The object with which to associate with this context
	 */
	public void persistObject(JSObject obj) {
		objects.put(obj.valueRef(), obj);
	}
	/**
	 * Removes a reference to an object in this context.  Should only be used from the 'finalize'
	 * object method.  This is handled by JSObject, and should not need to be called by clients.
	 * @param obj
	 */
	public void finalizeObject(JSObject obj) {
		objects.remove(obj.valueRef());
	}
	/**
	 * Reuses a stored reference to a JavaScript object if it exists, otherwise, it creates the
	 * reference.
	 * @param objRef
	 * @return The JSObject representing the reference
	 */
	public JSObject getObjectFromRef(long objRef) {
		JSObject obj = objects.get(objRef);
		if (obj==null) {
			obj = new JSObject(objRef, this);
		}
		return obj;
	}
	/**
	 * Forces JavaScript garbage collection on this context
	 */
	public void garbageCollect() {
		garbageCollect(ctx);
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
	
	static {
		System.loadLibrary("JavaScriptCoreWrapper");
		System.loadLibrary("JavaScriptCore");
	}
}
