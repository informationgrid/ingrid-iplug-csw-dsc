package de.ingrid.iplug.csw.dsc.record;

import java.util.Iterator;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;

import junit.framework.TestCase;
import de.ingrid.iplug.csw.dsc.ConfigurationKeys;
import de.ingrid.iplug.csw.dsc.TestUtil;
import de.ingrid.iplug.csw.dsc.cache.Cache;
import de.ingrid.iplug.csw.dsc.cswclient.CSWFactory;
import de.ingrid.iplug.csw.dsc.cswclient.CSWRecord;
import de.ingrid.iplug.csw.dsc.cswclient.constants.ElementSetName;
import de.ingrid.iplug.csw.dsc.cswclient.impl.GenericRecord;
import de.ingrid.iplug.csw.dsc.om.CswCacheSourceRecord;
import de.ingrid.iplug.csw.dsc.record.mapper.CreateIdfMapper;
import de.ingrid.iplug.csw.dsc.record.mapper.CswIdfMapper;
import de.ingrid.iplug.csw.dsc.tools.SimpleSpringBeanFactory;
import de.ingrid.utils.xml.XMLUtils;
import de.ingrid.utils.xml.XPathUtils;

public class MapperToIngridTest extends TestCase {

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
		
		
        CreateIdfMapper createIdfMapper = new CreateIdfMapper();
        CswIdfMapper cswIdfMapper = new CswIdfMapper();
		Set<String> testRecordIds = TestUtil.getRecordIds();
		for (Iterator<String> it = testRecordIds.iterator(); it.hasNext();) {
			String testRecordId = it.next();
			CSWRecord cswRecord = TestUtil.getRecord(testRecordId, ElementSetName.FULL, new GenericRecord());
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            DocumentBuilder docBuilder = dbf.newDocumentBuilder();
            org.w3c.dom.Document idfDoc = docBuilder.newDocument();
			try {
			    createIdfMapper.map(new CswCacheSourceRecord(cswRecord), idfDoc);
			    cswIdfMapper.map(new CswCacheSourceRecord(cswRecord), idfDoc);
			} catch (Throwable t) {
				System.out.println(t);
			}
			
			assertTrue("Idf found.", idfDoc.hasChildNodes());
			System.out.println(XMLUtils.toString(idfDoc));
			XPath xpath = XPathUtils.getXPathInstance();
			assertTrue("Metadata found.", XPathUtils.nodeExists(idfDoc, "//gmd:MD_Metadata"));
		}
	}
}
