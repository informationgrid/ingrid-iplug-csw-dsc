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
