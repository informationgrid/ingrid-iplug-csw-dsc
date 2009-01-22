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

/**
 * The update job.
 * @author ingo herwig <ingo@wemove.com>
 */
public class UpdateJob {

	final protected static Log log = LogFactory.getLog(UpdateJob.class);
	
	protected CSWFactory factory = null;	
	protected Cache cache = null;
	protected Document filter = null;

	/**
	 * Configure the job
	 * @param factory
	 * @param cache
	 * @param filterStr The ogc:Filter string to query the server with
	 * @throws Exception 
	 */
	public void configure(CSWFactory factory, Cache cache, String filterStr) throws Exception {
		this.factory = factory;
		this.cache = cache;
		
		// create the filter document
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
		this.filter = docBuilder.parse(new InputSource(new StringReader(filterStr)));
	}
		
	/**
	 * Execute the update job.
	 * @param elementSetName
	 * @param recordsPerCall
	 * @param requestPause The pause between two requests in milliseconds
	 * @throws Exception
	 */
	public void execute(ElementSetName elementSetName, int recordsPerCall, int requestPause) throws Exception {
		
		int numTotal = 0;
		List<String> fetchedRecordIds = new ArrayList<String>();
		
		// get cached record ids (for later removal of records that do not exist anymore)
		Set<String> cachedRecordIds = this.cache.getCachedRecordIds();

		// set up client
		CSWClient client = (CSWClient)this.factory.createClient();
		client.configure(this.factory);

		// create the query
		CSWQuery query = this.factory.createQuery();
		query.setConstraint(this.filter);
		query.setResultType(ResultType.RESULTS);
		query.setElementSetName(elementSetName);
		query.setMaxRecords(recordsPerCall);
		query.setStartPosition(1);
		
		// do requests
		
		// do first request
		CSWSearchResult result = client.getRecords(query);
		numTotal = result.getNumberOfRecordsTotal();
		if (numTotal > 0) {
			
			// process
			processResult(result, fetchedRecordIds);	
			
			while (result.getNumberOfRecords() > 0 && fetchedRecordIds.size() < numTotal) {
				log.info("Fetched "+fetchedRecordIds.size()+" records of "+numTotal+" for Elementset " + elementSetName + ".");

				Thread.sleep(requestPause);

				// prepare next request
				query.setStartPosition(query.getStartPosition()+result.getNumberOfRecords());

				// do next request
				result = client.getRecords(query);

				// process
				processResult(result, fetchedRecordIds);			
			}
		}
		
		// check duplicates
		int duplicates = fetchedRecordIds.size() - new HashSet<String>(fetchedRecordIds).size();
		log.info("Fetched "+fetchedRecordIds.size()+" records of "+numTotal+" for Elementset " + elementSetName + ". Duplicates: "+duplicates);
		
		// remove deprecated records
		for (String cachedRecordId : cachedRecordIds) {
			if (!fetchedRecordIds.contains(cachedRecordId))
				this.cache.removeRecord(cachedRecordId);
		}
	}
	
	protected void processResult(CSWSearchResult result, List<String> fetchedRecordIds) throws Exception {
		for (CSWRecord record : result.getRecordList()) {
			String id = record.getId();
			if (log.isDebugEnabled())
				log.debug("Record: "+id+" "+record.getElementSetName());
			if (fetchedRecordIds.contains(id)) {
				log.warn("Duplicated id: "+id+". Overriding previous entry.");
			}
			this.cache.putRecord(record);
			fetchedRecordIds.add(id);
		}
		if (log.isDebugEnabled())
			log.debug("Fetched "+fetchedRecordIds.size()+" of "+result.getNumberOfRecordsTotal()+
					" [starting from "+result.getQuery().getStartPosition()+"]");		
	}
}
