package org.liquidplayer.androidjscoreexample;

import org.liquidplayer.webkit.javascriptcore.JSContext;
import org.liquidplayer.webkit.javascriptcore.JSException;
import org.liquidplayer.webkit.javascriptcore.JSObject;
import org.liquidplayer.webkit.javascriptcore.JSValue;

/**
 * Created by eric on 1/5/16.
 */
public class ExceptionHandlingExample implements IExample, JSContext.IJSExceptionHandler {

    private static String naughtyFunctionCode =
            "function naughtyFunction() { \n" +
            "    var access = nothing.prop; \n" +
            "    return access; \n" +
            "} \n";

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
        ret = null;
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