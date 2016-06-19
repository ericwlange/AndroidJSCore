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

import java.lang.reflect.Array;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * A convenience class for handling JavaScript arrays.  Implements java.util.List interface for
 * simple integration with Java methods.
 *
 */
public class JSArray<T> extends JSObject implements List<T> {

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
		context = ctx;
        mType = cls;
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
		context = ctx;
        mType = cls;
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
		context = ctx;
        mType = cls;
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

    /**
	 * Wraps an existing JavaScript object and treats it as an array.
	 * @param objRef  The JavaScriptCore reference to the object
	 * @param ctx  The JSContext in which the array exists
	 * @since 1.0
	 * @throws JSException
	 */
    @SuppressWarnings("unchecked")
	public JSArray(long objRef, JSContext ctx) throws JSException {
		super(objRef,ctx);
        mType = (Class<T>) Object.class;
	}

    private JSArray(JSArray<T> superList, int leftBuffer, int rightBuffer, Class<T> cls) {
        mType = cls;
        mLeftBuffer = leftBuffer;
        mRightBuffer = rightBuffer;
        context = superList.context;
        valueRef = superList.valueRef();
        mSuperList = superList;
    }

    private Class<T> mType;
    private int mLeftBuffer = 0;
    private int mRightBuffer = 0;
    private JSArray<T> mSuperList = null;

    /**
     * Converts to a static array with elements of class 'clazz'
     * @param clazz   The class to convert the elements to (Integer.class, Double.class,
     *                String.class, JSValue.class, etc.)
     * @return The captured static array
     * @since 3.0
     * @throws JSException
     */
    public Object[] toArray(Class clazz) throws JSException {
		int count = size();

		Object [] array = (Object[]) Array.newInstance(clazz,count);
        for (int i=0; i<count; i++) {
			array[i] = propertyAtIndex(i).toJavaObject(clazz);
		}
		return array;
	}

	/**
	 * Extracts Java JSValue array from JavaScript array
     * @see List#toArray()
	 * @return JavaScript array as Java array of JSValues
	 * @throws JSException
	 */
    @Override @NonNull
	public Object [] toArray() throws JSException {
		return toArray(mType);
	}
	
	/**
	 * Gets JSValue at 'index'
     * @see List#get(int)
	 * @param index  Index of the element to get
	 * @return  The JSValue at index 'index'
	 * @since 1.0
	 * @throws JSException
	 */
    @Override
    @SuppressWarnings("unchecked")
	public T get(final int index) {
		int count = size();
		if (index >= count) {
			throw new ArrayIndexOutOfBoundsException();
		}
		return (T) propertyAtIndex(index).toJavaObject(mType);
	}

	/**
	 * Adds a JSValue to the end of an array.  The Java Object is converted to a JSValue.
     * @see List#add(Object)
	 * @param val  The Java object to add to the array, will get converted to a JSValue
	 * @since 1.0
	 * @throws JSException
	 */
    @Override
	public boolean add(final T val) throws JSException {
		int count = size();
		JSValue newVal = new JSValue(context,val);
		propertyAtIndex(count,newVal);
        return true;
	}

    /**
     * @see List#size()
     * @since 3.0
     */
    @Override
	public int size() {
        if (mSuperList == null) {
            return property("length").toNumber().intValue();
        } else {
            return Math.max(0, mSuperList.size() - mLeftBuffer - mRightBuffer);
        }
	}

    @Override
    public JSValue propertyAtIndex(final int index) {
        if (mSuperList == null)
            return super.propertyAtIndex(index);
        else
            return mSuperList.propertyAtIndex(index + mLeftBuffer);
    }

    @Override
    public void propertyAtIndex(final int index, final Object value) {
        if (mSuperList == null)
            super.propertyAtIndex(index, value);
        else
            mSuperList.propertyAtIndex(index + mLeftBuffer, value);
    }

    /**
     * @see List#isEmpty() ()
     * @since 3.0
     */
    @Override
    public boolean isEmpty() {
        return (size() == 0);
    }

    /**
     * @see List#contains(Object)  ()
     * @since 3.0
     */
    @Override
    public boolean contains(final Object object) {
        for (int i=0; i<size(); i++) {
            if(get(i).equals(object))
                return true;
        }
        return false;
    }

    private class ArrayIterator implements ListIterator<T> {
        private int current = 0;
        private Integer modifiable = null;

        public ArrayIterator() {
            this(0);
        }
        public ArrayIterator(int index) {
            if (index > size()) index = size();
            if (index < 0) index = 0;
            current = index;
        }

