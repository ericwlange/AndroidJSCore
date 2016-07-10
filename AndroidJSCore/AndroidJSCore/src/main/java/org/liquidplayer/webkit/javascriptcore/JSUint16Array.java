package org.liquidplayer.webkit.javascriptcore;

/**
 * Created by Eric on 6/26/16.
 */
public class JSUint16Array extends JSTypedArray<Short> {
    public JSUint16Array(JSContext ctx, int length) {
        super(ctx,length,"Uint16Array",Short.class);
    }
    public JSUint16Array(JSTypedArray tarr) {
        super(tarr,"Uint16Array",Short.class);
    }
    public JSUint16Array(JSContext ctx, Object object) {
        super(ctx,object,"Uint16Array",Short.class);
    }
    public JSUint16Array(JSArrayBuffer buffer, int byteOffset, int length) {
        super(buffer,byteOffset,length,"Uint16Array",Short.class);
    }
    public JSUint16Array(JSArrayBuffer buffer, int byteOffset) {
        super(buffer,byteOffset,"Uint16Array",Short.class);
    }
    public JSUint16Array(JSArrayBuffer buffer) {
        super(buffer,"Uint16Array",Short.class);
    }
    public JSUint16Array(long valueRef, JSContext ctx) {
        super(valueRef,ctx,Short.class);
    }
}
