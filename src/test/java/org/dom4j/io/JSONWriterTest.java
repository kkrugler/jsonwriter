/*
 * Copyright 2007 Krugle, Inc.
 * 
   Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *   http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */

package org.dom4j.io;

import java.io.StringWriter;

import junit.textui.TestRunner;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.tree.FlyweightText;

/**
 * A simple test harness to check that the JSON Writer works.
 * 
 * Based on XMLWriterTest, by James Strachan
 */
public class JSONWriterTest extends AbstractTestCase {
    private static final Logger LOGGER = Logger.getLogger(JSONWriterTest.class);

    protected static final boolean VERBOSE = false;

    public static void main(String[] args) {
        TestRunner.run(JSONWriterTest.class);
    }

    // Test case(s)
    // -------------------------------------------------------------------------
    public void testWriter() throws Exception {
        Object object = document;
        StringWriter out = new StringWriter();

        JSONWriter writer = new JSONWriter(out);
        writer.write(object);
        writer.close();

        String text = out.toString();

        if (VERBOSE) {
            LOGGER.info("Text output is [");
            LOGGER.info(text);
            LOGGER.info("]. Done");
        }

        assertTrue("Output text should be bigger than 10 characters",
                        text.length() > 10);
    }

    public void testTextContent() throws Exception {
        Document doc = DocumentFactory.getInstance().createDocument();
        Element alice = doc.addElement("alice");
        alice.add(new FlyweightText("bob"));

        dumpXMLOutput(doc);
        checkJSONOutput(doc, JSONFormat.BASIC_OUTPUT, "{ \"alice\": \"bob\" }");
        checkJSONOutput(doc, JSONFormat.RABBIT_FISH, "{ \"alice\": \"bob\" }");
        checkJSONOutput(doc, JSONFormat.BADGER_FISH, "{ \"alice\": { \"$\": \"bob\" } }");
    }

    public void testNestedElements() throws Exception {
        Document doc = DocumentFactory.getInstance().createDocument();
        Element alice = doc.addElement("alice");
        Element bob = alice.addElement("bob");
        bob.add(new FlyweightText("charlie"));
        Element david = alice.addElement("david");
        david.add(new FlyweightText("edgar"));

        dumpXMLOutput(doc);
        checkJSONOutput(doc, JSONFormat.BASIC_OUTPUT, "{ \"alice\": { \"bob\": \"charlie\", \"david\": \"edgar\" } }");
        checkJSONOutput(doc, JSONFormat.RABBIT_FISH, "{ \"alice\": { \"bob\": \"charlie\", \"david\": \"edgar\" } }");
        checkJSONOutput(doc, JSONFormat.BADGER_FISH, "{ \"alice\": { \"bob\": { \"$\": \"charlie\" }, \"david\": { \"$\": \"edgar\" } } }");
    }

    public void testMultiElementArray() throws Exception {
        Document doc = DocumentFactory.getInstance().createDocument();
        Element alice = doc.addElement("alice");
        Element bob1 = alice.addElement("bob");
        bob1.add(new FlyweightText("charlie"));
        Element bob2 = alice.addElement("bob");
        bob2.add(new FlyweightText("david"));

        dumpXMLOutput(doc);
        checkJSONOutput(doc, JSONFormat.BASIC_OUTPUT, "{ \"alice\": { \"bob\": [ \"charlie\", \"david\" ] } }");
        checkJSONOutput(doc, JSONFormat.RABBIT_FISH, "{ \"alice\": { \"bob\": [ \"charlie\", \"david\" ] } }");
        checkJSONOutput(doc, JSONFormat.BADGER_FISH, "{ \"alice\": { \"bob\": [ { \"$\": \"charlie\" }, { \"$\": \"david\" } ] } }");
    }

    public void testMixedContent() throws Exception {
        Document doc = DocumentFactory.getInstance().createDocument();
        Element alice = doc.addElement("alice");
        alice.add(new FlyweightText("bob"));
        Element charlie = alice.addElement("charlie");
        charlie.setText("david");
        alice.add(new FlyweightText("edgar"));

        dumpXMLOutput(doc);
        checkJSONOutput(doc, JSONFormat.BASIC_OUTPUT, "{ \"alice\": [ \"bob\", { \"charlie\": \"david\" }, \"edgar\" ] }");
        checkJSONOutput(doc, JSONFormat.RABBIT_FISH, "{ \"alice\": [ \"bob\", { \"charlie\": \"david\" }, \"edgar\" ] }");
        checkJSONOutput(doc, JSONFormat.BADGER_FISH, "{ \"alice\": [ { \"$\": \"bob\" }, { \"charlie\": { \"$\": \"david\" } }, { \"$\": \"edgar\" } ] }");
    }

