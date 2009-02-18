/*
 * Copyright (c) 2008 wemove digital solutions. All rights reserved.
 */

package de.ingrid.iplug.csw.dsc.cswclient;

import org.w3c.dom.Document;

import de.ingrid.iplug.csw.dsc.cswclient.constants.Operation;

/**
 * Representation of a CSW Server's getCapabilities response that
 * describes the service metadata.
 * TODO: add methods to get the parameters of a getRecords request, to check the configuration against
 * @author ingo herwig <ingo@wemove.com>
 */
public interface CSWCapabilities {
	
	/**
	 * Initialize the CSWCapabilities instance.
	 * @param capDoc The capabilities document received from a CSW server
	 */
	public void initialize(Document capDoc);
	
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

	/**
	 * Get the SOAP URL for a given operation from the appropriate OperationsMetadata/Operation/DCP/HTTP/POST 
	 * element if defined in the capabilities document.
	 * @param op The operation
	 * @return The URL String or null of no SOAP URL is defined
	 */
	public String getOperationUrl(Operation op);
	
	/**
	 * Get a String representation of the Capabilities document
	 * @return String
	 */
	public String toString();
}