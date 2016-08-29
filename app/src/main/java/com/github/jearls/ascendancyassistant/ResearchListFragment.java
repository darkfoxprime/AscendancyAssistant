package com.github.jearls.ascendancyassistant;

import android.app.NotificationManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass. Activities that contain this
 * fragment must implement the {@link OnResearchListFragmentInteractionListener}
 * interface to handle interaction events. Use the {@link
 * ResearchListFragment#newInstance} factory method to create an
 * instance of this fragment.
 */
public class ResearchListFragment extends Fragment implements Toolbar.OnMenuItemClickListener, AdapterView.OnItemSelectedListener, ResearchListAdapter.OnResearchListSelectionChange
        , ResearchProject.OnResearchCompletedChangeListener, ResearchProject.OnResearchPathChangeListener {
    private static final int                    NOTIFICATION_LOADING_ALERT = 1;
    private static final boolean                DEBUG                      = true;
    private static final String                 TAG                        = "ResearchListFragment";
    private static final int                    FLAG_ONLY_ONE              = 0b00000001;
    private static final int                    FLAG_ALL_COMPLETE          = 0b00000010;
    private static final int                    FLAG_ALL_GOAL              = 0b00000100;
    private static final int                    FLAG_SOME_COMPLETE         = 0b00001000;
    private static final int                    FLAG_SOME_GOAL             = 0b00010000;
    private static final int                    FLAG_FIRST_GOAL            = 0b00100000;
    private static final int                    FLAG_LAST_GOAL             = 0b01000000;
    private static final int                    FLAG_BITS                  = 0b11111111;
    private static final int                    MASK_SHIFT                 = 8;
    private static final int                    MASK_ONLY_ONE              = FLAG_ONLY_ONE << MASK_SHIFT;
    private static final int                    MASK_ALL_COMPLETE          = FLAG_ALL_COMPLETE << MASK_SHIFT;
    private static final int                    MASK_ALL_GOAL              = FLAG_ALL_GOAL << MASK_SHIFT;
    private static final int                    MASK_SOME_COMPLETE         = FLAG_SOME_COMPLETE << MASK_SHIFT;
    private static final int                    MASK_SOME_GOAL             = FLAG_SOME_GOAL << MASK_SHIFT;
    private static final int                    MASK_FIRST_GOAL            = FLAG_FIRST_GOAL << MASK_SHIFT;
    private static final int                    MASK_LAST_GOAL             = FLAG_LAST_GOAL << MASK_SHIFT;
    private final        Map<MenuItem, Integer> menuItemVisibilityFlags    = new HashMap<>();
    // the fragment's state
    @SuppressWarnings("FieldCanBeLocal")
    private              ResearchListAdapter    mAdapter                   = null;
    private              ResearchProjectDbModel mDbModel                   = null;
    private OnResearchListFragmentInteractionListener mListener;
    private int mCurrentFlags = 0;
    //    private int mCountComplete = 0;
//    private int mCountGoal = 0;
//    private int mCountInPath = 0;
    private int mCountTotal   = 0;

    public ResearchListFragment() {
        if (DEBUG)
            debug("> ResearchListFragment()");
        if (DEBUG)
            debug("< ResearchListFragment()");
    }

    private static void error(String msg) {
        error(msg, null);
    }

    private static void error(String msg, Throwable throwable) {
        System.err.println(TAG + ": " + msg);
        if (throwable == null) {
            Log.e(TAG, msg);
        } else {
            throwable.printStackTrace(System.err);
            Log.e(TAG, msg, throwable);
        }
    }

    private static void debug(String msg) {
        debug(msg, null);
    }

    private static void debug(String msg, Throwable throwable) {
        System.err.println(TAG + ": " + msg);
        if (throwable == null) {
            Log.d(TAG, msg);
        } else {
            throwable.printStackTrace(System.err);
            Log.d(TAG, msg, throwable);
        }
    }

    /**
     * Use this factory method to create a new instance of this fragment
     * using the provided parameters.
     *
     * @return A new instance of fragment ResearchListFragment.
     */
    public static ResearchListFragment newInstance() {
        if (DEBUG) debug("> newInstance()");
        //noinspection UnnecessaryLocalVariable
        ResearchListFragment fragment = new ResearchListFragment();
//        Bundle args = new Bundle();
//        args.putSerializable(ARG_RESEARCH, research);
//        fragment.setArguments(args);
        if (DEBUG)
            debug("< newInstance() returning " + fragment);
        return fragment;
    }

    @SuppressWarnings("EmptyMethod")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (DEBUG)
            debug("> onCreate(" + savedInstanceState + ")");
        if (DEBUG)
            debug("< onCreate(" + savedInstanceState + ")");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (DEBUG)
            debug("> onCreateView(" + inflater + ", " + container + ", " + savedInstanceState + ")");
        View     rootView = inflater.inflate(R.layout.fragment_research, container, false);
        ListView listView = (ListView) rootView.findViewById(R.id.research_list_view);
        listView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
        this.mAdapter = new ResearchListAdapter(listView);
        this.mAdapter.setOnResearchListSelectionChangeListener(this);
        listView.setAdapter(this.mAdapter);
        listView.setOnItemClickListener(this.mAdapter);
        listView.setItemsCanFocus(false);
        Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.research_toolbar);
        toolbar.inflateMenu(R.menu.research_menu_toolbar);
        toolbar.setOnMenuItemClickListener(this);
        Spinner spinner = (Spinner) toolbar.findViewById(R.id.research_list_showing);
        spinner.setOnItemSelectedListener(this);
        onCreateToolbarMenu(toolbar.getMenu());
        if (DEBUG)
            debug("< onCreateView(" + inflater + ", " + container + ", " + savedInstanceState + ") returning " + rootView);
        return rootView;
    }

    @Override
    public void onResearchListSelectionChange(ResearchProject researchProject, boolean selected) {
        if (DEBUG)
            debug("> onResearchListSelectionChange(" + researchProject + ", " + selected + ")");
        mCountTotal = mAdapter.getSelectedResearch().size();
        int     countGoal     = 0;
        int     countComplete = 0;
        int     countInPath   = 0;
        boolean firstGoal     = false;
        boolean lastGoal      = false;
        for (ResearchProject selectedResearchProject : mAdapter.getSelectedResearch()) {
            if (selectedResearchProject.isGoal()) {
                countGoal += 1;
                if (selectedResearchProject.getGoalNumber() == 1) {
                    firstGoal = true;
                }
                if (selectedResearchProject.getGoalNumber() == ResearchProject.getResearchGoals().size()) {
                    lastGoal = true;
                }
            }
            if (selectedResearchProject.isCompleted()) {
                countComplete += 1;
            }
            if (selectedResearchProject.isInPath()) {
                countInPath += 1;
            }
        }
        if (DEBUG)
            debug("* onResearchListSelectionChange: mCountTotal=" + mCountTotal + " countGoal=" + countGoal + " countComplete=" + countComplete + " countInPath=" + countInPath + " firstGoal=" + firstGoal + " lastGoal=" + lastGoal);
        if (DEBUG)
            debug("* onResearchListSelectionChange: before adjustment: mCurrentFlags=" + Integer.toBinaryString(mCurrentFlags));
        mCurrentFlags = 0;
        if (mCountTotal == 1) mCurrentFlags |= FLAG_ONLY_ONE;
        if (countComplete == mCountTotal)
            mCurrentFlags |= FLAG_ALL_COMPLETE;
        if (countComplete > 0) mCurrentFlags |= FLAG_SOME_COMPLETE;
        if (countGoal == mCountTotal) mCurrentFlags |= FLAG_ALL_GOAL;
        if (countGoal > 0) mCurrentFlags |= FLAG_SOME_GOAL;
        if (firstGoal) mCurrentFlags |= FLAG_FIRST_GOAL;
        if (lastGoal) mCurrentFlags |= FLAG_LAST_GOAL;
        if (DEBUG)
            debug("* onResearchListSelectionChange: after adjustment: mCurrentFlags=" + Integer.toBinaryString(mCurrentFlags));
        updateMenuVisibility();
        if (DEBUG)
            debug("< onResearchListSelectionChange(" + researchProject + ", " + selected + ")");
    }

    public void onCreateToolbarMenu(Menu menu) {
        try {
            Method m = menu.getClass().getDeclaredMethod(
                    "setOptionalIconsVisible", Boolean.TYPE);
            m.setAccessible(true);
            m.invoke(menu, true);
        } catch (Exception e) {
            error("onPrepareOptionsPanel...unable to set icons for overflow menu", e);
        }
        menuItemVisibilityFlags.clear();
        for (int i = 0; i < menu.size(); i += 1) {
            MenuItem menuItem = menu.getItem(i);
            Drawable icon     = menuItem.getIcon();
            if (icon != null) {
                Resources.Theme currentTheme  = getContext().getTheme();
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
            int visibility = 0;
            switch (menuItem.getItemId()) {
                case R.id.menu_add_goal:
                    // no selected projects may be complete
                    // no selected projects may be a goal
                    visibility = MASK_SOME_COMPLETE | MASK_SOME_GOAL;
                    break;
                case R.id.menu_complete_project:
                case R.id.menu_complete_project_recursively:
                    // no selected projects may be complete
                    visibility = MASK_SOME_COMPLETE;
                    break;
                case R.id.menu_remove_goal:
                    // no selected projects may be complete
                    // all selected projects must be a goal
                    visibility = MASK_SOME_COMPLETE | MASK_ALL_GOAL | FLAG_ALL_GOAL;
                    break;
                case R.id.menu_move_goal_up:
                    visibility = MASK_FIRST_GOAL;
                case R.id.menu_move_goal_down:
                    if (visibility == 0) visibility = MASK_LAST_GOAL;
                    // no selected projects may be complete
                    // all selected projects must be a goal
                    // at most one project may be selected
                    visibility |= MASK_SOME_COMPLETE | MASK_ALL_GOAL | MASK_ONLY_ONE | FLAG_ALL_GOAL | FLAG_ONLY_ONE;
                    break;
                default:
                    // always visible
                    visibility = 0;
            }
            menuItemVisibilityFlags.put(menuItem, visibility);
        }
        updateMenuVisibility();
    }

    private void updateMenuVisibility() {
        for (Map.Entry<MenuItem, Integer> menuEntry : menuItemVisibilityFlags.entrySet()) {
            if (DEBUG)
                debug("mCurrentFlags=" + Integer.toBinaryString(mCurrentFlags) + " menuItem=" + menuEntry.getKey() + " visibilityFlags=" + Integer.toBinaryString(menuEntry.getValue()));
            boolean visible = (mCountTotal > 0) && ((mCurrentFlags & (menuEntry.getValue() >> MASK_SHIFT)) == (menuEntry.getValue() & FLAG_BITS));
            menuEntry.getKey().setVisible(visible).setEnabled(visible);
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if (DEBUG) debug("> onMenuItemClick(" + item + ")");
        boolean processed = false;
        switch (item.getItemId()) {
            case R.id.menu_add_goal:
                ResearchProject.enterBatchTriggerMode();
                for (ResearchProject researchProject : mAdapter.getSelectedResearch()) {
                    researchProject.addToGoals();
                }
                ResearchProject.recomputePath();
                ResearchProject.leaveBatchTriggerMode();
                processed = true;
                break;
            case R.id.menu_remove_goal:
                ResearchProject.enterBatchTriggerMode();
                for (ResearchProject researchProject : mAdapter.getSelectedResearch()) {
                    researchProject.removeFromGoals();
                }
                ResearchProject.recomputePath();
                ResearchProject.leaveBatchTriggerMode();
                processed = true;
                break;
            case R.id.menu_move_goal_up:
            case R.id.menu_move_goal_down:
                ResearchProject.enterBatchTriggerMode();
                ResearchProject researchProject = mAdapter.getSelectedResearch().iterator().next();
                int goalNumber = researchProject.getGoalNumber();
                if (DEBUG)
                    debug("* onMenuItemClick: researchProject=" + researchProject + " goal number = " + goalNumber);
                researchProject.removeFromGoals();
                if (item.getItemId() == R.id.menu_move_goal_up) {
                    researchProject.addToGoals(goalNumber - 1);
                } else {
                    researchProject.addToGoals(goalNumber + 1);
                }
                ResearchProject.recomputePath();
                if (DEBUG)
                    debug("* onMenuItemClick: researchProject=" + researchProject + " new goal number = " + researchProject.getGoalNumber());
                ResearchProject.leaveBatchTriggerMode();
                processed = true;
                break;
            default:
                Toast.makeText(getContext(), "selected menu item " + item.toString(), Toast.LENGTH_SHORT).show();
        }
        if (processed) mAdapter.clearSelectedResearch();
        if (DEBUG) debug("< onMenuItemClick(" + item + ")");
        return true;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        // assumption is that switcher items are in same order as
        // ResearchListAdapter.SHOW_* constants
        mAdapter.setMode(pos + 1);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (DEBUG)
            debug("> onAttach(" + context + ")");
        if (context instanceof OnResearchListFragmentInteractionListener) {
            mListener = (OnResearchListFragmentInteractionListener) context;
            mDbModel = new ResearchProjectDbModel(getContext());
            mDbModel.open();
            mDbModel.fetchResearchTree();
            if (ResearchProject.getAllResearchProjects().isEmpty()) {
                try {
                    ResearchProject.loadXmlResearchTree(this.getResources().getXml(R.xml.default_research_tree));
                } catch (ResearchProject.ResearchXmlParserException e) {
                    NotificationCompat.Builder alert = new NotificationCompat.Builder(this.getContext()).setSmallIcon(android.R.drawable.ic_dialog_alert).setContentTitle("Unable to load default research tree").setContentText(e.getLocalizedMessage());
                    ((NotificationManager) this.getContext().getSystemService(Context.NOTIFICATION_SERVICE)).notify(NOTIFICATION_LOADING_ALERT, alert.build());
                }
                mDbModel.createResearchTree();
            }
            mDbModel.close();
            try {
                ResearchProject.loadXmlInitialResearch(this.getResources().getXml(R.xml.initial_research_list));
            } catch (ResearchProject.ResearchXmlParserException e) {
                NotificationCompat.Builder alert = new NotificationCompat.Builder(this.getContext()).setSmallIcon(android.R.drawable.ic_dialog_alert).setContentTitle("Unable to load initial research list").setContentText(e.getLocalizedMessage());
                ((NotificationManager) this.getContext().getSystemService(Context.NOTIFICATION_SERVICE)).notify(NOTIFICATION_LOADING_ALERT, alert.build());
            }
            for (ResearchProject researchProject : ResearchProject.getAllResearchProjects()) {
                researchProject.addResearchCompletedChangeListener(this);
            }
            ResearchProject.addResearchPathChangeListener(this);
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnResearchListFragmentInteractionListener");
        }
        if (DEBUG)
            debug("< onAttach(" + context + ")");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (DEBUG) debug("> onDetach()");
        ResearchProject.reset();
        mListener = null;
        if (DEBUG) debug("< onDetach()");
    }

    @Override
    public void onResearchCompletedChange(ResearchProject project, boolean completed) {
        mDbModel.open();
        mDbModel.updateResearchProject(project);
        mDbModel.close();
    }

    @Override
    public void onResearchPathChange(List<ResearchProject> pathList, ResearchProject added, ResearchProject removed) {
        mDbModel.open();
        if (removed != null) {
            mDbModel.updateResearchProject(removed);
        }
        for (ResearchProject researchProject : pathList) {
            mDbModel.updateResearchProject(researchProject);
        }
        mDbModel.close();
    }

    @Override
    public void onResearchGoalsChange(List<ResearchProject> goalsList, ResearchProject added, ResearchProject removed) {
        // same functionality as onResearchPathChange
        onResearchPathChange(goalsList, added, removed);
    }

    /**
     * This interface must be implemented by activities that contain
     * this fragment to allow an interaction in this fragment to be
     * communicated to the activity and potentially other fragments
     * contained in that activity.
     * <p/>
     * See the Android Training lesson <a href= "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    @SuppressWarnings({"UnusedParameters", "EmptyMethod"})
    public interface OnResearchListFragmentInteractionListener {
        int ACTION_COMPLETE              = R.id.menu_complete_project;
        int ACTION_COMPLETE_DEPENDENCIES = R.id.menu_complete_project_recursively;
        int ACTION_ADD                   = R.id.menu_add_goal;
        int ACTION_REMOVE                = R.id.menu_remove_goal;
        int ACTION_MOVE_UP               = R.id.menu_move_goal_up;
        int ACTION_MOVE_DOWN             = R.id.menu_move_goal_down;

        // TODO: Update argument type and sName
        void onResearchListFragmentInteraction(ResearchProject researchProject, int action);
    }
}
