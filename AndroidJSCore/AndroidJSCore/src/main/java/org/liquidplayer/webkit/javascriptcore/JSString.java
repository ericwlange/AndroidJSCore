//
// JSString.java
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
 *  A JavaScript string 
 * @since 1.0
 */
public class JSString {

    private static final JSWorkerQueue workerQueue = new JSWorkerQueue(new Runnable() {
		@Override
		public void run() {

		}
	});

    private class JNIReturnClass implements Runnable {
        @Override
        public void run() {}
        JNIReturnObject jni;
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
	public boolean equals(Object other) {
		if (other == null) return false;
		if (other == this) return true;
		JSString otherJSString;
		if (other instanceof JSString) { 
			otherJSString = (JSString)other;
		} else if (other instanceof String) {
			String string = (String)other;
            otherJSString = new JSString(string);
		} else if (other instanceof JSValue) {
			JSValue v = (JSValue)other;
			if (v.isString()) {
				try {
					otherJSString = v.toJSString();
				} catch (JSException e) {
					return false;
				}
			} else {
				return false;
			}
		} else {
			return false;
		}
        final long foo = otherJSString.stringRef;
        JNIReturnClass payload = new JNIReturnClass() {
            @Override
            public void run() {
                jni = new JNIReturnObject();
                jni.bool = isEqual(stringRef, foo);
            }
        };
        workerQueue.sync(payload);
		return payload.jni.bool;
	}
	@Override
	public String toString() {
        JNIReturnClass payload = new JNIReturnClass() {
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
	
	/**
	 * Gets the length of the string in characters
	 * @return  the number of characters in the string
	 * @since 1.0
	 */
	public Integer length() {
        JNIReturnClass payload = new JNIReturnClass() {
            @Override
            public void run() {
                jni = new JNIReturnObject();
                jni.reference = getLength(stringRef);
            }
        };
        workerQueue.sync(payload);
        return Long.valueOf(payload.jni.reference).intValue();
	}
	
	protected native long createWithCharacters(String str);
	protected native long retain(long strRef);
	protected native void release(long stringRef);
	protected native int getLength(long stringRef);
	protected native boolean isEqual(long a, long b);
	protected native String toString(long strRef);

	@SuppressWarnings("unused")
	protected native long createWithUTF8CString(String str);
	@SuppressWarnings("unused")
	protected native int getMaximumUTF8CStringSize(long stringRef);
	@SuppressWarnings("unused")
	protected native boolean isEqualToUTF8CString(long a, String b);
}
