/*
 * Copyright (c) 2008 wemove digital solutions. All rights reserved.
 */

package de.ingrid.iplug.csw.dsc.cswclient;

import org.w3c.dom.Document;

/**
 * Representation of a CSW Server's getCapabilities response that
 * describes the service metadata.
 * @author ingo herwig <ingo@wemove.com>
 */
public interface CSWCapabilities {
	
	/**
	 * Configure the CSWCapabilities instance.
	 * @param capDoc The capabilities document received from a CSW server
	 */
	public void configure(Document capDoc);
	
	/**
	 * Check if the CSW server supports the given operations
	 * @param operations An array of operation names
	 * @return boolean
	 */
	public boolean isSupportingOperations(String[] operations);

	/**
	 * Check if the CSW server supports iso profiles
	 * OpenGIS Catalogue Services Specification 2.0.2 - ISO Metadata Application Profile p.62:
	 * If the ‘operations constraint” ‘IsoProfiles’ (see 7.5) is included in the Capabilities 
	 * document the CSW server is identified as an ISO profile server.
	 * @return boolean
	 */
	public boolean isSupportingIsoProfiles();
}