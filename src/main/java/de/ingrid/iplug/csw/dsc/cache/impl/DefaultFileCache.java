/*
 * **************************************************-
 * ingrid-iplug-csw-dsc:war
 * ==================================================
 * Copyright (C) 2014 - 2021 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.1 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl5
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
/*
 * Copyright (c) 2009 wemove digital solutions. All rights reserved.
 */

package de.ingrid.iplug.csw.dsc.cache.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;

import de.ingrid.iplug.csw.dsc.cache.Cache;
import de.ingrid.iplug.csw.dsc.cswclient.CSWFactory;
import de.ingrid.iplug.csw.dsc.cswclient.CSWRecord;
import de.ingrid.iplug.csw.dsc.cswclient.constants.ElementSetName;
import de.ingrid.iplug.csw.dsc.tools.FileUtils;
import de.ingrid.iplug.csw.dsc.tools.StringUtils;

public class DefaultFileCache implements Cache, Serializable {

	private static final long serialVersionUID = DefaultFileCache.class.getName().hashCode();
	
	final protected static Log log = LogFactory.getLog(DefaultFileCache.class);

	protected CSWFactory factory = null;

	protected boolean isInitialized = false;
	protected boolean inTransaction = false;
	
	/**
	 * The original path of the cache. If the cache is not in transaction mode,
	 * all content is served from and stored in there. Commited content will be copied to there.
	 */
	protected String cachePath = null;
	
	/**
	 * The temporary path of the cache, that is used in transaction mode.
	 */
	protected String tmpPath = null;

	/**
	 * The initial cache from which a transaction was started.
	 */
	protected Cache initialCache = null;
	
	/**
	 * File name filter for recognizing cached files
	 */
	protected class CacheFileFilter implements FileFilter {
	    public boolean accept(File file) {
	        return !file.isDirectory() && file.getName().endsWith(".xml");
	    }
	};

