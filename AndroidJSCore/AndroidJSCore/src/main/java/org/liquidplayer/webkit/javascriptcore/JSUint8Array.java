package org.liquidplayer.webkit.javascriptcore;

/**
 * Created by Eric on 6/22/16.
 */
public class JSUint8Array extends JSTypedArray<Byte> {
    public JSUint8Array(JSContext ctx, int length) {
        super(ctx,length,"Uint8Array",Byte.class);
    }
    public JSUint8Array(JSTypedArray tarr) {
        super(tarr,"Uint8Array",Byte.class);
    }
    public JSUint8Array(JSContext ctx, Object object) {
        super(ctx,object,"Uint8Array",Byte.class);
    }
    public JSUint8Array(JSArrayBuffer buffer, int byteOffset, int length) {
        super(buffer,byteOffset,length,"Uint8Array",Byte.class);
    }
    public JSUint8Array(JSArrayBuffer buffer, int byteOffset) {
        super(buffer,byteOffset,"Uint8Array",Byte.class);
    }
    public JSUint8Array(JSArrayBuffer buffer) {
        super(buffer,"Uint8Array",Byte.class);
    }
    public JSUint8Array(long valueRef, JSContext ctx) {
        super(valueRef,ctx,Byte.class);
    }
}
