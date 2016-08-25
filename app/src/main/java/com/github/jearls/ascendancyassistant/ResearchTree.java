package com.github.jearls.ascendancyassistant;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

//import android.util.Log;

/**
 * The research "tree" is really just a map of project names to
 * projects.
 *
 * @author Johnson Earls
 */
@SuppressWarnings("WeakerAccess")
public class ResearchTree implements Serializable {
    @SuppressWarnings("unused")
    private static final long serialVersionUID = 2016082400L;

    private final Map<String, ResearchProject> sResearchProjects = new HashMap<>();
    private transient final Set<OnResearchTreeChangeListener> sListeners = new HashSet<>();

    public ResearchTree() {
    }

    public static ResearchTree loadXmlResearchTree(XmlPullParser parser) throws IOException {
        ResearchProject project = null;
        ResearchTree tree = new ResearchTree();
        ResearchTreeXmlParser researchTreeXmlParser = new ResearchTreeXmlParser(parser);

        try {
            int event = researchTreeXmlParser.next();
            while (event != ResearchTreeXmlParser.DONE) {
                switch (event) {
                    case ResearchTreeXmlParser.RESEARCH_PROJECT:
                        project = tree.getResearchProject(researchTreeXmlParser.getName());
                        if (project == null) {
                            project = new ResearchProject(researchTreeXmlParser.getName());
                            tree.addResearchProject(project);
                        }
                        break;
                    case ResearchTreeXmlParser.DEPENDENCY:
                        ResearchProject dependency = tree.getResearchProject(researchTreeXmlParser.getName());
                        if (dependency == null) {
                            dependency = new ResearchProject(researchTreeXmlParser.getName());
                            tree.addResearchProject(dependency);
                        }
                        //noinspection ConstantConditions
                        assert project != null;
                        project.addDependency(dependency);
                        break;
                    case ResearchTreeXmlParser.TECHNOLOGY:
                        //noinspection ConstantConditions
                        assert project != null;
                        project.addTechnology(researchTreeXmlParser.getName());
                        break;
                }
            }
            return tree;
        } catch (XmlPullParserException e) {
            for (ResearchProject proj : tree.getResearchProjects()) {
                tree.removeResearchProject(proj);
                for (Iterator<ResearchProject> di = proj.getDependencies(); di.hasNext(); ) {
                    proj.removeDependency(di.next());
                }
            }
            throw new IOException(e.getMessage());
        }
    }

    public ResearchProject getResearchProject(String name) {
        return sResearchProjects.get(name);
    }

    public void addResearchProject(ResearchProject project) {
        sResearchProjects.put(project.getName(), project);
        this.triggerResearchProjectAdded(project);
    }

    @SuppressWarnings("unused")
    public void removeResearchProject(ResearchProject project) {
        sResearchProjects.remove(project.getName());
        this.triggerResearchProjectRemoved(project);
    }

    public Collection<ResearchProject> getResearchProjects() {
        return sResearchProjects.values();
    }

    public void addResearchTreeChangeListener(OnResearchTreeChangeListener listener) {
        this.sListeners.add(listener);
    }

    @SuppressWarnings("unused")
    public void removeResearchTreeChangeListener(OnResearchTreeChangeListener listener) {
        this.sListeners.remove(listener);
    }

    private void triggerResearchProjectAdded(ResearchProject project) {
        for (OnResearchTreeChangeListener listener : sListeners) {
            listener.onResearchProjectAdded(this, project);
        }
    }

    private void triggerResearchProjectRemoved(ResearchProject project) {
        for (OnResearchTreeChangeListener listener : sListeners) {
            listener.onResearchProjectRemoved(this, project);
        }
    }

    public String toString() {
        return "ResearchTree[" + this.sResearchProjects.size() + " projects]";
    }

    public interface OnResearchTreeChangeListener {
        void onResearchProjectAdded(ResearchTree researchTree, ResearchProject project);

        void onResearchProjectRemoved(ResearchTree researchTree, ResearchProject project);
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
            this.sParser = parser;
            mCurrentState = mNextState = STATE_START;
        }

        public String getName() {
            return mName;
        }

        public int next() throws XmlPullParserException {
            try {
                while (mCurrentState != STATE_SUCCESS && mCurrentState != STATE_ERROR) {
                    mCurrentState = mNextState;
                    mNextState = STATE_ERROR;
                    mName = null;
                    int tag = sParser.next();
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
                            tag = 0;
                    }

                    switch (mCurrentState) {
                        case STATE_START:
                            if (tag == ResearchTreeXmlParser.TAG_START_DOCUMENT) {
                                mNextState = STATE_START;
                            } else if (tag == ResearchTreeXmlParser.TAG_END_DOCUMENT) {
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
                                return TECHNOLOGY;
                            }
                            break;
                        case STATE_TECHNOLOGY:
                            if (tag == ResearchTreeXmlParser.TAG_END_TECHNOLOGY) {
                                mNextState = STATE_TECHNOLOGIES;
                            }
                            break;
                    }

                }
            } catch (XmlPullParserException | IOException e) {
                mNextState = STATE_ERROR;
            }
            throw new XmlPullParserException("Invalid XML Research Tree");
        }
    }

}

