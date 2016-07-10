//
// JSArray.java
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

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * A convenience class for handling JavaScript arrays.  Implements java.util.List interface for
 * simple integration with Java methods.
 *
 */
public class JSArray<T> extends JSBaseArray<T> {

    /**
     * Creates a JavaScript array object, initialized with 'array' JSValues
     * @param ctx  The JSContext to create the array in
     * @param array  An array of JSValues with which to initialize the JavaScript array object
     * @param cls  The class of the component objects
     * @since 3.0
     * @throws JSException
     */
    @SuppressWarnings("unused")
    public JSArray(JSContext ctx, JSValue [] array, Class<T> cls) throws JSException {
        super(cls);
        context = ctx;
        long [] valueRefs = new long[array.length];
        for (int i=0; i<array.length; i++) {
            valueRefs[i] = array[i].valueRef();
        }
        JNIReturnObject jni = makeArray(context.ctxRef(), valueRefs);
        if (jni.exception!=0) {
            context.throwJSException(new JSException(new JSValue(jni.exception, context)));
            jni.reference = make(context.ctxRef(), 0L);
        }
        valueRef = jni.reference;
        context.persistObject(this);
    }

    /**
     * Creates an empty JavaScript array object
     * @param ctx  The JSContext to create the array in
     * @param cls  The class of the component objects
     * @since 3.0
     * @throws JSException
     */
    public JSArray(JSContext ctx, Class<T> cls) throws JSException {
        super(cls);
        context = ctx;
        long [] valueRefs = new long[0];
        JNIReturnObject jni = makeArray(context.ctxRef(), valueRefs);
        if (jni.exception!=0) {
            context.throwJSException(new JSException(new JSValue(jni.exception, context)));
            jni.reference = make(context.ctxRef(), 0L);
        }
        valueRef = jni.reference;
        context.persistObject(this);
    }

    /**
     * Creates a JavaScript array object, initialized with 'array' Java values
     * @param ctx  The JSContext to create the array in
     * @param array  An array of Java objects with which to initialize the JavaScript array object.  Each
     *               Object will be converted to a JSValue
     * @param cls  The class of the component objects
     * @since 3.0
     * @throws JSException
     */
    public JSArray(JSContext ctx, Object [] array, Class<T> cls) throws JSException {
        super(cls);
        context = ctx;
        long [] valueRefs = new long[array.length];
        for (int i=0; i<array.length; i++) {
            JSValue v = new JSValue(context,array[i]);
            valueRefs[i] = v.valueRef();
        }
        JNIReturnObject jni = makeArray(context.ctxRef(), valueRefs);
        if (jni.exception!=0) {
            context.throwJSException(new JSException(new JSValue(jni.exception, context)));
            jni.reference = make(context.ctxRef(), 0L);
        }
        valueRef = jni.reference;
        context.persistObject(this);
    }

    @SuppressWarnings("unchecked")
    protected JSArray(long valueRef, JSContext ctx) {
        super(valueRef,ctx,(Class<T>)JSValue.class);
    }

    private JSArray(JSArray<T> superList, int leftBuffer, int rightBuffer, Class<T> cls) {
        super(superList,leftBuffer,rightBuffer,cls);
    }

    /**
     * Creates a JavaScript array object, initialized with 'list' Java values
     *
     * @param ctx  The JSContext to create the array in
     * @param list The Collection of values with which to initialize the JavaScript array object.  Each
     *             object will be converted to a JSValue
     * @param cls  The class of the component objects
     * @since 3.0
     * @throws JSException
     */
    public JSArray(JSContext ctx, Collection list, Class<T> cls) throws JSException {
        this(ctx,list.toArray(),cls);
    }

    @Override
    protected JSBaseArray toSuperArray(JSValue array) {
        return array.toJSArray();
    }

    /**
     * @see java.util.List#add(int, Object)
     * @since 3.0
     */
    @Override
    @SuppressWarnings("unchecked")
    public void add(final int index, final T element) {
        if (this == element) {
            throw new IllegalArgumentException();
        }
        int count = size();
        if (index > count) {
            throw new ArrayIndexOutOfBoundsException();
        }
        splice(index,0,element);
    }

    /**
     * @see java.util.List#remove(int)
     * @since 3.0
     */
    @Override
    @SuppressWarnings("unchecked")
    public T remove(final int index) {
        int count = size();
        if (index >= count) {
            throw new ArrayIndexOutOfBoundsException();
        }
        if (mSuperList == null) {
            return splice(index,1).get(0);
        } else {
            return mSuperList.remove(index + mLeftBuffer);
        }
    }

    /**
     * @see java.util.List#subList(int, int)
     * @since 3.0
     */
    @Override @NonNull
    @SuppressWarnings("unchecked")
    public List<T> subList(final int fromIndex, final int toIndex) {
        if (fromIndex < 0 || toIndex > size() || fromIndex > toIndex) {
            throw new IndexOutOfBoundsException();
        }
        return new JSArray(this,fromIndex,size()-toIndex,mType);
    }

    /** JavaScript methods **/

