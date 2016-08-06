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

import android.os.AsyncTask;
import android.os.Looper;
import android.util.Log;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * A JavaScript value
 * @since 1.0
 */
public class JSValue {

    /**
     * Used in communicating with JavaScriptCore JNI.
     * Clients do not need to use this.
     */
    protected static class JNIReturnObject {
        /**
         * The boolean return value
         */
        public boolean bool;
        /**
         * The numeric return value
         */
        public double number;
        /**
         * The reference return value
         */
        public long reference;
        /**
         * The exception reference if one was thrown, otherwise 0L
         */
        public long exception;
    }

    protected static class JSWorkerQueue {
        public JSWorkerQueue(final Runnable monitor) {
            mMonitor = monitor;
        }
        final Runnable mMonitor;

        private class JSTask extends AsyncTask<Runnable, Void, JSException> {
            @Override
            public JSException doInBackground(Runnable ... params) {
                try {
                    params[0].run();
                    mMonitor.run();
                } catch (JSException e) {
                    return e;
                }
                return null;
            }
        }

        public void sync(final Runnable runnable) {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                try {
                    JSException e = new JSTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, runnable).get();
                    if (e != null) throw e;
                } catch (ExecutionException e) {
                    Log.e("JSWorkerQueue", e.getMessage());
                } catch (InterruptedException e) {
                    Thread.interrupted();
                }
            } else {
                runnable.run();
                mMonitor.run();
            }
        }

        public void async(final Runnable runnable) {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                new JSTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, runnable);
            } else {
                runnable.run();
                mMonitor.run();
            }
        }

        public void quit() {

        }
    }

    protected static class JSString {

        private static final JSWorkerQueue workerQueue = new JSWorkerQueue(new Runnable() {
            @Override
            public void run() {
            }
        });

        private abstract class JNIStringReturnClass implements Runnable {
            String string;
        }

        protected Long stringRef;

        /**
         * Creates a JavaScript string from a Java string
         * @param s  The Java string with which to initialize the JavaScript string
         * @since 1.0
         */
        public JSString(final String s) {
            if (s==null) stringRef = 0L;
            else {
                workerQueue.sync(new Runnable() {
                    @Override
                    public void run() {
                        stringRef = createWithCharacters(s);
                    }
                });
            }
        }
        /**
         * Wraps an existing JavaScript string
         * @param stringRef  The JavaScriptCore reference to the string
         */
        public JSString(Long stringRef) {
            this.stringRef = stringRef;
        }
        @Override
        protected void finalize() throws Throwable {
            super.finalize();
            if (stringRef != 0)
                release(stringRef);
        }

        @Override
        public String toString() {
            JNIStringReturnClass payload = new JNIStringReturnClass() {
                @Override
                public void run() {
                    string = JSString.this.toString(stringRef);
                }
            };
            workerQueue.sync(payload);
            return payload.string;
        }

        /**
         * Gets the JavaScriptCore string reference
         * @return  the JavaScriptCore string reference
         * @since 1.0
         */
        public Long stringRef() {
            return stringRef;
        }

        protected native long createWithCharacters(String str);
        protected native long retain(long strRef);
        protected native void release(long stringRef);
        protected native boolean isEqual(long a, long b);
        protected native String toString(long strRef);

        @SuppressWarnings("unused")
        protected native int getLength(long stringRef);
        @SuppressWarnings("unused")
        protected native long createWithUTF8CString(String str);
        @SuppressWarnings("unused")
        protected native int getMaximumUTF8CStringSize(long stringRef);
        @SuppressWarnings("unused")
        protected native boolean isEqualToUTF8CString(long a, String b);
    }

    private abstract class JNIReturnClass implements Runnable {
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
    @SuppressWarnings("unchecked")
    public JSValue(JSContext ctx, final Object val) {
        context = ctx;
        context.sync(new Runnable() {
            @Override
            public void run() {
                if (val == null) {
                    valueRef = makeNull(context.ctxRef());
                } else if (val instanceof JSValue) {
                    valueRef = ((JSValue) val).valueRef();
                    protect(context.ctxRef(), valueRef);
                } else if (val instanceof Map) {
                    valueRef = new JSObjectPropertiesMap(context, (Map)val, Object.class).getJSObject().valueRef();
                    protect(context.ctxRef(), valueRef);
                } else if (val instanceof List) {
                    valueRef = new JSArray<>(context, (List) val, JSValue.class).valueRef();
                    protect(context.ctxRef(), valueRef);
                } else if (val.getClass().isArray()) {
                    valueRef = new JSArray<>(context, (Object[])val, JSValue.class).valueRef();
                    protect(context.ctxRef(), valueRef);
                } else if (val instanceof Boolean) {
                    valueRef = makeBoolean(context.ctxRef(), (Boolean)val);
                } else if (val instanceof Double) {
                    valueRef = makeNumber(context.ctxRef(), (Double)val);
                } else if (val instanceof Float) {
                    valueRef = makeNumber(context.ctxRef(), Double.valueOf(val.toString()));
                } else if (val instanceof Integer ) {
                    valueRef = makeNumber(context.ctxRef(), ((Integer)val).doubleValue());
                } else if (val instanceof Long) {
                    valueRef = makeNumber(context.ctxRef(), ((Long)val).doubleValue());
                } else if (val instanceof Byte) {
                    valueRef = makeNumber(context.ctxRef(), ((Byte)val).doubleValue());
                } else if (val instanceof Short) {
                    valueRef = makeNumber(context.ctxRef(), ((Short)val).doubleValue());
                } else if (val instanceof String) {
                    JSString s = new JSString((String)val);
                    valueRef = makeString(context.ctxRef(), s.stringRef);
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
    protected JSValue(final long valueRef, JSContext ctx) {
        context = ctx;
        context.sync(new Runnable() {
            @Override
            public void run() {
                if (valueRef == 0) {
                    JSValue.this.valueRef = makeUndefined(context.ctxRef());
                }
                else {
                    JSValue.this.valueRef = valueRef;
                    protect(context.ctxRef(), valueRef);
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
     */
    public Boolean isInstanceOfConstructor(final JSObject constructor) {
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
    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    public boolean equals(Object other) {
        return isEqual(other);
    }

    /**
     * JavaScript definition of equality (==).  JSValue.equals() and JSValue.isEqual() represent
     * the Java and JavaScript definitions, respectively.  Normally they will return the same
     * value, however some classes may override and offer different results.  Example,
     * in JavaScript, new Float32Array([1,2,3]) == new Float32Array([1,2,3]) will be false (as
     * the equality is only true if they are the same physical object), but from a Java util.java.List
     * perspective, these two are equal.
     * @param other the value to compare for equality
     * @return true if == from JavaScript perspective, false otherwise
     * @since 3.0
     */
    public boolean isEqual(Object other) {
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
        return runnable.jni.exception==0 && runnable.jni.bool;
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
     */
    public Double toNumber() {
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
     */
    protected JSString toJSString() {
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
     */
    public JSObject toObject() {
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

    /**
     * If the JS value is a function, gets the JSFunction
     * @return  The JSFunction for this value
     * @since 3.0
     */
    public JSFunction toFunction() {
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
     * If the JS value is an array, gets the JSArray
     * @return  The JSArray for this value
     * @since 3.0
     */
    public JSBaseArray toJSArray() {
        if (isObject() && toObject() instanceof JSBaseArray) {
            return (JSBaseArray)toObject();
        } else if (!isObject()) {
            toObject();
            return null;
        } else {
            context.throwJSException(new JSException(context, "JSObject not an array"));
            return null;
        }
    }

    /**
     * Gets the JSON of this JS value
     * @param indent  number of spaces to indent
     * @return  the JSON representing this value, or null if value is undefined
     * @since 1.0
     */
    public String toJSON(final int indent) {
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
     */
    public String toJSON() {
        return toJSON(0);
    }

    @SuppressWarnings("unchecked")
    protected Object toJavaObject(Class clazz) {
        if (clazz == Object.class)
            return this;
        else if (clazz == Map.class)
            return new JSObjectPropertiesMap(toObject(),Object.class);
        else if (clazz == List.class)
            return toJSArray();
        else if (clazz == String.class)
            return toString();
        else if (clazz == Double.class || clazz == double.class)
            return toNumber();
        else if (clazz == Float.class || clazz == float.class)
            return toNumber().floatValue();
        else if (clazz == Integer.class || clazz == int.class)
            return toNumber().intValue();
        else if (clazz == Long.class || clazz == long.class)
            return toNumber().longValue();
        else if (clazz == Byte.class || clazz == byte.class)
            return toNumber().byteValue();
        else if (clazz == Short.class || clazz == short.class)
            return toNumber().shortValue();
        else if (clazz == Boolean.class || clazz == boolean.class)
            return toBoolean();
        else if (clazz.isArray())
            return toJSArray().toArray(clazz.getComponentType());
        else if (JSObject.class.isAssignableFrom(clazz))
            return clazz.cast(toObject());
        else if (JSValue.class.isAssignableFrom(clazz))
            return clazz.cast(this);
        return null;
    }

    @Override
    public int hashCode() {
        if (isBoolean()) return toBoolean().hashCode();
        else if (isNumber()) return toNumber().hashCode();
        else if (isString()) return toString().hashCode();
        else if (isUndefined() || isNull()) return 0;
        else return super.hashCode();
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
            context.markForUnprotection(valueRef());
        isProtected = false;
    }
    private boolean isProtected = true;

    /* Native functions */
    @SuppressWarnings("unused")
    protected native int getType(long ctxRef, long valueRef);
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
    protected native void protect(long ctx, long valueRef);
    protected native void unprotect(long ctx, long valueRef);
    protected native void setException(long valueRef, long exceptionRefRef);
}
