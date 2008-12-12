/*
 * Copyright (c) 2008 wemove digital solutions. All rights reserved.
 */

package de.ingrid.iplug.csw.dsc.cswclient;

import java.util.List;

import de.ingrid.iplug.csw.dsc.cswclient.CSWClientFactory;

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
	 * @return A list of CSWRecord instances
	 */
	public List<CSWRecord> getRecords() throws Exception;

	/**
	 * Do the CSW-Discovery.GetRecordById request
	 * @return A CSWRecord instances
	 */
	public CSWRecord getRecordById() throws Exception;
}
