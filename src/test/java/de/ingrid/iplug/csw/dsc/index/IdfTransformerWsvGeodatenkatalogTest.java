/*
 * **************************************************-
 * ingrid-iplug-csw-dsc:war
 * ==================================================
 * Copyright (C) 2014 - 2020 wemove digital solutions GmbH
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

import org.springframework.core.io.FileSystemResource;
import org.w3c.dom.Node;

import de.ingrid.iplug.csw.dsc.analyze.CoupledResources;
import de.ingrid.iplug.csw.dsc.analyze.IsoCacheCoupledResourcesAnalyzer;
import de.ingrid.iplug.csw.dsc.cswclient.CSWRecord;
import de.ingrid.iplug.csw.dsc.cswclient.constants.ElementSetName;
import de.ingrid.iplug.csw.dsc.om.CswCoupledResourcesCacheSourceRecord;
import de.ingrid.iplug.csw.dsc.record.IdfRecordCreator;
import de.ingrid.iplug.csw.dsc.record.mapper.CouplingResourcesMapper;
import de.ingrid.iplug.csw.dsc.record.mapper.CreateIdfMapper;
import de.ingrid.iplug.csw.dsc.record.mapper.CswIdfMapper;
import de.ingrid.iplug.csw.dsc.record.mapper.IIdfMapper;
import de.ingrid.iplug.csw.dsc.record.mapper.ProcessIdfMapper;
import de.ingrid.iplug.csw.dsc.record.producer.CswRecordProducer;
import de.ingrid.iplug.csw.dsc.tools.StringUtils;
import de.ingrid.utils.ElasticDocument;
import de.ingrid.utils.idf.IdfTool;
import de.ingrid.utils.statusprovider.StatusProviderService;

public class IdfTransformerWsvGeodatenkatalogTest extends BaseIndexTestCase {

    StatusProviderService statusProviderService;
    
    public IdfTransformerWsvGeodatenkatalogTest() {
        super();
        statusProviderService = new StatusProviderService();
    }
    
    /**
     * @throws Exception
     */
    public void testTransform() throws Exception {

        ProcessIdfMapper processIdfMapperWSV = new ProcessIdfMapper();
        processIdfMapperWSV.setStyleSheetResource( new FileSystemResource( "src/main/release/presets/wsv/mapping/post_process_idf_wsv_geodatenkatalog.xsl" ) );

        setupCache( new String[] { "24265_wsv" } );

        CswRecordProducer cswRecordProducer = new CswRecordProducer();

        cswRecordProducer.setCache( cache );
        cswRecordProducer.setFactory( factory );

        List<IIdfMapper> record2IdfMapperList = new ArrayList<IIdfMapper>();

        CreateIdfMapper m = new CreateIdfMapper();
        record2IdfMapperList.add( m );
        CswIdfMapper m1 = new CswIdfMapper();
        m1.setStyleSheetResource( new FileSystemResource( "src/main/resources/mapping/iso_to_idf.xsl" ) );
        record2IdfMapperList.add( m1 );
        CouplingResourcesMapper m2 = new CouplingResourcesMapper();
        record2IdfMapperList.add( m2 );
        record2IdfMapperList.add( processIdfMapperWSV );

        IdfRecordCreator idfRecordCreator = new IdfRecordCreator();
        idfRecordCreator.setRecordProducer( cswRecordProducer );
        idfRecordCreator.setRecord2IdfMapperList( record2IdfMapperList );

        IsoCacheCoupledResourcesAnalyzer a = new IsoCacheCoupledResourcesAnalyzer();
        a.setStatusProviderService( statusProviderService );
        CoupledResources cr = a.analyze( cache );

        for (String id : cache.getCachedRecordIds()) {
            CSWRecord cswRecord = cache.getRecord( id, ElementSetName.FULL );
            ElasticDocument doc = new ElasticDocument();

            Node idfNode = StringUtils.stringToDocument(
                    IdfTool.getIdfDataFromRecord( idfRecordCreator.getRecord( doc, new CswCoupledResourcesCacheSourceRecord( cswRecord, cache, cr.getCoupledRecordIds( id ) ) ) ) )
                    .getDocumentElement();

            String[] urls = xPathUtils.getStringArray( idfNode, "//gmd:URL" );
            assertTrue( "Idf found.", idfNode.hasChildNodes() );
            assertTrue( "Metadata found.", xPathUtils.nodeExists( idfNode, "//idf:idfMdMetadata" ) );
            for (String url : urls) {
                assertTrue( "All urls are absolute and start with 'http'.", url.startsWith( "http" ) );
            }
        }

    }

}
