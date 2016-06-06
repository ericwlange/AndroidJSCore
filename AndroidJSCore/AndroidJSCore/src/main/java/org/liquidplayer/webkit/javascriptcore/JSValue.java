//
// JSValue.java
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
package org.liquidplayer.webkit.javascriptcore;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.HashMap;

/**
 * A JavaScript value
 * @since 1.0
 */
public class JSValue {

    private class JNIReturnClass implements Runnable {
        @Override
        public void run() {}
        JNIReturnObject jni;
    }

    protected Long valueRef = 0L;
	protected JSContext context = null;
	protected Boolean isDefunct = false;
	
	/* Constructors */
	/**
	 * Creates an empty JSValue.  This can only be used by subclasses, and those
	 * subclasses must define 'context' and 'valueRef' themselves
	 * @since 1.0
	 */
	protected JSValue() {
	}
	/**
	 * Creates a new undefined JavaScript value
	 * @param ctx  The context in which to create the value
	 * @since 1.0
	 */
	public JSValue(final JSContext ctx) {
		context = ctx;
        context.sync(new Runnable() {
            @Override
            public void run() {
                valueRef = makeUndefined(context.ctxRef());
            }
        });
	}
	/**
	 * Creates a new JavaScript value from a Java value.  Classes supported are:
	 * Boolean, Double, Integer, Long, String, and JSString.  Any other object will
	 * generate an undefined JavaScript value.
	 * @param ctx  The context in which to create the value
	 * @param val  The Java value
	 * @since 1.0
	 */
	public JSValue(JSContext ctx, final Object val) {
		context = ctx;
        context.sync(new Runnable() {
            @Override
            public void run() {
                if (val == null) {
                    valueRef = makeNull(context.ctxRef());
                } else if (val instanceof Boolean) {
                    valueRef = makeBoolean(context.ctxRef(), (Boolean)val);
                } else if (val instanceof Double) {
                    valueRef = makeNumber(context.ctxRef(), (Double)val);
                } else if (val instanceof Float) {
                    valueRef = makeNumber(context.ctxRef(), Double.valueOf(((Float)val).toString()));
                } else if (val instanceof Integer ) {
                    valueRef = makeNumber(context.ctxRef(), ((Integer)val).doubleValue());
                } else if (val instanceof Long) {
                    valueRef = makeNumber(context.ctxRef(), ((Long)val).doubleValue());
                } else if (val instanceof String) {
                    JSString s = new JSString((String)val);
                    valueRef = makeString(context.ctxRef(), s.stringRef);
                } else if (val instanceof JSString) {
                    valueRef = makeString(context.ctxRef(), ((JSString) val).stringRef);
                } else {
                    valueRef = makeUndefined(context.ctxRef());
                }
            }
        });
	}

    /**
	 * Wraps an existing JavaScript value
	 * @param valueRef  The JavaScriptCore reference to the value
	 * @param ctx  The context in which the value exists
	 * @since 1.0
	 */
	public JSValue(final long valueRef, JSContext ctx) {
		context = ctx;
        context.sync(new Runnable() {
            @Override
            public void run() {
                if (valueRef == 0) {
					JSValue.this.valueRef = makeUndefined(context.ctxRef());
				}
                else {
					JSValue.this.valueRef = valueRef;
				}
            }
        });
	}
	@Override
	protected void finalize() throws Throwable {
		super.finalize();
        unprotect();
	}
	
