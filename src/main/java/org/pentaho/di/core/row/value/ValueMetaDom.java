package org.pentaho.di.core.row.value;

import java.io.StringWriter;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.w3c.dom.Document;

@ValueMetaPlugin(id = "200", name = "DOM", description = "DOM type")
public class ValueMetaDom extends ValueMetaBase implements ValueMetaInterface {

	public static final int TYPE_DOM = 200;

	public ValueMetaDom() {
		this(null);
	}

	public ValueMetaDom(String name) {
		super(name, TYPE_DOM);
	}

	public ValueMetaDom(String valueName, int typeString) {
		super(valueName, typeString);
	}

	public ValueMetaDom(String targetFieldName, int typeString,
			int targetFieldLength, int precision) {
		super(targetFieldName, typeString, targetFieldLength, precision);
	}

	@Override
	public String getString(Object object) throws KettleValueException {
		return toString((Document) object);
	}

	public static String toString(Document doc) {
		try {
			StringWriter sw = new StringWriter();
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer transformer = tf.newTransformer();
			transformer
					.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			transformer.setOutputProperty(OutputKeys.INDENT, "no");
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

			transformer.transform(new DOMSource(doc), new StreamResult(sw));
			return sw.toString();
		} catch (Exception ex) {
			throw new RuntimeException("Error converting to String", ex);
		}
	}
}
