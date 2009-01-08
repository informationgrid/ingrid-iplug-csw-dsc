/*
 * Copyright (c) 2009 wemove digital solutions. All rights reserved.
 */

package de.ingrid.iplug.csw.dsc.index;

import java.io.IOException;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.Document;

import de.ingrid.iplug.csw.dsc.ConfigurationKeys;
import de.ingrid.iplug.csw.dsc.cache.Cache;
import de.ingrid.iplug.csw.dsc.cswclient.CSWClientFactory;
import de.ingrid.iplug.csw.dsc.cswclient.CSWRecord;
import de.ingrid.iplug.csw.dsc.cswclient.constants.ElementSetName;
import de.ingrid.iplug.csw.dsc.mapping.DocumentMapper;
import de.ingrid.utils.PlugDescription;

/**
 * Provides documents for the Indexer
 * @author ingo herwig <ingo@wemove.com>
 */
public class CSWDocumentReader implements IDocumentReader {

	final protected static Log log = LogFactory.getLog(CSWDocumentReader.class);
	
	protected CSWClientFactory factory = null;
	protected Cache cache = null;
	protected Iterator<String> recordIter = null;
	protected DocumentMapper mapper = null;

	/**
	 * Constructor
	 * @param plugDescription
	 */
	public CSWDocumentReader(PlugDescription plugDescription) {
		
		// get the csw factory instance from the configuration 
		if (plugDescription.containsKey(ConfigurationKeys.CSW_FACTORY)) {
			this.factory = (CSWClientFactory)plugDescription.get(ConfigurationKeys.CSW_FACTORY);
		}
		else
			throw new IllegalArgumentException("The plugdescription does not contain a key '"+ConfigurationKeys.CSW_FACTORY+"'");

		// get the cache instance from the configuration 
		if (plugDescription.containsKey(ConfigurationKeys.CSW_CACHE)) {
			this.cache = (Cache)plugDescription.get(ConfigurationKeys.CSW_CACHE);
			this.recordIter = this.cache.getCachedRecordIds().iterator();
		}
		else
			throw new IllegalArgumentException("The plugdescription does not contain a key '"+ConfigurationKeys.CSW_CACHE+"'");

		// get the mapper instance from the configuration 
		if (plugDescription.containsKey(ConfigurationKeys.CSW_MAPPER)) {
			this.mapper = (DocumentMapper)plugDescription.get(ConfigurationKeys.CSW_MAPPER);
		}
		else
			throw new IllegalArgumentException("The plugdescription does not contain a key '"+ConfigurationKeys.CSW_MAPPER+"'");
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