	/* Testers */
	/**
	 * Tests whether the value is undefined
	 * @return  true if undefined, false otherwise
	 * @since 1.0
	 */
	public Boolean isUndefined() {
        JNIReturnClass runnable = new JNIReturnClass() {
            @Override
            public void run() {
                jni = new JNIReturnObject();
                jni.bool = isUndefined(context.ctxRef(), valueRef);
            }
        };
        context.sync(runnable);
		return runnable.jni.bool;
	}
	/**
	 * Tests whether the value is null
	 * @return  true if null, false otherwise
	 * @since 1.0
	 */
	public Boolean isNull() {
        JNIReturnClass runnable = new JNIReturnClass() {
            @Override
            public void run() {
                jni = new JNIReturnObject();
                jni.bool = isNull(context.ctxRef(), valueRef);
            }
        };
        context.sync(runnable);
        return runnable.jni.bool;
	}
	/**
	 * Tests whether the value is boolean
	 * @return  true if boolean, false otherwise
	 * @since 1.0
	 */
	public Boolean isBoolean() {
        JNIReturnClass runnable = new JNIReturnClass() {
            @Override
            public void run() {
                jni = new JNIReturnObject();
                jni.bool = isBoolean(context.ctxRef(), valueRef);
            }
        };
        context.sync(runnable);
        return runnable.jni.bool;
	}
	/**
	 * Tests whether the value is a number
	 * @return  true if a number, false otherwise
	 * @since 1.0
	 */
	public Boolean isNumber() {
        JNIReturnClass runnable = new JNIReturnClass() {
            @Override
            public void run() {
                jni = new JNIReturnObject();
                jni.bool = isNumber(context.ctxRef(), valueRef);
            }
        };
        context.sync(runnable);
        return runnable.jni.bool;
	}
	/**
	 * Tests whether the value is a string
	 * @return  true if a string, false otherwise
	 * @since 1.0
	 */
	public Boolean isString() {
        JNIReturnClass runnable = new JNIReturnClass() {
            @Override
            public void run() {
                jni = new JNIReturnObject();
                jni.bool = isString(context.ctxRef(), valueRef);
            }
        };
        context.sync(runnable);
        return runnable.jni.bool;
	}
	/**
	 * Tests whether the value is an array
	 * @return  true if an array, false otherwise
	 * @since 2.2
	 */
	public Boolean isArray() {
        JNIReturnClass runnable = new JNIReturnClass() {
            @Override
            public void run() {
                jni = new JNIReturnObject();
                jni.bool = isArray(context.ctxRef(), valueRef);
            }
        };
        context.sync(runnable);
        return runnable.jni.bool;
	}
    /**
     * Tests whether the value is a date object
     * @return  true if a date object, false otherwise
     * @since 2.2
     */
    public Boolean isDate() {
        JNIReturnClass runnable = new JNIReturnClass() {
            @Override
            public void run() {
                jni = new JNIReturnObject();
                jni.bool = isDate(context.ctxRef(), valueRef);
            }
        };
        context.sync(runnable);
        return runnable.jni.bool;
    }
    /**
     * Tests whether the value is an object
     * @return  true if an object, false otherwise
     * @since 1.0
     */
    public Boolean isObject() {
        JNIReturnClass runnable = new JNIReturnClass() {
            @Override
            public void run() {
                jni = new JNIReturnObject();
                jni.bool = isObject(context.ctxRef(), valueRef);
            }
        };
        context.sync(runnable);
        return runnable.jni.bool;
    }
	/**
	 * Tests whether a value in an instance of a constructor object
	 * @param constructor  The constructor object to test
	 * @return  true if the value is an instance of the given constructor object, false otherwise
	 * @since 1.0
	 * @throws JSException
	 */
	public Boolean isInstanceOfConstructor(final JSObject constructor) throws JSException {
        JNIReturnClass runnable = new JNIReturnClass() {
            @Override
            public void run() {
                jni = isInstanceOfConstructor(context.ctxRef(), valueRef, constructor.valueRef());
            }
        };
        context.sync(runnable);
        if (runnable.jni.exception!=0) {
            context.throwJSException(new JSException(new JSValue(runnable.jni.exception, context)));
            runnable.jni.bool = false;
        }
        return runnable.jni.bool;
	}
	
	/* Comparators */
	@Override
	public boolean equals(Object other) {
		if (other == this) return true;
        JSValue otherJSValue;
		if (other instanceof JSValue) {
            otherJSValue = (JSValue)other;
        } else {
            otherJSValue = new JSValue(context, other);
        }
        final JSValue ojsv = otherJSValue;
        JNIReturnClass runnable = new JNIReturnClass() {
            @Override
            public void run() {
                jni = isEqual(context.ctxRef(), valueRef, ojsv.valueRef);
            }
        };
        context.sync(runnable);
		if (runnable.jni.exception!=0) {
			return false;
		}
		return runnable.jni.bool;
	}
	
	/**
	 * Tests whether two values are strict equal.  In JavaScript, equivalent to '===' operator.
	 * @param other  The value to test against
	 * @return  true if values are strict equal, false otherwise
	 * @since 1.0
	 */
	public boolean isStrictEqual(Object other) {
        if (other == this) return true;
        JSValue otherJSValue;
        if (other instanceof JSValue) {
            otherJSValue = (JSValue)other;
        } else {
            otherJSValue = new JSValue(context, other);
        }
        final JSValue ojsv = otherJSValue;
        JNIReturnClass runnable = new JNIReturnClass() {
            @Override
            public void run() {
                jni = new JNIReturnObject();
                jni.bool = isStrictEqual(context.ctxRef(), valueRef, ojsv.valueRef);
            }
        };
        context.sync(runnable);
        return runnable.jni.bool;
	}
	
