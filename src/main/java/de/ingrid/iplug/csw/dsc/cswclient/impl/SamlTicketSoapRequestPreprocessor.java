package de.ingrid.iplug.csw.dsc.cswclient.impl;

import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.ingrid.iplug.csw.dsc.cswclient.CSWRequestPreprocessor;
import de.ingrid.iplug.csw.dsc.tools.StringUtils;

public class SamlTicketSoapRequestPreprocessor implements CSWRequestPreprocessor<SOAPMessage> {

    final static Log log = LogFactory.getLog(SamlTicketSoapRequestPreprocessor.class);

    private String soapHeaderTemplate = null;

    private SamlTicketProvider samlTicketProvider = null;

    @Override
    public SOAPMessage process(SOAPMessage param) {
        String samlTicket = samlTicketProvider.get();
        if (log.isDebugEnabled()) {
            log.debug("Got SAML Ticket: " + samlTicket);
        }

        try {

            soapHeaderTemplate = soapHeaderTemplate.replaceAll("\\$\\{SAML_TICKET\\}", samlTicket);
            SOAPHeader header = param.getSOAPPart().getEnvelope().getHeader();
            if (header == null) {
                header = param.getSOAPPart().getEnvelope().addHeader();
            }
            header.addChildElement((SOAPFactory.newInstance()).createElement(StringUtils.stringToDocument(soapHeaderTemplate).getDocumentElement()));
        } catch (Exception e) {
            log.error("Error adding SOAP header.", e);
        }

        return param;
    }

    public void setSoapHeaderTemplate(String soapHeaderTemplate) {
        this.soapHeaderTemplate = soapHeaderTemplate;
    }

    public void setSamlTicketProvider(SamlTicketProvider samlTicketProvider) {
        this.samlTicketProvider = samlTicketProvider;
    }

}
