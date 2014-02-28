/**
 * 
 */
package de.ingrid.iplug.csw.dsc.cswclient.impl;

import java.io.IOException;
import java.util.Iterator;

import javax.xml.namespace.NamespaceContext;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import junit.framework.TestCase;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.ingrid.utils.xpath.XPathUtils;

/**
 * @author joachim
 * 
 */
public class SamlTicketSoapRequestPreprocessorTest extends TestCase {

    /**
     * Test method for
     * {@link de.ingrid.iplug.csw.dsc.cswclient.impl.SamlTicketSoapRequestPreprocessor#process(org.apache.axis2.client.ServiceClient)}
     * .
     * 
     * @throws IOException
     * @throws AxisFault
     * @throws NoSuchFieldException
     * @throws SecurityException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws SOAPException
     */
    public void testProcess() throws IOException, SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException, SOAPException {
        SamlTicketSoapRequestPreprocessor p = new SamlTicketSoapRequestPreprocessor();
        DummySamlTicketProvider provider = new DummySamlTicketProvider();
        p.setSamlTicketProvider(provider);

        // set up the client

        MessageFactory messageFactory = MessageFactory.newInstance();
        SOAPMessage soapMessage = messageFactory.createMessage();

        p.setSoapHeaderTemplate("<tcExt:tcSecurity soapenv:role=\"http://www.conterra.de/service.csw#component::terraCatalog#catalog\" soapenv:mustUnderstand=\"false\" xmlns:tcExt=\"http://www.conterra.de/catalog/ext\" xmlns:soapenv=\"http://www.w3.org/2003/05/soap-envelope\">\n"
                + "            <tcExt:tcSecuredAction>\n"
                + "                <tcExt:action>service.csw::discovery.read</tcExt:action>\n"
                + "            </tcExt:tcSecuredAction>\n"
                + "            <tcExt:samlTicket>${SAML_TICKET}</tcExt:samlTicket>\n" + "        </tcExt:tcSecurity>");

        p.process(soapMessage);

        NodeList nodes = soapMessage.getSOAPPart().getEnvelope().getHeader().getChildNodes();
        XPathUtils xpath = new XPathUtils(new TCExtNamespaceContext());

        for (int i = 0; i < nodes.getLength(); i++) {
            Node n = nodes.item(i);
            if (xpath.getString(n, "//tcExt:samlTicket").equals(DummySamlTicketProvider.DUMMY_RESULT)) {
                assertEquals(true, true);
                return;
            }
        }

        fail("Not found: " + DummySamlTicketProvider.DUMMY_RESULT);
    }

    public class DummySamlTicketProvider extends SamlTicketProvider {

        public static final String DUMMY_RESULT = "DUMMY_SAML_TICKET";

        public DummySamlTicketProvider() {
            super();
        }

        public String get() {
            return DUMMY_RESULT;

        }

    }

    public class TCExtNamespaceContext implements NamespaceContext {

        @Override
        public String getNamespaceURI(String prefix) {
            return "http://www.conterra.de/catalog/ext";
        }

        @Override
        public String getPrefix(String namespaceURI) {
            return "tcExt";
        }

        @Override
        public Iterator getPrefixes(String namespaceURI) {
            return null;
        }

    }

}
