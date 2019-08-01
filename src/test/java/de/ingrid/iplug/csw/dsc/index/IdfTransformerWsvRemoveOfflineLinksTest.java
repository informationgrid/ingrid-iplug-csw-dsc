/*
 * **************************************************-
 * ingrid-iplug-csw-dsc:war
 * ==================================================
 * Copyright (C) 2014 - 2019 wemove digital solutions GmbH
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
import de.ingrid.iplug.csw.dsc.record.producer.CswRecordProducer;
import de.ingrid.iplug.csw.dsc.tools.StringUtils;
import de.ingrid.utils.ElasticDocument;
import de.ingrid.utils.idf.IdfTool;
import de.ingrid.utils.statusprovider.StatusProviderService;

public class IdfTransformerWsvRemoveOfflineLinksTest extends BaseIndexTestCase {

    StatusProviderService statusProviderService;
    
    public IdfTransformerWsvRemoveOfflineLinksTest() {
        super();
        statusProviderService = new StatusProviderService();
    }
    
    /**
     * @throws Exception
     */
    public void testTransform() throws Exception {

        setupCache( new String[] {
                "geokatalogWSV_870043be-85e0-4f7d-9cdc-43fe293b0c90" ,
                "geokatalogWSV_191fc9e5-eaa9-4dda-b45d-5c9534246012" ,
                "geokatalogWSV_8e822fdd-f508-4d7a-a596-e60684dd0c97" } );

        CswRecordProducer cswRecordProducer = new CswRecordProducer();

        cswRecordProducer.setCache( cache );
        cswRecordProducer.setFactory( factory );

        List<IIdfMapper> record2IdfMapperList = new ArrayList<IIdfMapper>();

        CreateIdfMapper m = new CreateIdfMapper();
        record2IdfMapperList.add( m );
        CswIdfMapper m1 = new CswIdfMapper();
        m1.setStyleSheetResource( new FileSystemResource( "src/main/resources/mapping/iso_to_idf_geokatalogWSV.xsl" ) );
        record2IdfMapperList.add( m1 );
        CouplingResourcesMapper m2 = new CouplingResourcesMapper();
        record2IdfMapperList.add( m2 );

        IdfRecordCreator idfRecordCreator = new IdfRecordCreator();
        idfRecordCreator.setRecordProducer( cswRecordProducer );
        idfRecordCreator.setRecord2IdfMapperList( record2IdfMapperList );

        IsoCacheCoupledResourcesAnalyzer a = new IsoCacheCoupledResourcesAnalyzer();
        a.setStatusProviderService( statusProviderService );
        CoupledResources cr = a.analyze( cache );

        // GeoKatalog.WSV Tests

        // remove offline URLs
        // see https://redmine.wemove.com/issues/1745 / "AF-00448 GP4: GeoKatalog - iPlug - Download Link anzeigen"
        
        CSWRecord record = cache.getRecord( "870043be-85e0-4f7d-9cdc-43fe293b0c90", ElementSetName.FULL );
        ElasticDocument doc = new ElasticDocument();
        Node n = StringUtils
                .stringToDocument(
                        IdfTool.getIdfDataFromRecord( idfRecordCreator.getRecord( doc,
                                new CswCoupledResourcesCacheSourceRecord( record, cache, cr.getCoupledRecordIds( record.getId() ) ) ) ) ).getDocumentElement();

        assertNotNull( "GeoKatalog.WSV: IDF record 870043be-85e0-4f7d-9cdc-43fe293b0c90 exists in cache.", n );
        assertEquals( "GeoKatalog.WSV: URLs of type 'localZipDownload' REMOVED",
                0, xPathUtils.getNodeList( n, "//gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource/gmd:function/gmd:CI_OnLineFunctionCode[@codeListValue='localZipDownload']" ).getLength() );
        assertEquals( "GeoKatalog.WSV: URLs of type 'download' exist",
                1, xPathUtils.getNodeList( n, "//gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource/gmd:function/gmd:CI_OnLineFunctionCode[@codeListValue='download']" ).getLength() );

    }

}
