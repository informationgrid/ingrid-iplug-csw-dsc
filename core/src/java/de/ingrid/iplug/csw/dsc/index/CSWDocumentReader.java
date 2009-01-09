/*
 * Copyright (c) 2009 wemove digital solutions. All rights reserved.
 */

package de.ingrid.iplug.csw.dsc.index;

import java.io.IOException;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.Document;

import de.ingrid.iplug.csw.dsc.cache.Cache;
import de.ingrid.iplug.csw.dsc.cswclient.CSWFactory;
import de.ingrid.iplug.csw.dsc.cswclient.CSWRecord;
import de.ingrid.iplug.csw.dsc.cswclient.constants.ElementSetName;
import de.ingrid.iplug.csw.dsc.mapping.DocumentMapper;

/**
 * Provides documents for the Indexer
 * @author ingo herwig <ingo@wemove.com>
 */
public class CSWDocumentReader implements IDocumentReader {

	final protected static Log log = LogFactory.getLog(CSWDocumentReader.class);
	
	protected CSWFactory factory = null;
	protected Cache cache = null;
	protected Iterator<String> recordIter = null;
	protected DocumentMapper mapper = null;

	public CSWDocumentReader(Cache cache, DocumentMapper mapper, CSWFactory factory) {
		
		this.cache = cache;
		this.recordIter = this.cache.getCachedRecordIds().iterator();
		this.mapper = mapper;
		this.factory = factory;
	}
	
	@Override
	public boolean hasNext() {
		// delegate to internal iterator
		return this.recordIter.hasNext();
	}

	@Override
	public Document next() {
		
		Document document = new Document();

		// get the record from the cache
		String recordId = this.recordIter.next();
		ElementSetName elementSetName = ElementSetName.BRIEF;
		CSWRecord record = null;
		try {
			record = this.factory.createRecord();
		} catch (RuntimeException e) {
			// record creation went wrong most likely because of configuration problems
			throw new RuntimeException("Could not create a record", e);
		}
		try {
			record = this.cache.getRecord(recordId, elementSetName, record);
		} catch (IOException e) {
			// the cache entry does not exist, so we proceed with an empty record
			throw new RuntimeException("Could not retrieve cached record "+recordId+" ["+elementSetName+"]", e);
		}
		
		// do the mapping from CSWRecord to Document
		try {
			document = this.mapper.mapCswToLucene(record);
		} catch (Exception e) {
			throw new RuntimeException("Could not map record "+recordId+" ["+elementSetName+"]", e);
		}
		
		return document;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("not implemented");
	}

}
