package com.github.jearls.ascendancyassistant;

import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
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

    private static final boolean DEBUG = false;
    private static final Map<String, ResearchProject> sResearchTree = new HashMap<>();
    private static final Set<OnResearchChangeListener> sResearchChangeListeners = new HashSet<>();

    private final String sName;
    private final Set<String> sTechnologies;
    private final Set<ResearchProject> sDependents;
    private final Set<ResearchProject> sDependers;
    private final Set<OnResearchStatusChangeListener> sResearchStatusChangeListeners;
    private final Set<OnResearchDependencyChangeListener> sResearchDependencyChangeListeners;
    private final Set<OnResearchTechnologyChangeListener> sResearchTechnologyChangeListeners;
    private boolean mCompleted;
    private boolean mGoal;
    private boolean mInPath;
    private int mGoalNumber;
    private int mPathNumber;

    public ResearchProject(String name) throws OnResearchChangeListener.ResearchChangeProhibitedException, DuplicateResearchProjectException {
        this.sName = name;
        if (sResearchTree.containsKey(name)) {
            throw new DuplicateResearchProjectException(name);
        }
        // allocate objects here so that allocation doesn't happen on a DuplicateResearchProjectException
        this.sTechnologies = new HashSet<>();
        this.sDependents = new HashSet<>();
        this.sDependers = new HashSet<>();
        this.mCompleted = false;
        this.mGoal = false;
        this.mInPath = false;
        this.mGoalNumber = 0;
        this.mPathNumber = 0;
        this.sResearchStatusChangeListeners = new HashSet<>();
        this.sResearchDependencyChangeListeners = new HashSet<>();
        this.sResearchTechnologyChangeListeners = new HashSet<>();
        try {
            sResearchTree.put(name, this);
            triggerResearchProjectAdded(this);
        } catch (OnResearchChangeListener.ResearchChangeProhibitedException e) {
            sResearchTree.remove(name);
            throw e;
        }
    }

    public static void addResearchChangeListener(OnResearchChangeListener listener) {
        sResearchChangeListeners.add(listener);
    }

    public static void removeResearchChangeListener(OnResearchChangeListener listener) {
        sResearchChangeListeners.remove(listener);
    }

    public static void clearResearchChangeListeners() {
        sResearchChangeListeners.clear();
    }

    private static void triggerResearchProjectAdded(ResearchProject researchProject) throws OnResearchChangeListener.ResearchChangeProhibitedException {
        for (OnResearchChangeListener listener : sResearchChangeListeners) {
            listener.onResearchProjectAdded(researchProject);
        }
    }

    private static void triggerResearchProjectRemoved(ResearchProject researchProject) throws OnResearchChangeListener.ResearchChangeProhibitedException {
        for (OnResearchChangeListener listener : sResearchChangeListeners) {
            listener.onResearchProjectRemoved(researchProject);
        }
    }

    @SuppressWarnings("SameParameterValue")
    private static void debug(String tag, String msg) {
        System.err.println(tag + ": " + msg);
        Log.d(tag, msg);
    }

    public static Collection<ResearchProject> getAllResearchProjects() {
        if (DEBUG)
            debug("ResearchProject", "> getAllResearchProjects()");
        Collection<ResearchProject> allResearchProjects = Collections.unmodifiableCollection(sResearchTree.values());
        if (DEBUG)
            debug("ResearchProject", "< getAllResearchProjects() returning " + allResearchProjects);
        return allResearchProjects;
    }

    public static ResearchProject getResearchProject(String name) {
        return sResearchTree.get(name);
    }

    public static void removeAllResearchProjects() throws OnResearchChangeListener.ResearchChangeProhibitedException {
        Iterator<ResearchProject> researchProjectIterator = sResearchTree.values().iterator();
        while (researchProjectIterator.hasNext()) {
            ResearchProject researchProject = researchProjectIterator.next();
            researchProjectIterator.remove();
            removeResearchProject(researchProject);
        }
    }

    public static void removeResearchProject(ResearchProject researchProject) throws OnResearchChangeListener.ResearchChangeProhibitedException {
        // FIXME properly preserve and restore state when needed
        // FIXME clear dependencies before triggering
        try {
            // remove from research tree
            sResearchTree.remove(researchProject.getName());
            triggerResearchProjectRemoved(researchProject);
        } catch (OnResearchChangeListener.ResearchChangeProhibitedException e) {
            sResearchTree.put(researchProject.getName(), researchProject);
            throw e;
        }
        // clear listeners
        researchProject.clearResearchDependencyChangeListeners();
        researchProject.clearResearchTechnologyChangeListeners();
        researchProject.clearResearchStatusChangeListeners();
        // clear dependency links
        Iterator<ResearchProject> dependencyIterator = researchProject.getDependents();
        while (dependencyIterator.hasNext()) {
            ResearchProject dependent = dependencyIterator.next();
            dependencyIterator.remove();
            try {
                researchProject.removeDependent(dependent);
            } catch (OnResearchDependencyChangeListener.ResearchDependencyChangeProhibitedException e) {
                /*NOTREACHED*/
            }
        }
        dependencyIterator = researchProject.getDependers();
        while (dependencyIterator.hasNext()) {
            ResearchProject depender = dependencyIterator.next();
            dependencyIterator.remove();
            try {
                researchProject.removeDepender(depender);
            } catch (OnResearchDependencyChangeListener.ResearchDependencyChangeProhibitedException e) {
                /*NOTREACHED*/
            }
        }
    }

    public static void loadXmlResearchTree(XmlPullParser parser) throws ResearchTreeXmlParserException, OnResearchChangeListener.ResearchChangeProhibitedException, OnResearchDependencyChangeListener.ResearchDependencyChangeProhibitedException, OnResearchTechnologyChangeListener.ResearchTechnologyChangeProhibitedException {
        if (DEBUG)
            debug("ResearchProject", "> loadXmlResearchTree(" + parser + ")");
        ResearchProject project = null;
        ResearchTreeXmlParser researchTreeXmlParser = new ResearchTreeXmlParser(parser);

        try {
            int event;
            while ((event = researchTreeXmlParser.next()) != ResearchTreeXmlParser.DONE) {
                switch (event) {
                    case ResearchTreeXmlParser.RESEARCH_PROJECT:
                        project = getResearchProject(researchTreeXmlParser.getName());
                        if (project == null) {
                            project = new ResearchProject(researchTreeXmlParser.getName());
                        }
                        break;
                    case ResearchTreeXmlParser.DEPENDENCY:
                        ResearchProject dependency = getResearchProject(researchTreeXmlParser.getName());
                        if (dependency == null) {
                            dependency = new ResearchProject(researchTreeXmlParser.getName());
                        }
                        //noinspection ConstantConditions
                        assert project != null;
                        project.addDependent(dependency);
                        break;
                    case ResearchTreeXmlParser.TECHNOLOGY:
                        //noinspection ConstantConditions
                        assert project != null;
                        project.addTechnology(researchTreeXmlParser.getName());
                        break;
                }
            }
            if (DEBUG)
                debug("ResearchProject", "< loadXmlResearchTree(" + parser + ")");
        } catch (ResearchTreeXmlParserException e) {
            try {
                removeAllResearchProjects();
            } catch (OnResearchChangeListener.ResearchChangeProhibitedException rcpe) { /* IGNORED */ }
            throw e;
        }
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
        if (this.mInPath != inPath) {
            this.mInPath = inPath;
            this.triggerOnInPathChange();
        }
    }

    public int getPathNumber() {
        return mPathNumber;
    }

    @SuppressWarnings("unused")
    public void setPathNumber(int pathNumber) {
        if (this.mPathNumber != pathNumber) {
            this.mPathNumber = pathNumber;
            this.triggerOnPathNumberChange();
        }
    }

    @SuppressWarnings("unused")
    public boolean isCompleted() {
        return mCompleted;
    }

    @SuppressWarnings("unused")
    public void setCompleted(boolean completed) {
        if (this.mCompleted != completed) {
            this.mCompleted = completed;
            this.triggerOnCompletedChange();
        }
    }

    @SuppressWarnings("unused")
    public boolean isGoal() {
        return mGoal;
    }

    @SuppressWarnings("unused")
    public void setGoal(boolean goal) {
        if (this.mGoal != goal) {
            this.mGoal = goal;
            this.triggerOnGoalChange();
        }
    }

    @SuppressWarnings("unused")
    public int getGoalNumber() {
        return mGoalNumber;
    }

    @SuppressWarnings("unused")
    public void setGoalNumber(int goalNumber) {
        if (this.mGoalNumber != goalNumber) {
            this.mGoalNumber = goalNumber;
            this.triggerOnGoalNumberChange();
        }
    }

    public String getName() {
        return sName;
    }

    public void addResearchTechnologyChangeListener(OnResearchTechnologyChangeListener listener) {
        this.sResearchTechnologyChangeListeners.add(listener);
    }

    public void removeResearchTechnologyChangeListener(OnResearchTechnologyChangeListener listener) {
        this.sResearchTechnologyChangeListeners.remove(listener);
    }

    public void clearResearchTechnologyChangeListeners() {
        this.sResearchTechnologyChangeListeners.clear();
    }

    public void triggerTechnologyAdded(String technology) throws OnResearchTechnologyChangeListener.ResearchTechnologyChangeProhibitedException {
        for (OnResearchTechnologyChangeListener listener : this.sResearchTechnologyChangeListeners) {
            listener.onTechnologyAdded(this, technology);
        }
    }

    public void triggerTechnologyRemoved(String technology) throws OnResearchTechnologyChangeListener.ResearchTechnologyChangeProhibitedException {
        for (OnResearchTechnologyChangeListener listener : this.sResearchTechnologyChangeListeners) {
            listener.onTechnologyRemoved(this, technology);
        }
    }

    public Iterator<String> getTechnologies() {
        return sTechnologies.iterator();
    }

    public void addTechnology(String technology) throws OnResearchTechnologyChangeListener.ResearchTechnologyChangeProhibitedException {
        try {
            sTechnologies.add(technology);
            triggerTechnologyAdded(technology);
        } catch (OnResearchTechnologyChangeListener.ResearchTechnologyChangeProhibitedException e) {
            sTechnologies.remove(technology);
            throw e;
        }
    }

    public void removeTechnology(String technology) throws OnResearchTechnologyChangeListener.ResearchTechnologyChangeProhibitedException {
        try {
            sTechnologies.remove(technology);
            triggerTechnologyRemoved(technology);
        } catch (OnResearchTechnologyChangeListener.ResearchTechnologyChangeProhibitedException e) {
            sTechnologies.add(technology);
            throw e;
        }

    }

    public boolean hasTechnology(String technology) {
        return sTechnologies.contains(technology);
    }

    public void addResearchDependencyChangeListener(OnResearchDependencyChangeListener listener) {
        this.sResearchDependencyChangeListeners.add(listener);
    }

    public void removeResearchDependencyChangeListener(OnResearchDependencyChangeListener listener) {
        if (DEBUG)
            debug("ResearchProject", "> removeResearchDependencyChangeListener(" + listener + ")");
        this.sResearchDependencyChangeListeners.remove(listener);
        if (DEBUG)
            debug("ResearchProject", "< removeResearchDependencyChangeListener(" + listener + ")");
    }

    public void clearResearchDependencyChangeListeners() {
        this.sResearchDependencyChangeListeners.clear();
    }

    public void triggerDependentAdded(ResearchProject dependent) throws OnResearchDependencyChangeListener.ResearchDependencyChangeProhibitedException {
        for (OnResearchDependencyChangeListener listener : this.sResearchDependencyChangeListeners) {
            listener.onDependentAdded(this, dependent);
        }
    }

    public void triggerDependentRemoved(ResearchProject dependent) throws OnResearchDependencyChangeListener.ResearchDependencyChangeProhibitedException {
        for (OnResearchDependencyChangeListener listener : this.sResearchDependencyChangeListeners) {
            listener.onDependentRemoved(this, dependent);
        }
    }

    public void triggerDependerAdded(ResearchProject dependent) throws OnResearchDependencyChangeListener.ResearchDependencyChangeProhibitedException {
        for (OnResearchDependencyChangeListener listener : this.sResearchDependencyChangeListeners) {
            listener.onDependerAdded(this, dependent);
        }
    }

    public void triggerDependerRemoved(ResearchProject dependent) throws OnResearchDependencyChangeListener.ResearchDependencyChangeProhibitedException {
        for (OnResearchDependencyChangeListener listener : this.sResearchDependencyChangeListeners) {
            listener.onDependerRemoved(this, dependent);
        }
    }

    public Iterator<ResearchProject> getDependents() {
        return sDependents.iterator();
    }

    public void addDependent(ResearchProject dependent) throws OnResearchDependencyChangeListener.ResearchDependencyChangeProhibitedException {
        try {
            sDependents.add(dependent);
            dependent.sDependers.add(this);
            this.triggerDependentAdded(dependent);
            dependent.triggerDependerAdded(this);
        } catch (OnResearchDependencyChangeListener.ResearchDependencyChangeProhibitedException e) {
            sDependents.remove(dependent);
            dependent.sDependers.remove(this);
            throw e;
        }
    }

    public void removeDependent(ResearchProject dependent) throws OnResearchDependencyChangeListener.ResearchDependencyChangeProhibitedException {
        try {
            sDependents.remove(dependent);
            dependent.sDependers.remove(this);
            this.triggerDependentRemoved(dependent);
            dependent.triggerDependerRemoved(this);
        } catch (OnResearchDependencyChangeListener.ResearchDependencyChangeProhibitedException e) {
            sDependents.add(dependent);
            dependent.sDependers.add(this);
            throw e;
        }
    }

    public boolean hasDependent(ResearchProject dependent) {
        return sDependents.contains(dependent);
    }

    public Iterator<ResearchProject> getDependers() {
        return sDependers.iterator();
    }

    public void addDepender(ResearchProject depender) throws OnResearchDependencyChangeListener.ResearchDependencyChangeProhibitedException {
        try {
            sDependers.add(depender);
            depender.sDependents.add(this);
            this.triggerDependerAdded(depender);
            depender.triggerDependentAdded(this);
        } catch (OnResearchDependencyChangeListener.ResearchDependencyChangeProhibitedException e) {
            sDependers.remove(depender);
            depender.sDependents.remove(this);
            throw e;
        }
    }

    public void removeDepender(ResearchProject depender) throws OnResearchDependencyChangeListener.ResearchDependencyChangeProhibitedException {
        try {
            sDependers.remove(depender);
            depender.sDependents.remove(this);
            this.triggerDependerRemoved(depender);
            depender.triggerDependentRemoved(this);
        } catch (OnResearchDependencyChangeListener.ResearchDependencyChangeProhibitedException e) {
            sDependers.add(depender);
            depender.sDependents.add(this);
            throw e;
        }
    }

    public boolean hasDepender(ResearchProject depender) {
        return sDependers.contains(depender);
    }

    public void addResearchStatusChangeListener(OnResearchStatusChangeListener listener) {
        this.sResearchStatusChangeListeners.add(listener);
    }

    public void removeResearchStatusChangeListener(OnResearchStatusChangeListener listener) {
        this.sResearchStatusChangeListeners.remove(listener);
    }

    public void clearResearchStatusChangeListeners() {
        this.sResearchStatusChangeListeners.clear();
    }

    private void triggerOnCompletedChange() {
        for (OnResearchStatusChangeListener listener : this.sResearchStatusChangeListeners) {
            listener.onCompletedChange(this, this.mCompleted);
        }
    }

    private void triggerOnGoalChange() {
        for (OnResearchStatusChangeListener listener : this.sResearchStatusChangeListeners) {
            listener.onGoalChange(this, this.mGoal);
        }
    }

    private void triggerOnInPathChange() {
        for (OnResearchStatusChangeListener listener : this.sResearchStatusChangeListeners) {
            listener.onInPathChange(this, this.mInPath);
        }
    }

    private void triggerOnPathNumberChange() {
        for (OnResearchStatusChangeListener listener : this.sResearchStatusChangeListeners) {
            listener.onPathNumberChange(this, mPathNumber);
        }
    }

    private void triggerOnGoalNumberChange() {
        for (OnResearchStatusChangeListener listener : this.sResearchStatusChangeListeners) {
            listener.onGoalNumberChange(this, mGoalNumber);
        }
    }

    public String toString() {
        return "ResearchProject(\"" + this.sName + "\")";
    }

    @SuppressWarnings("UnusedParameters")
    public interface OnResearchStatusChangeListener {
        void onCompletedChange(ResearchProject project, boolean completed);

        void onGoalChange(ResearchProject project, boolean goal);

        void onInPathChange(ResearchProject project, boolean inPath);

        void onGoalNumberChange(ResearchProject project, int goalNumber);

        void onPathNumberChange(ResearchProject project, int pathNumber);
    }

    @SuppressWarnings({"UnusedParameters", "RedundantThrows"})
    public interface OnResearchChangeListener {
        void onResearchProjectAdded(ResearchProject project) throws ResearchChangeProhibitedException;

        void onResearchProjectRemoved(ResearchProject project) throws ResearchChangeProhibitedException;

        @SuppressWarnings("unused")
        class ResearchChangeProhibitedException extends Exception {
            @SuppressWarnings("unused")
            public ResearchChangeProhibitedException() {
                super();
            }

            @SuppressWarnings("unused")
            public ResearchChangeProhibitedException(String message) {
                super(message);
            }
        }
    }

    @SuppressWarnings("UnusedParameters")
    public interface OnResearchDependencyChangeListener {
        void onDependentAdded(ResearchProject project, ResearchProject dependent) throws ResearchDependencyChangeProhibitedException;

        void onDependentRemoved(ResearchProject project, ResearchProject dependent) throws ResearchDependencyChangeProhibitedException;

        void onDependerAdded(ResearchProject project, ResearchProject depender) throws ResearchDependencyChangeProhibitedException;

        void onDependerRemoved(ResearchProject project, ResearchProject depender) throws ResearchDependencyChangeProhibitedException;

        @SuppressWarnings("unused")
        class ResearchDependencyChangeProhibitedException extends Exception {
            @SuppressWarnings("unused")
            public ResearchDependencyChangeProhibitedException() {
                super();
            }

            @SuppressWarnings("unused")
            public ResearchDependencyChangeProhibitedException(String message) {
                super(message);
            }
        }
    }

    public interface OnResearchTechnologyChangeListener {
        void onTechnologyAdded(ResearchProject project, String technology) throws ResearchTechnologyChangeProhibitedException;

        void onTechnologyRemoved(ResearchProject project, String technology) throws ResearchTechnologyChangeProhibitedException;

        class ResearchTechnologyChangeProhibitedException extends Exception {
            public ResearchTechnologyChangeProhibitedException() {
                super();
            }

            public ResearchTechnologyChangeProhibitedException(String message) {
                super(message);
            }
        }
    }

    @SuppressWarnings("unused")
    public static class DuplicateResearchProjectException extends IllegalArgumentException {
        public DuplicateResearchProjectException() {
        }

        public DuplicateResearchProjectException(String s) {
            super(s);
        }
    }

    private static class ResearchTreeXmlParser {
        public static final int DONE = 0;
        public static final int RESEARCH_PROJECT = 1;
        public static final int DEPENDENCY = 2;
        public static final int TECHNOLOGY = 3;

        private static final int TAG_START_DOCUMENT = 1;
        private static final int TAG_END_DOCUMENT = 2;
        private static final int TAG_START_RESEARCH_TREE = "researchtree".hashCode();
        private static final int TAG_END_RESEARCH_TREE = TAG_START_RESEARCH_TREE + 1;
        private static final int TAG_START_RESEARCH_PROJECT = "researchproject".hashCode();
        private static final int TAG_END_RESEARCH_PROJECT = TAG_START_RESEARCH_PROJECT + 1;
        private static final int TAG_START_REQUIREMENTS = "requirements".hashCode();
        private static final int TAG_END_REQUIREMENTS = TAG_START_REQUIREMENTS + 1;
        private static final int TAG_START_REQUIRES = "requires".hashCode();
        private static final int TAG_END_REQUIRES = TAG_START_REQUIRES + 1;
        private static final int TAG_START_TECHNOLOGIES = "technologies".hashCode();
        private static final int TAG_END_TECHNOLOGIES = TAG_START_TECHNOLOGIES + 1;
        private static final int TAG_START_TECHNOLOGY = "technology".hashCode();
        private static final int TAG_END_TECHNOLOGY = TAG_START_TECHNOLOGY + 1;

        private final int STATE_START = 0;
        private final int STATE_SUCCESS = 1;
        private final int STATE_ERROR = 2;
        //private final int STATE_DOCUMENT = 3;
        private final int STATE_RESEARCH_TREE = 4;
        private final int STATE_RESEARCH_PROJECT = 5;
        private final int STATE_REQUIREMENTS = 6;
        private final int STATE_REQUIRES = 7;
        private final int STATE_TECHNOLOGIES = 8;
        private final int STATE_TECHNOLOGY = 9;

        private final XmlPullParser sParser;
        private String mName;

        private int mCurrentState, mNextState;


        public ResearchTreeXmlParser(XmlPullParser parser) {
            if (DEBUG)
                debug("ResearchProject", "> ResearchTreeXmlParser(" + parser + ")");
            this.sParser = parser;
            mCurrentState = mNextState = STATE_START;
            if (DEBUG)
                debug("ResearchProject", "< ResearchTreeXmlParser(" + parser + ")");
        }

        public String getName() {
            if (DEBUG) debug("ResearchProject", "> getName()");
            if (DEBUG)
                debug("ResearchProject", "< getName() returning \"" + mName + "\"");
            return mName;
        }

        public int next() throws ResearchTreeXmlParserException {
            int tag = 0;
            if (DEBUG) debug("ResearchProject", "> next()");
            while (mNextState != STATE_SUCCESS && mNextState != STATE_ERROR) {
                try {
                    mCurrentState = mNextState;
                    mNextState = STATE_ERROR;
                    mName = null;
                    tag = sParser.next();
                    while (tag == XmlPullParser.TEXT) {
                        tag = sParser.next();
                    }
                    switch (tag) {
                        case XmlPullParser.START_DOCUMENT:
                            tag = TAG_START_DOCUMENT;
                            break;
                        case XmlPullParser.END_DOCUMENT:
                            tag = TAG_END_DOCUMENT;
                            break;
                        case XmlPullParser.START_TAG:
                            tag = sParser.getName().toLowerCase().hashCode();
                            mName = sParser.getAttributeValue(null, "name");
                            break;
                        case XmlPullParser.END_TAG:
                            tag = sParser.getName().toLowerCase().hashCode() + 1;
                            break;
                        default:
                            tag = 0; // NOTREACHED
                    }

                    switch (mCurrentState) {
                        case STATE_START:
                            if (tag == ResearchTreeXmlParser.TAG_START_DOCUMENT) {
                                mNextState = STATE_START;
                            } else if (tag == ResearchTreeXmlParser.TAG_END_DOCUMENT) {
                                if (DEBUG)
                                    debug("ResearchProject", "< next() returning DONE");
                                return DONE;
                            } else if (tag == ResearchTreeXmlParser.TAG_START_RESEARCH_TREE) {
                                mNextState = STATE_RESEARCH_TREE;
                            }
                            break;
                        case STATE_RESEARCH_TREE:
                            if (tag == ResearchTreeXmlParser.TAG_END_RESEARCH_TREE) {
                                mNextState = STATE_START;
                            } else if (tag == ResearchTreeXmlParser.TAG_START_RESEARCH_PROJECT) {
                                mNextState = STATE_RESEARCH_PROJECT;
                                if (DEBUG)
                                    debug("ResearchProject", "< next() returning RESEARCH_PROJECT (\"" + mName + "\")");
                                return RESEARCH_PROJECT;
                            }
                            break;
                        case STATE_RESEARCH_PROJECT:
                            if (tag == ResearchTreeXmlParser.TAG_END_RESEARCH_PROJECT) {
                                mNextState = STATE_RESEARCH_TREE;
                            } else if (tag == ResearchTreeXmlParser.TAG_START_REQUIREMENTS) {
                                mNextState = STATE_REQUIREMENTS;
                            } else if (tag == ResearchTreeXmlParser.TAG_START_TECHNOLOGIES) {
                                mNextState = STATE_TECHNOLOGIES;
                            }
                            break;
                        case STATE_REQUIREMENTS:
                            if (tag == ResearchTreeXmlParser.TAG_END_REQUIREMENTS) {
                                mNextState = STATE_RESEARCH_PROJECT;
                            } else if (tag == ResearchTreeXmlParser.TAG_START_REQUIRES) {
                                mNextState = STATE_REQUIRES;
                                if (DEBUG)
                                    debug("ResearchProject", "< next() returning DEPENDENCY (\"" + mName + "\")");
                                return DEPENDENCY;
                            }
                            break;
                        case STATE_REQUIRES:
                            if (tag == ResearchTreeXmlParser.TAG_END_REQUIRES) {
                                mNextState = STATE_REQUIREMENTS;
                            }
                            break;
                        case STATE_TECHNOLOGIES:
                            if (tag == ResearchTreeXmlParser.TAG_END_TECHNOLOGIES) {
                                mNextState = STATE_RESEARCH_PROJECT;
                            } else if (tag == ResearchTreeXmlParser.TAG_START_TECHNOLOGY) {
                                mNextState = STATE_TECHNOLOGY;
                                if (DEBUG)
                                    debug("ResearchProject", "< next() returning TECHNOLOGY (\"" + mName + "\")");
                                return TECHNOLOGY;
                            }
                            break;
                        case STATE_TECHNOLOGY:
                            if (tag == ResearchTreeXmlParser.TAG_END_TECHNOLOGY) {
                                mNextState = STATE_TECHNOLOGIES;
                            }
                            break;
                    }

                } catch (XmlPullParserException | IOException e) {
                    throw new ResearchTreeXmlParserException("Invalid XML Research Tree", e);
                }
            }
            if (DEBUG)
                debug("ResearchProject", "* next() throw new XmlPullParserException(\"Invalid XML Research Tree\")");
            throw new ResearchTreeXmlParserException("Invalid XML Research Tree (tag " + tag + " at state " + mCurrentState + ")");
        }
    }

    @SuppressWarnings("unused")
    public static class ResearchTreeXmlParserException extends IOException {
        @SuppressWarnings("unused")
        public ResearchTreeXmlParserException() {
        }

        public ResearchTreeXmlParserException(String message) {
            super(message);
        }

        @SuppressWarnings("SameParameterValue")
        public ResearchTreeXmlParserException(String message, Throwable cause) {
            super(message, cause);
        }

        @SuppressWarnings("unused")
        public ResearchTreeXmlParserException(Throwable cause) {
            super(cause);
        }
    }
}
