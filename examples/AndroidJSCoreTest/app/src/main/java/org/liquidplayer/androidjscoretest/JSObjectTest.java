package org.liquidplayer.androidjscoretest;

import org.liquidplayer.webkit.javascriptcore.JSArray;
import org.liquidplayer.webkit.javascriptcore.JSContext;
import org.liquidplayer.webkit.javascriptcore.JSException;
import org.liquidplayer.webkit.javascriptcore.JSObject;
import org.liquidplayer.webkit.javascriptcore.JSValue;

import java.util.ArrayList;

/**
 * Created by Eric on 5/13/16.
 */
public class JSObjectTest extends JSTest {
    JSObjectTest(MainActivity activity) {
        super(activity);
    }

    public interface IFunctionObject {
        public void voidFunc();
        public JSValue jsvalueFunc();
        public JSObject jsobjectFunc();
        public Integer intFunc();
        public int intFunc2();
        public Long longFunc();
        public long longFunc2();
        public Float floatFunc();
        public float floatFunc2();
        public Double doubleFunc();
        public double doubleFunc2();
        public String stringFunc();
        public Boolean booleanFunc();
        public Integer[] arrayFunc();
    }
    public static class FunctionObject extends JSObject implements IFunctionObject {
        private final JSContext context;
        public FunctionObject(JSContext ctx) {
            super(ctx, IFunctionObject.class);
            context = ctx;
        }
        public FunctionObject(JSContext ctx, Class<?> iface, Class<? extends JSObject> constructor) {
            super(ctx, iface, constructor);
            context = ctx;
        }
        public FunctionObject(long objRef, JSContext ctx) {
            super(objRef, ctx);
            context = ctx;
        }

        @Override
        public void voidFunc() {

        }
        @Override
        public JSValue jsvalueFunc() {
            return new JSValue(context);
        }
        @Override
        public JSObject jsobjectFunc() {
            return new JSObject(context);
        }
        @Override
        public Integer intFunc() {
            return 5;
        }
        @Override
        public Long longFunc() {
            return 6L;
        }
        @Override
        public Float floatFunc() {
            return 7.6f;
        }
        @Override
        public Double doubleFunc() {
            return 8.8;
        }
        @Override
        public String stringFunc() {
            return "string";
        }
        @Override
        public Boolean booleanFunc() {
            return true;
        }
        @Override
        public int intFunc2() {
            return 9;
        }
        @Override
        public long longFunc2() {
            return 10L;
        }
        @Override
        public float floatFunc2() {
            return 17.6f;
        }
        @Override
        public double doubleFunc2() {
            return 18.8;
        }
        @Override
        public Integer[] arrayFunc() {
            return new Integer[] {5,6,7,8};
        }
    }

    public interface IConstructorObject extends IFunctionObject {
        public void _IConstructorObject(Integer val);
        public Integer myValue();
    }

    public static class ConstructorObject extends FunctionObject implements IConstructorObject {
        public ConstructorObject(JSContext ctx) {
            super(ctx, IConstructorObject.class, ConstructorObject.class);
        }
        public ConstructorObject(long objRef, JSContext ctx) {
            super(objRef, ctx);
        }
        @Override
        public void _IConstructorObject(Integer val) {
            property("value", val);
        }
        @Override
        public Integer myValue() {
            return property("value").toNumber().intValue();
        }
    }

