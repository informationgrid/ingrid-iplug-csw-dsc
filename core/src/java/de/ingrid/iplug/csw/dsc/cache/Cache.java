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
 *
 * Transaction support. A transaction allows to do all cache operations in a temporary 
 * store. The cache guarantees that changes do not affect the content of the cache from
 * which the transaction started until the transaction is committed. A rollback ends the 
 * transaction without changing the original content.
 * 
 * @author ingo herwig <ingo@wemove.com>
 */
public interface Cache {

	/**
	 * Get the ids all the records, that are cached.
	 * @return List
	 */
	public Set<String> getCachedRecordIds();

	/**
	 * Check if a record is cached.
	 * @param id
	 * @param elementSetName
	 * @return boolean
	 */
	public boolean isCached(String id, ElementSetName elementSetName) throws IOException;

	/**
	 * Get a record.
	 * @param id
	 * @param elementSetName
	 * @param record The record instance to fill
	 * @return CSWRecord
	 */
	public CSWRecord getRecord(String id, ElementSetName elementSetName, CSWRecord record) throws IOException;

	/**
	 * Store a record.
	 * @param record
	 */
	public void putRecord(CSWRecord record) throws IOException;

	/**
	 * Remove a record.
	 * @param id
	 */
	public void removeRecord(String id);

	/**
	 * Remove all records.
	 */
	public void removeAllRecords();
	
	/**
	 * Check wether the cache is in transaction mode.
	 * @param boolean
	 */
	public boolean isInTransaction();

	/**
	 * Start the transaction. The content of the returned cache is the same as the content
	 * of this cache initially. 
	 * @param Returns a new cache instance in transaction mode.
	 */
	public Cache startTransaction() throws IOException;

	/**
	 * Commit the transaction. Transfer all changes, that are done since the transaction was opened, 
	 * to the original content.
	 */
	public void commitTransaction() throws IOException;

	/**
	 * Rollback the transaction. Discard all changes, that are done since the transaction was opened.
	 */
	public void rollbackTransaction();
}
