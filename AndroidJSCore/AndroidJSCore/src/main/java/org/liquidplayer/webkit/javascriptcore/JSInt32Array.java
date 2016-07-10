package org.liquidplayer.webkit.javascriptcore;

/**
 * Created by Eric on 6/26/16.
 */
public class JSInt32Array extends JSTypedArray<Integer> {
    public JSInt32Array(JSContext ctx, int length) {
        super(ctx,length,"Int32Array",Integer.class);
    }
    public JSInt32Array(JSTypedArray tarr) {
        super(tarr,"Int32Array",Integer.class);
    }
    public JSInt32Array(JSContext ctx, Object object) {
        super(ctx,object,"Int32Array",Integer.class);
    }
    public JSInt32Array(JSArrayBuffer buffer, int byteOffset, int length) {
        super(buffer,byteOffset,length,"Int32Array",Integer.class);
    }
    public JSInt32Array(JSArrayBuffer buffer, int byteOffset) {
        super(buffer,byteOffset,"Int32Array",Integer.class);
    }
    public JSInt32Array(JSArrayBuffer buffer) {
        super(buffer,"Int32Array",Integer.class);
    }
    public JSInt32Array(long valueRef, JSContext ctx) {
        super(valueRef,ctx,Integer.class);
    }
}
