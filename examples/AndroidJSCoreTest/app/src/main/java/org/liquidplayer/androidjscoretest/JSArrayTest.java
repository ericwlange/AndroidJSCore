package org.liquidplayer.androidjscoretest;

import org.liquidplayer.webkit.javascriptcore.JSArray;
import org.liquidplayer.webkit.javascriptcore.JSBaseArray;
import org.liquidplayer.webkit.javascriptcore.JSContext;
import org.liquidplayer.webkit.javascriptcore.JSDate;
import org.liquidplayer.webkit.javascriptcore.JSFunction;
import org.liquidplayer.webkit.javascriptcore.JSIterator;
import org.liquidplayer.webkit.javascriptcore.JSMap;
import org.liquidplayer.webkit.javascriptcore.JSObject;
import org.liquidplayer.webkit.javascriptcore.JSValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;

/**
 * Created by Eric on 6/11/16.
 */
public class JSArrayTest extends JSTest {
    JSArrayTest(MainActivity activity) {
        super(activity);
    }

    public void testJSArrayConstructors() throws TestAssertException {
        JSContext context = track(new JSContext(),"testJSArrayContructors:context");

        /**
         * new JSArray(context, JSValue[], cls)
         */
        JSValue [] initializer = new JSValue[] { new JSValue(context,1), new JSValue(context,"two")};
        JSArray<JSValue> array = new JSArray<JSValue>(context, initializer, JSValue.class);
        tAssert(array.size()==2 && array.get(0).equals(1) && array.get(1).equals("two"),
                "new JSArray(context, JSValue[], cls)");

        /**
         * new JSArray(context, cls)
         */
        JSArray<Integer> array2 = new JSArray<Integer>(context,Integer.class);
        array2.add(10);
        array2.add(20);
        tAssert(array2.size()==2 && array2.get(0).equals(10) && array2.get(1).equals(20),
                "new JSArray(context, cls)");

        /**
         * new JSArray(context, Object[], cls)
         */
        Object [] objinit = new Object [] { 1, 2.0, "three"};
        JSArray<JSValue> array3 = new JSArray<JSValue>(context, objinit, JSValue.class);
        tAssert(array3.size()==3 && array3.get(0).equals("1") && array3.get(1).isStrictEqual(2) &&
                array3.get(2).isStrictEqual("three"),
                "new JSArray(context, Object[], cls)");

        /**
         * new JSArray(context, List, cls)
         */
        List<String> list = new ArrayList<>();
        list.add("first");
        list.add("second");
        JSArray<String> array4 = new JSArray<String>(context,list,String.class);
        tAssert(array4.size()==2 && array4.get(0).equals("first") && array4.get(1).equals("second"),
                "new JSArray(context, List, cls)");

    }

