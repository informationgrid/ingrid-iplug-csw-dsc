/*
 * Copyright (c) 2008 wemove digital solutions. All rights reserved.
 */

package de.ingrid.iplug.csw.dsc.cswclient.impl;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.ingrid.iplug.csw.dsc.cswclient.CSWFactory;
import de.ingrid.iplug.csw.dsc.cswclient.CSWQuery;
import de.ingrid.iplug.csw.dsc.cswclient.CSWRecord;
import de.ingrid.iplug.csw.dsc.cswclient.CSWSearchResult;
import de.ingrid.iplug.csw.dsc.tools.StringUtils;
import de.ingrid.iplug.csw.dsc.tools.XPathUtils;

public class GenericSearchResult implements CSWSearchResult {

	protected CSWQuery query = null;
	protected Document document = null;
	protected List<CSWRecord> records = null;
	protected int recordsTotal = 0;
	protected int startIndex = 0;

	@Override
	public void initialize(CSWFactory factory, CSWQuery query, Document document) throws Exception {
		this.query = query;
		this.document = document;
		this.records = new ArrayList<CSWRecord>();
		
		// parse the document and create the record list
		Integer numMatched = XPathUtils.getInt(document, "GetRecordsResponse/SearchResults/@numberOfRecordsMatched");
		if (numMatched != null) {
			this.recordsTotal = numMatched.intValue();
			
			NodeList recordNodes = XPathUtils.getNodeList(document, "GetRecordsResponse/SearchResults/child::node()");
			if (recordNodes != null) {
				for (int i=0; i<recordNodes.getLength(); i++) {
					
					// make sure to only pass the node (not the whole document)
					// TODO: check if this can be done better
					Node node = StringUtils.stringToDocument(StringUtils.nodeToString(recordNodes.item(i)));

					// create the record
					CSWRecord record = factory.createRecord();
					record.initialize(query.getElementSetName(), node);
					records.add(record);
				}
		    }
		}
	}

	@Override
	public CSWQuery getQuery() {
		return this.query;
	}

	@Override
	public Document getOriginalResponse() {
		return this.document;
	}

	@Override
	public int getNumberOfRecordsTotal() {
		return this.recordsTotal;
	}

	@Override
	public int getNumberOfRecords() {
		if (this.records != null)
			return this.records.size();
		return 0;
	}

	@Override
	public List<CSWRecord> getRecordList() {
		return this.records;
	}
}
