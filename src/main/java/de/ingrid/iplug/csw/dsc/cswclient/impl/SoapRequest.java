/*
 * **************************************************-
 * ingrid-iplug-csw-dsc:war
 * ==================================================
 * Copyright (C) 2014 - 2022 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.1 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl5
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

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import de.ingrid.iplug.csw.dsc.Configuration;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.util.XMLUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
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

    private CSWRequestPreprocessor<ServiceClient> preProcessor = null;

    @Autowired
    private Configuration cswConfig;

    /**
     * CSWRequest implementation
     */

    /**
     * OpenGIS Catalogue Services Specification 2.0.2 - ISO Metadata
     *      Application Profile 8.2.1.1
     */
    @Override
    public Document doGetCapabilitiesRequest(String serverURL) throws Exception {

        // create the request
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace cswNs = fac.createOMNamespace(Namespace.CSW_2_0_2.getQName().getNamespaceURI(), Namespace.CSW_2_0_2
                .getQName().getPrefix());
        OMNamespace owsNs = fac.createOMNamespace(Namespace.OWS.getQName().getNamespaceURI(), Namespace.OWS.getQName()
                .getPrefix());

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
        outputFormat.setText(OutputFormat.APPLICATION_XML.toString());
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

    /**
     * OpenGIS Catalogue Services Specification 2.0.2 - ISO Metadata
     *      Application Profile 8.2.2.1
     */
    @Override
    public Document doGetRecords(String serverURL, CSWQuery query) throws Exception {

        // create the request
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace cswNs = fac.createOMNamespace(query.getSchema().getQName().getNamespaceURI(), query.getSchema()
                .getQName().getPrefix());

        // create method
        OMElement method = fac.createOMElement(Operation.GET_RECORDS.toString(), cswNs);
        method.declareNamespace(Namespace.ISO.getQName().getNamespaceURI(), Namespace.ISO.getQName().getPrefix());
        method.declareNamespace(Namespace.GML.getQName().getNamespaceURI(), Namespace.GML.getQName().getPrefix());

        // add the default parameters
        method.addAttribute("service", CSWConstants.SERVICE_TYPE, null);

        // add the query specific parameters
        method.addAttribute("version", query.getVersion(), null);
        method.addAttribute("outputFormat", query.getOutputFormat().toString(), null);
        method.addAttribute("resultType", query.getResultType().toString(), null);
        method.addAttribute("startPosition", Integer.toString(query.getStartPosition()), null);
        method.addAttribute("maxRecords", Integer.toString(query.getMaxRecords()), null);

        QName outputSchemaQN = query.getOutputSchema().getQName();
        method.declareNamespace(outputSchemaQN.getNamespaceURI(), outputSchemaQN.getPrefix());
        if (outputSchemaQN.getLocalPart().length() > 0)
            method.addAttribute("outputSchema", outputSchemaQN.getPrefix() + ":" + outputSchemaQN.getLocalPart(), null);
        else
            method.addAttribute("outputSchema", outputSchemaQN.getNamespaceURI(), null);

        // create Query element typename
        OMElement queryElem = fac.createOMElement("Query", cswNs);
        // add typeNames attribute
        List<String> typeNames = new ArrayList<String>();
        for (TypeName typeName : query.getTypeNames()) {
            QName typeNameQN = typeName.getQName();
            method.declareNamespace(typeNameQN.getNamespaceURI(), typeNameQN.getPrefix());
            typeNames.add(typeNameQN.getPrefix() + ":" + typeNameQN.getLocalPart());
        }
        String typeNamesValue = StringUtils.join(typeNames.toArray(), ",");
        queryElem.addAttribute("typeNames", typeNamesValue, null);

        // create ElementSetName element typename
        OMElement elementSetName = fac.createOMElement("ElementSetName", cswNs);
        elementSetName.setText(query.getElementSetName().toString());
        queryElem.addChild(elementSetName);

        // add the Filter
        if (query.getConstraint() != null) {
            // create Constraint
            // make sure the constraint element is only created when
            // we have a filter.
            OMElement constraint = fac.createOMElement("Constraint", cswNs);
            constraint.addAttribute("version", query.getConstraintLanguageVersion(), null);
            queryElem.addChild(constraint);
            OMElement filter = XMLUtils.toOM(query.getConstraint().getDocumentElement());
            constraint.addChild(filter);
        }

        method.addChild(queryElem);

        // send the request
        try {
            return sendRequest(serverURL, method);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * OpenGIS Catalogue Services Specification 2.0.2 - ISO Metadata
     *      Application Profile 8.2.2.2
     */
    @Override
    public Document doGetRecordById(String serverURL, CSWQuery query) throws Exception {

        // create the request
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace cswNs = fac.createOMNamespace(query.getSchema().getQName().getNamespaceURI(), query.getSchema()
                .getQName().getPrefix());

        // create method
        OMElement method = fac.createOMElement(Operation.GET_RECORD_BY_ID.toString(), cswNs);

        // add the default parameters
        method.addAttribute("service", CSWConstants.SERVICE_TYPE, null);

        // add the query specific parameters
        method.addAttribute("version", query.getVersion(), null);
        method.addAttribute("outputFormat", query.getOutputFormat().toString(), null);

        QName outputSchemaQN = query.getOutputSchema().getQName();
        method.declareNamespace(outputSchemaQN.getNamespaceURI(), outputSchemaQN.getPrefix());
        if (outputSchemaQN.getLocalPart().length() > 0)
            method.addAttribute("outputSchema", outputSchemaQN.getPrefix() + ":" + outputSchemaQN.getLocalPart(), null);
        else
            method.addAttribute("outputSchema", outputSchemaQN.getNamespaceURI(), null);

        // create Id
        OMElement idNode = fac.createOMElement("Id", cswNs);
        idNode.setText(query.getId());
        method.addChild(idNode);

        // create ElementSetName element typename
        OMElement elementSetName = fac.createOMElement("ElementSetName", cswNs);
        elementSetName.setText(query.getElementSetName().toString());
        method.addChild(elementSetName);

        // send the request
        try {
            return sendRequest(serverURL, method);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void setPreProcessor(CSWRequestPreprocessor<ServiceClient> preProcessor) {
        this.preProcessor = preProcessor;
    }

    /**
     * Helper methods
     */

    /**
     * Create a soap client for the given server url
     * 
     * @param serverURL
     * @return ServiceClient
     * @throws AxisFault
     */
    protected ServiceClient createClient(String serverURL) throws AxisFault, Exception {
        // set up the client
        ConfigurationContext configContext = ConfigurationContextFactory
                .createConfigurationContextFromFileSystem((new ClassPathResource("axis2.xml")).getURI().getPath());
        ServiceClient serviceClient = new ServiceClient(configContext, null);

        Options opts = new Options();
        opts.setTo(new EndpointReference(serverURL));
        opts.setProperty(org.apache.axis2.kernel.http.HTTPConstants.CHUNKED, false);
        opts.setProperty(org.apache.axis2.Constants.Configuration.CHARACTER_SET_ENCODING, "UTF-8");
        opts.setTimeOutInMilliSeconds(cswConfig.httpReadTimeout);
        /*
         * opts.setProperty(org.apache.axis2.transport.http.HTTPConstants.HTTP_PROTOCOL_VERSION
         * , org.apache.axis2.transport.http.HTTPConstants.HEADER_PROTOCOL_10);
         */
        opts.setSoapVersionURI(org.apache.axiom.soap.SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI);
        // opts.setSoapVersionURI(org.apache.axiom.soap.SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI);
        opts.setAction("urn:anonOutInOp");
        serviceClient.setOptions(opts);

        return serviceClient;
    }

    /**
     * Send the given request to the server.
     * 
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
        if (log.isDebugEnabled()) {
            log.debug("Request URL: " + serverURL);
            log.debug("Request: " + serializeElement(payload.cloneOMElement()));
        }
        OMElement result = null;
        if (preProcessor != null) {
            serviceClient = preProcessor.process(serviceClient);
        }
        result = serviceClient.sendReceive(payload);
        if (log.isDebugEnabled())
            log.debug("Response: " + serializeElement(result.cloneOMElement()));
        Document doc = convertToDOM(result);
        serviceClient.cleanupTransport();
        serviceClient.cleanup();
        serviceClient.getServiceContext().getConfigurationContext().terminate();
        return doc;
    }

    /**
     * Get a string representation for an OMElement
     * 
     * @param element
     * @return String
     * @throws XMLStreamException
     */
    protected String serializeElement(OMElement element) throws XMLStreamException {
        return element.toStringWithConsume();
    }

    /**
     * Convert an OMElement to a w3c DOM Document TODO: possible performance
     * bottleneck
     * 
     * @param element
     * @return Document
     * @throws Exception
     */
    protected Document convertToDOM(OMElement element) throws Exception {
        String xmlString = serializeElement(element);
        return StringUtils.stringToDocument(xmlString);
    }
}
