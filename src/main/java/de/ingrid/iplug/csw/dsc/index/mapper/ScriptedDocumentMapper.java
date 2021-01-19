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
/**
 * 
 */
package de.ingrid.iplug.csw.dsc.index.mapper;

import java.io.InputStreamReader;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.apache.log4j.Logger;
import org.springframework.core.io.Resource;

import de.ingrid.codelists.CodeListService;
import de.ingrid.iplug.csw.dsc.cswclient.CSWRecord;
import de.ingrid.iplug.csw.dsc.cswclient.constants.ElementSetName;
import de.ingrid.iplug.csw.dsc.cswclient.impl.GenericRecord;
import de.ingrid.iplug.csw.dsc.om.CswCoupledResourcesCacheSourceRecord;
import de.ingrid.iplug.csw.dsc.om.SourceRecord;
import de.ingrid.iplug.csw.dsc.record.IdfRecordCreator;
import de.ingrid.iplug.csw.dsc.tools.StringUtils;
import de.ingrid.utils.ElasticDocument;
import de.ingrid.utils.dsc.Record;
import de.ingrid.utils.idf.IdfTool;
import de.ingrid.utils.xml.IDFNamespaceContext;
import de.ingrid.utils.xpath.XPathUtils;

/**
 * Script based source record to lucene document mapping. This class takes a
 * {@link Resource} as parameter to specify the mapping script. The scripting
 * engine will be automatically determined from the extension of the mapping
 * script.
 * <p />
 * If the {@link compile} parameter is set to true, the script is compiled, if
 * the ScriptEngine supports compilation.
 * 
 * @author joachim@wemove.com
 * 
 */
public class ScriptedDocumentMapper implements IRecordMapper {

    private Resource mappingScript;

    private boolean compile = false;

    private ScriptEngine engine;
    private CompiledScript compiledScript;

    private CodeListService codelistService;

    private IdfRecordCreator idfRecordCreator;

    private static final Logger log = Logger.getLogger( ScriptedDocumentMapper.class );

    @Override
    public void map(SourceRecord record, ElasticDocument doc) throws Exception {
        if (mappingScript == null) {
            log.error( "Mapping script is not set!" );
            throw new IllegalArgumentException( "Mapping script is not set!" );
        }

        if (!(record instanceof CswCoupledResourcesCacheSourceRecord)) {
            log.error( "Source Record is not a CswCoupledResourcesCacheSourceRecord!" );
            throw new IllegalArgumentException( "Source Record is not a CswCoupledResourcesCacheSourceRecord!" );
        }

        String idfString;
        if (doc.containsKey( IdfProducerDocumentMapper.DOCUMENT_FIELD_IDF )) {
            idfString = (String) doc.get( IdfProducerDocumentMapper.DOCUMENT_FIELD_IDF );
        } else {
            Record rec = idfRecordCreator.getRecord( doc, record );
            idfString = IdfTool.getIdfDataFromRecord( rec );
            doc.put( IdfProducerDocumentMapper.DOCUMENT_FIELD_IDF, idfString );
        }
        CSWRecord idfRecord = new GenericRecord();
        idfRecord.initialize( ElementSetName.IDF, StringUtils.stringToDocument( idfString ).getDocumentElement() );

        try {
            if (engine == null) {
                String scriptName = mappingScript.getFilename();
                String extension = scriptName.substring( scriptName.lastIndexOf( '.' ) + 1, scriptName.length() );
                ScriptEngineManager mgr = new ScriptEngineManager();
                engine = mgr.getEngineByExtension( extension );
                if (compile) {
                    if (engine instanceof Compilable) {
                        Compilable compilable = (Compilable) engine;
                        compiledScript = compilable.compile( new InputStreamReader( mappingScript.getInputStream() ) );
                    }
                }
            }

            XPathUtils xpathUtils = new XPathUtils( new IDFNamespaceContext() );

            Bindings bindings = engine.createBindings();
            bindings.put( "cswRecord", idfRecord );
            bindings.put( "document", doc );
            bindings.put( "log", log );
            bindings.put( "codelistService", codelistService );
            bindings.put( "XPathUtils", xpathUtils );
            bindings.put( "javaVersion", System.getProperty( "java.version" ) );

            if (compiledScript != null) {
                compiledScript.eval( bindings );
            } else {
                engine.eval( new InputStreamReader( mappingScript.getInputStream() ), bindings );
            }
        } catch (Exception e) {
            String cswRecordId = null;
            if (idfRecord != null) {
                cswRecordId = idfRecord.getId();
            }
            log.error( "Error mapping source record to lucene document: cswRecordId=" + cswRecordId, e );
            throw e;
        }
    }

    public Resource getMappingScript() {
        return mappingScript;
    }

    public void setMappingScript(Resource mappingScript) {
        this.mappingScript = mappingScript;
    }

    public boolean isCompile() {
        return compile;
    }

    public void setCompile(boolean compile) {
        this.compile = compile;
    }

    public void setCodelistService(CodeListService codelistService) {
        this.codelistService = codelistService;
    }
    
    public IdfRecordCreator getIdfRecordCreator() {
        return idfRecordCreator;
    }

    public void setIdfRecordCreator(IdfRecordCreator idfRecordCreator) {
        this.idfRecordCreator = idfRecordCreator;
    }

    @Override
    public void cleanup() {
        // use a new script engine, try to avoid funny long term errors like
        // INGRID-2350
        compiledScript = null;
        engine = null;
    }
}
