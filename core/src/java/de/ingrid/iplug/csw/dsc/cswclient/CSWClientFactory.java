/*
 * Copyright (c) 2008 wemove digital solutions. All rights reserved.
 */

package de.ingrid.iplug.csw.dsc.cswclient;

import de.ingrid.iplug.csw.dsc.cswclient.impl.GenericClient;
import de.ingrid.utils.PlugDescription;

/**
 * This class is used to create all concrete CSW related classes
 * which may vary between the different CSW servers. 
 * The specific implementation is configured in plugdescription.xml.
 * @author ingo herwig <ingo@wemove.com>
 * TODO: Check if request implementation may vary with operation
 */
public class CSWClientFactory {

	private static final String CWS_CLIENT_IMPL 			= "CSWClientImpl";
	private static final String CWS_REQUEST_IMPL 			= "CSWRequestImpl";
	private static final String CWS_CAPABILITIES_IMPL 		= "CSWCapabilitiesImpl";
	private static final String CWS_RECORD_DESCRIPTION_IMPL = "CSWRecordDescriptionImpl";
	private static final String CWS_QUERY_IMPL 				= "CSWQueryImpl";
	private static final String CWS_RECORD_IMPL 			= "CSWRecordImpl";

	private static PlugDescription plugDescription;
	
	/**
	 * Configure the factory.
	 * @param plugDescription
	 * @throws Exception
	 */
	public void configure(PlugDescription plugDescription) throws IllegalArgumentException {
		// check required keys
		if (!plugDescription.containsKey(CWS_CLIENT_IMPL))
			throw new IllegalArgumentException("Key '"+CWS_CLIENT_IMPL+"' missing in plugdescription.xml");
		if (!plugDescription.containsKey(CWS_REQUEST_IMPL))
			throw new IllegalArgumentException("Key '"+CWS_REQUEST_IMPL+"' missing in plugdescription.xml");
		if (!plugDescription.containsKey(CWS_CAPABILITIES_IMPL))
			throw new IllegalArgumentException("Key '"+CWS_CAPABILITIES_IMPL+"' missing in plugdescription.xml");
		if (!plugDescription.containsKey(CWS_RECORD_DESCRIPTION_IMPL))
			throw new IllegalArgumentException("Key '"+CWS_RECORD_DESCRIPTION_IMPL+"' missing in plugdescription.xml");
		if (!plugDescription.containsKey(CWS_QUERY_IMPL))
			throw new IllegalArgumentException("Key '"+CWS_QUERY_IMPL+"' missing in plugdescription.xml");
		if (!plugDescription.containsKey(CWS_RECORD_IMPL))
			throw new IllegalArgumentException("Key '"+CWS_RECORD_IMPL+"' missing in plugdescription.xml");
		// assign member
		CSWClientFactory.plugDescription = plugDescription;
	}

	/**
	 * Get a configuration value from the PlugDescription.
	 * @param key The key
	 * @return String The value
	 */
	public String getConfigurationValue(String key) {
		return CSWClientFactory.plugDescription.get(key).toString();
	}

	/**
	 * Create a CSWClient.
	 * @return A concrete CSWClient instance
	 * @throws ClassNotFoundException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public CSWClient createClient() throws Exception {
		if (plugDescription != null)
		{
			String className = (String)plugDescription.get(CWS_CLIENT_IMPL);
			GenericClient factory = (GenericClient)Class.forName(className).newInstance();
			return factory;
		}
		else
			throw new RuntimeException("CSWClientFactory is not configured properly. Make sure to call CSWClientFactory.configure.");
	}

	/**
	 * Create a CSWRequest.
	 * @return A concrete CSWRequest instance
	 */
	public CSWRequest createRequest() throws Exception {
		if (plugDescription != null)
		{
			String className = (String)plugDescription.get(CWS_REQUEST_IMPL);
			CSWRequest request = (CSWRequest)Class.forName(className).newInstance();
			return request;
		}
		else
			throw new RuntimeException("CSWClientFactory is not configured properly. Make sure to call CSWClientFactory.configure.");
	}

	/**
	 * Create a CSWCapabilities.
	 * @return A concrete CSWCapabilities instance
	 */
	public CSWCapabilities createCapabilities() throws Exception {
		if (plugDescription != null)
		{
			String className = (String)plugDescription.get(CWS_CAPABILITIES_IMPL);
			CSWCapabilities capabilities = (CSWCapabilities)Class.forName(className).newInstance();
			return capabilities;
		}
		else
			throw new RuntimeException("CSWClientFactory is not configured properly. Make sure to call CSWClientFactory.configure.");
	}

	/**
	 * Create a CSWRecordDescription.
	 * @return A concrete CSWRecordDescription instance
	 */
	public CSWRecordDescription createRecordDescription() throws Exception {
		if (plugDescription != null)
		{
			String className = (String)plugDescription.get(CWS_RECORD_DESCRIPTION_IMPL);
			CSWRecordDescription description = (CSWRecordDescription)Class.forName(className).newInstance();
			return description;
		}
		else
			throw new RuntimeException("CSWClientFactory is not configured properly. Make sure to call CSWClientFactory.configure.");
	}

	/**
	 * Create a CSWQuery.
	 * @return A concrete CSWQuery instance
	 */
	public CSWQuery createQuery() throws Exception {
		if (plugDescription != null)
		{
			String className = (String)plugDescription.get(CWS_QUERY_IMPL);
			CSWQuery query = (CSWQuery)Class.forName(className).newInstance();
			return query;
		}
		else
			throw new RuntimeException("CSWClientFactory is not configured properly. Make sure to call CSWClientFactory.configure.");
	}

	/**
	 * Create a CSWRecord.
	 * @return A concrete CSWRecord instance
	 */
	public CSWRecord createRecord() throws Exception {
		if (plugDescription != null)
		{
			String className = (String)plugDescription.get(CWS_RECORD_IMPL);
			CSWRecord record = (CSWRecord)Class.forName(className).newInstance();
			return record;
		}
		else
			throw new RuntimeException("CSWClientFactory is not configured properly. Make sure to call CSWClientFactory.configure.");
	}
}
