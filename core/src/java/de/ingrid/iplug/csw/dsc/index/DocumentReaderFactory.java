package de.ingrid.iplug.csw.dsc.index;

import java.util.ArrayList;
import java.util.List;

public class DocumentReaderFactory {

	public static List<IDocumentReader> getDocumentReaderCollection() {
		List<IDocumentReader> arrayList = new ArrayList<IDocumentReader>();
		IDocumentReader documentReader = getDummyDocumentReader();
		arrayList.add(documentReader);
		// TODO add document reader that reads entries from the xml
		return arrayList;
	}

	private static IDocumentReader getDummyDocumentReader() {
		IDocumentReader documentReader = new DummyDocumentReader(10);
		return documentReader;
	}
}
