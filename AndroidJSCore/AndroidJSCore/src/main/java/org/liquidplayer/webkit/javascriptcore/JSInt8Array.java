package org.liquidplayer.webkit.javascriptcore;

/**
 * Created by Eric on 6/20/16.
 */
public class JSInt8Array extends JSTypedArray<Byte> {
    public JSInt8Array(JSContext ctx, int length) {
        super(ctx,length,"Int8Array",Byte.class);
    }
    public JSInt8Array(JSTypedArray tarr) {
        super(tarr,"Int8Array",Byte.class);
    }
    public JSInt8Array(JSContext ctx, Object object) {
        super(ctx,object,"Int8Array",Byte.class);
    }
    public JSInt8Array(JSArrayBuffer buffer, int byteOffset, int length) {
        super(buffer,byteOffset,length,"Int8Array",Byte.class);
    }
    public JSInt8Array(JSArrayBuffer buffer, int byteOffset) {
        super(buffer,byteOffset,"Int8Array",Byte.class);
    }
    public JSInt8Array(JSArrayBuffer buffer) {
        super(buffer,"Int8Array",Byte.class);
    }
    public JSInt8Array(long valueRef, JSContext ctx) {
        super(valueRef,ctx,Byte.class);
    }
}
