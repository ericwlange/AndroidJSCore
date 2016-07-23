package org.liquidplayer.webkit.javascriptcore;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class JSIteratorTest {

    @Test
    public void testJSIterator() throws Exception {
        JSContext context = new JSContext();
        JSArray<Integer> array = new JSArray<>(context, Arrays.asList(1,2,3,4,5), Integer.class);
        JSArray.ValuesIterator iterator = array.values();

        int i=0;
        for(; iterator.hasNext(); i++) {
            assertEquals(iterator.next(),array.get(i));
            boolean exception = false;
            try {
                iterator.remove();
            } catch (UnsupportedOperationException e) {
                exception = true;
            } finally {
                assertTrue(exception);
            }
        }
        assertEquals(i,array.size());
    }
}