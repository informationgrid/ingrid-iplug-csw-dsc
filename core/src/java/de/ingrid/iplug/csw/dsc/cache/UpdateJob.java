/*
 * Copyright (c) 2008 wemove digital solutions. All rights reserved.
 */

package de.ingrid.iplug.csw.dsc.cache;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import de.ingrid.iplug.csw.dsc.cswclient.CSWClient;
import de.ingrid.iplug.csw.dsc.cswclient.CSWClientFactory;
import de.ingrid.iplug.csw.dsc.cswclient.CSWQuery;
import de.ingrid.iplug.csw.dsc.cswclient.CSWRecord;
import de.ingrid.iplug.csw.dsc.cswclient.CSWSearchResult;

public class UpdateJob {

	final protected static Log log = LogFactory.getLog(UpdateJob.class);
	
	private static UpdateJob instance;
	private UpdateJob() {}
	
	/**
	 * Get the singleton instance
	 * @return UpdateJob
	 */
	public static UpdateJob getInstance() {
		if (instance == null) {
			instance = new UpdateJob();
		}
		return instance;
	}
	
	/**
	 * Execute the update job.
	 * @param factory
	 * @param recordsPerCall
	 * @param requestPause The pause between two requests in milliseconds
	 * @throws Exception
	 */
	public void execute(CSWClientFactory factory, int recordsPerCall, int requestPause) throws Exception {
		
		int numTotal = 0;
		List<String> fetchedRecordIds = new ArrayList<String>();

		// set up client
		CSWClient client = (CSWClient)factory.createClient();
		client.configure(factory);

		// create the query
		String filterStr = 
			"<ogc:Filter xmlns:ogc=\"http://www.opengis.net/ogc\">" +
			"<ogc:PropertyIsLike escapeChar=\"\\\" singleChar=\"?\" wildCard=\"*\">" +
			"<ogc:PropertyName>fileIdentifier</ogc:PropertyName>" +
			"<ogc:Literal>1*</ogc:Literal>" +
			"</ogc:PropertyIsLike>" +
			"</ogc:Filter>";
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
		Document filter = docBuilder.parse(new InputSource(new StringReader(filterStr)));
		
		CSWQuery query = factory.createQuery();
		query.setConstraint(filter);
		query.setMaxRecords(recordsPerCall);
		query.setStartPosition(1);
		
		// do requests
		
		// do first request
		CSWSearchResult result = client.getRecords(query);
		numTotal = result.getNumberOfRecordsTotal();
		if (numTotal > 0) {
			
			// process
			processResult(result, fetchedRecordIds);			
			while (fetchedRecordIds.size() < numTotal) {

				Thread.sleep(requestPause);

				// update status
				int numLastFetched = result.getNumberOfRecords();
				int diff = numTotal-fetchedRecordIds.size();
				
				// prepare next request
				query.setStartPosition(query.getStartPosition()+numLastFetched);
				if (diff < recordsPerCall) {
					query.setMaxRecords(diff);
				}

				// do next request
				result = client.getRecords(query);

				// process
				processResult(result, fetchedRecordIds);			
			}
		}
		log.info("Fetched "+fetchedRecordIds.size()+" records");
	}
	
	protected void processResult(CSWSearchResult result, List<String> fetchedRecordIds) {
		for (CSWRecord record : result.getRecordList()) {
			fetchedRecordIds.add(record.getId());
		}
		log.info("Fetched "+fetchedRecordIds.size()+" of "+result.getNumberOfRecordsTotal()+
				" [starting from "+result.getQuery().getStartPosition()+"]");		
	}
}
