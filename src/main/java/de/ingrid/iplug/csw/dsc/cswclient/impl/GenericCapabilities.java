/*
 * **************************************************-
 * ingrid-iplug-csw-dsc:war
 * ==================================================
 * Copyright (C) 2014 - 2025 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.2 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
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
import de.ingrid.utils.xml.Csw202NamespaceContext;
import de.ingrid.utils.xpath.XPathUtils;

public class GenericCapabilities implements CSWCapabilities {

    protected Document capDoc = null;

    final private XPathUtils xPathUtils = new XPathUtils(new Csw202NamespaceContext());

    @Override
    public void initialize(Document capDoc) {

        // check if capDoc is a valid capabilities document
        Node rootNode = xPathUtils.getNode(capDoc, "//csw:Capabilities");
        if (rootNode != null) {
            this.capDoc = capDoc;
        } else {
            // check if capDoc is an ExceptionReport
            String exStr = "The returned document is not a Capabilities document. Node 'Capabilities' could not be found.";
            Node exNode = xPathUtils.getNode(capDoc, "ows:ExceptionReport/ows:Exception/ows:ExceptionText");
            if (exNode != null) {
                exStr += ": " + exNode.getTextContent();
            }
            throw new RuntimeException(exStr);
        }
    }

    @Override
    public boolean isSupportingOperations(String[] operations) {

        int supportingOperations = 0;

        NodeList operationNodes = xPathUtils.getNodeList(capDoc,
                "//csw:Capabilities/ows:OperationsMetadata/ows:Operation[@name]");
        // compare supported operations with requested
        if (operationNodes != null) {
            Collection<String> requestedOperations = Arrays.asList(operations);
            for (int i = 0; i < operationNodes.getLength(); i++) {
                String curName = operationNodes.item(i).getAttributes().getNamedItem("name").getNodeValue();
                if (requestedOperations.contains(curName))
                    supportingOperations++;
            }
        }
        return supportingOperations == operations.length;
    }

    @Override
    public boolean isSupportingIsoProfiles() {
        NodeList constraintNodes = xPathUtils.getNodeList(capDoc,
                "//csw:Capabilities/ows:OperationsMetadata/ows:Operation/ows:Constraint[@name='IsoProfiles']");
        return constraintNodes.getLength() > 0;
    }

    @Override
    public String getOperationUrl(Operation op) {

        // extract the operation url from the OperationsMetadata element
        NodeList postNodes = xPathUtils
                .getNodeList(capDoc, "//csw:Capabilities/ows:OperationsMetadata/ows:Operation[@name='" + op
                        + "']/ows:DCP/ows:HTTP/ows:Post");

        // if there are multiple POST nodes, we choose the one with the SOAP
        // PostEncoding constraint
        Node postNode = null;
        if (postNodes.getLength() > 1) {
            postNode = xPathUtils.getNode(capDoc, "//csw:Capabilities/ows:OperationsMetadata/ows:Operation[@name='"
                    + op + "']/ows:DCP/ows:HTTP/ows:Post[ows:Constraint[@name='PostEncoding']/ows:Value='SOAP']");
        }

        if (postNode != null) {
            // we search for an attribute named ...href (this seems to be the
            // most robust way, because we can't be sure that
            // the ns prefix is always xlink and that the namespace is
            // recognised correctly using the getNamedItemNS method)
            NamedNodeMap nodeAttributes = postNode.getAttributes();
            Node hrefNode = null;
            for (int i = 0; i < nodeAttributes.getLength(); i++) {
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
