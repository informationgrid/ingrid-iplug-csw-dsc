/*
 * Copyright (c) 1997-2005 by media style GmbH
 * 
 * $Source: /cvs/asp-search/src/java/com/ms/aspsearch/PermissionDeniedException.java,v $
 */

package de.ingrid.iplug.csw.dsc.index;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.Document;

import de.ingrid.iplug.csw.dsc.cache.Cache;
import de.ingrid.iplug.csw.dsc.cswclient.CSWRecord;
import de.ingrid.iplug.csw.dsc.cswclient.constants.ElementSetName;
import de.ingrid.iplug.csw.dsc.mapping.DocumentMapper;
import de.ingrid.utils.dsc.Record;

/**
 * Returns the details of a document
 */
public class RecordLoader {

	protected static Log log = LogFactory.getLog(RecordLoader.class);

	/**
	 * This method fetches the original FULL csw record from the catch and
	 * maps it to the ingrid record.
	 * @param document
	 * @param mapper
	 * @param cache
	 * @return Record
	 * @throws RuntimeException
	 */
	public Record getDetails(Document document, DocumentMapper mapper, Cache cache) throws RuntimeException {

		// get the id of the CSW record from the document
		String recordId = document.get("T01_object.obj_id");
		ElementSetName elementSetName = ElementSetName.FULL;
		
		// fetch the record from the cache
		CSWRecord cswRecord;
		try {
			cswRecord = cache.getRecord(recordId, elementSetName);
		} catch (IOException e) {
			throw new RuntimeException("Could not fetch cached record "+recordId+" ["+elementSetName+"]", e);
		}
		
		// do the mapping from CSWRecord to Record
		Record ingridRecord = null;
		try {
			ingridRecord = mapper.mapCswToIngrid(cswRecord);
		} catch (Exception e) {
			throw new RuntimeException("Could not map record "+recordId+" ["+elementSetName+"]", e);
		}
		
		return ingridRecord;
	}

	public void close() throws Exception {
	}
}
