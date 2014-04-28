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
import org.apache.lucene.document.Document;
import org.springframework.core.io.Resource;

import de.ingrid.codelists.CodeListService;
import de.ingrid.iplug.csw.dsc.cswclient.CSWRecord;
import de.ingrid.iplug.csw.dsc.om.CswCacheSourceRecord;
import de.ingrid.iplug.csw.dsc.om.SourceRecord;
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

    private static final Logger log = Logger
            .getLogger(ScriptedDocumentMapper.class);

    @Override
    public void map(SourceRecord record, Document doc) throws Exception {
        if (mappingScript == null) {
            log.error("Mapping script is not set!");
            throw new IllegalArgumentException("Mapping script is not set!");
        }

        if (!(record instanceof CswCacheSourceRecord)) {
            log.error("Source Record is not a CswCacheSourceRecord!");
            throw new IllegalArgumentException(
                    "Source Record is not a CswCacheSourceRecord!");
        }

        CSWRecord cswRecord = (CSWRecord) (((CswCacheSourceRecord) record).get(CswCacheSourceRecord.CSW_RECORD));

        try {
            if (engine == null) {
                String scriptName = mappingScript.getFilename();
                String extension = scriptName.substring(scriptName
                        .lastIndexOf('.') + 1, scriptName.length());
                ScriptEngineManager mgr = new ScriptEngineManager();
                engine = mgr.getEngineByExtension(extension);
                if (compile) {
                    if (engine instanceof Compilable) {
                        Compilable compilable = (Compilable) engine;
                        compiledScript = compilable
                                .compile(new InputStreamReader(mappingScript
                                        .getInputStream()));
                    }
                }
            }
            
            XPathUtils xpathUtils = new XPathUtils(new IDFNamespaceContext());
            
            Bindings bindings = engine.createBindings();
            bindings.put("cswRecord", cswRecord);
            bindings.put("document", doc);
            bindings.put("log", log);
            bindings.put("codelistService", codelistService);
            bindings.put("XPathUtils", xpathUtils);
            
            if (compiledScript != null) {
                compiledScript.eval(bindings);
            } else {
                engine.eval(new InputStreamReader(mappingScript
                        .getInputStream()), bindings);
            }
        } catch (Exception e) {
            log.error("Error mapping source record to lucene document.", e);
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

    @Override
    public void cleanup() {
        // use a new script engine, try to avoid funny long term errors like INGRID-2350
        compiledScript = null;
        engine = null;
    }
}
