//
// JSArray.java
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

/**
 * A convenience class for handling JavaScript arrays
 *
 */
public class JSArray extends JSObject {
	/**
	 * Creates a JavaScript array object, initialized with 'array' JSValues
	 * @param ctx  The JSContext to create the array in
	 * @param array  An array of JSValues with which to initialize the JavaScript array object
	 * @throws JSException
	 */
	public JSArray(JSContext ctx, JSValue [] array) throws JSException {
		context = ctx;
		long [] valueRefs = new long[array.length];
		for (int i=0; i<array.length; i++) {
			valueRefs[i] = array[i].valueRef();
		}
		JNIReturnObject jni = makeArray(context.ctxRef(), valueRefs);
		if (jni.exception!=0) {
			throw (new JSException(new JSValue(jni.exception, context)));
		}
		valueRef = jni.reference;
		protect(ctx,valueRef);
	}
	/**
	 * Creates an empty JavaScript array object
	 * @param ctx  The JSContext to create the array in
	 * @throws JSException
	 */
	public JSArray(JSContext ctx) throws JSException {
		context = ctx;
		long [] valueRefs = new long[0];
		JNIReturnObject jni = makeArray(context.ctxRef(), valueRefs);
		if (jni.exception!=0) {
			throw (new JSException(new JSValue(jni.exception, context)));
		}
		valueRef = jni.reference;
		protect(ctx,valueRef);
	}
	/**
	 * Creates a JavaScript array object, initialized with 'array' Java values
	 * @param ctx  The JSContext to create the array in
	 * @param array  An array of Java objects with which to initialize the JavaScript array object.  Each
	 *               Object will be converted to a JSValue
	 * @throws JSException
	 */
	public JSArray(JSContext ctx, Object [] array) throws JSException {
		context = ctx;
		long [] valueRefs = new long[array.length];
		for (int i=0; i<array.length; i++) {
			JSValue v = new JSValue(context,array[i]);
			valueRefs[i] = v.valueRef();
		}
		JNIReturnObject jni = makeArray(context.ctxRef(), valueRefs);
		if (jni.exception!=0) {
			throw (new JSException(new JSValue(jni.exception, context)));
		}
		valueRef = jni.reference;
		protect(ctx,valueRef);
	}

	/**
	 * Wraps an existing JavaScript object and treats it as an array.
	 * @param objRef  The JavaScriptCore reference to the object
	 * @param ctx  The JSContext in which the array exists
	 * @throws JSException
	 */
	public JSArray(long objRef, JSContext ctx) throws JSException {
		super(objRef,ctx);
	}
	
	/**
	 * Extracts Java JSValue array from JavaScript array
	 * @return JavaScript array as Java array of JSValues
	 * @throws JSException
	 */
	public JSValue [] toArray() throws JSException {
		int count = property("length").toNumber().intValue();
		JSValue [] array = new JSValue[count];
		for (int i=0; i<count; i++) {
			array[i] = propertyAtIndex(i);
		}
		return array;
	}
	
	/**
	 * Gets JSValue at 'index'
	 * @param index  Index of the element to get
	 * @return  The JSValue at index 'index'
	 * @throws JSException
	 */
	public JSValue get(int index) throws JSException {
		int count = property("length").toNumber().intValue();
		if (index >= count) {
			throw new ArrayIndexOutOfBoundsException();
		}
		return propertyAtIndex(index);
	}

	/**
	 * Replaces a JSValue at array index 'index'.  The Java Object is converted to a JSValue.
	 * @param index  The index of the object to replace
	 * @param val  The Java object of the new value to set in the array
	 * @throws JSException
	 */
	public void replace(int index, Object val) throws JSException {
		int count = property("length").toNumber().intValue();
		if (index >= count) {
			throw new ArrayIndexOutOfBoundsException();
		}
		propertyAtIndex(index,val);
	}
	
	/**
	 * Adds a JSValue to the end of an array.  The Java Object is converted to a JSValue.
	 * @param val  The Java object to add to the array, will get converted to a JSValue
	 * @throws JSException
	 */
	public void add(Object val) throws JSException {
		int count = property("length").toNumber().intValue();
		JSValue newVal = new JSValue(context,val);
		propertyAtIndex(count,newVal);
	}

	/**
	 * Removes the value at index 'index'
	 * @param index  The index of the value to remove
	 * @throws JSException
	 */
	public void remove(int index) throws JSException {
		int count = property("length").toNumber().intValue();
		if (index >= count) {
			throw new ArrayIndexOutOfBoundsException();
		}
		for (int i=index+1; i<count; i++) {
			propertyAtIndex(i-1,propertyAtIndex(i));
		}
		property("length",count-1);
	}
	
	/**
	 * Inserts new object 'val' at index 'index'
	 * @param index  Index at which to insert value
	 * @param val  Java object to insert, will get converted to JSValue
	 * @throws JSException
	 */
	public void insert(int index, Object val) throws JSException {
		int count = property("length").toNumber().intValue();
		if (index >= count) {
			throw new ArrayIndexOutOfBoundsException();
		}
		propertyAtIndex(count,new JSValue(context));
		for (int i=count; i>index; --i) {
			propertyAtIndex(i,propertyAtIndex(i-1));
		}
		propertyAtIndex(index,val);
	}
	
	/**
	 * Gets the number of elements in the array
	 * @return  length of the array
	 * @throws JSException
	 */
	public int length() throws JSException {
		int count = property("length").toNumber().intValue();
		return count;
	}
}
