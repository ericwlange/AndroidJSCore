//
// ExceptionHandlingExample.java
// AndroidJSCore project
//
// https://github.com/ericwlange/AndroidJSCore/
//
// Created by Eric Lange
//
/*
 Copyright (c) 2016 Eric Lange. All rights reserved.

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
package org.liquidplayer.androidjscoreexample;

import org.liquidplayer.webkit.javascriptcore.JSContext;
import org.liquidplayer.webkit.javascriptcore.JSException;
import org.liquidplayer.webkit.javascriptcore.JSValue;

public class ExceptionHandlingExample implements IExample, JSContext.IJSExceptionHandler {

    public ExceptionHandlingExample(ExampleContext ctx) {
        context = ctx;
    }
    private final ExampleContext context;

    @Override
    public void handle(JSException exception) {
        context.log("Caught error in context's exception handler");
        context.log(exception.toString());
    }

    @Override
    public void run() throws JSException {
        final String naughtyFunctionCode =
                "function naughtyFunction() { \n" +
                        "    var access = nothing.prop; \n" +
                        "    return access; \n" +
                        "} \n";

        context.clear();
        context.clearExceptionHandler();

        context.evaluateScript(naughtyFunctionCode, null, "source.js", 1);

        context.log("Handle exception with JavaScript try/catch block");
        context.log("------------------------------------------------");
        JSValue ret = context.evaluateScript(
                "try { naughtyFunction(); } catch(e) { log('Caught error in JS catch block'); log(e); }");
        context.log("return value should be undefined: " + ((ret==null)?"NULL":ret.toString()));
        context.log("");

        context.log("Handle exception with Java try/catch block");
        context.log("------------------------------------------");
        ret = null;
        try {
            ret = context.evaluateScript("naughtyFunction()");
            context.log("We really shouldn't get here");
        } catch (JSException e) {
            context.log("Caught error in Java catch block");
            context.log(e.toString());
        }
        context.log("return value should be unset (NULL): " + ((ret==null)?"NULL":ret.toString()));
        context.log("");

        context.log("Handle exception with context.setExceptionHandler()");
        context.log("---------------------------------------------------");
        context.setExceptionHandler(this);
        ret = context.evaluateScript("naughtyFunction()");
        context.log("return value should be undefined (not NULL): " + ((ret==null)?"NULL":ret.toString()));
        context.log("");

        context.log("Ignore Java try/catch block");
        context.log("---------------------------");
        ret = null;
        try {
            ret = context.evaluateScript("naughtyFunction()");
            context.log("Now we really SHOULD get here");
        } catch (JSException e) {
            context.log("And we shouldn't get here");
            context.log(e.toString());
        }
        context.log("return value should be undefined (not NULL): " + ((ret==null)?"NULL":ret.toString()));
        context.log("");

        context.clearExceptionHandler();
    }
}