/*
 * **************************************************-
 * ingrid-iplug-csw-dsc:war
 * ==================================================
 * Copyright (C) 2014 - 2018 wemove digital solutions GmbH
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

package de.ingrid.iplug.csw.dsc.cswclient;

import java.util.Hashtable;
import java.util.Map;

import junit.framework.TestCase;
import de.ingrid.iplug.csw.dsc.cswclient.constants.Operation;
import de.ingrid.iplug.csw.dsc.cswclient.impl.KVPGetRequest;
import de.ingrid.iplug.csw.dsc.cswclient.impl.SoapRequest;
import de.ingrid.utils.PlugDescription;

public class CSWFactoryTest extends TestCase {
	
	public static final String cswClientImpl = "de.ingrid.iplug.csw.dsc.cswclient.impl.GenericClient";
	public static final String cswCapabilitiesImpl = "de.ingrid.iplug.csw.dsc.cswclient.impl.GenericCapabilities";
	public static final String cswRecordDescriptionImpl = "de.ingrid.iplug.csw.dsc.cswclient.impl.GenericRecordDescription";
	public static final CSWRequest cswRequestKVPGetImpl = new KVPGetRequest();
	public static final CSWRequest cswRequestSoapImpl = new SoapRequest();
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
    		Map<String, CSWRequest> requestImpl = new Hashtable<String, CSWRequest>();
    		requestImpl.put(Operation.GET_CAPABILITIES.toString(), cswRequestSoapImpl);
    		requestImpl.put(Operation.DESCRIBE_RECORD.toString(), cswRequestSoapImpl);
    		requestImpl.put(Operation.GET_DOMAIN.toString(), cswRequestSoapImpl);
    		requestImpl.put(Operation.GET_RECORDS.toString(), cswRequestSoapImpl);
    		requestImpl.put(Operation.GET_RECORD_BY_ID.toString(), cswRequestSoapImpl);
    		f.setRequestImpl(requestImpl);
    	}
    	else
    		f.setRequestImpl((Map<String, CSWRequest>)desc.get("CSWRequestImpl"));

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

    	f.configure(desc);
    	
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