        @Override
        public boolean hasNext() {
            return (current < size());
        }

        @Override
        public boolean hasPrevious() {
            return (current > 0);
        }

        @Override
        public T next() {
            if (!hasNext())
                throw new NoSuchElementException();
            modifiable = current;
            return get(current++);
        }

        @Override
        public T previous() {
            if (!hasPrevious())
                throw new NoSuchElementException();
            modifiable = --current;
            return get(current);
        }

        @Override
        public void remove() {
            if (modifiable==null)
                throw new NoSuchElementException();

            JSArray.this.remove(modifiable.intValue());
            current = modifiable;
            modifiable = null;
        }

        @Override
        public int nextIndex() {
            return current;
        }

        @Override
        public int previousIndex() {
            return current - 1;
        }

        @Override
        public void set(T value) {
            if (modifiable==null)
                throw new NoSuchElementException();

            JSArray.this.set(modifiable,value);
        }

        @Override
        public void add(T value) {
            JSArray.this.add(current++,value);
            modifiable = null;
        }
    }

    /**
     * @see List#iterator()
     * @since 3.0
     */
    @Override
    public @NonNull Iterator<T> iterator() {
        return new ArrayIterator();
    }

    /**
     * @see List#toArray(Object[])
     * @since 3.0
     */
    @Override
    @SuppressWarnings("unchecked")
    @NonNull
    public <T> T[] toArray(final @NonNull T[] elemArray) {
        if (size() > elemArray.length) {
            return (T[])toArray();
        }
        ArrayIterator iterator = new ArrayIterator();
        int index = 0;
        while (iterator.hasNext()) {
            Object next = iterator.next();
            elemArray[index++] = (T)next;
        }
        for (int i = index; i < elemArray.length; i++) {
            elemArray[i] = null;
        }
        return elemArray;
    }

    /**
     * @see List#remove(Object)
     * @since 3.0
     */
    @Override
    public boolean remove(final Object object) {
        ArrayIterator listIterator = new ArrayIterator();
        while (listIterator.hasNext()) {
            if (listIterator.next().equals(object)) {
                listIterator.remove();
                return true;
            }
        }
        return false;
    }

    /**
     * @see List#containsAll(Collection)
     * @since 3.0
     */
    @Override
    public boolean containsAll(final @NonNull Collection<?> collection) {
        for (Object item : collection.toArray()) {
            if (!contains(item)) return false;
        }
        return true;
    }

    /**
     * @see List#addAll(Collection)
     * @since 3.0
     */
    @Override
    public boolean addAll(final @NonNull  Collection<? extends T> collection) {
        return addAll(size(), collection);
    }

    /**
     * @see List#addAll(int, Collection)
     * @since 3.0
     */
    @Override
    @SuppressWarnings("unchecked")
    public boolean addAll(final int index, final @NonNull Collection<? extends T> collection) {
        if (collection.isEmpty()) {
            return false;
        }

        int i = index;
        for (Object item : collection.toArray()) {
            add(i++,(T)item);
        }
        return true;
    }

    /**
     * @see List#removeAll(Collection)
     * @since 3.0
     */
    @Override
    public boolean removeAll(final @NonNull Collection<?> collection) {
        if (collection.isEmpty()) {
            return false;
        }
        boolean any = false;
        ListIterator<T> listIterator = listIterator();
        while (listIterator.hasNext()) {
            T compare = listIterator.next();
            for (Object element : collection) {
                if (compare.equals(element)) {
                    listIterator.remove();
                    any = true;
                    break;
                }
            }
        }
        return any;
    }

    /**
     * @see List#retainAll(Collection)
     * @since 3.0
     */
    @Override
    public boolean retainAll(final @NonNull Collection<?> collection) {
        if (collection.isEmpty()) {
            return false;
        }
        boolean any = false;
        ListIterator<T> listIterator = listIterator();
        while (listIterator.hasNext()) {
            T compare = listIterator.next();
            boolean remove = true;
            for (Object element : collection) {
                if (compare.equals(element)) {
                    remove = false;
                    break;
                }
            }
            if (remove) {
                listIterator.remove();
                any = true;
            }
        }
        return any;
    }

    /**
     * @see List#clear()
     * @since 3.0
     */
    @Override
    public void clear() {
        if (isEmpty()) {
            return;
        }
        for (int i=size(); i > 0; --i) {
            remove(i-1);
        }
    }

