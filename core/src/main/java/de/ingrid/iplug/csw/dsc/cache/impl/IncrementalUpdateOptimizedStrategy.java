/*
 * Copyright (c) 2008 wemove digital solutions. All rights reserved.
 */

package de.ingrid.iplug.csw.dsc.cache.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
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
import de.ingrid.iplug.csw.dsc.tools.XPathUtils;
import de.ingrid.utils.udk.UtilsCSWDate;
import de.ingrid.utils.udk.UtilsDate;

/**
 * The optimized incremental update job. It differs from the incremental 
 * update job in getting the modified records based on the modification date
 * in the initially fetched BRIEF records. Therefore it does not need to fetch 
 * modified brief records twice.
 * 
 * @author joachim mueller <joachim@wemove.com>
 */
public class IncrementalUpdateOptimizedStrategy extends AbstractUpdateStrategy {

	final protected static Log log = LogFactory.getLog(IncrementalUpdateOptimizedStrategy.class);
	
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

		// fetch all BRIEF records to get all record ids from the server
		if (log.isInfoEnabled())
			log.info("Fetching BRIEF records...");
		List<String> allRecordIds = fetchRecords(client, ElementSetName.BRIEF,
				filterSet, true);

		// detect the modified BRIEF records
		if (log.isInfoEnabled())
			log.info("Detect modified BRIEF records...");
		List<String> modifiedRecordIds = detectModifiedRecords(allRecordIds);
		
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
		
		Cache initialCache = cache.getInitialCache();
		// check if the record exists in the current worker cache
		// if yes, do nothing
		if (cache.isCached(recordId, elementSetName)) {
			if (log.isInfoEnabled()) {
				log.info("Record is already in worker cache: "+recordId+" "+elementSetName);
			}
		// check if the record exists in the initial cache already
		// if yes, take it from there
		} else if (initialCache.isCached(recordId, elementSetName)) {
			record = initialCache.getRecord(recordId, elementSetName);
			if (log.isInfoEnabled()) {
				log.info("Reused record from initial cache: "+recordId+" "+elementSetName);
			}
		// if not, fetch it from the server
		} else {
			CSWFactory factory = client.getFactory();
			CSWQuery query = factory.createQuery();
			query.setElementSetName(elementSetName);
			query.setId(recordId);
			record = client.getRecordById(query);
			if (log.isInfoEnabled()) {
				log.info("Fetched record (was not cached): "+recordId+" "+elementSetName);
			}
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
	
	/**
	 * Return all modified records compared to the last execution date
	 * from context.
	 * 
	 * @param allRecordIds
	 * @return
	 * @throws IOException
	 */
	protected List<String> detectModifiedRecords(List<String> allRecordIds) throws IOException {
		CSWRecord record = null;
		Cache cache = this.context.getCache();
		List<String> resultList = new ArrayList<String>();
		
		for (String recordId : allRecordIds) {
			record = cache.getRecord(recordId, ElementSetName.BRIEF);
			// get the record modified date
			NodeList idNodes = XPathUtils.getNodeList(record.getOriginalResponse(), "//*/dateStamp");
			if (idNodes == null || idNodes.item(0) == null) {
				log.debug("CSWRecord '" + recordId + "' does not contain a dateStamp/Date element. Record marked for refetching.");
				resultList.add(recordId);
			} else if (idNodes.getLength() > 1) {
				log.debug("CSWRecord '" + recordId + "' contains more than one dateStamp/Date element. Record marked for refetching.");
				resultList.add(recordId);
			} else {
				Node dateStampNode = idNodes.item(0);
				String modifiedDateString = XPathUtils.getString(dateStampNode, "Date");
				if (modifiedDateString == null || modifiedDateString.length() == 0) {
					modifiedDateString = XPathUtils.getString(dateStampNode, "DateTime");
				}
				// make sure we have a date
				if (modifiedDateString != null && modifiedDateString.length() > 3) {
					String datePattern = UtilsCSWDate.getDatePattern(modifiedDateString);
					if (datePattern == null) {
						log.info("Unrecognized date pattern for date '" + modifiedDateString + "'. Record '" + recordId + "' marked for refetching.");
					} else {
						Date modifiedDate = UtilsDate.parseDateString(UtilsDate.convertDateString(modifiedDateString, UtilsCSWDate.getDatePattern(modifiedDateString), "yyyyMMddHHmmssSSS"));
						if (modifiedDate.after(this.context.getLastExecutionDate())) {
							log.debug("CSWRecord '" + recordId + "' has been modified since last retrieval. Record marked for refetching.");
							resultList.add(recordId);
						} else {
							log.debug("CSWRecord '" + recordId + "' has not been modified since last retrieval, skip.");
						}
					}
				} else {
					log.debug("Could not select Date or DateTime element below existing dateStamp element. Record '" + recordId + "' marked for refetching.");
					resultList.add(recordId);
				}
			}
		}
		return resultList;
	}
}
