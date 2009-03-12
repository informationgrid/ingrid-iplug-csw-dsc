/*
 * Copyright (c) 2008 wemove digital solutions. All rights reserved.
 */

package de.ingrid.iplug.csw.dsc.cache;

import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import de.ingrid.iplug.csw.dsc.cswclient.CSWClient;
import de.ingrid.iplug.csw.dsc.cswclient.CSWFactory;
import de.ingrid.iplug.csw.dsc.cswclient.CSWQuery;
import de.ingrid.iplug.csw.dsc.cswclient.CSWRecord;
import de.ingrid.iplug.csw.dsc.cswclient.CSWSearchResult;
import de.ingrid.iplug.csw.dsc.cswclient.constants.ElementSetName;
import de.ingrid.iplug.csw.dsc.cswclient.constants.ResultType;
import de.ingrid.iplug.csw.dsc.tools.StringUtils;

/**
 * The update job.
 * @author ingo herwig <ingo@wemove.com>
 */
public class UpdateJob {

	final protected static Log log = LogFactory.getLog(UpdateJob.class);
	
	protected CSWFactory factory = null;	
	protected Cache cache = null;
	protected Set<Document> filterSet = null;
	protected Set<Document> filterSetModifiedOnly = null;
	protected boolean isIncrementalUpdate;

	/**
	 * Configure the job
	 * @param factory
	 * @param cache
	 * @param filterStrSet A Set of ogc:Filter strings to query the server with
	 * @param incrementalFilterAddition A ogc:Filter string that describes the condition,
	 * that records for the incremental update must satisfy (maybe null, which means no incremental update)
	 * @throws Exception 
	 */
	public void configure(CSWFactory factory, Cache cache, Set<String> filterStrSet, 
			String incrementalFilterAddition) throws Exception {
		this.factory = factory;
		this.cache = cache;
		this.isIncrementalUpdate = incrementalFilterAddition != null;
		
		// create the filter documents
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();

		// create the incremental filter addition if requested
		Document incrementalFilterAdditionDoc = null;
		if (isIncrementalUpdate) {
			this.filterSetModifiedOnly = new HashSet<Document>();
			incrementalFilterAddition = processVariables(incrementalFilterAddition);
			incrementalFilterAdditionDoc = docBuilder.parse(new InputSource(new StringReader(incrementalFilterAddition)));
			if (log.isInfoEnabled())
				log.info("Doing an incremental update with additional filter condition: "+
						StringUtils.nodeToString(incrementalFilterAdditionDoc).replace("\n", ""));			
		}

		// prepare the filter set
		this.filterSet = new HashSet<Document>();
		for (String filterStr : filterStrSet) {
			filterStr = processVariables(filterStr);
			Document filterDoc = docBuilder.parse(new InputSource(new StringReader(filterStr)));
			this.filterSet.add(filterDoc);
			
			// if we do a incremental update, we also create filters that take the modified
			// date into account
			if (isIncrementalUpdate) {
				Document incrementalUpdateFilterDoc = addIncrementalUpdateCondition(filterDoc,
						incrementalFilterAdditionDoc);
				filterSetModifiedOnly.add(incrementalUpdateFilterDoc);
			}
		}
	}
		
	/**
	 * Execute the update job.
	 * @param recordsPerCall
	 * @param requestPause The time between two requests in milliseconds
	 * @throws Exception
	 */
	public void execute(int recordsPerCall, int requestPause) throws Exception {
		
		// get cached record ids (for later removal of records that do not exist anymore)
		Set<String> cachedRecordIds = this.cache.getCachedRecordIds();

		// set up client
		CSWClient client = (CSWClient)this.factory.createClient();
		client.configure(this.factory);

		// fetch all BRIEF records to get the ids from the server
		if (log.isInfoEnabled())
			log.info("Fetching BRIEF records...");
		List<String> allRecordIds = fetchRecords(client, ElementSetName.BRIEF,
				this.filterSet, recordsPerCall, requestPause);

		// default update fetches all records in SUMMARY and FULL flavour
		List<String> recordIdsToUpdate = allRecordIds;
		
		// when doing an incremental update, we only fetch the modified records
		// in SUMMARY and FULL flavour (decision is based on incrementalFilterAddition)
		if (this.isIncrementalUpdate) {
			
			// clean the cache first
			this.cache.removeAllRecords();
			
			if (log.isInfoEnabled())
				log.info("Fetching modified BRIEF records...");
			List<String> modifiedRecordIds = fetchRecords(client, ElementSetName.BRIEF,
					this.filterSetModifiedOnly, recordsPerCall, requestPause);
			recordIdsToUpdate = modifiedRecordIds;			
		}
		
		// fetch the SUMMARY record for each id to update
		if (log.isInfoEnabled())
			log.info("Fetching SUMMARY records...");
		fetchRecords(client, ElementSetName.SUMMARY, recordIdsToUpdate);

		// fetch the FULL record for each id to update
		if (log.isInfoEnabled())
			log.info("Fetching FULL records...");
		fetchRecords(client, ElementSetName.FULL, recordIdsToUpdate);
		
		// when doing an incremental update, we copy the un modified records from
		// the initial cache to our cache
		if (this.isIncrementalUpdate) {
			Cache initialCache = this.cache.getInitialCache();
			for (String recordId : allRecordIds) {
				if (!recordIdsToUpdate.contains(recordId)) {
					if (initialCache.isCached(recordId, ElementSetName.BRIEF))
						this.cache.putRecord(initialCache.getRecord(recordId, ElementSetName.BRIEF));
					if (initialCache.isCached(recordId, ElementSetName.SUMMARY))
						this.cache.putRecord(initialCache.getRecord(recordId, ElementSetName.SUMMARY));
					if (initialCache.isCached(recordId, ElementSetName.FULL))
						this.cache.putRecord(initialCache.getRecord(recordId, ElementSetName.FULL));
				}
			}
		}
		
		// remove deprecated records
		for (String cachedRecordId : cachedRecordIds) {
			if (!allRecordIds.contains(cachedRecordId))
				this.cache.removeRecord(cachedRecordId);
		}

		// summary
		// duplicates are filtered out automatically by the cache, so there is no need for action here
		int duplicates = allRecordIds.size() - new HashSet<String>(allRecordIds).size();
		log.info("Fetched "+allRecordIds.size()+" records of "+allRecordIds.size()+". Duplicates: "+duplicates);
	}
	
