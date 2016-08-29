package com.github.jearls.ascendancyassistant;

import android.annotation.SuppressLint;
import android.util.Log;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Iterator;

import static org.easymock.EasyMock.anyString;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


@SuppressWarnings("unused")
@RunWith(PowerMockRunner.class)
@PrepareForTest(Log.class)
public class ResearchProjectTest {
    private static final boolean DEBUG = false;
    private static final String TAG = "ResearchProjectTest";

    private static void debug(String msg) {
        System.err.println(ResearchProjectTest.TAG + ": " + msg);
    }

////////////////////////////////////////////////////////////////////////

    private static String strRepr(String str) {
        return "\"" + (str.replaceAll("([\\\\\"])", "\\$1").replace("\n", "\\n").replace("\r", "\\r").replace("\b", "\\b").replace("\t", "\\t").replace("\f", "\\f")) + "\"";
    }

    private static String arrayRepr(Object[] ary) {
        StringBuilder buf = new StringBuilder("[");
        for (int i = 0; i < ary.length; i += 1) {
            if (i > 0) {
                buf.append(", ");
            }
            buf.append(repr(ary[i]));
        }
        buf.append("]");
        return buf.toString();
    }

////////////////////////////////////////////////////////////////////////

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

    @SuppressLint("LogConditional")
    @Before
    public void mockLogClass() {
        PowerMock.mockStatic(Log.class);
        expect(Log.d(anyString(), anyString())).andReturn(0).anyTimes();
        PowerMock.replay(Log.class);
    }

    @Before
    public void clearResearchProjects() {
        ResearchProject.clearResearchChangeListeners();
        ResearchProject.clearResearchProjects();
    }

    private void assertResearchHasProjectCount(int count) {
        Iterator<ResearchProject> pi = ResearchProject.getAllResearchProjects().iterator();
        int found = 0;
        while (pi.hasNext()) {
            found += 1;
            pi.next();
        }
        assertEquals("Expected " + count + " research projects, found " + found, count, found);
    }

    private void assertResearchHasProjectNamed(String project) {
        assertNotNull("Expected to find research project " + project, ResearchProject.getResearchProject(project));
    }

    private void assertNamedProjectHasDependentCount(String project, int count) {
        assertResearchHasProjectNamed(project);
        int found = ResearchProject.getResearchProject(project).getDependents().size();
        assertEquals("Expected " + count + " dependents, found " + found, count, found);
    }

    private void assertNamedProjectHasDependentNamed(String project, String dep) {
        assertResearchHasProjectNamed(project);
        assertResearchHasProjectNamed(dep);
        assertNotNull("Expected to find dependent " + dep, ResearchProject.getResearchProject(project).hasDependent(ResearchProject.getResearchProject(dep)));
    }

    private void assertNamedProjectHasDependerCount(String project, int count) {
        assertResearchHasProjectNamed(project);
        int found = ResearchProject.getResearchProject(project).getDependers().size();
        assertEquals("Expected " + count + " dependers, found " + found, count, found);
    }

////////////////////////////////////////////////////////////////////////

    private void assertNamedProjectHasDependerNamed(String project, String dep) {
        assertResearchHasProjectNamed(project);
        assertResearchHasProjectNamed(dep);
        assertNotNull("Expected to find depender " + dep, ResearchProject.getResearchProject(project).hasDepender(ResearchProject.getResearchProject(dep)));
    }

    private void assertNamedProjectHasTechnologyCount(String project, int count) {
        assertResearchHasProjectNamed(project);
        Iterator<String> pi = ResearchProject.getResearchProject(project).getTechnologies();
        int found = 0;
        while (pi.hasNext()) {
            found += 1;
            pi.next();
        }
        assertEquals("Expected " + count + " sDependencies, found " + found, count, found);
    }

    private void assertNamedProjectHasTechnologyNamed(String project, String tech) {
        assertResearchHasProjectNamed(project);
        assertNotNull("Expected to find technology " + tech, ResearchProject.getResearchProject(project).hasTechnology(tech));
    }

    @Test
    public void testResearchProject() {
        new ResearchProject("project");
        assertResearchHasProjectNamed("project");
        assertNamedProjectHasDependentCount("project", 0);
        assertNamedProjectHasDependerCount("project", 0);
        assertNamedProjectHasTechnologyCount("project", 0);
    }

    @Test
    public void testUniqueResearchProjects() {
        new ResearchProject("project");
        new ResearchProject("project 2");
        assertResearchHasProjectCount(2);
        assertResearchHasProjectNamed("project");
        assertResearchHasProjectNamed("project 2");
    }

    @Test(expected = ResearchProject.DuplicateResearchProjectException.class)
    public void testDuplicateResearchProjects() {
        new ResearchProject("project");
        new ResearchProject("project");
    }

    @Test
    public void testResearchProjectParameters() {
        ResearchProject researchProject = new ResearchProject("project");
        researchProject.setCompleted(true);
        researchProject.addToPath();
        researchProject.addToGoals();
        assertEquals(researchProject.isCompleted(), true);
        assertEquals(researchProject.isGoal(), true);
        assertEquals(researchProject.isInPath(), true);
        assertEquals(researchProject.getPathNumber(), 1);
        assertEquals(researchProject.getGoalNumber(), 1);
    }

    @Test
    public void testResearchChangeListenerOnAdd() {
        ResearchChangeListener listener = new ResearchChangeListener();
        ResearchProject.addResearchChangeListener(listener);
        new ResearchProject("project");
        assertEquals(listener.projectsAdded, 1);
        assertEquals(listener.projectsRemoved, 0);
    }

    @Test
    public void testResearchChangeListenerOnRemove() {
        ResearchProject researchProject = new ResearchProject("project");
        ResearchChangeListener listener = new ResearchChangeListener();
        ResearchProject.addResearchChangeListener(listener);
        ResearchProject.removeResearchProject(researchProject);
        assertEquals(listener.projectsAdded, 0);
        assertEquals(listener.projectsRemoved, 1);
    }

