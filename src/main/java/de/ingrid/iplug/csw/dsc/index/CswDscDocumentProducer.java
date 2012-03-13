/**
 * 
 */
package de.ingrid.iplug.csw.dsc.index;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.Document;

import de.ingrid.admin.object.IDocumentProducer;
import de.ingrid.iplug.csw.dsc.cache.Cache;
import de.ingrid.iplug.csw.dsc.cache.UpdateJob;
import de.ingrid.iplug.csw.dsc.cswclient.CSWFactory;
import de.ingrid.iplug.csw.dsc.index.mapper.IRecordMapper;
import de.ingrid.iplug.csw.dsc.index.producer.ICswCacheRecordSetProducer;
import de.ingrid.iplug.csw.dsc.om.SourceRecord;
import de.ingrid.utils.PlugDescription;

/**
 * @author joachim
 * 
 */
public class CswDscDocumentProducer implements IDocumentProducer {

    private ICswCacheRecordSetProducer recordSetProducer = null;

    private List<IRecordMapper> recordMapperList = null;

    Cache cache;

    Cache tmpCache = null;

    CSWFactory factory;
    
    UpdateJob job;
    
    final private static Log log = LogFactory.getLog(CswDscDocumentProducer.class);
    
    public CswDscDocumentProducer() {
        log.info("CswDscDocumentProducer started.");
    }
    
    public void init() {
        cache.configure(factory);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see de.ingrid.admin.object.IDocumentProducer#hasNext()
     */
    @Override
    public boolean hasNext() {
        try {
            if (tmpCache == null) {
                try {
                    // start transaction
                    tmpCache = cache.startTransaction();
                    recordSetProducer.setCache(tmpCache);
                    tmpCache.removeAllRecords();

                    // run the update job: fetch all csw data from csw source
                    job.setCache(tmpCache);
                    job.init();
                    job.execute();
                    

                } catch (Exception e) {
                    log.error("Error harvesting CSW datasource.", e);
                    if (tmpCache != null) {
                        tmpCache.rollbackTransaction();
                    }
                }
            }
            if (recordSetProducer.hasNext()) {
                return true;
            } else {
                // prevent runtime exception if the cache was not in transaction
                // this can happen if the harvest process throws an exception and the
                // transaction was rolled back (see above)
                if (tmpCache.isInTransaction()) {
                    tmpCache.commitTransaction();
                }
                tmpCache = null;
                return false;
            }
        } catch (Exception e) {
            log.error("Error obtaining information about a next record. Skip all records.", e);
            // make sure the tmp cache is released after exception occurs
            // otherwise the indexer will never "heal" from this exception
            tmpCache = null;
            return false;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.ingrid.admin.object.IDocumentProducer#next()
     */
    @Override
    public Document next() {
        Document doc = new Document();
        try {
            SourceRecord record = recordSetProducer.next();
            for (IRecordMapper mapper : recordMapperList) {
                long start = 0;
                if (log.isDebugEnabled()) {
                    start = System.currentTimeMillis();
                }
                mapper.map(record, doc);
                if (log.isDebugEnabled()) {
                    log.debug("Mapping of source record with " + mapper + " took: " + (System.currentTimeMillis() - start) + " ms.");
                }
            }
            return doc;
        } catch (Exception e) {
            log.error("Error obtaining next record.", e);
            if (tmpCache != null) {
                tmpCache.rollbackTransaction();
                tmpCache = null;
            }
            return null;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * de.ingrid.utils.IConfigurable#configure(de.ingrid.utils.PlugDescription)
     */
    @Override
    public void configure(PlugDescription arg0) {
        log.info("CswDscDocumentProducer: configure called.");
    }

    public ICswCacheRecordSetProducer getRecordSetProducer() {
        return recordSetProducer;
    }

    public void setRecordSetProducer(ICswCacheRecordSetProducer recordProducer) {
        this.recordSetProducer = recordProducer;
    }

    public List<IRecordMapper> getRecordMapperList() {
        return recordMapperList;
    }

    public void setRecordMapperList(List<IRecordMapper> recordMapperList) {
        this.recordMapperList = recordMapperList;
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
    
    public UpdateJob getJob() {
        return job;
    }

    public void setJob(UpdateJob job) {
        this.job = job;
    }
    
}
