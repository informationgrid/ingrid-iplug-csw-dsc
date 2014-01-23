/**
 * 
 */
package de.ingrid.iplug.csw.dsc.analyze;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;

import de.ingrid.iplug.csw.dsc.cache.Cache;
import de.ingrid.iplug.csw.dsc.cswclient.CSWRecord;
import de.ingrid.iplug.csw.dsc.cswclient.constants.ElementSetName;
import de.ingrid.utils.xml.IDFNamespaceContext;
import de.ingrid.utils.xpath.XPathUtils;

/**
 * Analyzes the cached CSW records and build coupling information between
 * dataset and services.
 * 
 * @author joachim
 * 
 */
public class IsoCacheAnalyzer {

    final private XPathUtils xPathUtils = new XPathUtils(new IDFNamespaceContext());

    protected static final Logger log = Logger.getLogger(IsoCacheAnalyzer.class);

    public CoupledResources analyze(Cache cache) throws IOException {

        CoupledResources result = new CoupledResources();

        Map<String, CSWRecord> coupledResourceIdentifier2ServiceRecords = new HashMap<String, CSWRecord>();
        Map<String, CSWRecord> resourceIdentifierMap = new HashMap<String, CSWRecord>();

        if (log.isInfoEnabled()) {
            log.info("Start analyzing " + cache.getCachedRecordIds().size() + " records for coupled resources.");
        }

        for (String id : cache.getCachedRecordIds()) {
            CSWRecord record = cache.getRecord(id, ElementSetName.FULL);
            Node n = record.getOriginalResponse();

            // record resource identifiers
            String[] resourceIdentifiers = xPathUtils.getStringArray(n, "//gmd:identificationInfo/*/gmd:citation/*/gmd:identifier/*/gmd:code/gco:CharacterString");
            for (String resourceIdentifier : resourceIdentifiers) {
                resourceIdentifierMap.put(resourceIdentifier, record);
            }

            // get coupled datasets for service records that are coupled over
            // the uuid of the record
            String[] coupledDatasets = xPathUtils.getStringArray(n, "//srv:operatesOn/@uuidref");
            if (coupledDatasets != null && coupledDatasets.length > 0) {
                for (String datasetId : coupledDatasets) {
                    if (cache.isCached(datasetId, ElementSetName.FULL)) {
                        if (log.isDebugEnabled()) {
                            log.debug("Found coupling between dataset '" + datasetId + "' and service '" + record.getId() + "' by uuid reference.");
                        }
                        result.addService(datasetId, record);
                    }
                }
            }

            String[] coupledResources = xPathUtils.getStringArray(n, "//srv:operatesOn/@xlink:href");
            if (coupledResources != null && coupledResources.length > 0) {
                for (String coupledResource : coupledResources) {
                    coupledResourceIdentifier2ServiceRecords.put(coupledResource, record);
                }
            }
        }
        for (String resourceIdentifier : resourceIdentifierMap.keySet()) {
            CSWRecord datasetRecord = resourceIdentifierMap.get(resourceIdentifier);
            CSWRecord serviceRecord = coupledResourceIdentifier2ServiceRecords.get(resourceIdentifier);
            if (serviceRecord != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Found coupling between dataset '" + datasetRecord.getId() + "' and service '" + serviceRecord.getId() + "' by resource identifier reference.");
                }
                result.addService(datasetRecord.getId(), serviceRecord);
            }
        }

        if (log.isInfoEnabled()) {
            log.info("Found " + result.getSize() + " couplings between datasets and services.");
        }
        return result;

    }

}