    @Test
    public void testRemoveResearchChangeListener() {
        ResearchChangeListener listener = new ResearchChangeListener();
        ResearchProject.addResearchChangeListener(listener);
        ResearchProject researchProject = new ResearchProject("project");
        ResearchProject.removeResearchChangeListener(listener);
        ResearchProject.removeResearchProject(researchProject);
        assertEquals(listener.projectsAdded, 1);
        assertEquals(listener.projectsRemoved, 0);
    }

    @Test
    public void testResearchTechnologyChangeListenerOnAddTechnology() {
        ResearchProject researchProject = new ResearchProject("project");
        ResearchTechnologyChangeListener listener = new ResearchTechnologyChangeListener();
        researchProject.addResearchTechnologyChangeListener(listener);
        researchProject.addTechnology("tech");
        assertEquals(listener.technologiesAdded, 1);
        assertEquals(listener.technologiesRemoved, 0);
    }

    @Test
    public void testResearchTechnologyChangeListenerOnRemoveTechnology() {
        ResearchProject researchProject = new ResearchProject("project");
        researchProject.addTechnology("tech");
        ResearchTechnologyChangeListener listener = new ResearchTechnologyChangeListener();
        researchProject.addResearchTechnologyChangeListener(listener);
        researchProject.removeTechnology("tech");
        assertEquals(listener.technologiesAdded, 0);
        assertEquals(listener.technologiesRemoved, 1);
    }

    @Test
    public void testRemoveResearchTechnologyChangeListener() {
        ResearchProject researchProject = new ResearchProject("project");
        ResearchTechnologyChangeListener listener = new ResearchTechnologyChangeListener();
        researchProject.addResearchTechnologyChangeListener(listener);
        researchProject.addTechnology("tech");
        researchProject.removeResearchTechnologyChangeListener(listener);
        researchProject.removeTechnology("tech");
        assertEquals(listener.technologiesAdded, 1);
        assertEquals(listener.technologiesRemoved, 0);
    }

    @Test
    public void testResearchDependencyChangeListenerOnAddDependent() {
        ResearchProject researchProject1 = new ResearchProject("project 1");
        ResearchProject researchProject2 = new ResearchProject("project 2");
        ResearchDependencyChangeListener listener = new ResearchDependencyChangeListener();
        researchProject1.addResearchDependencyChangeListener(listener);
        researchProject1.addDependent(researchProject2);
        assertEquals(listener.dependentsAdded, 1);
        assertEquals(listener.dependentsRemoved, 0);
        assertEquals(listener.dependersAdded, 0);
        assertEquals(listener.dependersRemoved, 0);
    }

    @Test
    public void testResearchDependencyChangeListenerOnAddDepender() {
        ResearchProject researchProject1 = new ResearchProject("project 1");
        ResearchProject researchProject2 = new ResearchProject("project 2");
        ResearchDependencyChangeListener listener = new ResearchDependencyChangeListener();
        researchProject1.addResearchDependencyChangeListener(listener);
        researchProject1.addDepender(researchProject2);
        assertEquals(listener.dependentsAdded, 0);
        assertEquals(listener.dependentsRemoved, 0);
        assertEquals(listener.dependersAdded, 1);
        assertEquals(listener.dependersRemoved, 0);
    }

    @Test
    public void testResearchDependencyChangeListenerOnRemoveDependent() {
        ResearchProject researchProject1 = new ResearchProject("project 1");
        ResearchProject researchProject2 = new ResearchProject("project 2");
        researchProject1.addDependent(researchProject2);
        ResearchDependencyChangeListener listener = new ResearchDependencyChangeListener();
        researchProject1.addResearchDependencyChangeListener(listener);
        researchProject1.removeDependent(researchProject2);
        assertEquals(listener.dependentsAdded, 0);
        assertEquals(listener.dependentsRemoved, 1);
        assertEquals(listener.dependersAdded, 0);
        assertEquals(listener.dependersRemoved, 0);
    }

    @Test
    public void testResearchDependencyChangeListenerOnRemoveDepender() {
        ResearchProject researchProject1 = new ResearchProject("project 1");
        ResearchProject researchProject2 = new ResearchProject("project 2");
        researchProject1.addDepender(researchProject2);
        ResearchDependencyChangeListener listener = new ResearchDependencyChangeListener();
        researchProject1.addResearchDependencyChangeListener(listener);
        researchProject1.removeDepender(researchProject2);
        assertEquals(listener.dependentsAdded, 0);
        assertEquals(listener.dependentsRemoved, 0);
        assertEquals(listener.dependersAdded, 0);
        assertEquals(listener.dependersRemoved, 1);
    }

    @Test
    public void testRemoveResearchDependencyChangeListener() {
        ResearchProject researchProject1 = new ResearchProject("project 1");
        ResearchProject researchProject2 = new ResearchProject("project 2");
        ResearchDependencyChangeListener listener = new ResearchDependencyChangeListener();
        researchProject1.addResearchDependencyChangeListener(listener);
        researchProject1.addDepender(researchProject2);
        researchProject1.removeResearchDependencyChangeListener(listener);
        researchProject1.removeDepender(researchProject2);
        assertEquals(listener.dependersAdded, 1);
        assertEquals(listener.dependersRemoved, 0);
    }

    @Test
    public void testResearchCompletedChangeListener() {
        ResearchProject                 researchProject = new ResearchProject("project");
        ResearchCompletedChangeListener listener        = new ResearchCompletedChangeListener();
        researchProject.addResearchCompletedChangeListener(listener);
        researchProject.setCompleted(!researchProject.isCompleted());
        assertEquals(listener.countCompletedChange, 1);
        researchProject.setCompleted(researchProject.isCompleted());
        assertEquals(listener.countCompletedChange, 1);
    }

    @Test
    public void testRemoveResearchStatusChangeListener() {
        ResearchProject                 researchProject = new ResearchProject("project");
        ResearchCompletedChangeListener listener        = new ResearchCompletedChangeListener();
        researchProject.addResearchCompletedChangeListener(listener);
        researchProject.setCompleted(!researchProject.isCompleted());
        assertEquals(listener.countCompletedChange, 1);
        researchProject.removeResearchCompletedChangeListener(listener);
        researchProject.setCompleted(!researchProject.isCompleted());
        assertEquals(listener.countCompletedChange, 1);
    }

