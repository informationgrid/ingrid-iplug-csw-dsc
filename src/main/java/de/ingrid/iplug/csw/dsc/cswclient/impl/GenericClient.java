/*
 * Copyright (c) 2008 wemove digital solutions. All rights reserved.
 */

package de.ingrid.iplug.csw.dsc.cswclient.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import de.ingrid.iplug.csw.dsc.cswclient.CSWCapabilities;
import de.ingrid.iplug.csw.dsc.cswclient.CSWClient;
import de.ingrid.iplug.csw.dsc.cswclient.CSWFactory;
import de.ingrid.iplug.csw.dsc.cswclient.CSWDomain;
import de.ingrid.iplug.csw.dsc.cswclient.CSWQuery;
import de.ingrid.iplug.csw.dsc.cswclient.CSWRecord;
import de.ingrid.iplug.csw.dsc.cswclient.CSWRecordDescription;
import de.ingrid.iplug.csw.dsc.cswclient.CSWSearchResult;
import de.ingrid.iplug.csw.dsc.cswclient.constants.ElementSetName;
import de.ingrid.iplug.csw.dsc.cswclient.constants.Operation;
import de.ingrid.iplug.csw.dsc.cswclient.constants.ResultType;
import de.ingrid.utils.xml.XPathUtils;

public class GenericClient implements CSWClient {

	final protected static Log log = LogFactory.getLog(GenericClient.class);
	
	protected CSWFactory factory;
	protected CSWCapabilities capabilities;

	@Override
	public void configure(CSWFactory factory) {
		this.factory = factory;
	}

	@Override
	public CSWFactory getFactory() {
		return factory;
	}

	@Override
	public CSWCapabilities getCapabilities() throws Exception {
		if (this.capabilities == null) {
			if (factory != null) {
				this.capabilities = factory.createCapabilities();
	
				String serviceUrl = factory.getServiceUrl();
				Document capDoc = factory.createRequest(Operation.GET_CAPABILITIES).doGetCapabilitiesRequest(serviceUrl);
				capabilities.initialize(capDoc);
			}
			else
				throw new RuntimeException("CSWClient is not configured properly. Make sure to call CSWClient.configure.");
		}
		return this.capabilities;
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
			CSWCapabilities cap = this.getCapabilities();
			String opUrl = cap.getOperationUrl(Operation.GET_RECORDS);
			if (opUrl == null)
				opUrl = factory.getServiceUrl();
			Document responseDoc = factory.createRequest(Operation.GET_RECORDS).doGetRecords(opUrl, query);

			CSWSearchResult result = factory.createSearchResult();
			result.initialize(factory, query, responseDoc);
			return result;
		}
		else
			throw new RuntimeException("CSWClient is not configured properly. Make sure to call CSWClient.configure.");
	}

	@Override
	public CSWRecord getRecordById(CSWQuery query) throws Exception {
		if (factory != null) {
			CSWCapabilities cap = this.getCapabilities();
			String opUrl = cap.getOperationUrl(Operation.GET_RECORD_BY_ID);
			if (opUrl == null)
				opUrl = factory.getServiceUrl();
			Document responseDoc = factory.createRequest(Operation.GET_RECORD_BY_ID).doGetRecordById(opUrl, query);
			
			// extract the record from the response
			Node recordNode = XPathUtils.getNode(responseDoc, "//csw:GetRecordByIdResponse/child::*");
			if (recordNode == null || recordNode.getNodeName() == "") {
				log.error("Invalid GetRecordByIdResponse! No response has been supplied by the connected service (requesting record: " + query.getId() + ").");
				throw new Exception("Invalid GetRecordByIdResponse! No response has been supplied by the connected service (requesting record: \" + query.getId() + \").");
			}
			
			CSWRecord record = factory.createRecord();
			record.initialize(query.getElementSetName(), recordNode);
			return record;
		}
		else
			throw new RuntimeException("CSWClient is not configured properly. Make sure to call CSWClient.configure.");
	}
}
