package de.ingrid.iplug.csw.dsc.index;

import org.apache.lucene.document.Document;
import org.springframework.core.io.FileSystemResource;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.ingrid.admin.search.GermanStemmer;
import de.ingrid.codelists.CodeListService;
import de.ingrid.iplug.csw.dsc.cswclient.CSWRecord;
import de.ingrid.iplug.csw.dsc.cswclient.constants.ElementSetName;
import de.ingrid.iplug.csw.dsc.index.mapper.ScriptedDocumentMapper;
import de.ingrid.iplug.csw.dsc.om.CswCacheSourceRecord;
import de.ingrid.iplug.csw.dsc.tools.LuceneTools;
import de.ingrid.utils.tool.StringUtil;

public class MapperToIndexTest extends BaseIndexTestCase {

    /**
     * @throws Exception
     */
    public void testMapper() throws Exception {

        prepareCache(null, null);

        // is autowired in spring environment !
        LuceneTools tmpLuceneTools = new LuceneTools();
        tmpLuceneTools.setDefaultStemmer(new GermanStemmer());

        ScriptedDocumentMapper mapper = new ScriptedDocumentMapper();
        mapper.setCompile(false);
        mapper.setMappingScript(new FileSystemResource("src/main/resources/mapping/idf_to_lucene.js"));
        mapper.setCodelistService(new CodeListService());

        for (String id : cache.getCachedRecordIds()) {
            CSWRecord idfRecord = cache.getRecord(id, ElementSetName.IDF);
            Document doc = new Document();
            // try {
            mapper.map(new CswCacheSourceRecord(idfRecord), doc);
            /*
             * } catch (Throwable t) { System.out.println(t); }
             */

            assertTrue("Lucene doc found.", doc != null);
            assertEquals(id, doc.get("t01_object.obj_id"));
            assertTrue("Valid hierarchyLevel set.", Integer.parseInt(doc.get("t01_object.obj_class")) >= 0 && Integer.parseInt(doc.get("t01_object.obj_class")) <= 5);
            String mdBrowseGraphic_FileName = xPathUtils.getString(idfRecord.getOriginalResponse(), "//gmd:identificationInfo//gmd:graphicOverview/gmd:MD_BrowseGraphic/gmd:fileName/gco:CharacterString");
            if (mdBrowseGraphic_FileName != null) {
                assertFalse("MD_BrowseGraphic is mapped as link", (doc.getValues("t017_url_ref.url_link").length > 0 && mdBrowseGraphic_FileName.equals(doc.getValues("t017_url_ref.url_link")[0]))
                        || (doc.getValues("t017_url_ref.url_link").length > 1 && mdBrowseGraphic_FileName.equals(doc.getValues("t017_url_ref.url_link")[1])));
            }
            String fileIdentifier = xPathUtils.getString(idfRecord.getOriginalResponse(), "//idf:idfMdMetadata/gmd:fileIdentifier/gco:CharacterString").trim();
            assertTrue("fileIdentifier is not mapped", fileIdentifier.equals(doc.getValues("t01_object.obj_id")[0]));

            // check gmd:referenceSystemInfo
            NodeList rsIdentifiers = xPathUtils.getNodeList(idfRecord.getOriginalResponse(), "//gmd:referenceSystemInfo/gmd:MD_ReferenceSystem/gmd:referenceSystemIdentifier/gmd:RS_Identifier");
            if (rsIdentifiers != null) {
                for (int i = 0; i < rsIdentifiers.getLength(); i++) {
                    String code = xPathUtils.getString(rsIdentifiers.item(i), "gmd:code/gco:CharacterString").trim();
                    String codeSpace = xPathUtils.getString(rsIdentifiers.item(i), "gmd:codeSpace/gco:CharacterString");
                    String val = code;
                    if (codeSpace != null && code != null) {
                        val = codeSpace + ":" + code;
                    }
                    if (val != null) {
                        assertTrue("spatial_system.referencesystem_value is not mapped", StringUtil.containsString(doc.getValues("spatial_system.referencesystem_value"), val));
                        assertTrue("t011_obj_geo.referencesystem_id", StringUtil.containsString(doc.getValues("t011_obj_geo.referencesystem_id"), val));
                    }
                }
            }

            // check refering_service_uuid
            Node crossReference = xPathUtils.getNode(idfRecord.getOriginalResponse(), "//idf:crossReference[./idf:objectType=3]");
            if (crossReference != null) {
                String data = xPathUtils.getString(crossReference, "./@uuid").trim() + "@@" + xPathUtils.getString(crossReference, "idf:objectName").trim() + "@@" + xPathUtils.getString(crossReference, "idf:serviceUrl").trim() + "@@"
                        + xPathUtils.getString(idfRecord.getOriginalResponse(), "//gmd:identificationInfo/*/gmd:citation/*/gmd:identifier/*/gmd:code/gco:CharacterString").trim();
                assertEquals("Crossreference from dataset to service exists.", data, doc.getValues("refering_service_uuid")[0]);
            }

        }

    }

}
