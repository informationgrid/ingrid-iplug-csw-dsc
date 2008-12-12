/*
 * Copyright (c) 2008 wemove digital solutions. All rights reserved.
 */

package de.ingrid.iplug.csw.dsc.cswclient;

public class CSWConstants {

	public static final String SERVICE_TYPE = "CSW";
	public static final String VERSION_2_0_2 = "2.0.2";

	/**
	 * The version preferred by the application
	 */
	public static final String PREFERRED_VERSION = VERSION_2_0_2;
	
	/**
	 * OGC_Service methods
	 */
	public static final String OP_GET_CAPABILITIES = "GetCapabilities";

	/**
	 * CSW-Discovery methods
	 */
	public static final String OP_DESCRIBE_RECORD = "DescribeRecord";
	public static final String OP_GET_DOMAIN = "GetDomain";
	public static final String OP_GET_RECORDS = "GetRecords";
	public static final String OP_GET_RECORD_BY_ID = "GetRecordById";
	
	/**
	 * namespaces
	 */
	public static final String NAMESPACE_CSW_URL = "http://www.opengis.net/cat/csw";
	public static final String NAMESPACE_CSW_PREFIX = "csw";
	public static final String NAMESPACE_OWS_URL = "http://www.opengis.net/ows";
	public static final String NAMESPACE_OWS_PREFIX = "ows";
	
}
