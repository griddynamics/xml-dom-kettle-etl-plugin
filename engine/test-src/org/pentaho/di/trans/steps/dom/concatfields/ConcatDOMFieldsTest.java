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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.row.RowMetaInterface;
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
public class ConcatDOMFieldsTest {

  private class ConcatFieldsHandler extends DOMConcatFields {

    private Object[] row;

    public ConcatFieldsHandler( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr,
        TransMeta transMeta, Trans trans ) {
      super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
    }

    /**
     * In case of getRow, we receive data from previous steps through the input rowset. In case we split the stream, we
     * have to copy the data to the alternate splits: rowsets 1 through n.
     */
    @Override
    public Object[] getRow() throws KettleException {
      return row;
    }

    public void setRow( Object[] row ) {
      this.row = row;
    }

//    @Override
//    protected Object[] putRowFastDataDump( Object[] r ) throws KettleStepException {
//      return null;
//    }
//
//    @Override
//    protected boolean writeHeader() {
//      return true;
//    }

//    @Override
//    Object[] putRowFromStream( Object[] r ) throws KettleStepException {
//      return prepareOutputRow( r );
//    }
  }

  private StepMockHelper<DOMConcatFieldsMeta, DOMConcatFieldsData> stepMockHelper;
  private TextFileField textFileField = new TextFileField( "Name", 2, "", 10, 20, "", "", "", "" );
  private TextFileField textFileField2 = new TextFileField( "Surname", 2, "", 10, 20, "", "", "", "" );
  private TextFileField[] textFileFields = new TextFileField[] { textFileField, textFileField2 };

  @Before
  public void setUp() throws Exception {
    stepMockHelper =
        new StepMockHelper<DOMConcatFieldsMeta, DOMConcatFieldsData>( "CONCAT DOM FIELDS TEST", DOMConcatFieldsMeta.class,
            DOMConcatFieldsData.class );
    when( stepMockHelper.logChannelInterfaceFactory.create( any(), any( LoggingObjectInterface.class ) ) ).thenReturn(
        stepMockHelper.logChannelInterface );
    when( stepMockHelper.trans.isRunning() ).thenReturn( true );
  }

  @After
  public void tearDown() throws Exception {
    stepMockHelper.cleanUp();
  }

  @Test
  public void testProcessRow() throws Exception {
    ConcatFieldsHandler concatFields =
        new ConcatFieldsHandler( stepMockHelper.stepMeta, stepMockHelper.stepDataInterface, 0,
            stepMockHelper.transMeta, stepMockHelper.trans );
    Object[] row = new Object[] { 
    		createTestDocument("<root><child1>child1 content</child1></root>"),
    		createTestDocument("<root><child2>child2 content</child2></root>") 
    		};
    String[] fieldNames = new String[] { "one", "two" };
    concatFields.setRow( row );
    RowMetaInterface inputRowMeta = mock( RowMetaInterface.class );
    when( inputRowMeta.clone() ).thenReturn( inputRowMeta );
    when( inputRowMeta.size() ).thenReturn( 2 );
    when( inputRowMeta.getFieldNames() ).thenReturn( fieldNames );
    when( stepMockHelper.processRowsStepMetaInterface.getOutputFields() ).thenReturn( textFileFields );
    
//    when( stepMockHelper.processRowsStepMetaInterface.isFastDump() ).thenReturn( Boolean.TRUE );
//    when( stepMockHelper.processRowsStepMetaInterface.isFileAppended() ).thenReturn( Boolean.FALSE );
//    when( stepMockHelper.processRowsStepMetaInterface.isFileNameInField() ).thenReturn( Boolean.FALSE );
//    when( stepMockHelper.processRowsStepMetaInterface.isHeaderEnabled() ).thenReturn( Boolean.TRUE );
//    when( stepMockHelper.processRowsStepMetaInterface.isRemoveSelectedFields() ).thenReturn( Boolean.TRUE );
    concatFields.setInputRowMeta( inputRowMeta );
    concatFields.init(stepMockHelper.processRowsStepMetaInterface, stepMockHelper.processRowsStepDataInterface);
    try {
      concatFields.processRow( stepMockHelper.processRowsStepMetaInterface,
              stepMockHelper.processRowsStepDataInterface );
     // concatFields.prepareOutputRow( row );
    } catch ( NullPointerException npe ) {
      fail( "NullPointerException issue reproduced " );
    }
  }
  
  private static Document createTestDocument(String str) {
		Document doc = null;
		try {
			DocumentBuilder db;
				db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			    InputSource is = new InputSource();
			    is.setCharacterStream(new StringReader(str));
					doc = db.parse(is);
		} catch (ParserConfigurationException | SAXException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return doc;
	}
}
