package de.ingrid.iplug.csw.dsc.index;

import java.util.Iterator;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.lucene.document.Document;
import org.springframework.core.io.FileSystemResource;

import de.ingrid.iplug.csw.dsc.ConfigurationKeys;
import de.ingrid.iplug.csw.dsc.TestUtil;
import de.ingrid.iplug.csw.dsc.cache.Cache;
import de.ingrid.iplug.csw.dsc.cswclient.CSWFactory;
import de.ingrid.iplug.csw.dsc.cswclient.CSWRecord;
import de.ingrid.iplug.csw.dsc.cswclient.constants.ElementSetName;
import de.ingrid.iplug.csw.dsc.cswclient.impl.GenericRecord;
import de.ingrid.iplug.csw.dsc.index.mapper.ScriptedDocumentMapper;
import de.ingrid.iplug.csw.dsc.om.CswCacheSourceRecord;
import de.ingrid.iplug.csw.dsc.tools.SimpleSpringBeanFactory;
import de.ingrid.utils.xml.XPathUtils;

public class MapperToIndexTest extends TestCase {

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
		
		ScriptedDocumentMapper mapper = new ScriptedDocumentMapper();
		mapper.setCompile(false);
        mapper.setMappingScript(new FileSystemResource("src/main/resources/mapping/csw-2.0.2-AP-ISO-1.0_to_lucene-igc-1.0.3.js"));
		Set<String> testRecordIds = TestUtil.getRecordIds();
		for (Iterator<String> it = testRecordIds.iterator(); it.hasNext();) {
			String testRecordId = it.next();
			CSWRecord cswRecord = TestUtil.getRecord(testRecordId, ElementSetName.FULL, new GenericRecord());
			Document doc = new Document();
			try {
				mapper.map(new CswCacheSourceRecord(cswRecord), doc);
			} catch (Throwable t) {
				System.out.println(t);
			}
			
			assertTrue("Lucene doc found.", doc != null);
			assertEquals(testRecordId, doc.get("t01_object.obj_id"));
			assertTrue("Valid hierarchyLevel set.", Integer.parseInt(doc.get("t01_object.obj_class")) >= 0 && Integer.parseInt(doc.get("t01_object.obj_class")) <= 5);
			String mdBrowseGraphic_FileName = XPathUtils.getString(cswRecord.getOriginalResponse(), "//gmd:identificationInfo//gmd:graphicOverview/gmd:MD_BrowseGraphic/gmd:fileName/gco:CharacterString");
			assertTrue("MD_BrowseGraphic is not set or is mapped as link", mdBrowseGraphic_FileName == null || mdBrowseGraphic_FileName.equals(doc.getValues("t017_url_ref.url_link")[0]) || mdBrowseGraphic_FileName.equals(doc.getValues("t017_url_ref.url_link")[1]));
            String fileIdentifier = XPathUtils.getString(cswRecord.getOriginalResponse(), "//gmd:MD_Metadata/gmd:fileIdentifier/gco:CharacterString");
            assertTrue("fileIdentifier is not mapped", fileIdentifier.equals(doc.getValues("t01_object.obj_id")[0]));
		}
		
		
	}
}