    @Test
    public void testEmptyDocument() throws ResearchProject.ResearchXmlParserException {
        DummyXmlEvent[] events = {
                new DummyXmlEndDocument(),
        };
        XmlPullParser xpp = new DummyXmlPullParser(events);
        ResearchProject.loadXmlResearchTree(xpp);
        assertResearchHasProjectCount(0);
    }

////////////////////////////////////////////////////////////////////////

    @Test
    public void testEmptyDocumentWithStartDocumentEvent() throws ResearchProject.ResearchXmlParserException {
        DummyXmlEvent[] events = {
                new DummyXmlStartDocument(),
                new DummyXmlEndDocument(),
        };
        XmlPullParser xpp = new DummyXmlPullParser(events);
        ResearchProject.loadXmlResearchTree(xpp);
        assertResearchHasProjectCount(0);
    }

    @Test
    public void testResearchTreeEmpty() throws ResearchProject.ResearchXmlParserException {
        DummyXmlEvent[] events = {
                new DummyXmlEmptyStartTag("ResearchTree"),
                new DummyXmlEndTag("ResearchTree"),
                new DummyXmlEndDocument(),
        };
        XmlPullParser xpp = new DummyXmlPullParser(events);
        ResearchProject.loadXmlResearchTree(xpp);
        assertResearchHasProjectCount(0);
    }

    @Test
    public void testResearchTreeWithTextContent() throws ResearchProject.ResearchXmlParserException {
        DummyXmlEvent[] events = {
                new DummyXmlStartTag("ResearchTree"),
                new DummyXmlText("This is some text"),
                new DummyXmlEndTag("ResearchTree"),
                new DummyXmlEndDocument(),
        };
        XmlPullParser xpp = new DummyXmlPullParser(events);
        ResearchProject.loadXmlResearchTree(xpp);
        assertResearchHasProjectCount(0);
    }

    @Test
    public void testResearchTreeWithSingleProject() throws ResearchProject.ResearchXmlParserException {
        DummyXmlEvent[] events = {
                new DummyXmlStartTag("ResearchTree"),
                new DummyXmlEmptyStartTag("ResearchProject", new String[]{"name", "project"}),
                new DummyXmlEndTag("ResearchProject"),
                new DummyXmlEndTag("ResearchTree"),
                new DummyXmlEndDocument(),
        };
        XmlPullParser xpp = new DummyXmlPullParser(events);
        ResearchProject.loadXmlResearchTree(xpp);
        assertResearchHasProjectCount(1);
        assertResearchHasProjectNamed("project");
        assertNamedProjectHasDependentCount("project", 0);
        assertNamedProjectHasDependerCount("project", 0);
        assertNamedProjectHasTechnologyCount("project", 0);
    }

    @Test
    public void testResearchTreeWithEmptyRequirements() throws ResearchProject.ResearchXmlParserException {
        DummyXmlEvent[] events = {
                new DummyXmlStartTag("ResearchTree"),
                new DummyXmlStartTag("ResearchProject", new String[]{"name", "project"}),
                new DummyXmlEmptyStartTag("Requirements"),
                new DummyXmlEndTag("Requirements"),
                new DummyXmlEndTag("ResearchProject"),
                new DummyXmlEndTag("ResearchTree"),
                new DummyXmlEndDocument(),
        };
        XmlPullParser xpp = new DummyXmlPullParser(events);
        ResearchProject.loadXmlResearchTree(xpp);
        assertResearchHasProjectCount(1);
        assertResearchHasProjectNamed("project");
        assertNamedProjectHasDependentCount("project", 0);
        assertNamedProjectHasDependerCount("project", 0);
        assertNamedProjectHasTechnologyCount("project", 0);
    }

    @Test
    public void testResearchTreeWithEmptyTechnologies() throws ResearchProject.ResearchXmlParserException {
        DummyXmlEvent[] events = {
                new DummyXmlStartTag("ResearchTree"),
                new DummyXmlStartTag("ResearchProject", new String[]{"name", "project"}),
                new DummyXmlEmptyStartTag("Technologies"),
                new DummyXmlEndTag("Technologies"),
                new DummyXmlEndTag("ResearchProject"),
                new DummyXmlEndTag("ResearchTree"),
                new DummyXmlEndDocument(),
        };
        XmlPullParser xpp = new DummyXmlPullParser(events);
        ResearchProject.loadXmlResearchTree(xpp);
        assertResearchHasProjectCount(1);
        assertResearchHasProjectNamed("project");
        assertNamedProjectHasDependentCount("project", 0);
        assertNamedProjectHasDependerCount("project", 0);
        assertNamedProjectHasTechnologyCount("project", 0);
    }

    @Test
    public void testResearchTreeWithRequirement() throws ResearchProject.ResearchXmlParserException {
        DummyXmlEvent[] events = {
                new DummyXmlStartTag("ResearchTree"),
                new DummyXmlEmptyStartTag("ResearchProject", new String[]{"name", "project"}),
                new DummyXmlEndTag("ResearchProject"),
                new DummyXmlStartTag("ResearchProject", new String[]{"name", "project 2"}),
                new DummyXmlStartTag("Requirements"),
                new DummyXmlEmptyStartTag("Requires", new String[]{"name", "project"}),
                new DummyXmlEndTag("Requires"),
                new DummyXmlEndTag("Requirements"),
                new DummyXmlEndTag("ResearchProject"),
                new DummyXmlEndTag("ResearchTree"),
                new DummyXmlEndDocument(),
        };
        XmlPullParser xpp = new DummyXmlPullParser(events);
        ResearchProject.loadXmlResearchTree(xpp);
        assertResearchHasProjectCount(2);
        assertResearchHasProjectNamed("project");
        assertResearchHasProjectNamed("project 2");
        assertNamedProjectHasDependentCount("project", 0);
        assertNamedProjectHasDependerCount("project", 1);
        assertNamedProjectHasDependerNamed("project", "project 2");
        assertNamedProjectHasTechnologyCount("project", 0);
        assertNamedProjectHasDependentCount("project 2", 1);
        assertNamedProjectHasDependentNamed("project 2", "project");
        assertNamedProjectHasDependerCount("project 2", 0);
        assertNamedProjectHasTechnologyCount("project", 0);
    }

