/*
 * Copyright (c) 1997-2005 by media style GmbH
 * 
 * $Source: /cvs/asp-search/src/java/com/ms/aspsearch/PermissionDeniedException.java,v $
 */

package de.ingrid.iplug.csw.dsc.index;

import java.io.File;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;

public class Indexer implements IIndexer {

	private File _targetFolder;

	private IndexWriter _indexWriter;

	private File _indexFolder;

	private static final Log LOG = LogFactory.getLog(Indexer.class);

	public void close() throws Exception {
		_indexWriter.optimize();
		_indexWriter.close();
		AbstractSearcher instance = AbstractSearcher.getInstance();
		if (instance != null) {
			instance.stop();
		}
		File oldIndex = new File(_targetFolder, "index");
		delete(oldIndex);
		_indexFolder.renameTo(oldIndex);
		if (instance != null) {
			instance.start();
		}
	}

	public void addDocument(Document document) throws IOException {
		_indexWriter.addDocument(document);
	}

	public void open(File file) throws IOException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Use '" + file.getAbsolutePath() + "' as base for the index.");
		}
		_targetFolder = file;
		_indexFolder = new File(_targetFolder, "newIndex");
		if (LOG.isDebugEnabled()) {
			LOG.debug("Created '" + _indexFolder.getAbsolutePath() + "' for creating a new index.");
		}
		_indexWriter = new IndexWriter(_indexFolder, new StandardAnalyzer(
				new String[0]), true);
	}

	public void index(IDocumentReader documentReader) throws IOException {
		while (documentReader.hasNext()) {
			Document document = documentReader.next();
			if (LOG.isDebugEnabled())
				LOG.debug("Indexer.index() " + document);
			_indexWriter.addDocument(document);
		}
	}

	private void delete(File folder) {
		File[] files = folder.listFiles();
		if (files != null) {
			for (int i = 0; i < files.length; i++) {
				File file = files[i];
				if (file.isDirectory()) {
					delete(file);
				}
				file.delete();
			}
		}
		folder.delete();
	}

}
