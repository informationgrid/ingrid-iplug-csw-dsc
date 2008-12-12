/*
 * Copyright (c) 2008 wemove digital solutions. All rights reserved.
 */

package de.ingrid.iplug.csw.dsc.cswclient.impl;

import java.io.BufferedReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

import de.ingrid.iplug.csw.dsc.cswclient.CSWConstants;
import de.ingrid.iplug.csw.dsc.cswclient.CSWRequest;

public class KVPGetRequest implements CSWRequest {

	@Override
	public Document doGetCapabilitiesRequest(String serverURL) throws Exception {
		
		// add the GetCapability request parameters, 
		// @note Parameters must be treated in case-insensitive manner on the server side
		String requestURL = serverURL+"?SERVICE="+CSWConstants.SERVICE_TYPE+"&REQUEST="+CSWConstants.OP_GET_CAPABILITIES+
			"&acceptversion="+CSWConstants.PREFERRED_VERSION+"&outputFormat=text/xml";
		
		// and make the call
		Document result = null;
		HttpURLConnection conn = null;
		BufferedReader rd = null;
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
		        DocumentBuilder builder = domFactory.newDocumentBuilder();
		        result = builder.parse(conn.getInputStream());
			}
		}
		catch (Exception e) {
			throw e;
		}
		finally {
			if (conn != null)
				conn.disconnect();
			if (rd != null)
				rd.close();
		}
		return result;
	}
}
