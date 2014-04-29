/**
 * 
 */
package de.ingrid.iplug.csw.dsc.analyze;

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

    public CoupledResources analyze(Cache cache) throws Exception {

        CoupledResources result = new CoupledResources();

        Map<String, String> coupledResourceIdentifier2ServiceRecords = new HashMap<String, String>();
        Map<String, String> resourceIdentifierMap = new HashMap<String, String>();

        if (log.isInfoEnabled()) {
            log.info("Start analyzing " + cache.getCachedRecordIds().size() + " records for coupled resources.");
        }

        for (String id : cache.getCachedRecordIds()) {
            CSWRecord record = cache.getRecord(id, ElementSetName.FULL);
            Node n = record.getOriginalResponse();

            // record resource identifiers
            String[] resourceIdentifiers = xPathUtils.getStringArray(n, "//gmd:identificationInfo/*/gmd:citation/*/gmd:identifier/*/gmd:code/gco:CharacterString");
            for (String resourceIdentifier : resourceIdentifiers) {
                resourceIdentifier = resourceIdentifier.trim();
                if (resourceIdentifier != null && resourceIdentifier.length() > 0) {
                    resourceIdentifierMap.put(resourceIdentifier, record.getId());
                }
            }

            // get coupled datasets for service records that are coupled over
            // the uuid of the record
            String[] coupledDatasets = xPathUtils.getStringArray(n, "//srv:operatesOn[//srv:serviceType/gco:LocalName='view']/@uuidref");
            if (coupledDatasets != null && coupledDatasets.length > 0) {
                for (String datasetId : coupledDatasets) {
                    datasetId = datasetId.trim();
                    if (datasetId != null && datasetId.length() > 0 && cache.isCached(datasetId, ElementSetName.FULL)) {
                        if (log.isDebugEnabled()) {
                            log.debug("Found coupling between dataset '" + datasetId + "' and service '" + record.getId() + "' by uuid reference.");
                        }
                        // add dataset -> service record
                        result.addCoupling(datasetId, record.getId());
                        // add service -> dataset record
                        result.addCoupling(id, cache.getRecord(datasetId, ElementSetName.FULL).getId());
                    }
                }
            }

            String[] coupledResources = xPathUtils.getStringArray(n, "//srv:operatesOn[//srv:serviceType/gco:LocalName='view']/@xlink:href");
            if (coupledResources != null && coupledResources.length > 0) {
                for (String coupledResource : coupledResources) {
                    coupledResource = coupledResource.trim();
                    if (coupledResource != null && coupledResource.length() > 0) {
                        coupledResourceIdentifier2ServiceRecords.put(coupledResource, record.getId());
                    }
                }
            }
        }
        for (String resourceIdentifier : resourceIdentifierMap.keySet()) {
            String datasetRecordId = resourceIdentifierMap.get(resourceIdentifier);
            String serviceRecordId = coupledResourceIdentifier2ServiceRecords.get(resourceIdentifier);
            if (serviceRecordId != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Found coupling between dataset '" + datasetRecordId + "' and service '" + serviceRecordId + "' by resource identifier reference.");
                }
                // add dataset -> service record
                result.addCoupling(datasetRecordId, serviceRecordId);
                // add service -> dataset record
                result.addCoupling(serviceRecordId, datasetRecordId);
            }
        }
        resourceIdentifierMap.clear();
        resourceIdentifierMap = null;

        coupledResourceIdentifier2ServiceRecords.clear();
        coupledResourceIdentifier2ServiceRecords = null;

        if (log.isInfoEnabled()) {
            log.info("Found " + result.getSize() + " couplings between datasets and services.");
        }
        return result;

    }

}
