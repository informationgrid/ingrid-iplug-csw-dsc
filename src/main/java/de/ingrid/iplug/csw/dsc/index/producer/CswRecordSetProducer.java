/**
 * 
 */
package de.ingrid.iplug.csw.dsc.index.producer;

import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.ingrid.iplug.csw.dsc.cache.Cache;
import de.ingrid.iplug.csw.dsc.cswclient.constants.ElementSetName;
import de.ingrid.iplug.csw.dsc.om.CswCacheSourceRecord;
import de.ingrid.iplug.csw.dsc.om.SourceRecord;

/**
 * Takes care of selecting all source record Ids from a database. The SQL
 * statement is configurable via Spring.
 * 
 * The database connection is configured via the PlugDescription.
 * 
 * 
 * @author joachim@wemove.com
 * 
 */
public class CswRecordSetProducer implements ICswCacheRecordSetProducer {

    Cache cache;

    Iterator<String> recordIdIterator = null;

    final private static Log log = LogFactory
            .getLog(CswRecordSetProducer.class);

    public CswRecordSetProducer() {
        log.info("CswRecordSetProducer started.");
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.ingrid.iplug.dsc.index.IRecordProducer#hasNext()
     */
    @Override
    public boolean hasNext() {
        try {
            if (recordIdIterator == null) {
                recordIdIterator = cache.getCachedRecordIds().iterator();
            }
            if (recordIdIterator.hasNext()) {
                return true;
            }
        } catch (Exception e) {
            log.error("Error obtaining record from cache:" + cache, e);
        }
        recordIdIterator = null;
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.ingrid.iplug.dsc.index.IRecordProducer#next()
     */
    @Override
    public SourceRecord next() {
        String recordId = null;
        try {
            recordId = recordIdIterator.next();
            return new CswCacheSourceRecord(cache.getRecord(recordId,
                    ElementSetName.FULL));
        } catch (Exception e) {
            log.error("Error reading record '" + recordId + "' from cache '"
                    + cache + "'.");
        }
        return null;
    }

    public void setCache(Cache cache) {
        this.cache = cache;
    }
    
}
