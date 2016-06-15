package org.liquidplayer.androidjscoretest;

import junit.framework.Test;

import org.liquidplayer.webkit.javascriptcore.JSArray;
import org.liquidplayer.webkit.javascriptcore.JSContext;
import org.liquidplayer.webkit.javascriptcore.JSException;
import org.liquidplayer.webkit.javascriptcore.JSFunction;
import org.liquidplayer.webkit.javascriptcore.JSObject;
import org.liquidplayer.webkit.javascriptcore.JSValue;

/**
 * Created by Eric on 6/11/16.
 */
public class JSFunctionTest extends JSTest {
    JSFunctionTest(MainActivity activity) {
        super(activity);
    }

    public interface IFunctionObject2 {
        public String [] testFunc(
                JSValue jsvalueParam,
                FunctionObject2 jsobjectParam,
                Integer intParam,
                int intParam2,
                Long longParam,
                long longParam2,
                Float floatParam,
                float floatParam2,
                Double doubleParam,
                double doubleParam2,
                String stringParam,
                Boolean booleanParam,
                Integer[] arrayParam
        );
    }
    public static class FunctionObject2 extends JSObject implements IFunctionObject2 {
        private final JSContext context;
        public FunctionObject2(JSContext ctx) {
            super(ctx, IFunctionObject2.class);
            context = ctx;
            property("hello","is it me you're looking for?");
        }

        public String [] testFunc(
                JSValue jsvalueParam,
                FunctionObject2 jsobjectParam,
                Integer intParam,
                int intParam2,
                Long longParam,
                long longParam2,
                Float floatParam,
                float floatParam2,
                Double doubleParam,
                double doubleParam2,
                String stringParam,
                Boolean booleanParam,
                Integer[] arrayParam
        ) {
            return new String [] {
                    jsvalueParam.toString(),
                    jsobjectParam.toJSON(),
                    intParam.toString(),
                    Integer.toString(intParam2),
                    longParam.toString(),
                    Long.toString(longParam2),
                    floatParam.toString(),
                    Float.toString(floatParam2),
                    doubleParam.toString(),
                    Double.toString(doubleParam2),
                    stringParam,
                    booleanParam.toString(),
                    new JSArray<Object>(context,arrayParam,Object.class).toJSON()
            };
        }
        final public String script =
                "(function(jsvalueParam,jsobjectParam,intParam,intParam2,longParam,longParam2, \n" +
                        "    floatParam,floatParam2,doubleParam,doubleParam2,stringParam,booleanParam,arrayParam) \n" +
                        "{ \n" +
                        "    result = [''+jsvalueParam, JSON.stringify(jsobjectParam), ''+intParam, ''+intParam2, \n" +
                        "        ''+longParam, ''+longParam2, ''+floatParam, ''+floatParam2, ''+doubleParam, \n" +
                        "        ''+doubleParam2, stringParam, ''+booleanParam, JSON.stringify(arrayParam)]; \n" +
                        "    return result; \n" +
                        "}) \n";
        public JSFunction nativeFunc() {
            return context.evaluateScript(script).toFunction();
        }
        public JSFunction javaFunc() {
            context.property("javaObj",this);
            return context.evaluateScript("javaObj.testFunc").toFunction();
        }
    }

    public interface IPrototypeObject extends JSObjectTest.IFunctionObject {
        public Integer myValue();
    }
    public static class PrototypeObject extends JSObject implements IPrototypeObject {
        public PrototypeObject(JSContext context) {
            super(context,IPrototypeObject.class);
        }
        @Override
        public Integer myValue() {
            return getThis().property("value").toNumber().intValue();
        }
        @Override
        public void voidFunc() {}
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

    public class ConstructorFunction extends JSFunction {
        public ConstructorFunction(JSContext ctx) {
            super(ctx,"constructor");
            prototype(new PrototypeObject(ctx));
        }
        public void constructor(int param) {
            getThis().property("value", param);
        }
    }

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

