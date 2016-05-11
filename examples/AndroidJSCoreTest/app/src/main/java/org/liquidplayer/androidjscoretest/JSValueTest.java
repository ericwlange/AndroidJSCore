package org.liquidplayer.androidjscoretest;

import org.liquidplayer.webkit.javascriptcore.JSContext;
import org.liquidplayer.webkit.javascriptcore.JSException;
import org.liquidplayer.webkit.javascriptcore.JSString;
import org.liquidplayer.webkit.javascriptcore.JSValue;

/**
 * Created by Eric on 5/7/16.
 */
public class JSValueTest extends JSTest {
    public JSValueTest(MainActivity activity) { super(activity); }

    public void testJSValueConstructors() throws TestAssertException {
        println("Test JSValue creation methods");
        JSContext context = new JSContext();
        JSValue undefined = new JSValue(context);
        tAssert(undefined.isUndefined(), "JSValue(context) -> undefined");
        JSValue NULL = new JSValue(context,null);
        tAssert(NULL.isNull(), "JSValue(context,null) -> null");
        JSValue bool = new JSValue(context,false);
        tAssert(bool.isBoolean(), "JSValue(context,<Boolean>) -> boolean");
        JSValue integer = new JSValue(context,50);
        JSValue doub = new JSValue(context,50.0);
        JSValue lng = new JSValue(context,50L);
        JSValue flt = new JSValue(context,50.f);
        tAssert(integer.isNumber(), "JSValue(context,<Integer>) -> number");
        tAssert(doub.isNumber(), "JSValue(context,<Double>) -> number");
        tAssert(lng.isNumber(), "JSValue(context,<Long>) -> number");
        tAssert(flt.isNumber(), "JSValue(context,<Float>) -> number");
        JSValue str1 = new JSValue(context,"This is a string");
        JSValue str2 = new JSValue(context,new JSString("This is a string on drugs"));
        tAssert(str1.isString(), "JSValue(context,<String>) -> string");
        tAssert(str2.isString(), "JSValue(context,<JSString>) -> string");
        JSValue alsoUndefined = new JSValue(context,new Object());
        tAssert(alsoUndefined.isUndefined(), "JSValue(context,<RandomObject>) -> undefined");
    }

    public void testJSValueTesters() throws TestAssertException {
        println("Test JSValue tester methods");
        final String script =
                "var undefined; \n" +
                "var NULL = null; \n" +
                "var bool = true; \n" +
                "var number = 15.6; \n" +
                "var string = 'string'; \n" +
                "var object = {}; \n" +
                "var array = []; \n" +
                "var date = new Date(); \n" +
                "";
        JSContext context = new JSContext();
        context.evaluateScript(script);
        boolean undefNot =
                context.property("undefined").isNull() ||
                context.property("undefined").isBoolean() ||
                context.property("undefined").isNumber() ||
                context.property("undefined").isString() ||
                context.property("undefined").isArray() ||
                context.property("undefined").isDate() ||
                context.property("undefined").isObject();
        tAssert(context.property("undefined").isUndefined() && !undefNot, "JSValue.isUndefined()");
        boolean nullNot =
                context.property("NULL").isUndefined() ||
                context.property("NULL").isBoolean() ||
                context.property("NULL").isNumber() ||
                context.property("NULL").isString() ||
                context.property("NULL").isArray() ||
                context.property("NULL").isDate() ||
                context.property("NULL").isObject();
        tAssert(context.property("NULL").isNull() && !nullNot, "JSValue.isNull()");
        boolean boolNot =
                context.property("bool").isUndefined() ||
                context.property("bool").isNull() ||
                context.property("bool").isNumber() ||
                context.property("bool").isString() ||
                context.property("bool").isArray() ||
                context.property("bool").isDate() ||
                context.property("bool").isObject();
        tAssert(context.property("bool").isBoolean() && !boolNot, "JSValue.isBoolean()");
        boolean numberNot =
                context.property("number").isUndefined() ||
                context.property("number").isNull() ||
                context.property("number").isBoolean() ||
                context.property("number").isString() ||
                context.property("number").isArray() ||
                context.property("number").isDate() ||
                context.property("number").isObject();
        tAssert(context.property("number").isNumber() && !numberNot, "JSValue.isNumber()");
        boolean stringNot =
                context.property("string").isUndefined() ||
                context.property("string").isNull() ||
                context.property("string").isBoolean() ||
                context.property("string").isNumber() ||
                context.property("string").isArray() ||
                context.property("string").isDate() ||
                context.property("string").isObject();
        tAssert(context.property("string").isString() && !stringNot, "JSValue.isString()");
        boolean objectNot =
                context.property("object").isUndefined() ||
                context.property("object").isNull() ||
                context.property("object").isBoolean() ||
                context.property("object").isNumber() ||
                context.property("object").isArray() ||
                context.property("object").isDate() ||
                context.property("object").isString();
        tAssert(context.property("object").isObject() && !objectNot, "JSValue.isObject()");
        boolean arrayNot =
                context.property("array").isUndefined() ||
                context.property("array").isNull() ||
                context.property("array").isBoolean() ||
                context.property("array").isNumber() ||
                context.property("array").isDate() ||
                context.property("array").isString();
        tAssert(context.property("array").isObject() &&
                context.property("array").isArray() && !arrayNot, "JSValue.isArray()");
        boolean dateNot =
                context.property("date").isUndefined() ||
                context.property("date").isNull() ||
                context.property("date").isBoolean() ||
                context.property("date").isNumber() ||
                context.property("date").isArray() ||
                context.property("date").isString();
        tAssert(context.property("date").isObject() &&
                context.property("date").isDate() && !dateNot, "JSValue.isDate()");

        final String script2 =
                "var foo = function() {}; var bar = new foo();";
        context.evaluateScript(script2);
        tAssert(context.property("bar").isInstanceOfConstructor(context.property("foo").toObject()) &&
                !context.property("foo").isInstanceOfConstructor(context.property("bar").toObject()),
                "JSValue.isInstanceOfConstructor()");
    }

