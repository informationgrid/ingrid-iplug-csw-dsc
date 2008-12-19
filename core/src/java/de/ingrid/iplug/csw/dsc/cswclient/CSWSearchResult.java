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
	 * Configure
	 * param factory
	 * param query
	 * param document
	 */
	public void configure(CSWClientFactory factory, CSWQuery query, Document document) throws Exception;
	
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
