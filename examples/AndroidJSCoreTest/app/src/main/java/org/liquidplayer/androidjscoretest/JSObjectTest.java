package org.liquidplayer.androidjscoretest;

import org.liquidplayer.webkit.javascriptcore.JSArray;
import org.liquidplayer.webkit.javascriptcore.JSContext;
import org.liquidplayer.webkit.javascriptcore.JSException;
import org.liquidplayer.webkit.javascriptcore.JSFunction;
import org.liquidplayer.webkit.javascriptcore.JSObject;
import org.liquidplayer.webkit.javascriptcore.JSValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public final static String functionObjectScript =
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

    public void testJSObjectConstructors() throws TestAssertException {
        JSContext context = track(new JSContext(),"testJSObjectConstructors:context");
        context.evaluateScript(functionObjectScript);

        /**
         * new JSObject(context)
         */
        JSObject empty = new JSObject(context);
        tAssert(empty.toJSON().equals(context.property("empty").toJSON()),
                "new JSObject(<context>) -> " + context.property("empty").toJSON());

        /**
         * new JSObject(context, interface)
         */
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

        /**
         * new JSObject(context, map)
         */
        Map<String,Integer> map = new HashMap<>();
        map.put("first",1);
        map.put("second",2);
        JSObject mapObject = new JSObject(context,map);
        tAssert(mapObject.property("first").isStrictEqual(1) &&
                mapObject.property("second").isStrictEqual(2), "new JSObject(<context>, <map>)");
    }

    public void testJSObjectProperties() throws TestAssertException {
        JSContext context = track(new JSContext(),"testJSObjectProperties:context");

        Map<String,Object> map = new HashMap<>();
        map.put("one",1);
        map.put("two",2.0);
        map.put("string","this is a string");
        map.put("object",new JSObject(context));
        map.put("array",new JSArray<>(context, new Integer[] {1,2,3}));
        map.put("func",new JSFunction(context,"func") {
           public int func(int x) {
               return x+1;
           }
        });
        context.evaluateScript("var object = {one:1, two:2.0, string:'this is a string', " +
                "object: {}, array:[1,2,3], func: function(x) { return x+1;} }");

        JSObject object = new JSObject(context,map);
        JSObject jsobj  = context.property("object").toObject();

        /**
         * hasProperty(property)
         */
        tAssert(object.hasProperty("one") && jsobj.hasProperty("one"), "JSObject.hasProperty(<yes>)");
        tAssert(!object.hasProperty("foo") && !jsobj.hasProperty("foo"), "JSObject.hasProperty(<no>)");

        /**
         * property(property)
         */
        tAssert(object.property("one").toNumber().equals(jsobj.property("one").toNumber()),
                "JSObject.property() -> int");
        tAssert(object.property("two").equals(jsobj.property("two")),
                "JSObject.property() -> double");
        tAssert(object.property("string").equals(jsobj.property("string")),
                "JSObject.property() -> string");
        tAssert(object.property("object").isObject() && jsobj.property("object").isObject(),
                "JSObject.property() -> object");
        tAssert(object.property("object").isObject() && jsobj.property("object").isObject(),
                "JSObject.property() -> object");
        tAssert(object.property("array").isArray() && jsobj.property("array").isArray(),
                "JSObject.property() -> array");
        tAssert(object.property("func").toObject().isFunction() && jsobj.property("func").toObject().isFunction(),
                "JSObject.property() -> function");

        /**
         * property(property,value,-attributes-)
         */
        object.property("added",3);
        tAssert(object.hasProperty("added") && object.property("added").equals(3),
                "JSObject.property(K,V)");
        object.property("readonly",4,JSObject.JSPropertyAttributeReadOnly);
        object.property("readonly",5);
        tAssert(object.hasProperty("readonly") && object.property("readonly").equals(4),
                "JSObject.property(K,V,JSPropertyAttributeReadOnly)");
        object.property("dontdelete",6,JSObject.JSPropertyAttributeDontDelete);
        object.deleteProperty("dontdelete");
        tAssert(object.hasProperty("dontdelete") && object.property("dontdelete").equals(6),
                "JSObject.property(K,V,JSPropertyAttributeReadOnly)");
        object.property("noenum",7,JSObject.JSPropertyAttributeDontEnum);
        tAssert(new JSFunction(context,"f",new String[] {"obj"},
                "for (p in obj) if (p=='noenum') return false; return true;",null,0).call(null,object).toBoolean() &&
                object.hasProperty("noenum") && object.property("noenum").equals(7),
                "JSObject.property(K,V,JSPropertyAttributeDontEnum)");

        /**
         * deleteProperty(property)
         */
        object.deleteProperty("added");
        tAssert(!object.hasProperty("added") && object.property("added").isUndefined(),
                "JSObject.deleteProperty()");

        /**
         * propertyAtIndex(index,value)
         */
        object.propertyAtIndex(50,"fifty");
        tAssert(object.property("50").equals("fifty"), "JSObject.propertyAtIndex(index,value)");

        /**
         * propertyAtIndex(index)
         */
        tAssert(object.propertyAtIndex(50).equals("fifty") && object.propertyAtIndex(51).isUndefined(),
                "JSObject.propertyAtIndex(index)");

        /**
         * propertyNames()
         */
        object.propertyAtIndex(52,"fifty-two");
        List<String> names = Arrays.asList(object.propertyNames());
        // readonly, dontdelete, noenum, 50, and 52 were added, but noenum is not enumerable (diff 4)
        tAssert(names.size() - jsobj.propertyNames().length == 4 &&
                names.contains("one") && names.contains("52") && !names.contains("noenum"),
                "JSObject.propertyNames()");
    }

    public void testJSObjectTesters() throws TestAssertException {
        JSContext context = track(new JSContext(),"testJSObjectTesters:context");
        final String script = "var func = function() {}; var nofunc = {}; var constr = function(x) {this.x = x;};";
        context.evaluateScript(script);
        JSFunction func = new JSFunction(context);
        JSObject nofunc = new JSObject(context);
        JSFunction constr = new JSFunction(context,"constructor") {
            public void constructor(int x) {
                getThis().property("x",x);
            }
        };

        /**
         * isFunction()
         */
        tAssert(func.isFunction() && !nofunc.isFunction() && constr.isFunction() &&
                context.property("func").toObject().isFunction() &&
                !context.property("nofunc").toObject().isFunction() &&
                context.property("constr").toObject().isFunction(),
                "JSObject.isFunction()");

        /**
         * isConstructor()
         */
        tAssert(func.isConstructor() && !nofunc.isConstructor() && constr.isConstructor() &&
                context.property("func").toObject().isConstructor() &&
                !context.property("nofunc").toObject().isConstructor() &&
                context.property("constr").toObject().isConstructor(),
                "JSObject.isConstructor()");

    }

    @Override
    public void run() throws TestAssertException {
        println("**** JSObject ****");
        println("------------------");
        testJSObjectConstructors();
        testJSObjectProperties();
        testJSObjectTesters();
        println("------------------");
    }
}
