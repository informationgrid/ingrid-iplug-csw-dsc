/*
 * Copyright (c) 2008 wemove digital solutions. All rights reserved.
 */

package de.ingrid.iplug.csw.dsc.cswclient.constants;

import javax.xml.namespace.QName;

import de.ingrid.iplug.csw.dsc.cswclient.CSWConstants;

public enum TypeName {
	MD_METADATA { public QName getQName() { return metaDataQName; } },
	RECORD 		{ public QName getQName() { return recordQName; } },
	DATASET		{ public QName getQName() { return datasetQName; } };
	
	QName metaDataQName = CSWConstants.NAMESPACE_ISO_METADATA;
	QName recordQName = CSWConstants.NAMESPACE_CSW_RECORD;
	QName datasetQName = CSWConstants.NAMESPACE_CSW_DATASET;
	
	public abstract QName getQName(); 
}
