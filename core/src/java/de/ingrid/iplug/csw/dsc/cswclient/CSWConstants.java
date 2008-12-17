/*
 * Copyright (c) 2008 wemove digital solutions. All rights reserved.
 */

package de.ingrid.iplug.csw.dsc.cswclient;

import javax.xml.namespace.QName;

public class CSWConstants {

	public static final String SERVICE_TYPE = "CSW";
	public static final String VERSION_2_0_0 = "2.0.0";
	public static final String VERSION_2_0_1 = "2.0.1";
	public static final String VERSION_2_0_2 = "2.0.2";

	/**
	 * The version preferred by the application
	 */
	public static final String PREFERRED_VERSION = VERSION_2_0_2;
		
	/**
	 * namespaces
	 */
	public static final QName NAMESPACE_CSW = new QName("http://www.opengis.net/cat/csw", "", "csw");
	public static final QName NAMESPACE_CSW_2_0_2 = new QName(NAMESPACE_CSW+"/2.0.2", "", NAMESPACE_CSW.getPrefix());
	public static final QName NAMESPACE_OWS = new QName("http://www.opengis.net/ows", "", "ows");
	public static final QName NAMESPACE_GMD = new QName("http://www.isotc211.org/2005/gmd", "", "gmd");
	
	
	public static final QName NAMESPACE_CSW_PROFILE = new QName(NAMESPACE_CSW.getNamespaceURI(), "profile", NAMESPACE_CSW.getPrefix());
	public static final QName NAMESPACE_CSW_RECORD = new QName(CSWConstants.NAMESPACE_CSW.getNamespaceURI(), "Record", CSWConstants.NAMESPACE_CSW.getPrefix());
	public static final QName NAMESPACE_CSW_DATASET = new QName(CSWConstants.NAMESPACE_CSW.getNamespaceURI(), "dataset", CSWConstants.NAMESPACE_CSW.getPrefix());

	public static final QName NAMESPACE_ISO_METADATA = new QName(CSWConstants.NAMESPACE_GMD.getNamespaceURI(), "MD_Metadata", CSWConstants.NAMESPACE_GMD.getPrefix());
}
