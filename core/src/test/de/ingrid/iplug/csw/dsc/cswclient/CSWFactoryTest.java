/*
 * Copyright (c) 2008 wemove digital solutions. All rights reserved.
 */

package de.ingrid.iplug.csw.dsc.cswclient;

import junit.framework.TestCase;
import de.ingrid.utils.PlugDescription;

public class CSWFactoryTest extends TestCase {

	public static final String URL_PORTALU = "http://www.portalu.de/csw";
	public static final String URL_WSVCSW = "http://csw.wsv.de/";
	public static final String URL_DISY_PRELUDIO = "http://demo.disy.net/preludio2.lubw/ws/csw";
	public static final String URL_BKG = "http://ims3.bkg.bund.de/mdm/CSW2Servlet";
	public static final String URL_ADV_MIS = "http://gdz-extern.bkg.bund.de/ingeo_csw/ingeo_csw";
	public static final String URL_GEODATA = "http://www.geodata.gov/Portal/csw202/discovery";
	public static final String URL_GEODATA_NL = "http://www.geodata.alterra.nl/excat";
	public static final String URL_UNIFI = "http://apollo.pin.unifi.it:8080/lucansdi-gi-cat-5.1.3/ogc-services";
	public static final String URL_SDISUITE="http://gdi-de.sdisuite.de/soapServices/services/CSWDiscovery";
	
	
	public static final String cswClientImpl = "de.ingrid.iplug.csw.dsc.cswclient.impl.GenericClient";
	public static final String cswCapabilitiesImpl = "de.ingrid.iplug.csw.dsc.cswclient.impl.GenericCapabilities";
	public static final String cswRecordDescriptionImpl = "de.ingrid.iplug.csw.dsc.cswclient.impl.GenericRecordDescription";
	public static final String cswRequestKVPGetImpl = "de.ingrid.iplug.csw.dsc.cswclient.impl.KVPGetRequest";
	public static final String cswRequestSoapImpl = "de.ingrid.iplug.csw.dsc.cswclient.impl.SoapRequest";
	public static final String cswQueryImpl = "de.ingrid.iplug.csw.dsc.cswclient.impl.GenericQuery";
	public static final String cswSearchResultImpl = "de.ingrid.iplug.csw.dsc.cswclient.impl.GenericSearchResult";
	public static final String cswRecordImpl = "de.ingrid.iplug.csw.dsc.cswclient.impl.GenericRecord";
	
    /**
     * Sets up the test fixture. 
     * (Called before every test case method.) 
     */ 
    public static CSWFactory createFactory(PlugDescription desc) { 

		// create the factory
		CSWFactory f = new CSWFactory();
		
		if (desc.containsKey("serviceUrl"))
			f.setServiceUrl(desc.get("serviceUrl").toString());

		// add default values, if not already defined
    	if (!desc.containsKey("CSWClientImpl"))
    		f.setClientImpl(cswClientImpl);
    	else
    		f.setClientImpl(desc.get("CSWClientImpl").toString());
    	
    	if (!desc.containsKey("CSWCapabilitiesImpl"))
    		f.setCapabilitiesImpl(cswCapabilitiesImpl);
    	else
    		f.setCapabilitiesImpl(desc.get("CSWCapabilitiesImpl").toString());
    	
    	if (!desc.containsKey("CSWRecordDescriptionImpl"))
			f.setRecordDescriptionImpl(cswRecordDescriptionImpl);
    	else
    		f.setRecordDescriptionImpl(desc.get("CSWRecordDescriptionImpl").toString());

    	if (!desc.containsKey("CSWRequestImpl"))
    		f.setRequestImpl(cswRequestKVPGetImpl);
    	else
    		f.setRequestImpl(desc.get("CSWRequestImpl").toString());

    	if (!desc.containsKey("CSWQueryImpl"))
    		f.setQueryImpl(cswQueryImpl);
    	else
    		f.setQueryImpl(desc.get("CSWQueryImpl").toString());

    	if (!desc.containsKey("CSWSearchResultImpl"))
    		f.setSearchResultImpl(cswSearchResultImpl);
    	else
    		f.setSearchResultImpl(desc.get("CSWSearchResultImpl").toString());

    	if (!desc.containsKey("setRecordImpl"))
    		f.setRecordImpl(cswRecordImpl);
    	else
    		f.setRecordImpl(desc.get("setRecordImpl").toString());

		return f;
    } 

	public void testCreation() throws Exception {
    	CSWFactory f = createFactory(new PlugDescription());
		
		// tests
		assertTrue("createClient returns a CSWClient implementation",
				f.createClient() instanceof CSWClient);

		assertTrue("createCapabilities returns a CSWCapabilities implementation",
				f.createCapabilities() instanceof CSWCapabilities);
		
		assertTrue("createRecordDescription returns a CSWRecordDescription implementation",
				f.createRecordDescription() instanceof CSWRecordDescription);
		
		assertTrue("createRequest returns a CSWRequest implementation",
				f.createRequest() instanceof CSWRequest);

		assertTrue("createQuery returns a CSWQuery implementation",
				f.createQuery() instanceof CSWQuery);
		
		assertTrue("createQuery returns a CSWSearchResult implementation",
				f.createSearchResult() instanceof CSWSearchResult);
		
		assertTrue("createRecord returns a CSWRecord implementation",
				f.createRecord() instanceof CSWRecord);
	}
}
