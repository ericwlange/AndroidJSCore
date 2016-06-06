package org.liquidplayer.androidjscoretest;

import org.liquidplayer.webkit.javascriptcore.JSContext;
import org.liquidplayer.webkit.javascriptcore.JSContextGroup;
import org.liquidplayer.webkit.javascriptcore.JSException;
import org.liquidplayer.webkit.javascriptcore.JSObject;
import org.liquidplayer.webkit.javascriptcore.JSValue;

/**
 * Created by Eric on 5/5/16.
 */
public class JSContextTest extends JSTest {
    public JSContextTest(MainActivity activity) { super(activity); }

    public interface JSContextInterface {
        public int func1();
    }
    public class JSContextClass extends JSContext implements JSContextInterface {
        public JSContextClass() {
            super(JSContextInterface.class);
        }
        @Override
        public int func1() {
            return 55;
        }
    }
    public class JSContextInGroup extends JSContext implements JSContextInterface {
        public JSContextInGroup(JSContextGroup inGroup) {
            super(inGroup, JSContextInterface.class);
        }
        @Override
        public int func1() {
            return property("testObject").toFunction().call().toNumber().intValue();
        }
    }

    public void testJSContextConstructor() throws TestAssertException {
        println("Test JSContext()");
        JSContext context = track(new JSContext(),"testJSContextConstructor:context");
        context.property("test",10);
        tAssert(context.property("test").toNumber().equals(10.0), "Read property on context");

        println("Test JSContext(<iface>)");
        JSContext context1 = track(new JSContextClass(),"testJSContextConstructor:context1");
        JSValue ret = context1.evaluateScript("func1()");
        tAssert(ret.toNumber().equals(55.0), "Call global java function on context");

        println("Test JSContext(<JSContextGroup>)");
        JSContextGroup contextGroup = new JSContextGroup();
        JSContext context2 = track(new JSContext(contextGroup), "testJSContextConstructor:context2");
        JSContext context3 = track(new JSContext(contextGroup), "testJSContextConstructor:context3");
        context2.evaluateScript("var forty_two = 42; var cx2_func = function() { return forty_two; };");
        JSValue cx2_func = context2.property("cx2_func");
        context3.property("cx3_func", cx2_func);
        JSValue forty_two = context3.evaluateScript("cx3_func()");
        tAssert(forty_two.toNumber().equals(42.0), "Share objects in JSContextGroup");

        println("Test JSContext(<JSContextGroup>,<iface>)");
        JSContextInGroup context4 = new JSContextInGroup(contextGroup);
        track(context4,"testJSContextConstructor:context4");
        context4.property("testObject", cx2_func);
        ret = context4.evaluateScript("func1()");
        tAssert(ret.toNumber().equals(42.0), "Call global java function on JSContextGroup");

        println("Test JSContext.getGroup()");
        tAssert(context2.getGroup().equals(context3.getGroup()) &&
                context3.getGroup().equals(context4.getGroup()) &&
                context4.getGroup() != null, "Check context group equivalence");
        tAssert(!context1.getGroup().equals(context2.getGroup()),
                "Check context group different");

        context.garbageCollect();
        context1.garbageCollect();
        context2.garbageCollect();
        context3.garbageCollect();
        context4.garbageCollect();
    }

    private boolean excp;

    public void testJSContextExceptionHandler() throws TestAssertException {
        println("Test IJSContextExceptionHandler()");
        JSContext context = track(new JSContext(),"testJSContextExceptionHandler:context");
        try {
            context.property("does_not_exist").toFunction();
            tAssert(false,"Catch exception in try/catch block");
        } catch (JSException e) {
            tAssert(true,"Catch exception in try/catch block");
        }

        println("Test JSContext.setExceptionHandler()");
        excp = false;
        context.setExceptionHandler(new JSContext.IJSExceptionHandler() {
            @Override
            public void handle(JSException e) {
                println("Exception caught " + e);
                excp = !excp;
            }
        });
        try {
            context.property("does_not_exist").toFunction();
            tAssert(excp,"Catch exception in exception handler");
        } catch (JSException e) {
            tAssert(false,"Catch exception in exception handler[2]");
        }

        println("Test JSContext.clearExceptionHandler()");
        context.clearExceptionHandler();
        try {
            context.property("does_not_exist").toFunction();
            tAssert(false,"Catch exception in try/catch block");
        } catch (JSException e) {
            // excp should still be true
            tAssert(excp,"Catch exception in try/catch block");
        }

        println("Test exception inside exception handler");
        excp = false;
        final JSContext context2 = track(new JSContext(),"testJSContextExceptionHandler:context2");
        context2.setExceptionHandler(new JSContext.IJSExceptionHandler() {
            @Override
            public void handle(JSException e) {
                excp = !excp;
                // Raise another exception.  Should throw JSException
                context2.property("does_not_exist").toFunction();
            }
        });
        try {
            context2.property("does_not_exist").toFunction();
            tAssert(false,"Exception inside of exception handler[2]");
        } catch (JSException e) {
            tAssert(excp,"Exception inside of exception handler");
        }

        context.garbageCollect();
        context2.garbageCollect();
    }

    public void testJSContextEvaluateScript() throws TestAssertException {
        println("Test JSContext.evaluateScript(<script>,<this>,<url>,<line>)");
        final String script1 = "" +
            "var val1 = 1;\n" +
            "var val2 = 'foo';\n" +
            "does_not_exist(do_something);";
        String url = "http://liquidplayer.com/script1.js";

        JSContext context = track(new JSContext(),"testJSContextEvaluateScript:context");
        try {
            context.evaluateScript(script1, null, url, 1);
            tAssert(false,"evaluateScript with URL & line[2]");
        } catch (JSException e) {
            String stack = e.getError().toObject().property("stack").toString();
            String expected = "global code@" + url + ":3:";
            tAssert(stack.substring(0,expected.length()).equals(expected),
                    "evaluateScript with URL & line");
        }

        println("Test JSContext.evaluateScript(<script>,<this>)");
        context.property("localv",69);
        JSValue val = context.evaluateScript("this.localv",null);
        tAssert(val.toNumber().equals(69.0), "Check that null 'this' is global object");
        JSObject obj = new JSObject(context);
        obj.property("localv",100);
        val = context.evaluateScript("this.localv",obj);
        tAssert(val.toNumber().equals(100.0), "Check that non-null 'this' trumps global obj");

        println("Test JSContext.evaluateScript(<script>)");
        val = context.evaluateScript("this.localv");
        tAssert(val.toNumber().equals(69.0), "Check that null 'this' is default");

        context.garbageCollect();
    }

    @Override
    public void run() throws TestAssertException {
        println("**** JSContext/JSContextGroup ****");
        println("----------------------------------");
        testJSContextConstructor();
        testJSContextExceptionHandler();
        testJSContextEvaluateScript();
        println("----------------------------------");
    }
}