    public void testJSValueComparators() throws TestAssertException {
        println("Test JSValue comparator methods");
        JSContext context = new JSContext();
        context.property("number",42f);
        tAssert(context.property("number").equals(42L) && !context.property("number").equals(43),
                "JSValue.equals(<Number>)");
        context.evaluateScript("string = 'string12345';");
        tAssert(context.property("string").equals("string12345") &&
                !context.property("string").equals(context.property("number")),
                "JSValue.equals(<String>)");
        context.evaluateScript("var another_number = 42");
        tAssert(context.property("number").equals(context.property("another_number")),
                "JSValue.equals(<JSValue>)");
        println("Test isStrictEqual (===)");
        tAssert(new JSValue(context,0).equals(false) && !(new JSValue(context,0).isStrictEqual(false)),
                "0 == false && 0 !== false");
        tAssert(new JSValue(context,1).equals("1") && !(new JSValue(context,1).isStrictEqual("1")),
                "1 == '1' && 1 !== '1'");
        tAssert(new JSValue(context,1).equals(1.0) && new JSValue(context,1).isStrictEqual(1.0),
                "1 == 1.0 && 1 === 1.0");
        tAssert(!context.evaluateScript("(function () { var foo; return foo === null; })()").toBoolean(),
                "undefined !== foo");
        tAssert(new JSValue(context).equals(null),
                "undefined == null");
        tAssert(!(new JSValue(context).isStrictEqual(null)),
                "null !== undefined");
        tAssert(new JSValue(context,null).equals(new JSValue(context)) &&
                !(new JSValue(context,null).isStrictEqual(new JSValue(context))),
                "null == undefined && null !== undefined");
    }

