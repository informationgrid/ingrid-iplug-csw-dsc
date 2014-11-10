/*
 * **************************************************-
 * ingrid-iplug-csw-dsc:war
 * ==================================================
 * Copyright (C) 2014 wemove digital solutions GmbH
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

import org.w3c.dom.Node;

import de.ingrid.iplug.csw.dsc.cswclient.CSWRecord;
import de.ingrid.iplug.csw.dsc.cswclient.constants.ElementSetName;

public class IdfTransformerTest extends BaseIndexTestCase {

    /**
     * @throws Exception
     */
    public void testTransform() throws Exception {

        prepareCache(null, null);
        
        CSWRecord record = cache.getRecord("CF902C59-D50B-42F6-ADE4-F3CEC39A3259", ElementSetName.IDF);
        Node n = record.getOriginalResponse();
        assertNotNull("Dataset IDF record CF902C59-D50B-42F6-ADE4-F3CEC39A3259 exists in cache.", record);
        assertEquals("Dataset IDF record CF902C59-D50B-42F6-ADE4-F3CEC39A3259 has reference to service CFA384AB-028F-476B-AC95-EB75CCEFB296.", "CFA384AB-028F-476B-AC95-EB75CCEFB296",
                xPathUtils.getString(n, "//idf:idfMdMetadata/idf:crossReference/@uuid"));
        
        record = cache.getRecord("CFA384AB-028F-476B-AC95-EB75CCEFB296", ElementSetName.IDF);
        n = record.getOriginalResponse();
        assertNotNull("Service IDF record CFA384AB-028F-476B-AC95-EB75CCEFB296 exists in cache.", record);
        assertEquals("Service IDF record CFA384AB-028F-476B-AC95-EB75CCEFB296 has reference to dataset CF902C59-D50B-42F6-ADE4-F3CEC39A3259.", "CF902C59-D50B-42F6-ADE4-F3CEC39A3259",
                xPathUtils.getString(n, "//idf:idfMdMetadata/idf:crossReference/@uuid"));

        record = cache.getRecord("0C12204F-5626-4A2E-94F4-514424F093A1", ElementSetName.IDF);
        n = record.getOriginalResponse();
        assertNotNull("Dataset IDF record 0C12204F-5626-4A2E-94F4-514424F093A1 exists in cache.", record);
        assertEquals("Dataset IDF record 0C12204F-5626-4A2E-94F4-514424F093A1 has reference to service 77793F43-707A-4346-9A24-9F4E22213F54.", "77793F43-707A-4346-9A24-9F4E22213F54",
                xPathUtils.getString(n, "//idf:idfMdMetadata/idf:crossReference/@uuid"));

        record = cache.getRecord("486d9622-c29d-44e5-b878-44389740011", ElementSetName.IDF);
        n = record.getOriginalResponse();
        assertNotNull("Dataset IDF record 486d9622-c29d-44e5-b878-44389740011 exists in cache.", record);
        assertEquals("Dataset IDF record 486d9622-c29d-44e5-b878-44389740011 has reference to service 77793F43-707A-4346-9A24-9F4E22213F54.", "77793F43-707A-4346-9A24-9F4E22213F54",
                xPathUtils.getString(n, "//idf:idfMdMetadata/idf:crossReference/@uuid"));
        
        
    }

}
