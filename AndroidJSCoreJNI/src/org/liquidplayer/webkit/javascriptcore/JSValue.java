//
// JSValue.java
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

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.HashMap;

/**
 * A JavaScript value
 *
 */
public class JSValue {
	
	protected Long valueRef = 0L;
	protected JSContext context = null;
	
	/* Constructors */
	/**
	 * Creates an empty JSValue.  This can only be used by subclasses, and those
	 * subclasses must define 'context' and 'valueRef' themselves
	 */
	protected JSValue() {
	}
	/**
	 * Creates a new undefined JavaScript value
	 * @param ctx  The context in which to create the value
	 */
	public JSValue(JSContext ctx) {
		context = ctx;
		valueRef = makeUndefined(context.ctxRef());
		protect(ctx,valueRef);
	}
	/**
	 * Creates a new JavaScript value from a Java value.  Classes supported are:
	 * Boolean, Double, Integer, Long, String, and JSString.  Any other object will
	 * generate an undefined JavaScript value.
	 * @param ctx  The context in which to create the value
	 * @param val  The Java value
	 */
	public JSValue(JSContext ctx, Object val) {
		context = ctx;
		if (val == null) valueRef = makeNull(context.ctxRef());
		if (val instanceof Boolean) {
			valueRef = makeBoolean(context.ctxRef(), (Boolean)val);
		} else if (val instanceof Double) {
			valueRef = makeNumber(context.ctxRef(), (Double)val);
		} else if (val instanceof Integer) {
			valueRef = makeNumber(context.ctxRef(), ((Integer)val).doubleValue());
		} else if (val instanceof Long) {
			valueRef = makeNumber(context.ctxRef(), ((Long)val).doubleValue());
		} else if (val instanceof String) {
			JSString s = new JSString((String)val);
			valueRef = makeString(context.ctxRef(), s.stringRef);
		} else if (val instanceof JSString) {
			valueRef = makeString(context.ctxRef(), ((JSString)val).stringRef);
		} else {
			valueRef = makeUndefined(context.ctxRef());
		}
		protect(ctx,valueRef);
	}
	/*
	 * JavaScript values held by the Java environment must be protected from garbage collection
	 * in the JS environment.  If only the Java environment holds a reference to a JS object, the
	 * JS garbage collector will see this value as unreferenced and clean it.  So we must manage
	 * protecting and unprotecting values we hold references to on the Java side.
	 */
	private static class ValRef {
		public long valueRef;
		public JSContext ctx;
		public ValRef(JSContext ctx, long val) {
			this.ctx = ctx; valueRef = val;
		}
	}
	private static ReferenceQueue<JSValue> rq = new ReferenceQueue<JSValue>();
	private static HashMap<WeakReference<JSValue>,ValRef> whm = new HashMap<WeakReference<JSValue>,ValRef>();
	private ValRef thisValRef = null;
	protected synchronized void protect(JSContext ctx, Long valueRef) {
		protect(ctx.ctxRef(),valueRef);
		thisValRef = new ValRef(context,valueRef);
		whm.put(new WeakReference<JSValue>(this,rq), thisValRef);
		unprotectDeadReferences();
	}
	private synchronized void unprotectDeadReferences() {
		Reference<? extends JSValue> p;
		HashMap<JSContext,Boolean> collect = new HashMap<JSContext,Boolean>();
		while ((p = rq.poll()) != null) {
			ValRef vr = whm.get(p);
			unprotect(vr.ctx.ctxRef(),vr.valueRef);
			whm.remove(p);
			collect.put(vr.ctx, true);
		}
		for (JSContext ctx : collect.keySet()) {
			ctx.garbageCollect();
		}
	}
	
