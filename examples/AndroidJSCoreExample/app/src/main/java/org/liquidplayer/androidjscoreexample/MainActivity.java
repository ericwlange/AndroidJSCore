package org.liquidplayer.androidjscoreexample;

import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;
import android.widget.Toast;

import org.liquidplayer.webkit.javascriptcore.JSException;

public class MainActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener {

    public static final String ARG_OBJECT = "object";
    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    static JSCoreExampleFragment [] fragments = new JSCoreExampleFragment[4];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        ViewPager viewPager = (ViewPager) findViewById(R.id.container);
        viewPager.setAdapter(sectionsPagerAdapter);
        viewPager.addOnPageChangeListener(this);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

            }
        });

    }

    @Override
    public void onPageSelected(int position) {
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        if (positionOffset == 0.0) {
            if (fragments[position] == null) return;
            fragments[position].example.run();
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static class JSCoreExampleFragment extends Fragment {

        public IExample example = null;

        public static JSCoreExampleFragment newInstance(int i) {
            JSCoreExampleFragment fragment = new JSCoreExampleFragment();
            Bundle args = new Bundle();
            // Our object is just an integer :-P
            args.putInt(ARG_OBJECT, i);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater,
                                 ViewGroup container, Bundle savedInstanceState) {
            // The last two arguments ensure LayoutParams are inflated
            // properly.
            View rootView = inflater.inflate(
                    R.layout.example_fragment, container, false);
            Bundle args = getArguments();

            try {
                ExampleContext ctx = new ExampleContext(
                        (TextView)rootView.findViewById(R.id.textview));
                switch(args.getInt(ARG_OBJECT)) {
                    case 1: example = new OwenMatthewsExample(ctx); break;
                    case 2: example = new SharingFunctionsExample(ctx); break;
                    case 3: example = new AsyncExample(ctx); break;
                    case 4: example = new ExceptionHandlingExample(ctx); break;
                }
                fragments[args.getInt(ARG_OBJECT)-1] = this;
            } catch (JSException e) {
                int duration = Toast.LENGTH_LONG;
                Toast toast = Toast.makeText(getActivity().getApplicationContext(),
                        e.toString(), duration);
                toast.show();
            }

            return rootView;
        }

    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return JSCoreExampleFragment.newInstance(position + 1);
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
        }

        @Override
        public int getCount() {
            // Show 4 total pages.
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Owen Matthews";
                case 1:
                    return "Sharing Functions";
                case 2:
                    return "Async Callbacks";
                case 3:
                    return "Exception Handling";
            }
            return null;
        }
    }
}
