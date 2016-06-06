package org.liquidplayer.androidjscoretest;

import android.content.res.AssetManager;

import junit.framework.Test;

import org.json.JSONException;
import org.json.JSONObject;
import org.liquidplayer.webkit.javascriptcore.JSContext;
import org.liquidplayer.webkit.javascriptcore.JSException;
import org.liquidplayer.webkit.javascriptcore.JSFunction;
import org.liquidplayer.webkit.javascriptcore.JSObject;
import org.liquidplayer.webkit.javascriptcore.JSString;
import org.liquidplayer.webkit.javascriptcore.JSValue;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;

public class LargeScriptTest extends JSTest {
    public LargeScriptTest(MainActivity activity) {
        super(activity);
    }

    @Override
    public void run() throws TestAssertException {

        println("Long js file");
        println("------------");
        JSContext workerContext = track(new JSContext(),"LargeScriptTest:workerContext");
        patchContext(workerContext);
        try {
            AssetManager assetManager = activity.getAssets();
            String[] files = assetManager.list("");
            InputStream input = assetManager.open("myFormAPI.js");
            int size = input.available();
            byte[] buffer = new byte[size];
            input.read(buffer);
            input.close();

            // byte buffer into a string
            String text = new String(buffer);
            JSString jsString = new JSString(text);
            println("length of jsString = " + jsString.length());
            tAssert(new JSValue(workerContext,text).isStrictEqual(new JSValue(workerContext,jsString)),
                    "strings are equivalent");
            tAssert(new JSValue(workerContext,text).equals(text),
                    "strings are equivalent[2]");
            tAssert(jsString.equals(text),
                    "strings are equivalent[3]");

            workerContext.setExceptionHandler(new JSExeceptionHandler());

            workerContext.evaluateScript(text);
            tAssert(true,"Large JS execution");

            println("------------");

        } catch (IOException e) {
            e.printStackTrace();
            throw new TestAssertException();
        }

        workerContext.garbageCollect();

    }

    public void patchContext(JSContext pContext) throws TestAssertException
    {
        pContext.property("postMessage", new JSFunction(pContext,"postMessage") {
            public void postMessage(String message)
            {
                JSValue workerID = context.property("workerID");

                HashMap<String, Object> map = new HashMap<String, Object>();
                JSONObject jObject = null;
                try {
                    jObject = new JSONObject(message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Iterator<?> keys = jObject.keys();

                while( keys.hasNext() ) {
                    String key = (String) keys.next();
                    Object value = null;
                    try {
                        value = jObject.get(key);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    map.put(key, value);

                }

                if(!workerID.toString().equalsIgnoreCase("undefined")) {

                    map.put("workerID",workerID.toNumber().intValue());
                    //webWorker.sendToCallback(map.toString());

                }
            }
        });
        pContext.property("importScripts",new JSFunction(pContext,"importScripts") {
            public void importScripts(String file)
            {

            }
        });
        pContext.property("setTimeout", new JSFunction(pContext, "setTimeout") {
            public void setTimeout(JSValue jFunction , JSValue timeout) throws InterruptedException {
                Long mTimeout = timeout.toNumber().longValue();
                JSValue [] args = {};
                Thread.sleep(mTimeout);

                jFunction.toFunction().apply(null,args);

//      new android.os.Handler().postDelayed(
//        new Runnable() {
//          public void run() {
//            jFunction.toObject().
//              callAsFunction(null,null);
//          }
//        },
//        timeout.toNumber().longValue());
            }
        });

        pContext.property("console", new Console(pContext));
    }


    public interface IConsole {
        public void log(String message);
        public void error(String message);
    }

    public static class Console extends JSObject implements IConsole {
        public Console(JSContext ctx) {
            super(ctx, IConsole.class);
        }

        @Override
        public void log(String message) {
            android.util.Log.d("console.log()", message);
        }

        @Override
        public void error(String message) {
            android.util.Log.e("console.error()", message);
        }
    }

    public class JSExeceptionHandler implements JSContext.IJSExceptionHandler
    {
        @Override
        public void handle(JSException exception)
        {
            android.util.Log.e("JS Exception", exception.getMessage());
            exception.printStackTrace();
        }

    }
}
