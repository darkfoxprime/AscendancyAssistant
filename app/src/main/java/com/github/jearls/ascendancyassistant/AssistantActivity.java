package com.github.jearls.ascendancyassistant;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;

import java.lang.reflect.Method;

public class AssistantActivity extends AppCompatActivity implements ResearchListFragment.OnResearchListFragmentInteractionListener {
    private static final boolean DEBUG         = false;
    private static final String  TAG           = "AssistantActivity";
    private static final int     DEFAULT_THEME = R.style.AppLight;
    private static final String  PREF_THEME    = "currentTheme";

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
    /**
     * Preferences instance
     */
    private SharedPreferences    mPrefs;
    /**
     * The current theme.
     */
    private int mCurrentTheme = DEFAULT_THEME;

    private static void debug(String tag, String msg) {
        System.err.println(tag + ": " + msg);
        Log.d(tag, msg);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (DEBUG)
            debug(TAG, "> onCreate(" + savedInstanceState + ")");
        mPrefs = getSharedPreferences(getApplication().getPackageName(), MODE_PRIVATE);
        mCurrentTheme = mPrefs.getInt(PREF_THEME, DEFAULT_THEME);
        setTheme(mCurrentTheme);
        setContentView(R.layout.activity_assistant);

        Toolbar toolbar = (Toolbar) findViewById(R.id.app_toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        if (DEBUG)
            debug(TAG, "< onCreate(" + savedInstanceState + ")");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.app_menu_toolbar, menu);
        try {
            Method m = menu.getClass().getDeclaredMethod(
                    "setOptionalIconsVisible", Boolean.TYPE);
            m.setAccessible(true);
            m.invoke(menu, true);
        } catch (Exception e) {
            Log.e(TAG, "onPrepareOptionsPanel...unable to set icons for overflow menu", e);
        }
        for (int i = 0; i < menu.size(); i += 1) {
            MenuItem menuItem = menu.getItem(i);
            Drawable icon     = menuItem.getIcon();
            if (icon != null) {
                Resources.Theme currentTheme  = getTheme();
                TypedValue      colorResource = new TypedValue();
                currentTheme.resolveAttribute(R.attr.colorText, colorResource, true);
                int iconTint;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    iconTint = getResources().getColor(colorResource.resourceId, currentTheme);
                } else {
                    //noinspection deprecation
                    iconTint = getResources().getColor(colorResource.resourceId);
                }
                icon.setColorFilter(iconTint, PorterDuff.Mode.SRC_IN);
            }
        }
        return true;
    }

//    @Override
//    protected boolean onPrepareOptionsPanel(View view, Menu menu) {
//        if (menu != null) {
//            if (menu.getClass().equals(MenuBuilder.class)) {
//                try {
//                    Method m = menu.getClass().getDeclaredMethod(
//                            "setOptionalIconsVisible", Boolean.TYPE);
//                    m.setAccessible(true);
//                    m.invoke(menu, true);
//                } catch (Exception e) {
//                    Log.e(TAG, "onPrepareOptionsPanel...unable to set icons for overflow menu", e);
//                }
//            }
//        }
//        return super.onPrepareOptionsPanel(view, menu);
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_theme_dark:
                mCurrentTheme = R.style.AppDark;
                mPrefs.edit().putInt(PREF_THEME, mCurrentTheme).apply();
                recreate();
                return true;
            case R.id.menu_theme_light:
                mCurrentTheme = R.style.AppLight;
                mPrefs.edit().putInt(PREF_THEME, mCurrentTheme).apply();
                recreate();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResearchListFragmentInteraction(ResearchProject researchProject, int action) {

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
                debug(TAG, "> SectionsPagerAdapter.getItem(" + position + ")");
            switch (position) {
                case 0:
                    if (DEBUG)
                        debug(TAG, "< SectionsPagerAdapter.getItem(" + position + ") returning a new ResearchListFragment instance");
                    return ResearchListFragment.newInstance();
            }
            if (DEBUG)
                debug(TAG, "< SectionsPagerAdapter.getItem(" + position + ") returning null");
            return null;
        }

        @Override
        public int getCount() {
            if (DEBUG)
                debug(TAG, "> SectionsPagerAdapter.getCount()");
            if (DEBUG)
                debug(TAG, "< SectionsPagerAdapter.getCount() returning 1");
            return 1;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (DEBUG)
                debug(TAG, "> SectionsPagerAdapter.getPageTitle(" + position + ")");
            CharSequence title = null;
            switch (position) {
                case 0:
                    title = "Research";
            }
            if (DEBUG)
                debug(TAG, "< SectionsPagerAdapter.getPageTitle(" + position + ") returning " + title);
            return title;
        }
    }
}
