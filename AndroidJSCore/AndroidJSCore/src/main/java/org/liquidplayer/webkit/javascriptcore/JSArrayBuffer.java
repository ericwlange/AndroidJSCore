//
// JSArrayBuffer.java
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

/**
 * A wrapper class for a JavaScript ArrayBuffer
 */
public class JSArrayBuffer {
    /**
     * Creates a new array buffer of 'length' bytes
     * @param ctx  the JSContext in which to create the ArrayBuffer
     * @param length  the length in bytes of the ArrayBuffer
     */
    public JSArrayBuffer(JSContext ctx, int length) {
        JSFunction constructor = new JSFunction(ctx,"_ArrayBuffer",new String[] {"length"},
                "return new ArrayBuffer(length);",
                null, 0);
        mArrayBuffer = constructor.call(null,length).toObject();
    }

    /**
     * Treats an existing JSObject as an ArrayBuffer.  It is up to the user to ensure the
     * underlying JSObject is actually an ArrayBuffer.
     * @param buffer  The ArrayBuffer JSObject to wrap
     */
    public JSArrayBuffer(JSObject buffer) {
        mArrayBuffer = buffer;
    }
    private final JSObject mArrayBuffer;

    /**
     * Gets underlying JSObject
     * @return JSObject representing the ArrayBuffer
     */
    public JSObject getJSObject() {
        return mArrayBuffer;
    }

    /**
     * JavaScript: ArrayBuffer.prototype.byteLength, see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/ArrayBuffer/byteLength
     * @return length of ArrayBuffer in bytes
     */
    public int byteLength() {
        return mArrayBuffer.property("byteLength").toNumber().intValue();
    }

    /**
     * JavaScript: ArrayBuffer.isView(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/ArrayBuffer/isView
     * @param arg the argument to be checked
     * @return true if arg is one of the ArrayBuffer views, such as typed array objects or
     * a DataView; false otherwise
     */
    public static boolean isView(JSValue arg) {
        return arg.getContext().property("ArrayBuffer").toObject().property("isView").toFunction()
                .call(null,arg).toBoolean();
    }

    /**
     * JavaScript: ArrayBuffer.transfer(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/ArrayBuffer/transfer
     * @param oldBuffer  An ArrayBuffer object from which to transfer from
     * @param newByteLength  The byte length of the new ArrayBuffer object
     * @return a new ArrayBuffer
     */
    public static JSArrayBuffer transfer(JSArrayBuffer oldBuffer, int newByteLength) {
        return new JSArrayBuffer(oldBuffer.getJSObject().getContext().property("ArrayBuffer").toObject()
                .property("transfer").toFunction().call(null,oldBuffer,newByteLength).toObject());
    }
    /**
     * JavaScript: ArrayBuffer.transfer(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/ArrayBuffer/transfer
     * @param oldBuffer  An ArrayBuffer object from which to transfer from
     * @return a new ArrayBuffer
     */
    public static JSArrayBuffer transfer(JSArrayBuffer oldBuffer) {
        return new JSArrayBuffer(oldBuffer.getJSObject().getContext().property("ArrayBuffer").toObject()
                .property("transfer").toFunction().call(null,oldBuffer).toObject());
    }

    /**
     * JavaScript: ArrayBuffer.prototype.slice(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/ArrayBuffer/slice
     * @param begin Zero-based byte index at which to begin slicing
     * @param end Byte index to end slicing
     * @return new ArrayBuffer with sliced contents copied
     */
    public JSArrayBuffer slice(int begin, int end) {
        return new JSArrayBuffer(
                mArrayBuffer.property("slice").toFunction().call(null,begin,end).toObject());
    }
    /**
     * JavaScript: ArrayBuffer.prototype.slice(), see:
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/ArrayBuffer/slice
     * @param begin Zero-based byte index at which to begin slicing
     * @return new ArrayBuffer with sliced contents copied
     */
    public JSArrayBuffer slice(int begin) {
        return new JSArrayBuffer(
                mArrayBuffer.property("slice").toFunction().call(null,begin).toObject());
    }
}
