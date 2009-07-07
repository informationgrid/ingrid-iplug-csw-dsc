/*
 * Copyright (c) 2009 wemove digital solutions. All rights reserved.
 */

package de.ingrid.iplug.csw.dsc.mapping;

import org.apache.lucene.document.Document;

import de.ingrid.iplug.csw.dsc.cswclient.CSWRecord;
import de.ingrid.utils.dsc.Record;

/**
 * Interface for document mapper.
 * @author ingo herwig <ingo@wemove.com>
 */
public interface DocumentMapper {

	/**
	 * Map a csw record to a lucene document.
	 * @param record
	 * @return Document
	 */
	public Document mapCswToLucene(CSWRecord record) throws Exception;
	
	/**
	 * Map a csw record to an ingrid record.
	 * @param record
	 * @return Record
	 */
	public Record mapCswToIngrid(CSWRecord record) throws Exception;
}
