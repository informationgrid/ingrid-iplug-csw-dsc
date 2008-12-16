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

public abstract class AbstractQuery implements CSWQuery {

	protected QName outputSchema = null;
	protected ElementSetName elementSetName = null;
	protected TypeName typeName = null;
	protected ResultType resultType = null;
	protected Document filter = null;

	public AbstractQuery() {
		super();
	}

	@Override
	public QName getOutputSchema() {
		return this.outputSchema;
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
	public void setFilter(Document filter) {
		this.filter = filter;
	}

	@Override
	public Document getFilter() {
		return this.filter ;
	}

}