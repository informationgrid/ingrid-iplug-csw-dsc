/*
 * **************************************************-
 * ingrid-iplug-csw-dsc:war
 * ==================================================
 * Copyright (C) 2014 - 2018 wemove digital solutions GmbH
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
/*
 * Copyright (c) 2006 wemove digital solutions. All rights reserved.
 */

package de.ingrid.iplug.csw.dsc;

import de.ingrid.admin.JettyStarter;
import de.ingrid.admin.elasticsearch.IndexScheduler;
import de.ingrid.elasticsearch.search.IndexImpl;
import de.ingrid.iplug.HeartBeatPlug;
import de.ingrid.iplug.IPlugdescriptionFieldFilter;
import de.ingrid.iplug.PlugDescriptionFieldFilters;
import de.ingrid.iplug.csw.dsc.cswclient.constants.ElementSetName;
import de.ingrid.iplug.csw.dsc.record.IdfRecordCreator;
import de.ingrid.utils.*;
import de.ingrid.utils.dsc.Record;
import de.ingrid.utils.metadata.IMetadataInjector;
import de.ingrid.utils.processor.IPostProcessor;
import de.ingrid.utils.processor.IPreProcessor;
import de.ingrid.utils.query.IngridQuery;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * This iPlug connects to the iBus delivers search results based on a index.
 * 
 * @author joachim@wemove.com
 * 
 */
@Service
public class CswDscSearchPlug extends HeartBeatPlug implements IRecordLoader {

    /**
     * The logging object
     */
    private static Log log = LogFactory.getLog(CswDscSearchPlug.class);

    private IdfRecordCreator dscRecordProducer = null;

    private final IndexImpl _indexSearcher;

    public static Configuration conf;
    private final IndexScheduler indexScheduler;

    @Autowired
    public CswDscSearchPlug(final IndexImpl indexSearcher, IPlugdescriptionFieldFilter[] fieldFilters, IMetadataInjector[] injector, IPreProcessor[] preProcessors, IPostProcessor[] postProcessors, IndexScheduler indexScheduler) {
        super(60000, new PlugDescriptionFieldFilters(fieldFilters), injector, preProcessors, postProcessors);
        _indexSearcher = indexSearcher;
        this.indexScheduler = indexScheduler;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.ingrid.utils.ISearcher#search(de.ingrid.utils.query.IngridQuery,
     * int, int)
     */
    @Override
    public final IngridHits search(final IngridQuery query, final int start, final int length) throws Exception {

        if (log.isDebugEnabled()) {
            log.debug("Incoming query: " + query.toString() + ", start=" + start + ", length=" + length);
        }
        preProcess(query);
        return _indexSearcher.search(query, start, length);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.ingrid.utils.IRecordLoader#getRecord(de.ingrid.utils.IngridHit)
     */
    @Override
    public Record getRecord(IngridHit hit) throws Exception {
        ElasticDocument document = _indexSearcher.getDocById( hit.getDocumentId() );
        return dscRecordProducer.getRecord(document);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.ingrid.iplug.HeartBeatPlug#close()
     */
    @Override
    public void close() {
        _indexSearcher.close();
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.ingrid.iplug.HeartBeatPlug#close()
     */
    @Override
    public IngridHitDetail getDetail(IngridHit hit, IngridQuery query, String[] fields) throws Exception {
        final IngridHitDetail detail = _indexSearcher.getDetail(hit, query, fields);

        // add original idf data (including the original response), if requested
        ElementSetName elementSetName = this.getDirectDataElementSetName(query);
        if (elementSetName != null) {
            if (log.isDebugEnabled()) {
                log.debug("Request for direct CSW Data found. (" + ConfigurationKeys.REQUEST_KEY_CSW_DIRECT_RESPONSE + ":" + elementSetName + ")");
            }
            this.setDirectData(detail);
        }

        return detail;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.ingrid.iplug.HeartBeatPlug#close()
     */
    @Override
    public IngridHitDetail[] getDetails(IngridHit[] hits, IngridQuery query, String[] fields) throws Exception {
        IngridHitDetail[] details = new IngridHitDetail[hits.length];
        for (int i = 0; i < hits.length; i++) {
            IngridHit ingridHit = hits[i];
            IngridHitDetail detail = getDetail(ingridHit, query, fields);
            details[i] = detail;
        }
        return details;
    }

    public IdfRecordCreator getDscRecordProducer() {
        return dscRecordProducer;
    }

    public void setDscRecordProducer(IdfRecordCreator dscRecordProducer) {
        this.dscRecordProducer = dscRecordProducer;
    }

    /**
     * Get the ElementSetName of the requested original csw data, if any
     * 
     * @param document is the document to add original csw data to
     * @return The ElementSetName or ElementSetName.FULL
     */
    protected ElementSetName getDirectDataElementSetName(IngridDocument document) {
        if (document.containsKey(ConfigurationKeys.REQUEST_KEY_CSW_DIRECT_RESPONSE)) {
            for (ElementSetName name : ElementSetName.values()) {
                if (name.toString().equals(document.getString(ConfigurationKeys.REQUEST_KEY_CSW_DIRECT_RESPONSE)))
                    return name;
            }
        }
        return null;
    }

    /**
     * Set the original idf data in an IngridHitDetail
     * 
     * @param document is the document to add idf data to
     * @throws Exception if record could not be found
     */
    protected void setDirectData(IngridHitDetail document) throws Exception {
        ElasticDocument luceneDoc = _indexSearcher.getDocById( document.getDocumentId() );
        long startTime = 0;
        if (log.isDebugEnabled()) {
            startTime = System.currentTimeMillis();
        }
        Record r = dscRecordProducer.getRecord(luceneDoc);
        if (log.isDebugEnabled()) {
            log.debug("Get IDF record in " + (System.currentTimeMillis() - startTime) + " ms");
        }
        document.put(ConfigurationKeys.RESPONSE_KEY_IDF_RECORD, r);
    }

    public static void main(String[] args) throws Exception {
        new JettyStarter();
    }

    @Override
    public IngridDocument call(IngridCall info) {
        IngridDocument doc = null;

        if ("index".equals(info.getMethod())) {
            indexScheduler.triggerManually();
            doc = new IngridDocument();
            doc.put("success", true);
        }
        log.warn("The following method is not supported: " + info.getMethod());

        return doc;
    }
}
