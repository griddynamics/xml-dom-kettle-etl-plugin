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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.pentaho.di.DOMTestUtilities;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.RowStepCollector;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.mock.StepMockHelper;
import org.pentaho.di.trans.steps.textfileoutput.TextFileField;
import org.w3c.dom.Document;

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
  private static String EQUAL_ROOT_TAGS_CONCAT_RESULT = "<root><child1>child1 content</child1><child2>child2 content</child2></root>";
  @Rule public TestName name = new TestName();

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
  }

  @After
  public void tearDown() throws Exception {
    stepMockHelper.cleanUp();
  }

  @Test
  public void testEqualRootTagsInputDocs() throws Exception {
    testProcessRow(CONCAT_DOM_INPUT1, CONCAT_DOM_INPUT2, EQUAL_ROOT_TAGS_CONCAT_RESULT,name.getMethodName() + "failed");
  }

  private void testProcessRow(String doc1, String doc2,
      String goldenImageXML, String errMessage) throws Exception {
    ConcatFieldsHandler concatFields = new ConcatFieldsHandler(
        stepMockHelper.stepMeta, stepMockHelper.stepDataInterface, 0,
        stepMockHelper.transMeta, stepMockHelper.trans);
    Object[] row = new Object[] { DOMTestUtilities.createTestDocument(doc1),
        DOMTestUtilities.createTestDocument(doc2) };
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
    
    concatFields.init(stepMockHelper.processRowsStepMetaInterface,
        stepMockHelper.processRowsStepDataInterface);
    RowStepCollector dummyRowCollector = new RowStepCollector();
    concatFields.addRowListener(dummyRowCollector);
    concatFields.processRow(stepMockHelper.processRowsStepMetaInterface,
        stepMockHelper.processRowsStepDataInterface);
    List<RowMetaAndData> resultRows = dummyRowCollector.getRowsWritten();
     
    Assert.assertEquals(errMessage, goldenImageXML, DOMTestUtilities.toString((Document)resultRows.get(0).getData()[2]));

  }
  
}
