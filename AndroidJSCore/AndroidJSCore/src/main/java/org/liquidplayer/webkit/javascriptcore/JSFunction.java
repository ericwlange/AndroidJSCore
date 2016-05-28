package org.liquidplayer.webkit.javascriptcore;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * A JavaScript function object.
 * @since 3.0
 *
 */
public class JSFunction extends JSObject {

    private class JNIReturnClass implements Runnable {
        @Override
        public void run() {}
        JNIReturnObject jni;
    }

    /**
     * Creates a JavaScript function that takes parameters 'parameterNames' and executes the
     * JS code in 'body'.
     *
     * @param ctx                The JSContext in which to create the function
     * @param name               The name of the function
     * @param parameterNames     A String array containing the names of the parameters
     * @param body               The JavaScript code to execute in the function
     * @param sourceURL          The URI of the source file, only used for reporting in stack trace (optional)
     * @param startingLineNumber The beginning line number, only used for reporting in stack trace (optional)
     * @throws JSException
     * @since 2.2
     */
    public JSFunction(JSContext ctx, final String name, final String[] parameterNames,
                      final String body, final String sourceURL, final int startingLineNumber)
            throws JSException {
        context = ctx;
        context.sync(new Runnable() {
            @Override
            public void run() {
                long[] names = new long[parameterNames.length];
                for (int i = 0; i < parameterNames.length; i++) {
                    names[i] = new JSString(parameterNames[i]).stringRef();
                }
                JNIReturnObject jni = makeFunction(
                        context.ctxRef(),
                        new JSString(name).stringRef(),
                        names,
                        new JSString(body).stringRef(),
                        (sourceURL == null) ? 0L : new JSString(sourceURL).stringRef(),
                        startingLineNumber);
                if (jni.exception != 0) {
                    context.throwJSException(new JSException(new JSValue(jni.exception, context)));
                    jni.reference = make(context.ctxRef(), 0L);
                }
                valueRef = jni.reference;
                protect(context, valueRef);
            }
        });
        context.persistObject(this);
    }

