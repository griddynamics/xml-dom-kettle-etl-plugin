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

package org.pentaho.di.trans.steps.dom.concatfields;

import java.util.List;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaDom;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInjectionInterface;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.textfileoutput.TextFileField;
import org.pentaho.di.trans.steps.textfileoutput.TextFileOutputMeta;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

/*
 * ConcatFieldsMeta
 * @author jb
 * @since 2012-08-31
 *
 */
@Step(id = "DOMConcat", image = "fields-to-map.png", name = "Concat DOM", description = "Joins Document objects", categoryDescription = "Transform")
public class DOMConcatFieldsMeta extends BaseStepMeta implements StepMetaInterface {

	private static Class<?> PKG = DOMConcatFieldsMeta.class; // for i18n purposes,
															// needed by
															// Translator2!!

	// have a different namespace in repository in contrast to the
	// TextFileOutput
	private static final String ConcatFieldsNodeNameSpace = "ConcatDOM";

	private String targetFieldName; // the target field name
	private int targetFieldLength; // the length of the string field
	// private boolean removeSelectedFields; // remove the selected fields in
	// the output stream
	/** The output fields */
	private TextFileField[] outputFields;

	public TextFileField[] getOutputFields() {
		return outputFields;
	}

	public void setOutputFields(TextFileField[] outputFields) {
		this.outputFields = outputFields;
	}

	public String getTargetFieldName() {
		return targetFieldName;
	}

	public void setTargetFieldName(String targetField) {
		this.targetFieldName = targetField;
	}

	public int getTargetFieldLength() {
		return targetFieldLength;
	}

	public void setTargetFieldLength(int targetFieldLength) {
		this.targetFieldLength = targetFieldLength;
	}

	public DOMConcatFieldsMeta() {
		super(); // allocate TextFileOutputMeta
	}

	public void allocate(int nrfields) {
		outputFields = new TextFileField[nrfields];
	}

	@Override
	public void setDefault() {
	    int i, nrfields = 0;

	    allocate( nrfields );

	    for ( i = 0; i < nrfields; i++ ) {
	      outputFields[i] = new TextFileField();

	      outputFields[i].setName( "field" + i );
	      outputFields[i].setType( "Number" );
	      outputFields[i].setFormat( " 0,000,000.00;-0,000,000.00" );
	      outputFields[i].setCurrencySymbol( "" );
	      outputFields[i].setDecimalSymbol( "," );
	      outputFields[i].setGroupingSymbol( "." );
	      outputFields[i].setNullString( "" );
	      outputFields[i].setLength( -1 );
	      outputFields[i].setPrecision( -1 );
	    }
		targetFieldName = "";
		targetFieldLength = 0;
	}

	@Override
	public void loadXML(Node stepnode, List<DatabaseMeta> databases,
			IMetaStore metaStore) throws KettleXMLException {
		Node fields = XMLHandler.getSubNode(stepnode, "fields");
		int nrfields = XMLHandler.countNodes(fields, "field");

		allocate(nrfields);

		for (int i = 0; i < nrfields; i++) {
			Node fnode = XMLHandler.getSubNodeByNr(fields, "field", i);

			outputFields[i] = new TextFileField();
			outputFields[i].setName(XMLHandler.getTagValue(fnode, "name"));
			outputFields[i].setType(XMLHandler.getTagValue(fnode, "type"));
			outputFields[i].setFormat(XMLHandler.getTagValue(fnode, "format"));
			outputFields[i].setCurrencySymbol(XMLHandler.getTagValue(fnode,
					"currency"));
			outputFields[i].setDecimalSymbol(XMLHandler.getTagValue(fnode,
					"decimal"));
			outputFields[i].setGroupingSymbol(XMLHandler.getTagValue(fnode,
					"group"));
			outputFields[i].setTrimType(ValueMeta.getTrimTypeByCode(XMLHandler
					.getTagValue(fnode, "trim_type")));
			outputFields[i].setNullString(XMLHandler.getTagValue(fnode,
					"nullif"));
			outputFields[i].setLength(Const.toInt(
					XMLHandler.getTagValue(fnode, "length"), -1));
			outputFields[i].setPrecision(Const.toInt(
					XMLHandler.getTagValue(fnode, "precision"), -1));
		}
		targetFieldName = XMLHandler.getTagValue(stepnode,
				ConcatFieldsNodeNameSpace, "targetFieldName");
		targetFieldLength = Const.toInt(XMLHandler.getTagValue(stepnode,
				ConcatFieldsNodeNameSpace, "targetFieldLength"), 0);
	}

