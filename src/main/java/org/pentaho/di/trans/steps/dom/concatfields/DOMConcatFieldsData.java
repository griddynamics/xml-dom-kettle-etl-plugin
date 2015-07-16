package org.pentaho.di.trans.steps.dom.concatfields;

import javax.xml.parsers.DocumentBuilder;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;
import org.w3c.dom.Node;

public class DOMConcatFieldsData extends BaseStepData implements StepDataInterface {
	public RowMetaInterface outputRowMeta;
	public DocumentBuilder documentBuilder;
	public int[] fieldnrs;
	public Node documentRoot;
	public RowMetaInterface inputRowMetaModified; 
}
