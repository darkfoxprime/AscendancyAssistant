package com.github.jearls.ascendancyassistant;

import android.content.Context;
import android.content.res.Resources;
import android.util.SparseBooleanArray;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * An adapter to manage a set of ResearchProject objects being displayed
 * in a list using the research_project_list_item layout.
 *
 * @author Johnson Earls
 */
public class ResearchListAdapter extends BaseAdapter implements ResearchProject.OnResearchCompletedChangeListener, ResearchProject.OnResearchChangeListener, ResearchProject.OnResearchPathChangeListener, AdapterView.OnItemClickListener {
    public static final  int     SHOW_ALL         = 1;
    public static final  int     SHOW_AVAILABLE   = 2;
    public static final  int     SHOW_RESEARCHING = 3;
    public static final  int     SHOW_COMPLETED   = 4;
    public static final  int     SHOW_INITIAL     = 5;
    private static final boolean DEBUG = false;
    private final List<ResearchProject>        sResearchOrder;
    private final Set<ResearchProject>         sVisibleResearch;
    private final List<ResearchProject>        sSelectedResearch;
    private final Map<ResearchProject, String> sResearchIconNames;
    //    private final Map<ResearchProject, View> sResearchViews;
    private final ListView                     sListView;
    private final LayoutInflater               sInflater;
    private       int                          mMode;
    private OnResearchListSelectionChange mResearchListSelectionChangeListener = null;

    public ResearchListAdapter(ListView listView) {
        if (DEBUG) debug("> ResearchListAdapter(" + listView + ")");
        this.sListView = listView;
        this.mMode = SHOW_ALL;
        this.sResearchOrder = new ArrayList<>();
        this.sVisibleResearch = new HashSet<>();
        this.sSelectedResearch = new ArrayList<>();
        this.sResearchIconNames = new HashMap<>();
//        this.sResearchViews = new HashMap<>();
        this.sInflater = (LayoutInflater) this.sListView.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ResearchProject.addResearchChangeListener(this);
        ResearchProject.addResearchPathChangeListener(this);
        for (ResearchProject project : ResearchProject.getAllResearchProjects()) {
            project.addResearchCompletedChangeListener(this);
        }
        this.update();

        if (DEBUG) debug("< ResearchListAdapter(" + listView + ")");
    }

    private static void debug(String msg) {
        System.err.println("ResearchListAdapter: " + msg);
        //Log.d("ResearchListAdapter", msg);
    }

    public void setOnResearchListSelectionChangeListener(OnResearchListSelectionChange listener) {
        mResearchListSelectionChangeListener = listener;
    }

    public Collection<ResearchProject> getSelectedResearch() {
        return Collections.unmodifiableCollection(sSelectedResearch);
    }

    public void clearSelectedResearch() {
        if (DEBUG) debug("> clearSelectedResearch()");
        while (!sSelectedResearch.isEmpty()) {
            ResearchProject researchProject = sSelectedResearch.get(0);
            this.setSelected(researchProject, false);
        }
        if (DEBUG) debug("< clearSelectedResearch()");
    }

    public boolean isSelected(ResearchProject researchProject) {
        return sSelectedResearch.contains(researchProject);
    }

    public void setSelected(ResearchProject researchProject, boolean selected) {
        if (DEBUG)
            debug("> setSelected(" + researchProject + ", " + selected + ")");
        if (DEBUG)
            debug("* setSelected: visible = " + sVisibleResearch.contains(researchProject));
        if (sVisibleResearch.contains(researchProject)) {
            int pos = sResearchOrder.indexOf(researchProject);
            if (DEBUG) debug("* setSelected: position = " + pos);
            if (sListView.isItemChecked(pos) != selected) {
                if (DEBUG)
                    debug("* setSelected: changing state to " + selected);
                sListView.setItemChecked(pos, selected);
                onItemClick(sListView, null, pos, 0L);
            }
        }
        if (DEBUG)
            debug("< setSelected(" + researchProject + ", " + selected + ")");
    }

    public void updateResearchProjectView(ResearchProject researchProject) {
        if (DEBUG)
            debug("> updateResearchProjectView(" + researchProject + ")");
        View view = sListView.getChildAt(sResearchOrder.indexOf(researchProject) - sListView.getFirstVisiblePosition());
        updateResearchProjectView(researchProject, view);
        if (DEBUG)
            debug("< updateResearchProjectView(" + researchProject + ")");
    }

