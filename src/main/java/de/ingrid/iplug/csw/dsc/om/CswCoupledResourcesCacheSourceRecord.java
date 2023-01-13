/*
 * **************************************************-
 * ingrid-iplug-csw-dsc:war
 * ==================================================
 * Copyright (C) 2014 - 2023 wemove digital solutions GmbH
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
package de.ingrid.iplug.csw.dsc.om;

import java.util.List;

import de.ingrid.iplug.csw.dsc.cache.Cache;
import de.ingrid.iplug.csw.dsc.cswclient.CSWRecord;

/**
 * Represents a record set from a csw cache including coupling information.
 * 
 * @author joachim@wemove.com
 * 
 */
public class CswCoupledResourcesCacheSourceRecord extends CswCacheSourceRecord {

    private static final long serialVersionUID = 5660304708840795055L;

    public static final String COUPLED_RESOURCES = "coupledResources";
    public static final String CACHE = "cache";

    /**
     * Creates a CswCoupledResourcesCacheSourceRecord. It holds the source
     * record id, the cache of the records and the coupled record ids, in case
     * it has specific coupling, that is not part of the original ISO format,
     * for further usage.
     * 
     * @param id
     * @param connection
     */
    public CswCoupledResourcesCacheSourceRecord(CSWRecord record, Cache cache, List<String> coupledResourceIds) {
        super(record);
        this.put(COUPLED_RESOURCES, coupledResourceIds);
        this.put(CACHE, cache);
    }

}