    /**
     * @see List#set(int, Object)
     * @since 3.0
     */
    @Override
    @SuppressWarnings("unchecked")
    public T set(final int index, final T element) {
        int count = size();
        if (index >= count) {
            throw new ArrayIndexOutOfBoundsException();
        }
        JSValue oldValue = propertyAtIndex(index);
        propertyAtIndex(index,element);
        return (T) oldValue.toJavaObject(mType);
    }

    /**
     * @see List#add(int, Object)
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
     * @see List#remove(int)
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
     * @see List#indexOf(Object)
     * @since 3.0
     */
    @Override
    public int indexOf(final Object object) {
        ListIterator<T> listIterator = listIterator();
        while (listIterator.hasNext()) {
            if (listIterator.next().equals(object)) {
                return listIterator.nextIndex() - 1;
            }
        }
        return -1;
    }

    /**
     * @see List#lastIndexOf(Object)
     * @since 3.0
     */
    @Override
    public int lastIndexOf(final Object object) {
        ListIterator<T> listIterator = listIterator(size());
        while (listIterator.hasPrevious()) {
            if (listIterator.previous().equals(object)) {
                return listIterator.previousIndex() + 1;
            }
        }
        return -1;
    }

    /**
     * @see List#listIterator()
     * @since 3.0
     */
    @Override
    public ListIterator<T> listIterator() {
        return listIterator(0);
    }

    /**
     * @see List#listIterator(int)
     * @since 3.0
     */
    @Override @NonNull
    public ListIterator<T> listIterator(final int index) {
        return new ArrayIterator(index);
    }

    /**
     * @see List#subList(int, int)
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

    /**
     * @see List#equals(Object)
     * @since 3.0
     */
    @Override
    public boolean equals(final Object other) {
        if (other == null) {
            return false;
        }
        if (!(other instanceof List<?>)) {
            return false;
        }
        List<?> otherList = (List<?>)other;
        if (size() != otherList.size()) {
            return false;
        }
        Iterator<T> iterator = iterator();
        Iterator<?> otherIterator = otherList.iterator();
        while (iterator.hasNext() && otherIterator.hasNext()) {
            T next = iterator.next();
            Object otherNext = otherIterator.next();
            if (!next.equals(otherNext)) {
                return false;
            }
        }
        return true;
    }

    /**
     * @see List#hashCode()
     * @since 3.0
     */
    @Override
    public int hashCode() {
        int hashCode = 1;
        for (Iterator<T> it = listIterator(); it.hasNext(); ) {
            T e = it.next();
            hashCode = 31 * hashCode + (e == null ? 0 : e.hashCode());
        }
        return hashCode;
    }

    /**
     * @deprecated  Deprecated in 3.0, use JSArray(JSContext ctx, JSValue[] array, Class<T> cls)
     *              instead
     * @param ctx  The JSContext to create the array in
     * @param array  An array of JSValues with which to initialize the JavaScript array object
     * @since 1.0
     * @throws JSException
     */
    @Deprecated
    @SuppressWarnings("unused")
    public JSArray(JSContext ctx, JSValue [] array) throws JSException {
        throw new UnsupportedOperationException();
    }

    /**
     * @deprecated Deprecated in 3.0, use JSArray(JSContext ctx, Class<T> cls) instead
     * @param ctx  The JSContext to create the array in
     * @since 1.0
     * @throws JSException
     */
    @Deprecated
    @SuppressWarnings("unused")
    public JSArray(JSContext ctx) throws JSException {
        throw new UnsupportedOperationException();
    }

    /**
     * @deprecated Deprecated in 3.0, use JSArray(JSContext ctx, Object[] array, Class<T> cls)
     *              instead
     * @param ctx  The JSContext to create the array in
     * @param array  An array of Java objects with which to initialize the JavaScript array object.  Each
     *               Object will be converted to a JSValue
     * @since 1.0
     * @throws JSException
     */
    @Deprecated
    @SuppressWarnings("unused")
    public JSArray(JSContext ctx, Object [] array) throws JSException {
        throw new UnsupportedOperationException();
    }

    /** JavaScript methods **/

    /**
     * Interface containing a condition test callback function
     * @since 3.0
     * @param <T>
     */
    public interface JSArrayEachBooleanCallback<T> {
        /**
         * A function to test each element of an array for a condition
         * @param currentValue value to test
         * @param index index in 'array'
         * @param array array being traversed
         * @since 3.0
         * @return true if condition is met, false otherwise
         */
        public boolean callback(T currentValue, int index, JSArray<T> array);
    }

