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
