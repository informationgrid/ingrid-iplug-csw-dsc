package de.ingrid.iplug.csw.dsc.index;

import java.util.Iterator;
import java.util.Set;

import junit.framework.TestCase;
import de.ingrid.iplug.csw.dsc.ConfigurationKeys;
import de.ingrid.iplug.csw.dsc.TestUtil;
import de.ingrid.iplug.csw.dsc.cswclient.CSWRecord;
import de.ingrid.iplug.csw.dsc.cswclient.constants.ElementSetName;
import de.ingrid.iplug.csw.dsc.cswclient.impl.GenericRecord;
import de.ingrid.iplug.csw.dsc.mapping.DocumentMapper;
import de.ingrid.iplug.csw.dsc.tools.SimpleSpringBeanFactory;
import de.ingrid.utils.dsc.Record;

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

		DocumentMapper mapper = SimpleSpringBeanFactory.INSTANCE.getBean(ConfigurationKeys.CSW_MAPPER, DocumentMapper.class);
		Set<String> testRecordIds = TestUtil.getRecordIds();
		for (Iterator<String> it = testRecordIds.iterator(); it.hasNext();) {
			String testRecordId = it.next();
			CSWRecord cswRecord = TestUtil.getRecord(testRecordId, ElementSetName.FULL, new GenericRecord());
			Record record = null;
			try {
				record = mapper.mapCswToIngrid(cswRecord);
			} catch (Throwable t) {
				System.out.println(t);
			}
			
			assertTrue("Detail record found.", record != null);
			assertTrue("Subrecords not null.", record.getSubRecords() != null);
			assertTrue("Subrecords found.", record.getSubRecords().length > 0);
		}
	}
}
