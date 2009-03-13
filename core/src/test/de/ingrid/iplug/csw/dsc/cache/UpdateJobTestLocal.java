/*
 * Copyright (c) 2008 wemove digital solutions. All rights reserved.
 */

package de.ingrid.iplug.csw.dsc.cache;

import junit.framework.TestCase;
import de.ingrid.iplug.csw.dsc.ConfigurationKeys;
import de.ingrid.iplug.csw.dsc.TestUtil;
import de.ingrid.iplug.csw.dsc.cache.impl.DefaultFileCache;
import de.ingrid.iplug.csw.dsc.cswclient.CSWFactory;
import de.ingrid.iplug.csw.dsc.cswclient.CSWQuery;
import de.ingrid.iplug.csw.dsc.cswclient.CSWRecord;
import de.ingrid.iplug.csw.dsc.cswclient.constants.ElementSetName;
import de.ingrid.iplug.csw.dsc.cswclient.impl.GenericRecord;
import de.ingrid.iplug.csw.dsc.tools.SimpleSpringBeanFactory;

public class UpdateJobTestLocal extends TestCase {

    private final String cachePath = "./test_case_updatejob";
	
	public void testExecute() throws Exception {
		
		// get instances from spring configuration
		/*
		SimpleSpringBeanFactory.INSTANCE.setBeanConfig("beans_portalu.xml");
		String id = "114CAFCF-5665-480A-853F-1F8370D302FE";
		*/
		//*
		SimpleSpringBeanFactory.INSTANCE.setBeanConfig("beans_sdisuite.xml");
		String id = "655e5998-a20e-66b5-c888-00005553421";
		//*/
		/*
		SimpleSpringBeanFactory.INSTANCE.setBeanConfig("beans_ieris.xml");
		String id = "DGF06323L7_b04";
		*/
		/*
		SimpleSpringBeanFactory.INSTANCE.setBeanConfig("beans_bbsr.xml");
		String id = "XYZ";
		*/
		/*
		SimpleSpringBeanFactory.INSTANCE.setBeanConfig("beans_egn.xml");
		String id = "XYZ";
		*/
		/*
		SimpleSpringBeanFactory.INSTANCE.setBeanConfig("beans_harrison.xml");
		String id = "1162C06F-7C34-11D6-BD62-0050DA46952F";
		*/

		CSWFactory factory = SimpleSpringBeanFactory.INSTANCE.getBean(ConfigurationKeys.CSW_FACTORY, CSWFactory.class);
		factory.setQueryTemplate(SimpleSpringBeanFactory.INSTANCE.getBean(ConfigurationKeys.CSW_QUERY_TEMPLATE, CSWQuery.class));
		Cache cache = SimpleSpringBeanFactory.INSTANCE.getBean(ConfigurationKeys.CSW_CACHE, Cache.class);
		cache.configure(factory);
		if (cache instanceof DefaultFileCache)
			((DefaultFileCache)cache).setCachePath(cachePath);
		
		// put old record into cache
		String oldRecordId = "A-12345";
		cache.putRecord(TestUtil.getRecord(oldRecordId, ElementSetName.BRIEF, new GenericRecord()));

		// start transaction
		Cache tmpCache = cache.startTransaction();
		tmpCache.removeAllRecords();
		
		// run the update job
		UpdateJob job = new UpdateJob(factory, tmpCache);
		job.execute(20, 2000);

		// commit transaction
		tmpCache.commitTransaction();
		
		// check for a cached record
		CSWRecord recordBrief = cache.getRecord(id, ElementSetName.BRIEF);
		assertTrue("record "+id+" was cached", recordBrief.getId().equals(id));
		CSWRecord recordSummary = cache.getRecord(id, ElementSetName.SUMMARY);
		assertTrue("record "+id+" was cached", recordSummary.getId().equals(id));
		CSWRecord recordFull = cache.getRecord(id, ElementSetName.FULL);
		assertTrue("record "+id+" was cached", recordFull.getId().equals(id));
		
		// check if all records are cached in all elementset names
		for (String cachedId : cache.getCachedRecordIds()) {
			assertTrue("all records are cached in all elementset names", 
					cache.isCached(cachedId, ElementSetName.BRIEF) && 
					cache.isCached(cachedId, ElementSetName.SUMMARY) &&
					cache.isCached(cachedId, ElementSetName.FULL));
		}
		
		// check if old record is removed
		assertTrue("record "+oldRecordId+" is removed", !cache.isCached(oldRecordId, ElementSetName.BRIEF));
	}
}
