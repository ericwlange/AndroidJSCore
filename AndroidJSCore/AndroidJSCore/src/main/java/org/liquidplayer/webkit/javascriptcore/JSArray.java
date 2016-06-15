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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
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
     * @param list The Collection of Java object T with which to initialize the JavaScript array object.  Each
     *             object will be converted to a JSValue
     * @param cls  The class of the component objects
     * @since 3.0
     * @throws JSException
     */
    public JSArray(JSContext ctx, Collection<T> list, Class<T> cls) throws JSException {
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

    private final Class<T> mType;
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
    public void add(final int index, final T element) {
        if (this == element) {
            throw new IllegalArgumentException();
        }
        int count = size();
        if (index > count) {
            throw new ArrayIndexOutOfBoundsException();
        }
        propertyAtIndex(count,new JSValue(context));
        for (int i=count; i>index; --i) {
            propertyAtIndex(i,propertyAtIndex(i-1));
        }
        propertyAtIndex(index,element);
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
            JSValue oldValue = propertyAtIndex(index);
            for (int i = index + 1; i < count; i++) {
                propertyAtIndex(i - 1, propertyAtIndex(i));
            }
            property("length", count - 1, JSObject.JSPropertyAttributeDontEnum);
            return (T) oldValue.toJavaObject(mType);
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



}
