/*
 * **************************************************-
 * ingrid-iplug-csw-dsc:war
 * ==================================================
 * Copyright (C) 2014 - 2015 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.1 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl5
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
/*
 * Copyright (c) 2009 wemove digital solutions. All rights reserved.
 */

package de.ingrid.iplug.csw.dsc.cache.impl;

import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import de.ingrid.iplug.csw.dsc.CswDscSearchPlug;
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

	/** The time in msec the strategy pauses between different requests to the CSW server. */
	int requestPause = 1000;
	
	/** The default number of records the strategy requests at once during fetching of records. */
	int recordsPerCall = 10;

	/** The default number of total fetching retries before ending the fetching process. */
	int maxNumRetries = 50;
	/** The default number of retries per request with increasing pause in between. */
	int numRetriesPerRequest = 3;
	/** The time to wait between retries in milliseconds. Is multiplied with the number of retries. */
	int timeBetweenRetries = 1000;

	
	/**
	 * Set the time in msec the strategy pauses between requests to the CSW server.
	 * 
	 * @param requestPause the requestPause to set
	 */
	public void setRequestPause(int requestPause) {
		this.requestPause = requestPause;
	}

	/**
	 * Set the number of records the strategy requests at once during fetching of records.
	 * 
	 * @param recordsPerCall the recordsPerCall to set
	 */
	public void setRecordsPerCall(int recordsPerCall) {
		this.recordsPerCall = recordsPerCall;
	}


	/**
	 * Create a filter Document from a filter string. Replace any filter
	 * variables. TODO: if there should be more variables, this could be done
	 * more generic
	 * 
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
	 * Fetch all records that satisfy the given filter using the GetRecords and
	 * return the ids and put them into the cache
	 * @note This method guarantees to query the server without a constraint, if the
	 * provided filter set is empty 
	 * 
	 * @param client The CSWClient to use
	 * @param elementSetName The ElementSetName of the records to fetch
	 * @param filterSet The filter set used to select the records
	 * @param doCache Determines wether to cache the record or not
	 * @return A list of ids of the fetched records
	 * @throws Exception
	 */
	protected List<String> fetchRecords(CSWClient client, ElementSetName elementSetName, 
			Set<Document> filterSet, boolean doCache) throws Exception {

		CSWFactory factory = client.getFactory();
		Log log = this.getLog();

		// if the filter set is empty, we add a null a least
		// this causes execution of the iteration below, but
		// but will not add a constraint definition to the request
		if (filterSet == null)
			filterSet = new HashSet<Document>();
		if (filterSet.size() == 0)
			filterSet.add(null);
				
		// variables for complete fetch process
		// int numTotal = 0;
		List<String> fetchedRecordIds = new CopyOnWriteArrayList<String>();

		// iterate over all filters
		int filterIndex = 1;
		for (Document filter : filterSet) {
			if (log.isDebugEnabled())
				log.debug("Processing filter "+filterIndex+": "+
						StringUtils.nodeToString(filter).replace("\n", "")+".");

			// variables for current fetch process (current filter)
			int numRecordsTotal = 0;
			int numRecordsFetched = 0;
			int numRetriesTotal = 0;
			List<String> currentFetchedRecordIds = new ArrayList<String>();

			// create the query
			CSWQuery query = factory.createQuery();
			query.setConstraint(filter);
			query.setResultType(ResultType.RESULTS);
			query.setElementSetName(elementSetName);
			query.setMaxRecords(this.recordsPerCall);
			query.setStartPosition(1);

			// do requests

			// do first request
			
			CSWSearchResult result = client.getRecords(query);
			numRecordsFetched += result.getNumberOfRecords();
			numRecordsTotal = result.getNumberOfRecordsTotal();
			if (log.isInfoEnabled())
				log.info(numRecordsTotal+" record(s) from filter "+filterIndex+":");

			if (numRecordsTotal > 0) {

				// process
				currentFetchedRecordIds.addAll(processResult(result, doCache));

				while (numRecordsFetched < numRecordsTotal) {

					if (numRetriesTotal > this.maxNumRetries) {
				    	log.error("Problems fetching records. Total number of retries reached (" + this.maxNumRetries +
				    		" retries across various requests). We end fetching process for this filter.");
					    break;
					}

					if (result == null) {
						// problems fetching !
					    if (CswDscSearchPlug.conf.continueFetchOnError) {
							// fetching error occured but we should continue !
					    	log.error("Continue fetching all following records for this filter for harvesting (conf continueFetchOnError=true).");
					    } else {
					    	log.error("Skipping all following records for this filter from harvesting (conf continueFetchOnError=false).");
						    break;
					    }
					}

					// generic pause between requests, set via spring
					Thread.sleep(this.requestPause);

					try {
    					// prepare next request
						// Just for safety: get number of last fetched records from last result, if we have a result and records.
						int numLastFetch = query.getMaxRecords();
						if (result != null && (result.getNumberOfRecords() > 0)) {
							numLastFetch = result.getNumberOfRecords();
						}
    					numRecordsFetched += numLastFetch;

    					query.setStartPosition(query.getStartPosition() + numLastFetch);

    
    					// do next request, if problems retry with increasing pause in between 
    					int numRetries = 0;
    					while (true) {
        					try {
            					result = null;
            					result = client.getRecords(query);
            					break;

        					} catch (Exception e) {
        						if (numRetries == this.numRetriesPerRequest) {
        						    log.error("Retried " + numRetries + "times ! We skip records " + query.getStartPosition() + " - " + query.getMaxRecords(), e);
        							break;
        						}
        						numRetries++;
        						int timeBetweenRetry = numRetries * this.timeBetweenRetries;
    						    log.error("Error fetching records " + query.getStartPosition() + " - " + query.getMaxRecords() + ". We retry " +
    						    		numRetries + ". time after " + timeBetweenRetry + "msec !", e);
        						Thread.sleep(timeBetweenRetry);    							
        					}
    					}
    					numRetriesTotal += numRetries;

    
    					// process
    					if (result != null) {
        					currentFetchedRecordIds.addAll(processResult(result, doCache));    						
    					}
					} catch (Exception e) {
					    log.error("Error processing records " + query.getStartPosition() + " - " + query.getMaxRecords());
					    log.error( ExceptionUtils.getStackTrace(e) );
					}
				}
			}

			// collect record ids
			fetchedRecordIds.addAll(currentFetchedRecordIds);
			// numTotal += currentFetchedRecordIds.size();
			filterIndex++;
		}
		return fetchedRecordIds;
	}

	/**
	 * Fetch all records from a id list using the GetRecordById and put them in the cache
	 * 
	 * @param client The CSWClient to use
	 * @param elementSetName The ElementSetName of the records to fetch
	 * @param recordIds The list of ids
	 * @param requestPause The time between two requests in milliseconds
	 * @throws Exception
	 */
	protected void fetchRecords(CSWClient client, ElementSetName elementSetName, 
			List<String> recordIds, int requestPause) throws Exception {

		CSWFactory factory = client.getFactory();
		Cache cache = this.getExecutionContext().getCache();
		Log log = this.getLog();

		CSWQuery query = factory.createQuery();
		query.setElementSetName(elementSetName);

		int cnt = 1;
        int max = recordIds.size();
		Iterator<String> it = recordIds.iterator();
		while (it.hasNext()) {
			String id = it.next();
		    query.setId(id);
			CSWRecord record = null;
			try {
				record = client.getRecordById(query);
				if (log.isDebugEnabled())
					log.debug("Fetched record: "+id+" "+record.getElementSetName() + " (" + cnt + "/" + max + ")");
				cache.putRecord(record);
			} catch (Exception e) {
				log.error("Error fetching record '" + query.getId() + "'! Removing record from cache.", e);
				cache.removeRecord(query.getId());
				recordIds.remove(id);
			}
			cnt++;
			Thread.sleep(requestPause);
		}
	}

	/**
	 * Process a fetched search result (collect ids and cache records)
	 * 
	 * @param result The search result
	 * @param doCache Determines wether to cache the record or not
	 * @return The list of ids of the fetched records
	 * @throws Exception
	 */
	private List<String> processResult(CSWSearchResult result, boolean doCache)
			throws Exception {

		Cache cache = this.getExecutionContext().getCache();
		Log log = this.getLog();

		List<String> fetchedRecordIds = new ArrayList<String>();
		for (CSWRecord record : result.getRecordList()) {
			String id = record.getId();

			if (log.isDebugEnabled())
				log.debug("Fetched record: "+id+" "+record.getElementSetName());
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
					" [starting from "+result.getQuery().getStartPosition() + "]");
		return fetchedRecordIds;
	}
}