    /**
     * Creates a new function object which calls method 'method' on this Java object.
     * Assumes the 'method' exists on this object and will throw a JSException if not found.  If
     * 'new' is called on this function, it will create a new 'instanceClass' instance.
     * In JS:
     * <pre>{@code
     * var f = function(a) { ... };
     * }
     * </pre>
     *
     * Example:
     * <pre>{@code
     *
     * public class FunctionObject extends JSObject {
     *     void function(int x) {
     *         getThis().property("varx",x);
     *     }
     * }
     *
     * public class MyFunc extends JSFunction {
     *     MyFunc(JSContext ctx) {
     *         super(ctx,
     *              FunctionObject.class.getMethod("function",int.class), // will call method 'function'
     *              JSObject.class                                // calling 'new' will create a JSObject
     *              new FunctionObject(ctx)                       // function will be called on FunctionObject
     *         );
     *     }
     * }
     * }
     * </pre>
     *
     * @param ctx    The JSContext to create the object in
     * @param method The method to invoke
     * @param instanceClass The class to be created on 'new' call
     * @param invokeObject  The object on which to invoke the method
     * @throws JSException
     * @since 3.0
     */
    public JSFunction(JSContext ctx,
                      final Method method,
                      final Class<? extends JSObject> instanceClass,
                      JSObject invokeObject) throws JSException {
        context = ctx;
        this.method = method;
        this.invokeObject = (invokeObject==null) ? this: invokeObject;
        context.sync(new Runnable() {
            @Override
            public void run() {
                valueRef = makeFunctionWithCallback(context.ctxRef(),
                        new JSString(method.getName()).stringRef());
                protect(context,valueRef);
                subclass = instanceClass;
            }
        });

        context.persistObject(this);
    }
    /**
     * Creates a new function object which calls method 'method' on this Java object.
     * Assumes the 'method' exists on this object and will throw a JSException if not found.  If
     * 'new' is called on this function, it will create a new 'instanceClass' instance.
     * In JS:
     * <pre>{@code
     * var f = function(a) { ... };
     * }
     * </pre>
     *
     * Example:
     * <pre>{@code
     * public class MyFunc extends JSFunction {
     *     MyFunc(JSContext ctx) {
     *         super(ctx,
     *              MyFunc.class.getMethod("function",int.class), // will call method 'function'
     *              JSObject.class                                // calling 'new' will create a JSObject
     *         );
     *     }
     *     void function(int x) {
     *         getThis().property("varx",x);
     *     }
     * }
     * }
     * </pre>
     *
     *
     * @param ctx    The JSContext to create the object in
     * @param method The method to invoke
     * @param instanceClass The class to be created on 'new' call
     * @throws JSException
     * @since 3.0
     */
    public JSFunction(JSContext ctx,
                      final Method method,
                      final Class<? extends JSObject> instanceClass) throws JSException {
        this(ctx,method,instanceClass,null);
    }
    /**
     * Creates a new function object which calls method 'method' on this Java object.
     * Assumes the 'method' exists on this object and will throw a JSException if not found.  If
     * 'new' is called on this function, it will create a new JSObject instance.
     * In JS:
     * <pre>{@code
     * var f = function(a) { ... };
     * }
     * </pre>
     *
     * Example:
     * <pre>{@code
     * public class MyFunc extends JSFunction {
     *     MyFunc(JSContext ctx) {
     *         super(ctx,
     *              MyFunc.class.getMethod("function",int.class), // will call method 'function'
     *              JSObject.class                                // calling 'new' will create a JSObject
     *         );
     *     }
     *     void function(int x) {
     *         getThis().property("varx",x);
     *     }
     * }
     * }
     * </pre>
     *
     *
     * @param ctx    The JSContext to create the object in
     * @param method The method to invoke
     * @throws JSException
     * @since 3.0
     */
    public JSFunction(JSContext ctx,
                      final Method method) throws JSException {
        this(ctx,method,JSObject.class);
    }
    /**
     * Creates a new function which basically does nothing.
     * In JS:
     * <pre>{@code
     * var f = function() {};
     * }
     * </pre>
     *
     * Example:
     * <pre>{@code
     * JSFunction f = new JSFunction(context);
     * }
     * </pre>
     *
     *
     * @param ctx    The JSContext to create the object in
     * @throws JSException
     * @since 3.0
     */
    public JSFunction(JSContext ctx) throws JSException {
        this(ctx,(Method)null);
    }

