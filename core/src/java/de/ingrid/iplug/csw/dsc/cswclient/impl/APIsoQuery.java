/*
 * Copyright (c) 2008 wemove digital solutions. All rights reserved.
 */

package de.ingrid.iplug.csw.dsc.cswclient.impl;

import de.ingrid.iplug.csw.dsc.cswclient.CSWConstants;
import de.ingrid.iplug.csw.dsc.cswclient.constants.TypeName;

/**
 * Implementation of the ap iso compatible query
 * @author ingo herwig <ingo@wemove.com>
 */
public class APIsoQuery extends GenericQuery {

	public APIsoQuery() {
		super();
		
		// set the default values
		this.setSchema(CSWConstants.NAMESPACE_CSW_2_0_2);
		this.outputSchema = CSWConstants.NAMESPACE_GMD;
		this.outputFormat = "application/xml";
		this.version = CSWConstants.VERSION_2_0_2;
		this.typeName = TypeName.MD_METADATA;
		this.constraintVersion = "1.1.0";
	}
}
