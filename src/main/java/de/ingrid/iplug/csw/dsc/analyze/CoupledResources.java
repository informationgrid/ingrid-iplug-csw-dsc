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
 * relates dataset UUIDs to service records since this information is not
 * included within the ISO file format and must be analyzed across all records
 * of a catalog.
 * 
 * 
 * @author joachim
 * 
 */
public class CoupledResources {

    Map<String, List<CSWRecord>> coupling = new HashMap<String, List<CSWRecord>>();

    public void addService(String datasetId, CSWRecord serviceRecord) {
        if (!coupling.containsKey(datasetId)) {
            List<CSWRecord> entry = new ArrayList<CSWRecord>();
            coupling.put(datasetId, entry);
        }
        List<CSWRecord> entry = coupling.get(datasetId);
        if (!entry.contains(serviceRecord)) {
            entry.add(serviceRecord);
        }
    }

    public List<CSWRecord> getCoupledServices(String datasetId) {
        return coupling.get(datasetId);
    }

    public Integer getSize() {
        return coupling.size();
    }

}
