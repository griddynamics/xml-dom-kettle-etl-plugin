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

package org.pentaho.di.trans.steps.dom.xslt;

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
import org.pentaho.di.trans.steps.xslt.XsltMeta;
import org.pentaho.metastore.api.IMetaStore;

@Step(id = "DOMXSLT", image = "ui/images/XSLT.svg", name = "DOM XSLT Transformation", description = "DOM XSLT Transformation", categoryDescription = "i18n:org.pentaho.di.trans.step:BaseStep.Category.Transform")
public class DOMXsltMeta extends XsltMeta {
  private static Class<?> PKG = DOMXsltMeta.class; // for i18n purposes, needed by Translator2!!

  public void getFields(RowMetaInterface inputRowMeta, String name,
      RowMetaInterface[] info, StepMeta nextStep, VariableSpace space,
      Repository repository, IMetaStore metaStore) throws KettleStepException {
    // Output field (String)
    ValueMetaInterface v = new ValueMetaDom(
        space.environmentSubstitute(getResultfieldname()),
        ValueMetaDom.TYPE_DOM);
    v.setOrigin(name);
    inputRowMeta.addValueMeta(v);
  }

  public StepInterface getStep(StepMeta stepMeta,
      StepDataInterface stepDataInterface, int cnr, TransMeta transMeta,
      Trans trans) {
    return new DOMXslt(stepMeta, stepDataInterface, cnr, transMeta, trans);

  }

  public StepDataInterface getStepData() {
    return new DOMXsltData();
  }
}
