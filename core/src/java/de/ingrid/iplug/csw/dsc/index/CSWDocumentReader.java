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
import de.ingrid.iplug.csw.dsc.cswclient.CSWRecord;
import de.ingrid.iplug.csw.dsc.cswclient.constants.ElementSetName;
import de.ingrid.iplug.csw.dsc.mapping.DocumentMapper;

/**
 * Provides documents for the Indexer
 * @author ingo herwig <ingo@wemove.com>
 */
public class CSWDocumentReader implements IDocumentReader {

	final protected static Log log = LogFactory.getLog(CSWDocumentReader.class);
	
	protected Cache cache = null;
	protected Iterator<String> recordIter = null;
	protected DocumentMapper mapper = null;

	public CSWDocumentReader(Cache cache, DocumentMapper mapper) {
		
		this.cache = cache;
		this.recordIter = this.cache.getCachedRecordIds().iterator();
		this.mapper = mapper;
	}
	
	@Override
	public boolean hasNext() {
		// delegate to internal iterator
		return this.recordIter.hasNext();
	}

	@Override
	public Document next() {
		
		Document luceneDocument = new Document();

		// get the record from the cache
		String recordId = this.recordIter.next();
		ElementSetName elementSetName = ElementSetName.FULL;
		CSWRecord cswRecord = null;
		try {
			cswRecord = this.cache.getRecord(recordId, elementSetName);
		} catch (IOException e) {
			// the cache entry does not exist, so we proceed with an empty record
			throw new RuntimeException("Could not retrieve cached record "+recordId+" ["+elementSetName+"]", e);
		}
		
		// do the mapping from CSWRecord to Document
		try {
			long startTime = 0;
			if (log.isInfoEnabled()) {
				startTime = System.currentTimeMillis();
			}
			luceneDocument = this.mapper.mapCswToLucene(cswRecord);
			if (log.isInfoEnabled()) {
				log.info("Mapping record '" + recordId + "' within "+ (System.currentTimeMillis() - startTime) + " ms.");
			}
		} catch (Exception e) {
			throw new RuntimeException("Could not map record "+recordId+" ["+elementSetName+"]", e);
		}
		
		return luceneDocument;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("not implemented");
	}

}
