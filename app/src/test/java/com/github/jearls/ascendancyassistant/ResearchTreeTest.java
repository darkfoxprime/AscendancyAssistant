package com.github.jearls.ascendancyassistant;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

import org.xmlpull.v1.*;
import java.io.IOException;
import java.io.FileReader;
import java.util.Iterator;
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public class ResearchTreeTest {

////////////////////////////////////////////////////////////////////////

    class DummyXMLAttribute {
        public String name;
        public String value;
        public DummyXMLAttribute(String name, String value) {
            this.name = name;
            this.value = value;
        }
    }

    class DummyXmlEvent {
        public int event;
        public boolean isEmpty;
        public String elementName;
        public String textContent;
        public DummyXMLAttribute[] attributes;

        public DummyXmlEvent(int event, boolean isEmpty, String elementName, String textContent, DummyXMLAttribute[] attributes) {
            this.event = event;
            this.isEmpty = isEmpty;
            this.elementName = elementName;
            this.textContent = textContent;
            this.attributes = attributes;
        }
    }

    class DummyXmlStartTag extends DummyXmlEvent {
        public DummyXmlStartTag(String elementName, DummyXMLAttributes[] attributes) {
            super(XmlPullProcess.START_TAG, false, elementName, null, attributes);
        }
    }

    class DummyXmlStartEndTag extends DummyXmlEvent {
        public DummyXmlStartEndTag(String elementName, DummyXMLAttributes[] attributes) {
            super(XmlPullProcess.START_TAG, true, elementName, null, attributes);
        }
    }

    class DummyXmlEndTag extends DummyXmlEvent {
        public DummyXmlEndTag() {
            super(XmlPullProcess.END_TAG, false, null, null, {});
        }
    }

    class DummyXmlText extends DummyXmlEvent {
        public DummyXmlText(String text) {
            super(XmlPullProcess.TEXT, false, null, text, {});
        }
    }

    class DummyXmlEndDocument extends DummyXmlEvent {
        public DummyXmlEndDocument() {
            super(XmlPullProcess.END_DOCUMENT, false, null, null, {});
        }
    }

    class DummyXmlPullParser implements XmlPullParser {

        DummyXMLEvents[] events;
        int index;
        int depth;

        public DummyXmlPullParser(DummyXMLEvents[] events) {
            self.setInput(null);
        }

        public void setFeature(String featureName, boolean state) throws XmlPullParserException {
            throw new XmlPullParserException("Not implemented");
        }

        public boolean getFeature(String featureName) {
            return false;
        }

        public void setProperty(String propertyName, boolean state) throws XmlPullParserException {
            throw new XmlPullParserException("Not implemented");
        }

        public boolean getProperty(String propertyName) {
            return false;
        }

        public void setInput(Reader in) throws XmlPullParserException {
            self.index = -1;
            self.depth = 0;
        }

        public void defineEntityReplacementText(String entityName, String replacementText) {
            throw new XmlPullParserException("Not implemented");
        }

        public int getNamespaceCount(int depth) throws XmlPullParserException {
            return 0;
        }

        public String getNamespacePrefix(int pos) throws XmlPullParserException {
            throw new XmlPullParserException("Not implemented");
        }

        public String getNamespaceUri(int pos) throws XmlPullParserException {
            throw new XmlPullParserException("Not implemented");
        }

        public String getNamespace(String prefix) throws XmlPullParserException {
            throw new XmlPullParserException("Not implemented");
        }

        public int getDepth() {
            return self.depth;
        }

        public String getPositionDescription() {
            return "Index " + self.index + " event " + self.getEventType();
        }

        public int getLineNumber() {
            return self.index + 1;
        }

        public int getColumnNumber() {
            return 0;
        }

        public boolean isWhitespace() throws XmlPullParserException {
            String txt = self.getText()
            if (txt == null) {
                throw new XmlPullParserException("Invalid element");
            }
            i = txt.length() - 1;
            while ((i >= 0) && (txt.charAt(i).isWhiteSpace())) { i -= 1; }
            return (i < 0);
        }

        public String getText() {
            return self.events[self.index].textContent;
        }

        public char[] getTextCharacters(int[] startLengthReturn) {
            String txt = self.getText();
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

        public String getNamespace() {
            if (self.getName() == null) {
                return null;
            }
            return "";
        }

        public String getName() {
            return self.events[self.index].elementName;
        }

        public String getPrefix() {
            return null;
        }

        public boolean isEmptyElementTag() throws XmlPullParserException {
            if (self.getEventType() != XmlPullParser.START_TAG) {
                throw new XmlPullParserException("Invalid element");
            }
            return self.events[self.index].isEmpty;
        }

        public int getAttributeCount() {
            if (self.getEventType() == XmlPullParser.START_TAG) {
                return self.events[self.index].attributes.length;
            }
            return -1;
        }

        public String getAttributeNamespace(int index) {
            if (index < 0 || index >= self.getAttributeCount()) {
                throw new IndexOutOfBoundsException("Invalid attribute index");
            }
            return "";
        }

        public String getAttributeName(int index) {
            if (index < 0 || index >= self.getAttributeCount()) {
                throw new IndexOutOfBoundsException("Invalid attribute index");
            }
            return self.events[self.index].attributes[index].name;
        }

        public String getAttributePrefix(int index) {
            if (index < 0 || index >= self.getAttributeCount()) {
                throw new IndexOutOfBoundsException("Invalid attribute index");
            }
            return null;
        }

        public String getAttributeValue(int index) {
            if (index < 0 || index >= self.getAttributeCount()) {
                throw new IndexOutOfBoundsException("Invalid attribute index");
            }
            return self.events[self.index].attributes[index].value;
        }

        public String getAttributeValue(String namespace, String name) {
            if (self.getEventType() != XmlPullParser.START_TAG) {
                throw new XmlPullParserException("Invalid element");
            }
            if (namespace == null) {
                int i = self.getAttributeCount() - 1;
                while ((i >= 0) && (self.getAttributeName(i) != name)) {
                    i -= 1;
                }
                if (i >= 0) {
                    return self.getAttributeValue(i);
                }
            }
            return null;
        }

        public int getEventType() {
            return self.events[self.index].event;
        }

        public int next() {
            if (self.index > 0 && self.events[self.index-1].event == XmlPullParser.END_TAG) {
                self.depth -= 1;
            }
            self.index += 1;
            if (self.index >= self.events.length) {
                throw new XmlPullParserException("No more data");
            }
            if (self.events[self.index].event == XmlPullParser.START_TAG) {
                self.depth += 1;
            }
            return self.events[self.index].event;
        }

        public int nextToken() {
            return self.next();
        }

        public void require(int type, String namespace, String name) throws XmlPullParserException {
            throw new XmlPullParserException("Not implemented");
        }

        public void readText() throws XmlPullParserException {
            throw new XmlPullParserException("Not implemented");
        }

    }

////////////////////////////////////////////////////////////////////////

    public void assertTreeHasProjectCount(ResearchTree tree, int count) {
        Iterator<ResearchProject> pi = tree.getResearchProjects();
        int found = 0;
        while (pi.hasNext()) {
            found += 1;
            pi.next();
        }
        assertEquals("Expected " + count + " research projects, found " + found, count, found);
    }
    public void assertTreeHasProjectNamed(ResearchTree tree, String project) {
        assertNotNull("Expected to find research project " + project, tree.getResearchProject(project));
    }
    public void assertNamedProjectHasDependencyCount(ResearchTree tree, String project, int count) {
        assertTreeHasProjectNamed(tree, project);
        Iterator<ResearchProject> pi = tree.getResearchProject(project).getDependencies();
        int found = 0;
        while (pi.hasNext()) {
            found += 1;
            pi.next();
        }
        assertEquals("Expected " + count + " dependencies, found " + found, count, found);
    }
    public void assertNamedProjectHasDependencyNamed(ResearchTree tree, String project, String dep) {
        assertTreeHasProjectNamed(tree, project);
        assertTreeHasProjectNamed(tree, dep);
        assertNotNull("Expected to find dependency " + dep, tree.getResearchProject(project).hasDependency(tree.getResearchProject(dep)));
    }
    public void assertNamedProjectHasTechnologyCount(ResearchTree tree, String project, int count) {
        assertTreeHasProjectNamed(tree, project);
        Iterator<String> pi = tree.getResearchProject(project).getTechnologie();
        int found = 0;
        while (pi.hasNext()) {
            found += 1;
            pi.next();
        }
        assertEquals("Expected " + count + " dependencies, found " + found, count, found);
    }
    public void assertNamedProjectHasTechnologyNamed(ResearchTree tree, String project, String tech) {
        assertTreeHasProjectNamed(tree, project);
        assertNotNull("Expected to find technology " + tech, tree.getResearchProject(project).hasTechnology(tech));
    }

    @Test
    public void testEmptyResearchTree() {
        DummyXMLEvents[] events = {
            new DummyXMLStartEndTag("ResearchTree", {}) ,
            new DummyXMLEndDocument() ,
        }
        XmlPullParser xpp = new DummyXmlPullParser(events);
        ResearchTree tree = ResearchTree.loadXmlResearchTree(xpp);
        assertTreeHasProjectCount(tree, 0);
    }

    @Test
    public void testTextContent() {
        DummyXMLEvents[] events = {
            new DummyXMLStartTag("ResearchTree", {}) ,
            new DummyXMLText("This is some text") ,
            new DummyXMLEndTag() ,
            new DummyXMLEndDocument() ,
        }
        XmlPullParser xpp = new DummyXmlPullParser(events);
        ResearchTree tree = ResearchTree.loadXmlResearchTree(xpp);
        assertTreeHasProjectCount(tree, 0);
    }

    @Test
    public void testSingleResearchProject() {
        DummyXMLEvents[] events = {
            new DummyXMLStartTag("ResearchTree", {}) ,
            new DummyXMLStartEndTag("ResearchProject", {new DummyXMLAttribute("name", "project")}) ,
            new DummyXMLEndTag() ,
            new DummyXMLEndDocument() ,
        }
        XmlPullParser xpp = new DummyXmlPullParser(events);
        ResearchTree tree = ResearchTree.loadXmlResearchTree(xpp);
        assertTreeHasProjectCount(tree, 1);
        assertTreeHasProjectNamed(tree, "project");
        assertNamedProjectHasDependencyCount(tree, "project", 0);
        assertNamedProjectHasTechnologyCount(tree, "project", 0);
    }

    @Test
    public void testResearchProjectTechnology() {
        DummyXMLEvents[] events = {
            new DummyXMLStartTag("ResearchTree", {}) ,
            new DummyXMLStartTag("ResearchProject", {new DummyXMLAttribute("name", "project")}) ,
            new DummyXMLStartTag("Technologies", {}) ,
            new DummyXMLStartEndTag("Technology", {"name", "tech"}) ,
            new DummyXMLEndTag() ,
            new DummyXMLEndTag() ,
            new DummyXMLEndTag() ,
            new DummyXMLEndDocument() ,
        }
        XmlPullParser xpp = new DummyXmlPullParser(events);
        ResearchTree tree = ResearchTree.loadXmlResearchTree(xpp);
        assertTreeHasProjectCount(tree, 1);
        assertTreeHasProjectNamed(tree, "project");
        assertNamedProjectHasDependencyCount(tree, "project", 0);
        assertNamedProjectHasTechnologyCount(tree, "project", 1);
        assertNamedProjectHasTechnologyNamed(tree, "project", "tech");
    }

    @Test
    public void testResearchProjectDependency() {
        DummyXMLEvents[] events = {
            new DummyXMLStartTag("ResearchTree", {}) ,
            new DummyXMLStartEndTag("ResearchProject", {new DummyXMLAttribute("name", "project")}) ,
            new DummyXMLStartTag("ResearchProject", {new DummyXMLAttribute("name", "project 2")}) ,
            new DummyXMLStartTag("Requirements", {}) ,
            new DummyXMLStartEndTag("Requires", {"name", "project"}) ,
            new DummyXMLEndTag() ,
            new DummyXMLEndTag() ,
            new DummyXMLEndTag() ,
            new DummyXMLEndDocument() ,
        }
        XmlPullParser xpp = new DummyXmlPullParser(events);
        ResearchTree tree = ResearchTree.loadXmlResearchTree(xpp);
        assertTreeHasProjectCount(tree, 2);
        assertTreeHasProjectNamed(tree, "project");
        assertTreeHasProjectNamed(tree, "project 2");
        assertNamedProjectHasDependencyCount(tree, "project", 0);
        assertNamedProjectHasTechnologyCount(tree, "project", 0);
        assertNamedProjectHasDependencyCount(tree, "project 2", 1);
        assertNamedProjectHasDependencyNamed(tree, "project 2", "project');
        assertNamedProjectHasTechnologyCount(tree, "project", 0);
    }

    @Test
    public void testComplexResearchProjectDependencies() {
        DummyXMLEvents[] events = {
            new DummyXMLStartTag("ResearchTree", {}) ,
            new DummyXMLStartEndTag("ResearchProject", {new DummyXMLAttribute("name", "p1")}) ,
            new DummyXMLStartTag("ResearchProject", {new DummyXMLAttribute("name", "p2")}) ,
            new DummyXMLStartTag("Requirements", {}) ,
            new DummyXMLStartEndTag("Requires", {"name", "p1"}) ,
            new DummyXMLEndTag() ,
            new DummyXMLStartTag("Technologies", {}) ,
            new DummyXMLStartEndTag("Technology", {"name", "tech2"}) ,
            new DummyXMLEndTag() ,
            new DummyXMLEndTag() ,
            new DummyXMLStartTag("ResearchProject", {new DummyXMLAttribute("name", "p3")}) ,
            new DummyXMLStartTag("Requirements", {}) ,
            new DummyXMLStartEndTag("Requires", {"name", "p1"}) ,
            new DummyXMLEndTag() ,
            new DummyXMLStartTag("Technologies", {}) ,
            new DummyXMLStartEndTag("Technology", {"name", "tech3"}) ,
            new DummyXMLEndTag() ,
            new DummyXMLEndTag() ,
            new DummyXMLStartTag("ResearchProject", {new DummyXMLAttribute("name", "p4")}) ,
            new DummyXMLStartTag("Requirements", {}) ,
            new DummyXMLStartEndTag("Requires", {"name", "p2"}) ,
            new DummyXMLStartEndTag("Requires", {"name", "p3"}) ,
            new DummyXMLEndTag() ,
            new DummyXMLStartTag("Technologies", {}) ,
            new DummyXMLStartEndTag("Technology", {"name", "tech4"}) ,
            new DummyXMLEndTag() ,
            new DummyXMLEndTag() ,
            new DummyXMLEndTag() ,
            new DummyXMLEndDocument() ,
        }
        XmlPullParser xpp = new DummyXmlPullParser(events);
        ResearchTree tree = ResearchTree.loadXmlResearchTree(xpp);
        assertTreeHasProjectCount(tree, 4);
        assertTreeHasProjectNamed(tree, "p1");
        assertNamedProjectHasDependencyCount(tree, "p1", 0);
        assertNamedProjectHasTechnologyCount(tree, "p1", 0);
        assertTreeHasProjectNamed(tree, "p2");
        assertNamedProjectHasDependencyCount(tree, "p2", 1);
        assertNamedProjectHasDependencyNamed(tree, "p2", "p1");
        assertNamedProjectHasTechnologyCount(tree, "p2", 1);
        assertNamedProjectHasTechnologyNamed(tree, "p2", "tech2");
        assertTreeHasProjectNamed(tree, "p3");
        assertNamedProjectHasDependencyCount(tree, "p3", 1);
        assertNamedProjectHasDependencyNamed(tree, "p3", "p1");
        assertNamedProjectHasTechnologyCount(tree, "p3", 1);
        assertNamedProjectHasTechnologyNamed(tree, "p3", "tech3");
        assertTreeHasProjectNamed(tree, "p4");
        assertNamedProjectHasDependencyCount(tree, "p4", 2);
        assertNamedProjectHasDependencyNamed(tree, "p4", "p2");
        assertNamedProjectHasDependencyNamed(tree, "p4", "p3");
        assertNamedProjectHasTechnologyCount(tree, "p4", 1);
        assertNamedProjectHasTechnologyNamed(tree, "p4", "tech4");
    }

}
