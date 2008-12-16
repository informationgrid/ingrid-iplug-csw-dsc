/*
 * Copyright (c) 2008 wemove digital solutions. All rights reserved.
 */

package de.ingrid.iplug.csw.dsc.cswclient.constants;

import javax.xml.namespace.QName;

import de.ingrid.iplug.csw.dsc.cswclient.CSWConstants;

public enum TypeName {
	MD_METADATA { public QName getQName() { return metaDataQName; } },
	RECORD 		{ public QName getQName() { return recordQName; } };
	
	QName metaDataQName = new QName(CSWConstants.NAMESPACE_GMD.getNamespaceURI(), "MD_Metadata", 
			CSWConstants.NAMESPACE_GMD.getPrefix());
	QName recordQName = new QName(CSWConstants.NAMESPACE_CSW.getNamespaceURI(), "Record", 
			CSWConstants.NAMESPACE_CSW.getPrefix());
	
	public abstract QName getQName(); 
}
