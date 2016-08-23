package com.github.jearls.ascendancyassistant;

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

    class DummyXmlPullParser implements XmlPullParser {

        // <ResearchTree>
        //  <ResearchProject name="foo">
        //   <Technologies>
        //    <Technology name="boo!" />
        //   </Technology>
        //  </ResearchProject>
        //  <ResearchProject name="bar">
        //   <Requirements>
        //    <Requires name="foo" />
        //   </Requirements>
        //  </ResearchProject>
        //  <ResearchProject name="baz" />
        //  <ResearchProject name="quux">
        //   <Requirements>
        //    <Requires name="foo" />
        //    <Requires name="baz" />
        //   </Requirements>
        //   <Technologies>
        //    <Technology name="cookies!" />
        //   </Technology>
        //  </ResearchProject>
        // </ResearchTree>
        //    

        final int eventType[] = {
            XmlPullParser.START_TAG , // ResearchTree
            XmlPullParser.TEXT      , // new line
            XmlPullParser.START_TAG , // ResearchProject name="foo"
            XmlPullParser.TEXT      , // new line
            XmlPullParser.START_TAG , // Technologies
            XmlPullParser.TEXT      , // new line
            XmlPullParser.START_TAG , // Technology name="boo!"
            XmlPullParser.END_TAG   , // Technology
            XmlPullParser.TEXT      , // new line
            XmlPullParser.END_TAG   , // Technologies
            XmlPullParser.TEXT      , // new line
            XmlPullParser.END_TAG   , // ResearchProject
            XmlPullParser.TEXT      , // new line
            XmlPullParser.START_TAG , // ResearchProject name="bar"
            XmlPullParser.TEXT      , // new line
            XmlPullParser.START_TAG , // Requirements
            XmlPullParser.TEXT      , // new line
            XmlPullParser.START_TAG , // Requires name="foo"
            XmlPullParser.END_TAG   , // Requires
            XmlPullParser.TEXT      , // new line
            XmlPullParser.END_TAG   , // Requirements
            XmlPullParser.TEXT      , // new line
            XmlPullParser.END_TAG   , // ResearchProject
            XmlPullParser.TEXT      , // new line
            XmlPullParser.START_TAG , // ResearchProject name="baz"
            XmlPullParser.END_TAG   , // ResearchProject
            XmlPullParser.TEXT      , // new line
            XmlPullParser.START_TAG , // ResearchProject name="bar"
            XmlPullParser.TEXT      , // new line
            XmlPullParser.START_TAG , // Requirements
            XmlPullParser.TEXT      , // new line
            XmlPullParser.START_TAG , // Requires name="foo"
            XmlPullParser.END_TAG   , // Requires
            XmlPullParser.TEXT      , // new line
            XmlPullParser.START_TAG , // Requires name="baz"
            XmlPullParser.END_TAG   , // Requires
            XmlPullParser.TEXT      , // new line
            XmlPullParser.END_TAG   , // Requirements
            XmlPullParser.TEXT      , // new line
            XmlPullParser.START_TAG , // Technologies
            XmlPullParser.TEXT      , // new line
            XmlPullParser.START_TAG , // Technology name="cookies!"
            XmlPullParser.END_TAG   , // Technology
            XmlPullParser.TEXT      , // new line
            XmlPullParser.END_TAG   , // Technologies
            XmlPullParser.TEXT      , // new line
            XmlPullParser.END_TAG   , // ResearchProject
            XmlPullParser.TEXT      , // new line
            XmlPullParser.END_TAG   , // ResearchTree
            XmlPullParser.TEXT      , // new line
            XmlPullParser.END_DOCUMENT
        } ;

        final boolean elementIsEmpty[] = {
            false ,
            false ,
            false ,
            false ,
            false ,
            false ,
            true ,
            false ,
            false ,
            false ,
            false ,
            false ,
            false ,
            false ,
            false ,
            false ,
            false ,
            true ,
            false ,
            false ,
            false ,
            false ,
            false ,
            false ,
            true ,
            false ,
            false ,
            false ,
            false ,
            false ,
            false ,
            true ,
            false ,
            false ,
            true ,
            false ,
            false ,
            false ,
            false ,
            false ,
            false ,
            true ,
            false ,
            false ,
            false ,
            false ,
            false ,
            false ,
            false ,
            false ,
            false ,
        } ;

        final String elementName[] = {
            "ResearchTree" ,
            null ,
            "ResearchProject" ,
            null ,
            "Technologies" ,
            null ,
            "Technology" ,
            null ,
            null ,
            null ,
            null ,
            null ,
            null ,
            "ResearchProject" ,
            null ,
            "Requirements" ,
            null ,
            "Requires" ,
            null ,
            null ,
            null ,
            null ,
            null ,
            null ,
            "ResearchProject" ,
            null ,
            null ,
            "ResearchProject" ,
            null ,
            "Requirements" ,
            null ,
            "Requires" ,
            null ,
            null ,
            "Requires" ,
            null ,
            null ,
            null ,
            null ,
            "Technologies" ,
            null ,
            "Technology" ,
            null ,
            null ,
            null ,
            null ,
            null ,
            null ,
            null ,
            null ,
            null ,
        } ;

        final String textContent[] = {
            null ,
            "\n" ,
            null ,
            "\n" ,
            null ,
            "\n" ,
            null ,
            null ,
            "\n" ,
            null ,
            "\n" ,
            null ,
            "\n" ,
            null ,
            "\n" ,
            null ,
            "\n" ,
            null ,
            null ,
            "\n" ,
            null ,
            "\n" ,
            null ,
            "\n" ,
            null ,
            null ,
            "\n" ,
            null ,
            "\n" ,
            null ,
            "\n" ,
            null ,
            null ,
            "\n" ,
            null ,
            null ,
            "\n" ,
            null ,
            "\n" ,
            null ,
            "\n" ,
            null ,
            null ,
            "\n" ,
            null ,
            "\n" ,
            null ,
            "\n" ,
            null ,
            "\n" ,
            null ,
        } ;

        final String[] attributeValues[] = {
            {} ,
            {} ,
            { "foo" , } ,
            {} ,
            {} ,
            {} ,
            { "boo!" , } ,
            {} ,
            {} ,
            {} ,
            {} ,
            {} ,
            {} ,
            { "bar" , } ,
            {} ,
            {} ,
            {} ,
            { "foo" , } ,
            {} ,
            {} ,
            {} ,
            {} ,
            {} ,
            {} ,
            { "baz" , } ,
            {} ,
            {} ,
            { "bar" , } ,
            {} ,
            {} ,
            {} ,
            { "foo" , } ,
            {} ,
            {} ,
            { "baz" , } ,
            {} ,
            {} ,
            {} ,
            {} ,
            {} ,
            {} ,
            { "cookies!" , } ,
            {} ,
            {} ,
            {} ,
            {} ,
            {} ,
            {} ,
            {} ,
            {} ,
            {} ,
        } ;

        final String[] attributeNames[] = {
            {} ,
            {} ,
            { "name" , } ,
            {} ,
            {} ,
            {} ,
            { "name" , } ,
            {} ,
            {} ,
            {} ,
            {} ,
            {} ,
            {} ,
            { "name" , } ,
            {} ,
            {} ,
            {} ,
            { "name" , } ,
            {} ,
            {} ,
            {} ,
            {} ,
            {} ,
            {} ,
            { "name" , } ,
            {} ,
            {} ,
            { "name" , } ,
            {} ,
            {} ,
            {} ,
            { "name" , } ,
            {} ,
            {} ,
            { "name" , } ,
            {} ,
            {} ,
            {} ,
            {} ,
            {} ,
            {} ,
            { "name" , } ,
            {} ,
            {} ,
            {} ,
            {} ,
            {} ,
            {} ,
            {} ,
            {} ,
            {} ,
        } ;

        int index;
        int depth;

        public DummyXmlPullParser() {
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
            return self.textContent[self.index];
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
            return self.elementName[self.index];
        }

        public String getPrefix() {
            return null;
        }

        public boolean isEmptyElementTag() throws XmlPullParserException {
            if (self.getEventType() != XmlPullParser.START_TAG) {
                throw new XmlPullParserException("Invalid element");
            }
            return self.elementIsEmpty[self.index];
        }

        public int getAttributeCount() {
            if (self.getEventType() == XmlPullParser.START_TAG) {
                return (self.attributeNames[self.index].length);
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
            return self.attributeNames[self.index][index];
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
            return self.attributeValues[self.index][index];
        }

        public String getAttributeValue(String namespace, String name) {
            if (self.getEventType() != XmlPullParser.START_TAG) {
                throw new XmlPullParserException("Invalid element");
            }
            if (namespace == null) {
                for (int i = 0; i < self.attributeNames[self.index].length; i += 1) {
                    if (self.attributeNames[self.index][i] == name) {
                        return self.attributeValues[self.index][i];
                    }
                }
            }
            return null;
        }

        public int getEventType() {
            return self.eventType[self.index];
        }

        public int next() {
            if (self.index > 0 && self.eventType[self.index-1] == XmlPullParser.END_TAG) {
                self.depth -= 1;
            }
            self.index += 1;
            if (self.index >= self.eventType.length) {
                throw new XmlPullParserException("No more data");
            }
            if (self.eventType[self.index] == XmlPullParser.START_TAG) {
                self.depth += 1;
            }
            return self.eventType[self.index];
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

    static int setProjectOrder(ResearchProject project, Map<ResearchProject,Integer> ordering) {
        if (ordering.containsKey(project)) {
            return ordering.get(project).intValue();
        } else {
            Iterator<ResearchProject> deps = project.getDependencies();
            int order = 0;
            while (deps.hasNext()) {
                int depOrder = setProjectOrder(deps.next(), ordering);
                if (depOrder >= order) {
                    order = depOrder + 1;
                }
            }
            ordering.put(project, new Integer(order));
            return order;
        }
    }

    public static void main(String args[]) throws IOException, XmlPullParserException {
        XmlPullParser testParser = new DummyXmlPullParser();
        ResearchTree tree = ResearchTree.loadXmlResearchTree(testParser);
        // we should have four projects:
        //   foo - dependencies { }, technologies { "boo!" }
        //   bar - dependencies { "foo" }, technologies { }
        //   baz - dependencies { }, technologies { }
        //   quux - dependencies { "foo", "baz" }, technologies { "cookies!" }
        Iterator<ResearchProject> pi = tree.getResearchProjects();
        while (pi.hasNext()) {
            ResearchProject proj = pi.next();
            if (proj.getName() == "foo") {
                //   dependencies { }
                //   technologies { "boo!" }
            } else if (proj.getName() == "bar") {
                //   dependencies { "foo" }
                //   technologies { }
            } else if (proj.getName() == "baz") {
                //   dependencies { }
                //   technologies { }
            } else if (proj.getName() == "quux") {
                //   dependencies { "foo", "baz" }
                //   technologies { "cookies!" }
            } else {
                System.err.println("Unknown project " + proj.getName());
            }
        }
    }

}