    public void testJSValueGetters() throws TestAssertException {
        println("Test JSValue getter methods");
        final String script =
                "var undefined; \n" +
                "var NULL = null; \n" +
                "var bool = true; \n" +
                "var number = 15.6; \n" +
                "var string = 'string'; \n" +
                "var object = {}; \n" +
                "var array = []; \n" +
                "var date = new Date(1970,10,30); \n" +
                "";
        JSContext context = new JSContext();
        context.evaluateScript(script);
        JSValue undefined = context.property("undefined");
        JSValue NULL = context.property("NULL");
        JSValue bool = context.property("bool");
        JSValue number = context.property("number");
        JSValue string = context.property("string");
        JSValue object = context.property("object");
        JSValue array = context.property("array");
        JSValue date = context.property("date");
        tAssert(
                !undefined.toBoolean() && !NULL.toBoolean() && bool.toBoolean() && number.toBoolean() &&
                string.toBoolean() && object.toBoolean() && array.toBoolean() && date.toBoolean(),
                "JSValue.toBoolean()"
        );
        tAssert(NULL.toNumber().equals(0.0), "<null>.toNumber() == 0");
        tAssert(bool.toNumber().equals(1.0), "true.toNumber() == 1");
        tAssert(number.toNumber().equals(15.6), "<number>.toNumber() == <number>");
        tAssert(context.evaluateScript("'11.5'").toNumber().equals(11.5), "'11.5'.toNumber() == 11.5");
        tAssert(undefined.toNumber().isNaN(), "<undefined>.toNumber() == NaN");
        tAssert(string.toNumber().isNaN(), "'string'.toNumber() == NaN");
        tAssert(object.toNumber().isNaN(), "{}.toNumber() == NaN");
        println("new Date() = " + date.toNumber());
        tAssert(array.toNumber().equals(0.0) && context.evaluateScript("[1,2,3]").toNumber().isNaN(),
                "[].toNumber() == 0 && [1,2,3].toNumber() == NaN");
        tAssert(date.toNumber().equals(context.evaluateScript("date.getTime()").toNumber()),
                "new Date(<d>).toNumber() == Date(<d>).getTime()");
        tAssert(undefined.toString().equals("undefined"), "<undefined>.toString() == 'undefined'");
        tAssert(NULL.toString().equals("null"), "<null>.toString() == 'null'");
        tAssert(bool.toString().equals("true") && context.evaluateScript("false").toString().equals("false"),
                "<boolean>.toString() == 'true' || 'false'");
        tAssert(number.toString().equals("15.6"), "<number>.toString() = '<number>'");
        tAssert(string.toString().equals("string"), "<string>.toString() = <string>");
        println("object.toString() = " + object.toString());
        println("array.toString() = " + context.evaluateScript("[1,2,3]").toString());
        println("date.toString() = " + date.toString());
        tAssert(object.toString().equals("[object Object]"), "object.toString() = '[object Object]'");
        tAssert(array.toString().equals(""), "[].toString() == ''");
        tAssert(context.evaluateScript("[1,2,3]").toString().equals("1,2,3"),
                "[1,2,3].toString() == '1,2,3'");
        tAssert(date.toString().startsWith("Mon Nov 30 1970"), "date.toString() = <date string>");
        final String script2 =
                "var jsUndefined = JSON.stringify(undefined); \n" +
                "var jsNULL = JSON.stringify(NULL); \n" +
                "var jsBool = JSON.stringify(bool); \n" +
                "var jsNumber = JSON.stringify(number); \n" +
                "var jsString = JSON.stringify(string); \n" +
                "var jsObject = JSON.stringify(object); \n" +
                "var jsArray = JSON.stringify(array); \n" +
                "var jsDate = JSON.stringify(date); \n" +
                "";
        context.evaluateScript(script2);
        tAssert(bool.toJSON().equals(context.property("jsBool").toString()),
                "<boolean>.toJSON() -> " + bool.toJSON());
        tAssert(number.toJSON().equals(context.property("jsNumber").toString()),
                "<number>.toJSON() -> " + number.toJSON());
        tAssert(string.toJSON().equals(context.property("jsString").toString()),
                "<string>.toJSON() -> " + string.toJSON());
        tAssert(object.toJSON().equals(context.property("jsObject").toString()),
                "<object>.toJSON() -> " + object.toJSON());
        tAssert(array.toJSON().equals(context.property("jsArray").toString()),
                "<array>.toJSON() -> " + array.toJSON());
        tAssert(date.toJSON().equals(context.property("jsDate").toString()),
                "<date>.toJSON() -> " + date.toJSON());
        tAssert(undefined.toJSON() == null,
                "<undefined>.toJSON() -> null");
        tAssert(NULL.toJSON().equals(context.property("jsNULL").toString()),
                "<null>.toJSON() -> " + NULL.toJSON());
    }

    public void run() throws TestAssertException {
        println("**** JSValue ****");
        println("-----------------");
        testJSValueConstructors();
        testJSValueTesters();
        testJSValueComparators();
        testJSValueGetters();
        println("-----------------");
    }

}
