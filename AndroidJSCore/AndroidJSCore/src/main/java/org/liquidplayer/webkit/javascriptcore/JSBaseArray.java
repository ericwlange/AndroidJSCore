//
// JSBaseArray.java
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
public abstract class JSBaseArray<T> extends JSFunction implements List<T> {

    protected Class<T> mType;
    protected int mLeftBuffer = 0;
    protected int mRightBuffer = 0;
    protected JSBaseArray<T> mSuperList = null;

    protected JSBaseArray(long valueRef, JSContext ctx, Class<T> cls) {
        super(valueRef,ctx);
        mType = cls;
    }
    protected JSBaseArray(JSBaseArray<T> superList, int leftBuffer, int rightBuffer, Class<T> cls) {
        mType = cls;
        mLeftBuffer = leftBuffer;
        mRightBuffer = rightBuffer;
        context = superList.context;
        valueRef = superList.valueRef();
        mSuperList = superList;
    }
    protected JSBaseArray(JSContext ctx, Class<T> cls) {
        context = ctx;
        mType = cls;
    }


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
            array[i] = elementAtIndex(i).toJavaObject(clazz);
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
        return (T) elementAtIndex(index).toJavaObject(mType);
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
        elementAtIndex(count,val);
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

    protected JSValue arrayElement(final int index) {
        return propertyAtIndex(index);
    }

    protected void arrayElement(final int index, final T value) {
        propertyAtIndex(index,value);
    }

    protected JSValue elementAtIndex(final int index) {
        if (mSuperList == null)
            return arrayElement(index);
        else
            return mSuperList.elementAtIndex(index + mLeftBuffer);
    }

    protected void elementAtIndex(final int index, final T value) {
        if (mSuperList == null)
            arrayElement(index, value);
        else
            mSuperList.elementAtIndex(index + mLeftBuffer, value);
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

            JSBaseArray.this.remove(modifiable.intValue());
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

            JSBaseArray.this.set(modifiable,value);
        }