    /**
     * JavaScript Array.from(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/from
     * @param ctx       the JavaScript context in which to create the array
     * @param arrayLike Any array-like object to build the array from
     * @param mapFn     A JavaScript function to map each new element of the array
     * @param thiz      The 'this' pointer passed to 'mapFn'
     * @since 3.0
     * @return          A new JavaScript array
     */
    @SuppressWarnings("unchecked")
    public static JSArray<JSValue> from(JSContext ctx, Object arrayLike, JSFunction mapFn, JSObject thiz) {
        JSFunction from = ctx.property("Array").toObject().property("from").toFunction();
        return (JSArray) from.call(null,arrayLike,mapFn,thiz).toJSArray();
    }
    /**
     * JavaScript Array.from(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/from
     * @param ctx       the JavaScript context in which to create the array
     * @param arrayLike Any array-like object to build the array from
     * @param mapFn     A JavaScript function to map each new element of the array
     * @since 3.0
     * @return          A new JavaScript array
     */
    @SuppressWarnings("unchecked")
    public static JSArray<JSValue> from(JSContext ctx, Object arrayLike, JSFunction mapFn) {
        JSFunction from = ctx.property("Array").toObject().property("from").toFunction();
        return (JSArray) from.call(null,arrayLike,mapFn).toJSArray();
    }
    /**
     * JavaScript Array.from(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/from
     * @param ctx       the JavaScript context in which to create the array
     * @param arrayLike Any array-like object to build the array from
     * @since 3.0
     * @return          A new JavaScript array
     */
    @SuppressWarnings("unchecked")
    public static JSArray<JSValue> from(JSContext ctx, Object arrayLike) {
        JSFunction from = ctx.property("Array").toObject().property("from").toFunction();
        return (JSArray) from.call(null,arrayLike).toJSArray();
    }
    /**
     * JavaScript Array.from(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/from
     * @param ctx       the JavaScript context in which to create the array
     * @param arrayLike Any array-like object to build the array from
     * @param mapFn     A Java function to map each new element of the array
     * @since 3.0
     * @return          A new JavaScript array
     */
    @SuppressWarnings("unchecked")
    public static JSArray<JSValue> from(JSContext ctx, Object arrayLike,
                                        final JSBaseArrayMapCallback<JSValue> mapFn) {
        JSFunction from = ctx.property("Array").toObject().property("from").toFunction();
        return (JSArray)  from.call(null,new JSFunction(ctx,"_callback") {
            public JSValue _callback(JSValue currentValue, int index, JSArray array) {
                return mapFn.callback(currentValue,index,array);
            }
        }).toJSArray();
    }

    /**
     * JavaScript Array.isArray(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/isArray
     * @since 3.0
     * @param value the value to test
     * @return true if 'value' is an array, false otherwise
     */
    public static boolean isArray(JSValue value) {
        if (value == null) return false;
        JSFunction isArray = value.getContext().property("Array").toObject().property("isArray").toFunction();
        return isArray.call(null,value).toBoolean();
    }

    /**
     * JavaScript Array.of(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/of
     * @since 3.0
     * @param ctx    The JSContext in which to create the array
     * @param params Elements to add to the array
     * @return the new JavaScript array
     */
    @SuppressWarnings("unchecked")
    public static JSArray<JSValue> of(JSContext ctx, Object ... params) {
        JSFunction of = ctx.property("Array").toObject().property("of").toFunction();
        return (JSArray) of.apply(null,params).toJSArray();
    }

    /**
     * JavaScript Array.prototype.concat(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/concat
     * @since 3.0
     * @param params values to concantenate to the array
     * @return a new JSArray
     */
    @SuppressWarnings("unchecked")
    public JSArray<T> concat(Object ... params) {
        JSArray concat = (JSArray) property("concat").toFunction().apply(this,params).toJSArray();
        concat.mType = mType;
        return concat;
    }

    /**
     * JavaScript Array.prototype.pop(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/pop
     * @since 3.0
     * @return the popped element
     */
    @SuppressWarnings("unchecked")
    public T pop() {
        return (T) property("pop").toFunction().call(this).toJavaObject(mType);
    }

    /**
     * JavaScript Array.prototype.push(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/push
     * @since 3.0
     * @param elements  The elements to push on the array
     * @return new size of the mutated array
     */
    @SuppressWarnings("unchecked")
    public int push(T ... elements) {
        return property("push").toFunction().apply(this,elements).toNumber().intValue();
    }

    /**
     * JavaScript Array.prototype.shift(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/shift
     * @since 3.0
     * @return the element shifted off the front of the array
     */
    @SuppressWarnings("unchecked")
    public T shift() {
        JSValue shifted = property("shift").toFunction().call(this);
        if (shifted.isUndefined()) return null;
        else return (T) shifted.toJavaObject(mType);
    }

    /**
     * JavaScript Array.prototype.splice(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/splice
     * @since 3.0
     * @param start the index to start splicing from (inclusive)
     * @param deleteCount the number of elements to remove
     * @param elements the elements to insert into the array at index 'start'
     * @return a new array containing the removed elements
     */
    @SuppressWarnings("unchecked")
    public JSArray<T> splice(int start, int deleteCount, T ... elements) {
        ArrayList<Object> args = new ArrayList<>(Arrays.asList((Object[])elements));
        args.add(0,deleteCount);
        args.add(0,start);
        JSBaseArray splice = toSuperArray(property("splice").toFunction().apply(this,args.toArray()));
        splice.mType = mType;
        return (JSArray<T>)splice;
    }

    /**
     * JavaScript Array.prototype.toLocaleString(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/toLocaleString
     * Note: AndroidJSCore does not include the localization library by default, as it adds too
     * much data to the build.  This function is supported for completeness, but localized values will
     * show as empty strings
     * @return a localized string representation of the array
     */
    public String toLocaleString() {
        return property("toLocaleString").toFunction().call(this).toString();
    }

    /**
     * JavaScript Array.prototype.unshift(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/unshift
     * @since 3.0
     * @param elements The values to add to the front of the array
     * @return the new size of the mutated array
     */
    @SuppressWarnings("unchecked")
    public int unshift(T ... elements) {
        return property("unshift").toFunction().apply(this,elements).toNumber().intValue();
    }
}
