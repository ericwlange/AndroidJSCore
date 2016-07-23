package org.liquidplayer.webkit.javascriptcore;

import org.junit.Test;

import java.nio.charset.Charset;

import static org.junit.Assert.*;

public class JSStringTest {

    @Test
    @SuppressWarnings("ObjectEqualsNull,EqualsBetweenInconvertibleTypes")
    public void testJSString() throws Exception {
        JSContext context = new JSContext();

        JSString jsString = new JSString("Hello World; 你好，世界");
        assertEquals(jsString.toString(),"Hello World; 你好，世界");
        assertEquals(jsString.length().intValue(),18);

        assertFalse(jsString.equals(null));
        assertEquals(jsString,jsString);

        JSString jsString1 = new JSString("foo");
        assertFalse(jsString.equals(jsString1));
        assertFalse(jsString.equals("bar"));

        JSString jsString2 = new JSString("10");
        assertFalse(jsString2.equals(new JSValue(context,10)));
        assertTrue(jsString2.equals(new JSValue(context,"10")));

        assertFalse(jsString.equals(10));
    }
}