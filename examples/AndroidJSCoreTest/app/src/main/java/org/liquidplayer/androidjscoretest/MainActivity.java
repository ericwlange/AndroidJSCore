package org.liquidplayer.androidjscoretest;

import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import org.liquidplayer.webkit.javascriptcore.JSContext;

public class MainActivity extends AppCompatActivity {

    private TextView   mTextView;
    private String     mScrollBuffer;
    private ScrollView mScrollView;

    public class Tests extends AsyncTask<Void,Void,Boolean> {
        @Override public
        Boolean doInBackground(Void ...params) {

            try {
                new JSContextTest(MainActivity.this).run();
                new JSValueTest(MainActivity.this).run();
                new JSObjectTest(MainActivity.this).run();
                new JSStringTest(MainActivity.this).run();
                new JSFunctionTest(MainActivity.this).run();
                new JSArrayTest(MainActivity.this).run();
                new JSMapTest(MainActivity.this).run();
                new JSDateTest(MainActivity.this).run();
                new JSONTest(MainActivity.this).run();
                new JSRegExpTest(MainActivity.this).run();
                new JSErrorTest(MainActivity.this).run();
                new JSExceptionTest(MainActivity.this).run();
                new LargeScriptTest(MainActivity.this).run();

                new JSTest(MainActivity.this) {
                    @Override public void run() throws TestAssertException {
                        Runtime.getRuntime().gc();
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        } finally {
                            String [] zombies = JSTest.check();
                            String list = "";
                            for (String zombie : zombies) {
                                if (!list.equals("")) list += ", ";
                                list += zombie;
                            }
                            println("There are " + zombies.length + " contexts still alive [" + list + "]");
                            tAssert(zombies.length == 0, "Check for memory leaks");
                        }
                    }
                }.run();

            } catch (TestAssertException e) {

            }


            return true;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_scrolling);

        mTextView = (TextView) findViewById(R.id.textView);
        mScrollView = (ScrollView) findViewById(R.id.scrollView);
        mScrollBuffer = "";

        new Tests().execute();
    }

    public synchronized void println(final String str) {
        new Handler(getMainLooper()).post(new Runnable() {
            @Override public void run() {
                mScrollBuffer += str + "\n";
                mTextView.setText(mScrollBuffer);
                mScrollView.fullScroll(View.FOCUS_DOWN);
            }
        });
    }
}
