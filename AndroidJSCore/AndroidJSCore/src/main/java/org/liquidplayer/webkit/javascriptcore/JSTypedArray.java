package org.liquidplayer.webkit.javascriptcore;

/**
 * Created by Eric on 6/20/16.
 */
public class JSTypedArray<T> extends JSBaseArray<T> {
    protected JSTypedArray(JSContext ctx, int length, String jsConstructor, Class<T> cls) {
        super(cls);
        context = ctx;
        JSFunction constructor = new JSFunction(context,"_" + jsConstructor,new String[] {"length"},
                "return new " + jsConstructor + "(length);",
                null, 0);
        JSValue newArray = constructor.call(null,length);
        valueRef = newArray.valueRef();
        protect(context.ctxRef(), valueRef);
        context.persistObject(this);
    }

    protected JSTypedArray(JSTypedArray typedArray, String jsConstructor, Class<T> cls) {
        super(cls);
        context = typedArray.getContext();
        JSFunction constructor = new JSFunction(context,"_" + jsConstructor,new String[] {"tarr"},
                "return new " + jsConstructor + "(tarr);",
                null, 0);
        JSValue newArray = constructor.call(null,typedArray);
        valueRef = newArray.valueRef();
        protect(context.ctxRef(), valueRef);
        context.persistObject(this);
    }

    protected JSTypedArray(JSContext ctx, Object object, String jsConstructor, Class<T> cls) {
        super(cls);
        context = ctx;
        JSFunction constructor = new JSFunction(context,"_" + jsConstructor,new String[] {"obj"},
                "return new " + jsConstructor + "(obj);",
                null, 0);
        JSValue newArray = constructor.call(null,object);
        valueRef = newArray.valueRef();
        protect(context.ctxRef(), valueRef);
        context.persistObject(this);
    }

    public JSTypedArray(JSArrayBuffer buffer, int byteOffset, int length, String jsConstructor,
                        Class<T> cls) {
        super(cls);
        JSFunction constructor = new JSFunction(context,"_" + jsConstructor,
                new String[] {"buffer,byteOffset,length"},
                "return new " + jsConstructor + "(buffer,byteOffset,length);",
                null, 0);
        JSValue newArray = constructor.call(null,buffer.getJSObject(),byteOffset,length);
        valueRef = newArray.valueRef();
        protect(context.ctxRef(), valueRef);
        context.persistObject(this);
    }
    public JSTypedArray(JSArrayBuffer buffer, int byteOffset, String jsConstructor,
                        Class<T> cls) {
        super(cls);
        JSFunction constructor = new JSFunction(context,"_" + jsConstructor,
                new String[] {"buffer,byteOffset"},
                "return new " + jsConstructor + "(buffer,byteOffset);",
                null, 0);
        JSValue newArray = constructor.call(null,buffer.getJSObject(),byteOffset);
        valueRef = newArray.valueRef();
        protect(context.ctxRef(), valueRef);
        context.persistObject(this);
    }
    public JSTypedArray(JSArrayBuffer buffer, String jsConstructor, Class<T> cls) {
        super(cls);
        JSFunction constructor = new JSFunction(context,"_" + jsConstructor,
                new String[] {"buffer"},
                "return new " + jsConstructor + "(buffer);",
                null, 0);
        JSValue newArray = constructor.call(null,buffer.getJSObject());
        valueRef = newArray.valueRef();
        protect(context.ctxRef(), valueRef);
        context.persistObject(this);
    }
    protected JSTypedArray(long objRef, JSContext ctx, Class<T> cls) {
        super(objRef,ctx,cls);
    }

    public static JSTypedArray from(JSObject obj) {
        JSTypedArray arr = null;
        if (isTypedArray(obj)) {
            switch(obj.property("constructor").toObject().property("name").toString()) {
                case "Int8Array":
                    arr = new JSInt8Array(obj.valueRef(),obj.getContext()); break;
                case "Uint8Array":
                    arr = new JSUint8Array(obj.valueRef(),obj.getContext()); break;
                case "Uint8ClampedArray":
                    arr = new JSUint8ClampedArray(obj.valueRef(),obj.getContext()); break;
                case "Int16Array":
                    arr = new JSInt16Array(obj.valueRef(),obj.getContext()); break;
                case "Uint16Array":
                    arr = new JSUint16Array(obj.valueRef(),obj.getContext()); break;
                case "Int32Array":
                    arr = new JSInt32Array(obj.valueRef(),obj.getContext()); break;
                case "Uint32Array":
                    arr = new JSUint32Array(obj.valueRef(),obj.getContext()); break;
                case "Float32Array":
                    arr = new JSFloat32Array(obj.valueRef(),obj.getContext()); break;
                case "Float64Array":
                    arr = new JSFloat64Array(obj.valueRef(),obj.getContext()); break;
            }
        }
        if (arr == null) throw new JSException(obj.getContext(),"Object not a typed array");
        arr.protect(arr.getContext().ctxRef(),arr.valueRef);
        return arr;
    }

    public static boolean isTypedArray(JSValue value) {
        if (!value.isObject()) return false;
        JSObject obj = value.toObject();
        if (obj.hasProperty("BYTES_PER_ELEMENT") && obj.hasProperty("length") &&
                obj.hasProperty("byteOffset") && obj.hasProperty("byteLength"))
            return true;
        return false;
    }

    public JSArrayBuffer buffer() {
        return new JSArrayBuffer(property("buffer").toObject());
    }

    public int byteLength() {
        return property("byteLength").toNumber().intValue();
    }

    public int byteOffset() {
        return property("byteOffset").toNumber().intValue();
    }

    @Override
    protected JSBaseArray toSuperArray(JSValue value) {
        return null;
    }

    @Override
    protected JSValue arrayElement(final int index) {
        JSFunction getElement = new JSFunction(context,"_getElement",new String[]{"thiz","index"},
                "thiz[index]",
                null, 0);
        return getElement.call(null,this,index);
    }

    @Override
    protected void arrayElement(final int index, final T value) {
        JSFunction setElement = new JSFunction(context,"_getElement",
                new String[]{"thiz","index","value"},
                "thiz[index] = value",
                null, 0);
        setElement.call(null,this,index,value);
    }

}
