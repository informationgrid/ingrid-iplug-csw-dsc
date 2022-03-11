/*
 * **************************************************-
 * ingrid-iplug-csw-dsc:war
 * ==================================================
 * Copyright (C) 2014 - 2022 wemove digital solutions GmbH
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

/**
 * Representation of a request sent to a CSW server.
 * @author ingo herwig <ingo@wemove.com>
 */
public interface CSWRequest {

	/**
	 * Do the GetCapabilities request
	 * @param serverURL
	 * @return The response DOM Document
	 */
	public Document doGetCapabilitiesRequest(String serverURL) throws Exception;

	/**
	 * Do the GetRecords request
	 * @param serverURL
	 * @param query
	 * @return The response DOM Document
	 */
	public Document doGetRecords(String serverURL, CSWQuery query) throws Exception;

	/**
	 * Do the GetRecordById request
	 * @param serverURL
	 * @param query
	 * @return The response DOM Document
	 */
	public Document doGetRecordById(String serverURL, CSWQuery query) throws Exception;
}