	/**
	 * Fetch all records that satisfy the given filter using the GetRecords and return the ids
	 * and put them into the cache
	 * @param client The CSWClient to use
	 * @param elementSetName The ElementSetName of the records to fetch
	 * @param filterSet The filter set used to select the records 
	 * @param recordsPerCall Number of records to fetch per call
	 * @param requestPause The time between two requests in milliseconds 
	 * @return A list of ids of the fetched records
	 * @throws Exception
	 */
	protected List<String> fetchRecords(CSWClient client, ElementSetName elementSetName,
			Set<Document> filterSet, int recordsPerCall, int requestPause) throws Exception {
		
		// variables for complete fetch process
		int numTotal = 0;
		List<String> fetchedRecordIds = new ArrayList<String>();
		
		// iterate over all filters
		int filterIndex = 1;
		for (Document filter : filterSet) {
			if (log.isDebugEnabled())
				log.debug("Processing filter "+filterIndex+": "+StringUtils.nodeToString(filter).replace("\n", "")+".");
			
			// variables for current fetch process (current filter)
			int numCurrentTotal = 0;
			List<String> currentFetchedRecordIds = new ArrayList<String>();

			// create the query
			CSWQuery query = this.factory.createQuery();
			query.setConstraint(filter);
			query.setResultType(ResultType.RESULTS);
			query.setElementSetName(elementSetName);
			query.setMaxRecords(recordsPerCall);
			query.setStartPosition(1);
			
			// do requests
			
			// do first request
			CSWSearchResult result = client.getRecords(query);
			numCurrentTotal = result.getNumberOfRecordsTotal();
			if (log.isInfoEnabled())
				log.info(numCurrentTotal+" record(s) from filter "+filterIndex+":");
			
			if (numCurrentTotal > 0) {
				
				// process
				currentFetchedRecordIds.addAll(processResult(result));	
				
				while (result.getNumberOfRecords() > 0 && currentFetchedRecordIds.size() < numCurrentTotal) {
					Thread.sleep(requestPause);
	
					// prepare next request
					query.setStartPosition(query.getStartPosition()+result.getNumberOfRecords());
	
					// do next request
					result = client.getRecords(query);
	
					// process
					currentFetchedRecordIds.addAll(processResult(result));			
				}
			}
			
			// collect record ids
			fetchedRecordIds.addAll(currentFetchedRecordIds);
			numTotal += numCurrentTotal;
			filterIndex++;
		}
		return fetchedRecordIds;
	}
	
	/**
	 * Fetch all records from a id list using the GetRecordById and put them in the cache
	 * @param client The CSWClient to use
	 * @param elementSetName The ElementSetName of the records to fetch
	 * @param recordIds The list of ids 
	 * @throws Exception
	 */
	protected void fetchRecords(CSWClient client, ElementSetName elementSetName,
			List<String> recordIds) throws Exception {

		CSWQuery query = this.factory.createQuery();
		query.setElementSetName(elementSetName);

		CSWRecord record = null;
		for (String id : recordIds) {
			query.setId(id);
			record = client.getRecordById(query);
			if (log.isInfoEnabled())
				log.info("Record: "+id+" "+record.getElementSetName());
			this.cache.putRecord(record);
		}		
	}
		
	/**
	 * Process a fetched search result (collect ids and cache records)
	 * @param result The search result
	 * @return The list of ids of the fetched records
	 * @throws Exception
	 */
	protected List<String> processResult(CSWSearchResult result) throws Exception {
		List<String> fetchedRecordIds = new ArrayList<String>();
		for (CSWRecord record : result.getRecordList()) {
			String id = record.getId();
			if (log.isInfoEnabled())
				log.info("Record: "+id+" "+record.getElementSetName());
			if (fetchedRecordIds.contains(id)) {
				log.warn("Duplicated id: "+id+". Overriding previous entry.");
			}
			this.cache.putRecord(record);
			fetchedRecordIds.add(id);
		}
		if (log.isInfoEnabled())
			log.info("Fetched "+fetchedRecordIds.size()+" of "+result.getNumberOfRecordsTotal()+
					" [starting from "+result.getQuery().getStartPosition()+"]");
		return fetchedRecordIds;
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
	 * Replace any filter variables.
	 * TODO: if there should be more variables, this could be done more generic 
	 * @param filterStr
	 * @return String
	 */
	protected String processVariables(String filterStr) {
		// replace last update date
		Pattern lastUpdateDatePattern = Pattern.compile("LAST_UPDATE_DATE", Pattern.MULTILINE);
		Matcher matcher = lastUpdateDatePattern.matcher(filterStr);
		if (matcher.find()) {
			Date lastUpdateDate = this.cache.getLastCommitDate();
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
			filterStr = matcher.replaceAll(df.format(lastUpdateDate));
		}
		return filterStr;
	}
}