	/* Getters */
	/**
	 * Gets the Boolean value of this JS value
	 * @return  the Boolean value
	 * @since 1.0
	 */
	public Boolean toBoolean() {
        JNIReturnClass runnable = new JNIReturnClass() {
            @Override
            public void run() {
                jni = new JNIReturnObject();
                jni.bool = toBoolean(context.ctxRef(), valueRef);
            }
        };
        context.sync(runnable);
		return runnable.jni.bool;
	}
	/**
	 * Gets the numeric value of this JS value
	 * @return  The numeric value
	 * @since 1.0
	 * @throws JSException
	 */
	public Double toNumber() throws JSException {
        JNIReturnClass runnable = new JNIReturnClass() {
            @Override
            public void run() {
                jni = toNumber(context.ctxRef(), valueRef);
            }
        };
        context.sync(runnable);
		if (runnable.jni.exception!=0) {
			context.throwJSException(new JSException(new JSValue(runnable.jni.exception, context)));
            return 0.0;
		}
		return runnable.jni.number;
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
	 * @since 1.0
	 * @throws JSException
	 */
	public JSString toJSString() throws JSException {
        JNIReturnClass runnable = new JNIReturnClass() {
            @Override
            public void run() {
                jni = toStringCopy(context.ctxRef(), valueRef);
            }
        };
        context.sync(runnable);
		if (runnable.jni.exception!=0) {
			context.throwJSException(new JSException(new JSValue(runnable.jni.exception, context)));
            return null;
		}
		return new JSString(runnable.jni.reference);
	}
	/**
	 * If the JS value is an object, gets the JSObject
	 * @return  The JSObject for this value
	 * @since 1.0
	 * @throws JSException  if not an object
	 */
	public JSObject toObject() throws JSException {
        JNIReturnClass runnable = new JNIReturnClass() {
            @Override
            public void run() {
                jni = toObject(context.ctxRef(), valueRef);
            }
        };
        context.sync(runnable);
		if (runnable.jni.exception!=0) {
			context.throwJSException(new JSException(new JSValue(runnable.jni.exception, context)));
            return new JSObject(context);
		}
		return context.getObjectFromRef(runnable.jni.reference);
	}
	public JSFunction toFunction() throws JSException {
		if (isObject() && toObject() instanceof JSFunction) {
			return (JSFunction)toObject();
		} else if (!isObject()) {
			toObject();
			return null;
		} else {
            context.throwJSException(new JSException(context, "JSObject not a function"));
            return null;
        }
	}
	/**
	 * Gets the JSON of this JS value
	 * @param indent  number of spaces to indent
	 * @return  the JSON representing this value, or null if value is undefined
	 * @since 1.0
	 * @throws JSException
	 */
	public String toJSON(final int indent) throws JSException {
        JNIReturnClass runnable = new JNIReturnClass() {
            @Override
            public void run() {
                jni = createJSONString(context.ctxRef(), valueRef, indent);
            }
        };
        context.sync(runnable);
		if (runnable.jni.exception!=0) {
			context.throwJSException(new JSException(new JSValue(runnable.jni.exception, context)));
            return null;
		}
		if (runnable.jni.reference==0) {
			return null;
		}
		return new JSString(runnable.jni.reference).toString();
	}
	/**
	 * Gets the JSON of this JS value
	 * @return  the JSON representing this value
	 * @since 1.0
	 * @throws JSException
	 */
	public String toJSON() throws JSException {
		return toJSON(0);
	}
	/**
	 * Gets the JSContext of this value
	 * @return the JSContext of this value
	 * @since 1.0
	 */
	public JSContext getContext() {
		return context;
	}
	/**
	 * Gets the JavaScriptCore value reference
	 * @return  the JavaScriptCore value reference
	 * @since 1.0
	 */
	public Long valueRef() {
		return valueRef;
	}

    protected void unprotect() {
        if (isProtected && !context.isDefunct)
            unprotect(context.ctxRef(),valueRef);
        isProtected = false;
    }
    private boolean isProtected = true;

    /* Native functions */
	protected native int getType(long ctxRef, long valueRef); /**/
	protected native boolean isUndefined(long ctxRef, long valueRef);
	protected native boolean isNull(long ctxRef, long valueRef );
	protected native boolean isBoolean(long ctxRef, long valueRef );
	protected native boolean isNumber(long ctxRef, long valueRef );
	protected native boolean isString(long ctxRef, long valueRef );
	protected native boolean isObject(long ctxRef, long valueRef );
    protected native boolean isArray(long ctxRef, long valueRef );
    protected native boolean isDate(long ctxRef, long valueRef );
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
	private native void protect(long ctx, long valueRef); /**/
	protected native void unprotect(long ctx, long valueRef); /**/
	protected native void setException(long valueRef, long exceptionRefRef);
}
