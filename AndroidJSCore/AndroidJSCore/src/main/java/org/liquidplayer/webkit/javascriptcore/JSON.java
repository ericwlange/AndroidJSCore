//
// JSON.java
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
 * A convenience class for creating JavaScript values from JSON 
 * @since 1.0
 */
public class JSON extends JSValue {
	/**
	 * Creates a new JavaScript value from a JSString JSON string
	 * @param ctx  The context in which to create the value
	 * @param str  The string containing the JSON
     * @since 1.0
     */
	public JSON(JSContext ctx, JSString str) {
		context = ctx;
		valueRef = this.makeFromJSONString(context.ctxRef(), str.stringRef());
		protect(ctx,valueRef);
	}
	/**
	 * Creates a new JavaScript value from a Java JSON string
	 * @param ctx  The context in which to create the value
	 * @param str  The string containing the JSON
     * @since 1.0
     */
	public JSON(JSContext ctx, String str) {
		context = ctx;
		valueRef = this.makeFromJSONString(context.ctxRef(), new JSString(str).stringRef());
		protect(ctx,valueRef);
	}	
}
