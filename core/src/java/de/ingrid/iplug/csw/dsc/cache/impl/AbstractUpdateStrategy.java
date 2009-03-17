/*
 * Copyright (c) 2009 wemove digital solutions. All rights reserved.
 */

package de.ingrid.iplug.csw.dsc.cache.impl;

import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.logging.Log;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import de.ingrid.iplug.csw.dsc.cache.Cache;
import de.ingrid.iplug.csw.dsc.cache.ExecutionContext;
import de.ingrid.iplug.csw.dsc.cache.UpdateStrategy;
import de.ingrid.iplug.csw.dsc.cswclient.CSWClient;
import de.ingrid.iplug.csw.dsc.cswclient.CSWFactory;
import de.ingrid.iplug.csw.dsc.cswclient.CSWQuery;
import de.ingrid.iplug.csw.dsc.cswclient.CSWRecord;
import de.ingrid.iplug.csw.dsc.cswclient.CSWSearchResult;
import de.ingrid.iplug.csw.dsc.cswclient.constants.ElementSetName;
import de.ingrid.iplug.csw.dsc.cswclient.constants.ResultType;
import de.ingrid.iplug.csw.dsc.tools.StringUtils;

public abstract class AbstractUpdateStrategy implements UpdateStrategy {
	
	DocumentBuilder docBuilder = null;

	/**
	 * Create a filter Document from a filter string. Replace any filter variables.
	 * TODO: if there should be more variables, this could be done more generic 
	 * @param filterStr
	 * @return Document
	 * @throws Exception 
	 */
	protected Document createFilterDocument(String filterStr) throws Exception {

		ExecutionContext context = this.getExecutionContext();

		if (this.docBuilder == null) {
			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
			docBuilder = docBuilderFactory.newDocumentBuilder();
		}

		// replace last update date variable
		Pattern lastUpdateDatePattern = Pattern.compile("\\{LAST_UPDATE_DATE\\}", Pattern.MULTILINE);
		Matcher matcher = lastUpdateDatePattern.matcher(filterStr);
		if (matcher.find()) {
			Date lastUpdateDate = context.getLastExecutionDate();
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
			filterStr = matcher.replaceAll(df.format(lastUpdateDate));
		}
		
		return docBuilder.parse(new InputSource(new StringReader(filterStr)));
	}

	/**
	 * Fetch all records that satisfy the given filter using the GetRecords and return the ids
	 * and put them into the cache
	 * @param client The CSWClient to use
	 * @param elementSetName The ElementSetName of the records to fetch
	 * @param filterSet The filter set used to select the records 
	 * @param recordsPerCall Number of records to fetch per call
	 * @param requestPause The time between two requests in milliseconds 
	 * @param doCache Determines wether to cache the record or not
	 * @return A list of ids of the fetched records
	 * @throws Exception
	 */
	protected List<String> fetchRecords(CSWClient client, ElementSetName elementSetName, Set<Document> filterSet,
			int recordsPerCall, int requestPause, boolean doCache) throws Exception {

		CSWFactory factory = client.getFactory();
		Log log = this.getLog();
				
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
			CSWQuery query = factory.createQuery();
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
				currentFetchedRecordIds.addAll(processResult(result, doCache));	
				
				while (result.getNumberOfRecords() > 0 && currentFetchedRecordIds.size() < numCurrentTotal) {
					Thread.sleep(requestPause);
	
					// prepare next request
					query.setStartPosition(query.getStartPosition()+result.getNumberOfRecords());
	
					// do next request
					result = client.getRecords(query);
	
					// process
					currentFetchedRecordIds.addAll(processResult(result, doCache));			
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
	 * @param requestPause The time between two requests in milliseconds 
	 * @throws Exception
	 */
	protected void fetchRecords(CSWClient client, ElementSetName elementSetName, List<String> recordIds,
			int requestPause) throws Exception {

		CSWFactory factory = client.getFactory();
		Cache cache = this.getExecutionContext().getCache();
		Log log = this.getLog();

		CSWQuery query = factory.createQuery();
		query.setElementSetName(elementSetName);
	
		for (String id : recordIds) {
			query.setId(id);
			CSWRecord record = client.getRecordById(query);
			if (log.isInfoEnabled())
				log.info("Fetched record: "+id+" "+record.getElementSetName());
			cache.putRecord(record);
			Thread.sleep(requestPause);
		}		
	}

	/**
	 * Process a fetched search result (collect ids and cache records)
	 * @param result The search result
	 * @param doCache Determines wether to cache the record or not
	 * @return The list of ids of the fetched records
	 * @throws Exception
	 */
	private List<String> processResult(CSWSearchResult result, boolean doCache) throws Exception {

		Cache cache = this.getExecutionContext().getCache();
		Log log = this.getLog();
		
		List<String> fetchedRecordIds = new ArrayList<String>();
		for (CSWRecord record : result.getRecordList()) {
			String id = record.getId();

			if (log.isInfoEnabled())
				log.info("Fetched record: "+id+" "+record.getElementSetName());
			if (fetchedRecordIds.contains(id)) {
				log.warn("Duplicated id: "+id+". Overriding previous entry.");
			}
			fetchedRecordIds.add(id);
		
			// cache only if requested
			if (doCache)
				cache.putRecord(record);
		}
		if (log.isInfoEnabled())
			log.info("Fetched "+fetchedRecordIds.size()+" of "+result.getNumberOfRecordsTotal()+
					" [starting from "+result.getQuery().getStartPosition()+"]");
		return fetchedRecordIds;
	}
}