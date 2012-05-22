/*
 * Copyright (c) 2008 wemove digital solutions. All rights reserved.
 */

package de.ingrid.iplug.csw.dsc.cswclient.impl;

import java.net.HttpURLConnection;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

import de.ingrid.iplug.csw.dsc.cswclient.CSWConstants;
import de.ingrid.iplug.csw.dsc.cswclient.CSWQuery;
import de.ingrid.iplug.csw.dsc.cswclient.CSWRequest;
import de.ingrid.iplug.csw.dsc.cswclient.constants.Operation;

public class KVPGetRequest implements CSWRequest {

	/**
	 * CSWRequest implementation
	 */
	
	/**
	 * @see OpenGIS Catalogue Services Specification 2.0.2 - ISO Metadata Application Profile 8.2.1.1
	 */
	@Override
	public Document doGetCapabilitiesRequest(String serverURL) throws Exception {
		
		// add the GetCapability request parameters, 
		// @note Parameters must be treated in case-insensitive manner on the server side
		String requestURL = serverURL;
		if (requestURL.endsWith("?")) {
		    requestURL = requestURL.substring(0, requestURL.length() -1);
		}
        URL getCapUrl = new URL(requestURL);
		if (getCapUrl.getQuery() == null) {
		    requestURL += "?SERVICE="+CSWConstants.SERVICE_TYPE+
            "&REQUEST="+Operation.GET_CAPABILITIES;
		} else if (getCapUrl.getQuery().toLowerCase().indexOf("service=") == -1) {
		    requestURL += "&SERVICE="+CSWConstants.SERVICE_TYPE;
        } else if (getCapUrl.getQuery().toLowerCase().indexOf("request=") == -1) {
            requestURL += "&REQUEST="+Operation.GET_CAPABILITIES;
        } else {
            requestURL += "&SERVICE="+CSWConstants.SERVICE_TYPE+
            "&REQUEST="+Operation.GET_CAPABILITIES;
		}
		
		Document result = sendRequest(requestURL);
		return result;
	}

	/**
	 * @see OpenGIS Catalogue Services Specification 2.0.2 - ISO Metadata Application Profile 8.2.2.1
	 */
	@Override
	public Document doGetRecords(String serverURL, CSWQuery query) throws Exception {
		throw new UnsupportedOperationException("This method binding is not mandatory. Please use the SOAP equivalent.");
	}

	/**
	 * @see OpenGIS Catalogue Services Specification 2.0.2 - ISO Metadata Application Profile 8.2.2.2
	 */
	@Override
	public Document doGetRecordById(String serverURL, CSWQuery query) throws Exception {

		// add the GetRecordById request parameters, 
		// @note Parameters must be treated in case-insensitive manner on the server side
		String requestURL = serverURL+
			"?SERVICE="+CSWConstants.SERVICE_TYPE+
			"&REQUEST="+Operation.GET_RECORD_BY_ID+
			"&OUTPUTFORMAT="+query.getOutputFormat().toString()+
			"&VERSION="+CSWConstants.PREFERRED_VERSION+
			"&ELEMENTSETNAME="+query.getElementSetName().toString()+
			"&OUTPUTSCHEMA="+query.getOutputSchema().getQName().getNamespaceURI()+
			"&ID="+query.getId();
		
		Document result = sendRequest(requestURL);
		return result;
	}
	
	/**
	 * Helper methods
	 */
	
	/**
	 * Send the given request to the server.
	 * @param serverURL
	 * @param payload
	 * @return Document
	 * @throws Exception 
	 */
	protected Document sendRequest(String requestURL) throws Exception {
		// and make the call
		Document result = null;
		HttpURLConnection conn = null;
		try {
			URL url = new URL(requestURL);
			conn = (HttpURLConnection)url.openConnection();
			conn.setRequestMethod("GET");
			conn.setAllowUserInteraction(false);
			conn.setReadTimeout(10000);
			conn.setDoOutput(true);
			conn.setRequestProperty("Content-type", "text/xml");
			conn.connect();
			
			int code = conn.getResponseCode();
			if (code >= 200 && code < 300) {
		        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
		        domFactory.setNamespaceAware(true);
		        DocumentBuilder builder = domFactory.newDocumentBuilder();
		        result = builder.parse(conn.getInputStream());
			}
			conn.disconnect();
			conn = null;
		}
		catch (Exception e) {
			throw e;
		}
		finally {
			if (conn != null)
				conn.disconnect();
		}
		return result;
	}
}
