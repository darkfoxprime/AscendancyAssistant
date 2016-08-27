package com.github.jearls.ascendancyassistant;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * An adapter to manage a set of ResearchProject objects being displayed
 * in a list using the research_project_list_item layout.
 *
 * @author Johnson Earls
 */
public class ResearchListAdapter extends BaseAdapter implements ResearchProject.OnProjectStatusChangeListener, ResearchTree.OnResearchTreeChangeListener {
    private static final boolean DEBUG = false;
    @SuppressWarnings("WeakerAccess")
    public final int SHOW_COMPLETED = 1;
    @SuppressWarnings("WeakerAccess")
    public final int SHOW_ALL = 2;
    @SuppressWarnings({"WeakerAccess", "unused"})
    public final int SHOW_AVAILABLE = 3;
    @SuppressWarnings("WeakerAccess")
    public final int SHOW_RESEARCHING = 4;
    private final ResearchTree sResearch;
    private final List<ResearchProject> sResearchOrder;
    private final Set<ResearchProject> sVisibleResearch;
    private final Context sContext;
    private final LayoutInflater sInflater;
    private int mMode;
    public ResearchListAdapter(Activity topActivity, ResearchTree research) {
        if (DEBUG)
            debug("ResearchListAdapter", "> ResearchListAdapter(" + topActivity + ", " + research + ")");
        this.sResearch = research;
        this.mMode = SHOW_ALL;
        this.sResearchOrder = new ArrayList<>();
        this.sVisibleResearch = new HashSet<>();
        this.sContext = topActivity;
        this.sInflater = (LayoutInflater) this.sContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.sResearch.addResearchTreeChangeListener(this);
        for (ResearchProject project : research.getResearchProjects()) {
            project.addProjectStatusChangeListener(this);
        }
        this.update();
        if (DEBUG)
            debug("ResearchListAdapter", "< ResearchListAdapter(" + topActivity + ", " + research + ")");
    }

    @SuppressWarnings("SameParameterValue")
    private static void debug(String tag, String msg) {
        System.err.println(tag + ": " + msg);
        Log.d(tag, msg);
    }

    @SuppressWarnings("unused")
    public int getMode() {
        if (DEBUG) debug("ResearchListAdapter", "> getMode()");
        if (DEBUG)
            debug("ResearchListAdapter", "< getMode() returning " + mMode);
        return mMode;
    }

    @SuppressWarnings("unused")
    public void setMode(int mode) {
        if (DEBUG)
            debug("ResearchListAdapter", "> setMode(" + mode + ")");
        if (mode != this.mMode) {
            this.mMode = mode;
            this.update();
        }
        if (DEBUG)
            debug("ResearchListAdapter", "< setMode(" + mode + ")");
    }

    private void update() {
        if (DEBUG) debug("ResearchListAdapter", "> update()");
        boolean updated = false;
        for (ResearchProject project : this.sResearch.getResearchProjects()) {
            boolean projectShouldBeDisplayed = (this.mMode == SHOW_ALL); // TODO: actually figure this out
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
            for (ResearchProject project : this.sVisibleResearch) {
                int i = this.sResearchOrder.size();
                if (this.mMode == SHOW_RESEARCHING) {
                    // sort based on research project's path order
                    int o = project.getPathNumber();
                    while ((i > 0) && (this.sResearchOrder.get(i - 1).getPathNumber() > o)) {
                        i -= 1;
                    }
                } else {
                    // sort based on research project's name
                    while ((i > 0) && (this.sResearchOrder.get(i - 1).getName().compareTo(project.getName()) > 0)) {
                        i -= 1;
                    }
                }
                this.sResearchOrder.add(i, project);
            }
            this.notifyDataSetChanged();
        }
        if (DEBUG) debug("ResearchListAdapter", "< update()");
    }

    // OnResearchTreeChangeListener methods

    @Override
    public void onResearchProjectAdded(ResearchTree tree, ResearchProject project) {
        if (DEBUG)
            debug("ResearchListAdapter", "> onResearchProjectAdded(" + tree + ", " + project + ")");
        project.addProjectStatusChangeListener(this);
        this.update();
        if (DEBUG)
            debug("ResearchListAdapter", "< onResearchProjectAdded(" + tree + ", " + project + ")");
    }

    @Override
    public void onResearchProjectRemoved(ResearchTree tree, ResearchProject project) {
        if (DEBUG)
            debug("ResearchListAdapter", "> onResearchProjectRemoved(" + tree + ", " + project + ")");
        this.update();
        if (DEBUG)
            debug("ResearchListAdapter", "< onResearchProjectRemoved(" + tree + ", " + project + ")");
    }

    // OnProjectStatusChangeListener methods


    @Override
    public void onCompletedChange(ResearchProject project, boolean completed) {
        if (DEBUG)
            debug("ResearchListAdapter", "> onCompletedChange(" + project + ", " + completed + ")");
        if (this.mMode != SHOW_ALL) {
            this.update();
        }
        if (DEBUG)
            debug("ResearchListAdapter", "< onCompletedChange(" + project + ", " + completed + ")");
    }

    @Override
    public void onGoalChange(ResearchProject project, boolean goal) {
        if (DEBUG)
            debug("ResearchListAdapter", "> onGoalChange(" + project + ", " + goal + ")");
        if (this.mMode != SHOW_COMPLETED) {
            this.update();
        }
        if (DEBUG)
            debug("ResearchListAdapter", "< onGoalChange(" + project + ", " + goal + ")");
    }

