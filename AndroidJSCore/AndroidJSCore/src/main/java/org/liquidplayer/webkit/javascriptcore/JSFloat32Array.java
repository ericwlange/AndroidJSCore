package org.liquidplayer.webkit.javascriptcore;

/**
 * Created by Eric on 6/26/16.
 */
public class JSFloat32Array extends JSTypedArray<Float> {
    public JSFloat32Array(JSContext ctx, int length) {
        super(ctx,length,"Float32Array",Float.class);
    }
    public JSFloat32Array(JSTypedArray tarr) {
        super(tarr,"Float32Array",Float.class);
    }
    public JSFloat32Array(JSContext ctx, Object object) {
        super(ctx,object,"Float32Array",Float.class);
    }
    public JSFloat32Array(JSArrayBuffer buffer, int byteOffset, int length) {
        super(buffer,byteOffset,length,"Float32Array",Float.class);
    }
    public JSFloat32Array(JSArrayBuffer buffer, int byteOffset) {
        super(buffer,byteOffset,"Float32Array",Float.class);
    }
    public JSFloat32Array(JSArrayBuffer buffer) {
        super(buffer,"Float32Array",Float.class);
    }
    public JSFloat32Array(long valueRef, JSContext ctx) {
        super(valueRef,ctx,Float.class);
    }
}