    @Test
    public void testResearchTreeWithTechnology() throws ResearchProject.ResearchXmlParserException {
        DummyXmlEvent[] events = {
                new DummyXmlStartTag("ResearchTree"),
                new DummyXmlStartTag("ResearchProject", new String[]{"name", "project"}),
                new DummyXmlStartTag("Technologies"),
                new DummyXmlEmptyStartTag("Technology", new String[]{"name", "tech"}),
                new DummyXmlEndTag("Technology"),
                new DummyXmlEndTag("Technologies"),
                new DummyXmlEndTag("ResearchProject"),
                new DummyXmlEndTag("ResearchTree"),
                new DummyXmlEndDocument(),
        };
        XmlPullParser xpp = new DummyXmlPullParser(events);
        ResearchProject.loadXmlResearchTree(xpp);
        assertResearchHasProjectCount(1);
        assertResearchHasProjectNamed("project");
        assertNamedProjectHasDependentCount("project", 0);
        assertNamedProjectHasDependerCount("project", 0);
        assertNamedProjectHasTechnologyCount("project", 1);
        assertNamedProjectHasTechnologyNamed("project", "tech");
    }

    @Test
    public void testResearchTreeWithComplexRequirements() throws ResearchProject.ResearchXmlParserException {
        DummyXmlEvent[] events = {
                new DummyXmlStartTag("ResearchTree"),
                new DummyXmlEmptyStartTag("ResearchProject", new String[]{"name", "p1"}),
                new DummyXmlEndTag("ResearchProject"),
                new DummyXmlStartTag("ResearchProject", new String[]{"name", "p2"}),
                new DummyXmlStartTag("Requirements"),
                new DummyXmlEmptyStartTag("Requires", new String[]{"name", "p1"}),
                new DummyXmlEndTag("Requires"),
                new DummyXmlEndTag("Requirements"),
                new DummyXmlStartTag("Technologies"),
                new DummyXmlEmptyStartTag("Technology", new String[]{"name", "tech2"}),
                new DummyXmlEndTag("Technology"),
                new DummyXmlEndTag("Technologies"),
                new DummyXmlEndTag("ResearchProject"),
                new DummyXmlStartTag("ResearchProject", new String[]{"name", "p3"}),
                new DummyXmlStartTag("Requirements"),
                new DummyXmlEmptyStartTag("Requires", new String[]{"name", "p1"}),
                new DummyXmlEndTag("Requires"),
                new DummyXmlEndTag("Requirements"),
                new DummyXmlStartTag("Technologies"),
                new DummyXmlEmptyStartTag("Technology", new String[]{"name", "tech3"}),
                new DummyXmlEndTag("Technology"),
                new DummyXmlEndTag("Technologies"),
                new DummyXmlEndTag("ResearchProject"),
                new DummyXmlStartTag("ResearchProject", new String[]{"name", "p4"}),
                new DummyXmlStartTag("Requirements"),
                new DummyXmlEmptyStartTag("Requires", new String[]{"name", "p2"}),
                new DummyXmlEndTag("Requires"),
                new DummyXmlEmptyStartTag("Requires", new String[]{"name", "p3"}),
                new DummyXmlEndTag("Requires"),
                new DummyXmlEndTag("Requirements"),
                new DummyXmlStartTag("Technologies"),
                new DummyXmlEmptyStartTag("Technology", new String[]{"name", "tech4"}),
                new DummyXmlEndTag("Technology"),
                new DummyXmlEndTag("Technologies"),
                new DummyXmlEndTag("ResearchProject"),
                new DummyXmlEndTag("ResearchTree"),
                new DummyXmlEndDocument(),
        };
        XmlPullParser xpp = new DummyXmlPullParser(events);
        ResearchProject.loadXmlResearchTree(xpp);
        assertResearchHasProjectCount(4);
        assertResearchHasProjectNamed("p1");
        assertNamedProjectHasDependentCount("p1", 0);
        assertNamedProjectHasDependerCount("p1", 2);
        assertNamedProjectHasDependerNamed("p1", "p2");
        assertNamedProjectHasDependerNamed("p1", "p3");
        assertNamedProjectHasTechnologyCount("p1", 0);
        assertResearchHasProjectNamed("p2");
        assertNamedProjectHasDependentCount("p2", 1);
        assertNamedProjectHasDependentNamed("p2", "p1");
        assertNamedProjectHasDependerCount("p2", 1);
        assertNamedProjectHasDependerNamed("p2", "p4");
        assertNamedProjectHasTechnologyCount("p2", 1);
        assertNamedProjectHasTechnologyNamed("p2", "tech2");
        assertResearchHasProjectNamed("p3");
        assertNamedProjectHasDependentCount("p3", 1);
        assertNamedProjectHasDependentNamed("p3", "p1");
        assertNamedProjectHasDependerCount("p3", 1);
        assertNamedProjectHasDependerNamed("p3", "p4");
        assertNamedProjectHasTechnologyCount("p3", 1);
        assertNamedProjectHasTechnologyNamed("p3", "tech3");
        assertResearchHasProjectNamed("p4");
        assertNamedProjectHasDependentCount("p4", 2);
        assertNamedProjectHasDependentNamed("p4", "p2");
        assertNamedProjectHasDependentNamed("p4", "p3");
        assertNamedProjectHasDependerCount("p4", 0);
        assertNamedProjectHasTechnologyCount("p4", 1);
        assertNamedProjectHasTechnologyNamed("p4", "tech4");

    }

