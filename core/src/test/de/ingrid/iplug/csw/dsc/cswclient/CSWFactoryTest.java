/*
 * Copyright (c) 2008 wemove digital solutions. All rights reserved.
 */

package de.ingrid.iplug.csw.dsc.cswclient;

import java.util.Hashtable;
import java.util.Map;

import junit.framework.TestCase;
import de.ingrid.iplug.csw.dsc.cswclient.constants.Operation;
import de.ingrid.utils.PlugDescription;

public class CSWFactoryTest extends TestCase {
	
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
    @SuppressWarnings("unchecked")
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
    	{
    		Map requestImpl = new Hashtable();
    		requestImpl.put(Operation.GET_CAPABILITIES.toString(), cswRequestSoapImpl);
    		requestImpl.put(Operation.DESCRIBE_RECORD.toString(), cswRequestSoapImpl);
    		requestImpl.put(Operation.GET_DOMAIN.toString(), cswRequestSoapImpl);
    		requestImpl.put(Operation.GET_RECORDS.toString(), cswRequestSoapImpl);
    		requestImpl.put(Operation.GET_RECORD_BY_ID.toString(), cswRequestSoapImpl);
    		f.setRequestImpl(requestImpl);
    	}
    	else
    		f.setRequestImpl((Map<String, String>)desc.get("CSWRequestImpl"));

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
				f.createRequest(Operation.GET_CAPABILITIES) instanceof CSWRequest);

		assertTrue("createQuery returns a CSWQuery implementation",
				f.createQuery() instanceof CSWQuery);
		
		assertTrue("createQuery returns a CSWSearchResult implementation",
				f.createSearchResult() instanceof CSWSearchResult);
		
		assertTrue("createRecord returns a CSWRecord implementation",
				f.createRecord() instanceof CSWRecord);
	}
}
