/**
 * 
 */
package de.ingrid.iplug.csw.dsc.om;

import java.util.List;

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

    /**
     * Creates a CswCoupledResourcesCacheSourceRecord. It holds the source
     * record id and the record and the coupling information, in case it has
     * specific coupling, that is not part of the original format, for further
     * usage.
     * 
     * @param id
     * @param connection
     */
    public CswCoupledResourcesCacheSourceRecord(CSWRecord record, List<CSWRecord> coupledResources) {
        super(record);
        this.put(COUPLED_RESOURCES, coupledResources);
    }

}
