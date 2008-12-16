/*
 * Copyright (c) 2008 wemove digital solutions. All rights reserved.
 */

package de.ingrid.iplug.csw.dsc.cswclient;

import javax.xml.namespace.QName;

public class CSWConstants {

	public static final String SERVICE_TYPE = "CSW";
	public static final String VERSION_2_0_2 = "2.0.2";

	/**
	 * The version preferred by the application
	 */
	public static final String PREFERRED_VERSION = VERSION_2_0_2;
		
	/**
	 * namespaces
	 */
	public static final QName NAMESPACE_CSW = new QName("http://www.opengis.net/cat/csw", "", "csw");
	public static final QName NAMESPACE_OWS = new QName("http://www.opengis.net/ows", "", "ows");
	public static final QName NAMESPACE_GMD = new QName("http://www.isotc211.org/2005/gmd", "", "gmd");
}
