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
import de.ingrid.iplug.csw.dsc.cswclient.constants.Namespace;
import de.ingrid.iplug.csw.dsc.cswclient.constants.Operation;
import de.ingrid.iplug.csw.dsc.cswclient.constants.OutputFormat;
import de.ingrid.iplug.csw.dsc.cswclient.constants.ResultType;
import de.ingrid.iplug.csw.dsc.cswclient.constants.TypeName;
import de.ingrid.iplug.csw.dsc.cswclient.impl.GenericQuery;
import de.ingrid.utils.PlugDescription;

@SuppressWarnings("unused")
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
		
		int recordCount = 4;
		
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
		//CSWQuery query = createDisyQuery(f.createQuery());
		CSWQuery query = createPortalUQuery(f.createQuery());

		query.setConstraint(filter);
		query.setMaxRecords(recordCount);

		// do request
		CSWSearchResult result = client.getRecords(query);
		
		// tests
		assertTrue("Fetched "+recordCount+" records from the server",
				recordCount == result.getNumberOfRecords());
		assertTrue("First record has id 1AFDCB03-3818-40F1-9560-9FB082956357",
				result.getRecordList().get(0).getId().equals("1AFDCB03-3818-40F1-9560-9FB082956357"));
	}
	
	public void testGetRecordByIdSoap() throws Exception {
		
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
		String recordId1 = "1AFDCB03-3818-40F1-9560-9FB082956357";
		String recordId2 = "1A7EFA6F-FEDF-44D4-B139-6D92FD68CF58";
		//CSWQuery query = createDisyQuery(f.createQuery());
		CSWQuery query = createPortalUQuery(f.createQuery());
		
		query.setId(recordId1);

		// do request
		CSWRecord result = client.getRecordById(query);
		
		// tests
		assertTrue("Fetched "+recordId1+" from the server",
				recordId1.equals(result.getId()));
	}
	
	private CSWQuery createDisyQuery(CSWQuery query) {
		query.setSchema(Namespace.CSW);
		query.setOutputSchema(Namespace.CSW_PROFILE);
		query.setOutputFormat(OutputFormat.APPLICATION_XML);
		query.setVersion("2.0.1");
		query.setTypeNames(new TypeName[] { TypeName.RECORD });
		query.setResultType(ResultType.RESULTS);
		query.setElementSetName(ElementSetName.BRIEF);
		query.setConstraintLanguageVersion("1.1.0");
		return query;
	}

	private CSWQuery createPortalUQuery(CSWQuery query) {
		query.setSchema(Namespace.CSW);
		query.setOutputSchema(Namespace.CSW_PROFILE);
		query.setOutputFormat(OutputFormat.TEXT_XML);
		query.setVersion("2.0.0");
		query.setTypeNames(new TypeName[] { TypeName.DATASET });
		query.setResultType(ResultType.RESULTS);
		query.setElementSetName(ElementSetName.BRIEF);
		query.setConstraintLanguageVersion("1.0.0");
		return query;
	}
}
