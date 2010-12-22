package de.ingrid.iplug.csw.dsc.index;

import java.util.Iterator;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.lucene.document.Document;

import de.ingrid.iplug.csw.dsc.ConfigurationKeys;
import de.ingrid.iplug.csw.dsc.TestUtil;
import de.ingrid.iplug.csw.dsc.cswclient.CSWRecord;
import de.ingrid.iplug.csw.dsc.cswclient.constants.ElementSetName;
import de.ingrid.iplug.csw.dsc.cswclient.impl.GenericRecord;
import de.ingrid.iplug.csw.dsc.mapping.DocumentMapper;
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

		DocumentMapper mapper = SimpleSpringBeanFactory.INSTANCE.getBean(ConfigurationKeys.CSW_MAPPER, DocumentMapper.class);
		Set<String> testRecordIds = TestUtil.getRecordIds();
		for (Iterator<String> it = testRecordIds.iterator(); it.hasNext();) {
			String testRecordId = it.next();
			CSWRecord cswRecord = TestUtil.getRecord(testRecordId, ElementSetName.FULL, new GenericRecord());
			Document doc = null;
			try {
				doc = mapper.mapCswToLucene(cswRecord);
			} catch (Throwable t) {
				System.out.println(t);
			}
			
			assertTrue("Lucene doc found.", doc != null);
			assertEquals(testRecordId, doc.get("t01_object.obj_id"));
			assertTrue("Valid hierarchyLevel set.", Integer.parseInt(doc.get("t01_object.obj_class")) >= 0 && Integer.parseInt(doc.get("t01_object.obj_class")) <= 5);
			String mdBrowseGraphic_FileName = XPathUtils.getString(cswRecord.getOriginalResponse(), "//gmd:identificationInfo//gmd:graphicOverview/gmd:MD_BrowseGraphic/gmd:fileName/gco:CharacterString");
			assertTrue("MD_BrowseGraphic is not set or is mapped as link", mdBrowseGraphic_FileName == null || mdBrowseGraphic_FileName.equals(doc.getValues("t017_url_ref.url_link")[0]) || mdBrowseGraphic_FileName.equals(doc.getValues("t017_url_ref.url_link")[1]));
		}
		
		
	}
}