    /**
     * Creates a new function object which calls method 'methodName' on this Java object.
     * Assumes the 'methodName' method exists on this object and will throw a JSException if not found.  If
     * 'new' is called on this function, it will create a new 'instanceClass' instance.
     * In JS:
     * <pre>{@code
     * var f = function(a) { ... };
     * }
     * </pre>
     *
     * Example:
     * <pre>{@code
     *
     * public class FunctionObject extends JSObject {
     *     void function(int x) {
     *         getThis().property("varx",x);
     *     }
     * }
     *
     * public class MyFunc extends JSFunction {
     *     MyFunc(JSContext ctx) {
     *         super(ctx,
     *              "function",               // will call method 'function'
     *              JSObject.class            // calling 'new' will create a JSObject
     *              new FunctionObject(ctx)   // function will be called on FunctionObject
     *         );
     *     }
     * }
     * }
     * </pre>
     *
     * @param ctx    The JSContext to create the object in
     * @param methodName The method to invoke (searches for first instance)
     * @param instanceClass The class to be created on 'new' call
     * @param invokeObject  The object on which to invoke the method
     * @throws JSException
     * @since 3.0
     */
    public JSFunction(JSContext ctx,
                      final String methodName,
                      final Class<? extends JSObject> instanceClass,
                      JSObject invokeObject) throws JSException {
        context = ctx;
        this.invokeObject = (invokeObject==null) ? this : invokeObject;
        Method [] methods = this.invokeObject.getClass().getMethods();
        for (Method method : methods) {
            if (method.getName().equals(methodName)) {
                this.method = method;
                break;
            }
        }
        if (method == null) {
            try {
                if (methodName != null) {
                    throw new NoSuchMethodException();
                }
                method = JSFunction.class.getMethod("__nullFunc", (Class<?>)null);
            } catch (NoSuchMethodException e) {
                context.throwJSException(new JSException(context,e.getMessage()));
            }
        }
        context.sync(new Runnable() {
            @Override
            public void run() {
                valueRef = makeFunctionWithCallback(context.ctxRef(),
                        new JSString(method.getName()).stringRef());
                protect(context,valueRef);
                subclass = instanceClass;
            }
        });

        context.persistObject(this);
    }
    /**
     * Creates a new function object which calls method 'methodName' on this Java object.
     * Assumes the 'methodName' method exists on this object and will throw a JSException if not found.  If
     * 'new' is called on this function, it will create a new JSObject instance.
     * In JS:
     * <pre>{@code
     * var f = function(a) { ... };
     * }
     * </pre>
     *
     * Example:
     * <pre>{@code
     *
     * JSFunction f = new JSFunction(context,"function",JSObject.class) {
     *     void function(int x) {
     *         getThis().property("varx",x);
     *     }
     * }
     * }
     * </pre>
     *
     * @param ctx    The JSContext to create the object in
     * @param methodName The method to invoke (searches for first instance)
     * @param instanceClass The class to be created on 'new' call
     * @throws JSException
     * @since 3.0
     */
    public JSFunction(JSContext ctx,
                      final String methodName,
                      final Class<? extends JSObject> instanceClass) throws JSException {
        this(ctx,methodName,instanceClass,null);
    }
    /**
     * Creates a new function object which calls method 'methodName' on this Java object.
     * Assumes the 'methodName' method exists on this object and will throw a JSException if not found.  If
     * 'new' is called on this function, it will create a new JSObject instance.
     * In JS:
     * <pre>{@code
     * var f = function(a) { ... };
     * }
     * </pre>
     *
     * Example:
     * <pre>{@code
     *
     * JSFunction f = new JSFunction(context,"function") {
     *     void function(int x) {
     *         getThis().property("varx",x);
     *     }
     * }
     * }
     * </pre>
     *
     * @param ctx    The JSContext to create the object in
     * @param methodName The method to invoke (searches for first instance)
     * @throws JSException
     * @since 3.0
     */
    public JSFunction(JSContext ctx,
                      final String methodName) throws JSException {
        this(ctx,methodName,JSObject.class);
    }

    /**
     * Wraps an existing object as a JSFunction
     * @param objRef  The JavaScriptCore object reference
     * @param context The JSContext the object
     * @since 3.0
     */
    public JSFunction(final long objRef, JSContext context) {
        super(objRef, context);
    }

    /**
     * Calls this JavaScript function, similar to 'Function.call()' in JavaScript
     * @param thiz  The 'this' object on which the function operates, null if not on a constructor object
     * @param args  The argument list to be passed to the function
     * @return The JSValue returned by the function
     * @since 3.0
     * @throws JSException
     */
    public JSValue call(final JSObject thiz, final Object ... args) throws JSException {
        return apply(thiz,args);
    }

