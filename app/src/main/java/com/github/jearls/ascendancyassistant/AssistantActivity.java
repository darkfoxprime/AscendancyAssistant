package com.github.jearls.ascendancyassistant;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;

import android.widget.Toast;

public class AssistantActivity extends AppCompatActivity implements ResearchListFragment.OnResearchListFragmentInteractionListener {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("AssistantActivity", "onCreate()");
        setContentView(R.layout.activity_assistant);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

    }

    @Override
    public void onResearchListFragmentInteraction(Uri uri) {
        Log.d("AssistantActivity", "onResearchListFragmentInteraction()");
        Toast toast = Toast.makeText(this, uri.toString(), Toast.LENGTH_SHORT);
        toast.show();
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
            Log.d("AssistantActivity", "SectionsPagerAdapter.getItem(" + position + ")");
            switch (position) {
                case 0:
                    Log.d("AssistantActivity", "... returning a new ResearchListFragment instance");
                    return ResearchListFragment.newInstance();
            }
            Log.d("AssistantActivity", "... returning null");
            return null;
        }

        @Override
        public int getCount() {
            Log.d("AssistantActivity", "SectionsPagerAdapter.getCount()");
            Log.d("AssistantActivity", "... returning 1");
            return 1;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Log.d("AssistantActivity", "SectionsPagerAdapter.getPageTitle(" + position + ")");
            switch (position) {
                case 0:
                    Log.d("AssistantActivity", "... returning \"Research\"");
                    return "Research";
            }
            Log.d("AssistantActivity", "... returning null");
            return null;
        }
    }
}
