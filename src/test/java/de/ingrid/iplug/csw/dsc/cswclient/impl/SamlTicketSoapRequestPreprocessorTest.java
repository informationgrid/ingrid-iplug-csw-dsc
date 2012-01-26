/**
 * 
 */
package de.ingrid.iplug.csw.dsc.cswclient.impl;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.springframework.core.io.ClassPathResource;

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
     */
    @SuppressWarnings("unchecked")
    public void testProcess() throws AxisFault, IOException, SecurityException, NoSuchFieldException,
            IllegalArgumentException, IllegalAccessException {
        SamlTicketSoapRequestPreprocessor p = new SamlTicketSoapRequestPreprocessor();
        DummySamlTicketProvider provider = new DummySamlTicketProvider();
        p.setSamlTicketProvider(provider);

        // set up the client
        ConfigurationContext configContext = ConfigurationContextFactory
                .createConfigurationContextFromFileSystem((new ClassPathResource("axis2.xml")).getURI().getPath());
        ServiceClient serviceClient = new ServiceClient(configContext, null);
        p
                .setSoapHeaderTemplate("<tcExt:tcSecurity soapenv:role=\"http://www.conterra.de/service.csw#component::terraCatalog#catalog\" soapenv:mustUnderstand=\"false\" xmlns:tcExt=\"http://www.conterra.de/catalog/ext\" xmlns:soapenv=\"http://www.w3.org/2003/05/soap-envelope\">\n"
                        + "            <tcExt:tcSecuredAction>\n"
                        + "                <tcExt:action>service.csw::discovery.read</tcExt:action>\n"
                        + "            </tcExt:tcSecuredAction>\n"
                        + "            <tcExt:samlTicket>${SAML_TICKET}</tcExt:samlTicket>\n"
                        + "        </tcExt:tcSecurity>");

        serviceClient = p.process(serviceClient);

        Field reqField = ServiceClient.class.getDeclaredField("headers");
        reqField.setAccessible(true);
        ArrayList<OMElement> headers = (ArrayList<OMElement>) reqField.get(serviceClient);
        OMElement e = headers.get(0);
        assertNotNull(e);
        OMElement result = e.getFirstChildWithName(new QName("http://www.conterra.de/catalog/ext", "samlTicket"));
        assertEquals(result.getText(), DummySamlTicketProvider.DUMMY_RESULT);
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

}