    @Test
    public void testResearchTreeWithRequirementBeforeProject() throws ResearchProject.ResearchXmlParserException {
        DummyXmlEvent[] events = {
                new DummyXmlStartTag("ResearchTree"),
                new DummyXmlEmptyStartTag("ResearchProject", new String[]{"name", "project"}),
                new DummyXmlStartTag("Requirements"),
                new DummyXmlEmptyStartTag("Requires", new String[]{"name", "project 2"}),
                new DummyXmlEndTag("Requires"),
                new DummyXmlEndTag("Requirements"),
                new DummyXmlEndTag("ResearchProject"),
                new DummyXmlStartTag("ResearchProject", new String[]{"name", "project 2"}),
                new DummyXmlEndTag("ResearchProject"),
                new DummyXmlEndTag("ResearchTree"),
                new DummyXmlEndDocument(),
        };
        XmlPullParser xpp = new DummyXmlPullParser(events);
        ResearchProject.loadXmlResearchTree(xpp);
        assertResearchHasProjectCount(2);
        assertResearchHasProjectNamed("project");
        assertResearchHasProjectNamed("project 2");
        assertNamedProjectHasDependentCount("project", 1);
        assertNamedProjectHasDependentNamed("project", "project 2");
        assertNamedProjectHasDependerCount("project", 0);
        assertNamedProjectHasTechnologyCount("project", 0);
        assertNamedProjectHasDependentCount("project 2", 0);
        assertNamedProjectHasDependerCount("project 2", 1);
        assertNamedProjectHasDependerNamed("project 2", "project");
        assertNamedProjectHasTechnologyCount("project 2", 0);
    }

    @Test(expected = ResearchProject.ResearchXmlParserException.class)
    public void testResearchTreeNoDocument() throws ResearchProject.ResearchXmlParserException {
        DummyXmlEvent[] events = {
        };
        XmlPullParser xpp = new DummyXmlPullParser(events);
        ResearchProject.loadXmlResearchTree(xpp);
    }

    @Test(expected = ResearchProject.ResearchXmlParserException.class)
    public void testResearchTreeNoEndDocument() throws ResearchProject.ResearchXmlParserException {
        DummyXmlEvent[] events = {
                new DummyXmlStartTag("ResearchTree"),
                new DummyXmlEndTag("ResearchTree"),
        };
        XmlPullParser xpp = new DummyXmlPullParser(events);
        ResearchProject.loadXmlResearchTree(xpp);
    }

    @Test(expected = ResearchProject.ResearchXmlParserException.class)
    public void testResearchTreeInvalidContent() throws ResearchProject.ResearchXmlParserException {
        DummyXmlEvent[] events = {
                new DummyXmlStartTag("ResearchTree"),
                new DummyXmlInvalidContent("Invalid Content"),
                new DummyXmlEndTag("ResearchTree"),
                new DummyXmlEndDocument(),
        };
        XmlPullParser xpp = new DummyXmlPullParser(events);
        ResearchProject.loadXmlResearchTree(xpp);
    }

    @Test(expected = ResearchProject.ResearchXmlParserException.class)
    public void testResearchTreeNoEndResearchTree() throws ResearchProject.ResearchXmlParserException {
        DummyXmlEvent[] events = {
                new DummyXmlStartTag("ResearchTree"),
                new DummyXmlEndDocument(),
        };
        XmlPullParser xpp = new DummyXmlPullParser(events);
        ResearchProject.loadXmlResearchTree(xpp);
    }

    @Test(expected = ResearchProject.ResearchXmlParserException.class)
    public void testResearchTreeExtraEndResearchTree() throws ResearchProject.ResearchXmlParserException {
        DummyXmlEvent[] events = {
                new DummyXmlStartTag("ResearchTree"),
                new DummyXmlEndTag("ResearchTree"),
                new DummyXmlEndTag("ResearchTree"),
                new DummyXmlEndDocument(),
        };
        XmlPullParser xpp = new DummyXmlPullParser(events);
        ResearchProject.loadXmlResearchTree(xpp);
    }

    @Test(expected = ResearchProject.ResearchXmlParserException.class)
    public void testResearchTreeNoEndResearchProject() throws ResearchProject.ResearchXmlParserException {
        DummyXmlEvent[] events = {
                new DummyXmlStartTag("ResearchTree"),
                new DummyXmlStartTag("ResearchProject", new String[]{"name", "project"}),
                new DummyXmlEndTag("ResearchTree"),
                new DummyXmlEndDocument(),
        };
        XmlPullParser xpp = new DummyXmlPullParser(events);
        ResearchProject.loadXmlResearchTree(xpp);
    }

    @Test(expected = ResearchProject.ResearchXmlParserException.class)
    public void testResearchTreeExtraEndResearchProject() throws ResearchProject.ResearchXmlParserException {
        DummyXmlEvent[] events = {
                new DummyXmlStartTag("ResearchTree"),
                new DummyXmlStartTag("ResearchProject", new String[]{"name", "project"}),
                new DummyXmlEndTag("ResearchProject"),
                new DummyXmlEndTag("ResearchProject"),
                new DummyXmlEndTag("ResearchTree"),
                new DummyXmlEndDocument(),
        };
        XmlPullParser xpp = new DummyXmlPullParser(events);
        ResearchProject.loadXmlResearchTree(xpp);
    }

    @Test(expected = ResearchProject.ResearchXmlParserException.class)
    public void testResearchTreeNoEndRequirements() throws ResearchProject.ResearchXmlParserException {
        DummyXmlEvent[] events = {
                new DummyXmlStartTag("ResearchTree"),
                new DummyXmlStartTag("ResearchProject", new String[]{"name", "project"}),
                new DummyXmlStartTag("Requirements"),
                new DummyXmlEndTag("ResearchProject"),
                new DummyXmlEndTag("ResearchTree"),
                new DummyXmlEndDocument(),
        };
        XmlPullParser xpp = new DummyXmlPullParser(events);
        ResearchProject.loadXmlResearchTree(xpp);
    }

    @Test(expected = ResearchProject.ResearchXmlParserException.class)
    public void testResearchTreeExtraEndRequirements() throws ResearchProject.ResearchXmlParserException {
        DummyXmlEvent[] events = {
                new DummyXmlStartTag("ResearchTree"),
                new DummyXmlStartTag("ResearchProject", new String[]{"name", "project"}),
                new DummyXmlStartTag("Requirements"),
                new DummyXmlEndTag("Requirements"),
                new DummyXmlEndTag("Requirements"),
                new DummyXmlEndTag("ResearchProject"),
                new DummyXmlEndTag("ResearchTree"),
                new DummyXmlEndDocument(),
        };
        XmlPullParser xpp = new DummyXmlPullParser(events);
        ResearchProject.loadXmlResearchTree(xpp);
    }