    /**
     * Interface containing a function to call on each element of an array
     * @since 3.0
     * @param <T>
     */
    public interface JSArrayForEachCallback<T> {
        /**
         * A function to call on each element of the array
         * @param currentValue current value in the array
         * @param index index in 'array'
         * @param array array being traversed
         * @since 3.0
         */
        public void callback(T currentValue, int index, JSArray<T> array);
    }

    /**
     * Interface containing a map function
     * @since 3.0
     * @param <T>
     */
    public interface JSArrayMapCallback<T> {
        /**
         * A function to map an array value to a new JSValue
         * @param currentValue value to map
         * @param index index in 'array'
         * @param array array being traversed
         * @since 3.0
         * @return mapped value
         */
        public JSValue callback(T currentValue, int index, JSArray<T> array);
    }

    /**
     * Interface containing a reduce function
     * @since 3.0
     */
    public interface JSArrayReduceCallback {
        /**
         * A function to reduce a mapped value into an accumulator
         * @param previousValue previous value of the accumulator
         * @param currentValue value of mapped item
         * @param index index in 'array'
         * @param array map array being traversed
         * @since 3.0
         * @return new accumulator value
         */
        public JSValue callback(JSValue previousValue, JSValue currentValue, int index,
                                JSArray<JSValue> array);
    }

    /**
     * Interface containing a compare function callback for sort
     * @since 3.0
     * @param <T>
     */
    public interface JSArraySortCallback<T> {
        /**
         * A function for comparing values in a sort
         * @param a first value
         * @param b second value
         * @since 3.0
         * @return 0 if values are the same, negative if 'b' comes before 'a', and positive if 'a' comes
         *         before 'b'
         */
        public double callback(T a, T b);
    }

