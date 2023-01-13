/*
 * **************************************************-
 * ingrid-iplug-csw-dsc:war
 * ==================================================
 * Copyright (C) 2014 - 2023 wemove digital solutions GmbH
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

import java.io.StringReader;
import java.util.Hashtable;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import de.ingrid.iplug.csw.dsc.TestServer;
import de.ingrid.iplug.csw.dsc.cswclient.constants.Operation;
import de.ingrid.utils.PlugDescription;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CSWClientTestLocal {
    
    String storedRecordId = null;


    @Test
    public void testGetCapabilitiesKVPGet() throws Exception {
		
		TestServer server = TestServer.PORTALU;
		
		// set up factory - KVPGet requests
		PlugDescription desc = new PlugDescription();
		desc.put("serviceUrl", server.getCapUrlGet());
		Map<String, CSWRequest> requestImpl = new Hashtable<String, CSWRequest>();
		requestImpl.put(Operation.GET_CAPABILITIES.toString(), CSWFactoryTest.cswRequestKVPGetImpl);
		desc.put("CSWRequestImpl", requestImpl );
		CSWFactory f = CSWFactoryTest.createFactory(desc);

		// set up client
		CSWClient client = (CSWClient)f.createClient();
		client.configure(f);
		
		// do request
		CSWCapabilities cap = client.getCapabilities();
		
		// tests
		assertFalse(cap.isSupportingOperations(new String[] { "MyFunction" }),
				"Server does not support MyFunction");
		
		assertTrue(cap.isSupportingOperations(new String[] { Operation.GET_RECORDS.toString(), 
						Operation.GET_RECORD_BY_ID.toString() }),
				"Server supports GetRecords and GetRecordById");
	}

    @Test
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
		assertFalse(cap.isSupportingOperations(new String[] { "MyFunction" }),
				"Server does not support MyFunction");
		
		assertTrue(cap.isSupportingOperations(new String[] { Operation.GET_RECORDS.toString(), 
						Operation.GET_RECORD_BY_ID.toString() }),
				"Server supports GetRecords and GetRecordById");
	}

    @Test
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
		assertEquals("http://dev.informationgrid.eu:80/csw",
				cap.getOperationUrl(Operation.GET_RECORDS),
				"GetRecords URL is correct");
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
        assertEquals(recordCount, result.getNumberOfRecords(), "Fetched " + recordCount + " records from the server");
		
        // create the query
        String recordId1 = result.getRecordList().get(0).getId();
        query = server.getQuery(f.createQuery());
        
        query.setId(recordId1);

        // do request
        CSWRecord resultById = client.getRecordById(query);

        // tests
        assertEquals(recordId1, resultById.getId(), "Fetched " + recordId1 + " from the server");
		
	}
	

}
