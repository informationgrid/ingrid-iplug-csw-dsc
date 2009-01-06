/*
 * Copyright (c) 2009 wemove digital solutions. All rights reserved.
 */

package de.ingrid.iplug.csw.dsc.cache.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

import org.w3c.dom.Document;

import de.ingrid.iplug.csw.dsc.cache.Cache;
import de.ingrid.iplug.csw.dsc.cswclient.CSWRecord;
import de.ingrid.iplug.csw.dsc.cswclient.constants.ElementSetName;
import de.ingrid.iplug.csw.dsc.tools.StringUtils;

public class DefaultFileCache implements Cache, Serializable {

	private static final long serialVersionUID = DefaultFileCache.class.getName().hashCode();
	
	protected boolean isInitialized = false;
	protected String cachePath = null;

	/**
	 * File name filter for recognizing cached files
	 */
	private class CacheFileFilter implements FileFilter {
	    public boolean accept(File file) {
	        return !file.isDirectory() && file.getName().endsWith(".xml");
	    }
	};

	/**
	 * Check if the cache is already initialized
	 * @return boolean
	 */
	protected boolean isInitialized() {
		return this.isInitialized;
	}

	/**
	 * Initialize the cache
	 */
	protected void initialize() {
		if (this.cachePath == null)
			throw new RuntimeException("DefaultFileCache is not configured properly: cachePath not set.");

		// check if the cache path exists and create it if not
		File cacheLocation = new File(this.cachePath);
		if (!cacheLocation.exists())
			cacheLocation.mkdir();
		
		this.isInitialized = true;
	}
	
	/**
	 * Get the record id from a cache filename
	 * @param filename
	 * @return String
	 */
	protected String getIdFromFilename(String filename) {
		return filename.substring(0, filename.lastIndexOf("_"));
	}

	/**
	 * Get the filename for a record
	 * @param record
	 * @return String
	 */
	protected String getFilename(CSWRecord record) {
		return this.getFilename(record.getId(), record.getElementSetName());
	}

	/**
	 * Get the filename for a record
	 * @param id
	 * @param elementSetName
	 * @return String
	 */
	protected String getFilename(String id, ElementSetName elementSetName) {
		return this.cachePath+"/"+id+"_"+elementSetName.toString()+".xml";
	}
	
	/**
	 * Set the cache path
	 * @param cachePath
	 */
	public void setCachePath(String cachePath) {
		this.cachePath = cachePath;
	}

	/**
	 * Cache interface implementation
	 */
	
	@Override
	public Set<String> getCachedRecordIds() {
		if (!isInitialized())
			initialize();
		
		Set<String> recordIds = new HashSet<String>();
			
		// read all cached record ids from the storage
		File cacheLocation = new File(this.cachePath);
		File[] files = cacheLocation.listFiles(new CacheFileFilter());
		if (files != null) {
			for (int i=0; i<files.length; i++) {
				recordIds.add(this.getIdFromFilename(files[i].getName()));
			}
		}
		return recordIds;
	}

	@Override
	public boolean isCached(String id, ElementSetName elementSetName) throws IOException {
		if (!isInitialized())
			initialize();
		
		File file = new File(this.getFilename(id, elementSetName));
		return file.exists();
	}

	@Override
	public CSWRecord getRecord(String id, ElementSetName elementSetName, CSWRecord record) throws IOException {
		if (!isInitialized())
			initialize();
		
		File file = new File(this.getFilename(id, elementSetName));
		if (file.exists()) {
		
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
			catch (Exception e) {
				throw new IOException(e);
			}
			finally {
				input.close();
			}
		}
		else
			throw new IOException("No cache entry with id "+id+" and elementset "+elementSetName+" found.");
	}

	@Override
	public void putRecord(CSWRecord record) throws IOException {
		if (!isInitialized())
			initialize();
		
		File file = new File(this.getFilename(record));
		Writer output = new BufferedWriter(new FileWriter(file));
		try {
			output.write(StringUtils.nodeToString(record.getOriginalResponse()));
		}
		finally {
			output.close();
		}
	}

	@Override
	public void removeAllRecords() {
		if (!isInitialized())
			initialize();

		File cacheLocation = new File(this.cachePath);
		if (cacheLocation.exists()) {
			File[] files = cacheLocation.listFiles(new CacheFileFilter());
			if (files != null) {
				for (int i=0; i<files.length; i++) {
					files[i].delete();
				}
			}
		}
	}

	@Override
	public void removeRecord(String id) {
		if (!isInitialized())
			initialize();

		// remove all cached files for this id
		ElementSetName[] names = ElementSetName.values();
		for (int i=0; i<names.length; i++) {
			File file = new File(this.getFilename(id, names[i]));
			if (file.exists())
				file.delete();
		}
	}	
}
