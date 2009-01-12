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
import de.ingrid.iplug.csw.dsc.cswclient.constants.OutputFormat;
import de.ingrid.iplug.csw.dsc.cswclient.impl.GenericRecord;
import de.ingrid.utils.PlugDescription;

public class UpdateJobTestLocal extends TestCase {

    private final String cachePath = "./test_case_updatejob";
	
	public void testExecute() throws Exception {
		
		PlugDescription desc = TestUtil.getPlugDescription();
		
		// get instances from plugdescription
		CSWFactory factory = (CSWFactory)desc.get(ConfigurationKeys.CSW_FACTORY);
		factory.setQueryTemplate((CSWQuery)desc.get(ConfigurationKeys.CSW_QUERY_TEMPLATE));
		Cache cache = (Cache)desc.get(ConfigurationKeys.CSW_CACHE);
		cache.configure(factory);
		if (cache instanceof DefaultFileCache)
			((DefaultFileCache)cache).setCachePath(cachePath);
		
        // check query configuration
		CSWQuery q = factory.createQuery();
		OutputFormat l = OutputFormat.valueOf("TEXT_XML");
		assertTrue("output format is application/xml", l.equals(q.getOutputFormat()));
		
		// put old record into cache
		String oldRecordId = "A-12345";
		cache.putRecord(TestUtil.getRecord(oldRecordId, ElementSetName.BRIEF, new GenericRecord()));

		// start transaction
		Cache tmpCache = cache.startTransaction();
		tmpCache.removeAllRecords();
		
		// run the update job for all elementset names
		UpdateJob job = new UpdateJob();
		job.configure(factory, tmpCache, (String)desc.get(ConfigurationKeys.CSW_HARVEST_FILTER));
		
		ElementSetName[] names = ElementSetName.values();
		for (int i=0; i<names.length; i++)
			job.execute(names[i], 20, 2000);

		// commit transaction
		tmpCache.commitTransaction();
		
		// check for a cached record
		String id = "1AFDCB03-3818-40F1-9560-9FB082956357";
		CSWRecord recordBrief = cache.getRecord(id, ElementSetName.BRIEF);
		assertTrue("record "+id+" was cached", recordBrief.getId().equals(id));
		CSWRecord recordSummary = cache.getRecord(id, ElementSetName.SUMMARY);
		assertTrue("record "+id+" was cached", recordSummary.getId().equals(id));
		CSWRecord recordFull = cache.getRecord(id, ElementSetName.FULL);
		assertTrue("record "+id+" was cached", recordFull.getId().equals(id));
		
		// check if old record is removed
		assertTrue("record "+oldRecordId+" is removed", !cache.isCached(oldRecordId, ElementSetName.BRIEF));
	}
}
