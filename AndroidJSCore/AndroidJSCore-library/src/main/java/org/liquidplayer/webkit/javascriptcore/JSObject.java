//
// JSObject.java
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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A JavaScript object.
 * @since 1.0
 *
 */
public class JSObject extends JSValue {

    private abstract class JNIReturnClass implements Runnable {
        JNIReturnObject jni;
    }

    /**
     * Specifies that a property has no special attributes.
     */
    public static int JSPropertyAttributeNone = 0;
    /**
     * Specifies that a property is read-only.
     */
    public static int JSPropertyAttributeReadOnly = 1 << 1;
    /**
     * Specifies that a property should not be enumerated by
     * JSPropertyEnumerators and JavaScript for...in loops.
     */
    public static int JSPropertyAttributeDontEnum = 1 << 2;
    /**
     * Specifies that the delete operation should fail on a property.
     */
    public static int JSPropertyAttributeDontDelete = 1 << 3;

    /**
     * Creates a new, empty JavaScript object.  In JS:
     * <pre>
     * {@code
     * var obj = {}; // OR
     * var obj = new Object();
     * }
     * </pre>
     *
     * @param ctx The JSContext to create the object in
     * @since 1.0
     */
    public JSObject(JSContext ctx) {
        context = ctx;
        context.sync(new Runnable() {
            @Override
            public void run() {
                valueRef = make(context.ctxRef(), 0L);
            }
        });
        context.persistObject(this);
    }

    /**
     * Called only by convenience subclasses.  If you use
     * this, you must set context and valueRef yourself.
     * @since 3.0
     */
     public JSObject() {
     }

    /**
     * Wraps an existing object from JavaScript
     *
     * @param objRef The JavaScriptCore object reference
     * @param ctx    The JSContext of the reference
     * @since 1.0
     */
    protected JSObject(final long objRef, JSContext ctx) {
        super(objRef, ctx);
        context.persistObject(this);
    }

    /**
     * Creates a new object with function properties set for each method
     * in the defined interface.
     * In JS:
     * <pre>
     * {@code
     * var obj = {
     *     func1: function(a)   { alert(a); },
     *     func2: function(b,c) { alert(b+c); }
     * };
     * }
     * </pre>
     * Where func1, func2, etc. are defined in interface 'iface'.  This JSObject
     * must implement 'iface'.
     *
     * @param ctx   The JSContext to create the object in
     * @param iface The Java Interface defining the methods to expose to JavaScript
     * @since 1.0
     */
    public JSObject(JSContext ctx, final Class<?> iface) {
        context = ctx;
        context.sync(new Runnable() {
            @Override
            public void run() {
                valueRef = make(context.ctxRef(), 0L);
                Method[] methods = iface.getDeclaredMethods();
                for (Method m : methods) {
                    JSObject f = new JSFunction(context, m,
                            JSObject.class, JSObject.this);
                    property(m.getName(), f);
                }
            }
        });
        context.persistObject(this);
    }

    /**
     * Creates a new function object with the entries in 'map' set as properties.
     *
     * @param ctx  The JSContext to create object in
     * @param map  The map containing the properties
     */
    @SuppressWarnings("unchecked")
    public JSObject(JSContext ctx, final Map map) {
        this(ctx);
        new JSObjectPropertiesMap<>(this,Object.class).putAll(map);
    }

    /**
     * Determines if the object contains a given property
     *
     * @param prop The property to test the existence of
     * @return true if the property exists on the object, false otherwise
     * @since 1.0
     */
    public boolean hasProperty(final String prop) {
        JNIReturnClass runnable = new JNIReturnClass() {
            @Override
            public void run() {
                jni = new JNIReturnObject();
                jni.bool = hasProperty(context.ctxRef(), valueRef, new JSString(prop).stringRef());
            }
        };
        context.sync(runnable);
        return runnable.jni.bool;
    }

    /**
     * Gets the property named 'prop'
     *
     * @param prop The name of the property to fetch
     * @return The JSValue of the property, or null if it does not exist
     * @since 1.0
     */
    public JSValue property(final String prop) {
        JNIReturnClass runnable = new JNIReturnClass() {
            @Override
            public void run() {
                jni = getProperty(context.ctxRef(), valueRef, new JSString(prop).stringRef());
            }
        };
        context.sync(runnable);
        if (runnable.jni.exception != 0) {
            context.throwJSException(new JSException(new JSValue(runnable.jni.exception, context)));
            return new JSValue(context);
        }
        return new JSValue(runnable.jni.reference, context);
    }

    /**
     * Sets the value of property 'prop'
     *
     * @param prop       The name of the property to set
     * @param value      The Java object to set.  The Java object will be converted to a JavaScript object
     *                   automatically.
     * @param attributes And OR'd list of JSProperty constants
     * @since 1.0
     */
    public void property(final String prop, final Object value, final int attributes) {
        JNIReturnClass runnable = new JNIReturnClass() {
            @Override
            public void run() {
                JSString name = new JSString(prop);
                jni = setProperty(
                        context.ctxRef(),
                        valueRef,
                        name.stringRef,
                        (value instanceof JSValue) ? ((JSValue) value).valueRef() : new JSValue(context, value).valueRef(),
                        attributes);
            }
        };
        context.sync(runnable);
        if (runnable.jni.exception != 0) {
            context.throwJSException(new JSException(new JSValue(runnable.jni.exception, context)));
        }
    }

