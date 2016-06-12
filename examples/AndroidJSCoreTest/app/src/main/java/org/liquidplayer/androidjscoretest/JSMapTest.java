package org.liquidplayer.androidjscoretest;

import org.liquidplayer.webkit.javascriptcore.JSContext;
import org.liquidplayer.webkit.javascriptcore.JSMap;
import org.liquidplayer.webkit.javascriptcore.JSObject;
import org.liquidplayer.webkit.javascriptcore.JSValue;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by Eric on 6/11/16.
 */
public class JSMapTest extends JSTest {
    JSMapTest(MainActivity activity) {
        super(activity);
    }

    public void testJSMapConstructors() throws TestAssertException {
        JSContext context = track(new JSContext(),"testJSMapContructors:context");

        /**
         * new JSMap(object,cls)
         */
        JSObject object = new JSObject(context);
        object.property("a",1);
        object.property("b",2.0f);
        Map<String,Integer> map = new JSMap<Integer>(object,Integer.class);
        tAssert(map.get("a").equals(1) && map.get("b").equals(2), "new JSMap(object,cls)");

        /**
         * new JSMap(context,map,cls)
         */
        Map<String,Integer> map2 = new JSMap<Integer>(context,map,Integer.class);
        tAssert(map2.get("a").equals(1) && map2.get("b").equals(2), "new JSMap(context,map,cls)");
        tAssert(!((JSMap)map2).getJSObject().isStrictEqual(((JSMap)map).getJSObject()),
                "Test creates new JSObject");

        /**
         * new JSMap(context,cls)
         */
        Map<String,Double> map3 = new JSMap<Double>(context,Double.class);
        map3.put("key",3.0);
        tAssert(map3.get("key").equals(3.0), "new JSMap(context,cls)");
    }

    public void testJSMapMethods() throws TestAssertException {
        JSContext context = track(new JSContext(),"testJSMapMethods:context");

        Map<String,Object> map = new JSMap<Object>(context,Object.class);

        /**
         * isEmpty()
         */
        tAssert(map.isEmpty(),"JSMap.isEmpty()");
        ((JSMap)map).getJSObject().property("foo","bar");
        tAssert(!map.isEmpty(), "!JSMap.isEmpty()");

        /**
         * size()
         */
        tAssert(map.size() == 1, "JSMap.size()");
        ((JSMap)map).getJSObject().property("mutt","jeff",JSObject.JSPropertyAttributeDontEnum);
        tAssert(map.size() == 1, "JSMap.size() not affected by unenumerable properties");
        map.put("cup","saucer");
        ((JSMap)map).getJSObject().property("yin","yang");
        tAssert(map.size() == 3, "JSMap.size() affected by enumerable properties");

        /**
         * containsKey()
         */
        tAssert(map.containsKey("foo") && map.containsKey("mutt") && map.containsKey("cup") &&
                map.containsKey("yin") && !map.containsKey("notme"), "JSMap.containsKey()");

        /**
         * containsValue()
         * (Non-enumerable values will not be here)
         */
        tAssert(map.containsValue("bar") && !map.containsValue("jeff") && map.containsValue("saucer") &&
                map.containsValue("yang") && !map.containsValue("notme"), "JSMap.containsValue()");

        /**
         * get()
         */
        tAssert(map.get("foo").equals("bar") && map.get("mutt").equals("jeff") &&
                map.get("cup").equals("saucer") && map.get("yin").equals("yang"), "JSMap.get()");
        tAssert(map.get("notme") == null, "JSMap.get(<not in map>) -> null");

        /**
         * put()
         */
        map.put("int",1);
        map.put("double",2.2);
        map.put("float",3.3f);
        map.put("string","a string");
        map.put("object", new JSObject(context));
        map.put("array", new Float [] { 1.1f, 2.2f, 3.3f});
        tAssert(map.get("int").equals(1) && map.get("double").equals(2.2) && map.get("float").equals(3.3) &&
                map.get("string").equals("a string") && ((JSValue)map.get("object")).isObject() &&
                ((JSValue)map.get("array")).isArray(), "JSMap.put() various types");

        Map<String,String> map2 = new JSMap<String>(context,String.class);
        map2.put("0","zero");
        map2.put("1","one");
        map2.put("2","two");
        tAssert(map2.get(0).equals("zero") && map2.get(1).equals("one") &&
                ((JSMap)map2).getJSObject().propertyAtIndex(2).equals("two"),
                "JSMap.get() with numeric keys");

        /**
         * remove()
         */
        ((JSMap)map).getJSObject().property("cantremoveme",3,JSObject.JSPropertyAttributeDontDelete);
        map.remove("double");
        map.remove("cantremoveme");
        tAssert(map.get("double") == null && map.get("cantremoveme").equals(3), "JSMap.remove()");

        /**
         * putAll()
         */
        Map<String,Object> map3 = new HashMap<>();
        map3.put("frommap3_1",1);
        map3.put("frommap3_2",2);
        map3.put("frommap3_3",3);
        map.putAll(map3);
        tAssert(map.get("frommap3_1").equals(1) && map.get("frommap3_2").equals(2) &&
                map.get("frommap3_3").equals(3) && map.get("mutt").equals("jeff"),
                "JSMap.putAll()");

        /**
         * keySet()
         */
        Set<String> keys = map.keySet();
        Set<String> keys2 = map2.keySet();
        tAssert(keys.contains("float") && keys2.contains("2") &&
                !keys.contains("notme") && !keys2.contains("5"),
                "JSMap.keySet()");

        /**
         * values()
         */
        Collection<Object> values = map.values();
        Collection<String> values2 = map2.values();
        tAssert(values.contains(3.3) && values.contains(1) && values.contains("a string") &&
                !values.contains(2.2) && !values.contains("foo") &&
                values2.contains("zero") && values2.contains("two") && !values2.contains("1"),
                "JSMap.values()");

        /**
         * entrySet()
         */
        Set<Map.Entry<String,Object>> entrySet = map.entrySet();
        boolean gotit = false;
        for (Map.Entry<String,Object> entry : entrySet) {
            gotit = gotit || (entry.getKey().equals("float") && entry.getValue().equals(3.3));
        }
        tAssert(gotit,"JSMap.entrySet()");

        /**
         * clear()
         */
        map.clear();
        map2.clear();
        tAssert(map.size()==1 && map2.size()==0, "JSMap.clear()");

    }

    @Override
    public void run() throws TestAssertException {
        println("**** JSMap ****");
        testJSMapConstructors();
        testJSMapMethods();
        println("---------------");
    }

}
