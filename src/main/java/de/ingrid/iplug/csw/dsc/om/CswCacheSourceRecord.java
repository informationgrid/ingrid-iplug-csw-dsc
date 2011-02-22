/**
 * 
 */
package de.ingrid.iplug.csw.dsc.om;

import de.ingrid.iplug.csw.dsc.cswclient.CSWRecord;

/**
 * Represents a record set from a csw cache.
 * 
 * @author joachim@wemove.com
 * 
 */
public class CswCacheSourceRecord extends SourceRecord {

    private static final long serialVersionUID = 5660303708840795055L;

    public static final String CSW_RECORD = "cswRecord";

    /**
     * Creates a CswCacheSourceRecord. It holds the source record id and the
     * cache for further usage.
     * 
     * @param id
     * @param connection
     */
    public CswCacheSourceRecord(CSWRecord record) {
        super(record.getId());
        this.put(CSW_RECORD, record);
    }

}
