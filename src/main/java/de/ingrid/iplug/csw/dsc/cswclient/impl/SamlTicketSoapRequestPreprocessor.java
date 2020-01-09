/*
 * **************************************************-
 * ingrid-iplug-csw-dsc:war
 * ==================================================
 * Copyright (C) 2014 - 2020 wemove digital solutions GmbH
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
package de.ingrid.iplug.csw.dsc.cswclient.impl;

import org.apache.axiom.om.impl.llom.util.AXIOMUtil;
import org.apache.axis2.client.ServiceClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.ingrid.iplug.csw.dsc.cswclient.CSWRequestPreprocessor;

public class SamlTicketSoapRequestPreprocessor implements CSWRequestPreprocessor<ServiceClient> {

    final static Log log = LogFactory.getLog(SamlTicketSoapRequestPreprocessor.class);

    private String soapHeaderTemplate = null;

    private SamlTicketProvider samlTicketProvider = null;

    @Override
    public ServiceClient process(ServiceClient param) {
        String samlTicket = samlTicketProvider.get();
        if (log.isDebugEnabled()) {
            log.debug("Got SAML Ticket: " + samlTicket);
        }

        try {

            soapHeaderTemplate = soapHeaderTemplate.replaceAll("\\$\\{SAML_TICKET\\}", samlTicket);

            param.addHeader(AXIOMUtil.stringToOM(soapHeaderTemplate));
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
