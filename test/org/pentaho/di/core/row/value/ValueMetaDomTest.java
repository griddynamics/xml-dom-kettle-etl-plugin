package org.pentaho.di.core.row.value;

import static org.junit.Assert.*;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.junit.Test;
import org.pentaho.di.DOMTestUtilities;
import org.pentaho.di.core.exception.KettleValueException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class ValueMetaDomTest {

  private static final String SAMPLE_XML_STRING = "<some><xml>text</xml></some>";

  @Test
  public void testGetString() throws ParserConfigurationException, SAXException, IOException, KettleValueException, XPathExpressionException {
    ValueMetaDom vmd = new ValueMetaDom();
    Document doc = DOMTestUtilities.createTestDocument(SAMPLE_XML_STRING);
    String xmlString = vmd.getString(doc);
    assertNull(DOMTestUtilities.validateXPath(xmlString, new String[]{"/some/xml/text()[1]='text'"}));
  }

}
