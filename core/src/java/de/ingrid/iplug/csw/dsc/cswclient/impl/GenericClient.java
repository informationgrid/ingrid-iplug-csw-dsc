/*
 * Copyright (c) 2008 wemove digital solutions. All rights reserved.
 */

package de.ingrid.iplug.csw.dsc.cswclient.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;

import de.ingrid.iplug.csw.dsc.cswclient.CSWCapabilities;
import de.ingrid.iplug.csw.dsc.cswclient.CSWClient;
import de.ingrid.iplug.csw.dsc.cswclient.CSWClientFactory;
import de.ingrid.iplug.csw.dsc.cswclient.CSWDomain;
import de.ingrid.iplug.csw.dsc.cswclient.CSWQuery;
import de.ingrid.iplug.csw.dsc.cswclient.CSWRecord;
import de.ingrid.iplug.csw.dsc.cswclient.CSWRecordDescription;
import de.ingrid.iplug.csw.dsc.cswclient.CSWSearchResult;
import de.ingrid.iplug.csw.dsc.cswclient.constants.ElementSetName;
import de.ingrid.iplug.csw.dsc.cswclient.constants.ResultType;

public class GenericClient implements CSWClient {

	final protected static Log log = LogFactory.getLog(GenericClient.class);
	
	protected CSWClientFactory factory;

	@Override
	public void configure(CSWClientFactory factory) {
		this.factory = factory;
	}

	@Override
	public CSWCapabilities getCapabilities() throws Exception {
		if (factory != null) {
			CSWCapabilities cap = factory.createCapabilities();

			String serviceUrl = factory.getServiceUrl();
			Document capDoc = factory.createRequest().doGetCapabilitiesRequest(serviceUrl);
			cap.initialize(capDoc);
			return cap;
		}
		else
			throw new RuntimeException("CSWClient is not configured properly. Make sure to call CSWClient.configure.");
	}

	@Override
	public CSWRecordDescription describeRecord() throws Exception {
		throw new UnsupportedOperationException("Not implemented yet.");
	}

	@Override
	public CSWDomain getDomain() throws Exception {
		throw new UnsupportedOperationException("Not implemented yet.");
	}

	@Override
	public CSWSearchResult getRecords(Document constraint, ResultType resultType, 
			ElementSetName elementSetName, int startPosition, int maxRecords) throws Exception {
		if (factory != null) {
			CSWQuery query = factory.createQuery();
			query.setConstraint(constraint);
			query.setStartPosition(startPosition);
			query.setMaxRecords(maxRecords);
			query.setResultType(resultType);
			query.setElementSetName(elementSetName);
			
			return this.getRecords(query);
		}
		else
			throw new RuntimeException("CSWClient is not configured properly. Make sure to call CSWClient.configure.");
	}

	@Override
	public CSWSearchResult getRecords(CSWQuery query) throws Exception {
		if (factory != null) {
			String serviceUrl = factory.getServiceUrl();
			Document recordDoc = factory.createRequest().doGetRecords(serviceUrl, query);

			CSWSearchResult result = factory.createSearchResult();
			result.initialize(factory, query, recordDoc);
			return result;
		}
		else
			throw new RuntimeException("CSWClient is not configured properly. Make sure to call CSWClient.configure.");
	}

	@Override
	public CSWRecord getRecordById(CSWQuery query) throws Exception {
		if (factory != null) {
			String serviceUrl = factory.getServiceUrl();
			Document recordDoc = factory.createRequest().doGetRecordById(serviceUrl, query);
			
			CSWRecord record = factory.createRecord();
			record.initialize(query.getElementSetName(), recordDoc);
			return record;
		}
		else
			throw new RuntimeException("CSWClient is not configured properly. Make sure to call CSWClient.configure.");
	}
}