    /**
     * Sets the value of property 'prop'.  No JSProperty attributes are set.
     *
     * @param prop  The name of the property to set
     * @param value The Java object to set.  The Java object will be converted to a JavaScript object
     *              automatically.
     * @since 1.0
     */
    public void property(String prop, Object value) {
        property(prop, value, JSPropertyAttributeNone);
    }

    /**
     * Deletes a property from the object
     *
     * @param prop The name of the property to delete
     * @return true if the property was deleted, false otherwise
     * @since 1.0
     */
    public boolean deleteProperty(final String prop) {
        JNIReturnClass runnable = new JNIReturnClass() {
            @Override
            public void run() {
                JSString name = new JSString(prop);
                jni = deleteProperty(context.ctxRef(), valueRef, name.stringRef());
            }
        };
        context.sync(runnable);
        if (runnable.jni.exception != 0) {
            context.throwJSException(new JSException(new JSValue(runnable.jni.exception, context)));
            return false;
        }
        return runnable.jni.bool;
    }

    /**
     * Returns the property at index 'index'.  Used for arrays.
     *
     * @param index The index of the property
     * @return The JSValue of the property at index 'index'
     * @since 1.0
     */
    public JSValue propertyAtIndex(final int index) {
        JNIReturnClass runnable = new JNIReturnClass() {
            @Override
            public void run() {
                jni = getPropertyAtIndex(context.ctxRef(), valueRef, index);
            }
        };
        context.sync(runnable);
        if (runnable.jni.exception != 0) {
            context.throwJSException(new JSException(new JSValue(runnable.jni.exception, context)));
            return new JSValue(context);
        }
        return new JSValue(runnable.jni.reference, context);
    }

    /**
     * Sets the property at index 'index'.  Used for arrays.
     *
     * @param index The index of the property to set
     * @param value The Java object to set, will be automatically converted to a JavaScript value
     * @since 1.0
     */
    public void propertyAtIndex(final int index, final Object value) {
        JNIReturnClass runnable = new JNIReturnClass() {
            @Override
            public void run() {
                jni = setPropertyAtIndex(context.ctxRef(), valueRef, index,
                        (value instanceof JSValue) ? ((JSValue) value).valueRef() : new JSValue(context, value).valueRef());
            }
        };
        context.sync(runnable);
        if (runnable.jni.exception != 0) {
            context.throwJSException(new JSException(new JSValue(runnable.jni.exception, context)));
        }
    }

    private abstract class StringArrayReturnClass implements Runnable {
        public String[] sArray;
    }

    /**
     * Gets the list of set property names on the object
     *
     * @return A string array containing the property names
     * @since 1.0
     */
    public String[] propertyNames() {
        StringArrayReturnClass runnable = new StringArrayReturnClass() {
            @Override
            public void run() {
                long propertyNameArray = copyPropertyNames(context.ctxRef(), valueRef);
                long[] refs = getPropertyNames(propertyNameArray);
                String[] names = new String[refs.length];
                for (int i = 0; i < refs.length; i++) {
                    JSString name = new JSString(refs[i]);
                    names[i] = name.toString();
                }
                releasePropertyNames(propertyNameArray);
                sArray = names;
            }
        };
        context.sync(runnable);
        return runnable.sArray;
    }

    /**
     * Determines if the object is a function
     *
     * @return true if the object is a function, false otherwise
     * @since 1.0
     */
    public boolean isFunction() {
        JNIReturnClass runnable = new JNIReturnClass() {
            @Override
            public void run() {
                jni = new JNIReturnObject();
                jni.bool = isFunction(context.ctxRef(), valueRef);
            }
        };
        context.sync(runnable);
        return runnable.jni.bool;
    }

    /**
     * Determines if the object is a constructor
     *
     * @return true if the object is a constructor, false otherwise
     * @since 1.0
     */
    public boolean isConstructor() {
        JNIReturnClass runnable = new JNIReturnClass() {
            @Override
            public void run() {
                jni = new JNIReturnObject();
                jni.bool = isConstructor(context.ctxRef(), valueRef);
            }
        };
        context.sync(runnable);
        return runnable.jni.bool;
    }

    @Override
    public int hashCode() {
        return valueRef().intValue();
    }