	/**
	 * Constructor
	 */
	public DefaultFileCache() {
		this.initialCache = this;
	}

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
		if (!this.isInitialized) {
			// check configuration
			if (this.factory == null)
				throw new RuntimeException("Cache is not configured properly. The 'factory' property is not set.");
			
			// check for original path
			String originalPath = this.getCachePath();
			if (originalPath == null)
				throw new RuntimeException("DefaultFileCache is not configured properly: cachePath not set.");
	
			// check if the original path exists and create it if not
			File cacheLocation = new File(originalPath);
			if (!cacheLocation.exists())
				cacheLocation.mkdirs();
			
			this.isInitialized = true;
		}
	}

	/**
	 * Get the work path of the cache. 
	 * If the cache is in transaction mode, the path will differ from cachePath.
	 * @return String
	 */
	protected String getWorkPath() {
		if (this.isInTransaction()) {
			return this.getTempPath();
		}
		else {
			return this.getCachePath();
		}
	}

	/**
	 * Encode an id to be used in a filename. 
	 * @return String
	 */
	protected String encodeId(String id) {
		return FileUtils.encodeFileName(id);
	}

	/**
	 * Decode an id that was used in a filename. 
	 * @return String
	 */
	protected String decodeId(String id) {
		return FileUtils.decodeFileName(id);
	}

	/**
	 * Get the record id from a cache filename
	 * @param filename The filename without the path
	 * @return String
	 */
	protected String getIdFromFilename(String filename) {
		File file = new File(filename);
		String basename = file.getName();
		String id = this.decodeId(basename.substring(0, basename.lastIndexOf("_")));
		return id;
	}

	/**
	 * Get the filename for a record
	 * @param id
	 * @param elementSetName
	 * @return String
	 */
	protected String getFilename(String id, ElementSetName elementSetName) {
		return this.encodeId(id)+"_"+elementSetName.toString()+".xml";
	}
	
	/**
	 * Get the relative path to a record starting from the cache root
	 * @param id
	 * @param elementSetName
	 * @return String
	 */
	protected String getRelativePath(String id, ElementSetName elementSetName) {
	    return this.encodeId(id).substring(0, 1);
	}
	
	/**
	 * Get the relative path to a record starting from the cache root
	 * @param id
	 * @param elementSetName
	 * @return String
	 */
	protected String getAbsolutePath(String id, ElementSetName elementSetName) {
		StringBuffer buf = new StringBuffer();
		buf.append(this.getWorkPath()).append(File.separatorChar).
			append(this.getRelativePath(id, elementSetName));
		return new File(buf.toString()).getAbsolutePath();
	}
	
	/**
	 * Get record ids from a directory and all sub directories
	 * @param directory The start directory
	 * @return Set
	 */
	protected Set<String> getRecordIds(File directory) {
		Set<String> recordIds = new HashSet<String>();
		FileFilter cacheFileFilter = new CacheFileFilter();
		File[] files = directory.listFiles();
		if (files != null) {
			for (int i=0; i<files.length; i++) {
				if (cacheFileFilter.accept(files[i]))
					recordIds.add(this.getIdFromFilename(files[i].getName()));
				if (files[i].isDirectory())
					recordIds.addAll(getRecordIds(files[i]));
			}
		}
		return recordIds;
	}

	/**
	 * Set the original cache path (that is used if not in transaction mode)
	 * @param cachePath
	 */
	public void setCachePath(String cachePath) {
		this.cachePath = cachePath;
	}
	
	/**
	 * Get the root path of the cache if it is not in transaction mode. 
	 * @return String
	 */
	public String getCachePath() {
		return this.cachePath;
	}

	/**
	 * Get the root path of the cache if it is in transaction mode. 
	 * @return String
	 */
	public String getTempPath() {
		if (this.tmpPath == null) {
			File originalPath = new File(this.getCachePath());
			File newPath = new File(originalPath.getParent()+File.separatorChar+originalPath.getName()+"_"+
					StringUtils.generateUuid());

			// check if the cache path exists and create it if not
			if (!newPath.exists())
				newPath.mkdirs();
			this.tmpPath = newPath.getAbsolutePath();
		}
		return this.tmpPath;
	}

	/**
	 * Get the absolute filename of a record.
	 * @param id
	 * @param elementSetName
	 * @param cacheOperation
	 * @return String
	 */
	public String getAbsoluteFilename(String id, ElementSetName elementSetName) {
		StringBuffer buf = new StringBuffer();
		buf.append(this.getAbsolutePath(id, elementSetName)).append(File.separatorChar).
			append(this.getFilename(id, elementSetName));
		return new File(buf.toString()).getAbsolutePath();
	}
	
	/**
	 * Cache interface implementation
	 */

	@Override
	public void configure(CSWFactory factory) {
		this.factory = factory;
	}
	
	@Override
	public Set<String> getCachedRecordIds() {
		if (!this.isInitialized())
			initialize();
		
		return getRecordIds(new File(this.getWorkPath()));
	}

	@Override
	public boolean isCached(String id, ElementSetName elementSetName) throws IOException {
		if (!this.isInitialized())
			initialize();
		
		try {
    		String filePath = this.getAbsoluteFilename(id, elementSetName);
    		File file = new File(filePath);
    		return file.exists();
		} catch (Exception e) {
		    if (log.isDebugEnabled()) {
		        log.debug("Could not find {" +id+ "," + elementSetName.name() +"} in cache.");
		    }
		    return false;
		}
	}

	@Override
	public CSWRecord getRecord(String id, ElementSetName elementSetName) throws IOException {
		if (!this.isInitialized())
			initialize();
		
		String filePath = this.getAbsoluteFilename(id, elementSetName);
		File file = new File(filePath);
		if (file.exists()) {
		
			StringBuilder content = new StringBuilder();
			BufferedReader input = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));

			try {
				String line = null;
				while((line = input.readLine()) != null) {
					content.append(line);
					content.append(System.getProperty("line.separator"));
				}
				input.close();
				input = null;
				
				Document document = StringUtils.stringToDocument(content.toString());
				CSWRecord record = this.factory.createRecord();
				record.initialize(elementSetName, document.getFirstChild());
				return record;
			}
			catch (Exception e) {
				throw new IOException(e);
			}
			finally {
				if (input != null)
					input.close();
			}
		}
		else
			throw new IOException("No cache entry with id "+id+" and elementset "+elementSetName+" found.");
	}

	@Override
	public void putRecord(CSWRecord record) throws IOException {
		if (!this.isInitialized())
			initialize();
		
		// ensure that the directory exists
		String path = this.getAbsolutePath(record.getId(), record.getElementSetName());
		new File(path).mkdirs();

		String filePath = this.getAbsoluteFilename(record.getId(), record.getElementSetName());
		BufferedWriter output = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath),"UTF8"));
		try {
			output.write(StringUtils.nodeToString(record.getOriginalResponse()));
			output.close();
			output = null;
		}
		finally {
			if (output != null)
				output.close();
		}
	}

	@Override
	public void removeAllRecords() {
		if (!this.isInitialized())
			initialize();

		File workPath = new File(this.getWorkPath());
		FileUtils.deleteRecursive(workPath);
		workPath.mkdirs();
	}

	@Override
	public void removeRecord(String id) {
		if (!this.isInitialized())
			initialize();

		// remove all cached files for this id
		ElementSetName[] names = ElementSetName.values();
		for (int i=0; i<names.length; i++) {
			String filePath = this.getAbsoluteFilename(id, names[i]);
			File file = new File(filePath);
			if (file.exists())
				file.delete();
		}
	}

	@Override
	public boolean isInTransaction() {
		return this.inTransaction;
	}	
	
	@Override
	public Cache startTransaction() throws IOException {
		if (!this.isInitialized())
			initialize();

		DefaultFileCache cache = new DefaultFileCache();
		cache.configure(this.factory);
		
		// the original content of the new cache instance
		// is the content of this cache
		cache.cachePath = this.getWorkPath();
		cache.initialCache = this;
		cache.inTransaction = true;
		
		// copy content of this instance to the new cache
		FileUtils.copyRecursive(new File(this.getWorkPath()), 
				new File(cache.getWorkPath()));
		
		return cache;
	}	

	@Override
	public void commitTransaction() throws IOException {
		if (!this.isInitialized())
			initialize();

		if (this.isInTransaction()) {
			// move content of this instance to the original cache
			File originalDir = new File(this.getCachePath());
			File tmpDir = new File(this.getWorkPath());
			if (log.isInfoEnabled()) {
			    log.info( "Remove cache: " + originalDir.getAbsolutePath() );
			}
			
			FileUtils.deleteRecursive(originalDir);

            if (log.isInfoEnabled()) {
                log.info( "Rename temp cache: " + tmpDir.getAbsolutePath() + " to " + originalDir.getAbsolutePath());
            }
			if (!tmpDir.renameTo(originalDir)) {
			    log.error( "Failed  to rename " + tmpDir.getAbsolutePath() + " to " + originalDir.getAbsolutePath());
			}
			
			this.inTransaction = false;
		}
		else
			throw new RuntimeException("The cache is not in transaction mode.");
	}

	@Override
	public void rollbackTransaction() {
		if (!this.isInitialized())
			initialize();

		if (this.isInTransaction()) {
			// remove content of this instance
			File tmpDir = new File(this.getWorkPath());
			FileUtils.deleteRecursive(tmpDir);

			this.inTransaction = false;
		}
		else
			throw new RuntimeException("The cache is not in transaction mode.");
	}

	@Override
	public Cache getInitialCache() {
		return this.initialCache;
	}

	@Override
	public Date getLastCommitDate() {
		// return the last modified date of the cache directory
		File cacheDir = new File(this.getCachePath());
		return new Date(cacheDir.lastModified());
	}
	
    @Override
	public String toString() {
        return this.getWorkPath() + ", " + super.toString();
	    
	}
}