    private final String script =
            "var empty = {}; \n" +
                    "var functionObject = {\n" +
                    "   voidFunc:    function() {}, \n" +
                    "   jsvalueFunc: function() { var undef; return undef; }, \n" +
                    "   jsobjectFunc:function() { return {}; }, \n" +
                    "   intFunc:     function() { return 5; }, \n" +
                    "   intFunc2:    function() { return 9; }, \n" +
                    "   longFunc:    function() { return 6; }, \n" +
                    "   longFunc2:   function() { return 10; }, \n" +
                    "   floatFunc:   function() { return 7.6; }, \n" +
                    "   floatFunc2:  function() { return 17.6; }, \n" +
                    "   doubleFunc:  function() { return 8.8; }, \n" +
                    "   doubleFunc2: function() { return 18.8; }, \n" +
                    "   stringFunc:  function() { return 'string'; }, \n" +
                    "   arrayFunc:   function() { return [5,6,7,8]; }, \n" +
                    "   booleanFunc: function() { return true; } \n" +
                    "};";
    private final String script2 =
            "var empty = {}; \n" +
                    "var constructorObject = function(val) {\n" +
                    "    this.value = val; \n" +
                    "};" +
                    "constructorObject.prototype = { \n" +
                    "   voidFunc:    function() {}, \n" +
                    "   jsvalueFunc: function() { var undef; return undef; }, \n" +
                    "   jsobjectFunc:function() { return {}; }, \n" +
                    "   intFunc:     function() { return 5; }, \n" +
                    "   intFunc2:    function() { return 9; }, \n" +
                    "   longFunc:    function() { return 6; }, \n" +
                    "   longFunc2:   function() { return 10; }, \n" +
                    "   floatFunc:   function() { return 7.6; }, \n" +
                    "   floatFunc2:  function() { return 17.6; }, \n" +
                    "   doubleFunc:  function() { return 8.8; }, \n" +
                    "   doubleFunc2: function() { return 18.8; }, \n" +
                    "   stringFunc:  function() { return 'string'; }, \n" +
                    "   arrayFunc:   function() { return [5,6,7,8]; }, \n" +
                    "   booleanFunc: function() { return true; }, \n" +
                    "   myValue:     function() { return this.value; } \n" +
                    "};";

    public static class Function extends JSObject {
        public final JSObject function;
        public Function(JSContext ctx) {
            super(ctx);
            JSObject f = null;
            try {
                f = new JSObject(ctx, this, Function.class.getMethod("functionBody", Double.class));
            } catch (NoSuchMethodException e) {
                f = null;
            } finally {
                function = f;
            }
        }
        public Integer functionBody(Double value) {
            return value.intValue() + 1;
        }
    }