    public void testJSFunctionConstructors() throws TestAssertException {
        JSContext context = track(new JSContext(),"testJSFunctionConstructors:context");

        println("Test JSFunction(<context>, <interface>)");
        ConstructorFunction constructorFunction = new ConstructorFunction(context);
        context.property("constructorObjectJava", constructorFunction);
        context.evaluateScript(script2);
        JSObject js1   = context.evaluateScript("new constructorObject(5)").toObject();
        JSObject java1 = context.evaluateScript("new constructorObjectJava(5)").toObject();
        JSObject js2   = context.evaluateScript("new constructorObject(6)").toObject();
        JSObject java2 = context.evaluateScript("new constructorObjectJava(6)").toObject();
        boolean equiv = js1.property("myValue").toFunction().call(js1).isStrictEqual(
                java1.property("myValue").toFunction().call(java1)
        );
        equiv &= js2.property("myValue").toFunction().call(js2).isStrictEqual(
                java2.property("myValue").toFunction().call(java2)
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
                        .isInstanceOfConstructor(constructorFunction),
                "Java.isInstanceOfConstructor(Java)");
        tAssert(!context.evaluateScript("new constructorObject(12)")
                        .isInstanceOfConstructor(constructorFunction),
                "!JS.isInstanceOfConstructor(Java)");

        context.property("functx", new JSFunction(context,"functionBody") {
            public Integer functionBody(Double value) {
                return value.intValue() + 1;
            }
        });
        tAssert(context.evaluateScript("functx(13.3)").isStrictEqual(14),
                "JSFunction(<context>, <methodName>)");

        String url = "http://www.liquidplayer.org/js_func.js";
        JSFunction increment = new JSFunction(context, "increment",
                new String[] {"value"},
                "if (typeof value === 'number') return value + 1;\n" +
                        "else return does_not_exist;\n",
                url, 10);
        tAssert(increment.call(null,5).isStrictEqual(6),
                "JSFunction(<context>, [<params>], <body>, <url>, <lineno>)");
        try {
            tAssert(increment.call().isStrictEqual(6),
                    "JSFunction(<context>, [<params>], <body>, <url>, <lineno>)[2]");
        } catch (JSException e) {
            String stack = e.getError().toObject().property("stack").toString();
            String expected = "increment@" + url + ":12:";
            tAssert(stack.substring(0,expected.length()).equals(expected),
                    "JSFunction(<context>, [<params>], <body>, <url>, <lineno>) stack trace");
        }

    }

    public void testJSFunctionCallback() throws TestAssertException {
        JSContext context = track(new JSContext(),"testJSFunctionCallback:context");
        context.evaluateScript(JSObjectTest.functionObjectScript);
        JSObject functionObject = new JSObjectTest.FunctionObject(context);
        JSObject functionObjectJS = context.property("functionObject").toObject();
        println("Test JSObject function return values");
        context.property("java", functionObject);
        for (String func : functionObjectJS.propertyNames()) {
            boolean strict = context.evaluateScript("functionObject." + func + "() === java." + func + "()").toBoolean();
            tAssert(functionObjectJS.property(func).toFunction().call().isStrictEqual(
                    functionObject.property(func).toFunction().call()
                    ) == strict && ((func.equals("jsobjectFunc") && !strict) ||
                            (func.equals("arrayFunc") && !strict) ||
                            strict),
                    "JS." + func + "() === Java." + func + "() [" + strict + "]");
        }
        tAssert(context.evaluateScript("functionObject.arrayFunc().sort().join('|') === java.arrayFunc().sort().join('|')").toBoolean(),
                "test return int array");
        println("Test JSObject function parameter values");
        FunctionObject2 functionObject2 = new FunctionObject2(context);
        Object [] params = new Object [] {
                new JSValue(context),
                functionObject2,
                1,
                2,
                3L,
                4L,
                5.5f,
                6.6f,
                7.7,
                8.8,
                "this is a string",
                false,
                new Integer[] {9,10,11,12}
        };
        String string1 = functionObject2.nativeFunc().apply(null,params).toJSON();
        String string2 = functionObject2.javaFunc().apply(null,params).toJSON();
        println("native : " + string1);
        println("java   : " + string2);
        tAssert(string1.equals(string2), "function parameters");
    }

    @Override
    public void run() throws TestAssertException {
        println("**** JSFunction ****");
        testJSFunctionConstructors();
        testJSFunctionCallback();
        println("--------------------");
    }

}