    protected final List<JSObject> zombies = new ArrayList<>();

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        context.finalizeObject(this);
    }

    protected void setThis(JSObject thiz) {
        this.thiz = thiz;
    }

    public JSObject getThis() {
        return thiz;
    }

    @SuppressWarnings("unused")
    public JSValue __nullFunc() {
        return new JSValue(context);
    }

    protected JSFunction isInstanceOf = null;
    private JSObject thiz = null;

    /* Native Methods */

    protected native long make(long ctx, long data);

    @SuppressWarnings("unused")
    protected native long makeInstance(long ctx);

    protected native JNIReturnObject makeArray(long ctx, long[] args);

    protected native JNIReturnObject makeDate(long ctx, long[] args);

    protected native JNIReturnObject makeError(long ctx, long[] args);

    protected native JNIReturnObject makeRegExp(long ctx, long[] args);

    protected native long getPrototype(long ctx, long object);

    protected native void setPrototype(long ctx, long object, long value);

    protected native boolean hasProperty(long ctx, long object, long propertyName);

    protected native JNIReturnObject getProperty(long ctx, long object, long propertyName);

    protected native JNIReturnObject setProperty(long ctx, long object, long propertyName, long value, int attributes);

    protected native JNIReturnObject deleteProperty(long ctx, long object, long propertyName);

    protected native JNIReturnObject getPropertyAtIndex(long ctx, long object, int propertyIndex);

    protected native JNIReturnObject setPropertyAtIndex(long ctx, long object, int propertyIndex, long value);

    @SuppressWarnings("unused")
    protected native long getPrivate(long object);

    @SuppressWarnings("unused")
    protected native boolean setPrivate(long object, long data);

    protected native boolean isFunction(long ctx, long object);

    protected native JNIReturnObject callAsFunction(long ctx, long object, long thisObject, long[] args);

    protected native boolean isConstructor(long ctx, long object);

    protected native JNIReturnObject callAsConstructor(long ctx, long object, long[] args);

    protected native long copyPropertyNames(long ctx, long object);

    protected native long[] getPropertyNames(long propertyNameArray);

    protected native void releasePropertyNames(long propertyNameArray);

    protected native long makeFunctionWithCallback(long ctx, long name);

    protected native JNIReturnObject makeFunction(long ctx, long name, long[] parameterNames,
                                                  long body, long sourceURL, int startingLineNumber);

    /* Deprecated Functions */

    /**
     * @deprecated Function constructors have been removed from JSObject since 3.0.  Use JSFunction instead.
     * @param ctx deprecated
     * @param iface deprecated
     * @param subclass deprecated
     * @since 1.0
     */
    @Deprecated
    @SuppressWarnings("unused")
    public JSObject(JSContext ctx, Class<?> iface, Class<? extends JSObject> subclass) {
        throw new UnsupportedOperationException();
    }

    /**
     * @deprecated Function constructors have been removed from JSObject since 3.0.  Use JSFunction instead.
     * @param ctx deprecated
     * @param invoke deprecated
     * @param method deprecated
     * @since 1.0
     */
    @Deprecated
    @SuppressWarnings("unused")
    public JSObject(JSContext ctx, JSObject invoke, final Method method) {
        throw new UnsupportedOperationException();
    }

    /**
     * @deprecated Function calls have been removed from JSObject since 3.0.  Use JSFunction.call or
     *             JSFunction.apply instead
     * @param thiz deprecated
     * @param args deprecated
     * @return deprecated
     * @since 1.0
     */
    @Deprecated
    @SuppressWarnings("unused")
    public JSValue callAsFunction(final JSObject thiz, final JSValue [] args) {
        throw new UnsupportedOperationException();
    }

    /**
     * @deprecated Function calls have been removed from JSObject since 3.0.  Use JSFunction.call or
     *             JSFunction.apply instead
     * @param thiz deprecated
     * @return deprecated
     * @since 2.2
     */
    @Deprecated
    @SuppressWarnings("unused")
    public JSValue callAsFunction(final JSObject thiz) {
        throw new UnsupportedOperationException();
    }

    /**
     * @deprecated Function calls have been removed from JSObject since 3.0.  Use JSFunction.call or
     *             JSFunction.apply instead
     * @param args deprecated
     * @return deprecated
     * @since 2.2
     */
    @Deprecated
    @SuppressWarnings("unused")
    public JSValue callAsFunction(final JSValue [] args) {
        throw new UnsupportedOperationException();
    }

    /**
     * @deprecated Function calls have been removed from JSObject since 3.0.  Use JSFunction.call or
     *             JSFunction.apply instead
     * @return deprecated
     * @since 2.2
     */
    @Deprecated
    @SuppressWarnings("unused")
    public JSValue callAsFunction() {
        throw new UnsupportedOperationException();
    }

    /**
     * @deprecated Constructor functions have been removed from JSObject since 3.0.  Use
     *             JSFunction.prototype() instead
     * @return deprecated
     */
    @Deprecated
    @SuppressWarnings("unused")
    public JSValue prototype() {
        throw new UnsupportedOperationException();
    }

    /**
     * @deprecated Constructor functions have been removed from JSObject since 3.0.  Use
     *             JSFunction.prototype() instead
     * @param proto deprecated
     */
    @Deprecated
    @SuppressWarnings("unused")
    public void prototype(JSValue proto) {
        throw new UnsupportedOperationException();
    }
}