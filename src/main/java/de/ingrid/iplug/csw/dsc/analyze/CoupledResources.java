/**
 * 
 */
package de.ingrid.iplug.csw.dsc.analyze;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.ingrid.iplug.csw.dsc.cswclient.CSWRecord;

/**
 * Represents a list of coupled resources of ISO metadata records. The coupling
 * relates record UUIDs to records.
 * 
 * 
 * @author joachim
 * 
 */
public class CoupledResources {

    Map<String, List<CSWRecord>> coupling = new HashMap<String, List<CSWRecord>>();

    public void addCoupling(String datasetId, CSWRecord record) {
        if (!coupling.containsKey(datasetId)) {
            List<CSWRecord> entry = new ArrayList<CSWRecord>();
            coupling.put(datasetId, entry);
        }
        List<CSWRecord> entry = coupling.get(datasetId);
        boolean newCoupling = true;
        for (CSWRecord r : entry) {
            if (r.getId().equals(record.getId())) {
                newCoupling = false;
                break;
            }
        }
        if (newCoupling) {
            entry.add(record);
        }
    }

    public List<CSWRecord> getCoupledRecords(String datasetId) {
        return coupling.get(datasetId);
    }

    public Integer getSize() {
        return coupling.size();
    }

}
