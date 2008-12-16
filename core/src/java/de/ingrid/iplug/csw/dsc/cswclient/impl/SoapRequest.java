/*
 * Copyright (c) 2008 wemove digital solutions. All rights reserved.
 */

package de.ingrid.iplug.csw.dsc.cswclient.impl;

//import javax.xml.namespace.QName;
import java.io.StringReader;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLStreamException;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.util.XMLUtils;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import de.ingrid.iplug.csw.dsc.cswclient.CSWConstants;
import de.ingrid.iplug.csw.dsc.cswclient.CSWQuery;
import de.ingrid.iplug.csw.dsc.cswclient.CSWRequest;
import de.ingrid.iplug.csw.dsc.cswclient.constants.Operation;

/**
 * OpenGIS Catalogue Services Specification 2.0.2 - ISO Metadata Application Profile p.91:
 * SOAP: Only SOAP messaging (via HTTP/POST) with document/literal style has to
 * be used. Messages must be compliant with SOAP 1.2 (http://www.w3.org/TR/SOAP/). 
 * The message payload will be in the body of the SOAP envelope.
 */
public class SoapRequest implements CSWRequest {

	@Override
	/**
	 * @see OpenGIS Catalogue Services Specification 2.0.2 - ISO Metadata Application Profile 8.2.1.1
	 */
	public Document doGetCapabilitiesRequest(String serverURL) throws Exception {

		/*
		<csw:GetCapabilities service="CSW" xmlns:csw="http://www.opengis.net/cat/csw" xmlns:ows="http://www.opengis.net/ows">
		    <ows:AcceptVersions>
		      <ows:Version>2.0.2</ows:Version>
		    </ows:AcceptVersions>
		    <ows:AcceptFormats>
		      <ows:OutputFormat>text/xml</ows:OutputFormat>
		    </ows:AcceptFormats>
	    </csw:GetCapabilities>
	    */

		// create the request
		OMFactory fac = OMAbstractFactory.getOMFactory();
		OMNamespace cswNs = fac.createOMNamespace(CSWConstants.NAMESPACE_CSW.getNamespaceURI(), 
				CSWConstants.NAMESPACE_CSW.getPrefix());
		OMNamespace owsNs = fac.createOMNamespace(CSWConstants.NAMESPACE_OWS.getNamespaceURI(),
				CSWConstants.NAMESPACE_OWS.getPrefix());

		// create method
		OMElement method = fac.createOMElement(Operation.GET_CAPABILITIES.toString(), cswNs);
		method.addAttribute("service", CSWConstants.SERVICE_TYPE, null);

		// create AcceptVersions element
		OMElement acceptVersions = fac.createOMElement("AcceptVersions", owsNs);
		OMElement version = fac.createOMElement("Version", owsNs);
		version.setText(CSWConstants.PREFERRED_VERSION);
		acceptVersions.addChild(version);

		// create AcceptFormats element
		OMElement acceptFormats = fac.createOMElement("AcceptFormats", owsNs);
		OMElement outputFormat = fac.createOMElement("OutputFormat", owsNs);
		outputFormat.setText("text/xml");
		acceptFormats.addChild(outputFormat);
		
		method.addChild(acceptVersions);
		method.addChild(acceptFormats);

		// send the request
		try {
			return sendRequest(serverURL, method);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	/**
	 * @see OpenGIS Catalogue Services Specification 2.0.2 - ISO Metadata Application Profile 8.2.2.1
	 */
	public Document doGetRecords(String serverURL, CSWQuery query) throws Exception {

		// create the request
		OMFactory fac = OMAbstractFactory.getOMFactory();
		OMNamespace cswNs = fac.createOMNamespace(
				CSWConstants.NAMESPACE_CSW.getNamespaceURI()+"/"+CSWConstants.PREFERRED_VERSION, 
				CSWConstants.NAMESPACE_CSW.getPrefix());
		
		// create method
		OMElement method = fac.createOMElement(Operation.GET_RECORDS.toString(), cswNs);
		// add the default parameters
		method.addAttribute("service", CSWConstants.SERVICE_TYPE, null);
		method.addAttribute("version", CSWConstants.PREFERRED_VERSION, null);
		method.addAttribute("outputFormat", "application/xml", null);
		// add the query specific parameters
		method.addAttribute("resultType", query.getResultType().toString(), null);
		method.addAttribute("outputSchema", query.getOutputSchema().getNamespaceURI(), null);

		// create Query element typename
		OMElement queryElem = fac.createOMElement("Query", cswNs);
		QName typeNameQN = query.getTypeNames().getQName();
		OMNamespace tnNs = fac.createOMNamespace(typeNameQN.getNamespaceURI(), typeNameQN.getPrefix());
		queryElem.addAttribute("typeNames", typeNameQN.getLocalPart(), tnNs);

		// create ElementSetName element typename
		OMElement elementSetName = fac.createOMElement("ElementSetName", cswNs);
		elementSetName.addAttribute("typeNames", typeNameQN.getLocalPart(), tnNs);
		elementSetName.setText(query.getElementSetName().toString());
		queryElem.addChild(elementSetName);

		// create Constraint
		OMElement constraintName = fac.createOMElement("Constraint", cswNs);
		constraintName.addAttribute("version", "1.1.0", null);
		queryElem.addChild(constraintName);
		
		// add the Filter
		OMElement filter = XMLUtils.toOM(query.getFilter().getDocumentElement());
		constraintName.addChild(filter);
		
		method.addChild(queryElem);

		// send the request
		try {
			return sendRequest(serverURL, method);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}	

	/**
	 * Send the given request to the server.
	 * @param serverURL
	 * @param payload
	 * @return Document
	 * @throws Exception 
	 */
	protected Document sendRequest(String serverURL, OMElement payload) throws Exception {
		// set up the client
		ServiceClient serviceClient;
		try {
			serviceClient = createClient(serverURL);
		} catch (AxisFault e) {
			throw new RuntimeException(e);
		}
		
		// send the request
		OMElement result = null;
		result = serviceClient.sendReceive(payload);
		System.out.println(serializeElement(result.cloneOMElement()));
		return convertToDOM(result);
	}

	/**
	 * Create a soap client for the given server url
	 * @param serverURL
	 * @return ServiceClient
	 * @throws AxisFault 
	 */
	protected ServiceClient createClient(String serverURL) throws AxisFault {
		// set up the client
		ServiceClient serviceClient = new ServiceClient();

		Options opts = new Options();
		opts.setTo(new EndpointReference(serverURL));
		opts.setProperty(HTTPConstants.CHUNKED, false);
		opts.setSoapVersionURI(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI);
		opts.setAction("urn:anonOutInOp");
		serviceClient.setOptions(opts);
		return serviceClient;
	}

	/**
	 * Get a string representation for an OMElement
	 * @param element
	 * @return String
	 * @throws XMLStreamException
	 */
	protected String serializeElement(OMElement element) throws XMLStreamException {
		return element.toStringWithConsume();
	}
	
	/**
	 * Convert an OMElement to a w3c DOM Document
	 * TODO: possible performance bottleneck
	 * @param element
	 * @return Document
	 * @throws Exception
	 */
	protected Document convertToDOM(OMElement element) throws Exception {
		String xmlString = serializeElement(element);
		
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = domFactory.newDocumentBuilder();
	    InputSource inStream = new InputSource();
	    inStream.setCharacterStream(new StringReader(xmlString));
	    return builder.parse(inStream);
	}
}
