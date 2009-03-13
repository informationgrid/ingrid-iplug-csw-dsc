/*
 * Copyright (c) 2009 wemove digital solutions. All rights reserved.
 */

package de.ingrid.iplug.csw.dsc.cache;

import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;

import de.ingrid.iplug.csw.dsc.cswclient.CSWFactory;

public interface UpdateStrategy {

	/**
	 * Execute the update strategy.
	 * @param factory The CSWFactory instance
	 * @param cache The Cache instance (UpdateStrategy assumes that the Cache is an transaction already)
	 * @param filterStrSet A Set of ogc:Filter strings to query the server with
	 * @param recordsPerCall The maximum number of records to fetch with each server call
	 * @param requestPause The time between two requests in milliseconds
	 * @return The list of ids that exist on the server
	 * @throws Exception
	 */
	public abstract List<String> execute(CSWFactory factory, Cache cache, Set<String> filterStrSet,
			int recordsPerCall, int requestPause) throws Exception;

	/**
	 * Get the Cache instance.
	 * @return The cache
	 */
	public abstract Cache getCache();

	/**
	 * Get the Log instance.
	 * @return The log
	 */
	public abstract Log getLog();
}