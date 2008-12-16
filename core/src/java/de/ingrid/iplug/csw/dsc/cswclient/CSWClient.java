/*
 * Copyright (c) 2008 wemove digital solutions. All rights reserved.
 */

package de.ingrid.iplug.csw.dsc.cswclient;

import java.util.List;

import org.w3c.dom.Document;

import de.ingrid.iplug.csw.dsc.cswclient.CSWClientFactory;
import de.ingrid.iplug.csw.dsc.cswclient.constants.ElementSetName;
import de.ingrid.iplug.csw.dsc.cswclient.constants.ResultType;

/**
 * Interface definition for a CSW ISO server.
 * @author ingo herwig <ingo@wemove.com>
 */
public interface CSWClient {

	/**
	 * Configure the CSWClient
	 * @param factory
	 */
	public void configure(CSWClientFactory factory);

	/**
	 * Do the OGC_Service.GetCapabilities request
	 * @return A CSWCapabilities instance
	 */
	public CSWCapabilities getCapabilities() throws Exception;
	
	/**
	 * Do the CSW-Discovery.DescribeRecord request
	 * @return A CSWRecordDescription instance
	 */
	public CSWRecordDescription describeRecord() throws Exception;

	/**
	 * Do the CSW-Discovery.GetDomain request
	 * @return A CSWRecordDescription instance
	 */
	public CSWDomain getDomain() throws Exception;

	/**
	 * Do the CSW-Discovery.GetRecords request
	 * @param filter A OGC filter document
	 * @param resultType The ResultType
	 * @param elementSetName The ElementSetName
	 * @return A list of CSWRecord instances
	 */
	public List<CSWRecord> getRecords(Document filter, ResultType resultType, 
			ElementSetName elementSetName) throws Exception;

	/**
	 * Do the CSW-Discovery.GetRecordById request
	 * @param id The id
	 * @return A CSWRecord instances
	 */
	public CSWRecord getRecordById(String id) throws Exception;
}
