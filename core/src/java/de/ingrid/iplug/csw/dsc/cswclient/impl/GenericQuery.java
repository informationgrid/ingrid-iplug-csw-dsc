/*
 * Copyright (c) 2008 wemove digital solutions. All rights reserved.
 */

package de.ingrid.iplug.csw.dsc.cswclient.impl;

import javax.xml.namespace.QName;

import org.w3c.dom.Document;

import de.ingrid.iplug.csw.dsc.cswclient.CSWQuery;
import de.ingrid.iplug.csw.dsc.cswclient.constants.ElementSetName;
import de.ingrid.iplug.csw.dsc.cswclient.constants.ResultType;
import de.ingrid.iplug.csw.dsc.cswclient.constants.TypeName;

public class GenericQuery implements CSWQuery {

	protected QName schema = null;
	protected QName outputSchema = null;
	protected String outputFormat = null;
	protected String version = null;
	protected ElementSetName elementSetName = null;
	protected TypeName typeName = null;
	protected ResultType resultType = null;
	protected String constraintVersion = null;
	protected Document filter = null;
	protected int startPosition = 1;
	protected int maxRecords = 0;

    public GenericQuery() {
    }

    public GenericQuery(String schema, String outputSchema, String outputFormat, String version, 
    		String elementSetName, String typeName, String resultType, String constraintVersion) {
    	System.out.println("construct generic query");
    }

	@Override
	public void setSchema(QName schema) {
		this.schema = schema;
	}

	@Override
	public QName getSchema() {
		return this.schema; 
	}

	@Override
	public void setOutputSchema(QName schema) {
		this.outputSchema = schema;
	}

	@Override
	public QName getOutputSchema() {
		return this.outputSchema;
	}

	@Override
	public void setOutputFormat(String format) {
		this.outputFormat = format;
	}

	@Override
	public String getOutputFormat() {
		return this.outputFormat;
	}

	@Override
	public void setVersion(String version) {
		this.version = version;
	}

	@Override
	public String getVersion() {
		return this.version;
	}

	@Override
	public void setTypeNames(TypeName typeName) {
		this.typeName = typeName;
	}

	@Override
	public TypeName getTypeNames() {
		return this.typeName;
	}

	@Override
	public void setResultType(ResultType resultType) {
		this.resultType  = resultType;
	}

	@Override
	public ResultType getResultType() {
		return this.resultType;
	}

	@Override
	public void setElementSetName(ElementSetName elementSetName) {
		this.elementSetName = elementSetName;
	}

	@Override
	public ElementSetName getElementSetName() {
		return elementSetName;
	}

	@Override
	public void setConstraintVersion(String version) {
		this.constraintVersion = version;
	}

	@Override
	public String getConstraintVersion() {
		return this.constraintVersion;
	}

	@Override
	public void setFilter(Document filter) {
		this.filter = filter;
	}

	@Override
	public Document getFilter() {
		return this.filter ;
	}

	@Override
	public void setStartPosition(int position) {
		this.startPosition = position;
	}

	@Override
	public int getStartPosition() {
		return this.startPosition;
	}

	@Override
	public void setMaxRecords(int max) {
		this.maxRecords = max;
	}

	@Override
	public int getMaxRecords() {
		return this.maxRecords;
	}
}