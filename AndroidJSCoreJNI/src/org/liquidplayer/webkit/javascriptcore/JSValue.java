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

public class JSValue {
	
	protected Long valueRef = 0L;
	protected JSContext context = null;
	
	/* Constructors */
	protected JSValue() {
	}
	public JSValue(JSContext ctx) {
		context = ctx;
		valueRef = makeUndefined(context.ctxRef());
	}
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
			valueRef = makeNumber(context.ctxRef(), (Double)val);
		} else if (val instanceof String) {
			JSString s = new JSString((String)val);
			valueRef = makeString(context.ctxRef(), s.stringRef);			
		} else if (val instanceof JSString) {
			valueRef = makeString(context.ctxRef(), ((JSString)val).stringRef);
		} else {
			valueRef = makeUndefined(context.ctxRef());
		}
	}
	public JSValue(long valueRef, JSContext ctx) {
		context = ctx;
		this.valueRef = valueRef;
	}
	
	/* Testers */
	public Boolean isUndefined() {
		return isUndefined(context.ctxRef(), valueRef);
	}
	public Boolean isNull() {
		return isNull(context.ctxRef(), valueRef);
	}
	public Boolean isBoolean() {
		return isBoolean(context.ctxRef(), valueRef);		
	}
	public Boolean isNumber() {
		return isNumber(context.ctxRef(), valueRef);
	}
	public Boolean isString() {
		return isString(context.ctxRef(), valueRef);
	}
	public Boolean isObject() {
		return isObject(context.ctxRef(), valueRef);
	}
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
	
	public boolean isStrictEqual(JSValue otherJSValue) {
		if (otherJSValue == null) return false;
		if (otherJSValue == this) return true;
		return isStrictEqual(context.ctxRef(), valueRef, otherJSValue.valueRef);
	}
	
	/* Getters */
	public Boolean toBoolean() {
		return toBoolean(context.ctxRef(), valueRef);
	}
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
	public JSString toJSString() throws JSException {
		JNIReturnObject jni = toStringCopy(context.ctxRef(), valueRef);
		if (jni.exception!=0) {
			throw (new JSException(new JSValue(jni.exception, context)));
		}
		return new JSString(jni.reference);
	}
	public JSObject toObject() throws JSException {
		JNIReturnObject jni = toObject(context.ctxRef(), valueRef);
		if (jni.exception!=0) {
			throw (new JSException(new JSValue(jni.exception, context)));
		}
		return context.getObjectFromRef(jni.reference);
	}
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
	public String toJSON() throws JSException {
		return toJSON(0);
	}
	public JSContext getContext() {
		return context;
	}
	
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
	protected native void unprotect(long ctx, long valueRef); /**/
	protected native void setException(long valueRef, long exceptionRefRef);
}
