/*
 * Copyright (c) 2008 wemove digital solutions. All rights reserved.
 */

package de.ingrid.iplug.csw.dsc.cache.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;

import de.ingrid.iplug.csw.dsc.cache.Cache;
import de.ingrid.iplug.csw.dsc.cswclient.CSWClient;
import de.ingrid.iplug.csw.dsc.cswclient.CSWFactory;
import de.ingrid.iplug.csw.dsc.cswclient.constants.ElementSetName;

/**
 * The update job.
 * @author ingo herwig <ingo@wemove.com>
 */
public class DefaultUpdateStrategy extends AbstractUpdateStrategy {

	final protected static Log log = LogFactory.getLog(DefaultUpdateStrategy.class);
	
	protected Cache cache = null;

	@Override
	public List<String> execute(CSWFactory factory, Cache cache, Set<String> filterStrSet,
			int recordsPerCall, int requestPause) throws Exception {

		this.cache = cache;
		
		// prepare the filter set
		Set<Document> filterSet = new HashSet<Document>();
		for (String filterStr : filterStrSet) {
			Document filterDoc = createFilterDocument(filterStr);
			filterSet.add(filterDoc);
		}
		
		// set up client
		CSWClient client = (CSWClient)factory.createClient();
		client.configure(factory);

		// fetch all BRIEF records to get the ids from the server
		if (log.isInfoEnabled())
			log.info("Fetching BRIEF records...");
		List<String> allRecordIds = fetchRecords(client, ElementSetName.BRIEF,
				filterSet, recordsPerCall, requestPause, true);

		// default update fetches all records in SUMMARY and FULL flavour
		List<String> recordIdsToUpdate = allRecordIds;
		
		// fetch the SUMMARY record for each id to update
		if (log.isInfoEnabled())
			log.info("Fetching SUMMARY records...");
		fetchRecords(client, ElementSetName.SUMMARY, recordIdsToUpdate, requestPause);

		// fetch the FULL record for each id to update
		if (log.isInfoEnabled())
			log.info("Fetching FULL records...");
		fetchRecords(client, ElementSetName.FULL, recordIdsToUpdate, requestPause);
		
		return allRecordIds;
	}

	@Override
	public Cache getCache() {
		return this.cache;
	}

	@Override
	public Log getLog() {
		return log;
	}
}
