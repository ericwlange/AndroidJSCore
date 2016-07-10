package org.liquidplayer.webkit.javascriptcore;

/**
 * Created by Eric on 6/26/16.
 */
public class JSInt16Array extends JSTypedArray<Short> {
    public JSInt16Array(JSContext ctx, int length) {
        super(ctx,length,"Int16Array",Short.class);
    }
    public JSInt16Array(JSTypedArray tarr) {
        super(tarr,"Int16Array",Short.class);
    }
    public JSInt16Array(JSContext ctx, Object object) {
        super(ctx,object,"Int16Array",Short.class);
    }
    public JSInt16Array(JSArrayBuffer buffer, int byteOffset, int length) {
        super(buffer,byteOffset,length,"Int16Array",Short.class);
    }
    public JSInt16Array(JSArrayBuffer buffer, int byteOffset) {
        super(buffer,byteOffset,"Int16Array",Short.class);
    }
    public JSInt16Array(JSArrayBuffer buffer) {
        super(buffer,"Int16Array",Short.class);
    }
    public JSInt16Array(long valueRef, JSContext ctx) {
        super(valueRef,ctx,Short.class);
    }
}
