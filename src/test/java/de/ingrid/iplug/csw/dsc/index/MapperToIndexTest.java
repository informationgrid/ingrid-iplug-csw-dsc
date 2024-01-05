/*
 * **************************************************-
 * ingrid-iplug-csw-dsc:war
 * ==================================================
 * Copyright (C) 2014 - 2024 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.2 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
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

import org.junit.jupiter.api.Test;
import org.springframework.core.io.FileSystemResource;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.ingrid.codelists.CodeListService;
import de.ingrid.iplug.csw.dsc.analyze.CoupledResources;
import de.ingrid.iplug.csw.dsc.analyze.IsoCacheCoupledResourcesAnalyzer;
import de.ingrid.iplug.csw.dsc.cswclient.CSWRecord;
import de.ingrid.iplug.csw.dsc.cswclient.constants.ElementSetName;
import de.ingrid.iplug.csw.dsc.index.mapper.IdfProducerDocumentMapper;
import de.ingrid.iplug.csw.dsc.index.mapper.ScriptedDocumentMapper;
import de.ingrid.iplug.csw.dsc.om.CswCoupledResourcesCacheSourceRecord;
import de.ingrid.iplug.csw.dsc.record.IdfRecordCreator;
import de.ingrid.iplug.csw.dsc.record.mapper.CouplingResourcesMapper;
import de.ingrid.iplug.csw.dsc.record.mapper.CreateIdfMapper;
import de.ingrid.iplug.csw.dsc.record.mapper.CswIdfMapper;
import de.ingrid.iplug.csw.dsc.record.mapper.IIdfMapper;
import de.ingrid.iplug.csw.dsc.record.producer.CswRecordProducer;
import de.ingrid.iplug.csw.dsc.tools.StringUtils;
import de.ingrid.utils.ElasticDocument;
import de.ingrid.utils.statusprovider.StatusProviderService;
import de.ingrid.utils.tool.StringUtil;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MapperToIndexTest extends BaseIndexTestCase {

    StatusProviderService statusProviderService;
    
    public MapperToIndexTest() {
        super();
        statusProviderService = new StatusProviderService();
    }

    /**
     * @throws Exception
     */
    @Test
    public void testMapper() throws Exception {

        prepareCache(null);

        ScriptedDocumentMapper mapper = new ScriptedDocumentMapper();
        mapper.setCompile(false);
        mapper.setMappingScript(new FileSystemResource("src/main/resources/mapping/idf_to_lucene.js"));
        mapper.setCodelistService(new CodeListService());
        
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
        a.setStatusProviderService( statusProviderService );
        CoupledResources cr = a.analyze( cache );
        
        mapper.setIdfRecordCreator( idfRecordCreator );
        
        
        for (String id : cache.getCachedRecordIds()) {
            CSWRecord cswRecord = cache.getRecord(id, ElementSetName.FULL);
            ElasticDocument doc = new ElasticDocument();
            // try {
            mapper.map(new CswCoupledResourcesCacheSourceRecord(cswRecord, cache, cr.getCoupledRecordIds( id )), doc);
            /*
             * } catch (Throwable t) { System.out.println(t); }
             */
            
            Node idfNode = StringUtils.stringToDocument( (String)doc.get( IdfProducerDocumentMapper.DOCUMENT_FIELD_IDF ) );

            assertTrue(doc != null, "Lucene doc found.");
            assertEquals(id, doc.get("t01_object.obj_id"));
            assertTrue(Integer.parseInt((String) doc.get("t01_object.obj_class")) >= 0 && Integer.parseInt((String) doc.get("t01_object.obj_class")) <= 5, "Valid hierarchyLevel set.");
            String mdBrowseGraphic_FileName = xPathUtils.getString(idfNode, "//gmd:identificationInfo//gmd:graphicOverview/gmd:MD_BrowseGraphic/gmd:fileName/gco:CharacterString");
            if (mdBrowseGraphic_FileName != null) {
                assertFalse((doc.getValues("t017_url_ref.url_link").length > 0 && mdBrowseGraphic_FileName.equals(doc.getValues("t017_url_ref.url_link")[0]))
                        || (doc.getValues("t017_url_ref.url_link").length > 1 && mdBrowseGraphic_FileName.equals(doc.getValues("t017_url_ref.url_link")[1])), "MD_BrowseGraphic is mapped as link");
            }
            String fileIdentifier = xPathUtils.getString(idfNode, "//idf:idfMdMetadata/gmd:fileIdentifier/gco:CharacterString").trim();
            assertEquals(fileIdentifier, doc.getValues("t01_object.obj_id")[0], "fileIdentifier is not mapped");

            // check gmd:referenceSystemInfo
            NodeList rsIdentifiers = xPathUtils.getNodeList(idfNode, "//gmd:referenceSystemInfo/gmd:MD_ReferenceSystem/gmd:referenceSystemIdentifier/gmd:RS_Identifier");
            if (rsIdentifiers != null) {
                for (int i = 0; i < rsIdentifiers.getLength(); i++) {
                    String code = xPathUtils.getString(rsIdentifiers.item(i), "gmd:code/gco:CharacterString").trim();
                    String codeSpace = xPathUtils.getString(rsIdentifiers.item(i), "gmd:codeSpace/gco:CharacterString");
                    String val = code;
                    if (codeSpace != null && code != null) {
                        val = codeSpace + ":" + code;
                    }
                    if (val != null) {
                        assertTrue(StringUtil.containsString(doc.getValues("spatial_system.referencesystem_value"), val), "spatial_system.referencesystem_value is not mapped");
                        assertTrue(StringUtil.containsString(doc.getValues("t011_obj_geo.referencesystem_id"), val), "t011_obj_geo.referencesystem_id");
                    }
                }
            }

            // check refering_service_uuid
            Node crossReference = xPathUtils.getNode(idfNode, "//idf:crossReference[./idf:objectType=3]");
            if (crossReference != null) {
                String data = xPathUtils.getString(crossReference, "./@uuid").trim() + "@@" + xPathUtils.getString(crossReference, "idf:objectName").trim() + "@@" + xPathUtils.getString(crossReference, "idf:serviceUrl").trim() + "@@"
                        + xPathUtils.getString(idfNode, "//gmd:identificationInfo/*/gmd:citation/*/gmd:identifier/*/gmd:code/gco:CharacterString").trim();
                assertEquals(data, doc.getValues("refering_service_uuid")[0], "Crossreference from dataset to service exists.");
            }
        }
    }
}
