package de.ingrid.iplug.csw.dsc.index;

import java.util.ArrayList;
import java.util.List;

import de.ingrid.utils.PlugDescription;

public class DocumentReaderFactory {

	public static List<IDocumentReader> getDocumentReaderCollection(PlugDescription plugDescription) {
		List<IDocumentReader> arrayList = new ArrayList<IDocumentReader>();
		IDocumentReader documentReader = getCSWDocumentReader(plugDescription);
		arrayList.add(documentReader);
		return arrayList;
	}

	private static IDocumentReader getCSWDocumentReader(PlugDescription plugDescription) {
		IDocumentReader documentReader = new CSWDocumentReader(plugDescription);
		return documentReader;
	}
}
