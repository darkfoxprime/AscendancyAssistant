package com.github.jearls.ascendancyassistant;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.junit.Test;

import org.xmlpull.v1.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Iterator;

@SuppressWarnings("WeakerAccess")
public class ResearchTreeTest {

////////////////////////////////////////////////////////////////////////

    public void assertTreeHasProjectCount(ResearchTree tree, int count) {
        Iterator<ResearchProject> pi = tree.getResearchProjects().iterator();
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
        Iterator<String> pi = tree.getResearchProject(project).getTechnologies();
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
                new DummyXMLStartEndTag("ResearchTree", new DummyXMLAttribute[]{}),
                new DummyXMLEndDocument(),
        };
        XmlPullParser xpp = new DummyXmlPullParser(events);
        try {
            ResearchTree tree = ResearchTree.loadXmlResearchTree(xpp);
            assertTreeHasProjectCount(tree, 0);
        } catch (IOException e) {
            fail("Expected to not throw IOException");
        }
    }

    @Test
    public void testTextContent() {
        DummyXMLEvents[] events = {
                new DummyXMLStartTag("ResearchTree", new DummyXMLAttribute[]{}),
                new DummyXMLText("This is some text"),
                new DummyXMLEndTag(),
                new DummyXMLEndDocument(),
        };
        try {
            XmlPullParser xpp = new DummyXmlPullParser(events);
            ResearchTree tree = ResearchTree.loadXmlResearchTree(xpp);
            assertTreeHasProjectCount(tree, 0);
        } catch (IOException e) {
            fail("Expected to not throw IOException");
        }

    }

