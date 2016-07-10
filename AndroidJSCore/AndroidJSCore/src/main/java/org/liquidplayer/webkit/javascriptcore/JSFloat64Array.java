package org.liquidplayer.webkit.javascriptcore;

/**
 * Created by Eric on 6/26/16.
 */
public class JSFloat64Array extends JSTypedArray<Double> {
    public JSFloat64Array(JSContext ctx, int length) {
        super(ctx,length,"Float64Array",Double.class);
    }
    public JSFloat64Array(JSTypedArray tarr) {
        super(tarr,"Float64Array",Double.class);
    }
    public JSFloat64Array(JSContext ctx, Object object) {
        super(ctx,object,"Float64Array",Double.class);
    }
    public JSFloat64Array(JSArrayBuffer buffer, int byteOffset, int length) {
        super(buffer,byteOffset,length,"Float64Array",Double.class);
    }
    public JSFloat64Array(JSArrayBuffer buffer, int byteOffset) {
        super(buffer,byteOffset,"Float64Array",Double.class);
    }
    public JSFloat64Array(JSArrayBuffer buffer) {
        super(buffer,"Float64Array",Double.class);
    }
    public JSFloat64Array(long valueRef, JSContext ctx) {
        super(valueRef,ctx,Double.class);
    }
}
