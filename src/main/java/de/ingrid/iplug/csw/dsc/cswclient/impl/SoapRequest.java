/*
 * Copyright (c) 2008 wemove digital solutions. All rights reserved.
 */

package de.ingrid.iplug.csw.dsc.cswclient.impl;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;

import de.ingrid.iplug.csw.dsc.cswclient.CSWConstants;
import de.ingrid.iplug.csw.dsc.cswclient.CSWQuery;
import de.ingrid.iplug.csw.dsc.cswclient.CSWRequest;
import de.ingrid.iplug.csw.dsc.cswclient.CSWRequestPreprocessor;
import de.ingrid.iplug.csw.dsc.cswclient.constants.Namespace;
import de.ingrid.iplug.csw.dsc.cswclient.constants.Operation;
import de.ingrid.iplug.csw.dsc.cswclient.constants.OutputFormat;
import de.ingrid.iplug.csw.dsc.cswclient.constants.TypeName;
import de.ingrid.iplug.csw.dsc.tools.StringUtils;

/**
 * OpenGIS Catalogue Services Specification 2.0.2 - ISO Metadata Application
 * Profile p.91: SOAP: Only SOAP messaging (via HTTP/POST) with document/literal
 * style has to be used. Messages must be compliant with SOAP 1.2
 * (http://www.w3.org/TR/SOAP/). The message payload will be in the body of the
 * SOAP envelope.
 */
public class SoapRequest implements CSWRequest {

    final protected static Log log = LogFactory.getLog(CSWRequest.class);

    private CSWRequestPreprocessor<SOAPMessage> preProcessor = null;

    /**
     * CSWRequest implementation
     */

