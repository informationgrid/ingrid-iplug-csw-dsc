/*
 * Copyright (c) 2008 wemove digital solutions. All rights reserved.
 */

package de.ingrid.iplug.csw.dsc.cswclient;

import org.w3c.dom.Node;

import de.ingrid.iplug.csw.dsc.cswclient.constants.ElementSetName;

/**
 * Representation of a record returned by a CSW server.
 * @author ingo herwig <ingo@wemove.com>
 */
public interface CSWRecord {

	/**
	 * Initialize the record.
	 * param elementSetName
	 * param node
	 */
	public void initialize(ElementSetName elementSetName, Node node) throws Exception;
	
	/**
	 * Get the id of the record
	 * @return String
	 */
	public String getId();

	/**
	 * Get the elementset name of the record
	 * @return ElementSetName
	 */
	public ElementSetName getElementSetName();

	/**
	 * Get the original response document
	 * @return Node
	 */
	public Node getOriginalResponse();	
	
}