    /**
     * Calls this JavaScript function, similar to 'Function.apply() in JavaScript
     * @param thiz  The 'this' object on which the function operates, null if not on a constructor object
     * @param args  An array of arguments to be passed to the function
     * @return The JSValue returned by the function
     * @since 3.0
     * @throws JSException
     */
    public JSValue apply(final JSObject thiz, final Object [] args) throws JSException {
        JNIReturnClass runnable = new JNIReturnClass() {
            @Override
            public void run() {
                ArrayList<JSValue> largs = new ArrayList<JSValue>();
                if (args!=null) {
                    for (Object o: args) {
                        JSValue v;
                        if (o.getClass() == Void.class)
                            v = new JSValue(context);
                        else if (o instanceof JSValue)
                            v = (JSValue)o;
                        else if (o instanceof Object[])
                            v = new JSArray(context, (Object[])o);
                        else
                            v = new JSValue(context,o);
                        largs.add(v);
                    }
                }
                long [] valueRefs = new long[largs.size()];
                for (int i=0; i<largs.size(); i++) {
                    valueRefs[i] = largs.get(i).valueRef();
                }
                jni = callAsFunction(context.ctxRef(), valueRef, (thiz==null)?0L:thiz.valueRef(), valueRefs);
            }
        };
        context.sync(runnable);
        if (runnable.jni.exception!=0) {
            context.throwJSException(new JSException(new JSValue(runnable.jni.exception,context)));
            return new JSValue(context);
        }
        return new JSValue(runnable.jni.reference,context);
    }
    /**
     * Calls this JavaScript function with no args and 'this' as null
     * @return The JSValue returned by the function
     * @since 3.0
     * @throws JSException
     */
    public JSValue call() throws JSException {
        return call(null);
    }

    /**
     * Gets the prototype object, if it exists
     * @return A JSValue referencing the prototype object, or null if none
     * @since 3.0
     */
    public JSValue prototype() {
        JNIReturnClass runnable = new JNIReturnClass() {
            @Override
            public void run() {
                jni = new JNIReturnObject();
                jni.reference = getPrototype(context.ctxRef(), valueRef);
            }
        };
        context.sync(runnable);
        if (runnable.jni.reference==0) return null;
        return new JSValue(runnable.jni.reference,context);
    }
    /**
     * Sets the prototype object
     * @param proto The object defining the function prototypes
     * @since 3.0
     */
    public void prototype(final JSValue proto) {
        context.sync(new Runnable() {
            @Override
            public void run() {
                setPrototype(context.ctxRef(), valueRef, proto.valueRef());
            }
        });
    }


    @Override
    protected void finalize() throws Throwable {
        context.sync(new Runnable() {
            @Override
            public void run() {
                releaseFunctionWithCallback(context.ctxRef(), valueRef);
            }
        });
        super.finalize();
    }

    private long functionCallback(long ctxRef, long functionRef, long thisObjectRef,
                                  long argumentsValueRef[], long exceptionRefRef) {

        assert(ctxRef == context.ctxRef());
        assert(functionRef == valueRef);

        try {
            JSValue [] args = new JSValue[argumentsValueRef.length];
            for (int i=0; i<argumentsValueRef.length; i++) {
                JSObject obj = context.getObjectFromRef(argumentsValueRef[i],false);
                if (obj!=null) args[i] = obj;
                else args[i] = new JSValue(argumentsValueRef[i],context);
            }
            JSObject thiz = context.getObjectFromRef(thisObjectRef);
            JSValue value = function(thiz,args,invokeObject);
            setException(0L, exceptionRefRef);
            return value.valueRef();
        } catch (JSException e) {
            e.printStackTrace();
            setException(e.getError().valueRef(), exceptionRefRef);
            return 0L;
        }
    }

    protected JSValue function(JSObject thiz, JSValue [] args) throws JSException {
        return function(thiz,args,this);
    }