    @Override
    public void onInPathChange(ResearchProject project, boolean inPath) {
        if (DEBUG)
            debug("ResearchListAdapter", "> onInPathChange(" + project + ", " + inPath + ")");
        if (this.mMode != SHOW_COMPLETED) {
            this.update();
        }
        if (DEBUG)
            debug("ResearchListAdapter", "< onInPathChange(" + project + ", " + inPath + ")");
    }

    @Override
    public void onGoalNumberChange(ResearchProject project, int goalNumber) {
        if (DEBUG)
            debug("ResearchListAdapter", "> onGoalNumberChange(" + project + ", " + goalNumber + ")");
        if (this.mMode == SHOW_RESEARCHING) {
            this.update();
        }
        if (DEBUG)
            debug("ResearchListAdapter", "< onGoalNumberChange(" + project + ", " + goalNumber + ")");
    }

    @Override
    public void onPathNumberChange(ResearchProject project, int pathNumber) {
        if (DEBUG)
            debug("ResearchListAdapter", "> onPathNumberChange(" + project + ", " + pathNumber + ")");
        if (this.mMode == SHOW_RESEARCHING) {
            this.update();
        }
        if (DEBUG)
            debug("ResearchListAdapter", "< onPathNumberChange(" + project + ", " + pathNumber + ")");
    }

    // Override BaseAdapter methods where needed

    @Override
    public boolean areAllItemsEnabled() {
        if (DEBUG)
            debug("ResearchListAdapter", "> areAllItemsEnabled()");
        if (DEBUG)
            debug("ResearchListAdapter", "< areAllItemsEnabled() returning true");
        return true;
    }

    @Override
    public int getItemViewType(int position) {
        if (DEBUG)
            debug("ResearchListAdapter", "> getItemViewType(" + position + ")");
        if (DEBUG)
            debug("ResearchListAdapter", "< getItemViewType(" + position + ") returning 0");
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        if (DEBUG) debug("ResearchListAdapter", "> getViewTypeCount()");
        if (DEBUG)
            debug("ResearchListAdapter", "< getViewTypeCount() returning 1");
        return 1;
    }

    @Override
    public boolean isEnabled(int position) {
        if (DEBUG)
            debug("ResearchListAdapter", "> isEnabled(" + position + ")");
        if (DEBUG)
            debug("ResearchListAdapter", "< isEnabled(" + position + ") returning true");
        return true;
    }

    @Override
    public boolean hasStableIds() {
        if (DEBUG) debug("ResearchListAdapter", "> hasStableIds()");
        if (DEBUG)
            debug("ResearchListAdapter", "< hasStableIds() returning true");
        return true;
    }

    @Override
    public boolean isEmpty() {
        if (DEBUG) debug("ResearchListAdapter", "> isEmpty()");
        if (DEBUG)
            debug("ResearchListAdapter", "< isEmpty() returning " + (this.getCount() == 0));
        return (this.getCount() == 0);
    }

    @Override
    public int getCount() {
        if (DEBUG) debug("ResearchListAdapter", "> getCount()");
        if (DEBUG)
            debug("ResearchListAdapter", "< getCount() returning " + (this.sResearchOrder.size()));
        return this.sResearchOrder.size();
    }

    @Override
    public long getItemId(int i) {
        if (DEBUG)
            debug("ResearchListAdapter", "> getItemId(" + i + ")");
        if (DEBUG)
            debug("ResearchListAdapter", "< getItemId(" + i + ") returning " + (this.getItem(i).hashCode()));
        return this.getItem(i).hashCode();
    }

    @Override
    public Object getItem(int i) {
        if (DEBUG) debug("ResearchListAdapter", "> getItem(" + i + ")");
        if (DEBUG)
            debug("ResearchListAdapter", "< getItem(" + i + ") returning " + (this.sResearchOrder.get(i)));
        return this.sResearchOrder.get(i);
    }

    @Override
    public View getView(int i, View view, ViewGroup parentView) {
        if (DEBUG)
            debug("ResearchListAdapter", "> getView(" + i + ", " + view + ", " + parentView + ")");
        if (view == null) {
            view = sInflater.inflate(R.layout.research_project_list_item, parentView, false);
        }
        ImageView icon = (ImageView) view.findViewById(R.id.research_project_list_item_icon);
        TextView title = (TextView) view.findViewById(R.id.research_project_list_item_title);
        TextView techs = (TextView) view.findViewById(R.id.research_project_list_item_technologies);
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
        String iconName = "research_" + project.getName().toLowerCase().replace(' ', '_').replaceAll("[^a-z_]", "");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            icon.setImageDrawable(sContext.getResources().getDrawable(sContext.getResources().getIdentifier(iconName, "drawable", this.getClass().getPackage().getName()), sContext.getTheme()));
        } else {
            //noinspection deprecation
            icon.setImageDrawable(sContext.getResources().getDrawable(sContext.getResources().getIdentifier(iconName, "drawable", this.getClass().getPackage().getName())));
        }

        if (DEBUG)
            debug("ResearchListAdapter", "< getView(" + i + ", " + view + ", " + parentView + ") returning " + (view));
        return view;
    }

}