	/**
	 * Wraps an existing JavaScript value
	 * @param valueRef  The JavaScriptCore reference to the value
	 * @param ctx  The context in which the value exists
	 */
	public JSValue(long valueRef, JSContext ctx) {
		context = ctx;
		this.valueRef = valueRef;
		protect(ctx,valueRef);
	}
	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		unprotectDeadReferences();
	}
	
	/* Testers */
	/**
	 * Tests whether the value is undefined
	 * @return  true if undefined, false otherwise
	 */
	public Boolean isUndefined() {
		return isUndefined(context.ctxRef(), valueRef);
	}
	/**
	 * Tests whether the value is null
	 * @return  true if null, false otherwise
	 */
	public Boolean isNull() {
		return isNull(context.ctxRef(), valueRef);
	}
	/**
	 * Tests whether the value is boolean
	 * @return  true if boolean, false otherwise
	 */
	public Boolean isBoolean() {
		return isBoolean(context.ctxRef(), valueRef);		
	}
	/**
	 * Tests whether the value is a number
	 * @return  true if a number, false otherwise
	 */
	public Boolean isNumber() {
		return isNumber(context.ctxRef(), valueRef);
	}
	/**
	 * Tests whether the value is a string
	 * @return  true if a string, false otherwise
	 */
	public Boolean isString() {
		return isString(context.ctxRef(), valueRef);
	}
	/**
	 * Tests whether the value is an object
	 * @return  true if an object, false otherwise
	 */
	public Boolean isObject() {
		return isObject(context.ctxRef(), valueRef);
	}
	/**
	 * Tests whether a value in an instance of a constructor object
	 * @param constructor  The constructor object to test
	 * @return  true if the value is an instance of the given constructor object, false otherwise
	 * @throws JSException
	 */
	public Boolean isInstanceOfConstructor(JSObject constructor) throws JSException {
		JNIReturnObject jni = this.isInstanceOfConstructor(context.ctxRef(), valueRef, constructor.valueRef());
		if (jni.exception!=0) {
			throw (new JSException(new JSValue(jni.exception, context)));
		}
		return jni.bool;
	}
	
	/* Comparators */
	@Override
	public boolean equals(Object other) {
		if (other == this) return true;
		JSValue otherJSValue;
		if (other == null) {
			otherJSValue = new JSValue(context,(Long)null);
		} else if (other instanceof JSValue) {
			otherJSValue = (JSValue)other;
		} else if (other instanceof Long) {
			otherJSValue = new JSValue(context,(Long)other);
		} else if (other instanceof Integer) {
			otherJSValue = new JSValue(context,(Integer)other);
		} else if (other instanceof Double) {
			otherJSValue = new JSValue(context,(Double)other);
		} else if (other instanceof String) {
			otherJSValue = new JSValue(context,(String)other);
		} else if (other instanceof Boolean) {
			otherJSValue = new JSValue(context,(Boolean)other);
		} else {
			return false;
		}
		JNIReturnObject jni = isEqual(context.ctxRef(), valueRef, otherJSValue.valueRef);
		if (jni.exception!=0) {
			return false;
		}
		return jni.bool;
	}
	
	/**
	 * Tests whether two values are strict equal.  In JavaScript, equivalent to '==' operator.  
	 * @param otherJSValue  The value to test against
	 * @return  true if values are strict equal, false otherwise
	 */
	public boolean isStrictEqual(JSValue otherJSValue) {
		if (otherJSValue == null) return false;
		if (otherJSValue == this) return true;
		return isStrictEqual(context.ctxRef(), valueRef, otherJSValue.valueRef);
	}
	
	/* Getters */
	/**
	 * Gets the Boolean value of this JS value
	 * @return  the Boolean value
	 */
	public Boolean toBoolean() {
		return toBoolean(context.ctxRef(), valueRef);
	}
	/**
	 * Gets the numeric value of this JS value
	 * @return  The numeric value
	 * @throws JSException
	 */
	public Double toNumber() throws JSException {
		JNIReturnObject jni = toNumber(context.ctxRef(), valueRef);
		if (jni.exception!=0) {
			throw (new JSException(new JSValue(jni.exception, context)));
		}
		return jni.number;
	}
	@Override
	public String toString() {
		try {
			return toJSString().toString();
		} catch (JSException e) {
			return e.toString();
		}
	}
	/**
	 * Gets the JSString value of this JS value
	 * @return  The JSString value
	 * @throws JSException
	 */
	public JSString toJSString() throws JSException {
		JNIReturnObject jni = toStringCopy(context.ctxRef(), valueRef);
		if (jni.exception!=0) {
			throw (new JSException(new JSValue(jni.exception, context)));
		}
		return new JSString(jni.reference);
	}
	/**
	 * If the JS value is an object, gets the JSObject
	 * @return  The JSObject for this value
	 * @throws JSException  if not an object
	 */
	public JSObject toObject() throws JSException {
		JNIReturnObject jni = toObject(context.ctxRef(), valueRef);
		if (jni.exception!=0) {
			throw (new JSException(new JSValue(jni.exception, context)));
		}
		return context.getObjectFromRef(jni.reference);
	}
	/**
	 * Gets the JSON of this JS value
	 * @param indent  number of spaces to indent
	 * @return  the JSON representing this value
	 * @throws JSException
	 */
	public String toJSON(int indent) throws JSException {
		JNIReturnObject jni = createJSONString(context.ctxRef(), valueRef, indent);
		if (jni.exception!=0) {
			throw (new JSException(new JSValue(jni.exception, context)));
		}
		if (jni.reference==0) {
			return null;
		}
		return new JSString(jni.reference).toString();
	}
	/**
	 * Gets the JSON of this JS value
	 * @return  the JSON representing this value
	 * @throws JSException
	 */
	public String toJSON() throws JSException {
		return toJSON(0);
	}
	/**
	 * Gets the JSContext of this value
	 * @return the JSContext of this value
	 */
	public JSContext getContext() {
		return context;
	}
	/**
	 * Gets the JavaScriptCore value reference
	 * @return  the JavaScriptCore value reference
	 */
	public Long valueRef() {
		return valueRef;
	}

	/* Native functions */
	protected native int getType(long ctxRef, long valueRef); /**/
	protected native boolean isUndefined(long ctxRef, long valueRef);
	protected native boolean isNull(long ctxRef, long valueRef );
	protected native boolean isBoolean(long ctxRef, long valueRef );
	protected native boolean isNumber(long ctxRef, long valueRef );
	protected native boolean isString(long ctxRef, long valueRef );
	protected native boolean isObject(long ctxRef, long valueRef );
	protected native JNIReturnObject isEqual(long ctxRef, long a, long b );
	protected native boolean isStrictEqual(long ctxRef, long a, long b );
	protected native JNIReturnObject isInstanceOfConstructor(long ctxRef, long valueRef, long constructor);
	protected native long makeUndefined(long ctx);
	protected native long makeNull(long ctx);
	protected native long makeBoolean(long ctx, boolean bool);
	protected native long makeNumber(long ctx, double number);
	protected native long makeString(long ctx, long stringRef);
	protected native long makeFromJSONString(long ctx, long stringRef);
	protected native JNIReturnObject createJSONString(long ctxRef, long valueRef, int indent);
	protected native boolean toBoolean(long ctx, long valueRef);
	protected native JNIReturnObject toNumber(long ctxRef, long valueRef);
	protected native JNIReturnObject toStringCopy(long ctxRef, long valueRef);
	protected native JNIReturnObject toObject(long ctxRef, long valueRef);
	protected native void protect(long ctx, long valueRef); /**/
	private native void unprotect(long ctx, long valueRef); /**/
	protected native void setException(long valueRef, long exceptionRefRef);
}
