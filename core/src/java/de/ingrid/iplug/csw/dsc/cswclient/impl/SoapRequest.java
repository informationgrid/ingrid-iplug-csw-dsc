/*
 * Copyright (c) 2008 wemove digital solutions. All rights reserved.
 */

package de.ingrid.iplug.csw.dsc.cswclient.impl;

//import javax.xml.namespace.QName;
import java.io.StringReader;

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
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import de.ingrid.iplug.csw.dsc.cswclient.CSWConstants;
import de.ingrid.iplug.csw.dsc.cswclient.CSWRequest;

/**
 * OpenGIS® Catalogue Services Specification 2.0.2 - ISO Metadata Application Profile p.91:
 * SOAP: Only SOAP messaging (via HTTP/POST) with document/literal style has to
 * be used. Messages must be compliant with SOAP 1.2 (http://www.w3.org/TR/SOAP/). 
 * The message payload will be in the body of the SOAP envelope.
 */
public class SoapRequest implements CSWRequest {

	@Override
	public Document doGetCapabilitiesRequest(String serverURL) throws Exception {

		// set up the client
		ServiceClient serviceClient;
		try {
			serviceClient = new ServiceClient();
		} catch (AxisFault e) {
			throw new RuntimeException(e);
		}

		Options opts = new Options();
		opts.setTo(new EndpointReference(serverURL));
		opts.setProperty(HTTPConstants.CHUNKED, false);
		opts.setSoapVersionURI(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI);
		opts.setAction("urn:anonOutInOp");
		serviceClient.setOptions(opts);
		
		/*
		<csw:GetCapabilities service="CSW" xmlns:csw="http://www.opengis.net/cat/csw" xmlns:ows="http://www.opengis.net/ows">
		    <ows:AcceptVersions>
		      <ows:Version>2.0.1</ows:Version>
		    </ows:AcceptVersions>
		    <ows:AcceptFormats>
		      <ows:OutputFormat>text/xml</ows:OutputFormat>
		    </ows:AcceptFormats>
	    </csw:GetCapabilities>
	    */

		// create the request
		OMFactory fac = OMAbstractFactory.getOMFactory();
		OMNamespace cswNs = fac.createOMNamespace(CSWConstants.NAMESPACE_CSW_URL, CSWConstants.NAMESPACE_CSW_PREFIX);
		OMNamespace owsNs = fac.createOMNamespace(CSWConstants.NAMESPACE_OWS_URL, CSWConstants.NAMESPACE_OWS_PREFIX);
		
		OMElement method = fac.createOMElement(CSWConstants.OP_GET_CAPABILITIES, cswNs);
		method.addAttribute("service", CSWConstants.SERVICE_TYPE, null);

		OMElement acceptVersions = fac.createOMElement("AcceptVersions", owsNs);
		OMElement version = fac.createOMElement("Version", owsNs);
		version.setText(CSWConstants.PREFERRED_VERSION);
		acceptVersions.addChild(version);

		OMElement acceptFormats = fac.createOMElement("AcceptFormats", owsNs);
		OMElement outputFormat = fac.createOMElement("OutputFormat", owsNs);
		outputFormat.setText("text/xml");
		acceptFormats.addChild(outputFormat);
		
		method.addChild(acceptVersions);
		method.addChild(acceptFormats);

		// send the request
		try {
			OMElement result = null;
			result = serviceClient.sendReceive(method);
			return convertToDOM(result);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	protected String serializeElement(OMElement element) throws XMLStreamException {
		return element.toStringWithConsume();
	}
	
	protected Document convertToDOM(OMElement element) throws Exception {
		String xmlString = serializeElement(element);
		
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = domFactory.newDocumentBuilder();
	    InputSource inStream = new InputSource();
	    inStream.setCharacterStream(new StringReader(xmlString));
	    return builder.parse(inStream);
	}	
}
