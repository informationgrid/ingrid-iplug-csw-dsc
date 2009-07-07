package de.ingrid.iplug.csw.dsc.index;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

public class DummyDocumentReader implements IDocumentReader {

	private int _counter = 10;

	private boolean _store = true;

	private boolean _index = true;

	private boolean _token = true;

	public DummyDocumentReader(int counter) {
		_counter = counter;
	}

	public boolean hasNext() {
		return _counter > 0;
	}

	public Document next() {
		_counter--;
		Document document = new Document();
		document
				.add(new Field("datatype", "default", !_store, _index, !_token));
		document.add(new Field("title", "title " + _counter, _store, _index,
				_token));
		document.add(new Field("content", "content " + _counter, _store,
				_index, _token));
		document
				.add(new Field("url", "url " + _counter, _store, _index, _token));
		return document;
	}

	public void remove() {
		throw new UnsupportedOperationException("not implemented");
	}

}
