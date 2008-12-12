/*
 * Copyright (c) 2008 wemove digital solutions. All rights reserved.
 */

package de.ingrid.iplug.csw.dsc.cswclient;

import junit.framework.TestCase;
import de.ingrid.utils.PlugDescription;

public class CSWClientTest extends TestCase {
	
	public void testGetCapabilitiesKVPGet() throws Exception {
		
		// set up factory - KVPGet requests
		PlugDescription desc = new PlugDescription();
		desc.put("serviceUrl", CSWClientFactoryTest.serviceUrlGet);
		desc.put("CSWRequestImpl", CSWClientFactoryTest.cswRequestKVPGetImpl);
		CSWClientFactory f = CSWClientFactoryTest.createFactory(desc);

		// set up client
		CSWClient client = (CSWClient)f.createClient();
		client.configure(f);
		CSWCapabilities cap = client.getCapabilities();
		
		// tests
		assertFalse("Server does not support MyFunction",
				cap.isSupportingOperations(new String[] { "MyFunction" }));
		
		assertTrue("Server supports GetRecords and GetRecordById",
				cap.isSupportingOperations(new String[] { CSWConstants.OP_GET_RECORDS, CSWConstants.OP_GET_RECORD_BY_ID }));
	}
	
	public void testGetCapabilitiesSoap() throws Exception {
		
		// set up factory - Soap requests
		PlugDescription desc = new PlugDescription();
		desc.put("serviceUrl", CSWClientFactoryTest.serviceUrlSoap);
		desc.put("CSWRequestImpl", CSWClientFactoryTest.cswRequestSoapImpl);
		CSWClientFactory f = CSWClientFactoryTest.createFactory(desc);

		// set up client
		CSWClient client = (CSWClient)f.createClient();
		client.configure(f);
		CSWCapabilities cap = client.getCapabilities();
		
		// tests
		assertFalse("Server does not support MyFunction",
				cap.isSupportingOperations(new String[] { "MyFunction" }));
		
		assertTrue("Server supports GetRecords and GetRecordById",
				cap.isSupportingOperations(new String[] { CSWConstants.OP_GET_RECORDS, CSWConstants.OP_GET_RECORD_BY_ID }));
	}
}
