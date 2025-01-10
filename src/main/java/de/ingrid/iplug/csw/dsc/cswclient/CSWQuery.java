/*
 * **************************************************-
 * ingrid-iplug-csw-dsc:war
 * ==================================================
 * Copyright (C) 2014 - 2025 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.2 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
/*
 * Copyright (c) 2008 wemove digital solutions. All rights reserved.
 */

package de.ingrid.iplug.csw.dsc.cswclient;

import org.w3c.dom.Document;

import de.ingrid.iplug.csw.dsc.cswclient.constants.ConstraintLanguage;
import de.ingrid.iplug.csw.dsc.cswclient.constants.ElementSetName;
import de.ingrid.iplug.csw.dsc.cswclient.constants.Namespace;
import de.ingrid.iplug.csw.dsc.cswclient.constants.OutputFormat;
import de.ingrid.iplug.csw.dsc.cswclient.constants.ResultType;
import de.ingrid.iplug.csw.dsc.cswclient.constants.TypeName;

/**
 * Representation of a CSW query. Encapsulates the varying parts of a query.
 * Instances may be used for the GetRecords or the GetRecordById request, where the
 * first would reuqire a constraint to be set, while the latter requires an id to be
 * set. Properties that are not needed for the specific request are ignored.
 * @see OpenGIS Catalogue Services Specification 2.0.2 - ISO Metadata Application Profile 8.2.2.1 and
 * OpenGIS Catalogue Services Specification 2.0.2 - ISO Metadata Application Profile 8.2.2.2
 * @author ingo herwig <ingo@wemove.com>
 */
public interface CSWQuery {

	/**
	 * Set the query schema
	 * @param schema
	 */
	public void setSchema(Namespace schema);

	/**
	 * Get the query schema
	 * @return Namespace
	 */
	public Namespace getSchema();

	/**
	 * Set the output schema
	 * @param schema
	 */
	public void setOutputSchema(Namespace schema);

	/**
	 * Get the output schema
	 * @return Namespace
	 */
	public Namespace getOutputSchema();

	/**
	 * Set the output format
	 * @param schema
	 */
	public void setOutputFormat(OutputFormat format);

	/**
	 * Get the output format
	 * @return OutputFormat
	 */
	public OutputFormat getOutputFormat();

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
	 * Set the typeNames for this query 
	 * @param typeName
	 */
	public void setTypeNames(TypeName[] typeNames);

	/**
	 * Get the typeNames for this query 
	 * @return TypeName[]
	 */
	public TypeName[] getTypeNames();

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
	 * Set the constraint language
	 * @param language
	 */
	public void setConstraintLanguage(ConstraintLanguage language);

	/**
	 * Get the constraint language
	 * @return ConstraintLanguage
	 */
	public ConstraintLanguage getConstraintLanguage();

	/**
	 * Set the constraint language version
	 * @param version
	 */
	public void setConstraintLanguageVersion(String version);

	/**
	 * Get the constraint language version
	 * @return String
	 */
	public String getConstraintLanguageVersion();

	/**
	 * Set the OGC filter (child element of Constraint node)
	 * @param filter
	 */
	public void setConstraint(Document constraint);

	/**
	 * Get the OGC filter (child element of Constraint node)
	 * @return Document
	 */
	public Document getConstraint();

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

	/**
	 * Set the id of the record to retrieve
	 * @param id
	 */
	public void setId(String id);

	/**
	 * Get the id of the record to retrieve
	 * @return String
	 */
	public String getId();
}
