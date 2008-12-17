/*
 * Copyright (c) 2008 wemove digital solutions. All rights reserved.
 */

package de.ingrid.iplug.csw.dsc.cswclient;

import java.io.StringReader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import junit.framework.TestCase;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import de.ingrid.iplug.csw.dsc.cswclient.constants.ElementSetName;
import de.ingrid.iplug.csw.dsc.cswclient.constants.Operation;
import de.ingrid.iplug.csw.dsc.cswclient.constants.ResultType;
import de.ingrid.iplug.csw.dsc.cswclient.constants.TypeName;
import de.ingrid.iplug.csw.dsc.cswclient.impl.GenericQuery;
import de.ingrid.utils.PlugDescription;

public class CSWClientTestLocal extends TestCase {
	
	public void testGetCapabilitiesKVPGet() throws Exception {
		
		// set up factory - KVPGet requests
		PlugDescription desc = new PlugDescription();
		desc.put("serviceUrl", CSWClientFactoryTest.URL_PORTALU);
		desc.put("CSWRequestImpl", CSWClientFactoryTest.cswRequestKVPGetImpl);
		CSWClientFactory f = CSWClientFactoryTest.createFactory(desc);

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
		
		// set up factory - Soap requests
		PlugDescription desc = new PlugDescription();
		desc.put("serviceUrl", CSWClientFactoryTest.URL_DISY_PRELUDIO);
		desc.put("CSWRequestImpl", CSWClientFactoryTest.cswRequestSoapImpl);
		CSWClientFactory f = CSWClientFactoryTest.createFactory(desc);

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

	public void testGetRecordsSoap() throws Exception {
		
		// set up factory - Soap requests
		PlugDescription desc = new PlugDescription();
		//desc.put("serviceUrl", CSWClientFactoryTest.URL_DISY_PRELUDIO);
		desc.put("serviceUrl", CSWClientFactoryTest.URL_PORTALU);
		desc.put("CSWRequestImpl", CSWClientFactoryTest.cswRequestSoapImpl);
		CSWClientFactory f = CSWClientFactoryTest.createFactory(desc);

		// set up client
		CSWClient client = (CSWClient)f.createClient();
		client.configure(f);

		// create the query
		String filterStr = 
			"<ogc:Filter xmlns:ogc=\"http://www.opengis.net/ogc\">" +
			"<ogc:PropertyIsLike escapeChar=\"\\\" singleChar=\"?\" wildCard=\"*\">" +
			"<ogc:PropertyName>fileIdentifier</ogc:PropertyName>" +
			"<ogc:Literal>1*</ogc:Literal>" +
			"</ogc:PropertyIsLike>" +
			"</ogc:Filter>";
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
		Document filter = docBuilder.parse(new InputSource(new StringReader(filterStr)));
		//CSWQuery query = createDisyQuery(filter);
		CSWQuery query = createPortalUQuery(filter);
		
		query.setMaxRecords(4);

		// do request
		CSWSearchResult result = client.getRecords(query);
		
		System.out.println(result.getNumberOfRecordsTotal());
		
		// tests
	}
	
	private CSWQuery createDisyQuery(Document filter) {
		CSWQuery query = new GenericQuery();
		query.setSchema(CSWConstants.NAMESPACE_CSW);
		query.setOutputSchema(CSWConstants.NAMESPACE_CSW_PROFILE);
		query.setOutputFormat("application/xml");
		query.setVersion("2.0.1");
		query.setTypeNames(TypeName.RECORD);
		query.setResultType(ResultType.RESULTS);
		query.setElementSetName(ElementSetName.BRIEF);
		query.setConstraintVersion("1.1.0");
		query.setFilter(filter);
		return query;
	}

	private CSWQuery createPortalUQuery(Document filter) {
		CSWQuery query = new GenericQuery();
		query.setSchema(CSWConstants.NAMESPACE_CSW);
		query.setOutputSchema(CSWConstants.NAMESPACE_CSW_PROFILE);
		query.setOutputFormat("text/xml");
		query.setVersion("2.0.0");
		query.setTypeNames(TypeName.DATASET);
		query.setResultType(ResultType.RESULTS);
		query.setElementSetName(ElementSetName.BRIEF);
		query.setConstraintVersion("1.0.0");
		query.setFilter(filter);
		return query;
	}
}
