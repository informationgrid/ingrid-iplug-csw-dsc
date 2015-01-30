/*
 * **************************************************-
 * ingrid-iplug-csw-dsc:war
 * ==================================================
 * Copyright (C) 2014 - 2015 wemove digital solutions GmbH
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
package de.ingrid.iplug.csw.dsc.index;

import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.document.Document;
import org.springframework.core.io.FileSystemResource;

import de.ingrid.admin.search.GermanStemmer;
import de.ingrid.codelists.CodeListService;
import de.ingrid.iplug.csw.dsc.cswclient.CSWRecord;
import de.ingrid.iplug.csw.dsc.cswclient.constants.ElementSetName;
import de.ingrid.iplug.csw.dsc.index.mapper.IRecordMapper;
import de.ingrid.iplug.csw.dsc.index.mapper.ScriptedDocumentMapper;
import de.ingrid.iplug.csw.dsc.om.CswCacheSourceRecord;
import de.ingrid.iplug.csw.dsc.tools.LuceneTools;

public class MapperToIndexWsvGeodatenkatalogTest extends BaseIndexTestCase {

    /**
     * @throws Exception
     */
    public void testMapper() throws Exception {

        prepareCache(null, null);

        // is autowired in spring environment !
        LuceneTools tmpLuceneTools = new LuceneTools();
        tmpLuceneTools.setDefaultStemmer(new GermanStemmer());

        // PROCESS MULTIPLE MAPPERS !
        List<IRecordMapper> myMappers = new ArrayList<IRecordMapper>();

        ScriptedDocumentMapper mapper = new ScriptedDocumentMapper();
        mapper.setCompile(false);
        mapper.setMappingScript(new FileSystemResource("src/main/resources/mapping/idf_to_lucene.js"));
        mapper.setCodelistService(new CodeListService());
        myMappers.add(mapper);

        // WSV "Fix"
        // see https://dev2.wemove.com/jira/browse/GEOPORTALWSV-39
        mapper = new ScriptedDocumentMapper();
        mapper.setCompile(false);
        mapper.setMappingScript(new FileSystemResource("src/main/release/presets/wsv/mapping/post_process_lucene_wsv_geodatenkatalog.js"));
        mapper.setCodelistService(new CodeListService());
        myMappers.add(mapper);

        for (String id : cache.getCachedRecordIds()) {
            CSWRecord cswRecord = cache.getRecord(id, ElementSetName.IDF);
            Document doc = new Document();

            for (IRecordMapper myMapper : myMappers) {
                // try {
                myMapper.map(new CswCacheSourceRecord(cswRecord), doc);

                // check valid URLs
                System.out.println("\nAfter Mapping by Mapper '" + myMapper + "': Content of t017_url_ref.url_link");
                for (String url : doc.getValues("t017_url_ref.url_link")) {
                    System.out.println(url);
                }
                System.out.println();
                /*
                 * } catch (Throwable t) { System.out.println(t); }
                 */
            }

            assertTrue("Lucene doc found.", doc != null);
            assertEquals(id, doc.get("t01_object.obj_id"));

            // check valid URLs
            for (String url : doc.getValues("t017_url_ref.url_link")) {
                assertTrue(!url.startsWith("/"));
            }
        }
    }
}
