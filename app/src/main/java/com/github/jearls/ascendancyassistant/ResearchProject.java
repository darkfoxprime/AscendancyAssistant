package com.github.jearls.ascendancyassistant;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * A research project consists of a name, a set of technologies, a set
 * of dependencies on other reesearch projects, flags indicating if the
 * project is complete, is a goal, or is in the research path towards a
 * goal, and goal and research path numbers, if the associated flags are
 * true.
 *
 * @author Johnson Earls
 */
public class ResearchProject {
    String name;
    Set<String> technologies;
    Set<ResearchProject> dependencies;
    boolean complete;
    boolean goal;
    boolean inPath;
    int goalNumber;
    int pathNumber;

    public ResearchProject(String name) {
        this.name = name;
        this.technologies = new HashSet<>();
        this.dependencies = new HashSet<>();
        this.complete = false;
        this.goal = false;
        this.inPath = false;
        this.goalNumber = 0;
        this.pathNumber = 0;
    }

    public boolean isInPath() {
        return inPath;
    }

    public void setInPath(boolean inPath) {
        this.inPath = inPath;
    }

    public int getPathNumber() {
        return pathNumber;
    }

    public void setPathNumber(int pathNumber) {
        this.pathNumber = pathNumber;
    }

    public boolean isComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }

    public boolean isGoal() {
        return goal;
    }

    public void setGoal(boolean goal) {
        this.goal = goal;
    }

    public int getGoalNumber() {
        return goalNumber;
    }

    public void setGoalNumber(int goalNumber) {
        this.goalNumber = goalNumber;
    }

    public String getName() {
        return name;
    }

    public Iterator<String> getTechnologies() {
        return technologies.iterator();
    }

    public void addTechnology(String technology) {
        technologies.add(technology);
    }

    public void removeTechnology(String technology) {
        technologies.remove(technology);
    }

    public boolean hasTechnology(String technology) {
        return technologies.contains(technology);
    }

    public Iterator<ResearchProject> getDependencies() {
        return dependencies.iterator();
    }

    public void addDependency(ResearchProject dependency) {
        dependencies.add(dependency);
    }

    public void removeDependency(ResearchProject dependency) {
        dependencies.remove(dependency);
    }

    public boolean hasDependency(String dependency) {
        return dependencies.contains(dependency);
    }
}
