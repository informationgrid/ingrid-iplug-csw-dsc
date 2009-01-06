/*
 * Copyright (c) 2008 wemove digital solutions. All rights reserved.
 */

package de.ingrid.iplug.csw.dsc.cache;

import java.io.IOException;
import java.util.Set;

import de.ingrid.iplug.csw.dsc.cswclient.CSWRecord;
import de.ingrid.iplug.csw.dsc.cswclient.constants.ElementSetName;

/**
 * Cache interface.
 * @author ingo herwig <ingo@wemove.com>
 */
public interface Cache {

	/**
	 * Get the ids all the records, that are cached
	 * @return List
	 */
	public Set<String> getCachedRecordIds();

	/**
	 * Check if a record is cached
	 * @param id
	 * @param elementSetName
	 * @return boolean
	 */
	public boolean isCached(String id, ElementSetName elementSetName) throws IOException;

	/**
	 * Store a record
	 * @param record
	 */
	public void putRecord(CSWRecord record) throws IOException;

	/**
	 * Get a record
	 * @param id
	 * @param elementSetName
	 * @param record The record instance to fill
	 * @return CSWRecord
	 */
	public CSWRecord getRecord(String id, ElementSetName elementSetName, CSWRecord record) throws IOException;

	/**
	 * Remove a record
	 * @param id
	 */
	public void removeRecord(String id);

	/**
	 * Remove all records
	 */
	public void removeAllRecords();
}
