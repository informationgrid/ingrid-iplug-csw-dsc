package de.ingrid.iplug.csw.dsc.index;

import java.util.ArrayList;
import java.util.List;

import de.ingrid.iplug.csw.dsc.cache.Cache;
import de.ingrid.iplug.csw.dsc.cswclient.CSWFactory;
import de.ingrid.iplug.csw.dsc.mapping.DocumentMapper;
import de.ingrid.utils.PlugDescription;

public class DocumentReaderFactory {

	public static List<IDocumentReader> getDocumentReaderCollection(Cache cache, 
			DocumentMapper mapper, CSWFactory factory) {
		List<IDocumentReader> arrayList = new ArrayList<IDocumentReader>();
		IDocumentReader documentReader = getCSWDocumentReader(cache, mapper, factory);
		arrayList.add(documentReader);
		return arrayList;
	}

	private static IDocumentReader getCSWDocumentReader(Cache cache, 
			DocumentMapper mapper, CSWFactory factory) {
		IDocumentReader documentReader = new CSWDocumentReader(cache, mapper, factory);
		return documentReader;
	}
}
