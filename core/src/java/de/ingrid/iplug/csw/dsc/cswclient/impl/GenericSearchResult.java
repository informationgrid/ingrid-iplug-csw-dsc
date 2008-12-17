/*
 * Copyright (c) 2008 wemove digital solutions. All rights reserved.
 */

package de.ingrid.iplug.csw.dsc.cswclient.impl;

import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.ingrid.iplug.csw.dsc.cswclient.CSWQuery;
import de.ingrid.iplug.csw.dsc.cswclient.CSWRecord;
import de.ingrid.iplug.csw.dsc.cswclient.CSWSearchResult;
import de.ingrid.iplug.csw.dsc.tools.XPathUtils;

public class GenericSearchResult implements CSWSearchResult {

	protected CSWQuery query = null;
	protected Document document = null;
	protected List<CSWRecord> records = null;
	protected int recordsTotal = 0;
	protected int startIndex = 0;

	@Override
	public void configure(CSWQuery query, Document document) {
		this.query = query;
		this.document = document;
		
		// parse the document and create the record list
		Integer numMatched = XPathUtils.getInt(document, "GetRecordsResponse/SearchResults/@numberOfRecordsMatched");
		if (numMatched != null) {
			this.recordsTotal = numMatched.intValue();
			
			NodeList records = XPathUtils.getNodeList(document, "//fileIdentifier/CharacterString");
			if (records != null) {
				for (int i=0; i<records.getLength(); i++) {
					Node record = records.item(i);
					System.out.println(record.getTextContent());
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
