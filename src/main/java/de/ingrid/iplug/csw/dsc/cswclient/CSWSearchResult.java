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

import java.util.List;

import org.w3c.dom.Document;

/**
 * Representation of a search result from a CSW server.
 * @author ingo herwig <ingo@wemove.com>
 */
public interface CSWSearchResult {

	/**
	 * Initialize the result.
	 * param factory
	 * param query
	 * param document
	 */
	public void initialize(CSWFactory factory, CSWQuery query, Document document) throws Exception;
	
	/**
	 * Get the associated query
	 * @return CSWQuery
	 */
	public CSWQuery getQuery();

	/**
	 * Get the original response document
	 * @return Document
	 */
	public Document getOriginalResponse();	
	
	/**
	 * Get the number of all records that matched the query
	 * @return int
	 */
	public int getNumberOfRecordsTotal();
	
	/**
	 * Get the number of records contained in this result
	 * @return int
	 */
	public int getNumberOfRecords();

	/**
	 * Get the record list
	 * @return List<CSWRecord>
	 */
	public List<CSWRecord> getRecordList();
}
