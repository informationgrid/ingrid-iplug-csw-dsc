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
	 * Set the query schema
	 * @param schema
	 */
	public void setSchema(QName schema);

	/**
	 * Get the query schema
	 * @return QName
	 */
	public QName getSchema();

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
	 * Set the output format
	 * @param schema
	 */
	public void setOutputFormat(String format);

	/**
	 * Get the output format
	 * @return String
	 */
	public String getOutputFormat();

	/**
	 * Set the version
	 * @param version
	 */
	public void setVersion(String version);

	/**
	 * Get the version
	 * @return String
	 */
	public String getVersion();

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
	 * Set the constraint version
	 * @param version
	 */
	public void setConstraintVersion(String version);

	/**
	 * Get the constraint version
	 * @return String
	 */
	public String getConstraintVersion();

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

	/**
	 * Set the start position
	 * @param position
	 */
	public void setStartPosition(int position);

	/**
	 * Get the start position
	 * @return int
	 */
	public int getStartPosition();

	/**
	 * Set the maximum number of records
	 * @param max
	 */
	public void setMaxRecords(int max);

	/**
	 * Get the maximum number of records
	 * @return int
	 */
	public int getMaxRecords();
}
