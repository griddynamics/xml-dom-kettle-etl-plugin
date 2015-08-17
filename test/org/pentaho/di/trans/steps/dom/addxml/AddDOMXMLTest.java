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
package org.pentaho.di.trans.steps.dom.addxml;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.DOMTestUtilities;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.trans.RowStepCollector;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.addxml.AddXMLData;
import org.pentaho.di.trans.steps.addxml.XMLField;
import org.pentaho.di.trans.steps.mock.StepMockHelper;
import org.pentaho.di.www.SocketRepository;
import org.w3c.dom.Document;

public class AddDOMXMLTest {
  
  private static final String ADDDOMXML_PROCESSROW_RESULT = "<ADDDOMXML_TEST_ROW><ADDDOMXML_FIELD_NAME>ROW_DATA</ADDDOMXML_FIELD_NAME></ADDDOMXML_TEST_ROW>";

  private StepMockHelper<AddDOMXMLMeta, AddXMLData> stepMockHelper;
  
  private class AddDOMXMLHandler extends AddDOMXML {

    private Object[] row;
    
    public AddDOMXMLHandler(StepMeta stepMeta, StepDataInterface sdi,
        int copyNr, TransMeta tm, Trans trans) {
      super(stepMeta, sdi, copyNr, tm, trans);
    }

    @Override
    public Object[] getRow() throws KettleException {
      return row;
    }

    public void setRow(Object[] row) {
      this.row = row;
    }

  }  

  @Before
  public void setup() {
    XMLField field = mock( XMLField.class );
    when( field.getElementName() ).thenReturn( "ADDDOMXML_FIELD_NAME" );
    when( field.isAttribute() ).thenReturn( false );

    stepMockHelper = new StepMockHelper<AddDOMXMLMeta, AddXMLData>( "ADDDOMXML_TEST", AddDOMXMLMeta.class, AddXMLData.class );
    when( stepMockHelper.logChannelInterfaceFactory.create( any(), any( LoggingObjectInterface.class ) ) ).thenReturn( stepMockHelper.logChannelInterface );
    when( stepMockHelper.trans.isRunning() ).thenReturn( true );
    when( stepMockHelper.trans.getSocketRepository() ).thenReturn( mock( SocketRepository.class ) );
    when( stepMockHelper.initStepMetaInterface.getOutputFields() ).thenReturn( new XMLField[] { field } );
    when( stepMockHelper.initStepMetaInterface.getRootNode() ).thenReturn( "ADDDOMXML_TEST_ROW" );
  }

  @After
  public void tearDown() {
    stepMockHelper.cleanUp();
  }

  @Test
  public void testProcessRow() throws KettleException {
    AddDOMXMLHandler addXML = new AddDOMXMLHandler(
        stepMockHelper.stepMeta, stepMockHelper.stepDataInterface, 0,
        stepMockHelper.transMeta, stepMockHelper.trans);
    Object[] row = new Object[] { "ROW_DATA" };
    addXML.setRow(row);
    addXML.init( stepMockHelper.initStepMetaInterface, stepMockHelper.initStepDataInterface );
    
    RowMetaInterface inputRowMeta = createInputRowMeta();

    addXML.setInputRowMeta(inputRowMeta);    
    
    RowStepCollector dummyRowCollector = new RowStepCollector();
    addXML.addRowListener(dummyRowCollector);
    
    assertTrue( addXML.processRow( stepMockHelper.initStepMetaInterface, stepMockHelper.processRowsStepDataInterface ) );
    assertTrue( addXML.getErrors() == 0 );
    assertTrue( addXML.getLinesWritten() > 0 );

    List<RowMetaAndData> resultRows = dummyRowCollector.getRowsWritten();
     
    Assert.assertEquals(ADDDOMXML_PROCESSROW_RESULT, DOMTestUtilities.toString((Document)resultRows.get(0).getData()[1]));
  }

  private RowMetaInterface createInputRowMeta() {
    RowMetaInterface inputRowMeta = mock(RowMetaInterface.class);
    when(inputRowMeta.getValueMeta( 0 )).thenReturn(new ValueMetaString( "ADDDOMXML_TEST" ));
    when(inputRowMeta.clone()).thenReturn(inputRowMeta);
    when(inputRowMeta.size()).thenReturn(1);
    return inputRowMeta;
  }
   
}
