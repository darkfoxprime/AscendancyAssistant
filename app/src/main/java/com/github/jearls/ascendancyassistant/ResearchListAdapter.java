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
        Log.d("ResearchListAdapter", "ResearchListAdapter(" + topActivity + ", " + research + ")");
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
    }

    @SuppressWarnings("unused")
    public int getMode() {
        Log.d("ResearchListAdapter", "getMode()");
        Log.d("ResearchListAdapter", "... returning " + mMode);
        return mMode;
    }

    @SuppressWarnings("unused")
    public void setMode(int mode) {
        Log.d("ResearchListAdapter", "setMode(" + mode + ")");
        if (mode != this.mMode) {
            this.mMode = mode;
            this.update();
        }
    }

    private void update() {
        Log.d("ResearchListAdapter", "update()");
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
    }

    // OnResearchTreeChangeListener methods

    @Override
    public void onResearchProjectAdded(ResearchTree tree, ResearchProject project) {
        Log.d("ResearchListAdapter", "onResearchProjectAdded(" + tree + ", \"" + project.getName() + "\")");
        project.addProjectStatusChangeListener(this);
        this.update();
    }

    @Override
    public void onResearchProjectRemoved(ResearchTree tree, ResearchProject project) {
        Log.d("ResearchListAdapter", "onResearchProjectRemoved(" + tree + ", \"" + project.getName() + "\")");
        this.update();
    }

    // OnProjectStatusChangeListener methods


    @Override
    public void onCompletedChange(ResearchProject project, boolean completed) {
        if (this.mMode != SHOW_ALL) {
            this.update();
        }
    }

    @Override
    public void onGoalChange(ResearchProject project, boolean goal) {
        if (this.mMode != SHOW_COMPLETED) {
            this.update();
        }
    }

    @Override
    public void onInPathChange(ResearchProject project, boolean inPath) {
        if (this.mMode != SHOW_COMPLETED) {
            this.update();
        }
    }

    @Override
    public void onGoalNumberChange(ResearchProject project, int goalNumber) {
        if (this.mMode == SHOW_RESEARCHING) {
            this.update();
        }
    }

    @Override
    public void onPathNumberChange(ResearchProject project, int pathNumber) {
        if (this.mMode == SHOW_RESEARCHING) {
            this.update();
        }
    }

    // Override BaseAdapter methods where needed

    @Override
    public boolean areAllItemsEnabled() {
        Log.d("ResearchListAdapter", "areAllItemsEnabled()");
        Log.d("ResearchListAdapter", "... returning true");
        return true;
    }

    @Override
    public int getItemViewType(int position) {
        Log.d("ResearchListAdapter", "getItemViewType(" + position + ")");
        Log.d("ResearchListAdapter", "... returning 0");
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        Log.d("ResearchListAdapter", "getViewTypeCount()");
        Log.d("ResearchListAdapter", "... returning 1");
        return 1;
    }

    @Override
    public boolean isEnabled(int position) {
        Log.d("ResearchListAdapter", "isEnabled(" + position + ")");
        Log.d("ResearchListAdapter", "... returning true");
        return true;
    }

    @Override
    public boolean hasStableIds() {
        Log.d("ResearchListAdapter", "hasStableIds()");
        Log.d("ResearchListAdapter", "... returning true");
        return true;
    }

    @Override
    public boolean isEmpty() {
        Log.d("ResearchListAdapter", "isEmpty()");
        Log.d("ResearchListAdapter", "... returning " + (this.getCount() == 0));
        return (this.getCount() == 0);
    }

    @Override
    public int getCount() {
        Log.d("ResearchListAdapter", "getCount()");
        Log.d("ResearchListAdapter", "... returning " + (this.sResearchOrder.size()));
        return this.sResearchOrder.size();
    }

    @Override
    public long getItemId(int i) {
        Log.d("ResearchListAdapter", "getItemId(" + i + ")");
        Log.d("ResearchListAdapter", "... returning " + (this.getItem(i).hashCode()));
        return this.getItem(i).hashCode();
    }

    @Override
    public Object getItem(int i) {
        Log.d("ResearchListAdapter", "getItem(" + i + ")");
        Log.d("ResearchListAdapter", "... returning " + (this.sResearchOrder.get(i)));
        return this.sResearchOrder.get(i);
    }

    @Override
    public View getView(int i, View view, ViewGroup parentView) {
        Log.d("ResearchListAdapter", "getView(" + i + ", " + view + ", " + parentView + ")");
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
        Log.d("ResearchListAdapter", "research project \"" + project.getName() + "\" -> icon \"" + iconName + "\"");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            icon.setImageDrawable(sContext.getResources().getDrawable(sContext.getResources().getIdentifier(iconName, "drawable", this.getClass().getPackage().getName()), sContext.getTheme()));
        } else {
            //noinspection deprecation
            icon.setImageDrawable(sContext.getResources().getDrawable(sContext.getResources().getIdentifier(iconName, "drawable", this.getClass().getPackage().getName())));
        }

        Log.d("ResearchListAdapter", "... returning " + (view));
        return view;
    }

}
