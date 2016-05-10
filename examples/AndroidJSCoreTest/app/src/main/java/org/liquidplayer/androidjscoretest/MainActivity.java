package org.liquidplayer.androidjscoretest;

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
    private JSContext  mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_scrolling);

        mTextView = (TextView) findViewById(R.id.textView);
        mScrollView = (ScrollView) findViewById(R.id.scrollView);
        mScrollBuffer = "";

        mContext = new JSContext();

        try {
            new JSContextTest(this).run();
            new JSValueTest(this).run();
        } catch (TestAssertException e) {

        }
    }

    public synchronized void println(String str) {
        mScrollBuffer += str + "\n";
        mTextView.setText(mScrollBuffer);
        mScrollView.fullScroll(View.FOCUS_DOWN);
    }

    public JSContext getMainJSContext() {
        return mContext;
    }
}
