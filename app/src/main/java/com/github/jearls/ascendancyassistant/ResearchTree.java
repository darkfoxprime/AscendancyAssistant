package com.github.jearls.ascendancyassistant;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * The research "tree" is really just a map of project names to
 * projects.
 *
 * @author Johnson Earls
 */
public class ResearchTree {
    Map<String, ResearchProject> researchProjects = new HashMap<>();

    public ResearchTree() {
    }

    public static ResearchTree loadXmlResearchTree(XmlPullParser parser) throws IOException {
        final int STATE_START = 0;
        final int STATE_SUCCESS = 1;
        final int STATE_ERROR = 2;
        final int STATE_DOCUMENT = 3;
        final int STATE_RESEARCH_TREE = 4;
        final int STATE_RESEARCH_PROJECT = 5;
        final int STATE_REQUIREMENTS = 6;
        final int STATE_REQUIRES = 7;
        final int STATE_TECHNOLOGIES = 8;
        final int STATE_TECHNOLOGY = 9;

        final int TAG_START_DOCUMENT = 1;
        final int TAG_END_DOCUMENT = 2;
        final int TAG_START_RESEARCH_TREE = "researchtree".hashCode();
        final int TAG_END_RESEARCH_TREE = TAG_START_RESEARCH_TREE + 1;
        final int TAG_START_RESEARCH_PROJECT = "researchproject".hashCode();
        final int TAG_END_RESEARCH_PROJECT = TAG_START_RESEARCH_PROJECT + 1;
        final int TAG_START_REQUIREMENTS = "requirements".hashCode();
        final int TAG_END_REQUIREMENTS = TAG_START_REQUIREMENTS + 1;
        final int TAG_START_REQUIRES = "requires".hashCode();
        final int TAG_END_REQUIRES = TAG_START_REQUIRES + 1;
        final int TAG_START_TECHNOLOGIES = "technologies".hashCode();
        final int TAG_END_TECHNOLOGIES = TAG_START_TECHNOLOGIES + 1;
        final int TAG_START_TECHNOLOGY = "technology".hashCode();
        final int TAG_END_TECHNOLOGY = TAG_START_TECHNOLOGY + 1;

        int nextState = STATE_START, state = STATE_START;
        ResearchProject project = null;
        ResearchTree tree = new ResearchTree();
        int tag = -1;

        while (nextState != STATE_ERROR && nextState != STATE_SUCCESS) {
            state = nextState;
            nextState = STATE_ERROR;
            try {
                tag = parser.next();
                String name = null;
                if (tag == parser.TEXT) {
                    nextState = state;
                    continue;
                }
                switch (tag) {
                    case XmlPullParser.START_DOCUMENT:
                        tag = TAG_START_DOCUMENT;
                        break;
                    case XmlPullParser.END_DOCUMENT:
                        tag = TAG_END_DOCUMENT;
                        break;
                    case XmlPullParser.START_TAG:
                        tag = parser.getName().toLowerCase().hashCode();
                        name = parser.getAttributeValue(null, "name");
                        break;
                    case XmlPullParser.END_TAG:
                        tag = parser.getName().toLowerCase().hashCode() + 1;
                        break;
                    default:
                        tag = 0;
                }
                switch (state) {
                    case STATE_START:
                        if (tag == TAG_END_DOCUMENT) {
                            nextState = STATE_SUCCESS;
                        } else if (tag == TAG_START_RESEARCH_TREE) {
                            nextState = STATE_RESEARCH_TREE;
                        }
                        break;
                    case STATE_RESEARCH_TREE:
                        if (tag == TAG_END_RESEARCH_TREE) {
                            nextState = STATE_START;
                        } else if (tag == TAG_START_RESEARCH_PROJECT) {
                            nextState = STATE_RESEARCH_PROJECT;
                            project = tree.researchProjects.get(name);
                            if (project == null) {
                                project = new ResearchProject(name);
                                tree.researchProjects.put(name, project);
                            }
                        }
                        break;
                    case STATE_RESEARCH_PROJECT:
                        if (tag == TAG_END_RESEARCH_PROJECT) {
                            nextState = STATE_RESEARCH_TREE;
                        } else if (tag == TAG_START_REQUIREMENTS) {
                            nextState = STATE_REQUIREMENTS;
                        } else if (tag == TAG_START_TECHNOLOGIES) {
                            nextState = STATE_TECHNOLOGIES;
                        }
                        break;
                    case STATE_REQUIREMENTS:
                        if (tag == TAG_END_REQUIREMENTS) {
                            nextState = STATE_RESEARCH_PROJECT;
                        } else if (tag == TAG_START_REQUIRES) {
                            nextState = STATE_REQUIRES;
                            ResearchProject dependency = tree.researchProjects.get(name);
                            if (dependency == null) {
                                dependency = new ResearchProject(name);
                                tree.researchProjects.put(name, dependency);
                            }
                            project.addDependency(dependency);
                        }
                        break;
                    case STATE_REQUIRES:
                        if (tag == TAG_END_REQUIRES) {
                            nextState = STATE_REQUIREMENTS;
                        }
                        break;
                    case STATE_TECHNOLOGIES:
                        if (tag == TAG_END_TECHNOLOGIES) {
                            nextState = STATE_RESEARCH_PROJECT;
                        } else if (tag == TAG_START_TECHNOLOGY) {
                            nextState = STATE_TECHNOLOGY;
                            project.addTechnology(name);
                        }
                        break;
                    case STATE_TECHNOLOGY:
                        if (tag == TAG_END_TECHNOLOGY) {
                            nextState = STATE_TECHNOLOGIES;
                        }
                        break;
                }
            } catch (XmlPullParserException | IOException e) {
                nextState = STATE_ERROR;
            }
        }
        if (nextState != STATE_SUCCESS) {
            throw new IOException("Invalid XML research tree");
        }
        return tree;
    }

    public ResearchProject getResearchProject(String name) {
        return researchProjects.get(name);
    }

    public void addResearchProject(ResearchProject project) {
        researchProjects.put(project.getName(), project);
    }

    public void removeResearchProject(ResearchProject project) {
        researchProjects.remove(project.getName());
    }

    public Collection<ResearchProject> getResearchProjects() {
        return researchProjects.values();
    }

}
