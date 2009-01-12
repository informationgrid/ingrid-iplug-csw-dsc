package de.ingrid.iplug.csw.dsc.index;

import java.util.ArrayList;
import java.util.List;

import de.ingrid.iplug.csw.dsc.cache.Cache;
import de.ingrid.iplug.csw.dsc.mapping.DocumentMapper;

public class DocumentReaderFactory {

	public static List<IDocumentReader> getDocumentReaderCollection(Cache cache, DocumentMapper mapper) {
		List<IDocumentReader> arrayList = new ArrayList<IDocumentReader>();
		IDocumentReader documentReader = getCSWDocumentReader(cache, mapper);
		arrayList.add(documentReader);
		return arrayList;
	}

	private static IDocumentReader getCSWDocumentReader(Cache cache, DocumentMapper mapper) {
		IDocumentReader documentReader = new CSWDocumentReader(cache, mapper);
		return documentReader;
	}
}
