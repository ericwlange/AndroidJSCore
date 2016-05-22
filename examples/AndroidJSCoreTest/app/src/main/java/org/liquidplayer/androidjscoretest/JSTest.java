package org.liquidplayer.androidjscoretest;

import org.liquidplayer.webkit.javascriptcore.JSContext;

/**
 * Created by Eric on 5/5/16.
 */
public class JSTest {
    final MainActivity activity;

    public JSTest(MainActivity activity) {
        this.activity = activity;
    }

    public void println(String message) {
        android.util.Log.d("console", message);
        activity.println(message);
    }

    public static String getCurrentClassAndMethodNames() {
        final StackTraceElement e = Thread.currentThread().getStackTrace()[3];
        return e.getMethodName();
    }

    public void run() throws TestAssertException {
        println("Nothing to do!");
    }

    protected void tAssert(boolean condition, String message) throws TestAssertException {
        if (!condition) {
            println(message + " [FAIL]");
            throw new TestAssertException();
        } else {
            println(message + " [PASS]");
        }
    }
}
