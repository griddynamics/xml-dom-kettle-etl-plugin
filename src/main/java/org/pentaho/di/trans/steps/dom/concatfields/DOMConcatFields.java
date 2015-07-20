package org.pentaho.di.trans.steps.dom.concatfields;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class DOMConcatFields extends BaseStep implements StepInterface {

	private static Class<?> PKG = DOMConcatFields.class; // for i18n purposes, needed by Translator2!!

	private DOMConcatFieldsData data;
	private DOMConcatFieldsMeta meta;

	public DOMConcatFields(StepMeta s, StepDataInterface stepDataInterface, int c,
			TransMeta t, Trans dis) {
		super(s, stepDataInterface, c, t, dis);
	}

	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi)
			throws KettleException {
		meta = (DOMConcatFieldsMeta) smi;
		data = (DOMConcatFieldsData) sdi;

		Object[] r = getRow(); // get row, blocks when needed!
		if (r == null) // no more input to be expected...
		{
			setOutputDone();
			return false;
		}

		if (first) {
			first = false;
			data.outputRowMeta = getInputRowMeta().clone();
			meta.getFields(data.outputRowMeta, getStepname(), null, null, this,
					repository, metaStore);

			data.inputRowMetaModified = getInputRowMeta().clone();
			data.fieldnrs = new int[meta.getOutputFields().length];

			for (int i = 0; i < meta.getOutputFields().length; i++) {
				data.fieldnrs[i] = data.inputRowMetaModified.indexOfValue(meta
						.getOutputFields()[i].getName());
				if (data.fieldnrs[i] < 0) {
					throw new KettleStepException(BaseMessages.getString(PKG,
							"ConcatFields.Error.FieldNotFoundInputStream", ""
									+ meta.getOutputFields()[i].getName()));
				}
			}

			 
			Document joinableDoc = (Document)r[data.fieldnrs[0]];
			data.documentRoot = joinableDoc.getFirstChild();
		}

		Document resultDocument = mergeDocuments(r);
		
		Object[] outputRowData = RowDataUtil.addValueData(r, getInputRowMeta()
				.size(), resultDocument);
		putRow(data.outputRowMeta, outputRowData);

		return true;
	}

	private Document mergeDocuments(Object[] r) {
		Document resultDocument = data.documentBuilder.newDocument();
		Element rootElement = resultDocument.createElement(data.documentRoot
				.getNodeName());
		resultDocument.appendChild(rootElement);

		for (int i = 0; i < meta.getOutputFields().length; i++) {

			Document doc = (Document) (r[data.fieldnrs[i]]);
			
			Element root = doc.getDocumentElement();
			NodeList childNodes = root.getChildNodes();

			for (int j = 0; j < childNodes.getLength(); j++) {
				Node importNode = resultDocument.importNode(childNodes.item(j),
						true);
				rootElement.appendChild(importNode);
			}

		}

		return resultDocument;
	}

	public boolean init(StepMetaInterface smi, StepDataInterface sdi) {
		meta = (DOMConcatFieldsMeta) smi;
		data = (DOMConcatFieldsData) sdi;

		if (!super.init(smi, sdi)) {
			return false;
		}

		try {
			data.documentBuilder = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder();
		} catch (ParserConfigurationException e) {

			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

		return true;
	}

	public void dispose(StepMetaInterface smi, StepDataInterface sdi) {
		meta = (DOMConcatFieldsMeta) smi;
		data = (DOMConcatFieldsData) sdi;

		super.dispose(smi, sdi);
	}

}