    public void testJSArrayListMethods() throws TestAssertException {
        final JSContext context = track(new JSContext(), "testJSArrayListMethods:context");

        List<Object> list = new JSArray<Object>(context, Object.class);
        /**
         * JSArray.add(value)
         */
        list.add("zero");
        list.add(1);
        list.add(2.0);
        list.add(new Integer[]{3});
        list.add(new JSObject(context));
        tAssert(list.get(0).equals("zero") && list.get(1).equals(1) && list.get(2).equals(2) &&
                        ((JSValue) list.get(3)).isArray() && ((JSValue) list.get(4)).isObject(),
                "JSArray.add(value)");

        /**
         * JSArray.toArray()
         */
        Object[] array = list.toArray();
        tAssert(array[0].equals("zero") && array[1].equals(1) && array[2].equals(2) &&
                        ((JSValue) array[3]).isArray() && ((JSValue) array[4]).isObject(),
                "JSArray.toArray()");

        /**
         * JSArray.get(index)
         */
        ((JSArray) list).propertyAtIndex(list.size(), "anotherone");
        tAssert(list.get(5).equals("anotherone"), "JSArray.get(index)");
        tAssert(((JSValue) list.get(3)).isArray(), "JSArray.get(index) -> array");

        /**
         * JSArray.size()
         */
        tAssert(list.size() == 6, "JSArray.size()");

        /**
         * JSArray.isEmpty()
         */
        List<Integer> list2 = new JSArray<Integer>(context, Integer.class);
        tAssert(!list.isEmpty() && list2.isEmpty(), "JSArray.isEmpty()");

        /**
         * JSArray.contains(object)
         */
        tAssert(list.contains("zero") && list.contains(1) && list.contains(2.0) &&
                        !list.contains(5),
                "JSArray.contains(object)");

        /**
         * JSArray.iterator()
         */
        int i = 0;
        for (Iterator<Object> it = list.iterator(); it.hasNext(); i++) {
            Object next = it.next();
            tAssert(list.contains(next), "JSArray.iterator() -> " + next);
        }
        tAssert(i == list.size(), "JSArray.iterator()");

        /**
         * JSArray.toArray(Object[])
         */
        list2.add(0);
        list2.add(1);
        list2.add(2);
        Integer[] arr1 = new Integer[3];
        Integer[] arr2 = list2.toArray(arr1);
        tAssert(arr2.length == 3 && arr2[0].equals(0) && arr2[1].equals(1) && arr2[2].equals(2),
                "JSArray.toArray(T[]) -> same size");
        list2.add(3);
        arr2 = list2.toArray(arr1);
        tAssert(arr2.length == 4 && arr2[0].equals(0) && arr2[1].equals(1) && arr2[2].equals(2) &&
                        arr2[3].equals(3),
                "JSArray.toArray(T[]) -> greater than arg size");
        list2.remove(3);
        list2.remove(2);
        arr2 = list2.toArray(arr1);
        tAssert(arr2.length == 3 && arr2[0].equals(0) && arr2[1].equals(1) && arr2[2] == null,
                "JSArray.toArray(T[]) -> less than arg size");

        /**
         * JSArray.remove(object)
         */
        tAssert(list2.remove(Integer.valueOf(1)) && !list2.remove(Integer.valueOf(2)) &&
                        !list2.contains(1),
                "JSArray.remove(object)");

        /**
         * JSArray.containsAll(collection)
         */
        Collection<Object> collection = new ArrayList<Object>();
        collection.add("zero");
        collection.add(1);
        collection.add(2);
        Collection<Object> collection2 = new ArrayList<Object>(collection);
        collection2.add(25.0);
        tAssert(list.containsAll(collection) && !list.containsAll(collection2),
                "JSArray.containsAll(collection)");

        /**
         * JSArray.addAll(collection)
         */
        int size = list.size();
        list.addAll(collection);
        tAssert(list.size() == size + collection.size(),
                "JSArray.addAll(collection)");

        /**
         * JSArray.removeAll(collection)
         */
        size = list.size();
        list.removeAll(collection);
        tAssert(list.size() == size - collection.size() * 2,
                "JSArray.removeAll(collection)");

        /**
         * JSArray.retainAll(collection)
         */
        list.addAll(collection);
        list.retainAll(collection);
        tAssert(list.size() == collection.size() && list.containsAll(collection),
                "JSArray.retainAll(collection)");

        /**
         * JSArray.clear()
         */
        list.clear();
        tAssert(list.size() == 0, "JSArray.clear()");

        /**
         * JSArray.set(index,object)
         */
        list.addAll(collection);
        Object last1;
        try {
            Object last2 = list.set(10, "bar");
            last1 = 0;
        } catch (IndexOutOfBoundsException e) {
            last1 = list.set(1, "foo");
        }
        tAssert(last1.equals(1) && list.get(1).equals("foo"),
                "JSArray.set(index,object)");

        /**
         * JSArray.add(index,object)
         */
        list.add(1, "hello");
        list.add(4, "world");
        try {
            list.add(10, 10.0);
        } catch (IndexOutOfBoundsException e) {

        }
        tAssert(list.get(1).equals("hello") && list.get(2).equals("foo") && list.size() == 5 &&
                list.get(4).equals("world"), "JSArray.add(index,object)");

        /**
         * JSArray.remove(index)
         */
        list.remove(4);
        list.remove(1);
        tAssert(list.get(1).equals("foo") && list.size() == 3, "JSArray.remove(index)");

        /**
         * JSArray.indexOf(object)
         */
        list.addAll(collection);
        tAssert(list.indexOf("zero") == 0 && list.indexOf("foo") == 1 && list.indexOf(2) == 2 &&
                        list.indexOf(1) == 4 && list.indexOf("world") == -1,
                "JSArray.indexOf(object)");

        /**
         * JSArray.lastIndexOf(object)
         */
        tAssert(list.lastIndexOf("zero") == 3 && list.lastIndexOf("foo") == 1 && list.lastIndexOf(2) == 5 &&
                        list.lastIndexOf(1) == 4 && list.lastIndexOf("world") == -1,
                "JSArray.lastIndexOf(object)");

        /**
         * JSArray.listIterator()
         */
        // List iterator is heavily used by underlying JSArray methods already tested.  Only
        // underlying methods untested are 'set' and 'add'
        for (ListIterator<Object> it = list.listIterator(); it.hasNext(); ) {
            Object dupe = it.next();
            it.set("changed");
            it.add(dupe);
        }
        tAssert(list.size() == 12 && list.indexOf("changed") == 0 && list.lastIndexOf("changed") == 10,
                "JSArray.listIterator()");

        /**
         * JSArray.listIterator(index)
         */
        for (ListIterator<Object> it = list.listIterator(0); it.hasNext(); ) {
            if (it.next().equals("changed")) it.remove();
        }
        tAssert(list.listIterator(list.size()).previous().equals(list.listIterator(list.size() + 10).previous()) &&
                        list.size() == 6,
                "JSArray.listIterator(index)");

        /**
         * JSArray.subList(fromIndex, toIndex)
         */
        list.subList(1, 4).clear();
        tAssert(list.size() == 3 && list.get(0).equals("zero") && list.get(1).equals(1) &&
                list.get(2).equals(2), "JSArray.subList(fromIndex,toIndex)");

        /**
         * JSArray.equals()
         */
        ArrayList<Object> arrayList = new ArrayList<Object>(collection);
        tAssert(list.equals(arrayList) && !list.equals(list2), "JSArray.equals()");

        /**
         * JSArray.hashCode()
         */
        JSArray<Object> hashList = new JSArray<Object>(context, collection, Object.class);
        ArrayList<Object> arrayList2 = new ArrayList<Object>();
        arrayList2.add("zero");
        arrayList2.add(1.0); // <-- Note: making these Doubles is necessary for hashCode match
        arrayList2.add(2.0); // <--
        tAssert(list.hashCode() == hashList.hashCode() && list.hashCode() != list2.hashCode() &&
                        list.hashCode() == arrayList2.hashCode() && list.equals(arrayList2),
                "JSArray.hashCode()");
    }

