/**
 * 
 */
package de.ingrid.iplug.csw.dsc.record.mapper;

import java.util.List;

import javax.xml.namespace.NamespaceContext;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.ingrid.iplug.csw.dsc.cache.Cache;
import de.ingrid.iplug.csw.dsc.cswclient.CSWRecord;
import de.ingrid.iplug.csw.dsc.cswclient.constants.ElementSetName;
import de.ingrid.iplug.csw.dsc.om.CswCoupledResourcesCacheSourceRecord;
import de.ingrid.iplug.csw.dsc.om.SourceRecord;
import de.ingrid.iplug.csw.dsc.tools.DOMUtils;
import de.ingrid.iplug.csw.dsc.tools.DOMUtils.IdfElement;
import de.ingrid.utils.xml.IDFNamespaceContext;
import de.ingrid.utils.xpath.XPathUtils;

/**
 * Creates an IDF Document out of a CSW Record (SourceRecord) !
 * 
 * @author joachim@wemove.com
 * 
 */
public class CouplingResourcesMapper implements IIdfMapper {

    final private NamespaceContext nsContext = new IDFNamespaceContext();
    final private XPathUtils xPathUtils = new XPathUtils(nsContext);

    protected static final Logger log = Logger.getLogger(CouplingResourcesMapper.class);

    @Override
    public void map(SourceRecord record, Document doc) throws Exception {

        if (!(record instanceof CswCoupledResourcesCacheSourceRecord)) {
            log.error("Source Record is not a CswCoupledResourcesCacheSourceRecord!");
            throw new IllegalArgumentException("Source Record is not a CswCoupledResourcesCacheSourceRecord!");
        }

        @SuppressWarnings("unchecked")
        List<String> coupledResourceIds = (List<String>) record.get(CswCoupledResourcesCacheSourceRecord.COUPLED_RESOURCES);
        Cache cache = (Cache) record.get(CswCoupledResourcesCacheSourceRecord.CACHE);

        if (coupledResourceIds != null) {
            DOMUtils domUtils = new DOMUtils(doc, xPathUtils, nsContext);
            IdfElement mdMetadata = domUtils.getElement(doc, "/idf:html/idf:body/idf:idfMdMetadata");
            for (String coupledRecordId : coupledResourceIds) {
                CSWRecord coupledRecord = cache.getRecord(coupledRecordId, ElementSetName.FULL);
                // check for coupling
                Node coupledResourceNode = coupledRecord.getOriginalResponse();
                if (isServiceRecordDocument(coupledResourceNode)) {
                    String serviceType = xPathUtils.getString(coupledResourceNode, "//srv:serviceType/gco:LocalName");
                    NodeList operationNodes = xPathUtils.getNodeList(coupledResourceNode, "//srv:containsOperations/srv:SV_OperationMetadata");
                    for (int i = 0; i < operationNodes.getLength(); i++) {
                        Node operationNode = operationNodes.item(i);
                        String operationName = xPathUtils.getString(operationNode, "srv:operationName/gco:CharacterString");
                        String operationServiceUrl = xPathUtils.getString(operationNode, "srv:connectPoint/gmd:CI_OnlineResource/gmd:linkage/gmd:URL");
                        if (operationName.equalsIgnoreCase("getcapabilities")) {
                            IdfElement crossReference = mdMetadata.addElement("idf:crossReference");
                            crossReference.addAttribute("direction", "IN").addAttribute("uuid", coupledRecordId).addAttribute("orig-uuid", coupledRecordId);
                            crossReference.addElement("idf:objectName").addText(xPathUtils.getString(coupledResourceNode, "//gmd:identificationInfo/*/gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString"));
                            crossReference.addElement("idf:attachedToField").addAttribute("entry-id", "3600").addAttribute("list-id", "2000").addText("Gekoppelte Daten");
                            crossReference.addElement("idf:objectType").addText("3");
                            crossReference.addElement("idf:description").addText(xPathUtils.getString(coupledResourceNode, "//gmd:identificationInfo/*/gmd:abstract/gco:CharacterString"));
                            crossReference.addElement("idf:serviceType").addText(serviceType);
                            crossReference.addElement("idf:serviceOperation").addText(operationName);
                            crossReference.addElement("idf:serviceUrl").addText(operationServiceUrl);
                        }
                    }
                } else {
                    IdfElement crossReference = mdMetadata.addElement("idf:crossReference");
                    crossReference.addAttribute("direction", "OUT").addAttribute("uuid", coupledRecordId).addAttribute("orig-uuid", coupledRecordId);
                    crossReference.addElement("idf:objectName").addText(xPathUtils.getString(coupledResourceNode, "//gmd:identificationInfo/*/gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString"));
                    crossReference.addElement("idf:objectType").addText("1");
                    crossReference.addElement("idf:attachedToField").addAttribute("entry-id", "3600").addAttribute("list-id", "2000").addText("Gekoppelte Daten");
                    crossReference.addElement("idf:description").addText(xPathUtils.getString(coupledResourceNode, "//gmd:identificationInfo/*/gmd:abstract/gco:CharacterString"));
                }
            }
        }
    }

    private boolean isServiceRecordDocument(Node node) {
        return xPathUtils.nodeExists(node, "//srv:serviceType");
    }

}
