/*
 * Copyright (c) 2008 wemove digital solutions. All rights reserved.
 */

package de.ingrid.iplug.csw.dsc.cache;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
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

	/**
	 * Configure the job
	 * @param factory
	 * @param cache
	 * @param filterStrSet A Set of ogc:Filter strings to query the server with
	 * @throws Exception 
	 */
	public void configure(CSWFactory factory, Cache cache, Set<String> filterStrSet) throws Exception {
		this.factory = factory;
		this.cache = cache;
		
		// create the filter document
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
		this.filterSet = new HashSet<Document>();
		for (String filterStr : filterStrSet)
			this.filterSet.add(docBuilder.parse(new InputSource(new StringReader(filterStr))));
	}
		
	/**
	 * Execute the update job.
	 * @param elementSetName
	 * @param recordsPerCall
	 * @param requestPause The pause between two requests in milliseconds
	 * @throws Exception
	 */
	public void execute(ElementSetName elementSetName, int recordsPerCall, int requestPause) throws Exception {
		
		// get cached record ids (for later removal of records that do not exist anymore)
		Set<String> cachedRecordIds = this.cache.getCachedRecordIds();

		// set up client
		CSWClient client = (CSWClient)this.factory.createClient();
		client.configure(this.factory);
		
		// variables for complete fetch process
		int numTotal = 0;
		List<String> fetchedRecordIds = new ArrayList<String>();
		
		// iterate over all filters
		for (Document filter : filterSet) {
			if (log.isDebugEnabled())
				log.debug("Processing filter "+StringUtils.nodeToString(filter).replace("\n", "")+".");
			
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
				log.info(numCurrentTotal+" records.");
			
			if (numCurrentTotal > 0) {
				
				// process
				processResult(result, currentFetchedRecordIds);	
				
				while (result.getNumberOfRecords() > 0 && currentFetchedRecordIds.size() < numCurrentTotal) {
					log.info("Fetched "+currentFetchedRecordIds.size()+" records of "+numCurrentTotal+" for Elementset "+elementSetName+".");
	
					Thread.sleep(requestPause);
	
					// prepare next request
					query.setStartPosition(query.getStartPosition()+result.getNumberOfRecords());
	
					// do next request
					result = client.getRecords(query);
	
					// process
					processResult(result, currentFetchedRecordIds);			
				}
			}
			
			// collect record ids
			fetchedRecordIds.addAll(currentFetchedRecordIds);
			numTotal += numCurrentTotal;
		}
		
		// check duplicates
		int duplicates = fetchedRecordIds.size() - new HashSet<String>(fetchedRecordIds).size();
		log.info("Fetched "+fetchedRecordIds.size()+" records of "+numTotal+" for Elementset "+elementSetName+". Duplicates: "+duplicates);
		
		// remove deprecated records
		for (String cachedRecordId : cachedRecordIds) {
			if (!fetchedRecordIds.contains(cachedRecordId))
				this.cache.removeRecord(cachedRecordId);
		}
	}
	
	protected void processResult(CSWSearchResult result, List<String> fetchedRecordIds) throws Exception {
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
	}
}
