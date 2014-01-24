package de.ingrid.iplug.csw.dsc.index;

import org.springframework.core.io.FileSystemResource;
import org.w3c.dom.Node;

import de.ingrid.iplug.csw.dsc.cswclient.CSWRecord;
import de.ingrid.iplug.csw.dsc.cswclient.constants.ElementSetName;
import de.ingrid.iplug.csw.dsc.record.mapper.ProcessIdfMapper;

public class IdfTransformerWsvGeodatenkatalogTest extends BaseIndexTestCase {

    /**
     * @throws Exception
     */
    public void testTransform() throws Exception {

        ProcessIdfMapper processIdfMapperWSV = new ProcessIdfMapper();
        processIdfMapperWSV.setStyleSheetResource(new FileSystemResource("src/main/release/presets/wsv/mapping/post_process_idf_wsv_geodatenkatalog.xsl"));
        
        prepareCache(processIdfMapperWSV, "24265_wsv");

        for (String id : cache.getCachedRecordIds()) {
            CSWRecord idfRecord = cache.getRecord(id, ElementSetName.IDF);
            Node idfDoc = idfRecord.getOriginalResponse();
            String[] urls = xPathUtils.getStringArray(idfDoc, "//gmd:URL");
            assertTrue("Idf found.", idfDoc.hasChildNodes());
            assertTrue("Metadata found.", xPathUtils.nodeExists(idfDoc, "//idf:idfMdMetadata"));
            for (String url : urls) {
                assertTrue("All urls are absolute and start with 'http'.", url.startsWith("http"));
            }
        }
        
    }

}
