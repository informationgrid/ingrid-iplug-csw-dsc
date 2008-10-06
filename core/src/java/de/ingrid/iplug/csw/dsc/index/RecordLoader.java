/*
 * Copyright (c) 1997-2005 by media style GmbH
 * 
 * $Source: /cvs/asp-search/src/java/com/ms/aspsearch/PermissionDeniedException.java,v $
 */

package de.ingrid.iplug.csw.dsc.index;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.Document;

import de.ingrid.utils.dsc.Record;

/**
 * Returns the details of a document
 */
public class RecordLoader {

	protected static Log log = LogFactory.getLog(RecordLoader.class);

	public Record getDetails(Document document) throws Exception {
		// TODO
		return new Record();
	}

	public void close() throws Exception {
	}

}