////////////////////////////////////////////////////////////////////////

    @Test
    public void testSingleResearchProject() {
        DummyXMLEvents[] events = {
                new DummyXMLStartTag("ResearchTree", new DummyXMLAttribute[]{}),
                new DummyXMLStartEndTag("ResearchProject", new DummyXMLAttribute[]{new DummyXMLAttribute("name", "project")}),
                new DummyXMLEndTag(),
                new DummyXMLEndDocument(),
        };
        try {
            XmlPullParser xpp = new DummyXmlPullParser(events);
            ResearchTree tree = ResearchTree.loadXmlResearchTree(xpp);
            assertTreeHasProjectCount(tree, 1);
            assertTreeHasProjectNamed(tree, "project");
            assertNamedProjectHasDependencyCount(tree, "project", 0);
            assertNamedProjectHasTechnologyCount(tree, "project", 0);
        } catch (IOException e) {
            fail("Expected to not throw IOException");
        }
    }

    @Test
    public void testResearchProjectTechnology() {
        DummyXMLEvents[] events = {
                new DummyXMLStartTag("ResearchTree", new DummyXMLAttribute[]{}),
                new DummyXMLStartTag("ResearchProject", new DummyXMLAttribute[]{new DummyXMLAttribute("name", "project")}),
                new DummyXMLStartTag("Technologies", new DummyXMLAttribute[]{}),
                new DummyXMLStartEndTag("Technology", new DummyXMLAttribute[]{new DummyXMLAttribute("name", "tech")}),
                new DummyXMLEndTag(),
                new DummyXMLEndTag(),
                new DummyXMLEndTag(),
                new DummyXMLEndDocument(),
        };
        try {
            XmlPullParser xpp = new DummyXmlPullParser(events);
            ResearchTree tree = ResearchTree.loadXmlResearchTree(xpp);
            assertTreeHasProjectCount(tree, 1);
            assertTreeHasProjectNamed(tree, "project");
            assertNamedProjectHasDependencyCount(tree, "project", 0);
            assertNamedProjectHasTechnologyCount(tree, "project", 1);
            assertNamedProjectHasTechnologyNamed(tree, "project", "tech");
        } catch (IOException e) {
            fail("Expected to not throw IOException");
        }
    }

    @Test
    public void testResearchProjectDependency() {
        DummyXMLEvents[] events = {
                new DummyXMLStartTag("ResearchTree", new DummyXMLAttribute[]{}),
                new DummyXMLStartEndTag("ResearchProject", new DummyXMLAttribute[]{new DummyXMLAttribute("name", "project")}),
                new DummyXMLStartTag("ResearchProject", new DummyXMLAttribute[]{new DummyXMLAttribute("name", "project 2")}),
                new DummyXMLStartTag("Requirements", new DummyXMLAttribute[]{}),
                new DummyXMLStartEndTag("Requires", new DummyXMLAttribute[]{new DummyXMLAttribute("name", "project")}),
                new DummyXMLEndTag(),
                new DummyXMLEndTag(),
                new DummyXMLEndTag(),
                new DummyXMLEndDocument(),
        };
        try {
            XmlPullParser xpp = new DummyXmlPullParser(events);
            ResearchTree tree = ResearchTree.loadXmlResearchTree(xpp);
            assertTreeHasProjectCount(tree, 2);
            assertTreeHasProjectNamed(tree, "project");
            assertTreeHasProjectNamed(tree, "project 2");
            assertNamedProjectHasDependencyCount(tree, "project", 0);
            assertNamedProjectHasTechnologyCount(tree, "project", 0);
            assertNamedProjectHasDependencyCount(tree, "project 2", 1);
            assertNamedProjectHasDependencyNamed(tree, "project 2", "project");
            assertNamedProjectHasTechnologyCount(tree, "project", 0);
        } catch (IOException e) {
            fail("Expected to not throw IOException");
        }
    }

    @Test
    public void testComplexResearchProjectDependencies() {
        DummyXMLEvents[] events = {
                new DummyXMLStartTag("ResearchTree", new DummyXMLAttribute[]{}),
                new DummyXMLStartEndTag("ResearchProject", new DummyXMLAttribute[]{new DummyXMLAttribute("name", "p1")}),
                new DummyXMLStartTag("ResearchProject", new DummyXMLAttribute[]{new DummyXMLAttribute("name", "p2")}),
                new DummyXMLStartTag("Requirements", new DummyXMLAttribute[]{}),
                new DummyXMLStartEndTag("Requires", new DummyXMLAttribute[]{new DummyXMLAttribute("name", "p1")}),
                new DummyXMLEndTag(),
                new DummyXMLStartTag("Technologies", new DummyXMLAttribute[]{}),
                new DummyXMLStartEndTag("Technology", new DummyXMLAttribute[]{new DummyXMLAttribute("name", "tech2")}),
                new DummyXMLEndTag(),
                new DummyXMLEndTag(),
                new DummyXMLStartTag("ResearchProject", new DummyXMLAttribute[]{new DummyXMLAttribute("name", "p3")}),
                new DummyXMLStartTag("Requirements", new DummyXMLAttribute[]{}),
                new DummyXMLStartEndTag("Requires", new DummyXMLAttribute[]{new DummyXMLAttribute("name", "p1")}),
                new DummyXMLEndTag(),
                new DummyXMLStartTag("Technologies", new DummyXMLAttribute[]{}),
                new DummyXMLStartEndTag("Technology", new DummyXMLAttribute[]{new DummyXMLAttribute("name", "tech3")}),
                new DummyXMLEndTag(),
                new DummyXMLEndTag(),
                new DummyXMLStartTag("ResearchProject", new DummyXMLAttribute[]{new DummyXMLAttribute("name", "p4")}),
                new DummyXMLStartTag("Requirements", new DummyXMLAttribute[]{}),
                new DummyXMLStartEndTag("Requires", new DummyXMLAttribute[]{new DummyXMLAttribute("name", "p2")}),
                new DummyXMLStartEndTag("Requires", new DummyXMLAttribute[]{new DummyXMLAttribute("name", "p3")}),
                new DummyXMLEndTag(),
                new DummyXMLStartTag("Technologies", new DummyXMLAttribute[]{}),
                new DummyXMLStartEndTag("Technology", new DummyXMLAttribute[]{new DummyXMLAttribute("name", "tech4")}),
                new DummyXMLEndTag(),
                new DummyXMLEndTag(),
                new DummyXMLEndTag(),
                new DummyXMLEndDocument(),
        };
        try {
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
        } catch (IOException e) {
            fail("Expected to not throw IOException");
        }

    }

    class DummyXMLAttribute {
        public final String name;
        public final String value;

        @SuppressWarnings("SameParameterValue")
        public DummyXMLAttribute(String name, String value) {
            this.name = name;
            this.value = value;
        }
    }

    class DummyXMLEvents {
        public final int event;
        public final boolean isEmpty;
        public final String elementName;
        public final String textContent;
        public final DummyXMLAttribute[] attributes;

        public DummyXMLEvents(int event, boolean isEmpty, String elementName, String textContent, DummyXMLAttribute[] attributes) {
            this.event = event;
            this.isEmpty = isEmpty;
            this.elementName = elementName;
            this.textContent = textContent;
            if (attributes == null) {
                this.attributes = new DummyXMLAttribute[0];
            } else {
                this.attributes = attributes;
            }
        }
    }

    class DummyXMLStartTag extends DummyXMLEvents {
        public DummyXMLStartTag(String elementName, DummyXMLAttribute[] attributes) {
            super(XmlPullParser.START_TAG, false, elementName, null, attributes);
        }
    }

    class DummyXMLStartEndTag extends DummyXMLEvents {
        public DummyXMLStartEndTag(String elementName, DummyXMLAttribute[] attributes) {
            super(XmlPullParser.START_TAG, true, elementName, null, attributes);
        }
    }

    class DummyXMLEndTag extends DummyXMLEvents {
        public DummyXMLEndTag() {
            super(XmlPullParser.END_TAG, false, null, null, null);
        }
    }

    class DummyXMLText extends DummyXMLEvents {
        @SuppressWarnings("SameParameterValue")
        public DummyXMLText(String text) {
            super(XmlPullParser.TEXT, false, null, text, null);
        }
    }

    class DummyXMLEndDocument extends DummyXMLEvents {
        public DummyXMLEndDocument() {
            super(XmlPullParser.END_DOCUMENT, false, null, null, null);
        }
    }

    class DummyXmlPullParser implements XmlPullParser {

        DummyXMLEvents[] events;
        int index;
        int depth;

        public DummyXmlPullParser(DummyXMLEvents[] events) {
            try {
                this.events = events;
                this.setInput(null);
            } catch (XmlPullParserException e) { /* never thrown */ }
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