    public void testJSObjectConstructors() throws TestAssertException {
        JSContext context = new JSContext();
        context.evaluateScript(script);
        JSObject empty = new JSObject(context);
        tAssert(empty.toJSON().equals(context.property("empty").toJSON()),
                "new JSObject(<context>) -> " + context.property("empty").toJSON());
        JSObject functionObject = new FunctionObject(context);
        JSObject functionObjectJS = context.property("functionObject").toObject();
        context.property("java", functionObject);
        String[] array1 = functionObject.propertyNames();
        String[] array2 = functionObjectJS.propertyNames();
        java.util.Arrays.sort(array1);
        java.util.Arrays.sort(array2);
        boolean pass = array1.length == array2.length;
        for (int i=0; pass & i<array1.length; i++) pass = array1[i].equals(array2[i]);
        tAssert(pass, "new JSObject(<context>, <interface>) -> JS");
        ConstructorObject constructorObject = new ConstructorObject(context);
        context.property("constructorObjectJava", constructorObject);
        println("Test JSObject(<context>, <interface>, <class>");
        context.evaluateScript(script2);
        JSObject js1   = context.evaluateScript("new constructorObject(5)").toObject();
        JSObject java1 = context.evaluateScript("new constructorObjectJava(5)").toObject();
        JSObject js2   = context.evaluateScript("new constructorObject(6)").toObject();
        JSObject java2 = context.evaluateScript("new constructorObjectJava(6)").toObject();
        boolean equiv = js1.property("myValue").toObject().callAsFunction(js1).isStrictEqual(
                java1.property("myValue").toObject().callAsFunction(java1)
        );
        equiv &= js2.property("myValue").toObject().callAsFunction(js2).isStrictEqual(
                java2.property("myValue").toObject().callAsFunction(java2)
        );
        equiv &= context.evaluateScript("new constructorObject(7).myValue() === new constructorObjectJava(7).myValue()").toBoolean();
        equiv &= context.evaluateScript("new constructorObject(8).myValue() !== new constructorObjectJava(9).myValue()").toBoolean();
        tAssert(equiv, "new JSObject(<context>, <interface>, <class> -> JS");
        tAssert(context.evaluateScript("new constructorObject(9) instanceof constructorObject").toBoolean(),
                "JS instanceof JS");
        tAssert(context.evaluateScript("new constructorObjectJava(10) instanceof constructorObjectJava").toBoolean(),
                "Java instanceof Java");
        tAssert(!context.evaluateScript("new constructorObject(11) instanceof constructorObjectJava").toBoolean(),
                "!JS instanceof Java");
        tAssert(!context.evaluateScript("new constructorObjectJava(12) instanceof constructorObject").toBoolean(),
                "!Java instanceof JS");
        tAssert(context.evaluateScript("new constructorObject(9)")
                .isInstanceOfConstructor(context.property("constructorObject").toObject()),
                "JS.isInstanceOfConstructor(JS)");
        tAssert(context.evaluateScript("new constructorObjectJava(10)")
                .isInstanceOfConstructor(context.property("constructorObjectJava").toObject()),
                "Java.isInstanceOfConstructor(Java-JS)");
        tAssert(context.evaluateScript("new constructorObjectJava(11)")
                .isInstanceOfConstructor(constructorObject),
                "Java.isInstanceOfConstructor(Java)");
        tAssert(!context.evaluateScript("new constructorObject(12)")
                .isInstanceOfConstructor(constructorObject),
                "!JS.isInstanceOfConstructor(Java)");
        context.property("functx", new Function(context).function);
        tAssert(context.evaluateScript("functx(13.3)").isStrictEqual(14),
                "JSObject(<context>, <object>, <method>)");
        String url = "http://www.liquidplayer.org/js_func.js";
        JSObject increment = new JSObject(context, "increment",
                new String[] {"value"},
                "if (typeof value === 'number') return value + 1;\n" +
                "else return does_not_exist;\n",
                url, 10);
        tAssert(increment.callAsFunction(new JSValue[] {new JSValue(context,5)} ).isStrictEqual(6),
                "JSObject(<context>, [<params>], <body>, <url>, <lineno>)");
        try {
            tAssert(increment.callAsFunction().isStrictEqual(6),
                    "JSObject(<context>, [<params>], <body>, <url>, <lineno>)[2]");
        } catch (JSException e) {
            String stack = e.getError().toObject().property("stack").toString();
            String expected = "increment@" + url + ":12:";
            tAssert(stack.substring(0,expected.length()).equals(expected),
                    "JSObject(<context>, [<params>], <body>, <url>, <lineno>) stack trace");
        }

    }

    public void testJSObjectFunctionCallback() throws TestAssertException {
        JSContext context = new JSContext();
        context.evaluateScript(script);
        JSObject functionObject = new FunctionObject(context);
        JSObject functionObjectJS = context.property("functionObject").toObject();
        println("Test JSObject function return values");
        context.property("java", functionObject);
        for (String func : functionObjectJS.propertyNames()) {
            boolean strict = context.evaluateScript("functionObject." + func + "() === java." + func + "()").toBoolean();
            tAssert(functionObjectJS.property(func).toObject().callAsFunction().isStrictEqual(
                    functionObject.property(func).toObject().callAsFunction()
                    ) == strict && ((func.equals("jsobjectFunc") && !strict) ||
                            (func.equals("arrayFunc") && !strict) ||
                            strict),
                    "JS." + func + "() === Java." + func + "() [" + strict + "]");
        }
        tAssert(context.evaluateScript("functionObject.arrayFunc().sort().join('|') === java.arrayFunc().sort().join('|')").toBoolean(),
                "test return int array");
    }

    @Override
    public void run() throws TestAssertException {
        println("**** JSObject ****");
        println("------------------");
        testJSObjectConstructors();
        testJSObjectFunctionCallback();
        println("------------------");
    }
}
