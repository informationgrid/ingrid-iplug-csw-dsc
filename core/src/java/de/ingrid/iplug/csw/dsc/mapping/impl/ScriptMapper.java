/*
 * Copyright (c) 2009 wemove digital solutions. All rights reserved.
 */

package de.ingrid.iplug.csw.dsc.mapping.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.Hashtable;
import java.util.Map;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.Document;

import de.ingrid.iplug.csw.dsc.cswclient.CSWRecord;
import de.ingrid.iplug.csw.dsc.mapping.DocumentMapper;
import de.ingrid.utils.dsc.Record;

/**
 * This mapper uses javascript to map between document types.
 * @author ingo herwig <ingo@wemove.com>
 */
public class ScriptMapper implements DocumentMapper, Serializable {
	
	private static final long serialVersionUID = ScriptMapper.class.getName().hashCode();

	final protected static Log log = LogFactory.getLog(ScriptMapper.class);
	
	/**
	 * Script files used for mapping
	 */
	protected String cswToLuceneMapping = null;
	protected String cswToIngridMapping = null;
	
	/**
	 * Set the mapping script file for the csw to lucene mapping
	 * @param mappingFile
	 */
	public void setCswToLuceneMapping(String mappingFile) {
		this.cswToLuceneMapping = mappingFile;
	}

	/**
	 * Set the mapping script file for the csw to ingrid mapping
	 * @param mappingFile
	 */
	public void setCswToIngridMapping(String mappingFile) {
		this.cswToIngridMapping = mappingFile;
	}

	/**
	 * Do the mapping
	 * @param script
	 * @param parameters
	 */
	protected void doMap(String script, Map<String, Object> parameters) throws Exception {
        // check if the script exists
        File scriptFile = new File(this.cswToLuceneMapping);
        if (!scriptFile.exists())
        	throw new IOException("The mapping script "+scriptFile.getAbsolutePath()+" does not exist.");

		// instantiate the engine
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("JavaScript");

		// pass all parameters
        for(String param : parameters.keySet())
        	engine.put(param, parameters.get(param));
        engine.put("log", log);

		// execute the mapping
        System.out.println(scriptFile.getAbsolutePath());
        InputStream st = new FileInputStream(scriptFile);
    	engine.eval(new InputStreamReader(st));
	}
	
	/**
	 * DocumentMapper interface implementation
	 */
	
	@Override
	public Document mapCswToLucene(CSWRecord record) throws Exception {
        Document document = new Document();
        
        Map<String, Object> parameters = new Hashtable<String, Object>();
        parameters.put("cswRecord", record);
        parameters.put("document", document);
        
        this.doMap(this.cswToLuceneMapping, parameters);
		return document;
	}

	@Override
	public Record mapCswToIngrid(CSWRecord record) throws Exception {
        Record document = new Record();
        
        Map<String, Object> parameters = new Hashtable<String, Object>();
        parameters.put("cswRecord", record);
        parameters.put("document", document);
        
        this.doMap(this.cswToIngridMapping, parameters);
		return document;
	}
}