        @Override
        public void add(T value) {
            JSBaseArray.this.add(current++,value);
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
    public <U> U[] toArray(final @NonNull U[] elemArray) {
        if (size() > elemArray.length) {
            return (U[])toArray();
        }
        ArrayIterator iterator = new ArrayIterator();
        int index = 0;
        while (iterator.hasNext()) {
            Object next = iterator.next();
            elemArray[index++] = (U)next;
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
        JSValue oldValue = elementAtIndex(index);
        elementAtIndex(index,element);
        return (T) oldValue.toJavaObject(mType);
    }

    /**
     * @see List#add(int, Object)
     * @since 3.0
     */
    @Override
    @SuppressWarnings("unchecked")
    public void add(final int index, final T element) {
        throw new UnsupportedOperationException();
    }

    /**
     * @see List#remove(int)
     * @since 3.0
     */
    @Override
    @SuppressWarnings("unchecked")
    public T remove(final int index) {
        throw new UnsupportedOperationException();
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
        throw new UnsupportedOperationException();
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
        for (T e : this) {
            hashCode = 31 * hashCode + (e == null ? 0 : e.hashCode());
        }
        return hashCode;
    }

    /** JavaScript methods **/

    /**
     * Interface containing a condition test callback function
     * @since 3.0
     * @param <T>
     */
    public interface JSBaseArrayEachBooleanCallback<T> {
        /**
         * A function to test each element of an array for a condition
         * @param currentValue value to test
         * @param index index in 'array'
         * @param array array being traversed
         * @since 3.0
         * @return true if condition is met, false otherwise
         */
        boolean callback(T currentValue, int index, JSBaseArray<T> array);
    }

    /**
     * Interface containing a function to call on each element of an array
     * @since 3.0
     * @param <T>
     */
    public interface JSBaseArrayForEachCallback<T> {
        /**
         * A function to call on each element of the array
         * @param currentValue current value in the array
         * @param index index in 'array'
         * @param array array being traversed
         * @since 3.0
         */
        void callback(T currentValue, int index, JSBaseArray<T> array);
    }

    /**
     * Interface containing a map function
     * @since 3.0
     * @param <T>
     */
    public interface JSBaseArrayMapCallback<T> {
        /**
         * A function to map an array value to a new JSValue
         * @param currentValue value to map
         * @param index index in 'array'
         * @param array array being traversed
         * @since 3.0
         * @return mapped value
         */
        JSValue callback(T currentValue, int index, JSBaseArray<T> array);
    }

    /**
     * Interface containing a reduce function
     * @since 3.0
     */
    public interface JSBaseArrayReduceCallback {
        /**
         * A function to reduce a mapped value into an accumulator
         * @param previousValue previous value of the accumulator
         * @param currentValue value of mapped item
         * @param index index in 'array'
         * @param array map array being traversed
         * @since 3.0
         * @return new accumulator value
         */
        JSValue callback(JSValue previousValue, JSValue currentValue, int index,
                                JSBaseArray<JSValue> array);
    }

    /**
     * Interface containing a compare function callback for sort
     * @since 3.0
     * @param <T>
     */
    public interface JSBaseArraySortCallback<T> {
        /**
         * A function for comparing values in a sort
         * @param a first value
         * @param b second value
         * @since 3.0
         * @return 0 if values are the same, negative if 'b' comes before 'a', and positive if 'a' comes
         *         before 'b'
         */
        double callback(T a, T b);
    }

    private JSValue each(JSFunction callback, JSObject thiz, String each) {
        return property(each).toFunction().call(this,callback,thiz);
    }
    private JSValue each(final JSBaseArrayEachBooleanCallback<T> callback, String each) {
        return property(each).toFunction().call(this,new JSFunction(context,"_callback") {
            @SuppressWarnings("unchecked,unused")
            public boolean _callback(T currentValue, int index, JSBaseArray array) {
                return callback.callback((T)((JSValue)currentValue).toJavaObject(mType),index,array);
            }
        });
    }
    private JSValue each(final JSBaseArrayForEachCallback<T> callback, String each) {
        return property(each).toFunction().call(this,new JSFunction(context,"_callback") {
            @SuppressWarnings("unchecked,unused")
            public void _callback(T currentValue, int index, JSBaseArray array) {
                callback.callback((T)((JSValue)currentValue).toJavaObject(mType),index,array);
            }
        });
    }
    private JSValue each(final JSBaseArrayMapCallback<T> callback, String each) {
        return property(each).toFunction().call(this,new JSFunction(context,"_callback") {
            @SuppressWarnings("unchecked,unused")
            public JSValue _callback(T currentValue, int index, JSBaseArray<T> array) {
                return callback.callback((T)((JSValue)currentValue).toJavaObject(mType),index,array);
            }
        });
    }
    private JSValue each(final JSBaseArrayReduceCallback callback, String each, Object initialValue) {
        return property(each).toFunction().call(this,new JSFunction(context,"_callback") {
            @SuppressWarnings("unused")
            public JSValue _callback(JSValue previousValue, JSValue currentValue, int index,
                                     JSBaseArray<JSValue> array) {
                return callback.callback(previousValue,currentValue,index,array);
            }
        },initialValue);
    }

    protected JSBaseArray toSuperArray(JSValue array) {
        return null;
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
    public JSBaseArray<T> copyWithin(int target, int start, int end) {
        return toSuperArray(property("copyWithin").toFunction().call(this,target,start,end));
    }
    /**
     * JavaScript Array.prototype.copyWithin(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/copyWithin
     * @since 3.0
     * @param target index to copy sequence to
     * @param start  index from which to start copying from
     * @return this (mutable operation)
     */
    public JSBaseArray<T> copyWithin(int target, int start) {
        return copyWithin(target,start,size());
    }
    /**
     * JavaScript Array.prototype.copyWithin(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/copyWithin
     * @since 3.0
     * @param target index to copy sequence to
     * @return this (mutable operation)
     */
    public JSBaseArray<T> copyWithin(int target) {
        return copyWithin(target,0);
    }

    /**
     * An array entry Iterator
     * @since 3.0
     * @param <U>
     */
    public class JSBaseArrayEntriesIterator<U> extends JSIterator<Map.Entry<Integer,U>> {
        protected JSBaseArrayEntriesIterator(JSObject iterator) {
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
            return new AbstractMap.SimpleEntry<>(next.propertyAtIndex(0).toNumber().intValue(),
                    (U) next.propertyAtIndex(1).toJavaObject(mType));
        }
    }

    /**
     * JavaScript Array.prototype.entries(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/entries
     * @since 3.0
     * @return an entry iterator
     */
    public JSBaseArrayEntriesIterator<T> entries() {
        return new JSBaseArrayEntriesIterator<>(property("entries").toFunction().call(this).toObject());
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
    public boolean every(final JSBaseArrayEachBooleanCallback<T> callback) {
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
    public JSBaseArray<T> fill(T value, int start, int end) {
        return toSuperArray(property("fill").toFunction().call(this,value,start,end));
    }
    /**
     * JavaScript Array.prototype.fill(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/fill
     * @since 3.0
     * @param value the value to fill
     * @param start the index to start filling
     * @return this (mutable)
     */
    public JSBaseArray<T> fill(T value, int start) {
        return fill(value,start,size());
    }
    /**
     * JavaScript Array.prototype.fill(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/fill
     * @since 3.0
     * @param value the value to fill
     * @return this (mutable)
     */
    public JSBaseArray<T> fill(T value) {
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
    public JSBaseArray<T> filter(JSFunction callback, JSObject thiz) {
        JSBaseArray filter = toSuperArray(each(callback,thiz,"filter"));
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
    public JSBaseArray<T> filter(JSFunction callback) {
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
    public JSBaseArray<T> filter(final JSBaseArrayEachBooleanCallback<T> callback) {
        JSBaseArray filter = toSuperArray(each(callback,"filter"));
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
    public T find(final JSBaseArrayEachBooleanCallback<T> callback) {
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
    public int findIndex(final JSBaseArrayEachBooleanCallback<T> callback) {
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
    public void forEach(final JSBaseArrayForEachCallback<T> callback) {
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
    public class JSBaseArrayKeysIterator extends JSIterator<Integer> {
        protected JSBaseArrayKeysIterator(JSObject iterator) {
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
    public JSBaseArrayKeysIterator keys() {
        return new JSBaseArrayKeysIterator(property("keys").toFunction().call(this).toObject());
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
    public JSBaseArray<JSValue> map(JSFunction callback, JSObject thiz) {
        return toSuperArray(each(callback,thiz,"map"));
    }
    /**
     * JavaScript Array.prototype.map(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/map
     * @since 3.0
     * @param callback the JavaScript function to call on each element
     * @return a new mapped array
     */
    public JSBaseArray<JSValue> map(JSFunction callback) {
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
    public JSBaseArray<JSValue> map(final JSBaseArrayMapCallback<T> callback) {
        return toSuperArray(each(callback,"map").toJSArray());
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
    public JSValue reduce(final JSBaseArrayReduceCallback callback, Object initialValue) {
        return each(callback,"reduce",initialValue);
    }
    /**
     * JavaScript Array.prototype.reduce(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/Reduce
     * @since 3.0
     * @param callback  The Java reduce function to call
     * @return A reduction of the mapped array
     */
    public JSValue reduce(final JSBaseArrayReduceCallback callback) {
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
    public JSValue reduceRight(JSFunction callback, Object initialValue) {
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
    public JSValue reduceRight(final JSBaseArrayReduceCallback callback, Object initialValue) {
        return each(callback,"reduceRight",initialValue);
    }
    /**
     * JavaScript Array.prototype.reduceRight(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/ReduceRight
     * @since 3.0
     * @param callback  The Java reduce function to call
     * @return A reduction of the mapped array
     */
    public JSValue reduceRight(final JSBaseArrayReduceCallback callback) {
        return reduceRight(callback, null);
    }

    /**
     * JavaScript Array.prototype.reverse(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/reverse
     * @since 3.0
     * @return this (mutable)
     */
    @SuppressWarnings("unchecked")
    public JSBaseArray<T> reverse() {
        return toSuperArray(property("reverse").toFunction().call(this));
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
    public JSBaseArray<T> slice(int begin, int end) {
        JSBaseArray slice = toSuperArray(property("slice").toFunction().call(this,begin,end));
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
    public JSBaseArray<T> slice(int begin) {
        JSBaseArray slice = toSuperArray(property("slice").toFunction().call(this,begin));
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
    public JSBaseArray<T> slice() {
        JSBaseArray slice = toSuperArray(property("slice").toFunction().call(this));
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
    public boolean some(final JSBaseArrayEachBooleanCallback<T> callback) {
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
    public JSBaseArray<T> sort(JSFunction compare) {
        return toSuperArray(property("sort").toFunction().call(this,compare));
    }
    /**
     * JavaScript Array.prototype.sort(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/sort
     * @since 3.0
     * @param callback the Java compare function to use for sorting
     * @return this (mutable)
     */
    @SuppressWarnings("unchecked")
    public JSBaseArray<T> sort(final JSBaseArraySortCallback<T> callback) {
        return toSuperArray(property("sort").toFunction().call(this,new JSFunction(context,"_callback") {
            @SuppressWarnings("unused")
            public double _callback(T a, T b) {
                return callback.callback((T)((JSValue)a).toJavaObject(mType),
                        (T)((JSValue)b).toJavaObject(mType));
            }
        }));
    }
    /**
     * JavaScript Array.prototype.sort(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/sort
     * @since 3.0
     * @return this (mutable)
     */
    @SuppressWarnings("unchecked")
    public JSBaseArray<T> sort() {
        return toSuperArray(property("sort").toFunction().call(this));
    }

    /**
     * An array value iterator
     * @since 3.0
     * @param <U>
     */
    public class JSBaseArrayValuesIterator<U> extends JSIterator<U> {
        protected JSBaseArrayValuesIterator(JSObject iterator) {
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
    public JSBaseArrayValuesIterator<T> values() {
        return new JSBaseArrayValuesIterator<>(property("values").toFunction().call(this).toObject());
    }
}