    public void updateResearchProjectView(ResearchProject researchProject, View view) {
        if (DEBUG)
            debug("> updateResearchProjectView(" + researchProject + ", " + view + ")");
        if (DEBUG) debug("* updateResearchProjectView: view = " + view);
        if (view != null) {
            ImageView icon     = (ImageView) view.findViewById(R.id.research_project_list_item_icon);
            String    iconName = sResearchIconNames.get(researchProject);
            int       backgroundAttr;
            if (sSelectedResearch.contains(researchProject)) {
                iconName = "research_selected_" + iconName;
                backgroundAttr = R.attr.selectedBackground;
            } else {
                iconName = "research_unselected_" + iconName;
                backgroundAttr = R.attr.normalBackground;
            }
            TypedValue      typedValue = new TypedValue();
            Resources.Theme theme      = view.getContext().getTheme();
            theme.resolveAttribute(backgroundAttr, typedValue, true);
            int backgroundColor = typedValue.data;
            if (DEBUG)
                debug("* updateResearchProjectView: iconName=\"" + iconName + "\" backgroundColor=\"" + backgroundColor + "\" (" + typedValue.toString() + ")");
            icon.setImageResource(sListView.getResources().getIdentifier(iconName, "drawable", this.getClass().getPackage().getName()));
            view.setBackgroundColor(backgroundColor);
            TextView path = (TextView) view.findViewById(R.id.research_project_list_item_path_order);
            if (DEBUG)
                debug(("* updateResearchProjectView: path = " + researchProject.getPathNumber() + " goal = " + researchProject.getGoalNumber()));
            if (researchProject.isInPath()) {
                path.setVisibility(View.VISIBLE);
                if (researchProject.isGoal()) {
                    path.setBackgroundResource(R.drawable.goal_circle);
                } else {
                    path.setBackgroundResource(R.drawable.path_circle);
                }
                path.setText(String.format(Locale.getDefault(), "%d", researchProject.getPathNumber()));
            } else {
                path.setVisibility(View.GONE);
            }

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            icon.setImageDrawable(sContext.getResources().getDrawable(sContext.getResources().getIdentifier(iconName, "drawable", this.getClass().getPackage().getName()), sContext.getTheme()));
//        } else {
//            //noinspection deprecation
//            icon.setImageDrawable(sContext.getResources().getDrawable(sContext.getResources().getIdentifier(iconName, "drawable", this.getClass().getPackage().getName())));
//        }
        }
        if (DEBUG)
            debug("< updateResearchProjectView(" + researchProject + ", " + view + ")");
    }

    @Override
    public View getView(int i, View view, ViewGroup parentView) {
        if (DEBUG)
            debug("> getView(" + i + ", " + view + ", " + parentView + ")");
//        Iterator<Map.Entry<ResearchProject, View>> researchViewIterator = sResearchViews.entrySet().iterator();
//        while (researchViewIterator.hasNext()) {
//            Map.Entry<ResearchProject, View> researchView = researchViewIterator.next();
//            if (researchView.getValue().equals(view)) {
//                if (DEBUG)
//                    debug("* getView: removing view " + researchView.getValue() + " for project " + researchView.getKey());
//                researchViewIterator.remove();
//            }
//        }
        if (view == null) {
            view = sInflater.inflate(R.layout.research_project_list_item, parentView, false);
        }
        TextView        title   = (TextView) view.findViewById(R.id.research_project_list_item_title);
        TextView        techs   = (TextView) view.findViewById(R.id.research_project_list_item_technologies);
        ResearchProject project = (ResearchProject) this.getItem(i);
        title.setText(project.getName());
        StringBuffer technologies = new StringBuffer();
        for (Iterator<String> ti = project.getTechnologies(); ti.hasNext(); ) {
            String technology = ti.next();
            if (technologies.length() > 0) {
                technologies.append(", ");
            }
            technologies.append(technology);
        }
        techs.setText(technologies);
        String iconName = project.getName().toLowerCase().replace(' ', '_').replaceAll("[^a-z_]", "");
        sResearchIconNames.put(project, iconName);
//        if (DEBUG)
//            debug("* getView: storing view " + view + " for project " + project);
//        sResearchViews.put(project, view);
        this.updateResearchProjectView(project, view);

        if (DEBUG)
            debug("< getView(" + i + ", " + view + ", " + parentView + ") returning " + (view));
        return view;
    }

    @SuppressWarnings("unused")
    public int getMode() {
        if (DEBUG) debug("> getMode()");
        if (DEBUG) debug("< getMode() returning " + mMode);
        return mMode;
    }

