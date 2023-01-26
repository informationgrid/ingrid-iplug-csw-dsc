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
package de.ingrid.iplug.csw.dsc.analyze;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a list of coupled resources of ISO metadata records. The coupling
 * relates record UUIDs to coupled records UUIDs.
 * 
 * 
 * @author joachim
 * 
 */
public class CoupledResources {

    Map<String, List<String>> coupling = new HashMap<String, List<String>>();

    public void addCoupling(String datasetId, String recordId) {
        if (!coupling.containsKey(datasetId)) {
            List<String> entry = new ArrayList<String>();
            coupling.put(datasetId, entry);
        }
        List<String> entry = coupling.get(datasetId);
        boolean newCoupling = true;
        for (String r : entry) {
            if (r.equals(recordId)) {
                newCoupling = false;
                break;
            }
        }
        if (newCoupling) {
            entry.add(recordId);
        }
    }

    public List<String> getCoupledRecordIds(String datasetId) {
        return coupling.get(datasetId);
    }

    public Integer getSize() {
        return coupling.size();
    }

}
