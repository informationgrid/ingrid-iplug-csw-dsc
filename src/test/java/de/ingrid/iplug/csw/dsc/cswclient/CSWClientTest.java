/*
 * Copyright (c) 2008 wemove digital solutions. All rights reserved.
 */

package de.ingrid.iplug.csw.dsc.cswclient;

import java.io.StringReader;
import java.util.Hashtable;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import junit.framework.TestCase;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import de.ingrid.iplug.csw.dsc.TestServer;
import de.ingrid.iplug.csw.dsc.cswclient.constants.Operation;
import de.ingrid.utils.PlugDescription;

@SuppressWarnings("unused")
public class CSWClientTest extends TestCase {
    
    String storedRecordId = null;
    
	
	public void testGetCapabilitiesKVPGet() throws Exception {
		
		TestServer server = TestServer.PORTALU;
		
		// set up factory - KVPGet requests
		PlugDescription desc = new PlugDescription();
		desc.put("serviceUrl", server.getCapUrlGet());
		Map<String, String> requestImpl = new Hashtable<String, String>();
		requestImpl.put(Operation.GET_CAPABILITIES.toString(), CSWFactoryTest.cswRequestKVPGetImpl);
		desc.put("CSWRequestImpl", requestImpl );
		CSWFactory f = CSWFactoryTest.createFactory(desc);

		// set up client
		CSWClient client = (CSWClient)f.createClient();
		client.configure(f);
		
		// do request
		CSWCapabilities cap = client.getCapabilities();
		
		// tests
		assertFalse("Server does not support MyFunction",
				cap.isSupportingOperations(new String[] { "MyFunction" }));
		
		assertTrue("Server supports GetRecords and GetRecordById",
				cap.isSupportingOperations(new String[] { Operation.GET_RECORDS.toString(), 
						Operation.GET_RECORD_BY_ID.toString() }));
	}
	
	public void testGetCapabilitiesSoap() throws Exception {
		
		TestServer server = TestServer.PORTALU;
		
		// set up factory - Soap requests
		PlugDescription desc = new PlugDescription();
		desc.put("serviceUrl", server.getCapUrlSoap());
		CSWFactory f = CSWFactoryTest.createFactory(desc);

		// set up client
		CSWClient client = (CSWClient)f.createClient();
		client.configure(f);
		
		// do request
		CSWCapabilities cap = client.getCapabilities();
		
		// tests
		assertFalse("Server does not support MyFunction",
				cap.isSupportingOperations(new String[] { "MyFunction" }));
		
		assertTrue("Server supports GetRecords and GetRecordById",
				cap.isSupportingOperations(new String[] { Operation.GET_RECORDS.toString(), 
						Operation.GET_RECORD_BY_ID.toString() }));
	}

	public void testGetOperationUrl() throws Exception {
		
		TestServer server = TestServer.PORTALU;
		
		// set up factory - Soap requests
		PlugDescription desc = new PlugDescription();
		desc.put("serviceUrl", server.getCapUrlSoap());
		CSWFactory f = CSWFactoryTest.createFactory(desc);

		// set up client
		CSWClient client = (CSWClient)f.createClient();
		client.configure(f);
		
		// do request
		CSWCapabilities cap = client.getCapabilities();
		
		// tests
		assertTrue("GetRecords URL is correct",
				"http://www.portalu.de:80/csw202".
				equals(cap.getOperationUrl(Operation.GET_RECORDS)));
	}
	
	public void localTestGetRecordsAndRecordByIdSoap() throws Exception {
		
		TestServer server = TestServer.PORTALU;
		int recordCount = 4;
		
		// set up factory - Soap requests
		PlugDescription desc = new PlugDescription();
		desc.put("serviceUrl", server.getCapUrlSoap());
		CSWFactory f = CSWFactoryTest.createFactory(desc);

		// set up client
		CSWClient client = (CSWClient)f.createClient();
		client.configure(f);

		// create the query
		String filterStr = 
			"<ogc:Filter xmlns:ogc=\"http://www.opengis.net/ogc\">" +
			"<ogc:PropertyIsLike escapeChar=\"\\\" singleChar=\"?\" wildCard=\"*\">" +
			"<ogc:PropertyName>title</ogc:PropertyName>" +
			"<ogc:Literal>a*</ogc:Literal>" +
			"</ogc:PropertyIsLike>" +
			"</ogc:Filter>";
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
		Document filter = docBuilder.parse(new InputSource(new StringReader(filterStr)));
		CSWQuery query = server.getQuery(f.createQuery());

		query.setConstraint(filter);
		query.setMaxRecords(recordCount);

		// do request
		CSWSearchResult result = client.getRecords(query);
		
		// tests
		assertTrue("Fetched "+recordCount+" records from the server",
				recordCount == result.getNumberOfRecords());
		
        // create the query
        String recordId1 = result.getRecordList().get(0).getId();
        query = server.getQuery(f.createQuery());
        
        query.setId(recordId1);

        // do request
        CSWRecord resultById = client.getRecordById(query);
        
        // tests
        assertTrue("Fetched "+recordId1+" from the server",
                recordId1.equals(resultById.getId()));
		
	}
	

}
