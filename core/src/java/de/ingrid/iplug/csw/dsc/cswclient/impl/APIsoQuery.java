/*
 * Copyright (c) 2008 wemove digital solutions. All rights reserved.
 */

package de.ingrid.iplug.csw.dsc.cswclient.impl;

import javax.xml.namespace.QName;

import de.ingrid.iplug.csw.dsc.cswclient.CSWConstants;
import de.ingrid.iplug.csw.dsc.cswclient.constants.TypeName;

/**
 * Implementation of the ap iso compatible query
 * @author ingo herwig <ingo@wemove.com>
 */
public class APIsoQuery extends AbstractQuery {

	public APIsoQuery() {
		super();
		
		// set the default values
		this.outputSchema = CSWConstants.NAMESPACE_GMD;
		this.typeName = TypeName.MD_METADATA;
	}

	@Override
	public void setOutputSchema(QName schema) {
		// ignore
	}

	@Override
	public void setTypeNames(TypeName typeName) {
		// ignore
	}
}
