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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.ingrid.iplug.csw.dsc.cache.Cache;
import de.ingrid.iplug.csw.dsc.cache.ExecutionContext;
import de.ingrid.iplug.csw.dsc.cswclient.CSWClient;
import de.ingrid.iplug.csw.dsc.cswclient.CSWFactory;
import de.ingrid.iplug.csw.dsc.cswclient.CSWQuery;
import de.ingrid.iplug.csw.dsc.cswclient.CSWRecord;
import de.ingrid.iplug.csw.dsc.cswclient.constants.ElementSetName;
import de.ingrid.iplug.csw.dsc.tools.StringUtils;

/**
 * The update job.
 * @author ingo herwig <ingo@wemove.com>
 */
public class IncrementalUpdateStrategy extends AbstractUpdateStrategy {

	final protected static Log log = LogFactory.getLog(IncrementalUpdateStrategy.class);
	
	protected ExecutionContext context = null;
	private String incrementalFilterAddition;

	/**
	 * Constructor
	 * @param incrementalFilterAddition
	 */
	public IncrementalUpdateStrategy(String incrementalFilterAddition) {
		this.incrementalFilterAddition = incrementalFilterAddition;
	}

	@Override
	public List<String> execute(ExecutionContext context) throws Exception {

		this.context = context;
		CSWFactory factory = context.getFactory();
		
		// create the incremental filter addition document
		Set<Document> filterSetModifiedOnly = new HashSet<Document>();
		Document incrementalFilterAdditionDoc = createFilterDocument(this.incrementalFilterAddition);
		if (log.isInfoEnabled())
			log.info("Doing an incremental update with additional filter condition: "+
					StringUtils.nodeToString(incrementalFilterAdditionDoc).replace("\n", ""));			

		// prepare the filter set
		Set<Document> filterSet = new HashSet<Document>();
		for (String filterStr : context.getFilterStrSet()) {

			Document filterDoc = createFilterDocument(filterStr);
			filterSet.add(filterDoc);
			
			Document incrementalUpdateFilterDoc = addIncrementalUpdateCondition(filterDoc, incrementalFilterAdditionDoc);
			filterSetModifiedOnly.add(incrementalUpdateFilterDoc);
		}
		
		// set up client
		CSWClient client = (CSWClient)factory.createClient();
		client.configure(factory);

		// fetch all BRIEF records to get all record ids from the server
		if (log.isInfoEnabled())
			log.info("Fetching BRIEF records...");
		List<String> allRecordIds = fetchRecords(client, ElementSetName.BRIEF,
				filterSet, false);

		// fetch the modified BRIEF records
		if (log.isInfoEnabled())
			log.info("Fetching modified BRIEF records...");
		List<String> modifiedRecordIds = fetchRecords(client, ElementSetName.BRIEF,
				filterSetModifiedOnly, true);
		
		// incremental update only fetches modified records in SUMMARY and FULL flavour
		List<String> recordIdsToUpdate = modifiedRecordIds;			
		
		// fetch the SUMMARY record for each id to update
		if (log.isInfoEnabled())
			log.info("Fetching SUMMARY records...");
		fetchRecords(client, ElementSetName.SUMMARY, recordIdsToUpdate, requestPause);

		// fetch the FULL record for each id to update
		if (log.isInfoEnabled())
			log.info("Fetching FULL records...");
		fetchRecords(client, ElementSetName.FULL, recordIdsToUpdate, requestPause);
		
		// copy the unmodified records from the initial cache to our cache
		for (String recordId : allRecordIds) {
			if (!recordIdsToUpdate.contains(recordId)) {
				reuseOrFetchRecord(client, ElementSetName.BRIEF, recordId);
				reuseOrFetchRecord(client, ElementSetName.SUMMARY, recordId);
				reuseOrFetchRecord(client, ElementSetName.FULL, recordId);
			}
		}
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

	/**
	 * Check if a record exists in the initial cache already. Reuse it, if yes
	 * and fetch it from the server, if not.
	 * @param client The CSWClient to use
	 * @param elementSetName The ElementSetName of the records to fetch
	 * @param recordId The id
	 * @throws Exception
	 */
	protected void reuseOrFetchRecord(CSWClient client, ElementSetName elementSetName,
			String recordId) throws Exception {

		CSWRecord record = null;
		Cache cache = this.context.getCache();
		
		// check if the record exists in the initial cache already
		// if yes, take it from there
		Cache initialCache = cache.getInitialCache();
		if (initialCache.isCached(recordId, elementSetName)) {
			record = initialCache.getRecord(recordId, elementSetName);
			if (log.isInfoEnabled())
				log.info("Reused record from cache: "+recordId+" "+record.getElementSetName());
		}
		// if not, fetch it from the server
		else {
			CSWFactory factory = client.getFactory();
			CSWQuery query = factory.createQuery();
			query.setElementSetName(elementSetName);
			query.setId(recordId);
			record = client.getRecordById(query);
			if (log.isInfoEnabled())
				log.info("Fetched record (was not cached): "+recordId+" "+record.getElementSetName());
		}
		
		// store the record in the cache
		if (record != null)
			cache.putRecord(record);
	}
		
	/**
	 * Create a new pgc filter document from the given filterDoc with the condition concatenated by AND 
	 * @param filterDoc
	 * @param condition
	 * @return Document
	 * @throws Exception
	 */
	protected Document addIncrementalUpdateCondition(Document filterDoc, Document condition) throws Exception {
		// create the new filter as a copy of the original one
		Document newFilterDoc = StringUtils.stringToDocument(StringUtils.nodeToString(filterDoc));

		// get the original filter
		Node filterNode = newFilterDoc.getFirstChild();
		NodeList originalFilterNodes = filterNode.getChildNodes();
		
		// create the And node and attach the original filter and the additional condition 
		Node andNode = newFilterDoc.createElement("And");
		for(int i=0; i<originalFilterNodes.getLength(); i++)
			andNode.insertBefore(originalFilterNodes.item(0), null);
		Node incrementalFilterAdditionNode = newFilterDoc.importNode(condition.getDocumentElement(), true);
		andNode.appendChild(incrementalFilterAdditionNode);
		
		// attach everything back to the filter
		filterNode.appendChild(andNode);

		return newFilterDoc;
	}
}
