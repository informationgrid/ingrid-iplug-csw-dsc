package de.ingrid.iplug.csw.dsc.index;

import java.io.File;
import java.util.List;

import junit.framework.TestCase;
import de.ingrid.iplug.csw.dsc.TestUtil;
import de.ingrid.utils.IngridHits;
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
public class IndexesTest extends TestCase {

	private File _folder = new File(System.getProperty("java.io.tempdir"),
			Indexer.class.getName());

	@Override
	protected void setUp() throws Exception {
		//assertTrue(_folder.mkdirs());
	}

	@Override
	protected void tearDown() throws Exception {
		//assertTrue(TestUtil.deleteDirectory(_folder));
	}

	/**
	 * @throws Exception
	 */
	public void testIndexer() throws Exception {
		/*
		Indexer indexer = new Indexer();
		indexer.open(_folder);

		List<IDocumentReader> collection = DocumentReaderFactory
				.getDocumentReaderCollection();
		for (IDocumentReader documentReader : collection) {
			indexer.index(documentReader);
		}

		indexer.close();
		DSCSearcher searcher = new DSCSearcher(new File(_folder, "index"),
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
		*/
	}

}
