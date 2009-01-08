package de.ingrid.iplug.csw.dsc.index;

import java.io.File;
import java.util.List;

import junit.framework.TestCase;
import de.ingrid.iplug.csw.dsc.ConfigurationKeys;
import de.ingrid.iplug.csw.dsc.TestData;
import de.ingrid.iplug.csw.dsc.TestUtil;
import de.ingrid.iplug.csw.dsc.cache.Cache;
import de.ingrid.iplug.csw.dsc.cache.impl.DefaultFileCache;
import de.ingrid.iplug.csw.dsc.cswclient.constants.ElementSetName;
import de.ingrid.iplug.csw.dsc.cswclient.impl.GenericRecord;
import de.ingrid.utils.IngridHits;
import de.ingrid.utils.PlugDescription;
import de.ingrid.utils.queryparser.QueryStringParser;
import de.ingrid.utils.xml.XMLSerializer;

/**
 * TODO comment for IndexesTest
 * 
 * <p/>created on 30.05.2006
 * 
 * @version $Revision: $
 * @author jz
 * @author $Author: ${lastedit}
 * 
 */
public class IndexesTestLocal extends TestCase {

    private final File descFile = new File("src/conf/plugdescription.xml");
	private final File folder = new File("./test_case_index", Indexer.class.getName());
	private final String cachePath = folder.getPath()+"/cache";

	@Override
	protected void setUp() throws Exception {
		folder.mkdirs();
	}

	@Override
	protected void tearDown() throws Exception {
		TestUtil.deleteDirectory(folder);
	}

	/**
	 * @throws Exception
	 */
	public void testIndexer() throws Exception {

		// read the PlugDescription
		XMLSerializer serializer = new XMLSerializer();
		serializer.aliasClass(PlugDescription.class.getName(), PlugDescription.class);
		PlugDescription desc = (PlugDescription)serializer.deSerialize(this.descFile);
		
		// prepare the cache
		Cache cache = (Cache)desc.get(ConfigurationKeys.CSW_CACHE);
		if (cache instanceof DefaultFileCache)
			((DefaultFileCache)cache).setCachePath(cachePath);
		cache.removeAllRecords();
		for(String id : TestData.getRecordIds())
			cache.putRecord(TestData.getRecord(id, ElementSetName.BRIEF, new GenericRecord()));

		Indexer indexer = new Indexer();
		indexer.open(folder);

		List<IDocumentReader> collection = DocumentReaderFactory.getDocumentReaderCollection(desc);
		for (IDocumentReader documentReader : collection) {
			indexer.index(documentReader);
		}

		indexer.close();
		DSCSearcher searcher = new DSCSearcher(new File(folder, "index"),
				"content");
		IngridHits hits = searcher.search(QueryStringParser.parse("1"), 0, 100);
		assertNotNull(hits);
		assertTrue(hits.getHits().length > 0);

		hits = searcher.search(QueryStringParser.parse("url:ur*"), 0, 100);
		assertNotNull(hits);
		assertTrue(hits.getHits().length > 0);

		hits = searcher.search(QueryStringParser.parse("title:title~"), 0, 100);
		assertNotNull(hits);
		assertTrue(hits.getHits().length > 0);
	}
}
