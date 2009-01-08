/*
 * Copyright (c) 2009 wemove digital solutions. All rights reserved.
 */

package de.ingrid.iplug.csw.dsc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashSet;
import java.util.Set;

import org.w3c.dom.Document;

import de.ingrid.iplug.csw.dsc.cswclient.CSWRecord;
import de.ingrid.iplug.csw.dsc.cswclient.constants.ElementSetName;
import de.ingrid.iplug.csw.dsc.tools.StringUtils;

public class TestData {
	
    private final static String dataFolder = "src/test/de/ingrid/iplug/csw/dsc/data";

	public static Set<String> getRecordIds() {
		Set<String> recordIds = new HashSet<String>();
		
		// read all record ids from the storage
		File dataLocation = new File(dataFolder);
		File[] files = dataLocation.listFiles();
		if (files != null) {
			for (int i=0; i<files.length; i++) {
				String filename = files[i].getName();
				recordIds.add(filename.substring(0, filename.lastIndexOf("_")));
			}
		}
		return recordIds;		
	}

	public static CSWRecord getRecord(String id, ElementSetName elementSetName, CSWRecord record) {
		try {
			return getRecordNode(id, elementSetName, record);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static CSWRecord getRecordNode(String id, ElementSetName elementSetName, CSWRecord record) throws Exception {
		
		File file = new File(dataFolder+"/"+id+"_"+elementSetName.toString()+".xml");
		StringBuilder content = new StringBuilder();
		BufferedReader input =  new BufferedReader(new FileReader(file));
		try {
			String line = null;
			while((line = input.readLine()) != null) {
				content.append(line);
				content.append(System.getProperty("line.separator"));
			}
			
			Document node = StringUtils.stringToDocument(content.toString());
			record.initialize(elementSetName, node);
			return record;
		}
		finally {
			input.close();
		}
	}
}
