/**
 * 
 */
package de.ingrid.iplug.csw.dsc.index.producer;

import java.io.IOException;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.ingrid.iplug.csw.dsc.cache.Cache;
import de.ingrid.iplug.csw.dsc.cache.UpdateJob;
import de.ingrid.iplug.csw.dsc.cswclient.CSWFactory;
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
public class CswRecordSetProducer implements IRecordSetProducer {

    Cache cache;
    Cache tmpCache = null;

    CSWFactory factory;

    Iterator<String> recordIdIterator = null;

    final private static Log log = LogFactory
            .getLog(CswRecordSetProducer.class);

    public CswRecordSetProducer() {
        log.info("PlugDescriptionConfiguredDatabaseRecordProducer started.");
    }

    public void init() {
        cache.configure(factory);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.ingrid.iplug.dsc.index.IRecordProducer#hasNext()
     */
    @Override
    public boolean hasNext() {
        if (recordIdIterator == null) {
            try {
                // start transaction
                tmpCache = cache.startTransaction();
                tmpCache.removeAllRecords();

                // run the update job: fetch all csw data from csw source
                UpdateJob job = new UpdateJob(factory, tmpCache);
                job.execute();

                recordIdIterator = tmpCache.getCachedRecordIds().iterator();
            } catch (Exception e) {
                if (tmpCache != null) {
                    tmpCache.rollbackTransaction();
                }
            }
        }
        if (recordIdIterator.hasNext()) {
            return true;
        } else {
            recordIdIterator = null;
            return false;
        }
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
        } catch (IOException e) {
            log.error("Error reading record '" + recordId + "' from cache '"
                    + cache.toString() + "'.");
        }
        return null;
    }

    public Cache getCache() {
        return cache;
    }

    public void setCache(Cache cache) {
        this.cache = cache;
    }

    public CSWFactory getFactory() {
        return factory;
    }

    public void setFactory(CSWFactory factory) {
        this.factory = factory;
    }

}