    public void testJSArrayJSMethods() throws TestAssertException {
        final JSContext context = track(new JSContext(), "testJSArrayJSMethods:context");

        // Array.from()
        @SuppressWarnings("unchecked")
        JSArray<JSValue> from = JSArray.from(context,"foo");
        String [] foo = { "f", "o", "o" };
        tAssert(from.equals(Arrays.asList(foo)), "JSArray.from()");

        // Array.isArray()
        tAssert(JSArray.isArray(from) && !JSArray.isArray(new JSValue(context,5)),
                "JSArray.isArray()");

        // Array.of()
        @SuppressWarnings("unchecked")
        JSArray<JSValue> of = JSArray.of(context,1,2,3);
        Integer [] bar = { 1, 2, 3 };
        tAssert(of.equals(Arrays.asList(bar)), "JSArray.of()");

        // Array.prototype.concat()
        @SuppressWarnings("unchecked")
        JSArray<JSValue> concat = JSArray.of(context,"first");
        concat = concat.concat(from,of,50);
        tAssert(concat.size()==8 && concat.get(2).equals("o") && concat.get(5).equals(2) &&
                concat.get(7).equals(50),
                "JSArray.concat()");

        // Array.prototype.copyWithin()
        JSArray copyWithin = JSArray.of(context,1,2,3,4,5);
        JSArray copyWithin2 = (JSArray) copyWithin.copyWithin(-2);
        Integer [] copyWithin3 = new Integer [] { 1,2,3,1,2 };
        tAssert(copyWithin.equals(Arrays.asList(copyWithin3)) &&
                copyWithin2.equals(Arrays.asList(copyWithin3)), "JSArray.copyWithin()");

        // Array.prototype.entries()
        JSArray<Integer> entriesArray = new JSArray<Integer>(context,copyWithin3,Integer.class);
        Iterator<Map.Entry<Integer,Integer>> entries = entriesArray.entries();
        tAssert(entries.next().getValue().equals(1) && entries.next().getValue().equals(2),
                "JSArray.entries()");

        // Array.prototype.every()
        JSArray<Integer> every1 = new JSArray<Integer>(context, JSArray.of(context,12,5,8,130,44), Integer.class);
        JSArray<Integer> every2 = new JSArray<Integer>(context, JSArray.of(context,12,54,18,130,44), Integer.class);
        tAssert(
        !every1.every(new JSArray.JSBaseArrayEachBooleanCallback<Integer>() {
            @Override
            public boolean callback(Integer value, int i, JSBaseArray<Integer> jsArray) {
                return value >= 10;
            }
        }) &&
        every2.every(new JSFunction(context,"callback",new String [] {"integer"},
                "return integer >= 10;", null, 0)),
        "JSArray.every()");

        // Array.prototype.fill()
        JSArray<Integer> fillArray = new JSArray<Integer>(context,copyWithin3,Integer.class);
        JSArray<Integer> fillArray2 = (JSArray<Integer>) fillArray.fill(4,1);
        Integer [] fillCompare = new Integer [] { 1,4,4,4,4 };
        tAssert(fillArray.equals(Arrays.asList(fillCompare)) &&
                fillArray2.equals(Arrays.asList(fillCompare)),
                "JSArray.fill()");

        // Array.prototype.filter()
        JSArray<Integer> filtered = (JSArray<Integer>)
            every1.filter(new JSArray.JSBaseArrayEachBooleanCallback<Integer>() {
            @Override
            public boolean callback(Integer value, int i, JSBaseArray<Integer> jsArray) {
                return value >= 10;
            }
        });
        tAssert(filtered.equals(Arrays.asList(12,130,44)), "JSArray.filter()");

        // Array.prototype.find()
        Map<String,Object> map1 = new HashMap<>();
        Map<String,Object> map2 = new HashMap<>();
        Map<String,Object> map3 = new HashMap<>();
        map1.put("name", "apples");  map2.put("name", "bananas");  map3.put("name", "cherries");
        map1.put("quantity", 2);     map2.put("quantity", 0);      map3.put("quantity", 5);

        JSArray<Map> inventory = new JSArray<Map>(context,Arrays.asList(map1,map2,map3),Map.class);
        Map cherries = inventory.find(new JSArray.JSBaseArrayEachBooleanCallback<Map>() {
            @Override
            public boolean callback(Map map, int i, JSBaseArray<Map> jsArray) {
                return map.get("name").equals("cherries");
            }
        });
        tAssert(cherries.get("quantity").equals(5), "JSArray.find()");

        // Array.prototype.findIndex()
        JSFunction isPrime = new JSFunction(context,"isPrime") {
            public boolean isPrime(Integer element) {
                int start = 2;
                while (start <= Math.sqrt(element)) {
                    if (element % start++ < 1) {
                        return false;
                    }
                }
                return element > 1;
            }
        };
        JSArray<Integer> notPrime = new JSArray<Integer>(context,Arrays.asList(4,6,8,12),Integer.class);
        JSArray<Integer> sevenPrime = new JSArray<Integer>(context,Arrays.asList(4,6,7,12),Integer.class);
        tAssert(notPrime.findIndex(isPrime) == -1 && sevenPrime.findIndex(isPrime) == 2,
                "JSArray.findIndex()");

        // Array.prototype.forEach()
        final HashMap<Integer,Integer> forEachMap = new HashMap<>();
        notPrime.forEach(new JSArray.JSBaseArrayForEachCallback<Integer>() {
            @Override
            public void callback(Integer integer, int i, JSBaseArray<Integer> jsArray) {
                forEachMap.put(i,integer);
            }
        });
        tAssert(forEachMap.size()==4 && forEachMap.get(0).equals(4) && forEachMap.get(3).equals(12),
                "JSArray.forEach()");

        // Array.prototype.includes()
        tAssert(!notPrime.includes(7) && sevenPrime.includes(7) && !sevenPrime.includes(7,3),
                "JSArray.includes()");

        // Array.prototype.indexOf()
        tAssert(notPrime.indexOf(8) == notPrime.indexOf(8,0), "JSArray.indexOf()");

        // Array.prototype.join()
        tAssert(sevenPrime.join("|").equals("4|6|7|12"), "JSArray.join()");

        // Array.prototype.keys()
        JSArray<String> keysArr = new JSArray<String>(context,String.class);
        keysArr.propertyAtIndex(0,"Zero");
        keysArr.propertyAtIndex(1,"One");
        keysArr.propertyAtIndex(1000,"Thousand");
        Iterator<Integer> keys = keysArr.keys();
        tAssert(keys.next().equals(0) && keys.next().equals(1) && keys.next().equals(2) &&
                keys.hasNext(), "JSArray.keys()");

        // Array.prototype.lastIndexOf()
        tAssert(notPrime.lastIndexOf(8) == notPrime.lastIndexOf(8,notPrime.size()-1),
                "JSArray.indexOf()");

        // Array.prototype.pop()
        String popped = keysArr.pop();
        tAssert(popped.equals("Thousand"), "JSArray.pop()");

        // Array.prototype.push()
        keysArr.push("One-Thousand","One-Thousand One");
        tAssert(keysArr.size()==1002 && keysArr.pop().equals("One-Thousand One"), "JSArray.push()");

        // Array.prototype.map()
        JSArray<JSValue> inventoryMap = (JSArray<JSValue>) inventory
                .map(new JSArray.JSBaseArrayMapCallback<Map>() {
                    @Override
                    public JSValue callback(Map map, int i, JSBaseArray<Map> jsArray) {
                        return new JSValue(context,map.get("quantity"));
                    }
                });
        tAssert(inventoryMap.equals(Arrays.asList(2,0,5)), "JSArray.map()");

        // Array.prototype.reduce()
        int inventoryCount = inventoryMap
                .reduce(new JSArray.JSBaseArrayReduceCallback() {
                    @Override
                    public JSValue callback(JSValue jsValue, JSValue jsValue1, int i,
                                            JSBaseArray<JSValue> jsArray) {
                        return new JSValue(context,jsValue.toNumber() + jsValue1.toNumber());
                    }
                }).toNumber().intValue();
        tAssert(inventoryCount==7, "JSArray.reduce()");

        // Array.prototype.reduceRight()
        int inventoryCountRight = inventory
                .map(new JSFunction(context,"map") {
                    public JSValue map(Map map) {
                        return (JSValue) map.get("quantity");
                    }
                })
                .reduceRight(new JSArray.JSBaseArrayReduceCallback() {
                    @Override
                    public JSValue callback(JSValue jsValue, JSValue jsValue1, int i,
                                            JSBaseArray<JSValue> jsArray) {
                        return new JSValue(context,jsValue.toNumber() - jsValue1.toNumber());
                    }
                },inventoryCount)
                .toNumber()
                .intValue();
        tAssert(inventoryCountRight==0, "JSArray.reduceRight()");

        // Array.prototype.reverse()
        JSArray<JSValue> forward = JSArray.of(context,"one","two","three");
        JSArray<JSValue> reverse = (JSArray<JSValue>) forward.reverse();
        tAssert(forward == reverse && reverse.equals(Arrays.asList("three","two","one")),
                "JSArray.reverse()");

        // Array.prototype.shift()
        tAssert(reverse.shift().equals("three") && reverse.size()==2, "JSArray.shift()");

        // Array.prototype.unshift()
        tAssert(reverse.unshift(new JSValue(context,"four"),new JSValue(context,"three")) == 4,
                "JSArray.unshift()");

        // Array.prototype.slice()
        JSArray<String> sliceme = new JSArray<String>(context,reverse,String.class);
        JSArray<String> slice = (JSArray<String>) sliceme.slice(1,3);
        tAssert(slice.equals(Arrays.asList("three","two")), "JSArray.slice()");

        // Array.prototype.some()
        boolean truthy = sevenPrime.some(new JSFunction(context,"some") {
            public boolean some(JSValue value) {
                return value.equals("7");
            }
        });
        boolean truth = notPrime.some(new JSArray.JSBaseArrayEachBooleanCallback<Integer>() {
            @Override
            public boolean callback(Integer integer, int i, JSBaseArray<Integer> jsArray) {
                return integer==7;
            }
        });
        tAssert(truthy && !truth, "JSArray.some()");

        // Array.prototype.sort()
        boolean sorted = reverse.sort().equals(Arrays.asList("four","one","three","two"));
        notPrime.sort(new JSArray.JSBaseArraySortCallback<Integer>() {
            @Override
            public double callback(Integer t1, Integer t2) {
                return t2 - t1;
            }
        });
        sorted &= notPrime.equals(Arrays.asList(12,8,6,4));
        tAssert(sorted,"JSArray.sort()");

        // Array.prototype.splice()
        JSArray<Integer> spliced = notPrime.splice(1,2,11,10,9);
        tAssert(spliced.equals(Arrays.asList(8,6)) && notPrime.equals(Arrays.asList(12,11,10,9,4)),
                "JSArray.splice()");

        // Array.prototype.toLocaleString()
        JSDate date = new JSDate(context);
        JSArray<JSValue> locale = JSArray.of(context,1337,date,"foo");
        String dateLocale = date.property("toLocaleString").toFunction().call(date).toString();
        tAssert(locale.toLocaleString().equals("1337," + dateLocale + ",foo"),
                "JSArray.toLocaleString() -> " + locale.toLocaleString());

        // Array.prototype.values()
        Iterator<String> values = keysArr.values();
        tAssert(values.next().equals("Zero") && values.next().equals("One") &&
                values.next().equals("undefined"),
                "JSArray.values()");
    }

    @Override
    public void run() throws TestAssertException {
        println("**** JSArray ****");
        testJSArrayConstructors();
        testJSArrayListMethods();
        testJSArrayJSMethods();
        println("-----------------");
    }

}