    protected JSValue function(JSObject thiz, JSValue [] args, final JSObject invokeObject) throws JSException {
        Class<?>[] pType  = method.getParameterTypes();
        Object [] passArgs = new Object[pType.length];
        for (int i=0; i<passArgs.length; i++) {
            if (i<args.length) {
                if (args[i]==null) passArgs[i] = null;
                else if (pType[i] == String.class) passArgs[i] = args[i].toString();
                else if (pType[i] == Double.class) passArgs[i] = args[i].toNumber();
                else if (pType[i] == double.class) passArgs[i] = args[i].toNumber();
                else if (pType[i] == Float.class) passArgs[i] = args[i].toNumber().floatValue();
                else if (pType[i] == float.class) passArgs[i] = args[i].toNumber().floatValue();
                else if (pType[i] == Integer.class) passArgs[i] = args[i].toNumber().intValue();
                else if (pType[i] == int.class) passArgs[i] = args[i].toNumber().intValue();
                else if (pType[i] == Long.class) passArgs[i] = args[i].toNumber().longValue();
                else if (pType[i] == long.class) passArgs[i] = args[i].toNumber().longValue();
                else if (pType[i] == Boolean.class) passArgs[i] = args[i].toBoolean();
                else if (pType[i] == boolean.class) passArgs[i] = args[i].toBoolean();
                else if (JSObject.class.isAssignableFrom(pType[i])) passArgs[i] =
                        pType[i].cast(args[i].toObject());
                else if (pType[i] == JSString.class) passArgs[i] = args[i].toJSString();
                else if (pType[i].isArray()) {
                    JSObject arr = args[i].toObject();
                    Integer length = 0;
                    if (arr.isArray()) {
                        length = arr.property("length").toNumber().intValue();
                    }
                    ArrayList<Object> objList = new ArrayList<Object>();
                    for (int j=0; j<length; j++) {
                        if (pType[i] == Boolean[].class || pType[i] == boolean[].class)
                            objList.add(arr.propertyAtIndex(j).toBoolean());
                        else if (pType[i] == Integer[].class || pType[i] == int[].class)
                            objList.add(arr.propertyAtIndex(j).toNumber().intValue());
                        else if (pType[i] == String[].class)
                            objList.add(arr.propertyAtIndex(j).toString());
                        else if (pType[i] == Long[].class || pType[i] == long[].class)
                            objList.add(arr.propertyAtIndex(j).toNumber().longValue());
                        else if (pType[i] == Double[].class || pType[i] == double[].class)
                            objList.add(arr.propertyAtIndex(j).toNumber());
                        else if (pType[i] == Float[].class || pType[i] == float[].class)
                            objList.add(arr.propertyAtIndex(j).toNumber());
                        else if (pType[i] == JSValue[].class)
                            objList.add(arr.propertyAtIndex(j));
                        else if (pType[i] == JSObject[].class)
                            objList.add(arr.propertyAtIndex(j).toObject());
                        else if (pType[i] == JSString[].class)
                            objList.add(arr.propertyAtIndex(j).toJSString());
                        else objList.add(null);
                    }
                    if (pType[i] == Boolean[].class || pType[i] == boolean[].class)
                        passArgs[i] = objList.toArray(new Boolean[objList.size()]);
                    else if (pType[i] == Integer[].class || pType[i] == int[].class)
                        passArgs[i] = objList.toArray(new Integer[objList.size()]);
                    else if (pType[i] == String[].class)
                        passArgs[i] = objList.toArray(new String[objList.size()]);
                    else if (pType[i] == Long[].class || pType[i] == long[].class)
                        passArgs[i] = objList.toArray(new Long[objList.size()]);
                    else if (pType[i] == Double[].class || pType[i] == double[].class)
                        passArgs[i] = objList.toArray(new Double[objList.size()]);
                    else if (pType[i] == Float[].class || pType[i] == float[].class)
                        passArgs[i] = objList.toArray(new Float[objList.size()]);
                    else if (pType[i] == JSValue[].class)
                        passArgs[i] = objList.toArray(new JSValue[objList.size()]);
                    else if (pType[i] == JSObject[].class)
                        passArgs[i] = objList.toArray(new JSObject[objList.size()]);
                    else if (pType[i] == JSString[].class)
                        passArgs[i] = objList.toArray(new JSString[objList.size()]);
                    else passArgs[i] = null;
                }
                else if (pType[i] == JSValue.class) passArgs[i] = args[i];
                else passArgs[i] = null;
            } else {
                passArgs[i] = null;
            }
        }
        JSValue returnValue;
        JSObject stack=null;
        try {
            stack = invokeObject.getThis();
            invokeObject.setThis(thiz);
            Object ret = method.invoke(invokeObject, passArgs);
            if (method.getReturnType() == Void.class || ret == null)
                returnValue = new JSValue(context);
            else if (ret instanceof JSValue)
                returnValue = (JSValue)ret;
            else if (ret instanceof Object[])
                returnValue = new JSArray(context, (Object[])ret);
            else
                returnValue = new JSValue(context,ret);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            context.throwJSException(new JSException(context, e.toString()));
            returnValue = new JSValue(context);
        } catch (IllegalAccessException e) {
            context.throwJSException(new JSException(context, e.toString()));
            returnValue = new JSValue(context);
        } finally {
            invokeObject.setThis(stack);
        }
        return returnValue;
    }