	@Override
	public String getXML() {

		StringBuffer retval = new StringBuffer(800);

		retval.append("    <fields>").append(Const.CR);
		for (int i = 0; i < outputFields.length; i++) {
			TextFileField field = outputFields[i];

			if (field.getName() != null && field.getName().length() != 0) {
				retval.append("      <field>").append(Const.CR);
				retval.append("        ").append(
						XMLHandler.addTagValue("name", field.getName()));
				retval.append("        ").append(
						XMLHandler.addTagValue("type", field.getTypeDesc()));
				retval.append("        ").append(
						XMLHandler.addTagValue("format", field.getFormat()));
				retval.append("        ").append(
						XMLHandler.addTagValue("currency",
								field.getCurrencySymbol()));
				retval.append("        ").append(
						XMLHandler.addTagValue("decimal",
								field.getDecimalSymbol()));
				retval.append("        ").append(
						XMLHandler.addTagValue("group",
								field.getGroupingSymbol()));
				retval.append("        ")
						.append(XMLHandler.addTagValue("nullif",
								field.getNullString()));
				retval.append("        ").append(
						XMLHandler.addTagValue("trim_type",
								field.getTrimTypeCode()));
				retval.append("        ").append(
						XMLHandler.addTagValue("length", field.getLength()));
				retval.append("        ").append(
						XMLHandler.addTagValue("precision",
								field.getPrecision()));
				retval.append("      </field>").append(Const.CR);
			}
		}
		retval.append("    </fields>").append(Const.CR);

		retval.append("    <").append(ConcatFieldsNodeNameSpace).append(">")
				.append(Const.CR);
		retval.append(XMLHandler
				.addTagValue("targetFieldName", targetFieldName));
		retval.append(XMLHandler.addTagValue("targetFieldLength",
				targetFieldLength));
		// retval = retval + XMLHandler.addTagValue( "removeSelectedFields",
		// removeSelectedFields );
		retval.append("    </" + ConcatFieldsNodeNameSpace + ">").append(
				Const.CR);
		return retval.toString();
	}

	@Override
	public void readRep(Repository rep, IMetaStore metaStore, ObjectId id_step,
			List<DatabaseMeta> databases) throws KettleException {
		

		int nrfields = rep.countNrStepAttributes(id_step, "field_name");

		allocate(nrfields);

		for (int i = 0; i < nrfields; i++) {
			outputFields[i] = new TextFileField();

			outputFields[i].setName(rep.getStepAttributeString(id_step, i,
					"field_name"));
			outputFields[i].setType(rep.getStepAttributeString(id_step, i,
					"field_type"));
			outputFields[i].setFormat(rep.getStepAttributeString(id_step, i,
					"field_format"));
			outputFields[i].setCurrencySymbol(rep.getStepAttributeString(
					id_step, i, "field_currency"));
			outputFields[i].setDecimalSymbol(rep.getStepAttributeString(
					id_step, i, "field_decimal"));
			outputFields[i].setGroupingSymbol(rep.getStepAttributeString(
					id_step, i, "field_group"));
			outputFields[i].setTrimType(ValueMeta.getTrimTypeByCode(rep
					.getStepAttributeString(id_step, i, "field_trim_type")));
			outputFields[i].setNullString(rep.getStepAttributeString(id_step,
					i, "field_nullif"));
			outputFields[i].setLength((int) rep.getStepAttributeInteger(
					id_step, i, "field_length"));
			outputFields[i].setPrecision((int) rep.getStepAttributeInteger(
					id_step, i, "field_precision"));
		}

		targetFieldName = rep.getStepAttributeString(id_step,
				ConcatFieldsNodeNameSpace + "targetFieldName");
		targetFieldLength = (int) rep.getStepAttributeInteger(id_step,
				ConcatFieldsNodeNameSpace + "targetFieldLength");
	}

