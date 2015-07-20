/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
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

import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaDom;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.addxml.AddXMLMeta;
import org.pentaho.metastore.api.IMetaStore;

/**
 * This class knows how to handle the MetaData for the DOM XML output step
 *
 */
@Step(id = "AddDOMXML", image = "ui/images/add_xml.svg", name = "Add DOM XML", description = "Add DOM XML", categoryDescription = "i18n:org.pentaho.di.trans.step:BaseStep.Category.Transform")
public class AddDOMXMLMeta extends AddXMLMeta {
  private static Class<?> PKG = AddDOMXMLMeta.class; // for i18n purposes, needed by Translator2!!
  
  public void getFields( RowMetaInterface row, String name, RowMetaInterface[] info, StepMeta nextStep,
    VariableSpace space, Repository repository, IMetaStore metaStore ) throws KettleStepException {

    ValueMetaInterface v = new ValueMetaDom( this.getValueName(), ValueMetaDom.TYPE_DOM );
    v.setOrigin( name );
    row.addValueMeta( v );
  }

  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr,
    TransMeta transMeta, Trans trans ) {
    return new AddDOMXML( stepMeta, stepDataInterface, cnr, transMeta, trans );
  }

}
