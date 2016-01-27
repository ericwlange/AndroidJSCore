//
// JSObject.java
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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * A JavaScript object.
 * @since 1.0
 *
 */
public class JSObject extends JSValue {

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
	 * @param ctx  The JSContext to create the object in
	 * @since 1.0
	 */
	public JSObject(JSContext ctx) {
		context = ctx;
		valueRef = make(context.ctxRef(), 0L);
		protect(ctx,valueRef);
	}
	/**
 	 * Called only by convenience subclasses.  If you use
	 * this, you must set context and valueRef yourself.  Also,
	 * don't forget to call protect()!
	 */
	protected JSObject() {
	}
	/**
	 * Wraps an existing object from JavaScript
	 * 
	 * @param objRef  The JavaScriptCore object reference
	 * @param ctx     The JSContext of the reference
	 * @since 1.0
	 */
	protected JSObject(long objRef, JSContext ctx) {
		context = ctx;
		valueRef = objRef;
		protect(ctx,valueRef);
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
	 * @throws JSException
	 */
	public JSObject(JSContext ctx, Class<?> iface) throws JSException {
		initJSInterface(ctx, iface, null);
	}
	/**
	 * Creates a new instance of a constructor object, which exposes prototype functions in
	 * 'iface'.
	 * In JS:
	 * <pre>
	 * {@code
	 * function clss(x,y,z) {
	 *     prototype : {
	 *         func1: function(a)   { alert(a); },
	 *         func2: function(b,c) { alert(b+c+this.x); }
	 *     }
	 *     this.x = x;
	 *     this.y = y;
	 *     this.z = z;
	 * };
	 * }
	 * </pre>
	 * Where func1(), func2(), and clss() are defined in 'iface'.  'subclass' must be the
	 * subclass of JSObject which instantiates this constructor object in Java.  For example:
	 * <pre>
	 * {@code
	 * public interface Clss {
	 *     // Constructor must have the same name as the interface, prefixed by an underscore
	 *     public void _Clss(Integer x, String y, JSObject z);
	 *     public void func1(Integer a);
	 *     public void func2(Intefer b, Integer c);
	 * }
	 * public class MyClass extends JSObject implements Clss {
	 *     public MyClass(JSContext ctx) {
	 *         super(ctx, Clss.class, MyClass.cls); // This constructor!
	 *     }
	 *     public void _Clss(Integer x, String y, JSObject z) {
	 *         property("x",x);
	 *         property("y",y);
	 *         property("z",z);
	 *     }
	 *     public void func1(Integer a) {
	 *         System.out.println(a);
	 *     }
	 *     public void func2(Integer b, Integer c) {
	 *         Integer out = b + c + property("x").toNumber().intValue();
	 *         System.out.println(out);
	 *     }
	 * }
	 * }
	 * </pre>
	 * @param ctx   The JSContext to create the object in
	 * @param iface The Java Interface defining the methods to expose to JavaScript, including the constructor
	 *              function which must have the same name as the interface, prefixed by an underscore
	 * @param subclass  The JSObject subclass to instantiate when creating an instance of this prototype
	 * @since 1.0
	 * @throws JSException
	 */
	public JSObject(JSContext ctx, Class<?> iface, Class<?> subclass) throws JSException {
		initJSInterface(ctx, iface, subclass);
	}
	protected void initJSInterface(JSContext ctx, Class<?> iface, Class<?> subclass) throws JSException {
		context = ctx;
		Method[] methods = iface.getDeclaredMethods();
		String name = new String("_").concat(iface.getSimpleName());
		JSObject protoObj = null;
		for (int i=0; i<methods.length; i++) {
			if (methods[i].getName().compareTo(name) == 0) {
				// This is a constructor
				this.subclass = subclass;
				if (subclass == null) {
					context.throwJSException(new JSException(context,"Constructor function must specify subclass"));
                    valueRef = make(context.ctxRef(), 0L);
					return;
				}
				isConstructor = true;
				method = methods[i];
				this.invokeObject = this;
				valueRef = makeFunctionWithCallback(context.ctxRef(),new JSString(method.getName()).stringRef());
				hasCallback = true;
				protoObj = new JSObject(context);
				break;
			}
		}
		if (protoObj == null) {
			if (valueRef==null || valueRef==0)
				valueRef = make(context.ctxRef(), 0L);
			protoObj = this;
		}
		for (int i=0; i<methods.length; i++) {
			if (methods[i].getName().compareTo(name) != 0) {
				JSObject f = new JSObject(context, this, methods[i]);
				f.subclass = this.subclass;
				functions.add(f);
				protoObj.property(methods[i].getName(), f);
			}
		}
		if (protoObj != this) {
			this.property("prototype", protoObj);
		}
		protect(context,valueRef);
	}
	/**
	 * Creates a new function object which calls method 'method' on Java object 'invoke'.
	 * In JS:
	 * <pre>{@code
	 * var f = function(a) { ... };
	 * }
	 * </pre>
	 * 
	 * @param ctx   The JSContext to create the object in
	 * @param invoke  The JSObject on which to invoke the function
	 * @param method  The method to invoke
	 * @since 1.0
	 * @throws JSException
	 */
	public JSObject(JSContext ctx, JSObject invoke, Method method) throws JSException {
		context = ctx;
		this.method = method;
		this.invokeObject = invoke;
		valueRef = makeFunctionWithCallback(context.ctxRef(),new JSString(method.getName()).stringRef());
		hasCallback = true;
		protect(ctx,valueRef);
	}
	/**
	 * Creates a JavaScript function that takes parameters 'parameterNames' and executes the
	 * JS code in 'body'.
	 * 
	 * @param ctx  The JSContext in which to create the function
	 * @param name The name of the function
	 * @param parameterNames  A String array containing the names of the parameters
	 * @param body  The JavaScript code to execute in the function
	 * @param sourceURL  The URI of the source file, only used for reporting in stack trace (optional)
	 * @param startingLineNumber  The beginning line number, only used for reporting in stack trace (optional)
	 * @since 1.0
	 * @throws JSException
	 */
	public JSObject(JSContext ctx, String name, String [] parameterNames,
			String body, String sourceURL, int startingLineNumber) throws JSException {
		super(ctx);
		hasCallback = false;
		long [] names = new long[parameterNames.length];
		for (int i=0; i<parameterNames.length; i++) {
			names[i] = new JSString(parameterNames[i]).stringRef();
		}
		JNIReturnObject jni = makeFunction(
				context.ctxRef(),
				new JSString(name).stringRef(),
				names,
				new JSString(body).stringRef(),
				(sourceURL==null)?0L:new JSString(sourceURL).stringRef(),
				startingLineNumber);
		if (jni.exception!=0) {
			context.throwJSException(new JSException(new JSValue(jni.exception, context)));
            jni.reference = make(context.ctxRef(), 0L);
		}
		valueRef = jni.reference;
		protect(ctx,valueRef);
	}

	@Override
	protected void finalize() throws Throwable {
		if (hasCallback) {
			releaseFunctionWithCallback(context.ctxRef(), valueRef);
		}
		super.finalize();
	}

	private ArrayList<JSObject> functions = new ArrayList<JSObject>();
	private boolean hasCallback = false;
	private boolean isConstructor = false;
	private Method method = null;
	private JSObject invokeObject = null;
	protected JSObject thiz = null;
	private Class<?> subclass = null;
	
	private long functionCallback(long ctxRef, long functionRef, long thisObjectRef,
			long argumentsValueRef[], long exceptionRefRef) {
	
		assert(ctxRef == context.ctxRef());
		assert(functionRef == valueRef);
		
		try {
			JSValue [] args = new JSValue[argumentsValueRef.length];
			for (int i=0; i<argumentsValueRef.length; i++) {
				args[i] = new JSValue(argumentsValueRef[i],context);
			}
			JSObject thizz = this;
			if (invokeObject.isConstructor) {
				thizz = context.getObjectFromRef(thisObjectRef);
				thizz.invokeObject = thizz;
				thizz.method = method;
			}
			invokeObject.thiz = context.getObjectFromRef(thisObjectRef);
			JSValue value = thizz.function(args);
			setException(0L, exceptionRefRef);
			return (value==null)?0L:value.valueRef();
		} catch (JSException e) {
			e.printStackTrace();
			setException(e.getError().valueRef(), exceptionRefRef);
			return 0L;
		}
	}
	private long constructorCallback(long ctxRef, long constructorRef,
			long argumentsValueRef[], long exceptionRefRef) {
	
		assert(ctxRef == context.ctxRef());
		assert(constructorRef == this.valueRef);
		try {
			JSValue [] args = new JSValue[argumentsValueRef.length];
			for (int i=0; i<argumentsValueRef.length; i++) {
				args[i] = new JSValue(argumentsValueRef[i],context);
			}
			JSObject newObj = constructor(args);
			setException(0L, exceptionRefRef);
			return newObj.valueRef();
		} catch (JSException e) {
			setException(e.getError().valueRef(), exceptionRefRef);
			return 0L;
		}
	}
	private void finalizeCallback(long objectRef) {
		assert(objectRef == this.valueRef);
		context.finalizeObject(this);
	}

	protected JSValue function(JSValue [] args) throws JSException {
		// Call method on parent object
		Class<?>[] pType  = method.getParameterTypes();
		Object [] passArgs = new Object[pType.length];
		for (int i=0; i<passArgs.length; i++) {
			if (i<args.length) {
				if (args[i]==null) passArgs[i] = null;
				else if (pType[i] == String.class) passArgs[i] = args[i].toString();
				else if (pType[i] == Double.class) passArgs[i] = args[i].toNumber();
				else if (pType[i] == Integer.class) passArgs[i] = args[i].toNumber().intValue();
				else if (pType[i] == Long.class) passArgs[i] = args[i].toNumber().longValue();
				else if (pType[i] == Boolean.class) passArgs[i] = args[i].toBoolean();
				else if (pType[i] == JSObject.class) passArgs[i] = args[i].toObject();
				else if (pType[i] == JSString.class) passArgs[i] = args[i].toJSString();
				else if (pType[i].isArray()) {
					JSObject arr = args[i].toObject();
					if (arr.property("length") == null) {
						// Not an array
						passArgs[i] = null;
					} else {
						Integer length = arr.property("length").toNumber().intValue();
						ArrayList<Object> objList = new ArrayList<Object>();
						for (int j=0; j<length; j++) {
							if (pType[i] == Boolean[].class)
								objList.add(arr.propertyAtIndex(j).toBoolean());
							else if (pType[i] == Integer[].class)
								objList.add(arr.propertyAtIndex(j).toNumber().intValue());
							else if (pType[i] == String[].class)
								objList.add(arr.propertyAtIndex(j).toString());
							else if (pType[i] == Long[].class)
								objList.add(arr.propertyAtIndex(j).toNumber().longValue());
							else if (pType[i] == Double[].class)
								objList.add(arr.propertyAtIndex(j).toNumber());
							else if (pType[i] == JSValue[].class)
								objList.add(arr.propertyAtIndex(j));
							else if (pType[i] == JSObject[].class)
								objList.add(arr.propertyAtIndex(j).toObject());
							else if (pType[i] == JSString[].class)
								objList.add(arr.propertyAtIndex(j).toJSString());
							else objList.add(null);
	 					}
						if (pType[i] == Boolean[].class)
							passArgs[i] = objList.toArray(new Boolean[objList.size()]);
						else if (pType[i] == Integer[].class)
							passArgs[i] = objList.toArray(new Integer[objList.size()]);
						else if (pType[i] == String[].class)
							passArgs[i] = objList.toArray(new String[objList.size()]);
						else if (pType[i] == Long[].class)
							passArgs[i] = objList.toArray(new Long[objList.size()]);
						else if (pType[i] == Double[].class)
							passArgs[i] = objList.toArray(new Double[objList.size()]);
						else if (pType[i] == JSValue[].class)
							passArgs[i] = objList.toArray(new JSValue[objList.size()]);
						else if (pType[i] == JSObject[].class)
							passArgs[i] = objList.toArray(new JSObject[objList.size()]);
						else if (pType[i] == JSString[].class)
							passArgs[i] = objList.toArray(new JSString[objList.size()]);
						else passArgs[i] = null;
					}
				}
				else if (pType[i] == JSValue.class) passArgs[i] = args[i];
				else passArgs[i] = null;
			} else {
				passArgs[i] = null;
			}
		}
		try {
			Object ret = method.invoke(invokeObject, passArgs);
			if (method.getReturnType() == Void.class) return null;
			if (ret instanceof JSValue) return (JSValue)ret;
			return new JSValue(context,ret);
		} catch (InvocationTargetException e) {
			e.printStackTrace();
			context.throwJSException(new JSException(context, e.toString()));
		} catch (IllegalAccessException e) {
			context.throwJSException(new JSException(context, e.toString()));
		}
        return new JSValue(context);
	}

	protected JSObject constructor(JSValue [] args) throws JSException {
		JSValue proto = new JSValue(this.property("prototype").valueRef(),context);
		try {
			Constructor<?> constructor = subclass.getConstructor(long.class, JSContext.class);
			JSObject thiz = (JSObject) constructor.newInstance(makeWithFinalizeCallback(context.ctxRef()),context);
			context.persistObject(thiz);
			thiz.invokeObject = thiz;
			thiz.prototype(proto);
			thiz.method = method;
			if (method!=null) {
				thiz.function(args);
			}
			return thiz;
		} catch (NoSuchMethodException e) {
			context.throwJSException(new JSException(context, e.toString()));
		} catch (InvocationTargetException e) {
			context.throwJSException(new JSException(context, e.toString()));
		} catch (IllegalAccessException e) {
			context.throwJSException(new JSException(context, e.toString()));
		} catch (InstantiationException e) {
			context.throwJSException(new JSException(context, e.toString()));
		}
        return new JSObject(context);
	}

	/**
	 * Gets the prototype object, if it exists
	 * @return A JSValue referencing the prototype object, or null if none
	 * @since 1.0
	 */
	public JSValue prototype() {
		long proto = getPrototype(context.ctxRef(), valueRef);
		if (proto==0) return null;
		return new JSValue(proto,context);
	}
	/**
	 * Sets the prototype object
	 * @param proto The object defining the function prototypes
	 * @since 1.0
	 */
	public void prototype(JSValue proto) {
		setPrototype(context.ctxRef(), valueRef, proto.valueRef());
	}
	/**
	 * Determines if the object contains a given property
	 * @param prop  The property to test the existence of
	 * @return true if the property exists on the object, false otherwise
	 * @since 1.0
	 */
	public boolean hasProperty(String prop) {
		return hasProperty(context.ctxRef(), valueRef, new JSString(prop).stringRef());
	}
	/**
	 * Gets the property named 'prop'
	 * @param prop  The name of the property to fetch
	 * @return The JSValue of the property, or null if it does not exist
	 * @since 1.0
	 * @throws JSException
	 */
	public JSValue property(String prop) throws JSException {
		JNIReturnObject jni = getProperty(context.ctxRef(), valueRef, new JSString(prop).stringRef());
        if (jni.exception!=0) {
            context.throwJSException(new JSException(new JSValue(jni.exception, context)));
            return new JSValue(context);
        }
		return new JSValue(jni.reference,context);
	}
	/**
	 * Sets the value of property 'prop'
	 * @param prop  The name of the property to set
	 * @param value  The Java object to set.  The Java object will be converted to a JavaScript object
	 *               automatically.
	 * @param attributes  And OR'd list of JSProperty constants
	 * @since 1.0
	 * @throws JSException
	 */
	public void property(String prop, Object value, int attributes) throws JSException {
		JSString name = new JSString(prop);
		JNIReturnObject jni = setProperty(
				context.ctxRef(),
				valueRef,
				name.stringRef,
				(value instanceof JSValue)?((JSValue)value).valueRef():new JSValue(context,value).valueRef(),
				attributes);
        if (jni.exception!=0) {
            context.throwJSException(new JSException(new JSValue(jni.exception, context)));
        }
	}
	/**
	 * Sets the value of property 'prop'.  No JSProperty attributes are set.
	 * @param prop  The name of the property to set
	 * @param value  The Java object to set.  The Java object will be converted to a JavaScript object
	 *               automatically.
	 * @since 1.0
	 * @throws JSException
	 */
	public void property(String prop, Object value) throws JSException {
		property(prop, value, JSPropertyAttributeNone);
	}
	/**
	 * Deletes a property from the object
	 * @param prop  The name of the property to delete
	 * @return true if the property was deleted, false otherwise
	 * @since 1.0
	 * @throws JSException
	 */
	public boolean deleteProperty(String prop) throws JSException {
		JSString name = new JSString(prop);
		JNIReturnObject jni = deleteProperty(context.ctxRef(), valueRef, name.stringRef());
        if (jni.exception!=0) {
            context.throwJSException(new JSException(new JSValue(jni.exception, context)));
            return false;
        }
		return jni.bool;
	}
	/**
	 * Returns the property at index 'index'.  Used for arrays.
	 * @param index  The index of the property
	 * @return  The JSValue of the property at index 'index'
	 * @since 1.0
	 * @throws JSException
	 */
	public JSValue propertyAtIndex(int index) throws JSException {
		JNIReturnObject jni = getPropertyAtIndex(context.ctxRef(), valueRef, index);
		if (jni.exception!=0) {
			context.throwJSException(new JSException(new JSValue(jni.exception,context)));
            return new JSValue(context);
		}
		return new JSValue(jni.reference,context);
	}
	/**
	 * Sets the property at index 'index'.  Used for arrays.
	 * @param index  The index of the property to set
	 * @param value  The Java object to set, will be automatically converted to a JavaScript value
	 * @since 1.0
	 * @throws JSException
	 */
	public void propertyAtIndex(int index, Object value) throws JSException {
		JNIReturnObject jni = setPropertyAtIndex(context.ctxRef(), valueRef,
				index, (value instanceof JSValue)?((JSValue)value).valueRef():new JSValue(context,value).valueRef());
		if (jni.exception != 0) {
            context.throwJSException(new JSException(new JSValue(jni.exception,context)));
		}		
	}
	/**
	 * Gets the list of set property names on the object
	 * @return  A string array containing the property names
	 * @since 1.0
	 */
	public String [] propertyNames() {
		long propertyNameArray = copyPropertyNames(context.ctxRef(), valueRef);
		long [] refs = getPropertyNames(propertyNameArray);
		String [] names = new String[refs.length];
		for (int i=0; i<refs.length; i++) {
			JSString name = new JSString(refs[i]);
			names[i] = name.toString();
		}
		releasePropertyNames(propertyNameArray);
		return names;
	}
	
	/**
	 * Determines if the object is a function
	 * @return true if the object is a function, false otherwise
	 * @since 1.0
	 */
	public boolean isFunction() {
		return isFunction(context.ctxRef(), valueRef);
	}
	/**
	 * Determines if the object is a constructor
	 * @return  true if the object is a constructor, false otherwise
	 * @since 1.0
	 */
	public boolean isConstructor() {
		return isConstructor(context.ctxRef(), valueRef);
	}
	/**
	 * Calls object as a JavaScript function
	 * @param thiz  The 'this' object on which the function operates, null if not on a constructor object
	 * @param args  An array of JSValues to pass as arguments to the function
	 * @return The JSValue returned by the function
	 * @since 1.0
	 * @throws JSException
	 */
	public JSValue callAsFunction(JSObject thiz, JSValue [] args) throws JSException {
		long [] valueRefs = new long[args.length];
		for (int i=0; i<args.length; i++) {
			valueRefs[i] = args[i].valueRef();
		}
		JNIReturnObject jni = callAsFunction(context.ctxRef(), valueRef, (thiz==null)?0L:thiz.valueRef(), valueRefs);
		if (jni.exception!=0) {
			context.throwJSException(new JSException(new JSValue(jni.exception,context)));
            return new JSValue(context);
		}
		return new JSValue(jni.reference,context);
	}
	
	protected native long make(long ctx, long data);
	protected native long makeWithFinalizeCallback(long ctx);
	protected native JNIReturnObject makeArray(long ctx, long [] args);
	protected native JNIReturnObject makeDate(long ctx, long [] args);
	protected native JNIReturnObject makeError(long ctx, long [] args);
	protected native JNIReturnObject makeRegExp(long ctx, long [] args);
	protected native long getPrototype(long ctx, long object);
	protected native void setPrototype(long ctx, long object, long value);
	protected native boolean hasProperty(long ctx, long object, long propertyName);
	protected native JNIReturnObject getProperty(long ctx, long object, long propertyName);
	protected native JNIReturnObject setProperty(long ctx, long object, long propertyName, long value, int attributes);
	protected native JNIReturnObject deleteProperty(long ctx, long object, long propertyName);
	protected native JNIReturnObject getPropertyAtIndex(long ctx, long object, int propertyIndex);
	protected native JNIReturnObject setPropertyAtIndex(long ctx, long object, int propertyIndex, long value);
	protected native long getPrivate(long object);
	protected native boolean setPrivate(long object, long data);
	protected native boolean isFunction(long ctx, long object);
	protected native JNIReturnObject callAsFunction(long ctx, long object, long thisObject, long [] args);
	protected native boolean isConstructor(long ctx, long object);
	protected native JNIReturnObject callAsConstructor(long ctx, long object, long [] args);
	protected native long copyPropertyNames(long ctx, long object);
	protected native long [] getPropertyNames(long propertyNameArray);
	protected native void releasePropertyNames(long propertyNameArray);
	protected native long makeFunctionWithCallback(long ctx, long name);
	protected native void releaseFunctionWithCallback(long ctx, long function);
	protected native JNIReturnObject makeFunction(long ctx, long name, long [] parameterNames,
			long body, long sourceURL, int startingLineNumber);
}
