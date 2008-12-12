/*
 * Copyright (c) 2008 wemove digital solutions. All rights reserved.
 */

package de.ingrid.iplug.csw.dsc.cswclient.impl;

import java.util.List;

import org.w3c.dom.Document;

import de.ingrid.iplug.csw.dsc.cswclient.CSWCapabilities;
import de.ingrid.iplug.csw.dsc.cswclient.CSWClient;
import de.ingrid.iplug.csw.dsc.cswclient.CSWClientFactory;
import de.ingrid.iplug.csw.dsc.cswclient.CSWDomain;
import de.ingrid.iplug.csw.dsc.cswclient.CSWRecord;
import de.ingrid.iplug.csw.dsc.cswclient.CSWRecordDescription;

public class GenericClient implements CSWClient {

	private CSWClientFactory factory;

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
		return null;
	}

	@Override
	public CSWDomain getDomain() throws Exception {
		return null;
	}

	@Override
	public List<CSWRecord> getRecords() throws Exception {
		return null;
	}

	@Override
	public CSWRecord getRecordById() throws Exception {
		return null;
	}
}
