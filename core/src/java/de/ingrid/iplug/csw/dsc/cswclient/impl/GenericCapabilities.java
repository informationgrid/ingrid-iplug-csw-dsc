/*
 * Copyright (c) 2008 wemove digital solutions. All rights reserved.
 */

package de.ingrid.iplug.csw.dsc.cswclient.impl;

import java.util.Arrays;
import java.util.Collection;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import de.ingrid.iplug.csw.dsc.cswclient.CSWCapabilities;
import de.ingrid.iplug.csw.dsc.tools.XPathUtils;

public class GenericCapabilities implements CSWCapabilities {
	
	protected Document capDoc = null;

	@Override
	public void initialize(Document capDoc) {
		this.capDoc = capDoc;
	}
	
	@Override
	public boolean isSupportingOperations(String[] operations) {

		int supportingOperations = 0;
		
		NodeList operationNodes = XPathUtils.getNodeList(capDoc, "Capabilities/OperationsMetadata/Operation[@name]");
		// compare supported operations with requested
		if (operationNodes != null) {
			Collection<String> requestedOperations = Arrays.asList(operations);
			for (int i=0; i<operationNodes.getLength(); i++) {
				String curName = operationNodes.item(i).getAttributes().getNamedItem("name").getNodeValue();
				if (requestedOperations.contains(curName))
					supportingOperations++;
			}
		}
		return supportingOperations == operations.length;
	}

	@Override
	public boolean isSupportingIsoProfiles() {
		NodeList constraintNodes = XPathUtils.getNodeList(capDoc, "Capabilities/OperationsMetadata/Operation/Constraint[@name='IsoProfiles']");
		return constraintNodes.getLength() > 0;
	}
}