    @Test(expected = ResearchProject.ResearchXmlParserException.class)
    public void testResearchTreeNoEndRequires() throws ResearchProject.ResearchXmlParserException {
        DummyXmlEvent[] events = {
                new DummyXmlStartTag("ResearchTree"),
                new DummyXmlStartTag("ResearchProject", new String[]{"name", "project"}),
                new DummyXmlStartTag("Requirements"),
                new DummyXmlStartTag("Requires", new String[]{"name", "project 2"}),
                new DummyXmlEndTag("Requirements"),
                new DummyXmlEndTag("ResearchProject"),
                new DummyXmlEndTag("ResearchTree"),
                new DummyXmlEndDocument(),
        };
        XmlPullParser xpp = new DummyXmlPullParser(events);
        ResearchProject.loadXmlResearchTree(xpp);
    }

    @Test(expected = ResearchProject.ResearchXmlParserException.class)
    public void testResearchTreeExtraEndRequires() throws ResearchProject.ResearchXmlParserException {
        DummyXmlEvent[] events = {
                new DummyXmlStartTag("ResearchTree"),
                new DummyXmlStartTag("ResearchProject", new String[]{"name", "project"}),
                new DummyXmlStartTag("Requirements"),
                new DummyXmlStartTag("Requires", new String[]{"name", "project 2"}),
                new DummyXmlEndTag("Requires"),
                new DummyXmlEndTag("Requires"),
                new DummyXmlEndTag("Requirements"),
                new DummyXmlEndTag("ResearchProject"),
                new DummyXmlEndTag("ResearchTree"),
                new DummyXmlEndDocument(),
        };
        XmlPullParser xpp = new DummyXmlPullParser(events);
        ResearchProject.loadXmlResearchTree(xpp);
    }

    @Test(expected = ResearchProject.ResearchXmlParserException.class)
    public void testResearchTreeNoEndTechnologies() throws ResearchProject.ResearchXmlParserException {
        DummyXmlEvent[] events = {
                new DummyXmlStartTag("ResearchTree"),
                new DummyXmlStartTag("ResearchProject", new String[]{"name", "project"}),
                new DummyXmlStartTag("Technologies"),
                new DummyXmlEndTag("ResearchProject"),
                new DummyXmlEndTag("ResearchTree"),
                new DummyXmlEndDocument(),
        };
        XmlPullParser xpp = new DummyXmlPullParser(events);
        ResearchProject.loadXmlResearchTree(xpp);
    }

    @Test(expected = ResearchProject.ResearchXmlParserException.class)
    public void testResearchTreeExtraEndTechnologies() throws ResearchProject.ResearchXmlParserException {
        DummyXmlEvent[] events = {
                new DummyXmlStartTag("ResearchTree"),
                new DummyXmlStartTag("ResearchProject", new String[]{"name", "project"}),
                new DummyXmlStartTag("Technologies"),
                new DummyXmlEndTag("Technologies"),
                new DummyXmlEndTag("Technologies"),
                new DummyXmlEndTag("ResearchProject"),
                new DummyXmlEndTag("ResearchTree"),
                new DummyXmlEndDocument(),
        };
        XmlPullParser xpp = new DummyXmlPullParser(events);
        ResearchProject.loadXmlResearchTree(xpp);
    }

    @Test(expected = ResearchProject.ResearchXmlParserException.class)
    public void testResearchTreeNoEndTechnology() throws ResearchProject.ResearchXmlParserException {
        DummyXmlEvent[] events = {
                new DummyXmlStartTag("ResearchTree"),
                new DummyXmlStartTag("ResearchProject", new String[]{"name", "project"}),
                new DummyXmlStartTag("Technologies"),
                new DummyXmlStartTag("Technology", new String[]{"name", "tech"}),
                new DummyXmlEndTag("Technologies"),
                new DummyXmlEndTag("ResearchProject"),
                new DummyXmlEndTag("ResearchTree"),
                new DummyXmlEndDocument(),
        };
        XmlPullParser xpp = new DummyXmlPullParser(events);
        ResearchProject.loadXmlResearchTree(xpp);
    }

    @Test(expected = ResearchProject.ResearchXmlParserException.class)
    public void testResearchTreeExtraEndTechnology() throws ResearchProject.ResearchXmlParserException {
        DummyXmlEvent[] events = {
                new DummyXmlStartTag("ResearchTree"),
                new DummyXmlStartTag("ResearchProject", new String[]{"name", "project"}),
                new DummyXmlStartTag("Technologies"),
                new DummyXmlStartTag("Technology", new String[]{"name", "project 2"}),
                new DummyXmlEndTag("Technology"),
                new DummyXmlEndTag("Technology"),
                new DummyXmlEndTag("Technologies"),
                new DummyXmlEndTag("ResearchProject"),
                new DummyXmlEndTag("ResearchTree"),
                new DummyXmlEndDocument(),
        };
        XmlPullParser xpp = new DummyXmlPullParser(events);
        ResearchProject.loadXmlResearchTree(xpp);
    }

    @Test(expected = ResearchProject.ResearchXmlParserException.class)
    public void testResearchTreeLevel0ResearchProject() throws ResearchProject.ResearchXmlParserException {
        DummyXmlEvent[] events = {
                new DummyXmlEmptyStartTag("ResearchProject", new String[]{"name", "project"}),
                new DummyXmlEndTag("ResearchProject"),
                new DummyXmlEndDocument(),
        };
        XmlPullParser xpp = new DummyXmlPullParser(events);
        ResearchProject.loadXmlResearchTree(xpp);
    }

    class ResearchChangeListener implements ResearchProject.OnResearchChangeListener {
        int projectsAdded = 0;
        int projectsRemoved = 0;

        @Override
        public void onResearchProjectAdded(ResearchProject project) {
            projectsAdded += 1;
        }

        @Override
        public void onResearchProjectRemoved(ResearchProject project) {
            projectsRemoved += 1;
        }
    }

