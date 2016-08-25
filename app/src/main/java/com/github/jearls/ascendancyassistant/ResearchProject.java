package com.github.jearls.ascendancyassistant;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * A research project consists of a name, a set of technologies, a set
 * of dependencies on other research projects, flags indicating if the
 * project is complete, is a goal, or is in the research path, and goal
 * and research path numbers, if the associated flags are true.
 *
 * @author Johnson Earls
 */
@SuppressWarnings("WeakerAccess")
public class ResearchProject {
    @SuppressWarnings("unused")
    private static final long serialVersionUID = 2016082400L;

    private final String sName;
    private final Set<String> sTechnologies;
    private final Set<ResearchProject> sDependencies;
    private final Set<OnProjectStatusChangeListener> sListeners;
    private boolean mCompleted;
    private boolean mGoal;
    private boolean mInPath;
    private int mGoalNumber;
    private int mPathNumber;

    public ResearchProject(String name) {
        this.sName = name;
        this.sTechnologies = new HashSet<>();
        this.sDependencies = new HashSet<>();
        this.mCompleted = false;
        this.mGoal = false;
        this.mInPath = false;
        this.mGoalNumber = 0;
        this.mPathNumber = 0;
        this.sListeners = new HashSet<>();
    }

    public int hashCode() {
        return this.sName.hashCode();
    }

    @SuppressWarnings("unused")
    public boolean isInPath() {
        return mInPath;
    }

    @SuppressWarnings("unused")
    public void setInPath(boolean inPath) {
        this.mInPath = inPath;
        this.triggerOnInPathChange();
    }

    public int getPathNumber() {
        return mPathNumber;
    }

    @SuppressWarnings("unused")
    public void setPathNumber(int pathNumber) {
        this.mPathNumber = pathNumber;
        this.triggerOnPathNumberChange();
    }

    @SuppressWarnings("unused")
    public boolean isCompleted() {
        return mCompleted;
    }

    @SuppressWarnings("unused")
    public void setCompleted(boolean completed) {
        this.mCompleted = completed;
        this.triggerOnCompletedChange();
    }

    @SuppressWarnings("unused")
    public boolean isGoal() {
        return mGoal;
    }

    @SuppressWarnings("unused")
    public void setGoal(boolean goal) {
        this.mGoal = goal;
        this.triggerOnGoalChange();
    }

    @SuppressWarnings("unused")
    public int getGoalNumber() {
        return mGoalNumber;
    }

    @SuppressWarnings("unused")
    public void setGoalNumber(int goalNumber) {
        this.mGoalNumber = goalNumber;
        this.triggerOnGoalNumberChange();
    }

    public String getName() {
        return sName;
    }

    public Iterator<String> getTechnologies() {
        return sTechnologies.iterator();
    }

    public void addTechnology(String technology) {
        sTechnologies.add(technology);
    }

    @SuppressWarnings("unused")
    public void removeTechnology(String technology) {
        sTechnologies.remove(technology);
    }

    public boolean hasTechnology(String technology) {
        return sTechnologies.contains(technology);
    }

    public Iterator<ResearchProject> getDependencies() {
        return sDependencies.iterator();
    }

    public void addDependency(ResearchProject dependency) {
        sDependencies.add(dependency);
    }

    @SuppressWarnings("unused")
    public void removeDependency(ResearchProject dependency) {
        sDependencies.remove(dependency);
    }

    public boolean hasDependency(ResearchProject dependency) {
        return sDependencies.contains(dependency);
    }

    /**
     * Add a status change listener.
     *
     * @param listener the listener to add
     */
    public void addProjectStatusChangeListener(OnProjectStatusChangeListener listener) {
        this.sListeners.add(listener);
    }

    /**
     * Remove a status change listener.
     *
     * @param listener the listener to remove
     */
    @SuppressWarnings("unused")
    public void removeProjectStatusChangeListener(OnProjectStatusChangeListener listener) {
        this.sListeners.remove(listener);
    }

    private void triggerOnCompletedChange() {
        for (OnProjectStatusChangeListener listener : this.sListeners) {
            listener.onCompletedChange(this, this.mCompleted);
        }
    }

    private void triggerOnGoalChange() {
        for (OnProjectStatusChangeListener listener : this.sListeners) {
            listener.onGoalChange(this, this.mGoal);
        }
    }

    private void triggerOnInPathChange() {
        for (OnProjectStatusChangeListener listener : this.sListeners) {
            listener.onInPathChange(this, this.mInPath);
        }
    }

    private void triggerOnPathNumberChange() {
        for (OnProjectStatusChangeListener listener : this.sListeners) {
            listener.onPathNumberChange(this, mPathNumber);
        }
    }

    private void triggerOnGoalNumberChange() {
        for (OnProjectStatusChangeListener listener : this.sListeners) {
            listener.onGoalNumberChange(this, mGoalNumber);
        }
    }

    public String toString() {
        return "ResearchProject(\"" + this.sName + "\")";
    }

    @SuppressWarnings("UnusedParameters")
    public interface OnProjectStatusChangeListener {
        void onCompletedChange(ResearchProject project, boolean completed);

        void onGoalChange(ResearchProject project, boolean goal);

        void onInPathChange(ResearchProject project, boolean inPath);

        void onGoalNumberChange(ResearchProject project, int goalNumber);

        void onPathNumberChange(ResearchProject project, int pathNumber);
    }
}
