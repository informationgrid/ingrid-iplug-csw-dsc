/*
 * Copyright (c) 2008 wemove digital solutions. All rights reserved.
 */

package de.ingrid.iplug.csw.dsc.cswclient.impl;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import de.ingrid.iplug.csw.dsc.cswclient.CSWRecord;
import de.ingrid.iplug.csw.dsc.tools.XPathUtils;

public class GenericRecord implements CSWRecord {

	protected Document document = null;
	protected String id = "";
	
	@Override
	public void configure(Document document) throws Exception{
		this.document = document;

		// parse the document and get the record id
		Node recordNode = XPathUtils.getNode(document, "//fileIdentifier/CharacterString");
		if (recordNode != null) {
			this.setId(recordNode.getTextContent());
		}
	}

	@Override
	public Document getOriginalResponse() {
		return this.document;
	}
	
	@Override
	public void setId(String id) {
		this.id = id;
	}

	@Override
	public String getId() {
		return this.id;
	}
}
