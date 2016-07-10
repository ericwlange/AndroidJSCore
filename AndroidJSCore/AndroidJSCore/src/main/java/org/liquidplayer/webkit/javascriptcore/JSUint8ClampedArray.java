package org.liquidplayer.webkit.javascriptcore;

/**
 * Created by Eric on 6/26/16.
 */
public class JSUint8ClampedArray extends JSTypedArray<Byte> {
    public JSUint8ClampedArray(JSContext ctx, int length) {
        super(ctx,length,"Uint8ClampedArray",Byte.class);
    }
    public JSUint8ClampedArray(JSTypedArray tarr) {
        super(tarr,"Uint8ClampedArray",Byte.class);
    }
    public JSUint8ClampedArray(JSContext ctx, Object object) {
        super(ctx,object,"Uint8ClampedArray",Byte.class);
    }
    public JSUint8ClampedArray(JSArrayBuffer buffer, int byteOffset, int length) {
        super(buffer,byteOffset,length,"Uint8ClampedArray",Byte.class);
    }
    public JSUint8ClampedArray(JSArrayBuffer buffer, int byteOffset) {
        super(buffer,byteOffset,"Uint8ClampedArray",Byte.class);
    }
    public JSUint8ClampedArray(JSArrayBuffer buffer) {
        super(buffer,"Uint8ClampedArray",Byte.class);
    }
    public JSUint8ClampedArray(long valueRef, JSContext ctx) {
        super(valueRef,ctx,Byte.class);
    }
}
