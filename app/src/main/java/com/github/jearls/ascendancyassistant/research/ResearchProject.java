package com.github.jearls.ascendancyassistant;

import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
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

    private static final boolean                           DEBUG                        = false;
    private static final Map<String, ResearchProject>      sResearchTree                = new HashMap<>();
    private static final Map<String, ResearchProject>      sResearchTechnologies        = new HashMap<>();
    private static final Set<OnResearchChangeListener>     sResearchChangeListeners     = new HashSet<>();
    private static final Set<OnResearchPathChangeListener> sResearchPathChangeListeners = new HashSet<>();
    private static final List<ResearchProject>             sInitialResearch             = new ArrayList<>();
    private static final List<ResearchProject>             sGoalsList                   = new ArrayList<>();
    private static final List<ResearchProject>             sPathList                    = new ArrayList<>();
    private static final List<Method>                      batchTriggerMethods          = new ArrayList<>();
    private static final List<Object>                      batchTriggerInstances        = new ArrayList<>();
    private static final List<Object[]>                    batchTriggerParameters       = new ArrayList<>();
    private static       boolean                           batchTriggerMode             = false;
    private final String                                  sName;
    private final Set<String>                             sTechnologies;
    private final Set<ResearchProject>                    sDependents;
    private final Set<ResearchProject>                    sDependers;
    private final Set<OnResearchCompletedChangeListener>  sResearchCompletedChangeListeners;
    private final Set<OnResearchDependencyChangeListener> sResearchDependencyChangeListeners;
    private final Set<OnResearchTechnologyChangeListener> sResearchTechnologyChangeListeners;
    private       boolean                                 mCompleted;
    private       int                                     mGoalNumber;
    private       int                                     mPathNumber;

    public ResearchProject(String name) throws DuplicateResearchProjectException {
        this.sName = name;
        if (sResearchTree.containsKey(name)) {
            throw new DuplicateResearchProjectException(name);
        }
        // allocate objects here so that allocation doesn't happen on a DuplicateResearchProjectException
        this.sTechnologies = new HashSet<>();
        this.sDependents = new HashSet<>();
        this.sDependers = new HashSet<>();
        this.mCompleted = false;
        this.mGoalNumber = 0;
        this.mPathNumber = 0;
        this.sResearchCompletedChangeListeners = new HashSet<>();
        this.sResearchDependencyChangeListeners = new HashSet<>();
        this.sResearchTechnologyChangeListeners = new HashSet<>();
        sResearchTree.put(name, this);
        triggerResearchProjectAdded(this);
    }

    public static List<ResearchProject> getInitialResearch() {
        return Collections.unmodifiableList(sInitialResearch);
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

    public static void reset() {
        enterBatchTriggerMode();
        clearResearchChangeListeners();
        clearResearchPathChangeListeners();
        clearResearchProjects();
        clearResearchGoals();
        clearResearchPath();
        resetBatchTriggerMode();
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

    public static List<ResearchProject> getResearchGoals() {
        if (DEBUG)
            debug("ResearchProject", "> getResearchGoals()");
        List<ResearchProject> goalList = Collections.unmodifiableList(sGoalsList);
        if (DEBUG)
            debug("ResearchProject", "< getResearchGoals() returning " + goalList);
        return goalList;
    }

    public static List<ResearchProject> getResearchPath() {
        if (DEBUG)
            debug("ResearchProject", "> getResearchPath()");
        List<ResearchProject> pathList = Collections.unmodifiableList(sPathList);
        if (DEBUG)
            debug("ResearchProject", "< getResearchPath() returning " + pathList);
        return pathList;
    }

    public static ResearchProject getResearchProject(String name) {
        return sResearchTree.get(name);
    }

    public static ResearchProject getResearchProjectForTechnology(String technology) {
        return sResearchTechnologies.get(technology);
    }

    public static void clearResearchProjects() {
        while (sResearchTree.size() > 0) {
            ResearchProject researchProject = sResearchTree.values().iterator().next();
            removeResearchProject(researchProject);
        }
    }

    public static void clearResearchPath() {
        while (!sPathList.isEmpty()) {
            ResearchProject researchProject = sPathList.get(0);
            removeFromPath(researchProject);
        }
    }

    public static void clearResearchGoals() {
        while (!sGoalsList.isEmpty()) {
            ResearchProject researchProject = sGoalsList.get(0);
            removeFromGoals(researchProject);
        }
    }

    public static void removeResearchProject(ResearchProject researchProject) {
        // FIXME properly preserve and restore state when needed
        // FIXME clear dependencies before triggering
        sResearchTree.remove(researchProject.getName());
        triggerResearchProjectRemoved(researchProject);
        // remove from path and goal lists
        if (researchProject.mPathNumber > 0)
            researchProject.removeFromPath();
        if (researchProject.mGoalNumber > 0)
            researchProject.removeFromGoals();
        // clear listeners
        researchProject.clearResearchDependencyChangeListeners();
        researchProject.clearResearchTechnologyChangeListeners();
        researchProject.clearResearchCompletedChangeListeners();
        // clear dependency links
        while (researchProject.sDependents.size() > 0) {
            researchProject.removeDependent(researchProject.sDependents.iterator().next());
        }
        while (researchProject.sDependers.size() > 0) {
            researchProject.removeDepender(researchProject.sDependers.iterator().next());
        }
    }

    public static void loadXmlResearchTree(XmlPullParser parser) throws ResearchXmlParserException {
        if (DEBUG)
            debug("ResearchProject", "> loadXmlResearchTree(" + parser + ")");
        enterBatchTriggerMode();
        clearResearchProjects();
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
        } catch (ResearchXmlParserException e) {
            resetBatchTriggerMode();
            clearResearchProjects();
            throw e;
        }
        leaveBatchTriggerMode();
    }

    public static void loadXmlInitialResearch(XmlPullParser parser) throws ResearchXmlParserException {
        if (DEBUG)
            debug("ResearchProject", "> loadXmlInitialResearch(" + parser + ")");
        sInitialResearch.clear();
        ResearchProject          project;
        InitialResearchXmlParser initialResearchXmlParser = new InitialResearchXmlParser(parser);

        try {
            int event;
            while ((event = initialResearchXmlParser.next()) != InitialResearchXmlParser.DONE) {
                switch (event) {
                    case InitialResearchXmlParser.RESEARCH_PROJECT:
                        project = getResearchProject(initialResearchXmlParser.getName());
                        if (project != null)
                            sInitialResearch.add(project);
                        break;
                    case InitialResearchXmlParser.TECHNOLOGY:
                        project = getResearchProjectForTechnology(initialResearchXmlParser.getName());
                        if (project != null)
                            sInitialResearch.add(project);
                        break;
                }
            }
            if (DEBUG)
                debug("ResearchProject", "< loadXmlResearchTree(" + parser + ")");
        } catch (ResearchXmlParserException e) {
            resetBatchTriggerMode();
            clearResearchProjects();
            throw e;
        }
    }

    public static void addResearchPathChangeListener(OnResearchPathChangeListener listener) {
        sResearchPathChangeListeners.add(listener);
    }

    public static void removeResearchPathChangeListener(OnResearchPathChangeListener listener) {
        sResearchPathChangeListeners.remove(listener);
    }

    public static void clearResearchPathChangeListeners() {
        sResearchPathChangeListeners.clear();
    }

    private static void triggerOnResearchPathChange(ResearchProject added, ResearchProject removed) {
        if (ResearchProject.canTrigger("triggerOnResearchPathChange", ResearchProject.class, new Class<?>[]{ResearchProject.class, ResearchProject.class}, added, removed)) {
            for (OnResearchPathChangeListener listener : sResearchPathChangeListeners) {
                listener.onResearchPathChange(getResearchPath(), added, removed);
            }
        }
    }

    private static void triggerOnResearchGoalsChange(ResearchProject added, ResearchProject removed) {
        if (ResearchProject.canTrigger("triggerOnResearchGoalsChange", ResearchProject.class, new Class<?>[]{ResearchProject.class, ResearchProject.class}, added, removed)) {
            for (OnResearchPathChangeListener listener : sResearchPathChangeListeners) {
                listener.onResearchGoalsChange(getResearchGoals(), added, removed);
            }
        }
    }

    public static boolean isBatchTriggerMode() {
        return batchTriggerMode;
    }

    public static void enterBatchTriggerMode() {
        if (DEBUG) debug("ResearchProject", "> enterBatchTriggerMode");
        batchTriggerMode = true;
        if (DEBUG) debug("ResearchProject", "< enterBatchTriggerMode");
    }

    public static void resetBatchTriggerMode() {
        if (DEBUG) debug("ResearchProject", "> resetBatchTriggerMode");
        batchTriggerMode = false;
        batchTriggerMethods.clear();
        batchTriggerInstances.clear();
        batchTriggerParameters.clear();
        if (DEBUG) debug("ResearchProject", "< resetBatchTriggerMode");
    }

    public static void leaveBatchTriggerMode() {
        if (DEBUG) debug("ResearchProject", "> clearBatchTriggerMode");
        batchTriggerMode = false;
        callTriggers();
        if (DEBUG) debug("ResearchProject", "< clearBatchTriggerMode");
    }

    private static String strRepr(String str) {
        return "\"" + (str.replaceAll("([\\\\\"])", "\\$1").replace("\n", "\\n").replace("\r", "\\r").replace("\b", "\\b").replace("\t", "\\t").replace("\f", "\\f")) + "\"";
    }

    private static String arrayRepr(Object[] ary) {
        StringBuilder buf = new StringBuilder(ary.getClass().getClass().getName() + "[");
        for (int i = 0; i < ary.length; i += 1) {
            if (i > 0) {
                buf.append(", ");
            }
            buf.append(repr(ary[i]));
        }
        buf.append("]");
        return buf.toString();
    }

    private static String repr(Object foo) {
        if (foo == null) {
            return "null";
        }
        if (foo instanceof String) {
            return strRepr((String) foo);
        }
        if (foo instanceof Object[]) {
            return arrayRepr((Object[]) foo);
        }
        return foo.toString();
    }


    private static boolean canTrigger(String methodName, Object instance, Class<?>[] methodSignature, Object... callParams) {
        if (DEBUG)
            debug("ResearchProject", "> canTrigger(" + repr(methodName) + ", " + repr(instance) + ", " + repr(methodSignature) + ", " + repr(callParams) + ")");
        try {
            Class<?> clazz;
            if (instance instanceof Class) {
                clazz = (Class<?>) instance;
            } else {
                clazz = instance.getClass();
            }
            Method method = clazz.getDeclaredMethod(methodName, methodSignature);
            if (batchTriggerMode) {
                int i = batchTriggerMethods.indexOf(method);
                if ((i < 0) || (!Arrays.equals(callParams, batchTriggerParameters.get(i)))) {
                    batchTriggerMethods.add(method);
                    batchTriggerInstances.add(instance);
                    batchTriggerParameters.add(callParams);
                }
            }
            if (DEBUG)
                debug("ResearchProject", "< canTrigger(" + repr(methodName) + ", " + repr(instance) + ", " + repr(methodSignature) + ", " + repr(callParams) + ") returning " + (!batchTriggerMode));
            return !batchTriggerMode;
        } catch (NoSuchMethodException e) {
            if (DEBUG)
                debug("ResearchProject", "< canTrigger(" + repr(methodName) + ", " + repr(instance) + ", " + repr(methodSignature) + ", " + repr(callParams) + ") unable to locate trigger method");
            throw new RuntimeException("Unable to locate trigger method " + methodName + " on instance " + instance + " with signature " + repr(methodSignature), e);
        }
    }

    private static void callTriggers() {
        if (DEBUG) debug("ResearchProject", "> callTriggers()");
        if (!batchTriggerMode) {
            for (int i = 0; i < batchTriggerMethods.size(); i += 1) {
                Method trigger = batchTriggerMethods.get(i);
                trigger.setAccessible(true);
                Object   instance      = batchTriggerInstances.get(i);
                Object[] triggerParams = batchTriggerParameters.get(i);
                try {
                    trigger.invoke(instance, triggerParams);
                } catch (ReflectiveOperationException e) {
                    /* FIXME: IGNORED */
                }
            }
        }
        if (DEBUG) debug("ResearchProject", "< callTriggers()");
    }

    private static void triggerResearchProjectAdded(ResearchProject researchProject) {
        if (ResearchProject.canTrigger("triggerResearchProjectAdded", ResearchProject.class, new Class<?>[]{ResearchProject.class}, researchProject)) {
            for (OnResearchChangeListener listener : sResearchChangeListeners) {
                listener.onResearchProjectAdded(researchProject);
            }
        }
    }

    private static void triggerResearchProjectRemoved(ResearchProject researchProject) {
        if (ResearchProject.canTrigger("triggerResearchProjectRemoved", ResearchProject.class, new Class<?>[]{ResearchProject.class}, researchProject)) {
            for (OnResearchChangeListener listener : sResearchChangeListeners) {
                listener.onResearchProjectRemoved(researchProject);
            }
        }
    }

    public static void removeFromPath(ResearchProject researchProject) {
        if (DEBUG)
            debug("ResearchProject", "> removeFromPath(" + researchProject + ")");
        if (researchProject.mPathNumber > 0) {
            int index = researchProject.mPathNumber - 1;
            researchProject.mPathNumber = 0;
            sPathList.remove(index);
            while (index < sPathList.size()) {
                sPathList.get(index).mPathNumber = index + 1;
                index += 1;
            }
            triggerOnResearchPathChange(null, researchProject);
        }
        if (DEBUG)
            debug("ResearchProject", "< removeFromPath(" + researchProject + ")");
    }

    public static void addToPath(ResearchProject researchProject, int index) throws ArrayIndexOutOfBoundsException, DuplicateResearchProjectException {
        if (DEBUG)
            debug("ResearchProject", "> addToPath(" + researchProject + ", " + index + ")");
        if ((index < 1) || (index > sPathList.size() + 1))
            throw new ArrayIndexOutOfBoundsException(index);
        if (researchProject.mPathNumber != 0)
            throw new DuplicateResearchProjectException("already in path");
        researchProject.mPathNumber = index;
        sPathList.add(index - 1, researchProject);
        while (index < sPathList.size()) {
            sPathList.get(index).mPathNumber = index + 1;
            index += 1;
        }
        triggerOnResearchPathChange(researchProject, null);
        if (DEBUG)
            debug("ResearchProject", "< addToPath(" + researchProject + ", " + index + ")");
    }

    public static void addToPath(ResearchProject researchProject) throws ArrayIndexOutOfBoundsException, DuplicateResearchProjectException {
        addToPath(researchProject, sPathList.size() + 1);
    }

    public static void removeFromGoals(ResearchProject researchProject) {
        if (DEBUG)
            debug("ResearchProject", "> removeFromGoals(" + researchProject + ")");
        if (researchProject.mGoalNumber > 0) {
            int index = researchProject.mGoalNumber - 1;
            researchProject.mGoalNumber = 0;
            sGoalsList.remove(index);
            while (index < sGoalsList.size()) {
                sGoalsList.get(index).mGoalNumber = index + 1;
                index += 1;
            }
            triggerOnResearchGoalsChange(null, researchProject);
        }
        if (DEBUG)
            debug("ResearchProject", "< removeFromGoals(" + researchProject + ")");
    }

    public static void addToGoals(ResearchProject researchProject, int index) throws ArrayIndexOutOfBoundsException, DuplicateResearchProjectException {
        if (DEBUG)
            debug("ResearchProject", "> addToGoals(" + researchProject + ", " + index + ")");
        if ((index < 1) || (index > sGoalsList.size() + 1))
            throw new ArrayIndexOutOfBoundsException(index);
        if (researchProject.mGoalNumber != 0)
            throw new DuplicateResearchProjectException("already a goal");
        researchProject.mGoalNumber = index;
        sGoalsList.add(index - 1, researchProject);
        while (index < sGoalsList.size()) {
            sGoalsList.get(index).mGoalNumber = index + 1;
            index += 1;
        }
        triggerOnResearchGoalsChange(researchProject, null);
        if (DEBUG)
            debug("ResearchProject", "< addToGoals(" + researchProject + ", " + index + ")");
    }

    public static void addToGoals(ResearchProject researchProject) throws ArrayIndexOutOfBoundsException, DuplicateResearchProjectException {
        addToGoals(researchProject, sGoalsList.size() + 1);
    }

    public static void recomputePath() {
        if (DEBUG)
            debug("ResearchProject", "> recomputePath()");
        clearResearchPath();
        for (ResearchProject researchProject : sGoalsList) {
            addDependentsToPath(researchProject);
        }
        int i = 1;
        while (i < sGoalsList.size()) {
            ResearchProject researchProject = sGoalsList.get(i);
            if (sGoalsList.get(i - 1).mPathNumber > researchProject.mPathNumber) {
                researchProject.removeFromGoals();
                researchProject.addToGoals(i); // since the goals are 1-indexed and 'i' is 0-indexed, this moves it up
                if (i > 1) {
                    i -= 1;
                } else {
                    i += 1;
                }
            } else {
                i += 1;
            }
        }
        if (DEBUG)
            debug("ResearchProject", "< recomputePath()");
    }

    private static void addDependentsToPath(ResearchProject project) {
        if (!sPathList.contains(project)) {
            for (ResearchProject dependentProject : project.getDependents()) {
                if (!sPathList.contains(dependentProject)) {
                    addDependentsToPath(dependentProject);
                }
            }
            addToPath(project);
        }
    }

    public int hashCode() {
        return this.sName.hashCode();
    }

    public boolean isCompleted() {
        return mCompleted;
    }

    public void setCompleted(boolean completed) {
        if (this.mCompleted != completed) {
            this.mCompleted = completed;
            this.triggerOnResearchCompletedChange();
        }
    }

    public boolean isInPath() {
        return mPathNumber != 0;
    }

    public int getPathNumber() {
        return mPathNumber;
    }

    public void removeFromPath() {
        removeFromPath(this);
    }

    public void addToPath(int index) throws ArrayIndexOutOfBoundsException, DuplicateResearchProjectException {
        addToPath(this, index);
    }

    public void addToPath() throws ArrayIndexOutOfBoundsException, DuplicateResearchProjectException {
        addToPath(this);
    }

    public boolean isGoal() {
        return mGoalNumber != 0;
    }

    public int getGoalNumber() {
        return mGoalNumber;
    }

    public void removeFromGoals() {
        removeFromGoals(this);
    }

    public void addToGoals(int index) throws ArrayIndexOutOfBoundsException, DuplicateResearchProjectException {
        addToGoals(this, index);
    }

    public void addToGoals() throws ArrayIndexOutOfBoundsException, DuplicateResearchProjectException {
        addToGoals(this, sGoalsList.size() + 1);
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

    public Iterator<String> getTechnologies() {
        return sTechnologies.iterator();
    }

    public void addTechnology(String technology) {
        sTechnologies.add(technology);
        sResearchTechnologies.put(technology, this);
        triggerTechnologyAdded(technology);
    }

    public void removeTechnology(String technology) {
        sTechnologies.remove(technology);
        sResearchTechnologies.remove(technology);
        triggerTechnologyRemoved(technology);
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

    public Collection<ResearchProject> getDependents() {
        return Collections.unmodifiableCollection(sDependents);
    }

    public void addDependent(ResearchProject dependent) {
        sDependents.add(dependent);
        dependent.sDependers.add(this);
        this.triggerDependentAdded(dependent);
        dependent.triggerDependerAdded(this);
    }

    public void removeDependent(ResearchProject dependent) {
        sDependents.remove(dependent);
        dependent.sDependers.remove(this);
        this.triggerDependentRemoved(dependent);
        dependent.triggerDependerRemoved(this);
    }

    public boolean hasDependent(ResearchProject dependent) {
        return sDependents.contains(dependent);
    }

    public Collection<ResearchProject> getDependers() {
        return Collections.unmodifiableCollection(sDependers);
    }

    public void addDepender(ResearchProject depender) {
        sDependers.add(depender);
        depender.sDependents.add(this);
        this.triggerDependerAdded(depender);
        depender.triggerDependentAdded(this);
    }

    public void removeDepender(ResearchProject depender) {
        sDependers.remove(depender);
        depender.sDependents.remove(this);
        this.triggerDependerRemoved(depender);
        depender.triggerDependentRemoved(this);
    }

    public boolean hasDepender(ResearchProject depender) {
        return sDependers.contains(depender);
    }

    public void addResearchCompletedChangeListener(OnResearchCompletedChangeListener listener) {
        this.sResearchCompletedChangeListeners.add(listener);
    }

    public void removeResearchCompletedChangeListener(OnResearchCompletedChangeListener listener) {
        this.sResearchCompletedChangeListeners.remove(listener);
    }

    public void clearResearchCompletedChangeListeners() {
        this.sResearchCompletedChangeListeners.clear();
    }

    public String toString() {
        return "ResearchProject(\"" + this.sName + "\")";
    }

    public void triggerTechnologyAdded(String technology) {
        if (ResearchProject.canTrigger("triggerTechnologyAdded", this, new Class<?>[]{String.class}, technology)) {
            for (OnResearchTechnologyChangeListener listener : this.sResearchTechnologyChangeListeners) {
                listener.onTechnologyAdded(this, technology);
            }
        }
    }

    public void triggerTechnologyRemoved(String technology) {
        if (ResearchProject.canTrigger("triggerTechnologyRemoved", this, new Class<?>[]{String.class}, technology)) {
            for (OnResearchTechnologyChangeListener listener : this.sResearchTechnologyChangeListeners) {
                listener.onTechnologyRemoved(this, technology);
            }
        }
    }

    public void triggerDependentAdded(ResearchProject dependent) {
        if (ResearchProject.canTrigger("triggerDependentAdded", this, new Class<?>[]{
                ResearchProject.class
        }, dependent)) {
            for (OnResearchDependencyChangeListener listener : this.sResearchDependencyChangeListeners) {
                listener.onDependentAdded(this, dependent);
            }
        }
    }

    public void triggerDependentRemoved(ResearchProject dependent) {
        if (ResearchProject.canTrigger("triggerDependentRemoved", this, new Class<?>[]{ResearchProject.class}, dependent)) {
            for (OnResearchDependencyChangeListener listener : this.sResearchDependencyChangeListeners) {
                listener.onDependentRemoved(this, dependent);
            }
        }
    }

    public void triggerDependerAdded(ResearchProject depender) {
        if (ResearchProject.canTrigger("triggerDependerAdded", this, new Class<?>[]{ResearchProject.class}, depender)) {
            for (OnResearchDependencyChangeListener listener : this.sResearchDependencyChangeListeners) {
                listener.onDependerAdded(this, depender);
            }
        }
    }

    public void triggerDependerRemoved(ResearchProject depender) {
        if (ResearchProject.canTrigger("triggerDependerRemoved", this, new Class<?>[]{ResearchProject.class}, depender)) {
            for (OnResearchDependencyChangeListener listener : this.sResearchDependencyChangeListeners) {
                listener.onDependerRemoved(this, depender);
            }
        }
    }

    private void triggerOnResearchCompletedChange() {
        if (ResearchProject.canTrigger("triggerOnResearchCompletedChange", this, new Class<?>[]{})) {
            for (OnResearchCompletedChangeListener listener : this.sResearchCompletedChangeListeners) {
                listener.onResearchCompletedChange(this, this.mCompleted);
            }
        }
    }

    @SuppressWarnings("UnusedParameters")
    public interface OnResearchCompletedChangeListener {
        void onResearchCompletedChange(ResearchProject project, boolean completed);
    }

    @SuppressWarnings("UnusedParameters")
    public interface OnResearchPathChangeListener {
        void onResearchPathChange(List<ResearchProject> pathList, ResearchProject added, ResearchProject removed);

        void onResearchGoalsChange(List<ResearchProject> goalsList, ResearchProject added, ResearchProject removed);
    }

    @SuppressWarnings({"UnusedParameters", "RedundantThrows"})
    public interface OnResearchChangeListener {
        void onResearchProjectAdded(ResearchProject project);

        void onResearchProjectRemoved(ResearchProject project);
    }

    @SuppressWarnings("UnusedParameters")
    public interface OnResearchDependencyChangeListener {
        void onDependentAdded(ResearchProject project, ResearchProject dependent);

        void onDependentRemoved(ResearchProject project, ResearchProject dependent);

        void onDependerAdded(ResearchProject project, ResearchProject depender);

        void onDependerRemoved(ResearchProject project, ResearchProject depender);
    }

    @SuppressWarnings("UnusedParameters")
    public interface OnResearchTechnologyChangeListener {
        void onTechnologyAdded(ResearchProject project, String technology);

        void onTechnologyRemoved(ResearchProject project, String technology);
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

        public int next() throws ResearchXmlParserException {
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
                    throw new ResearchXmlParserException("Invalid XML Research Tree", e);
                }
            }
            if (DEBUG)
                debug("ResearchProject", "* next() throw new XmlPullParserException(\"Invalid XML Research Tree\")");
            throw new ResearchXmlParserException("Invalid XML Research Tree (tag " + tag + " at state " + mCurrentState + ")");
        }
    }

    @SuppressWarnings("unused")
    public static class ResearchXmlParserException extends IOException {
        @SuppressWarnings("unused")
        public ResearchXmlParserException() {
        }

        public ResearchXmlParserException(String message) {
            super(message);
        }

        @SuppressWarnings("SameParameterValue")
        public ResearchXmlParserException(String message, Throwable cause) {
            super(message, cause);
        }

        @SuppressWarnings("unused")
        public ResearchXmlParserException(Throwable cause) {
            super(cause);
        }
    }

    private static class InitialResearchXmlParser {
        public static final int DONE             = 0;
        public static final int RESEARCH_PROJECT = 1;
        public static final int DEPENDENCY       = 2;
        public static final int TECHNOLOGY       = 3;

        private static final int TAG_START_DOCUMENT         = 1;
        private static final int TAG_END_DOCUMENT           = 2;
        private static final int TAG_START_INITIAL_RESEARCH = "initialresearch".hashCode();
        private static final int TAG_END_INITIAL_RESEARCH   = TAG_START_INITIAL_RESEARCH + 1;
        private static final int TAG_START_RESEARCH_PROJECT = "researchproject".hashCode();
        private static final int TAG_END_RESEARCH_PROJECT   = TAG_START_RESEARCH_PROJECT + 1;
        private static final int TAG_START_TECHNOLOGY       = "technology".hashCode();
        private static final int TAG_END_TECHNOLOGY         = TAG_START_TECHNOLOGY + 1;

        private final int STATE_START            = 0;
        private final int STATE_SUCCESS          = 1;
        private final int STATE_ERROR            = 2;
        private final int STATE_INITIAL_RESEARCH = 3;
        private final int STATE_RESEARCH_PROJECT = 4;
        private final int STATE_TECHNOLOGY       = 5;

        private final XmlPullParser sParser;
        private       String        mName;

        private int mCurrentState, mNextState;


        public InitialResearchXmlParser(XmlPullParser parser) {
            if (DEBUG)
                debug("ResearchProject", "> InitialResearchXmlParser(" + parser + ")");
            this.sParser = parser;
            mCurrentState = mNextState = STATE_START;
            if (DEBUG)
                debug("ResearchProject", "< InitialResearchXmlParser(" + parser + ")");
        }

        public String getName() {
            if (DEBUG) debug("ResearchProject", "> getName()");
            if (DEBUG)
                debug("ResearchProject", "< getName() returning \"" + mName + "\"");
            return mName;
        }

        public int next() throws ResearchXmlParserException {
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
                            if (tag == InitialResearchXmlParser.TAG_START_DOCUMENT) {
                                mNextState = STATE_START;
                            } else if (tag == InitialResearchXmlParser.TAG_END_DOCUMENT) {
                                if (DEBUG)
                                    debug("ResearchProject", "< next() returning DONE");
                                return DONE;
                            } else if (tag == InitialResearchXmlParser.TAG_START_INITIAL_RESEARCH) {
                                mNextState = STATE_INITIAL_RESEARCH;
                            }
                            break;
                        case STATE_INITIAL_RESEARCH:
                            if (tag == InitialResearchXmlParser.TAG_END_INITIAL_RESEARCH) {
                                mNextState = STATE_START;
                            } else if (tag == InitialResearchXmlParser.TAG_START_RESEARCH_PROJECT) {
                                mNextState = STATE_RESEARCH_PROJECT;
                                if (DEBUG)
                                    debug("ResearchProject", "< next() returning RESEARCH_PROJECT (\"" + mName + "\")");
                                return RESEARCH_PROJECT;
                            } else if (tag == InitialResearchXmlParser.TAG_START_TECHNOLOGY) {
                                mNextState = STATE_TECHNOLOGY;
                                if (DEBUG)
                                    debug("ResearchProject", "< next() returning TECHNOLOGY (\"" + mName + "\")");
                                return TECHNOLOGY;
                            }
                            break;
                        case STATE_RESEARCH_PROJECT:
                            if (tag == InitialResearchXmlParser.TAG_END_RESEARCH_PROJECT) {
                                mNextState = STATE_INITIAL_RESEARCH;
                            }
                            break;
                        case STATE_TECHNOLOGY:
                            if (tag == InitialResearchXmlParser.TAG_END_TECHNOLOGY) {
                                mNextState = STATE_INITIAL_RESEARCH;
                            }
                            break;
                    }

                } catch (XmlPullParserException | IOException e) {
                    throw new ResearchXmlParserException("Invalid XML Research Tree", e);
                }
            }
            if (DEBUG)
                debug("ResearchProject", "* next() throw new XmlPullParserException(\"Invalid XML Research Tree\")");
            throw new ResearchXmlParserException("Invalid XML Research Tree (tag " + tag + " at state " + mCurrentState + ")");
        }
    }
}
