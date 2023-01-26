/*
 * **************************************************-
 * ingrid-iplug-csw-dsc:war
 * ==================================================
 * Copyright (C) 2014 - 2023 wemove digital solutions GmbH
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
 * Copyright (c) 2008 wemove digital solutions. All rights reserved.
 */

package de.ingrid.iplug.csw.dsc.cswclient;

import org.w3c.dom.Document;

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
	public void configure(CSWFactory factory);

	/**
	 * Get the CSWFactory
	 * @return A CSWFactory instance
	 */
	public CSWFactory getFactory();
	
	/**
	 * Do the OGC_Service.GetCapabilities request
	 * @note The request url is the service url 
	 * @return A CSWCapabilities instance
	 */
	public CSWCapabilities getCapabilities() throws Exception;
	
	/**
	 * Do the CSW-Discovery.DescribeRecord request
	 * @note The request url is the taken from the capabilities document
	 * and defaults to service url, if not defined there 
	 * @return A CSWRecordDescription instance
	 */
	public CSWRecordDescription describeRecord() throws Exception;

	/**
	 * Do the CSW-Discovery.GetDomain request
	 * @note The request url is the taken from the capabilities document
	 * and defaults to service url, if not defined there 
	 * @return A CSWRecordDescription instance
	 */
	public CSWDomain getDomain() throws Exception;

	/**
	 * Do the CSW-Discovery.GetRecords request using the
	 * CSWQuery implementation provided by CSWFactory
	 * @note The request url is the taken from the capabilities document
	 * and defaults to service url, if not defined there 
	 * @param constraint A OGC filter document
	 * @param resultType The ResultType
	 * @param elementSetName The ElementSetName
	 * @param startPosition The position to start fetching from
	 * @param maxRecords The maximum number if records to get
	 * @return A CSWSearchResult instances
	 */
	public CSWSearchResult getRecords(Document constraint, ResultType resultType, 
			ElementSetName elementSetName, int startPosition, int maxRecords) throws Exception;

	/**
	 * Do the CSW-Discovery.GetRecords request using a
	 * given CSWQuery implementation
	 * @note The request url is the taken from the capabilities document
	 * and defaults to service url, if not defined there 
	 * @param query
	 * @return A CSWSearchResult instances
	 */
	public CSWSearchResult getRecords(CSWQuery query) throws Exception;

	/**
	 * Do the CSW-Discovery.GetRecordById request
	 * @note The request url is the taken from the capabilities document
	 * and defaults to service url, if not defined there 
	 * @param query
	 * @return A CSWRecord instances
	 */
	public CSWRecord getRecordById(CSWQuery query) throws Exception;
}
