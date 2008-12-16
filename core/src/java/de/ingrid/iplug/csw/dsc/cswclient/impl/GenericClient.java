/*
 * Copyright (c) 2008 wemove digital solutions. All rights reserved.
 */

package de.ingrid.iplug.csw.dsc.cswclient.impl;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

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
import de.ingrid.iplug.csw.dsc.cswclient.constants.ElementSetName;
import de.ingrid.iplug.csw.dsc.cswclient.constants.ResultType;
import de.ingrid.iplug.csw.dsc.index.AbstractSearcher;

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

			String serviceUrl = factory.getConfigurationValue("serviceUrl");
			Document capDoc = factory.createRequest().doGetCapabilitiesRequest(serviceUrl);
			cap.configure(capDoc);
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
	public List<CSWRecord> getRecords(Document filter, ResultType resultType, 
			ElementSetName elementSetName) throws Exception {
		log.debug("getRecords");
		if (factory != null) {
			CSWQuery query = factory.createQuery();
			query.setFilter(filter);
			query.setResultType(resultType);
			query.setElementSetName(elementSetName);
			
			String serviceUrl = factory.getConfigurationValue("serviceUrl");
			Document recordDoc = factory.createRequest().doGetRecords(serviceUrl, query);
			return null;
		}
		else
			throw new RuntimeException("CSWClient is not configured properly. Make sure to call CSWClient.configure.");
	}

	@Override
	public CSWRecord getRecordById(String id) throws Exception {
		return null;
	}
}
