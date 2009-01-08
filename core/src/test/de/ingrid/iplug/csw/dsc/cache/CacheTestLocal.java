/*
 * Copyright (c) 2008 wemove digital solutions. All rights reserved.
 */

package de.ingrid.iplug.csw.dsc.cache;

import java.io.File;
import java.util.Set;

import junit.framework.TestCase;
import de.ingrid.iplug.csw.dsc.TestData;
import de.ingrid.iplug.csw.dsc.cache.impl.DefaultFileCache;
import de.ingrid.iplug.csw.dsc.cswclient.CSWRecord;
import de.ingrid.iplug.csw.dsc.cswclient.constants.ElementSetName;
import de.ingrid.iplug.csw.dsc.cswclient.impl.GenericRecord;

public class CacheTestLocal extends TestCase {
	
	final String CACHE_PATH = "./test_case_cache";

	public void testPut() throws Exception {
		
		String id = "1A0D667F-56E7-4EA7-9893-248F1658E0BF";
		ElementSetName elementSetName = ElementSetName.BRIEF;
		
		this.putRecord(id, elementSetName);
		
		File file = new File(CACHE_PATH+"/"+id+"_"+elementSetName+".xml");
		assertTrue("The record exists in the filesystem.", file.exists());
	}

	public void testExists() throws Exception {

		String id = "1A0D667F-56E7-4EA7-9893-248F1658E0BF";
		ElementSetName elementSetName = ElementSetName.BRIEF;
		
		this.putRecord(id, elementSetName);
		
		Cache cache = this.setupCache();
		assertTrue("The record exists in the cache.", cache.isCached(id, elementSetName));
		assertFalse("The record does not exist in the cache.", cache.isCached("12345", elementSetName));
	}

	public void testGet() throws Exception {

		String id = "1A0D667F-56E7-4EA7-9893-248F1658E0BF";
		ElementSetName elementSetName = ElementSetName.BRIEF;
		
		this.putRecord(id, elementSetName);
		
		Cache cache = this.setupCache();
		CSWRecord record = cache.getRecord(id, elementSetName, new GenericRecord());
		assertTrue("The cached record has the requested id.", id.equals(record.getId()));
	}

	public void testGetIds() throws Exception {

		String[] ids = new String[]{
				"1A0D667F-56E7-4EA7-9893-248F1658E0BF",
				"1A1CBD95-23BB-47D6-90D2-8DBCFBA07E32"
		};
		
		this.putRecord(ids[0], ElementSetName.BRIEF);
		this.putRecord(ids[1], ElementSetName.SUMMARY);
		
		Cache cache = this.setupCache();
		Set<String> cachedIds = cache.getCachedRecordIds();
		assertTrue("The first record is cached.", cachedIds.contains(ids[0]));
		assertTrue("The second record is cached.", cachedIds.contains(ids[1]));
		assertFalse("The record is not cached.", cachedIds.contains("12345"));
	}

	public void testRemoveRecord() throws Exception {

		String[] ids = new String[]{
				"1A0D667F-56E7-4EA7-9893-248F1658E0BF",
				"1A1CBD95-23BB-47D6-90D2-8DBCFBA07E32"
		};
		
		this.putRecord(ids[0], ElementSetName.BRIEF);
		this.putRecord(ids[1], ElementSetName.BRIEF);
		
		Cache cache = this.setupCache();
		cache.removeRecord(ids[1]);
		Set<String> cachedIds = cache.getCachedRecordIds();
		assertTrue("The first record is cached.", cachedIds.contains(ids[0]));
		assertTrue("The second record is removed.", !cachedIds.contains(ids[1]));
	}

	public void testRemoveAllRecords() throws Exception {

		String[] ids = new String[]{
				"1A0D667F-56E7-4EA7-9893-248F1658E0BF",
				"1A1CBD95-23BB-47D6-90D2-8DBCFBA07E32"
		};
		
		this.putRecord(ids[0], ElementSetName.BRIEF);
		this.putRecord(ids[1], ElementSetName.BRIEF);
		
		Cache cache = this.setupCache();
		cache.removeAllRecords();
		Set<String> cachedIds = cache.getCachedRecordIds();
		assertTrue("No files are cached.", cachedIds.size() == 0);
	}

	/**
	 * Helper methods
	 */

	protected void tearDown() {
		// remove all files from the cache
		File cacheLocation = new File(CACHE_PATH);
		if (cacheLocation.exists()) {
			File[] files = cacheLocation.listFiles();
			if (files != null) {
				for (int i=0; i<files.length; i++) {
					files[i].delete();
				}
			}
		}		
	}
	
	private Cache setupCache() {
		DefaultFileCache cache = new DefaultFileCache();
		cache.setCachePath(CACHE_PATH);
		return cache;
	}
	
	private void putRecord(String id, ElementSetName elementSetName) throws Exception {
		Cache cache = this.setupCache();
		CSWRecord record = TestData.getRecord(id, elementSetName, new GenericRecord());
		cache.putRecord(record);
	}
}
