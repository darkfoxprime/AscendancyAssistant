package com.github.jearls.ascendancyassistant;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Toast;

public class AssistantActivity extends AppCompatActivity implements ResearchListFragment.OnResearchListFragmentInteractionListener {
    private static final boolean DEBUG = false;
    /**
     * The {@link android.support.v4.view.PagerAdapter} that will
     * provide fragments for each of the sections. We use a {@link
     * FragmentPagerAdapter} derivative, which will keep every loaded
     * fragment in memory. If this becomes too memory intensive, it may
     * be best to switch to a {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    @SuppressWarnings("FieldCanBeLocal")
    private SectionsPagerAdapter mSectionsPagerAdapter;
    /**
     * The {@link ViewPager} that will host the section contents.
     */
    @SuppressWarnings("FieldCanBeLocal")
    private ViewPager mViewPager;

    private static void debug(String tag, String msg) {
        System.err.println(tag + ": " + msg);
        Log.d(tag, msg);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (DEBUG)
            debug("AssistantActivity", "> onCreate(" + savedInstanceState + ")");
        setContentView(R.layout.activity_assistant);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        if (DEBUG)
            debug("AssistantActivity", "< onCreate(" + savedInstanceState + ")");
    }

    @Override
    public void onResearchListFragmentInteraction(Uri uri) {
        if (DEBUG)
            debug("AssistantActivity", "> onResearchListFragmentInteraction(" + uri + ")");
        Toast toast = Toast.makeText(this, uri.toString(), Toast.LENGTH_SHORT);
        toast.show();
        if (DEBUG)
            debug("AssistantActivity", "< onResearchListFragmentInteraction(" + uri + ")");
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment
     * corresponding to one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (DEBUG)
                debug("AssistantActivity", "> SectionsPagerAdapter.getItem(" + position + ")");
            switch (position) {
                case 0:
                    if (DEBUG)
                        debug("AssistantActivity", "< SectionsPagerAdapter.getItem(" + position + ") returning a new ResearchListFragment instance");
                    return ResearchListFragment.newInstance();
            }
            if (DEBUG)
                debug("AssistantActivity", "< SectionsPagerAdapter.getItem(" + position + ") returning null");
            return null;
        }

        @Override
        public int getCount() {
            if (DEBUG)
                debug("AssistantActivity", "> SectionsPagerAdapter.getCount()");
            if (DEBUG)
                debug("AssistantActivity", "< SectionsPagerAdapter.getCount() returning 1");
            return 1;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (DEBUG)
                debug("AssistantActivity", "> SectionsPagerAdapter.getPageTitle(" + position + ")");
            CharSequence title = null;
            switch (position) {
                case 0:
                    title = "Research";
            }
            if (DEBUG)
                debug("AssistantActivity", "< SectionsPagerAdapter.getPageTitle(" + position + ") returning " + title);
            return title;
        }
    }
}
