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

import de.ingrid.iplug.csw.dsc.cache.ExecutionContext;
import de.ingrid.iplug.csw.dsc.cswclient.CSWClient;
import de.ingrid.iplug.csw.dsc.cswclient.CSWFactory;
import de.ingrid.iplug.csw.dsc.cswclient.constants.ElementSetName;

/**
 * The update job.
 * @author ingo herwig <ingo@wemove.com>
 */
public class DefaultUpdateStrategy extends AbstractUpdateStrategy {

	final protected static Log log = LogFactory.getLog(DefaultUpdateStrategy.class);
	
	protected ExecutionContext context = null;

	@Override
	public List<String> execute(ExecutionContext context) throws Exception {

		this.context = context;
		CSWFactory factory = context.getFactory();
		
		// prepare the filter set
		Set<Document> filterSet = new HashSet<Document>();
		for (String filterStr : context.getFilterStrSet()) {
			Document filterDoc = createFilterDocument(filterStr);
			filterSet.add(filterDoc);
		}
		
		// set up client
		CSWClient client = (CSWClient)factory.createClient();
		client.configure(factory);

		// fetch all BRIEF records to get the ids from the server
		if (log.isInfoEnabled())
			log.info("Fetching BRIEF records...");
		List<String> allRecordIds = fetchRecords(client, ElementSetName.FULL,
				filterSet, true);
		
		return allRecordIds;
	}

	@Override
	public ExecutionContext getExecutionContext() {
		return this.context;
	}

	@Override
	public Log getLog() {
		return log;
	}
}
