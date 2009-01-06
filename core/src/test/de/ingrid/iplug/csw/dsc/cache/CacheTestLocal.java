/*
 * Copyright (c) 2008 wemove digital solutions. All rights reserved.
 */

package de.ingrid.iplug.csw.dsc.cache;

import java.io.File;
import java.io.StringReader;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import junit.framework.TestCase;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import de.ingrid.iplug.csw.dsc.cache.impl.DefaultFileCache;
import de.ingrid.iplug.csw.dsc.cswclient.CSWRecord;
import de.ingrid.iplug.csw.dsc.cswclient.constants.ElementSetName;
import de.ingrid.iplug.csw.dsc.cswclient.impl.GenericRecord;

public class CacheTestLocal extends TestCase {
	
	final String CACHE_PATH = "./test_case_cache";

	public void testPut() throws Exception {
		
		String id = "1AFDCB03-3818-40F1-9560-9FB082956357";
		ElementSetName elementSetName = ElementSetName.BRIEF;
		
		this.putRecord(id, elementSetName);
		
		File file = new File(CACHE_PATH+"/"+id+"_"+elementSetName+".xml");
		assertTrue("The record exists in the filesystem.", file.exists());
	}

	public void testExists() throws Exception {

		String id = "1AFDCB03-3818-40F1-9560-9FB082956357";
		ElementSetName elementSetName = ElementSetName.BRIEF;
		
		this.putRecord(id, elementSetName);
		
		Cache cache = this.setupCache();
		assertTrue("The record exists in the cache.", cache.isCached(id, elementSetName));
		assertFalse("The record does not exist in the cache.", cache.isCached("12345", elementSetName));
	}

	public void testGet() throws Exception {

		String id = "1AFDCB03-3818-40F1-9560-9FB082956357";
		ElementSetName elementSetName = ElementSetName.BRIEF;
		
		this.putRecord(id, elementSetName);
		
		Cache cache = this.setupCache();
		CSWRecord record = cache.getRecord(id, elementSetName, new GenericRecord());
		assertTrue("The cached record has the requested id.", id.equals(record.getId()));
	}

	public void testGetIds() throws Exception {

		String[] ids = new String[]{
				"1AFDCB03-3818-40F1-9560-9FB082956357",
				"1AFDCB03-3818-40F1-9560-9FB082956358"
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
				"1AFDCB03-3818-40F1-9560-9FB082956357",
				"1AFDCB03-3818-40F1-9560-9FB082956358"
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
				"1AFDCB03-3818-40F1-9560-9FB082956357",
				"1AFDCB03-3818-40F1-9560-9FB082956358"
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
		CSWRecord record = new GenericRecord();
		record.initialize(elementSetName, this.getRecordNode(id));
		cache.putRecord(record);
	}
	
	private Document getRecordNode(String id) throws Exception {
		String xmlStr = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"+
		"<iso19115brief:MD_Metadata xmlns:iso19115brief=\"http://schemas.opengis.net/iso19115brief\" "+
		"xmlns:iso19119=\"http://schemas.opengis.net/iso19119\" "+
		"xmlns:smXML=\"http://metadata.dgiwg.org/smXML\">"+
		"<fileIdentifier>"+
		"<smXML:CharacterString>"+id+"</smXML:CharacterString>"+
		"</fileIdentifier>"+
		"<hierarchyLevel>"+
		"<smXML:MD_ScopeCode codeList=\"http://www.tc211.org/ISO19139/resources/codeList.xml?MD_ScopeCode\" codeListValue=\"dataset\" />"+
		"</hierarchyLevel>"+
		"<iso19115brief:identificationInfo>"+
		"<smXML:MD_DataIdentification>"+
		"<smXML:title>"+
		"<smXML:CharacterString>Landschaftsschutzgebiete Deutschlands</smXML:CharacterString>"+
		"</smXML:title>"+
		"<smXML:topicCategory>"+
		"<smXML:MD_TopicCategoryCode>7</smXML:MD_TopicCategoryCode>"+
		"</smXML:topicCategory>"+
		"</smXML:MD_DataIdentification>"+
		"</iso19115brief:identificationInfo>"+
		"</iso19115brief:MD_Metadata>";
		
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
		return docBuilder.parse(new InputSource(new StringReader(xmlStr)));		
	}
}
