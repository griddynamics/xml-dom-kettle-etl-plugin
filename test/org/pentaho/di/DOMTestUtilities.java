package org.pentaho.di;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class DOMTestUtilities {
  private static final ThreadLocal<DocumentBuilder> builderTL = new ThreadLocal<DocumentBuilder>();
  private static final ThreadLocal<XPath> xpathTL = new ThreadLocal<XPath>();  
  
  public static String toString(Document doc) {
    try {
      StringWriter sw = new StringWriter();
      TransformerFactory tf = TransformerFactory.newInstance();
      Transformer transformer = tf.newTransformer();
      transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
      transformer.setOutputProperty(OutputKeys.METHOD, "xml");
      transformer.setOutputProperty(OutputKeys.INDENT, "no");
      transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

      transformer.transform(new DOMSource(doc), new StreamResult(sw));
      return sw.toString();
    } catch (Exception ex) {
      throw new RuntimeException("Error converting to String", ex);
    }
  }
  
  /**
   * A helper method which validates a String against an array of XPath test
   * strings.
   *
   * @param xml The xml String to validate
   * @param tests Array of XPath strings to test (in boolean mode) on the xml
   * @return null if all good, otherwise the first test that fails.
   */
  public static String validateXPath(String xml, String... tests)
      throws XPathExpressionException, SAXException {

    if (tests==null || tests.length == 0) return null;

    Document document = null;
    try {
      document = getXmlDocumentBuilder().parse(new ByteArrayInputStream
          (xml.getBytes(StandardCharsets.UTF_8)));
    } catch (UnsupportedEncodingException e1) {
      throw new RuntimeException("Totally weird UTF-8 exception", e1);
    } catch (IOException e2) {
      throw new RuntimeException("Totally weird io exception", e2);
    }

    for (String xp : tests) {
      xp=xp.trim();
      Boolean bool = (Boolean) getXpath().evaluate(xp, document, XPathConstants.BOOLEAN);

      if (!bool) {
        return xp;
      }
    }
    return null;
  } 
  
  public static Document createTestDocument(String str) {
    Document doc = null;
    try {
      InputSource is = new InputSource();
      is.setCharacterStream(new StringReader(str));
      doc = getXmlDocumentBuilder().parse(is);
    } catch (SAXException e) {
        throw new RuntimeException(e);
    } catch (IOException e) {
        throw new RuntimeException(e);
    }
    return doc;
  }

  public static DocumentBuilder getXmlDocumentBuilder() {
    try {
      DocumentBuilder builder = builderTL.get();
      if (builder == null) {
        builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        builderTL.set(builder);
      }
      return builder;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static XPath getXpath() {
    try {
      XPath xpath = xpathTL.get();
      if (xpath == null) {
        xpath = XPathFactory.newInstance().newXPath();
        xpathTL.set(xpath);
      }
      return xpath;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }  
}
