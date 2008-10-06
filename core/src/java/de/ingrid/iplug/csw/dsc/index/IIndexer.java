package de.ingrid.iplug.csw.dsc.index;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.document.Document;

public interface IIndexer {

	void open(File file) throws IOException;

	void index(IDocumentReader documentReader) throws IOException;

	void addDocument(Document document) throws IOException;

	void close() throws Exception;
}