    private interface IJSObjectReturnClass {
        public JSObject execute();
    }
    private class JSObjectReturnClass implements Runnable, IJSObjectReturnClass {
        public JSObject object;
        @Override
        public void run() {
            object = execute();
        }
        @Override
        public JSObject execute() { return null; }
    }

    protected JSObject constructor(final JSValue [] args) throws JSException {
        JSObjectReturnClass runnable = new JSObjectReturnClass() {
            @Override
            public JSObject execute() {
                JSObject proto = context.getObjectFromRef(getPrototype(context.ctxRef(),valueRef()));
                try {
                    Constructor<?> defaultConstructor = subclass.getConstructor();
                    JSObject thiz = (JSObject) defaultConstructor.newInstance();
                    thiz.context = context;
                    thiz.valueRef = makeWithFinalizeCallback(context.ctxRef());
                    thiz.isInstanceOf = JSFunction.this;
                    function(thiz,args);
                    context.persistObject(thiz);
                    for (String prop : proto.propertyNames()) {
                        thiz.property(prop,proto.property(prop));
                    }
                    return thiz;
                } catch (NoSuchMethodException e) {
                    String error = e.toString() + "If " + subclass.getName() + " is an embedded " +
                            "class, did you specify it as 'static'?";
                    context.throwJSException(new JSException(context, error));
                } catch (InvocationTargetException e) {
                    String error = e.toString() + "; Did you remember to call super?";
                    context.throwJSException(new JSException(context, error));
                } catch (IllegalAccessException e) {
                    String error = e.toString() + "; Is your constructor public?";
                    context.throwJSException(new JSException(context, error));
                } catch (InstantiationException e) {
                    context.throwJSException(new JSException(context, e.toString()));
                }
                return new JSObject(context);
            }
        };
        context.sync(runnable);
        return runnable.object;
    }

    private long constructorCallback(long ctxRef, long constructorRef,
                                     long argumentsValueRef[], long exceptionRefRef) {

        assert(ctxRef == context.ctxRef());
        assert(constructorRef == this.valueRef);
        try {
            JSValue [] args = new JSValue[argumentsValueRef.length];
            for (int i=0; i<argumentsValueRef.length; i++) {
                JSObject obj = context.getObjectFromRef(argumentsValueRef[i],false);
                if (obj!=null) args[i] = obj;
                else args[i] = new JSValue(argumentsValueRef[i],context);
            }
            JSObject newObj = constructor(args);
            setException(0L, exceptionRefRef);
            return newObj.valueRef();
        } catch (JSException e) {
            setException(e.getError().valueRef(), exceptionRefRef);
            return 0L;
        }
    }
    private boolean hasInstanceCallback(long ctxRef, long constructorRef,
                                        long possibleInstanceRef, long exceptionRefRef) {
        assert(ctxRef == context.ctxRef());
        assert(constructorRef == this.valueRef);

        setException(0L, exceptionRefRef);

        JSValue instance = new JSValue(possibleInstanceRef, context);
        if (instance.isObject()) {
            return (instance.toObject()).isInstanceOf == this;
        }

        return false;
    }

    private Class<? extends JSObject> subclass = null;


    /**
     * Called only by convenience subclasses.  If you use
     * this, you must set context and valueRef yourself.  Also,
     * don't forget to call protect()!
     */
    protected JSFunction() {
    }

    private void finalizeCallback(long objectRef) {
        assert(objectRef == this.valueRef);
        context.finalizeObject(this);
    }

    private JSValue __nullFunc() {
        return new JSValue(context);
    }

    protected Method method = null;
    private JSObject invokeObject = null;
}
