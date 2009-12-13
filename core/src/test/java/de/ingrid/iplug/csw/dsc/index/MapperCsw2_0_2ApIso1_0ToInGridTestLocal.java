package de.ingrid.iplug.csw.dsc.index;

import junit.framework.TestCase;
import de.ingrid.iplug.csw.dsc.ConfigurationKeys;
import de.ingrid.iplug.csw.dsc.TestUtil;
import de.ingrid.iplug.csw.dsc.cswclient.CSWRecord;
import de.ingrid.iplug.csw.dsc.cswclient.constants.ElementSetName;
import de.ingrid.iplug.csw.dsc.cswclient.impl.GenericRecord;
import de.ingrid.iplug.csw.dsc.mapping.DocumentMapper;
import de.ingrid.iplug.csw.dsc.tools.SimpleSpringBeanFactory;
import de.ingrid.utils.dsc.Record;

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
public class MapperCsw2_0_2ApIso1_0ToInGridTestLocal extends TestCase {

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

		SimpleSpringBeanFactory.INSTANCE.setBeanConfig("beans_sdisuite.xml");

		DocumentMapper mapper = SimpleSpringBeanFactory.INSTANCE.getBean(ConfigurationKeys.CSW_MAPPER, DocumentMapper.class);
		
		CSWRecord cswRecord = TestUtil.getRecord("33462e89-e5ab-11c3-737d-b3a61366d028", ElementSetName.FULL, new GenericRecord());
		Record record = null;
		try {
			record = mapper.mapCswToIngrid(cswRecord);
		} catch (Throwable t) {
			System.out.println(t);
		}
		
		assertTrue("Detail record found.", record != null);
		assertTrue("Subrecords not null.", record.getSubRecords() != null);
		assertTrue("Subrecords found.", record.getSubRecords().length > 0);

		
		cswRecord = TestUtil.getRecord("550e8400-e29b-41d4-a716-446655441234", ElementSetName.FULL, new GenericRecord());
		record = null;
		try {
			record = mapper.mapCswToIngrid(cswRecord);
		} catch (Throwable t) {
			System.out.println(t);
		}
		
		assertTrue("Detail record found.", record != null);
		assertTrue("Subrecords not null.", record.getSubRecords() != null);
		assertTrue("Subrecords found.", record.getSubRecords().length > 0);

		cswRecord = TestUtil.getRecord("10453eff-59fa-42e9-a3e1-6e3cd99e2a05", ElementSetName.FULL, new GenericRecord());
		record = null;
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
