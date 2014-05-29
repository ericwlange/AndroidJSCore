package org.liquidplayer.webkit.javascriptcore;

import android.test.ActivityInstrumentationTestCase2;

/**
 * This is a simple framework for a test of an Application.  See
 * {@link android.test.ApplicationTestCase ApplicationTestCase} for more information on
 * how to write and extend Application tests.
 * <p/>
 * To run this test, you can type:
 * adb shell am instrument -w \
 * -e class org.liquidplayer.webkit.javascriptcore.JSValue \
 * org.liquidplayer.webkit.javascriptcore.tests/android.test.InstrumentationTestRunner
 */
public class JSValueTest extends ActivityInstrumentationTestCase2<JSValue> {

	public JSValueTest() {
		super("org.liquidplayer.webkit.javascriptcore.jsvalue", JSValue.class);
	}

}
