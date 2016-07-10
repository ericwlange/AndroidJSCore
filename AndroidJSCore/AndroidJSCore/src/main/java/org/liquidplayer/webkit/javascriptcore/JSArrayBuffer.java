package org.liquidplayer.webkit.javascriptcore;

/**
 * Created by Eric on 6/20/16.
 */
public class JSArrayBuffer {
    public JSArrayBuffer(JSContext ctx, int length) {
        JSFunction constructor = new JSFunction(ctx,"_ArrayBuffer",new String[] {"length"},
                "return new ArrayBuffer(length);",
                null, 0);
        mArrayBuffer = constructor.call(null,length).toObject();
    }
    public JSArrayBuffer(JSObject buffer) {
        mArrayBuffer = buffer;
    }
    private final JSObject mArrayBuffer;

    public JSObject getJSObject() {
        return mArrayBuffer;
    }

    public int length() {
        return mArrayBuffer.property("length").toNumber().intValue();
    }

    public static boolean isView(JSValue arg) {
        return arg.getContext().property("ArrayBuffer").toObject().property("isView").toFunction()
                .call(null,arg).toBoolean();
    }

    public static JSArrayBuffer transfer(JSArrayBuffer oldBuffer, int newByteLength) {
        return new JSArrayBuffer(oldBuffer.getJSObject().getContext().property("ArrayBuffer").toObject()
                .property("transfer").toFunction().call(null,oldBuffer,newByteLength).toObject());
    }
    public static JSArrayBuffer transfer(JSArrayBuffer oldBuffer) {
        return new JSArrayBuffer(oldBuffer.getJSObject().getContext().property("ArrayBuffer").toObject()
                .property("transfer").toFunction().call(null,oldBuffer).toObject());
    }

    public JSArrayBuffer slice(int begin, int end) {
        return new JSArrayBuffer(
                mArrayBuffer.property("slice").toFunction().call(null,begin,end).toObject());
    }
    public JSArrayBuffer slice(int begin) {
        return new JSArrayBuffer(
                mArrayBuffer.property("slice").toFunction().call(null,begin).toObject());
    }
}
