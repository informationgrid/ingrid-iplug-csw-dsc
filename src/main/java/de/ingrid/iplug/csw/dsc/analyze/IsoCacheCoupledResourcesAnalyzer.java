/*
 * **************************************************-
 * ingrid-iplug-csw-dsc:war
 * ==================================================
 * Copyright (C) 2014 - 2022 wemove digital solutions GmbH
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
package de.ingrid.iplug.csw.dsc.analyze;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Node;

import de.ingrid.iplug.csw.dsc.cache.Cache;
import de.ingrid.iplug.csw.dsc.cswclient.CSWRecord;
import de.ingrid.iplug.csw.dsc.cswclient.constants.ElementSetName;
import de.ingrid.utils.statusprovider.StatusProviderService;
import de.ingrid.utils.xml.IDFNamespaceContext;
import de.ingrid.utils.xpath.XPathUtils;

/**
 * Analyzes the cached CSW records and build coupling information between
 * dataset and services.
 * 
 * @author joachim
 * 
 */
public class IsoCacheCoupledResourcesAnalyzer {

    
    @Autowired
    private StatusProviderService statusProviderService;

    final private XPathUtils xPathUtils = new XPathUtils(new IDFNamespaceContext());

    protected static final Logger log = Logger.getLogger(IsoCacheCoupledResourcesAnalyzer.class);

    public CoupledResources analyze(Cache cache) throws Exception {

        CoupledResources result = new CoupledResources();

        Map<String, String> coupledResourceIdentifier2ServiceRecords = new HashMap<String, String>();
        Map<String, String> resourceIdentifierMap = new HashMap<String, String>();

        if (log.isInfoEnabled()) {
            log.info("Start analyzing " + cache.getCachedRecordIds().size() + " records for coupled resources.");
        }
        statusProviderService.getDefaultStatusProvider().addState( "ANALYZE_COUPLING", "Analyze for coupled resources..." );

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
        
        statusProviderService.getDefaultStatusProvider().addState( "ANALYZE_COUPLING", "Analyze for coupled resources... found " + result.getSize() + " couplings between datasets and services.");
        
        return result;

    }

    public void setStatusProviderService(StatusProviderService statusProviderService) {
        this.statusProviderService = statusProviderService;
    }

}