	@Override
	public void saveRep(Repository rep, IMetaStore metaStore,
			ObjectId id_transformation, ObjectId id_step)
			throws KettleException {
		super.saveRep(rep, metaStore, id_transformation, id_step);
		rep.saveStepAttribute(id_transformation, id_step,
				ConcatFieldsNodeNameSpace + "targetFieldName", targetFieldName);
		rep.saveStepAttribute(id_transformation, id_step,
				ConcatFieldsNodeNameSpace + "targetFieldLength",
				targetFieldLength);

		for (int i = 0; i < outputFields.length; i++) {
			TextFileField field = outputFields[i];

			rep.saveStepAttribute(id_transformation, id_step, i, "field_name",
					field.getName());
			rep.saveStepAttribute(id_transformation, id_step, i, "field_type",
					field.getTypeDesc());
			rep.saveStepAttribute(id_transformation, id_step, i,
					"field_format", field.getFormat());
			rep.saveStepAttribute(id_transformation, id_step, i,
					"field_currency", field.getCurrencySymbol());
			rep.saveStepAttribute(id_transformation, id_step, i,
					"field_decimal", field.getDecimalSymbol());
			rep.saveStepAttribute(id_transformation, id_step, i, "field_group",
					field.getGroupingSymbol());
			rep.saveStepAttribute(id_transformation, id_step, i,
					"field_trim_type", field.getTrimTypeCode());
			rep.saveStepAttribute(id_transformation, id_step, i,
					"field_nullif", field.getNullString());
			rep.saveStepAttribute(id_transformation, id_step, i,
					"field_length", field.getLength());
			rep.saveStepAttribute(id_transformation, id_step, i,
					"field_precision", field.getPrecision());
		}
	}

	@Override
	public void getFields(RowMetaInterface row, String name,
			RowMetaInterface[] info, StepMeta nextStep, VariableSpace space,
			Repository repository, IMetaStore metaStore)
			throws KettleStepException {
		// do not call the super class from TextFileOutputMeta since it modifies
		// the source meta data
		// see getFieldsModifyInput() instead

		// Check Target Field Name
		if (Const.isEmpty(targetFieldName)) {
			throw new KettleStepException(BaseMessages.getString(PKG,
					"ConcatFieldsMeta.CheckResult.TargetFieldNameMissing"));
		}
		// add targetFieldName
		ValueMetaInterface vValue = new ValueMetaDom(targetFieldName,
				ValueMetaDom.TYPE_DOM, targetFieldLength, 0);
		vValue.setOrigin(name);

		row.addValueMeta(vValue);
	}

	@Override
	public void check(List<CheckResultInterface> remarks, TransMeta transMeta,
			StepMeta stepMeta, RowMetaInterface prev, String[] input,
			String[] output, RowMetaInterface info, VariableSpace space,
			Repository repository, IMetaStore metaStore) {
		CheckResult cr;

		// Check Target Field Name
		if (Const.isEmpty(targetFieldName)) {
			cr = new CheckResult(
					CheckResultInterface.TYPE_RESULT_ERROR,
					BaseMessages
							.getString(PKG,
									"ConcatFieldsMeta.CheckResult.TargetFieldNameMissing"),
					stepMeta);
			remarks.add(cr);
		}

		// Check output fields
		if (prev != null && prev.size() > 0) {
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK,
					BaseMessages.getString(PKG,
							"ConcatFieldsMeta.CheckResult.FieldsReceived", ""
									+ prev.size()), stepMeta);
			remarks.add(cr);

			String error_message = "";
			boolean error_found = false;

			// Starting from selected fields in ...
			for (int i = 0; i < getOutputFields().length; i++) {
				int idx = prev.indexOfValue(getOutputFields()[i].getName());
				if (idx < 0) {
					error_message += "\t\t" + getOutputFields()[i].getName()
							+ Const.CR;
					error_found = true;
				}
			}
			if (error_found) {
				error_message = BaseMessages.getString(PKG,
						"ConcatFieldsMeta.CheckResult.FieldsNotFound",
						error_message);
				cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR,
						error_message, stepMeta);
				remarks.add(cr);
			} else {
				cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK,
						BaseMessages.getString(PKG,
								"ConcatFieldsMeta.CheckResult.AllFieldsFound"),
						stepMeta);
				remarks.add(cr);
			}
		}

	}

	@Override
	public StepInterface getStep(StepMeta stepMeta,
			StepDataInterface stepDataInterface, int cnr, TransMeta transMeta,
			Trans trans) {
		return new DOMConcatFields(stepMeta, stepDataInterface, cnr, transMeta,
				trans);
	}

	@Override
	public StepDataInterface getStepData() {
		return new DOMConcatFieldsData();
	}

}
