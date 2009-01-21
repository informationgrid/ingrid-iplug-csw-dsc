package de.ingrid.iplug.csw.dsc.index;

import java.io.File;
import java.util.HashSet;
import java.util.List;

import org.hsqldb.lib.Set;

import junit.framework.TestCase;
import de.ingrid.iplug.csw.dsc.ConfigurationKeys;
import de.ingrid.iplug.csw.dsc.TestUtil;
import de.ingrid.iplug.csw.dsc.cache.Cache;
import de.ingrid.iplug.csw.dsc.cache.impl.DefaultFileCache;
import de.ingrid.iplug.csw.dsc.cswclient.CSWFactory;
import de.ingrid.iplug.csw.dsc.cswclient.constants.ElementSetName;
import de.ingrid.iplug.csw.dsc.cswclient.impl.GenericRecord;
import de.ingrid.iplug.csw.dsc.mapping.DocumentMapper;
import de.ingrid.utils.IngridHits;
import de.ingrid.utils.PlugDescription;
import de.ingrid.utils.dsc.Record;
import de.ingrid.utils.queryparser.QueryStringParser;

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
public class IndexesCsw2_0_2ApIso1_0TestLocal extends TestCase {

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
		PlugDescription desc = TestUtil.getPlugDescription();
		desc.setWorkinDirectory(folder);

		CSWFactory factory = (CSWFactory)desc.get(ConfigurationKeys.CSW_FACTORY);
		DocumentMapper mapper = (DocumentMapper)desc.get(ConfigurationKeys.CSW_MAPPER);
		
		// prepare the cache
		Cache cache = (Cache)desc.get(ConfigurationKeys.CSW_CACHE);
		cache.configure(factory);
		if (cache instanceof DefaultFileCache)
			((DefaultFileCache)cache).setCachePath(cachePath);
		
		// start transaction
		Cache tmpCache = cache.startTransaction();
		tmpCache.removeAllRecords();
		
		HashSet<String> recordIds = new HashSet<String>();
		// service
		recordIds.add("33462e89-e5ab-11c3-737d-b3a61366d028");
		// dataset
		recordIds.add("550e8400-e29b-41d4-a716-446655441234");
		
		// fill tmp cache
		for(String id : recordIds) {
			tmpCache.putRecord(TestUtil.getRecord(id, ElementSetName.FULL, new GenericRecord()));
		}

		// run indexer
		Indexer indexer = new Indexer();
		indexer.open(folder);
		List<IDocumentReader> collection = DocumentReaderFactory.getDocumentReaderCollection(tmpCache, mapper);
		for (IDocumentReader documentReader : collection) {
			indexer.index(documentReader);
		}
		indexer.close();
		
		// commit transaction
		tmpCache.commitTransaction();
		
		// do some search tests
		DSCSearcher searcher = new DSCSearcher(new File(folder, "index"), "content");
		searcher.configure(desc);
		
		IngridHits hits = searcher.search(QueryStringParser.parse("1"), 0, 100);
		assertNotNull(hits);
		assertTrue(hits.getHits().length > 0);

		hits = searcher.search(QueryStringParser.parse("title:JR*"), 0, 100);
		assertNotNull(hits);
		assertTrue(hits.getHits().length > 0);
		
		Record record = searcher.getRecord(hits.getHits()[0]);
		assertTrue("Detail record found.", record != null);
	}
}