    private JSValue each(JSFunction callback, JSObject thiz, String each) {
        return property(each).toFunction().call(this,callback,thiz);
    }
    private JSValue each(final JSArrayEachBooleanCallback<T> callback, String each) {
        return property(each).toFunction().call(this,new JSFunction(context,"_callback") {
            @SuppressWarnings("unchecked")
            public boolean _callback(T currentValue, int index, JSArray array) {
                return callback.callback((T)((JSValue)currentValue).toJavaObject(mType),index,array);
            }
        });
    }
    private JSValue each(final JSArrayForEachCallback<T> callback, String each) {
        return property(each).toFunction().call(this,new JSFunction(context,"_callback") {
            @SuppressWarnings("unchecked")
            public void _callback(T currentValue, int index, JSArray array) {
                callback.callback((T)((JSValue)currentValue).toJavaObject(mType),index,array);
            }
        });
    }
    private JSValue each(final JSArrayMapCallback<T> callback, String each) {
        return property(each).toFunction().call(this,new JSFunction(context,"_callback") {
            @SuppressWarnings("unchecked")
            public JSValue _callback(T currentValue, int index, JSArray<T> array) {
                return callback.callback((T)((JSValue)currentValue).toJavaObject(mType),index,array);
            }
        });
    }
    private JSValue each(final JSArrayReduceCallback callback, String each, Object initialValue) {
        return property(each).toFunction().call(this,new JSFunction(context,"_callback") {
            public JSValue _callback(JSValue previousValue, JSValue currentValue, int index,
                                     JSArray<JSValue> array) {
                return callback.callback(previousValue,currentValue,index,array);
            }
        },initialValue);
    }

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
        return from.call(null,arrayLike,mapFn,thiz).toJSArray();
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
        return from.call(null,arrayLike,mapFn).toJSArray();
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
        return from.call(null,arrayLike).toJSArray();
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
                                        final JSArrayMapCallback<JSValue> mapFn) {
        JSFunction from = ctx.property("Array").toObject().property("from").toFunction();
        return from.call(null,new JSFunction(ctx,"_callback") {
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

    //

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
        return of.apply(null,params).toJSArray();
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
        JSArray concat = property("concat").toFunction().apply(this,params).toJSArray();
        concat.mType = mType;
        return concat;
    }

    /**
     * JavaScript Array.prototype.copyWithin(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/copyWithin
     * @since 3.0
     * @param target index to copy sequence to
     * @param start  index from which to start copying from
     * @param end    index from which to end copying from
     * @return this (mutable operation)
     */
    @SuppressWarnings("unchecked")
    public JSArray<T> copyWithin(int target, int start, int end) {
        return property("copyWithin").toFunction().call(this,target,start,end).toJSArray();
    }
    /**
     * JavaScript Array.prototype.copyWithin(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/copyWithin
     * @since 3.0
     * @param target index to copy sequence to
     * @param start  index from which to start copying from
     * @return this (mutable operation)
     */
    public JSArray<T> copyWithin(int target, int start) {
        return copyWithin(target,start,size());
    }
    /**
     * JavaScript Array.prototype.copyWithin(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/copyWithin
     * @since 3.0
     * @param target index to copy sequence to
     * @return this (mutable operation)
     */
    public JSArray<T> copyWithin(int target) {
        return copyWithin(target,0);
    }

    /**
     * An array entry Iterator
     * @since 3.0
     * @param <U>
     */
    public class JSArrayEntriesIterator<U> extends JSIterator<Map.Entry<Integer,U>> {
        protected JSArrayEntriesIterator(JSObject iterator) {
            super(iterator);
        }

        /**
         * Gets the next entry in the array
         * @return a Map.Entry element containing the index and value
         */
        @Override
        @SuppressWarnings("unchecked")
        public Map.Entry<Integer,U> next() {
            JSObject next = jsnext().value().toObject();
            return new AbstractMap.SimpleEntry<Integer,U>(next.propertyAtIndex(0).toNumber().intValue(),
                    (U) next.propertyAtIndex(1).toJavaObject(mType));
        }
    }

    //

    /**
     * JavaScript Array.prototype.entries(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/entries
     * @since 3.0
     * @return an entry iterator
     */
    public JSArrayEntriesIterator<T> entries() {
        return new JSArrayEntriesIterator<T>(property("entries").toFunction().call(this).toObject());
    }

    /**
     * JavaScript: Array.prototype.every(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/every
     * @since 3.0
     * @param callback the JavaScript function to call on each element
     * @param thiz the 'this' value passed to callback
     * @return true if every element in the array meets the condition, false otherwise
     */
    public boolean every(JSFunction callback, JSObject thiz) {
        return each(callback,thiz,"every").toBoolean();
    }
    /**
     * JavaScript: Array.prototype.every(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/every
     * @since 3.0
     * @param callback the JavaScript function to call on each element
     * @return true if every element in the array meets the condition, false otherwise
     */
    public boolean every(JSFunction callback) {
        return every(callback,null);
    }
    /**
     * JavaScript: Array.prototype.every(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/every
     * @since 3.0
     * @param callback the Java function to call on each element
     * @return true if every element in the array meets the condition, false otherwise
     */
    public boolean every(final JSArrayEachBooleanCallback<T> callback) {
        return each(callback,"every").toBoolean();
    }

    /**
     * JavaScript Array.prototype.fill(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/fill
     * @since 3.0
     * @param value the value to fill
     * @param start the index to start filling
     * @param end   the index (exclusive) to stop filling
     * @return this (mutable)
     */
    @SuppressWarnings("unchecked")
    public JSArray<T> fill(T value, int start, int end) {
        return property("fill").toFunction().call(this,value,start,end).toJSArray();
    }
    /**
     * JavaScript Array.prototype.fill(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/fill
     * @since 3.0
     * @param value the value to fill
     * @param start the index to start filling
     * @return this (mutable)
     */
    public JSArray<T> fill(T value, int start) {
        return fill(value,start,size());
    }
    /**
     * JavaScript Array.prototype.fill(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/fill
     * @since 3.0
     * @param value the value to fill
     * @return this (mutable)
     */
    public JSArray<T> fill(T value) {
        return fill(value,0);
    }

    /**
     * JavaScript Array.prototype.filter(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/filter
     * @since 3.0
     * @param callback the JavaScript function to call on each element
     * @param thiz the 'this' value passed to callback
     * @return a new filtered array
     */
    @SuppressWarnings("unchecked")
    public JSArray<T> filter(JSFunction callback, JSObject thiz) {
        JSArray filter = each(callback,thiz,"filter").toJSArray();
        filter.mType = mType;
        return filter;
    }
    /**
     * JavaScript Array.prototype.filter(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/filter
     * @since 3.0
     * @param callback the JavaScript function to call on each element
     * @return a new filtered array
     */
    public JSArray<T> filter(JSFunction callback) {
        return filter(callback,null);
    }
    /**
     * JavaScript Array.prototype.filter(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/filter
     * @since 3.0
     * @param callback the Java function to call on each element
     * @return a new filtered array
     */
    @SuppressWarnings("unchecked")
    public JSArray<T> filter(final JSArrayEachBooleanCallback<T> callback) {
        JSArray filter = each(callback,"filter").toJSArray();
        filter.mType = mType;
        return filter;
    }

    /**
     * JavaScript Array.prototype.find(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/find
     * @since 3.0
     * @param callback the JavaScript function to call on each element
     * @param thiz the 'this' value passed to callback
     * @return the first value matching the condition set by the function
     */
    @SuppressWarnings("unchecked")
    public T find(JSFunction callback, JSObject thiz) {
        return (T) each(callback,thiz,"find").toJavaObject(mType);
    }
    /**
     * JavaScript Array.prototype.find(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/find
     * @since 3.0
     * @param callback the JavaScript function to call on each element
     * @return the first value matching the condition set by the function
     */
    public T find(JSFunction callback) {
        return find(callback,null);
    }
    /**
     * JavaScript Array.prototype.find(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/find
     * @since 3.0
     * @param callback the Java function to call on each element
     * @return the first value matching the condition set by the function
     */
    @SuppressWarnings("unchecked")
    public T find(final JSArrayEachBooleanCallback<T> callback) {
        return (T) each(callback,"find").toJavaObject(mType);
    }

    /**
     * JavaScript Array.prototype.findIndex(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/findIndex
     * @since 3.0
     * @param callback the JavaScript function to call on each element
     * @param thiz the 'this' value passed to callback
     * @return the index of the first value matching the condition set by the function
     */
    public int findIndex(JSFunction callback, JSObject thiz) {
        return each(callback,thiz,"findIndex").toNumber().intValue();
    }
    /**
     * JavaScript Array.prototype.findIndex(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/findIndex
     * @since 3.0
     * @param callback the JavaScript function to call on each element
     * @return the index of the first value matching the condition set by the function
     */
    public int findIndex(JSFunction callback) {
        return findIndex(callback,null);
    }
    /**
     * JavaScript Array.prototype.findIndex(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/findIndex
     * @since 3.0
     * @param callback the Java function to call on each element
     * @return the index of the first value matching the condition set by the function
     */
    public int findIndex(final JSArrayEachBooleanCallback<T> callback) {
        return each(callback,"findIndex").toNumber().intValue();
    }

    /**
     * JavaScript Array.prototype.forEach(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/forEach
     * @since 3.0
     * @param callback the JavaScript function to call on each element
     * @param thiz the 'this' value passed to callback
     */
    public void forEach(JSFunction callback, JSObject thiz) {
        each(callback,thiz,"forEach");
    }
    /**
     * JavaScript Array.prototype.forEach(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/forEach
     * @since 3.0
     * @param callback the JavaScript function to call on each element
     */
    public void forEach(JSFunction callback) {
        forEach(callback,null);
    }
    /**
     * JavaScript Array.prototype.forEach(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/forEach
     * @since 3.0
     * @param callback the Java function to call on each element
     */
    public void forEach(final JSArrayForEachCallback<T> callback) {
        each(callback,"forEach");
    }

    /**
     * JavaScript Array.prototype.includes(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/includes
     * @since 3.0
     * @param element   the value to search for
     * @param fromIndex the index in the array to start searching from
     * @return true if the element exists in the array, false otherwise
     */
    public boolean includes(T element, int fromIndex) {
        return property("includes").toFunction().call(this,element,fromIndex).toBoolean();
    }
    /**
     * JavaScript Array.prototype.includes(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/includes
     * @since 3.0
     * @param element   the value to search for
     * @return true if the element exists in the array, false otherwise
     */
    public boolean includes(T element) {
        return includes(element,0);
    }

    /**
     * JavaScript Array.prototype.indexOf(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/indexOf
     * @since 3.0
     * @param element   the value to search for
     * @param fromIndex the index in the array to start searching from
     * @return index of the first instance of 'element', -1 if not found
     */
    public int indexOf(T element, int fromIndex) {
        return property("indexOf").toFunction().call(this,element,fromIndex).toNumber().intValue();
    }

    /**
     * JavaScript Array.prototype.join(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/join
     * @since 3.0
     * @param separator the separator to use between values
     * @return a string representation of the joined array
     */
    public String join(String separator) {
        return property("join").toFunction().call(this,separator).toString();
    }
    /**
     * JavaScript Array.prototype.join(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/join
     * @since 3.0
     * @return a string representation of the joined array with a comma separator
     */
    public String join() {
        return property("join").toFunction().call(this).toString();
    }

    /**
     * An array key Iterator
     * @since 3.0
     */
    public class JSArrayKeysIterator extends JSIterator<Integer> {
        protected JSArrayKeysIterator(JSObject iterator) {
            super(iterator);
        }

        /**
         * Gets the next key in the array
         * @return the array index
         */
        @Override
        @SuppressWarnings("unchecked")
        public Integer next() {
            Next jsnext = jsnext();

            if (jsnext.value().isUndefined()) return null;

            JSValue next = jsnext.value();
            return (Integer) next.toJavaObject(Integer.class);
        }
    }

    /**
     * JavaScript Array.prototype.keys(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/keys
     * @since 3.0
     * @return An array index iterator
     */
    public JSArrayKeysIterator keys() {
        return new JSArrayKeysIterator(property("keys").toFunction().call(this).toObject());
    }

    /**
     * JavaScript Array.prototype.lastIndexOf(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/lastIndexOf
     * @since 3.0
     * @param element   the value to search for
     * @param fromIndex the index in the array to start searching from (reverse order)
     * @return index of the last instance of 'element', -1 if not found
     */
    public int lastIndexOf(T element, int fromIndex) {
        return property("lastIndexOf").toFunction().call(this,element,fromIndex).toNumber().intValue();
    }

    /**
     * JavaScript Array.prototype.map(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/map
     * @since 3.0
     * @param callback the JavaScript function to call on each element
     * @param thiz the 'this' value passed to callback
     * @return a new mapped array
     */
    @SuppressWarnings("unchecked")
    public JSArray<JSValue> map(JSFunction callback, JSObject thiz) {
        return each(callback,thiz,"map").toJSArray();
    }
    /**
     * JavaScript Array.prototype.map(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/map
     * @since 3.0
     * @param callback the JavaScript function to call on each element
     * @return a new mapped array
     */
    public JSArray<JSValue> map(JSFunction callback) {
        return map(callback,null);
    }
    /**
     * JavaScript Array.prototype.map(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/map
     * @since 3.0
     * @param callback the Java function to call on each element
     * @return a new mapped array
     */
    @SuppressWarnings("unchecked")
    public JSArray<JSValue> map(final JSArrayMapCallback<T> callback) {
        return each(callback,"map").toJSArray();
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
     * JavaScript Array.prototype.reduce(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/Reduce
     * @since 3.0
     * @param callback  The JavaScript reduce function to call
     * @param initialValue The initial value of the reduction
     * @return A reduction of the mapped array
     */
    public JSValue reduce(JSFunction callback, Object initialValue) {
        return property("reduce").toFunction().call(this,callback,initialValue);
    }
    /**
     * JavaScript Array.prototype.reduce(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/Reduce
     * @since 3.0
     * @param callback  The JavaScript reduce function to call
     * @return A reduction of the mapped array
     */
    public JSValue reduce(JSFunction callback) {
        return property("reduce").toFunction().call(this,callback);
    }
    /**
     * JavaScript Array.prototype.reduce(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/Reduce
     * @since 3.0
     * @param callback  The Java reduce function to call
     * @param initialValue The initial value of the reduction
     * @return A reduction of the mapped array
     */
    public JSValue reduce(final JSArrayReduceCallback callback, Object initialValue) {
        return each(callback,"reduce",initialValue);
    }
    /**
     * JavaScript Array.prototype.reduce(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/Reduce
     * @since 3.0
     * @param callback  The Java reduce function to call
     * @return A reduction of the mapped array
     */
    public JSValue reduce(final JSArrayReduceCallback callback) {
        return reduce(callback,null);
    }

    /**
     * JavaScript Array.prototype.reduceRight(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/ReduceRight
     * @since 3.0
     * @param callback  The JavaScript reduce function to call
     * @param initialValue The initial value of the reduction
     * @return A reduction of the mapped array
     */
    public JSValue reduceRight(JSFunction callback, JSObject thiz, Object initialValue) {
        return property("reduceRight").toFunction().call(this,callback,initialValue);
    }
    /**
     * JavaScript Array.prototype.reduceRight(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/ReduceRight
     * @since 3.0
     * @param callback  The JavaScript reduce function to call
     * @return A reduction of the mapped array
     */
    public JSValue reduceRight(JSFunction callback) {
        return property("reduceRight").toFunction().call(this,callback);
    }
    /**
     * JavaScript Array.prototype.reduceRight(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/ReduceRight
     * @since 3.0
     * @param callback  The Java reduce function to call
     * @param initialValue The initial value of the reduction
     * @return A reduction of the mapped array
     */
    public JSValue reduceRight(final JSArrayReduceCallback callback, Object initialValue) {
        return each(callback,"reduceRight",initialValue);
    }
    /**
     * JavaScript Array.prototype.reduceRight(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/ReduceRight
     * @since 3.0
     * @param callback  The Java reduce function to call
     * @return A reduction of the mapped array
     */
    public JSValue reduceRight(final JSArrayReduceCallback callback) {
        return reduceRight(callback, null);
    }

    /**
     * JavaScript Array.prototype.reverse(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/reverse
     * @since 3.0
     * @return this (mutable)
     */
    @SuppressWarnings("unchecked")
    public JSArray<T> reverse() {
        return property("reverse").toFunction().call(this).toJSArray();
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
     * JavaScript Array.prototype.slice(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/slice
     * @since 3.0
     * @param begin the index to begin slicing (inclusive)
     * @param end the index to end slicing (exclusive)
     * @return the new sliced array
     */
    @SuppressWarnings("unchecked")
    public JSArray<T> slice(int begin, int end) {
        JSArray slice = property("slice").toFunction().call(this,begin,end).toJSArray();
        slice.mType = mType;
        return slice;
    }
    /**
     * JavaScript Array.prototype.slice(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/slice
     * @since 3.0
     * @param begin the index to begin slicing (inclusive)
     * @return the new sliced array
     */
    @SuppressWarnings("unchecked")
    public JSArray<T> slice(int begin) {
        JSArray slice = property("slice").toFunction().call(this,begin).toJSArray();
        slice.mType = mType;
        return slice;
    }
    /**
     * JavaScript Array.prototype.slice(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/slice
     * @since 3.0
     * @return the new sliced array (essentially a copy of the original array)
     */
    @SuppressWarnings("unchecked")
    public JSArray<T> slice() {
        JSArray slice = property("slice").toFunction().call(this).toJSArray();
        slice.mType = mType;
        return slice;
    }

    /**
     * JavaScript: Array.prototype.some(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/some
     * @since 3.0
     * @param callback the JavaScript function to call on each element
     * @param thiz the 'this' value passed to callback
     * @return true if some element in the array meets the condition, false otherwise
     */
    public boolean some(JSFunction callback, JSObject thiz) {
        return each(callback,thiz,"some").toBoolean();
    }
    /**
     * JavaScript: Array.prototype.some(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/some
     * @since 3.0
     * @param callback the JavaScript function to call on each element
     * @return true if some element in the array meets the condition, false otherwise
     */
    public boolean some(JSFunction callback) {
        return some(callback,null);
    }
    /**
     * JavaScript: Array.prototype.some(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/some
     * @since 3.0
     * @param callback the Java function to call on each element
     * @return true if some element in the array meets the condition, false otherwise
     */
    public boolean some(final JSArrayEachBooleanCallback<T> callback) {
        return each(callback,"some").toBoolean();
    }

    /**
     * JavaScript Array.prototype.sort(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/sort
     * @since 3.0
     * @param compare the JavaScript compare function to use for sorting
     * @return this (mutable)
     */
    @SuppressWarnings("unchecked")
    public JSArray<T> sort(JSFunction compare) {
        return property("sort").toFunction().call(this,compare).toJSArray();
    }
    /**
     * JavaScript Array.prototype.sort(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/sort
     * @since 3.0
     * @param callback the Java compare function to use for sorting
     * @return this (mutable)
     */
    @SuppressWarnings("unchecked")
    public JSArray<T> sort(final JSArraySortCallback<T> callback) {
        return property("sort").toFunction().call(this,new JSFunction(context,"_callback") {
            public double _callback(T a, T b) {
                return callback.callback((T)((JSValue)a).toJavaObject(mType),
                        (T)((JSValue)b).toJavaObject(mType));
            }
        }).toJSArray();
    }
    /**
     * JavaScript Array.prototype.sort(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/sort
     * @since 3.0
     * @return this (mutable)
     */
    @SuppressWarnings("unchecked")
    public JSArray<T> sort() {
        return property("sort").toFunction().call(this).toJSArray();
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
        JSArray splice = property("splice").toFunction().apply(this,args.toArray()).toJSArray();
        splice.mType = mType;
        return splice;
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

    /**
     * An array value iterator
     * @since 3.0
     * @param <U>
     */
    public class JSArrayValuesIterator<U> extends JSIterator<U> {
        protected JSArrayValuesIterator(JSObject iterator) {
            super(iterator);
        }

        /**
         * Gets the next element of the array
         * @return the next value in the array
         */
        @Override
        @SuppressWarnings("unchecked")
        public U next() {
            Next jsnext = jsnext();
            return (U) jsnext.value().toJavaObject(mType);
        }
    }

    /**
     * JavaScript Array.prototype.values(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/values
     * @since 3.0
     * @return an array value iterator
     */
    public JSArrayValuesIterator<T> values() {
        return new JSArrayValuesIterator<T>(property("values").toFunction().call(this).toObject());
    }
}
