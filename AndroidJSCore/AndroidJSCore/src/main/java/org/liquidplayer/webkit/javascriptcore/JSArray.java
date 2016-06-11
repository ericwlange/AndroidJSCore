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
    private T dummy = null;

	/**
	 * Creates a JavaScript array object, initialized with 'array' JSValues
	 * @param ctx  The JSContext to create the array in
	 * @param array  An array of JSValues with which to initialize the JavaScript array object
	 * @since 1.0
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
			context.throwJSException(new JSException(new JSValue(jni.exception, context)));
            jni.reference = make(context.ctxRef(), 0L);
		}
		valueRef = jni.reference;
        context.persistObject(this);
	}
	/**
	 * Creates an empty JavaScript array object
	 * @param ctx  The JSContext to create the array in
	 * @since 1.0
	 * @throws JSException
	 */
	public JSArray(JSContext ctx) throws JSException {
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
	 * @since 1.0
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
     * @param list The List of Java object T with which to initialize the JavaScript array object.  Each
     *             object will be converted to a JSValue
     * @since 3.0
     * @throws JSException
     */
    public JSArray(JSContext ctx, List<T> list) throws JSException {
        this(ctx,list.toArray());
    }

	/**
	 * Wraps an existing JavaScript object and treats it as an array.
	 * @param objRef  The JavaScriptCore reference to the object
	 * @param ctx  The JSContext in which the array exists
	 * @since 1.0
	 * @throws JSException
	 */
	public JSArray(long objRef, JSContext ctx) throws JSException {
		super(objRef,ctx);
	}

	public Object[] toArray(Class clazz) throws JSException {
		int count = property("length").toNumber().intValue();

		Object [] array = (Object[]) Array.newInstance(clazz,count);
        for (int i=0; i<count; i++) {
			array[i] = propertyAtIndex(i).toJavaObject(clazz);
		}
		return array;
	}

	/**
	 * Extracts Java JSValue array from JavaScript array
	 * @return JavaScript array as Java array of JSValues
	 * @throws JSException
	 */
    @Override @NonNull
	public Object [] toArray() throws JSException {
		return toArray(JSValue.class);
	}
	
	/**
	 * Gets JSValue at 'index'
	 * @param index  Index of the element to get
	 * @return  The JSValue at index 'index'
	 * @since 1.0
	 * @throws JSException
	 */
    @Override
    @SuppressWarnings("unchecked")
	public T get(final int index) {
		int count = property("length").toNumber().intValue();
		if (index >= count) {
			throw new ArrayIndexOutOfBoundsException();
		}
		return (T) dummy.getClass().cast(propertyAtIndex(index));
	}

	/**
	 * Replaces a JSValue at array index 'index'.  The Java Object is converted to a JSValue.
	 * @param index  The index of the object to replace
	 * @param val  The Java object of the new value to set in the array
	 * @since 1.0
	 * @throws JSException
	 */
	public JSValue replace(int index, Object val) throws JSException {
		int count = property("length").toNumber().intValue();
		if (index >= count) {
			throw new ArrayIndexOutOfBoundsException();
		}
        JSValue oldValue = propertyAtIndex(index);
		propertyAtIndex(index,val);
        return oldValue;
	}
	
	/**
	 * Adds a JSValue to the end of an array.  The Java Object is converted to a JSValue.
	 * @param val  The Java object to add to the array, will get converted to a JSValue
	 * @since 1.0
	 * @throws JSException
	 */
    @Override
	public boolean add(final T val) throws JSException {
		int count = property("length").toNumber().intValue();
		JSValue newVal = new JSValue(context,val);
		propertyAtIndex(count,newVal);
        return true;
	}

	/**
	 * Removes the value at index 'index'
	 * @param index  The index of the value to remove
	 * @since 1.0
	 * @throws JSException
	 */
	public JSValue removeItemAtIndex(final int index) throws JSException {
		int count = property("length").toNumber().intValue();
		if (index >= count) {
			throw new ArrayIndexOutOfBoundsException();
		}
        JSValue oldValue = propertyAtIndex(index);
		for (int i=index+1; i<count; i++) {
			propertyAtIndex(i-1,propertyAtIndex(i));
		}
		property("length",count-1);
        return oldValue;
	}
	
	/**
	 * Inserts new object 'val' at index 'index'
	 * @param index  Index at which to insert value
	 * @param val  Java object to insert, will get converted to JSValue
	 * @since 1.0
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
	 * @since 1.0
	 * @throws JSException
	 */
	public int length() throws JSException {
		return property("length").toNumber().intValue();
	}

    @Override
    public int size() {
        return length();
    }

    @Override
    public boolean isEmpty() {
        return (size() == 0);
    }

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

        public ArrayIterator() {
            this(0);
        }
        public ArrayIterator(int index) {
            if (index >= size()) index = size() - 1;
            if (index < 0) index = 0;
            current = index;
        }

        @Override
        public boolean hasNext() {
            return (current <= length());
        }

        @Override
        public boolean hasPrevious() {
            return (current > 0);
        }

        @Override
        public T next() {
            if (!hasNext())
                throw new NoSuchElementException();
            return get(current++);
        }

        @Override
        public T previous() {
            if (!hasPrevious())
                throw new NoSuchElementException();
            return get(--current);
        }

        @Override
        public void remove() {
            JSArray.this.removeItemAtIndex(current);
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
            JSArray.this.set(current,value);
        }

        @Override
        public void add(T value) {
            JSArray.this.add(value);
        }
    }

    @Override
    public @NonNull Iterator<T> iterator() {
        return new ArrayIterator();
    }

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

    @Override
    public boolean containsAll(final @NonNull Collection<?> collection) {
        for (Object item : collection.toArray()) {
            if (!contains(item)) return false;
        }
        return true;
    }

    @Override
    public boolean addAll(final @NonNull  Collection<? extends T> collection) {
        return addAll(size(), collection);
    }

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

    @Override
    public boolean removeAll(final @NonNull Collection<?> collection) {
        if (collection.isEmpty()) {
            return false;
        }
        boolean any = false;
        for (Object item : collection.toArray()) {
            any = remove(item) || any;
        }
        return any;
    }

    @Override
    public boolean retainAll(final @NonNull Collection<?> collection) {
        if (collection.isEmpty()) {
            return false;
        }
        boolean any = false;
        for (int i=length(); i > 0; --i) {
            if (!collection.contains(get(i))) {
                removeItemAtIndex(i);
                any = true;
            }
        }
        return any;
    }

    @Override
    public void clear() {
        if (isEmpty()) {
            return;
        }
        for (int i=length(); i > 0; --i) {
            removeItemAtIndex(i);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public T set(final int index, final T element) {
        if (this == element) {
            throw new IllegalArgumentException();
        }
        JSValue old = replace(index, element);
        return (T) old.toJavaObject(dummy.getClass());
    }

    @Override
    public void add(final int index, final T element) {
        if (this == element) {
            throw new IllegalArgumentException();
        }
        insert(index, element);
    }

    @Override
    @SuppressWarnings("unchecked")
    public T remove(final int index) {
        JSValue value = removeItemAtIndex(index);
        return (T) value.toJavaObject(dummy.getClass());
    }

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

    @Override
    public ListIterator<T> listIterator() {
        return listIterator(0);
    }

    @Override @NonNull
    public ListIterator<T> listIterator(final int index) {
        return new ArrayIterator(index);
    }

    @Override @NonNull
    @SuppressWarnings("unchecked")
    public List<T> subList(final int fromIndex, final int toIndex) {
        if (fromIndex < 0 || toIndex > size() || fromIndex > toIndex) {
            throw new IndexOutOfBoundsException();
        }
        JSArray subArray = new JSArray(context);
        for (int index = fromIndex; index < toIndex; index++) {
            subArray.add(get(index));
        }
        return subArray;
    }

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
            if (next.equals(otherNext)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }
}
