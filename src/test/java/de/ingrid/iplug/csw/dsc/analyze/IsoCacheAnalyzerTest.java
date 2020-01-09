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
/**
 * 
 */
package de.ingrid.iplug.csw.dsc.analyze;

import java.io.File;

import de.ingrid.iplug.csw.dsc.TestUtil;
import de.ingrid.iplug.csw.dsc.cache.Cache;
import de.ingrid.iplug.csw.dsc.cache.impl.DefaultFileCache;
import de.ingrid.iplug.csw.dsc.cswclient.CSWFactory;
import de.ingrid.iplug.csw.dsc.cswclient.CSWRecord;
import de.ingrid.iplug.csw.dsc.cswclient.constants.ElementSetName;
import de.ingrid.iplug.csw.dsc.cswclient.impl.GenericRecord;
import de.ingrid.utils.statusprovider.StatusProviderService;
import junit.framework.TestCase;

/**
 * @author joachim
 * 
 */
public class IsoCacheAnalyzerTest extends TestCase {

    private final String cachePath = "./analyze_test_case_cache";
    private Cache cache = null;
    
    StatusProviderService statusProviderService;
    
    public IsoCacheAnalyzerTest() {
        super();
        statusProviderService = new StatusProviderService();
    }

    /**
     * Test method for
     * {@link de.ingrid.iplug.csw.dsc.analyze.IsoCacheCoupledResourcesAnalyzer#analyze(de.ingrid.iplug.csw.dsc.cache.Cache)}
     * .
     * 
     * @throws Exception
     */
    public void testAnalyze() throws Exception {

        String[] ids = new String[] {
                "33462e89-e5ab-11c3-737d-b3a61366d028",
                "0C12204F-5626-4A2E-94F4-514424F093A1",
                "486d9622-c29d-44e5-b878-44389740011",
                "77793F43-707A-4346-9A24-9F4E22213F54",
                "CF902C59-D50B-42F6-ADE4-F3CEC39A3259",
                "CFA384AB-028F-476B-AC95-EB75CCEFB296"
        };
        
        for (String id : ids) {
            this.putRecord(id, ElementSetName.FULL);
        }


        DefaultFileCache cache = (DefaultFileCache) this.setupCache();

        IsoCacheCoupledResourcesAnalyzer isoCacheAnalyzer = new IsoCacheCoupledResourcesAnalyzer();
        isoCacheAnalyzer.setStatusProviderService( statusProviderService );
        CoupledResources result = isoCacheAnalyzer.analyze(cache);

        assertNull("Dataset 3B20D603-30D1-47D5-AC62-E10193CDE1D8 is coupled to service 33462e89-e5ab-11c3-737d-b3a61366d028, but does not exist in cache.",
                result.getCoupledRecordIds("3B20D603-30D1-47D5-AC62-E10193CDE1D8"));

        assertEquals("Dataset CF902C59-D50B-42F6-ADE4-F3CEC39A3259 is coupled to one service.",1, 
                result.getCoupledRecordIds("CF902C59-D50B-42F6-ADE4-F3CEC39A3259").size());

        assertEquals("Dataset CF902C59-D50B-42F6-ADE4-F3CEC39A3259 is coupled to service CFA384AB-028F-476B-AC95-EB75CCEFB296 by uuid ref.", "CFA384AB-028F-476B-AC95-EB75CCEFB296", 
                result.getCoupledRecordIds("CF902C59-D50B-42F6-ADE4-F3CEC39A3259").get(0));
        
        assertEquals("Dataset 486d9622-c29d-44e5-b878-44389740011 is coupled to one service.",1, 
                result.getCoupledRecordIds("486d9622-c29d-44e5-b878-44389740011").size());
        
        assertEquals("Dataset 486d9622-c29d-44e5-b878-44389740011 is coupled to service 77793F43-707A-4346-9A24-9F4E22213F54 by resource identifier.", "77793F43-707A-4346-9A24-9F4E22213F54",
                result.getCoupledRecordIds("486d9622-c29d-44e5-b878-44389740011").get(0));

        assertEquals("Dataset 0C12204F-5626-4A2E-94F4-514424F093A1 is coupled to one service.",1, 
                result.getCoupledRecordIds("0C12204F-5626-4A2E-94F4-514424F093A1").size());

        assertEquals("Dataset 0C12204F-5626-4A2E-94F4-514424F093A1 is coupled to service 77793F43-707A-4346-9A24-9F4E22213F54 by uuid ref.", "77793F43-707A-4346-9A24-9F4E22213F54",
                result.getCoupledRecordIds("0C12204F-5626-4A2E-94F4-514424F093A1").get(0));

        assertEquals("Service 77793F43-707A-4346-9A24-9F4E22213F54 is coupled to 2 existing datasets", 2,
                result.getCoupledRecordIds("77793F43-707A-4346-9A24-9F4E22213F54").size());
        
        
    }

    /**
     * Helper methods
     */

    protected void tearDown() {
        // delete cache
        TestUtil.deleteDirectory(new File(cachePath));
    }

    private Cache setupCache() {
        if (this.cache == null) {
            CSWFactory factory = new CSWFactory();
            factory.setRecordImpl("de.ingrid.iplug.csw.dsc.cswclient.impl.GenericRecord");
            DefaultFileCache cache = new DefaultFileCache();
            cache.configure(factory);
            cache.setCachePath(cachePath);
            this.cache = cache;
        }
        return this.cache;
    }

    private void putRecord(String id, ElementSetName elementSetName) throws Exception {
        Cache cache = this.setupCache();
        CSWRecord record = TestUtil.getRecord(id, elementSetName, new GenericRecord());
        cache.putRecord(record);
    }

}
