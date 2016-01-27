//
// JSString.java
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
 *  A JavaScript string 
 * @since 1.0
 */
public class JSString {

	protected final Long stringRef;
	
	/**
	 * Creates a JavaScript string from a Java string
	 * @param s  The Java string with which to initialize the JavaScript string
	 * @since 1.0
	 */
	public JSString(String s) {
		stringRef = createWithUTF8CString(s);
	}
	/**
	 * Wraps an existing JavaScript string
	 * @param stringRef  The JavaScriptCore reference to the string
	 */
	public JSString(Long stringRef) {
		this.stringRef = retain(stringRef);
	}
	@Override
	protected void finalize() throws Throwable {
		if (stringRef==null) return;
		if (stringRef!=null && stringRef!=0) release(stringRef);
		super.finalize();
	}
	@Override
	public boolean equals(Object other) {
		if (other == null) return false;
		if (other == this) return true;
		JSString otherJSString;
		if (other instanceof JSString) { 
			otherJSString = (JSString)other;
		} else if (other instanceof String) {
			return isEqualToUTF8CString(stringRef, (String)other);
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
		
		return isEqual(stringRef, otherJSString.stringRef);
	}
	@Override
	public String toString() {
		return toString(stringRef);
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
		return getLength(stringRef);
	}
	
	protected native long createWithCharacters(String str);
	protected native long createWithUTF8CString(String str);
	protected native long retain(long strRef);
	protected native void release(long stringRef);
	protected native int getLength(long stringRef);
	protected native int getMaximumUTF8CStringSize(long stringRef);
	protected native boolean isEqual(long a, long b);
	protected native boolean isEqualToUTF8CString(long a, String b);
	protected native String toString(long strRef);
}
