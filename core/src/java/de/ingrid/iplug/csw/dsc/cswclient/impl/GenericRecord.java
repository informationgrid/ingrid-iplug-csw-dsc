/*
 * Copyright (c) 2008 wemove digital solutions. All rights reserved.
 */

package de.ingrid.iplug.csw.dsc.cswclient.impl;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.ingrid.iplug.csw.dsc.cswclient.CSWRecord;
import de.ingrid.iplug.csw.dsc.cswclient.constants.ElementSetName;
import de.ingrid.iplug.csw.dsc.tools.StringUtils;
import de.ingrid.iplug.csw.dsc.tools.XPathUtils;

public class GenericRecord implements CSWRecord {

	protected String id = null;
	protected ElementSetName elementSetName = null;
	protected Node node = null;
	
	@Override
	public void initialize(ElementSetName elementSetName, Node node) throws Exception{
		this.node = node;
		this.elementSetName = elementSetName;

		// get the record id
		NodeList idNodes = XPathUtils.getNodeList(node, "//fileIdentifier/CharacterString");
		if (idNodes == null)
			throw new RuntimeException("CSWRecord does not contain an id (looking for //fileIdentifier/CharacterString):\n"+StringUtils.nodeToString(node));
		if (idNodes.getLength() > 1)
			throw new RuntimeException("CSWRecord contains more than one id (looking for //fileIdentifier/CharacterString):\n"+StringUtils.nodeToString(node));
		
		this.id = idNodes.item(0).getTextContent();
	}

	@Override
	public String getId() {
		if (this.id != null) {
			return this.id;
		}
		else
			throw new RuntimeException("CSWRecord is not initialized properly. Make sure to call CSWRecord.initialize.");
	}

	@Override
	public ElementSetName getElementSetName() {
		if (this.elementSetName != null) {
			return this.elementSetName;
		}
		else
			throw new RuntimeException("CSWRecord is not initialized properly. Make sure to call CSWRecord.initialize.");
	}

	@Override
	public  Node getOriginalResponse() {
		if (this.node != null) {
			return this.node;
		}
		else
			throw new RuntimeException("CSWRecord is not initialized properly. Make sure to call CSWRecord.initialize.");
	}
}
