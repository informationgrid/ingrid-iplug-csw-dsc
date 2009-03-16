package de.ingrid.iplug.csw.dsc.index;

import java.io.File;
import java.util.List;

import org.w3c.dom.Node;

import junit.framework.TestCase;
import de.ingrid.iplug.csw.dsc.ConfigurationKeys;
import de.ingrid.iplug.csw.dsc.TestUtil;
import de.ingrid.iplug.csw.dsc.cache.Cache;
import de.ingrid.iplug.csw.dsc.cache.impl.DefaultFileCache;
import de.ingrid.iplug.csw.dsc.cswclient.CSWFactory;
import de.ingrid.iplug.csw.dsc.cswclient.constants.ElementSetName;
import de.ingrid.iplug.csw.dsc.cswclient.impl.GenericRecord;
import de.ingrid.iplug.csw.dsc.mapping.DocumentMapper;
import de.ingrid.iplug.csw.dsc.tools.SimpleSpringBeanFactory;
import de.ingrid.iplug.csw.dsc.tools.StringUtils;
import de.ingrid.utils.IngridHitDetail;
import de.ingrid.utils.IngridHits;
import de.ingrid.utils.PlugDescription;
import de.ingrid.utils.dsc.Record;
import de.ingrid.utils.query.IngridQuery;
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
public class IndexesTestLocal extends TestCase {

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
		SimpleSpringBeanFactory.INSTANCE.setBeanConfig("beans_sdisuite.xml");
		CSWFactory factory = SimpleSpringBeanFactory.INSTANCE.getBean(ConfigurationKeys.CSW_FACTORY, CSWFactory.class);
		DocumentMapper mapper = SimpleSpringBeanFactory.INSTANCE.getBean(ConfigurationKeys.CSW_MAPPER, DocumentMapper.class);
		PlugDescription desc = SimpleSpringBeanFactory.INSTANCE.getBean(ConfigurationKeys.PLUGDESCRIPTION, PlugDescription.class);
		desc.setWorkinDirectory(folder);
		
		// prepare the cache
		Cache cache = SimpleSpringBeanFactory.INSTANCE.getBean(ConfigurationKeys.CSW_CACHE, Cache.class);
		cache.configure(factory);
		if (cache instanceof DefaultFileCache)
			((DefaultFileCache)cache).setCachePath(cachePath);
		
		// start transaction
		Cache tmpCache = cache.startTransaction();
		tmpCache.removeAllRecords();
		
		// fill tmp cache
		for(String id : TestUtil.getRecordIds()) {
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

		hits = searcher.search(QueryStringParser.parse("title:Karte*"), 0, 100);
		assertNotNull(hits);
		assertTrue(hits.getHits().length > 0);
		
		Record record = searcher.getRecord(hits.getHits()[0]);
		assertTrue("Detail record found.", record != null);
		
		// test direct response
		if (desc.getBoolean("directData") == Boolean.TRUE) {
			IngridQuery query = QueryStringParser.parse("1");
			query.put("cswDirectResponse", ElementSetName.FULL.toString());

			// getDetails
			hits = searcher.search(query, 0, 100);
			IngridHitDetail[] details = searcher.getDetails(hits.getHits(), query, new String[]{});
			for (IngridHitDetail detail : details) {
				assertTrue("Detail has original response", detail.containsKey("cswData"));
				assertTrue("Response string length > 0", StringUtils.nodeToString((Node)detail.get("cswData")).length() > 0);
			}

			// getDetail
			IngridHitDetail detail = searcher.getDetail(hits.getHits()[0], query, new String[]{});
			assertTrue("Detail has original response", detail.containsKey("cswData"));
			assertTrue("Response string length > 0", StringUtils.nodeToString((Node)detail.get("cswData")).length() > 0);
			
			// getRecord
			record = searcher.getRecord(hits.getHits()[0]);
			assertTrue("Detail has original response", record.containsKey("cswData"));
			assertTrue("Response string length > 0", StringUtils.nodeToString((Node)record.get("cswData")).length() > 0);
		}
	}
}
