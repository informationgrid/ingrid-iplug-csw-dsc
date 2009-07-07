/*
 * Copyright (c) 2008 wemove digital solutions. All rights reserved.
 */

package de.ingrid.iplug.csw.dsc.cswclient.impl;

import java.util.Arrays;
import java.util.Collection;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.ingrid.iplug.csw.dsc.cswclient.CSWCapabilities;
import de.ingrid.iplug.csw.dsc.cswclient.constants.Operation;
import de.ingrid.iplug.csw.dsc.tools.StringUtils;
import de.ingrid.iplug.csw.dsc.tools.XPathUtils;

public class GenericCapabilities implements CSWCapabilities {
	
	protected Document capDoc = null;

	@Override
	public void initialize(Document capDoc) {
		
		// check if capDoc is a valid capabilities document
		Node rootNode = XPathUtils.getNode(capDoc, "Capabilities");
		if (rootNode != null) {
			this.capDoc = capDoc;
		}
		else {
			// check if capDoc is an ExceptionReport 
			String exStr = "The returned document is not a Capabilities document";
			Node exNode = XPathUtils.getNode(capDoc, "ExceptionReport/Exception/ExceptionText");
			if (exNode != null) {
				exStr += ": "+exNode.getTextContent();
			}
			throw new RuntimeException(exStr);
		}
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

	@Override
	public String getOperationUrl(Operation op) {
		
		// extract the operation url from the OperationsMetadata element
		NodeList postNodes = XPathUtils.getNodeList(capDoc, "Capabilities/OperationsMetadata/Operation[@name='"+op+"']/DCP/HTTP/Post");
		
		// if there are multiple POST nodes, we choose the one with the SOAP PostEncoding constraint
		Node postNode = null;
		if (postNodes.getLength() > 1) {
			postNode = XPathUtils.getNode(capDoc, "Capabilities/OperationsMetadata/Operation[@name='"+op+"']/DCP/HTTP/Post[Constraint[@name='PostEncoding']/Value='SOAP']");
		}

		if (postNode != null) {
			// we search for an attribute named ...href (this seems to be the most robust way, because we can't be sure that 
			// the ns prefix is always xlink and that the namespace is recognised correctly using the getNamedItemNS method)
			NamedNodeMap nodeAttributes = postNode.getAttributes();
			Node hrefNode = null;
			for (int i=0; i<nodeAttributes.getLength(); i++) {
				if (nodeAttributes.item(i).getNodeName().endsWith("href")) {
					hrefNode = nodeAttributes.item(i);
					break;
				}
			}
			if (hrefNode != null) {
				return hrefNode.getNodeValue();
			}
		}
		return null;
	}
	
	@Override
	public String toString() {
		return StringUtils.nodeToString(capDoc);
	}
}
