/*
 * Copyright (c) 2008 wemove digital solutions. All rights reserved.
 */

package de.ingrid.iplug.csw.dsc.cswclient;

import java.io.File;

import de.ingrid.utils.PlugDescription;
import junit.framework.TestCase;

public class CSWClientFactoryTest extends TestCase {

	public static final String URL_PORTALU = "http://www.portalu.de/csw";
	public static final String URL_WSVCSW = "http://csw.wsv.de/";
	public static final String URL_DISY_PRELUDIO = "http://demo.disy.net/preludio2.lubw/ws/csw";

	public static final String cswClientImpl = "de.ingrid.iplug.csw.dsc.cswclient.impl.GenericClient";
	public static final String cswCapabilitiesImpl = "de.ingrid.iplug.csw.dsc.cswclient.impl.GenericCapabilities";
	public static final String cswRecordDescriptionImpl = "de.ingrid.iplug.csw.dsc.cswclient.impl.GenericRecordDescription";
	public static final String cswRequestKVPGetImpl = "de.ingrid.iplug.csw.dsc.cswclient.impl.KVPGetRequest";
	public static final String cswRequestSoapImpl = "de.ingrid.iplug.csw.dsc.cswclient.impl.SoapRequest";
	public static final String cswQueryImpl = "de.ingrid.iplug.csw.dsc.cswclient.impl.APIsoQuery";
	public static final String cswSearchResultImpl = "de.ingrid.iplug.csw.dsc.cswclient.impl.GenericSearchResult";
	public static final String cswRecordImpl = "de.ingrid.iplug.csw.dsc.cswclient.impl.GenericRecord";
	
    /**
     * Sets up the test fixture. 
     * (Called before every test case method.) 
     */ 
    public static CSWClientFactory createFactory(PlugDescription desc) { 

		// add default values, if not already defined
    	if (!desc.containsKey("CSWClientImpl"))
    		desc.put("CSWClientImpl", cswClientImpl);
    	if (!desc.containsKey("CSWCapabilitiesImpl"))
    		desc.put("CSWCapabilitiesImpl", cswCapabilitiesImpl);
    	if (!desc.containsKey("CSWRecordDescriptionImpl"))
			desc.put("CSWRecordDescriptionImpl", cswRecordDescriptionImpl);
    	if (!desc.containsKey("CSWRequestImpl"))
    		desc.put("CSWRequestImpl", cswRequestKVPGetImpl);
    	if (!desc.containsKey("CSWQueryImpl"))
    		desc.put("CSWQueryImpl", cswQueryImpl);
    	if (!desc.containsKey("CSWSearchResultImpl"))
    		desc.put("CSWSearchResultImpl", cswSearchResultImpl);
    	if (!desc.containsKey("CSWRecordImpl"))
    		desc.put("CSWRecordImpl", cswRecordImpl);
		File workingDir = new File("c:\\");
		desc.setWorkinDirectory(workingDir);

		// create the factory
		CSWClientFactory f = new CSWClientFactory();
		f.configure(desc);
		return f;
    } 

	public void testCreation() throws Exception {
    	CSWClientFactory f = createFactory(new PlugDescription());
		
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
