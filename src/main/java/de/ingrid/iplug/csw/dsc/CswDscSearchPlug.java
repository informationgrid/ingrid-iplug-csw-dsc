/*
 * **************************************************-
 * ingrid-iplug-csw-dsc:war
 * ==================================================
 * Copyright (C) 2014 - 2024 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.2 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
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

import de.ingrid.admin.Config;
import de.ingrid.admin.elasticsearch.IndexScheduler;
import de.ingrid.elasticsearch.ElasticConfig;
import de.ingrid.elasticsearch.IBusIndexManager;
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
import de.ingrid.utils.query.ClauseQuery;
import de.ingrid.utils.query.FieldQuery;
import de.ingrid.utils.query.IngridQuery;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tomcat.util.scan.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.ImportResource;

import java.io.IOException;

/**
 * This iPlug connects to the iBus delivers search results based on a index.
 *
 * @author joachim@wemove.com
 */
@ImportResource({"/springapp-servlet.xml", "/override/*.xml"})
@SpringBootApplication(scanBasePackages = "de.ingrid")
@ComponentScan(
        basePackages = "de.ingrid",
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "de.ingrid.admin.object.DefaultDataType"),
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "de.ingrid.admin.object.BasePlug"),
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "de.ingrid.admin.BaseWebappApplication"),
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "de.ingrid.admin.controller.RedirectController"),

        })
public class CswDscSearchPlug extends HeartBeatPlug implements IRecordLoader {

    /**
     * The logging object
     */
    private static final Log log = LogFactory.getLog(CswDscSearchPlug.class);

    @Autowired
    private ElasticConfig elasticConfig;

    @Autowired
    private IBusIndexManager iBusIndexManager;

    @Autowired
    private IBusIndexManager indexManager;

    private IdfRecordCreator dscRecordProducer = null;

    private final IndexImpl _indexSearcher;

    public static Configuration conf;
    private final IndexScheduler indexScheduler;

    private final Config baseConfig;

    @Autowired
    public CswDscSearchPlug(
            final IndexImpl indexSearcher,
            Config baseConfig,
            Configuration externalConfig,
            IPlugdescriptionFieldFilter[] fieldFilters,
            IMetadataInjector[] injector,
            IPreProcessor[] preProcessors,
            IPostProcessor[] postProcessors,
            IndexScheduler indexScheduler) {
        super(60000, new PlugDescriptionFieldFilters(fieldFilters), injector, preProcessors, postProcessors);
        _indexSearcher = indexSearcher;
        this.indexScheduler = indexScheduler;

        this.baseConfig = baseConfig;
        try {
            baseConfig.initialize();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (externalConfig != null) {
            externalConfig.initialize();
        } else {
            log.info("No external configuration found.");
        }
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

        // request iBus directly to get search results from within this iPlug
        // adapt query to only get results coming from this iPlug and activated in iBus
        // But when not connected to an iBus then use direct connection to Elasticsearch
        if (elasticConfig.esCommunicationThroughIBus) {

            ClauseQuery cq = new ClauseQuery(true, false);
            cq.addField(new FieldQuery(true, false, "iPlugId", baseConfig.communicationProxyUrl));
            query.addClause(cq);
            return this.iBusIndexManager.search(query, start, length);
        }

        return _indexSearcher.search(query, start, length);
    }

    /*
     * (non-Javadoc)
     *
     * @see de.ingrid.utils.IRecordLoader#getRecord(de.ingrid.utils.IngridHit)
     */
    @Override
    public Record getRecord(IngridHit hit) throws Exception {
        ElasticDocument document;
        if (elasticConfig.esCommunicationThroughIBus) {
            document = this.iBusIndexManager.getDocById(hit.getDocumentId());
        } else {
            document = indexManager.getDocById(hit.getDocumentId());
        }

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

        IngridHitDetail detail;
        // request iBus directly to get search results from within this iPlug
        // adapt query to only get results coming from this iPlug and activated in iBus
        // But when not connected to an iBus then use direct connection to Elasticsearch
        if (elasticConfig.esCommunicationThroughIBus) {
            detail = this.iBusIndexManager.getDetail(hit, query, fields);
        } else {
            detail = _indexSearcher.getDetail(hit, query, fields);
        }

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
        ElasticDocument luceneDoc;
        if (elasticConfig.esCommunicationThroughIBus) {
            luceneDoc = this.iBusIndexManager.getDocById(document.getDocumentId());
        } else {
            luceneDoc = indexManager.getDocById(document.getDocumentId());
        }

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
        System.setProperty(Constants.SKIP_JARS_PROPERTY, "activation.jar,xercesImpl.jar,xml-apis.jar,serializer.jar");
        SpringApplication.run(CswDscSearchPlug.class, args);
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