    @SuppressWarnings("unused")
    public void setMode(int mode) {
        if (DEBUG) debug("> setMode(" + mode + ")");
        if (mode != this.mMode) {
            if (DEBUG)
                debug("* setMode: " + this.mMode + " -> " + mode);
            this.mMode = mode;
            this.update();
        }
        if (DEBUG) debug("< setMode(" + mode + ")");
    }

    private void update() {
        if (DEBUG) debug("> update()");
        boolean               updated         = false;
        List<ResearchProject> initialResearch = ResearchProject.getInitialResearch();
        for (ResearchProject project : ResearchProject.getAllResearchProjects()) {
            boolean projectShouldBeDisplayed = true;
            switch (this.mMode) {
                case SHOW_AVAILABLE:
                    projectShouldBeDisplayed = !(project.isCompleted());
                    break;
                case SHOW_COMPLETED:
                    projectShouldBeDisplayed = project.isCompleted();
                    break;
                case SHOW_RESEARCHING:
                    projectShouldBeDisplayed = project.isInPath();
                    break;
                case SHOW_INITIAL:
                    projectShouldBeDisplayed = !(project.isCompleted()) && initialResearch.contains(project);
                    break;
            }
            if (DEBUG)
                debug("* update: " + project + " shouldBeDisplayed " + projectShouldBeDisplayed + " mode " + mMode);
            if (projectShouldBeDisplayed) {
                if (!this.sVisibleResearch.contains(project)) {
                    this.sVisibleResearch.add(project);
                    updated = true;
                }
            } else {
                if (this.sVisibleResearch.contains(project)) {
                    this.sVisibleResearch.remove(project);
                    updated = true;
                }
            }
        }
        if (updated) {
            this.sResearchOrder.clear();
            if (this.mMode == SHOW_INITIAL) {
                for (ResearchProject project : initialResearch) {
                    if (this.sVisibleResearch.contains(project)) {
                        this.sResearchOrder.add(project);
                    }
                }
            } else {
                for (ResearchProject project : this.sVisibleResearch) {
                    int i = this.sResearchOrder.size();
                    switch (this.mMode) {
                        case SHOW_RESEARCHING:
                            // sort based on research project's path order
                            int o = project.getPathNumber();
                            while ((i > 0) && (this.sResearchOrder.get(i - 1).getPathNumber() > o)) {
                                i -= 1;
                            }
                            break;
                        default:
                            // sort based on research project's name
                            while ((i > 0) && (this.sResearchOrder.get(i - 1).getName().compareTo(project.getName()) > 0)) {
                                i -= 1;
                            }
                    }
                    this.sResearchOrder.add(i, project);
                }
            }
            this.notifyDataSetChanged();
        }
        if (DEBUG) debug("< update()");
    }

    @Override
    public void notifyDataSetChanged() {
        if (DEBUG) debug("> notifyDataSetChanged");
        super.notifyDataSetChanged();
        if (DEBUG) debug("< notifyDataSetChanged");
    }

