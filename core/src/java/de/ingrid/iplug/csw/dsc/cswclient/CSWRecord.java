/*
 * Copyright (c) 2008 wemove digital solutions. All rights reserved.
 */

package de.ingrid.iplug.csw.dsc.cswclient;

import org.w3c.dom.Document;

/**
 * Representation of a record returned by a CSW server.
 * @author ingo herwig <ingo@wemove.com>
 */
public interface CSWRecord {

	/**
	 * Set the query and original response document
	 * param query
	 * param document
	 */
	public void configure(CSWQuery query, Document document) throws Exception;
	
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
	 * Set the id of the record
	 * @param id
	 */
	public void setId(String id);

	/**
	 * Get the id of the record
	 * @return id
	 */
	public String getId();
}
