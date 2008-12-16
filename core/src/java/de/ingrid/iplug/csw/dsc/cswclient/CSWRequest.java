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
}