    // OnItemSelectedListener methods

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
        if (DEBUG)
            debug("> onItemClick(" + parent + ", " + view + ", " + pos + ", " + id + ")");
        SparseBooleanArray checked = ((ListView) parent).getCheckedItemPositions();
        for (int i = 0; i < checked.size(); i += 1) {
            ResearchProject researchProject = this.sResearchOrder.get(checked.keyAt(i));
            boolean         currentState    = sSelectedResearch.contains(researchProject);
            if (DEBUG)
                debug("* onItemClick(): researchProject=" + researchProject + " checked=" + checked.valueAt(i) + " currentState=" + currentState);
            if (checked.valueAt(i) != currentState) {
                if (checked.valueAt(i)) {
                    sSelectedResearch.add(researchProject);
                } else {
                    sSelectedResearch.remove(researchProject);
                }
                updateResearchProjectView(researchProject);
                if (mResearchListSelectionChangeListener != null) {
                    mResearchListSelectionChangeListener.onResearchListSelectionChange(researchProject, checked.valueAt(i));
                }
            }
        }
        if (DEBUG)
            debug("< onItemClick(" + parent + ", " + view + ", " + pos + ", " + id + ")");
    }

    // OnResearchTreeChangeListener methods

    @Override
    public void onResearchProjectAdded(ResearchProject project) {
        if (DEBUG) debug("> onResearchProjectAdded(" + project + ")");
        project.addResearchCompletedChangeListener(this);
        this.update();
        if (DEBUG) debug("< onResearchProjectAdded(" + project + ")");
    }

    @Override
    public void onResearchProjectRemoved(ResearchProject project) {
        if (DEBUG) debug("> onResearchProjectRemoved(" + project + ")");
        this.update();
        if (DEBUG) debug("< onResearchProjectRemoved(" + project + ")");
    }

    // OnResearchCompletedChangeListener methods

    @Override
    public void onResearchCompletedChange(ResearchProject project, boolean completed) {
        if (DEBUG)
            debug("> onResearchCompletedChange(" + project + ", " + completed + ")");
        if (this.mMode != SHOW_ALL) {
            this.update();
        }
        if (DEBUG)
            debug("< onResearchCompletedChange(" + project + ", " + completed + ")");
    }

    // OnResearchPathChangeListener methods

    @Override
    public void onResearchGoalsChange(List<ResearchProject> goalsList, ResearchProject added, ResearchProject removed) {
        if (DEBUG)
            debug("> onResearchGoalsChange(" + goalsList + ")");
        this.update();
        if (removed != null) {
            updateResearchProjectView(removed);
        }
        for (ResearchProject researchProject : goalsList) {
            if (sVisibleResearch.contains(researchProject)) {
                updateResearchProjectView(researchProject);
                if (DEBUG)
                    debug("* onResearchGoalsChange: " + researchProject + " goal " + researchProject.getGoalNumber());
            }
        }
        if (DEBUG)
            debug("< onResearchGoalsChange(" + goalsList + ")");
    }

    @Override
    public void onResearchPathChange(List<ResearchProject> pathList, ResearchProject added, ResearchProject removed) {
        if (DEBUG)
            debug("> onResearchPathChange(" + pathList + ")");
        this.update();
        if (removed != null) {
            updateResearchProjectView(removed);
        }
        for (ResearchProject researchProject : pathList) {
            if (sVisibleResearch.contains(researchProject)) {
                updateResearchProjectView(researchProject);
                if (DEBUG)
                    debug("* onResearchPathChange: " + researchProject + " path " + researchProject.getPathNumber());
            }
        }
        if (DEBUG)
            debug("> onResearchPathChange(" + pathList + ")");
    }

    // Override BaseAdapter methods where needed

    @Override
    public boolean areAllItemsEnabled() {
        if (DEBUG) debug("> areAllItemsEnabled()");
        if (DEBUG) debug("< areAllItemsEnabled() returning true");
        return true;
    }

    @Override
    public int getItemViewType(int position) {
        if (DEBUG) debug("> getItemViewType(" + position + ")");
        if (DEBUG)
            debug("< getItemViewType(" + position + ") returning 0");
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        if (DEBUG) debug("> getViewTypeCount()");
        if (DEBUG) debug("< getViewTypeCount() returning 1");
        return 1;
    }

    @Override
    public boolean isEnabled(int position) {
//        if (DEBUG) debug("> isEnabled(" + position + ")");
//        if (DEBUG) debug("< isEnabled(" + position + ") returning true");
        return true;
    }

    @Override
    public boolean hasStableIds() {
        if (DEBUG) debug("> hasStableIds()");
        if (DEBUG) debug("< hasStableIds() returning true");
        return true;
    }

    @Override
    public boolean isEmpty() {
        if (DEBUG) debug("> isEmpty()");
        if (DEBUG)
            debug("< isEmpty() returning " + (this.getCount() == 0));
        return (this.getCount() == 0);
    }

    @Override
    public int getCount() {
        if (DEBUG) debug("> getCount()");
        if (DEBUG)
            debug("< getCount() returning " + (this.sResearchOrder.size()));
        return this.sResearchOrder.size();
    }

    @Override
    public long getItemId(int i) {
        if (DEBUG) debug("> getItemId(" + i + ")");
        if (DEBUG)
            debug("< getItemId(" + i + ") returning " + (this.getItem(i).hashCode()));
        return this.getItem(i).hashCode();
    }

    @Override
    public Object getItem(int i) {
        if (DEBUG) debug("> getItem(" + i + ")");
        if (DEBUG)
            debug("< getItem(" + i + ") returning " + (this.sResearchOrder.get(i)));
        return this.sResearchOrder.get(i);
    }

    public interface OnResearchListSelectionChange {
        void onResearchListSelectionChange(ResearchProject researchProject, boolean selected);
    }
}