    ////////////////////////////////////////////////////////////////////

    class ResearchTechnologyChangeListener implements ResearchProject.OnResearchTechnologyChangeListener {
        int technologiesAdded = 0;
        int technologiesRemoved = 0;

        @Override
        public void onTechnologyAdded(ResearchProject project, String technology) {
            technologiesAdded += 1;
        }

        @Override
        public void onTechnologyRemoved(ResearchProject project, String technology) {
            technologiesRemoved += 1;
        }
    }

    class ResearchDependencyChangeListener implements ResearchProject.OnResearchDependencyChangeListener {
        int dependentsAdded = 0;
        int dependentsRemoved = 0;
        int dependersAdded = 0;
        int dependersRemoved = 0;

        @Override
        public void onDependentAdded(ResearchProject project, ResearchProject dependent) {
            dependentsAdded += 1;
        }

        @Override
        public void onDependentRemoved(ResearchProject project, ResearchProject dependent) {
            dependentsRemoved += 1;
        }

        @Override
        public void onDependerAdded(ResearchProject project, ResearchProject depender) {
            dependersAdded += 1;
        }

        @Override
        public void onDependerRemoved(ResearchProject project, ResearchProject depender) {
            dependersRemoved += 1;
        }
    }

    public class ResearchCompletedChangeListener implements ResearchProject.OnResearchCompletedChangeListener {
        public int countCompletedChange = 0;

        @Override
        public void onResearchCompletedChange(ResearchProject project, boolean completed) {
            this.countCompletedChange += 1;
        }
    }

    class DummyXmlAttribute {
        public final String name;
        public final String value;


        public DummyXmlAttribute(String name, String value) {
            this.name = name;
            this.value = value;
        }

        public String toString() {
            return repr(name) + "=" + repr(value);
        }
    }


    public class DummyXmlEvent {
        public final int event;
        public final boolean isEmpty;
        public final String elementName;
        public final String textContent;
        public final DummyXmlAttribute[] attributes;

        public DummyXmlEvent(int event, boolean isEmpty, String elementName, String textContent, String[] attributes) {
            this.event = event;
            this.isEmpty = isEmpty;
            this.elementName = elementName;
            this.textContent = textContent;
            if (attributes == null) {
                this.attributes = new DummyXmlAttribute[0];
            } else if ((attributes.length % 2) != 0) {
                throw new IllegalArgumentException("attributes must be even-length array");
            } else {
                this.attributes = new DummyXmlAttribute[attributes.length / 2];
                for (int i = 0; i < attributes.length; i += 2) {
                    this.attributes[i / 2] = new DummyXmlAttribute(attributes[i], attributes[i + 1]);
                }
            }
        }

        public String toString() {
            return "DummyXMLEvent(" +
                    "event=" + repr(event) +
                    ", isEmpty=" + repr(isEmpty) +
                    ", elementName=" + repr(elementName) +
                    ", textContent=" + repr(textContent) +
                    ", attributes=" + repr(attributes) +
                    ")";
        }
    }

    @SuppressWarnings("unused")
    public class DummyXmlStartTag extends DummyXmlEvent {
        public DummyXmlStartTag(String elementName, String[] attributes) {
            super(XmlPullParser.START_TAG, false, elementName, null, attributes);
        }

        public DummyXmlStartTag(String elementName) {
            super(XmlPullParser.START_TAG, false, elementName, null, null);
        }
    }

    class DummyXmlEmptyStartTag extends DummyXmlEvent {
        public DummyXmlEmptyStartTag(String elementName, String[] attributes) {
            super(XmlPullParser.START_TAG, true, elementName, null, attributes);
        }

        public DummyXmlEmptyStartTag(String elementName) {
            super(XmlPullParser.START_TAG, true, elementName, null, null);
        }
    }

    class DummyXmlEndTag extends DummyXmlEvent {
        public DummyXmlEndTag(String elementName) {
            super(XmlPullParser.END_TAG, false, elementName, null, null);
        }
    }

    class DummyXmlText extends DummyXmlEvent {
        @SuppressWarnings("SameParameterValue")
        public DummyXmlText(String text) {
            super(XmlPullParser.TEXT, false, null, text, null);
        }
    }


    class DummyXmlStartDocument extends DummyXmlEvent {
        public DummyXmlStartDocument() {
            super(XmlPullParser.START_DOCUMENT, false, null, null, null);
        }
    }

    class DummyXmlEndDocument extends DummyXmlEvent {
        public DummyXmlEndDocument() {
            super(XmlPullParser.END_DOCUMENT, false, null, null, null);
        }
    }

    class DummyXmlInvalidContent extends DummyXmlEvent {
        public DummyXmlInvalidContent(String msg) {
            super(DummyXmlPullParser.INVALID_CONTENT, false, msg, null, null);
        }
    }

    class DummyXmlPullParser implements XmlPullParser {
        public static final int INVALID_CONTENT = -1;

        DummyXmlEvent[] events;
        int index;
        int depth;

        public DummyXmlPullParser(DummyXmlEvent[] events) {
            if (DEBUG)
                debug("> DummyXmlPullParser(" + repr(events) + ")");
            try {
                this.events = events;
                this.setInput(null);
            } catch (XmlPullParserException e) { /* never thrown */ }
            if (DEBUG)
                debug("< DummyXmlPullParser(" + repr(events) + ")");
        }

        @Override
        public String getInputEncoding() {
            return null;
        }

        @Override
        public boolean isAttributeDefault(int index) {
            return false;
        }

        @Override
        public void setFeature(String featureName, boolean state) throws XmlPullParserException {
            throw new XmlPullParserException("Not implemented");
        }

        @Override
        public boolean getFeature(String featureName) {
            return false;
        }

        @Override
        public String getProperty(String propertyName) {
            return null;
        }

        @Override
        public void setProperty(String name,
                                Object value) throws XmlPullParserException, IllegalArgumentException {
            throw new XmlPullParserException("Not implemented");
        }

        @Override
        public void setInput(Reader in) throws XmlPullParserException {
            this.index = -1;
            this.depth = 0;
        }

