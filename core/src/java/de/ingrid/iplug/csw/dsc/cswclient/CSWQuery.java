/*
 * Copyright (c) 2008 wemove digital solutions. All rights reserved.
 */

package de.ingrid.iplug.csw.dsc.cswclient;

import javax.xml.namespace.QName;

import org.w3c.dom.Document;

import de.ingrid.iplug.csw.dsc.cswclient.constants.ElementSetName;
import de.ingrid.iplug.csw.dsc.cswclient.constants.ResultType;
import de.ingrid.iplug.csw.dsc.cswclient.constants.TypeName;

/**
 * Representation of a CSW query. Encapsulates the varying parts of a query.
 * @see OpenGIS Catalogue Services Specification 2.0.2 - ISO Metadata Application Profile 8.2.2.1
 * @author ingo herwig <ingo@wemove.com>
 */
public interface CSWQuery {

	/**
	 * Set the output schema
	 * @param schema
	 */
	public void setOutputSchema(QName schema);

	/**
	 * Get the output schema
	 * @return QName
	 */
	public QName getOutputSchema();

	/**
	 * Get the typeNames for this query 
	 * @param typeName
	 */
	public void setTypeNames(TypeName typeName);

	/**
	 * Get the typeNames for this query 
	 * @return TypeName
	 */
	public TypeName getTypeNames();

	/**
	 * Set the result type for this query
	 * @param resultType 
	 */
	public void setResultType(ResultType resultType);

	/**
	 * Get the result type for this query
	 * @return ResultType 
	 */
	public ResultType getResultType();
	
	/**
	 * Set the element set name for this query
	 * @param elementSetName
	 */
	public void setElementSetName(ElementSetName elementSetName);

	/**
	 * Get the element set name for this query
	 * @return elementSetName
	 */
	public ElementSetName getElementSetName();

	/**
	 * Set the OGC filter
	 * @param filter
	 */
	public void setFilter(Document filter);

	/**
	 * Get the OGC filter
	 * @return Document
	 */
	public Document getFilter();
}
