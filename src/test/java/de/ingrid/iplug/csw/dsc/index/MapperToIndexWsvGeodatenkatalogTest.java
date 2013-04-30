package de.ingrid.iplug.csw.dsc.index;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.lucene.document.Document;
import org.springframework.core.io.FileSystemResource;

import de.ingrid.admin.search.GermanStemmer;
import de.ingrid.iplug.csw.dsc.ConfigurationKeys;
import de.ingrid.iplug.csw.dsc.TestUtil;
import de.ingrid.iplug.csw.dsc.cache.Cache;
import de.ingrid.iplug.csw.dsc.cswclient.CSWFactory;
import de.ingrid.iplug.csw.dsc.cswclient.CSWRecord;
import de.ingrid.iplug.csw.dsc.cswclient.constants.ElementSetName;
import de.ingrid.iplug.csw.dsc.cswclient.impl.GenericRecord;
import de.ingrid.iplug.csw.dsc.index.mapper.IRecordMapper;
import de.ingrid.iplug.csw.dsc.index.mapper.ScriptedDocumentMapper;
import de.ingrid.iplug.csw.dsc.om.CswCacheSourceRecord;
import de.ingrid.iplug.csw.dsc.tools.LuceneTools;
import de.ingrid.iplug.csw.dsc.tools.SimpleSpringBeanFactory;

public class MapperToIndexWsvGeodatenkatalogTest extends TestCase {

	@Override
	protected void setUp() throws Exception {
	}

	@Override
	protected void tearDown() throws Exception {
	}

	/**
	 * @throws Exception
	 */
	public void testMapper() throws Exception {

		SimpleSpringBeanFactory.INSTANCE.setBeanConfig("beans_mapper_test.xml");

		Cache cache = SimpleSpringBeanFactory.INSTANCE.getBean(ConfigurationKeys.CSW_CACHE, Cache.class);
        CSWFactory factory = SimpleSpringBeanFactory.INSTANCE.getBean(ConfigurationKeys.CSW_FACTORY, CSWFactory.class);
        cache.configure(factory);
		
        // is autowired in spring environment !
        LuceneTools tmpLuceneTools = new LuceneTools();
        tmpLuceneTools.setDefaultStemmer(new GermanStemmer());

        // PROCESS MULTIPLE MAPPERS !
        List<IRecordMapper> myMappers = new ArrayList<IRecordMapper>();
        
		ScriptedDocumentMapper mapper = new ScriptedDocumentMapper();
		mapper.setCompile(false);
        mapper.setMappingScript(new FileSystemResource("src/main/resources/mapping/csw-2.0.2-AP-ISO-1.0_to_lucene-igc-1.0.3.js"));
        myMappers.add(mapper);
        
        // WSV "Fix"
        // see https://dev2.wemove.com/jira/browse/GEOPORTALWSV-39
		mapper = new ScriptedDocumentMapper();
		mapper.setCompile(false);
        mapper.setMappingScript(new FileSystemResource("src/main/release/presets/wsv/mapping/post_process_lucene_wsv_geodatenkatalog.js"));
        myMappers.add(mapper);

		Set<String> testRecordIds = TestUtil.getRecordIds();
		for (Iterator<String> it = testRecordIds.iterator(); it.hasNext();) {
			String testRecordId = it.next();
			CSWRecord cswRecord = TestUtil.getRecord(testRecordId, ElementSetName.FULL, new GenericRecord());
			Document doc = new Document();
			
			for (IRecordMapper myMapper : myMappers) {
//				try {
					myMapper.map(new CswCacheSourceRecord(cswRecord), doc);

					// check valid URLs
					System.out.println("\nAfter Mapping by Mapper '" + myMapper + "': Content of t017_url_ref.url_link");
					for (String url : doc.getValues("t017_url_ref.url_link")) {
						System.out.println(url);
					}
					System.out.println();
/*					
				} catch (Throwable t) {
					System.out.println(t);
				}
*/
			}
			
			assertTrue("Lucene doc found.", doc != null);
			assertEquals(testRecordId, doc.get("t01_object.obj_id"));
			
			// check valid URLs
			for (String url : doc.getValues("t017_url_ref.url_link")) {
				assertTrue(!url.startsWith("/"));
			}
		}
	}
}