    /**
     * @see OpenGIS Catalogue Services Specification 2.0.2 - ISO Metadata
     *      Application Profile 8.2.1.1
     */
    @Override
    public Document doGetCapabilitiesRequest(String serverURL) throws Exception {

        SOAPMessage soapMessage = createSoapMessage();
        SOAPPart soapPart = soapMessage.getSOAPPart();

        // SOAP Envelope
        SOAPEnvelope envelope = soapPart.getEnvelope();
        envelope.addNamespaceDeclaration(Namespace.CSW_2_0_2.getQName().getPrefix(), Namespace.CSW_2_0_2.getQName().getNamespaceURI());
        envelope.addNamespaceDeclaration(Namespace.OWS.getQName().getPrefix(), Namespace.OWS.getQName().getNamespaceURI());

        // SOAP Body
        SOAPBody soapBody = envelope.getBody();
        SOAPElement method = soapBody.addChildElement(Operation.GET_CAPABILITIES.toString(), Namespace.CSW_2_0_2.getQName().getPrefix());
        method.addAttribute(new QName("service"), CSWConstants.SERVICE_TYPE);

        // create AcceptVersions element
        method.addChildElement("AcceptVersions", Namespace.OWS.getQName().getPrefix()).addChildElement("Version", Namespace.OWS.getQName().getPrefix()).addTextNode(CSWConstants.PREFERRED_VERSION);

        // create AcceptFormats element
        method.addChildElement("AcceptFormats", Namespace.OWS.getQName().getPrefix()).addChildElement("OutputFormat", Namespace.OWS.getQName().getPrefix()).addTextNode(OutputFormat.APPLICATION_XML.toString());

        soapMessage.saveChanges();

        // send the request
        try {
            return sendRequest(serverURL, soapMessage);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @see OpenGIS Catalogue Services Specification 2.0.2 - ISO Metadata
     *      Application Profile 8.2.2.1
     */
    @Override
    public Document doGetRecords(String serverURL, CSWQuery query) throws Exception {

        SOAPMessage soapMessage = createSoapMessage();
        SOAPPart soapPart = soapMessage.getSOAPPart();

        // SOAP Envelope
        SOAPEnvelope envelope = soapPart.getEnvelope();
        envelope.addNamespaceDeclaration(query.getSchema().getQName().getPrefix(), query.getSchema().getQName().getNamespaceURI());

        // SOAP Body
        SOAPBody soapBody = envelope.getBody();
        SOAPElement method = soapBody.addChildElement(Operation.GET_RECORDS.toString(), query.getSchema().getQName().getPrefix());
        method.addNamespaceDeclaration(Namespace.ISO.getQName().getPrefix(), Namespace.ISO.getQName().getNamespaceURI());
        method.addNamespaceDeclaration(Namespace.GML.getQName().getPrefix(), Namespace.GML.getQName().getNamespaceURI());
        method.addAttribute(new QName("service"), CSWConstants.SERVICE_TYPE);
        method.addAttribute(new QName("version"), query.getVersion());
        method.addAttribute(new QName("outputFormat"), query.getOutputFormat().toString());
        method.addAttribute(new QName("resultType"), query.getResultType().toString());
        method.addAttribute(new QName("startPosition"), Integer.toString(query.getStartPosition()));
        method.addAttribute(new QName("maxRecords"), Integer.toString(query.getMaxRecords()));

        QName outputSchemaQN = query.getOutputSchema().getQName();
        method.addNamespaceDeclaration(outputSchemaQN.getPrefix(), outputSchemaQN.getNamespaceURI());
        if (outputSchemaQN.getLocalPart().length() > 0)
            method.addAttribute(new QName("outputSchema"), outputSchemaQN.getPrefix() + ":" + outputSchemaQN.getLocalPart());
        else
            method.addAttribute(new QName("outputSchema"), outputSchemaQN.getNamespaceURI());

        // create Query element typename
        SOAPElement queryElem = method.addChildElement("Query", query.getSchema().getQName().getPrefix());

        // add typeNames attribute
        List<String> typeNames = new ArrayList<String>();
        for (TypeName typeName : query.getTypeNames()) {
            QName typeNameQN = typeName.getQName();
            method.addNamespaceDeclaration(typeNameQN.getPrefix(), typeNameQN.getNamespaceURI());
            typeNames.add(typeNameQN.getPrefix() + ":" + typeNameQN.getLocalPart());
        }
        String typeNamesValue = StringUtils.join(typeNames.toArray(), ",");
        queryElem.addAttribute(new QName("typeNames"), typeNamesValue);

        // create ElementSetName element typename
        queryElem.addChildElement("ElementSetName", query.getSchema().getQName().getPrefix()).setTextContent(query.getElementSetName().toString());

        // add the Filter
        if (query.getConstraint() != null) {
            // create Constraint
            // make sure the constraint element is only created when
            // we have a filter.
            SOAPElement constraint = queryElem.addChildElement("Constraint", query.getSchema().getQName().getPrefix());
            constraint.addAttribute(new QName("version"), query.getConstraintLanguageVersion());
            constraint.addChildElement((SOAPFactory.newInstance()).createElement(query.getConstraint().getDocumentElement()));
        }

        soapMessage.saveChanges();

        // send the request
        try {
            return sendRequest(serverURL, soapMessage);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @see OpenGIS Catalogue Services Specification 2.0.2 - ISO Metadata
     *      Application Profile 8.2.2.2
     */
    @Override
    public Document doGetRecordById(String serverURL, CSWQuery query) throws Exception {

        SOAPMessage soapMessage = createSoapMessage();
        SOAPPart soapPart = soapMessage.getSOAPPart();

        // SOAP Envelope
        SOAPEnvelope envelope = soapPart.getEnvelope();
        envelope.addNamespaceDeclaration(query.getSchema().getQName().getPrefix(), query.getSchema().getQName().getNamespaceURI());

        // SOAP Body
        SOAPBody soapBody = envelope.getBody();
        SOAPElement method = soapBody.addChildElement(Operation.GET_RECORD_BY_ID.toString(), Namespace.CSW_2_0_2.getQName().getPrefix());
        method.addAttribute(new QName("service"), CSWConstants.SERVICE_TYPE);
        method.addAttribute(new QName("version"), query.getVersion());
        method.addAttribute(new QName("outputFormat"), query.getOutputFormat().toString());

        QName outputSchemaQN = query.getOutputSchema().getQName();
        method.addNamespaceDeclaration(outputSchemaQN.getPrefix(), outputSchemaQN.getNamespaceURI());
        if (outputSchemaQN.getLocalPart().length() > 0)
            method.addAttribute(new QName("outputSchema"), outputSchemaQN.getPrefix() + ":" + outputSchemaQN.getLocalPart());
        else
            method.addAttribute(new QName("outputSchema"), outputSchemaQN.getNamespaceURI());

        // create Id
        method.addChildElement("Id", query.getSchema().getQName().getPrefix()).addTextNode(query.getId());

        method.addChildElement("ElementSetName", query.getSchema().getQName().getPrefix()).addTextNode(query.getElementSetName().toString());

        soapMessage.saveChanges();

        // send the request
        try {
            return sendRequest(serverURL, soapMessage);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void setPreProcessor(CSWRequestPreprocessor<SOAPMessage> preProcessor) {
        this.preProcessor = preProcessor;
    }

    /**
     * Helper methods
     */

    /**
     * Send the given request to the server.
     * 
     * @param serverURL
     * @param soapRequest
     * @return Document
     * @throws Exception
     */
    protected Document sendRequest(String serverURL, SOAPMessage soapRequest) throws Exception {

        Document doc = null;

        SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
        SOAPConnection soapConnection = soapConnectionFactory.createConnection();

        // send the request
        if (log.isDebugEnabled()) {
            log.debug("Request SOAP Message: " + StringUtils.soapMessageTostring(soapRequest));
        }

        if (preProcessor != null) {
            preProcessor.process(soapRequest);
        }

        // Send SOAP Message to SOAP Server
        SOAPMessage soapResponse = soapConnection.call(soapRequest, serverURL);

        if (log.isDebugEnabled()) {
            log.debug("Response SOAP Message: " + StringUtils.soapMessageTostring(soapResponse));
        }

        doc = soapResponse.getSOAPPart();
        soapConnection.close();

        return doc;
    }

    private SOAPMessage createSoapMessage() throws SOAPException {
        MessageFactory messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
        SOAPMessage soapMessage = messageFactory.createMessage();
        return soapMessage;
    }

}
