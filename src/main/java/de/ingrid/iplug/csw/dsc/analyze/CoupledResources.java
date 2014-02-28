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
