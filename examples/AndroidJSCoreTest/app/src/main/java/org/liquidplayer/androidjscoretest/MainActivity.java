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
                new LargeScriptTest(MainActivity.this).run();
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
