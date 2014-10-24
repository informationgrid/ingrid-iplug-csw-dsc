/*
 * Copyright (c) 2006 wemove digital solutions. All rights reserved.
 */

package de.ingrid.iplug.csw.dsc;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tngtech.configbuilder.ConfigBuilder;

import de.ingrid.admin.JettyStarter;
import de.ingrid.admin.search.IngridIndexSearcher;
import de.ingrid.iplug.HeartBeatPlug;
import de.ingrid.iplug.IPlugdescriptionFieldFilter;
import de.ingrid.iplug.PlugDescriptionFieldFilters;
import de.ingrid.iplug.csw.dsc.cswclient.constants.ElementSetName;
import de.ingrid.iplug.csw.dsc.record.IdfRecordCreator;
import de.ingrid.utils.IRecordLoader;
import de.ingrid.utils.IngridDocument;
import de.ingrid.utils.IngridHit;
import de.ingrid.utils.IngridHitDetail;
import de.ingrid.utils.IngridHits;
import de.ingrid.utils.dsc.Record;
import de.ingrid.utils.metadata.IMetadataInjector;
import de.ingrid.utils.processor.IPostProcessor;
import de.ingrid.utils.processor.IPreProcessor;
import de.ingrid.utils.query.IngridQuery;

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

    private final IngridIndexSearcher _indexSearcher;

    public static Configuration conf;
	
    @Autowired
    public CswDscSearchPlug(final IngridIndexSearcher indexSearcher, IPlugdescriptionFieldFilter[] fieldFilters, IMetadataInjector[] injector, IPreProcessor[] preProcessors, IPostProcessor[] postProcessors) throws IOException {
        super(60000, new PlugDescriptionFieldFilters(fieldFilters), injector, preProcessors, postProcessors);
        _indexSearcher = indexSearcher;
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
        Document document = _indexSearcher.doc(hit.getDocumentId());
        return dscRecordProducer.getRecord(document);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.ingrid.iplug.HeartBeatPlug#close()
     */
    @Override
    public void close() throws Exception {
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
            this.setDirectData(detail, elementSetName);
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
     * @param document
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
     * @param document
     * @param elementSetName
     * @throws Exception
     */
    protected void setDirectData(IngridHitDetail document, ElementSetName elementSetName) throws Exception {
        Document luceneDoc = _indexSearcher.doc(document.getDocumentId());
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
        conf = new ConfigBuilder<Configuration>(Configuration.class).withCommandLineArgs(args).build();
        new JettyStarter( conf );
    }
}
