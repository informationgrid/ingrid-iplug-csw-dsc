package de.ingrid.iplug.csw.dsc.index;

import java.util.Iterator;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.lucene.document.Document;
import org.springframework.core.io.FileSystemResource;
import org.w3c.dom.NodeList;

import de.ingrid.admin.search.GermanStemmer;
import de.ingrid.iplug.csw.dsc.ConfigurationKeys;
import de.ingrid.iplug.csw.dsc.TestUtil;
import de.ingrid.iplug.csw.dsc.cache.Cache;
import de.ingrid.iplug.csw.dsc.cswclient.CSWFactory;
import de.ingrid.iplug.csw.dsc.cswclient.CSWRecord;
import de.ingrid.iplug.csw.dsc.cswclient.constants.ElementSetName;
import de.ingrid.iplug.csw.dsc.cswclient.impl.GenericRecord;
import de.ingrid.iplug.csw.dsc.index.mapper.ScriptedDocumentMapper;
import de.ingrid.iplug.csw.dsc.om.CswCacheSourceRecord;
import de.ingrid.iplug.csw.dsc.tools.LuceneTools;
import de.ingrid.iplug.csw.dsc.tools.SimpleSpringBeanFactory;
import de.ingrid.utils.tool.StringUtil;
import de.ingrid.utils.xml.Csw202NamespaceContext;
import de.ingrid.utils.xpath.XPathUtils;

public class MapperToIndexTest extends TestCase {

    final private XPathUtils xPathUtils = new XPathUtils(new Csw202NamespaceContext());
    
    
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
        // is autowired in spring environment !
        LuceneTools tmpLuceneTools = new LuceneTools();
        tmpLuceneTools.setDefaultStemmer(new GermanStemmer());

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
			String mdBrowseGraphic_FileName = xPathUtils.getString(cswRecord.getOriginalResponse(), "//gmd:identificationInfo//gmd:graphicOverview/gmd:MD_BrowseGraphic/gmd:fileName/gco:CharacterString");
			if (mdBrowseGraphic_FileName != null) {
			    assertFalse("MD_BrowseGraphic is mapped as link", (doc.getValues("t017_url_ref.url_link").length > 0 && mdBrowseGraphic_FileName.equals(doc.getValues("t017_url_ref.url_link")[0])) || (doc.getValues("t017_url_ref.url_link").length > 1 && mdBrowseGraphic_FileName.equals(doc.getValues("t017_url_ref.url_link")[1])));
			}
            String fileIdentifier = xPathUtils.getString(cswRecord.getOriginalResponse(), "//gmd:MD_Metadata/gmd:fileIdentifier/gco:CharacterString");
            assertTrue("fileIdentifier is not mapped", fileIdentifier.equals(doc.getValues("t01_object.obj_id")[0]));

            // check gmd:referenceSystemInfo
        	NodeList rsIdentifiers = xPathUtils.getNodeList(cswRecord.getOriginalResponse(), "//gmd:referenceSystemInfo/gmd:MD_ReferenceSystem/gmd:referenceSystemIdentifier/gmd:RS_Identifier");
        	if (rsIdentifiers != null) {
        		for (int i=0; i<rsIdentifiers.getLength(); i++ ) {
        			String code = xPathUtils.getString(rsIdentifiers.item(i), "gmd:code/gco:CharacterString");
        			String codeSpace = xPathUtils.getString(rsIdentifiers.item(i), "gmd:codeSpace/gco:CharacterString");
                    String val = code;
        			if (codeSpace != null && code != null) {
                        val = codeSpace + ":" + code;
        			}
        			if (val != null) {
                        assertTrue("spatial_system.referencesystem_value is not mapped", StringUtil.containsString(doc.getValues("spatial_system.referencesystem_value"), val));        				
                        assertTrue("t011_obj_geo.referencesystem_id", StringUtil.containsString(doc.getValues("t011_obj_geo.referencesystem_id"), val));        				
        			}
        		}
        	}
		}
		
		
	}
}
