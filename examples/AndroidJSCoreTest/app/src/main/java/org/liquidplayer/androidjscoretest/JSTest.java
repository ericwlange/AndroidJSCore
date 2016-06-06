package org.liquidplayer.androidjscoretest;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Handler;

import org.liquidplayer.webkit.javascriptcore.JSContext;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

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

    protected void tAssert(boolean condition, final String message) throws TestAssertException {
        if (!condition) {
            println(message + " [FAIL]");
            new Handler(activity.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {

                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);
                    alertDialogBuilder.setTitle("Test Failure");
                    alertDialogBuilder
                            .setMessage(message)
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // if this button is clicked, close
                                    // current activity
                                    activity.finish();
                                }
                            });

                    // create alert dialog
                    AlertDialog alertDialog = alertDialogBuilder.create();

                    // show it
                    alertDialog.show();
                }
            });

            throw new TestAssertException();
        } else {
            println(message + " [PASS]");
        }
    }

    private static ReferenceQueue<JSContext> rq = new ReferenceQueue<JSContext>();
    private static Map<WeakReference<JSContext>,String> whm = new HashMap<WeakReference<JSContext>,String>();

    public JSContext track(JSContext ctx, String name) {
        whm.put(new WeakReference<JSContext>(ctx,rq), name);
        return ctx;
    }
    public static String [] check() {
        Reference<? extends JSContext> p;
        while ((p = rq.poll()) != null) {
            whm.remove(p);
        }
        String [] zombies = new String[whm.size()];
        int i = 0;
        for (Reference<? extends JSContext> reference : whm.keySet()) {
            zombies[i++] = whm.get(reference);
        }
        return zombies;
    }

}
