/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2014 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.trans.steps.dom.concatfields;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.pentaho.di.TestFailedException;
import org.pentaho.di.TestUtilities;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaBase;
import org.pentaho.di.core.row.value.ValueMetaDom;
import org.pentaho.di.trans.RowStepCollector;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.mock.StepMockHelper;
import org.pentaho.di.trans.steps.textfileoutput.TextFileField;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * User: Dzmitry Stsiapanau Date: 2/11/14 Time: 11:00 AM
 */
public class DOMConcatFieldsTest {

	private class ConcatFieldsHandler extends DOMConcatFields {

		private Object[] row;

		public ConcatFieldsHandler(StepMeta stepMeta,
				StepDataInterface stepDataInterface, int copyNr,
				TransMeta transMeta, Trans trans) {
			super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
		}

		/**
		 * In case of getRow, we receive data from previous steps through the
		 * input rowset. In case we split the stream, we have to copy the data
		 * to the alternate splits: rowsets 1 through n.
		 */
		@Override
		public Object[] getRow() throws KettleException {
			return row;
		}

		public void setRow(Object[] row) {
			this.row = row;
		}

	}

	private StepMockHelper<DOMConcatFieldsMeta, DOMConcatFieldsData> stepMockHelper;
	private TextFileField textFileField = new TextFileField("Name", 2, "", 10,
			20, "", "", "", "");
	private TextFileField textFileField2 = new TextFileField("Surname", 2, "",
			10, 20, "", "", "", "");
	private TextFileField[] textFileFields = new TextFileField[] {
			textFileField, textFileField2 };
	private static String CONCAT_DOM_INPUT1 = "<root><child1>child1 content</child1></root>";
	private static String CONCAT_DOM_INPUT2 = "<root><child2>child2 content</child2></root>";
	private static String CONCAT_DOM_INPUT3 = "<differentRoot><child3>child3 content</child3></differentRoot>";
	private static String EQUAL_ROOT_TAGS_CONCAT_RESULT = "<root><child1>child1 content</child1><child2>child2 content</child2></root>";
	//private static String DIFFERENT_ROOT_TAGS_CONCAT_RESULT = "<root><child1>child1 content</child1><child2>child2 content</child2></root>";
	private static DocumentBuilder db;
	@Rule public TestName name = new TestName();

//	private List<RowMetaAndData> createEqualRootTagsResults(RowMetaInterface rowMetaInterface, Object[] result) {
//		List<RowMetaAndData> list = new ArrayList<RowMetaAndData>();
//
//		Object[] r1 = new Object[] { new Long(1L), "Orlando", "Florida" };
//		Object[] r2 = new Object[] { new Long(1L), "Orlando", "Florida" };
//		Object[] r3 = new Object[] { new Long(1L), "Orlando", "Florida" };
//
//		list.add(new RowMetaAndData(rowMetaInterface, r1));
//		list.add(new RowMetaAndData(rowMetaInterface, r2));
//		list.add(new RowMetaAndData(rowMetaInterface, r3));
//
//		return list;
//	}

	@Before
	public void setUp() throws Exception {
		stepMockHelper = new StepMockHelper<DOMConcatFieldsMeta, DOMConcatFieldsData>(
				"CONCAT DOM FIELDS TEST", DOMConcatFieldsMeta.class,
				DOMConcatFieldsData.class);
		when(
				stepMockHelper.logChannelInterfaceFactory.create(any(),
						any(LoggingObjectInterface.class))).thenReturn(
				stepMockHelper.logChannelInterface);
		when(stepMockHelper.trans.isRunning()).thenReturn(true);
		db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
	}

	@After
	public void tearDown() throws Exception {
		stepMockHelper.cleanUp();
	}

//	@Test
//	public void testDifferentRootTagsInputDocs() throws Exception {
//		getProcessRowResult(CONCAT_DOM_INPUT1, CONCAT_DOM_INPUT3, DIFFERENT_ROOT_TAGS_CONCAT_RESULT);//, createEqualRootTagsResultRow());
//	}

	@Test
	public void testEqualRootTagsInputDocs() throws Exception {
		getProcessRowResult(CONCAT_DOM_INPUT1, CONCAT_DOM_INPUT2, EQUAL_ROOT_TAGS_CONCAT_RESULT,name.getMethodName() + "failed");
	}

	private void getProcessRowResult(String doc1, String doc2,
			String goldenImageXML, String errMessage) throws Exception {
		ConcatFieldsHandler concatFields = new ConcatFieldsHandler(
				stepMockHelper.stepMeta, stepMockHelper.stepDataInterface, 0,
				stepMockHelper.transMeta, stepMockHelper.trans);
		Object[] row = new Object[] { createTestDocument(doc1),
				createTestDocument(doc2) };
		String[] fieldNames = new String[] { "one", "two" };
		concatFields.setRow(row);
		RowMetaInterface inputRowMeta = mock(RowMetaInterface.class);
		when(inputRowMeta.clone()).thenReturn(inputRowMeta);
		when(inputRowMeta.size()).thenReturn(2);
		when(inputRowMeta.getFieldNames()).thenReturn(fieldNames);
		when(inputRowMeta.indexOfValue("Name")).thenReturn(0);
		when(inputRowMeta.indexOfValue("Surname")).thenReturn(1);
		when(stepMockHelper.processRowsStepMetaInterface.getOutputFields())
				.thenReturn(textFileFields);

		concatFields.setInputRowMeta(inputRowMeta);
//		List<ValueMetaInterface> valueMetaList = new ArrayList<ValueMetaInterface>();
//		valueMetaList.add(new ValueMetaBase("Name"));
//		valueMetaList.add(new ValueMetaBase("Surname"));
//		RowMeta rowMeta = new RowMeta();
//		rowMeta.setValueMetaList(valueMetaList);
		
		concatFields.init(stepMockHelper.processRowsStepMetaInterface,
				stepMockHelper.processRowsStepDataInterface);
		//concatFields.setInputRowMeta(rowMeta);
		RowStepCollector dummyRowCollector = new RowStepCollector();
		concatFields.addRowListener(dummyRowCollector);
		concatFields.processRow(stepMockHelper.processRowsStepMetaInterface,
				stepMockHelper.processRowsStepDataInterface);
		List<RowMetaAndData> resultRows = dummyRowCollector.getRowsWritten();
		 
		Assert.assertEquals(errMessage,goldenImageXML,toString((Document)resultRows.get(0).getData()[2]));
			
		
		
//		try {
//			TestUtilities.checkRows(goldenImageRows, resultRows);
//		} catch (TestFailedException tfe) {
//			fail(tfe.getMessage());
//		}
	}

	private static Document createTestDocument(String str) {
		Document doc = null;
		try {
			InputSource is = new InputSource();
			is.setCharacterStream(new StringReader(str));
			doc = db.parse(is);
		} catch (SAXException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return doc;
	}
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
}
