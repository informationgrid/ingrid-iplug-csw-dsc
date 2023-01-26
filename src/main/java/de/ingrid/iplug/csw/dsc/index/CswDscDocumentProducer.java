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
package de.ingrid.iplug.csw.dsc.index;

import de.ingrid.admin.object.IDocumentProducer;
import de.ingrid.elasticsearch.IndexInfo;
import de.ingrid.iplug.csw.dsc.Configuration;
import de.ingrid.iplug.csw.dsc.analyze.CoupledResources;
import de.ingrid.iplug.csw.dsc.analyze.IsoCacheCoupledResourcesAnalyzer;
import de.ingrid.iplug.csw.dsc.cache.Cache;
import de.ingrid.iplug.csw.dsc.cache.UpdateJob;
import de.ingrid.iplug.csw.dsc.cswclient.CSWFactory;
import de.ingrid.iplug.csw.dsc.cswclient.CSWRecord;
import de.ingrid.iplug.csw.dsc.index.mapper.IRecordMapper;
import de.ingrid.iplug.csw.dsc.index.producer.ICswCacheRecordSetProducer;
import de.ingrid.iplug.csw.dsc.om.CswCacheSourceRecord;
import de.ingrid.iplug.csw.dsc.om.CswCoupledResourcesCacheSourceRecord;
import de.ingrid.iplug.csw.dsc.om.SourceRecord;
import de.ingrid.utils.ElasticDocument;
import de.ingrid.utils.PlugDescription;
import de.ingrid.utils.statusprovider.StatusProviderService;
import de.ingrid.utils.statusprovider.StatusProvider.Classification;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

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
    
    IsoCacheCoupledResourcesAnalyzer isoCacheCoupledResourcesAnalyzer;
    
    private CoupledResources coupledResources = null;

    @Autowired
    StatusProviderService statusProviderService;

    @Autowired
    IndexInfo indexInfo;

    @Autowired
    private Configuration cswConfig;
    
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
        boolean result = false;
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
                    
                    // analyze records for coupling information
                    coupledResources = isoCacheCoupledResourcesAnalyzer.analyze( tmpCache );
                    

                } catch (Exception e) {
                    statusProviderService.getDefaultStatusProvider().addState( "ERROR_FETCH", "Error harvesting CSW datasource with URL: " + cswConfig.serviceUrl, Classification.ERROR );
                    log.error("Error harvesting CSW datasource.", e);
                    if (tmpCache != null) {
                        tmpCache.rollbackTransaction();
                    }
                    throw new RuntimeException("Error harvesting CSW datasource");
                }
            }
            if (recordSetProducer.hasNext()) {
                result = true;
            } else {
                // prevent runtime exception if the cache was not in transaction
                // this can happen if the harvest process throws an exception and the
                // transaction was rolled back (see above)
                if (tmpCache.isInTransaction()) {
                    tmpCache.commitTransaction();
                }
                tmpCache = null;
                result = false;
            }
        } catch (Exception e) {
            log.error("Error obtaining information about a next record. Skip all records.", e);
            // make sure the tmp cache is released after exception occurs
            // otherwise the indexer will never "heal" from this exception
            tmpCache = null;
            throw new RuntimeException("Error harvesting CSW datasource");
        } finally {
            if (!result) {
                tmpCache = null;
                for (IRecordMapper mapper : recordMapperList) {
                    mapper.cleanup();
                }
            }
            
        }
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.ingrid.admin.object.IDocumentProducer#next()
     */
    @Override
    public ElasticDocument next() {
        ElasticDocument doc = new ElasticDocument();
        CswCacheSourceRecord record = null;
        try {
            record = (CswCacheSourceRecord)recordSetProducer.next();
            CSWRecord cswRecord = (CSWRecord) record.get( CswCacheSourceRecord.CSW_RECORD );
            SourceRecord sourceRecord = new CswCoupledResourcesCacheSourceRecord(cswRecord, tmpCache, coupledResources.getCoupledRecordIds(cswRecord.getId()));
            for (IRecordMapper mapper : recordMapperList) {
                long start = 0;
                if (log.isDebugEnabled()) {
                    start = System.currentTimeMillis();
                }
                mapper.map(sourceRecord, doc);
                if (log.isDebugEnabled()) {
                    log.debug("Mapping of source record with " + mapper + " took: " + (System.currentTimeMillis() - start) + " ms.");
                }
            }
            return doc;
        } catch (Throwable t) {
        	if (record == null) {
                log.error("Error obtaining next record, IS NULL.", t);
        	} else {
                log.error("Error mapping record.", t);
        	}

            // DO NOT EMPTY CACHE !!! We want to continue indexing the fetched records !!!
            // if tmpCache is set to null the fetching process is started from scratch (see this.hasNext() method) !

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

    
    public IsoCacheCoupledResourcesAnalyzer getIsoCacheCoupledResourcesAnalyzer() {
        return isoCacheCoupledResourcesAnalyzer;
    }

    public void setIsoCacheCoupledResourcesAnalyzer(IsoCacheCoupledResourcesAnalyzer isoCacheCoupledResourcesAnalyzer) {
        this.isoCacheCoupledResourcesAnalyzer = isoCacheCoupledResourcesAnalyzer;
    }

    @Override
    public IndexInfo getIndexInfo() {
        return indexInfo;
    }

    @Override
    public Integer getDocumentCount() {
        return null;
    }
    
}
