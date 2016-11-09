/*
 * **************************************************-
 * ingrid-iplug-csw-dsc:war
 * ==================================================
 * Copyright (C) 2014 - 2016 wemove digital solutions GmbH
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

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.io.FileSystemResource;

import de.ingrid.admin.elasticsearch.StatusProvider;
import de.ingrid.iplug.csw.dsc.analyze.CoupledResources;
import de.ingrid.iplug.csw.dsc.analyze.IsoCacheCoupledResourcesAnalyzer;
import de.ingrid.iplug.csw.dsc.cswclient.CSWRecord;
import de.ingrid.iplug.csw.dsc.cswclient.constants.ElementSetName;
import de.ingrid.iplug.csw.dsc.index.mapper.IRecordMapper;
import de.ingrid.iplug.csw.dsc.index.mapper.ScriptedDocumentMapper;
import de.ingrid.iplug.csw.dsc.om.CswCoupledResourcesCacheSourceRecord;
import de.ingrid.iplug.csw.dsc.record.IdfRecordCreator;
import de.ingrid.iplug.csw.dsc.record.mapper.CouplingResourcesMapper;
import de.ingrid.iplug.csw.dsc.record.mapper.CreateIdfMapper;
import de.ingrid.iplug.csw.dsc.record.mapper.CswIdfMapper;
import de.ingrid.iplug.csw.dsc.record.mapper.IIdfMapper;
import de.ingrid.iplug.csw.dsc.record.producer.CswRecordProducer;
import de.ingrid.utils.ElasticDocument;

public class MapperToOGDtoIndexTest extends BaseIndexTestCase {

    @Mock StatusProvider statusProvider;
    
    public MapperToOGDtoIndexTest() {
        super();
        MockitoAnnotations.initMocks( this );
    }
    
    /**
     * @throws Exception
     */
    public void testMapper() throws Exception {

        prepareCacheWithUuids(new String[] {"0f910bbc-e43d-4a56-bc09-49ab736a94d4"});
        
        CswRecordProducer cswRecordProducer = new CswRecordProducer();
        cswRecordProducer.setCache( cache );
        cswRecordProducer.setFactory( factory );

        List<IIdfMapper> record2IdfMapperList = new ArrayList<IIdfMapper>();
        CreateIdfMapper m = new CreateIdfMapper();
        record2IdfMapperList.add( m );
        CswIdfMapper m1 = new CswIdfMapper();
        m1.setStyleSheetResource( new FileSystemResource("src/main/resources/mapping/iso_to_idf.xsl") );
        record2IdfMapperList.add( m1 );
        CouplingResourcesMapper m2 = new CouplingResourcesMapper();
        record2IdfMapperList.add( m2 );
        
        IdfRecordCreator idfRecordCreator = new IdfRecordCreator();
        idfRecordCreator.setRecordProducer( cswRecordProducer );
        idfRecordCreator.setRecord2IdfMapperList( record2IdfMapperList );

        IsoCacheCoupledResourcesAnalyzer a = new IsoCacheCoupledResourcesAnalyzer();
        a.setStatusProvider( statusProvider );
        CoupledResources cr = a.analyze( cache );
        
        List<IRecordMapper> myMappers = new ArrayList<IRecordMapper>();
        ScriptedDocumentMapper mapper = new ScriptedDocumentMapper();
        mapper.setCompile(false);
        mapper.setMappingScript(new FileSystemResource("src/main/resources/mapping/iso_to_ogd.js"));
//        mapper.setCodelistService(new CodeListService());
        mapper.setIdfRecordCreator( idfRecordCreator );
        myMappers.add(mapper);

        for (String id : cache.getCachedRecordIds()) {
            CSWRecord cswRecord = cache.getRecord(id, ElementSetName.FULL);
            ElasticDocument doc = new ElasticDocument();

            for (IRecordMapper myMapper : myMappers) {
                // try {
                myMapper.map(new CswCoupledResourcesCacheSourceRecord(cswRecord, cache, cr.getCoupledRecordIds( id )), doc);
            }

            assertTrue("Lucene doc found.", doc != null);
            assertEquals(id, doc.get("id"));
        }
    }
}