    public void testAttributes() throws Exception {
        Document doc = DocumentFactory.getInstance().createDocument();
        Element alice = doc.addElement("alice");
        alice.addAttribute("charlie", "\\dav\"id/");
        alice.setText("\\bob\"by/");

        dumpXMLOutput(doc);
        checkJSONOutput(doc, JSONFormat.BASIC_OUTPUT, "{ \"alice\": { \"charlie\": \"\\\\dav\\\"id\\/\", \"$\": \"\\\\bob\\\"by\\/\" } }");
        checkJSONOutput(doc, JSONFormat.RABBIT_FISH, "{ \"alice\": { \"@charlie\": \"\\\\dav\\\"id\\/\", \"$\": \"\\\\bob\\\"by\\/\" } }");
        checkJSONOutput(doc, JSONFormat.BADGER_FISH, "{ \"alice\": { \"@charlie\": \"\\\\dav\\\"id\\/\", \"$\": \"\\\\bob\\\"by\\/\" } }");
    }

    public void testEmptyValue() throws Exception {
        Document doc = DocumentFactory.getInstance().createDocument();
        Element alice = doc.addElement("alice");
        alice.addElement("bob");
        Element charlie = alice.addElement("charlie");
        charlie.setText("david");
        Element edgar = alice.addElement("edgar");
        edgar.setText("");

        dumpXMLOutput(doc);
        checkJSONOutput(doc, JSONFormat.BASIC_OUTPUT, "{ \"alice\": { \"bob\": \"\", \"charlie\": \"david\", \"edgar\": \"\" } }");
        checkJSONOutput(doc, JSONFormat.RABBIT_FISH, "{ \"alice\": { \"bob\": \"\", \"charlie\": \"david\", \"edgar\": \"\" } }");
        checkJSONOutput(doc, JSONFormat.BADGER_FISH, "{ \"alice\": { \"bob\": { \"$\": \"\" }, \"charlie\": { \"$\": \"david\" }, \"edgar\": { \"$\": \"\" } } }");
    }

    public void testWhitespaceTrimming() throws Exception {
        Document doc = DocumentFactory.getInstance().createDocument();
        Element alice = doc.addElement("alice");
        alice.add(new FlyweightText("\n"));
        Element charlie = alice.addElement("charlie");
        charlie.setText("\n\n");
        alice.add(new FlyweightText(" \n \n "));
        alice.add(new FlyweightText("edgar"));

        dumpXMLOutput(doc);
        checkJSONOutput(doc, JSONFormat.BASIC_OUTPUT, "{ \"alice\": [ { \"charlie\": \"\" }, \"edgar\" ] }");
        checkJSONOutput(doc, JSONFormat.RABBIT_FISH, "{ \"alice\": [ { \"charlie\": \"\" }, \"edgar\" ] }");
        checkJSONOutput(doc, JSONFormat.BADGER_FISH, "{ \"alice\": [ { \"charlie\": { \"$\": \"\" } }, { \"$\": \"edgar\" } ] }");
    }

    public void testJsonElementName() throws Exception {
        Document doc = DocumentFactory.getInstance().createDocument();
        Element alice = doc.addElement("-al-ice\u0F00");
        Element charlie = alice.addElement("\u0F00bob+");
        charlie.setText("+-charlie");
        Element _private = alice.addElement("private");
        _private.setText("public");

        dumpXMLOutput(doc);
        checkJSONOutput(doc, JSONFormat.BASIC_OUTPUT, "{ \"_al_ice\u0F00\": { \"\u0F00bob_u002B_\": \"+-charlie\", \"_private\": \"public\" } }");
        checkJSONOutput(doc, JSONFormat.RABBIT_FISH, "{ \"_al_ice\u0F00\": { \"\u0F00bob_u002B_\": \"+-charlie\", \"_private\": \"public\" } }");
        checkJSONOutput(doc, JSONFormat.BADGER_FISH, "{ \"_al_ice\u0F00\": { \"\u0F00bob_u002B_\": { \"$\": \"+-charlie\" }, \"_private\": { \"$\": \"public\" } } }");
    }

    private void dumpXMLOutput(Document doc) throws Exception {
        StringWriter xmlBuffer = new StringWriter();
        XMLWriter xmlWriter = new XMLWriter(xmlBuffer);
        xmlWriter.write(doc);
        String xmlOutput = xmlBuffer.toString();
        System.out.println(String.format("%s: %s", "XML", xmlOutput));
    }

    private void checkJSONOutput(Document doc, JSONFormat format, String expectedOutput) throws Exception {
        StringWriter jsonBuffer = new StringWriter();
        JSONWriter jsonWriter = new JSONWriter(jsonBuffer, format);
        jsonWriter.write(doc);
        String jsonOutput = jsonBuffer.toString();   
        System.out.println(String.format("%s: %s", format.getName(), jsonOutput));

        assertTrue( String.format("Unexpected %s output:\r%s\rProper %s output:\r%s",
                        format.getName(),
                        jsonOutput,
                        format.getName(),
                        expectedOutput),
                        expectedOutput.trim().replaceAll("\\s+", " ")
                        .equals(jsonOutput.trim().replaceAll("\\s+", " ")));
    }

}

