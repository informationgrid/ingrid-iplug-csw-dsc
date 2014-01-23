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

import de.ingrid.iplug.csw.dsc.cswclient.CSWRecord;
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
        List<CSWRecord> coupledResources = (List<CSWRecord>) record.get(CswCoupledResourcesCacheSourceRecord.COUPLED_RESOURCES);

        if (coupledResources != null) {
            DOMUtils domUtils = new DOMUtils(doc, xPathUtils, nsContext);
            IdfElement mdMetadata = domUtils.getElement(doc, "/idf:html/idf:body/idf:idfMdMetadata");
            for (CSWRecord serviceRecord : coupledResources) {
                Node n = serviceRecord.getOriginalResponse();
                String serviceType = xPathUtils.getString(n, "//srv:serviceType/gco:LocalName");
                NodeList operationNodes = xPathUtils.getNodeList(n, "//srv:containsOperations/srv:SV_OperationMetadata");
                for (int i = 0; i < operationNodes.getLength(); i++) {
                    Node operationNode = operationNodes.item(i);
                    String operationName = xPathUtils.getString(operationNode, "srv:operationName/gco:CharacterString");
                    String operationServiceUrl = xPathUtils.getString(operationNode, "srv:connectPoint/gmd:CI_OnlineResource/gmd:linkage/gmd:URL");
                    if (operationName.equalsIgnoreCase("getcapabilities")) {
                        IdfElement crossReference = mdMetadata.addElement("idf:crossReference");
                        crossReference.addAttribute("direction", "IN").addAttribute("uuid", serviceRecord.getId()).addAttribute("orig-uuid", serviceRecord.getId());
                        crossReference.addElement("idf:objectName").addText(xPathUtils.getString(n, "//gmd:identificationInfo/*/gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString"));
                        crossReference.addElement("idf:objectType").addText("3");
                        crossReference.addElement("idf:description").addText(xPathUtils.getString(n, "//gmd:identificationInfo/*/gmd:abstract/gco:CharacterString"));
                        crossReference.addElement("idf:serviceType").addText(serviceType);
                        crossReference.addElement("idf:serviceOperation").addText(operationName);
                        crossReference.addElement("idf:serviceUrl").addText(operationServiceUrl);
                    }
                }
            }

        }
    }

}
