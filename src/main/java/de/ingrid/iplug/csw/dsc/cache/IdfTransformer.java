/**
 * 
 */
package de.ingrid.iplug.csw.dsc.cache;

import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import de.ingrid.iplug.csw.dsc.analyze.CoupledResources;
import de.ingrid.iplug.csw.dsc.analyze.IsoCacheAnalyzer;
import de.ingrid.iplug.csw.dsc.cswclient.CSWRecord;
import de.ingrid.iplug.csw.dsc.cswclient.constants.ElementSetName;
import de.ingrid.iplug.csw.dsc.cswclient.impl.GenericRecord;
import de.ingrid.iplug.csw.dsc.om.CswCoupledResourcesCacheSourceRecord;
import de.ingrid.iplug.csw.dsc.om.SourceRecord;
import de.ingrid.iplug.csw.dsc.record.mapper.IIdfMapper;
import de.ingrid.utils.xml.XMLUtils;

/**
 * Transforms all CSW records in a cache to the IDF format. Also includes
 * coupling information between dataset and service records, since they do not
 * exists in the original CSW response.
 * 
 * @author joachim
 * 
 */
public class IdfTransformer {

    protected static final Logger log = Logger.getLogger(IdfTransformer.class);

    private List<IIdfMapper> record2IdfMapperList = null;

    private IsoCacheAnalyzer isoCacheAnalyzer;
    
    /**
     * Transforms all CSW records in a cache to the IDF format. Also includes
     * coupling information between dataset and service records, since they do
     * not exists in the original CSW response.
     * 
     * @param cache
     * @param coupledResources
     * @throws Exception
     */
    public void transform(Cache cache) throws Exception {

        // analyze records for coupling information
        CoupledResources coupledResources = isoCacheAnalyzer.analyze(cache);
        
        
        for (String id : cache.getCachedRecordIds()) {
            CSWRecord cswRecord = cache.getRecord(id, ElementSetName.FULL);

            Document idfDoc = null;

            try {
                SourceRecord sourceRecord = new CswCoupledResourcesCacheSourceRecord(cswRecord, coupledResources.getCoupledServices(cswRecord.getId()));
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder docBuilder = dbf.newDocumentBuilder();
                idfDoc = docBuilder.newDocument();

                for (IIdfMapper record2IdfMapper : record2IdfMapperList) {
                    long start = 0;
                    if (log.isDebugEnabled()) {
                        start = System.currentTimeMillis();
                    }
                    record2IdfMapper.map(sourceRecord, idfDoc);
                    if (log.isDebugEnabled()) {
                        log.debug("Mapping of source record with " + record2IdfMapper + " took: " + (System.currentTimeMillis() - start) + " ms.");
                    }
                }
                if (log.isDebugEnabled()) {
                    String data = XMLUtils.toString(idfDoc);
                    log.debug("Resulting IDF document:\n" + data);
                }
            } catch (Exception e) {
                log.error("Error creating IDF document.", e);
                throw e;
            }

            CSWRecord idfRecord = new GenericRecord();
            idfRecord.initialize(ElementSetName.IDF, idfDoc.getDocumentElement());
            cache.putRecord(idfRecord);
        }

    }

    public void setRecord2IdfMapperList(List<IIdfMapper> record2IdfMapperList) {
        this.record2IdfMapperList = record2IdfMapperList;
    }

    public void setIsoCacheAnalyzer(IsoCacheAnalyzer isoCacheAnalyzer) {
        this.isoCacheAnalyzer = isoCacheAnalyzer;
    }

}