        @Override
        public void setInput(InputStream inputStream, String inputEncoding) throws XmlPullParserException {
            throw new XmlPullParserException("Not implemented");
        }

        @Override
        public void defineEntityReplacementText(String entityName, String replacementText) throws XmlPullParserException {
            throw new XmlPullParserException("Not implemented");
        }

        @Override
        public int getNamespaceCount(int depth) throws XmlPullParserException {
            return 0;
        }

        @Override
        public String getNamespacePrefix(int pos) throws XmlPullParserException {
            throw new XmlPullParserException("Not implemented");
        }

        @Override
        public String getNamespaceUri(int pos) throws XmlPullParserException {
            throw new XmlPullParserException("Not implemented");
        }

        @Override
        public String getNamespace(String prefix) {
            return null;
        }

        @Override
        public int getDepth() {
            return this.depth;
        }

        @Override
        public String getPositionDescription() {
            return "Index " + this.index + " event " + this.getEventType();
        }

        @Override
        public int getLineNumber() {
            return this.index + 1;
        }

        @Override
        public int getColumnNumber() {
            return 0;
        }

        @Override
        public boolean isWhitespace() throws XmlPullParserException {
            String txt = this.getText();
            if (txt == null) {
                throw new XmlPullParserException("Invalid element");
            }
            int i = txt.length() - 1;
            while ((i >= 0) && (Character.isWhitespace(txt.charAt(i)))) {
                i -= 1;
            }
            return (i < 0);
        }

        @Override
        public String getText() {
            return this.events[this.index].textContent;
        }

        @Override
        public char[] getTextCharacters(int[] startLengthReturn) {
            String txt = this.getText();
            if (txt != null) {
                char[] buf = txt.toCharArray();
                startLengthReturn[0] = 0;
                startLengthReturn[1] = buf.length;
                return buf;
            } else {
                startLengthReturn[0] = startLengthReturn[1] = -1;
                return null;
            }
        }

        @Override
        public String getNamespace() {
            if (this.getName() == null) {
                return null;
            }
            return "";
        }

        @Override
        public String getName() {
            if (DEBUG) debug("> getName()");
            if (DEBUG)
                debug("< getName() returning " + repr(this.events[this.index].elementName));
            return this.events[this.index].elementName;
        }

        @Override
        public String getPrefix() {
            return null;
        }

        @Override
        public boolean isEmptyElementTag() throws XmlPullParserException {
            if (this.getEventType() != XmlPullParser.START_TAG) {
                throw new XmlPullParserException("Invalid element");
            }
            return this.events[this.index].isEmpty;
        }

        @Override
        public int getAttributeCount() {
            if (this.getEventType() == XmlPullParser.START_TAG) {
                return this.events[this.index].attributes.length;
            }
            return -1;
        }

        @Override
        public String getAttributeNamespace(int index) {
            if (index < 0 || index >= this.getAttributeCount()) {
                throw new IndexOutOfBoundsException("Invalid attribute index");
            }
            return "";
        }

        @Override
        public String getAttributeName(int index) {
            if (index < 0 || index >= this.getAttributeCount()) {
                throw new IndexOutOfBoundsException("Invalid attribute index");
            }
            return this.events[this.index].attributes[index].name;
        }

        @Override
        public String getAttributeType(int index) {
            return "CDATA";
        }

        @Override
        public String getAttributePrefix(int index) {
            if (index < 0 || index >= this.getAttributeCount()) {
                throw new IndexOutOfBoundsException("Invalid attribute index");
            }
            return null;
        }

        @Override
        public String getAttributeValue(int index) {
            if (index < 0 || index >= this.getAttributeCount()) {
                throw new IndexOutOfBoundsException("Invalid attribute index");
            }
            return this.events[this.index].attributes[index].value;
        }

        @Override
        public String getAttributeValue(String namespace, String name) {
            if (this.getEventType() != XmlPullParser.START_TAG) {
                throw new IndexOutOfBoundsException("Invalid element");
            }
            if (namespace == null) {
                int i = this.getAttributeCount() - 1;
                while ((i >= 0) && (!this.getAttributeName(i).equals(name))) {
                    i -= 1;
                }
                if (i >= 0) {
                    return this.getAttributeValue(i);
                }
            }
            return null;
        }

        @Override
        public int getEventType() {
            return this.events[this.index].event;
        }

        @Override
        public int next() throws IOException, XmlPullParserException {
            if (this.index > 0 && this.events[this.index - 1].event == XmlPullParser.END_TAG) {
                this.depth -= 1;
            }
            this.index += 1;
            if (this.index >= this.events.length) {
                throw new XmlPullParserException("No more data");
            }
            if (this.events[this.index].event == INVALID_CONTENT) {
                throw new XmlPullParserException(this.events[this.index].elementName);
            }
            if (this.events[this.index].event == XmlPullParser.START_TAG) {
                this.depth += 1;
            }
            return this.events[this.index].event;
        }

        @Override
        public int nextTag() throws IOException, XmlPullParserException {
            int eventType = next();
            if (eventType == TEXT && isWhitespace()) {   // skip whitespace
                eventType = next();
            }
            if (eventType != START_TAG && eventType != END_TAG) {
                throw new XmlPullParserException("expected start or end tag", this, null);
            }
            return eventType;
        }

        @Override
        public String nextText() throws IOException, XmlPullParserException {
            if (getEventType() != START_TAG) {
                throw new XmlPullParserException(
                        "parser must be on START_TAG to read next text", this, null);
            }
            int eventType = next();
            if (eventType == TEXT) {
                String result = getText();
                eventType = next();
                if (eventType != END_TAG) {
                    throw new XmlPullParserException(
                            "event TEXT it must be immediately followed by END_TAG", this, null);
                }
                return result;
            } else if (eventType == END_TAG) {
                return "";
            } else {
                throw new XmlPullParserException(
                        "parser must be on START_TAG or TEXT to read text", this, null);
            }
        }

        @Override
        public int nextToken() throws IOException, XmlPullParserException {
            return this.next();
        }

        @Override
        public void require(int type, String namespace, String name) throws XmlPullParserException {
            throw new XmlPullParserException("Not implemented");
        }

    }

}
