package org.liquidplayer.webkit.javascriptcore;

/**
 * Created by Eric on 6/26/16.
 */
public class JSUint32Array extends JSTypedArray<Integer> {
    public JSUint32Array(JSContext ctx, int length) {
        super(ctx,length,"Uint32Array",Integer.class);
    }
    public JSUint32Array(JSTypedArray tarr) {
        super(tarr,"Uint32Array",Integer.class);
    }
    public JSUint32Array(JSContext ctx, Object object) {
        super(ctx,object,"Uint32Array",Integer.class);
    }
    public JSUint32Array(JSArrayBuffer buffer, int byteOffset, int length) {
        super(buffer,byteOffset,length,"Uint32Array",Integer.class);
    }
    public JSUint32Array(JSArrayBuffer buffer, int byteOffset) {
        super(buffer,byteOffset,"Uint32Array",Integer.class);
    }
    public JSUint32Array(JSArrayBuffer buffer) {
        super(buffer,"Uint32Array",Integer.class);
    }
    public JSUint32Array(long valueRef, JSContext ctx) {
        super(valueRef,ctx,Integer.class);
    }
}
