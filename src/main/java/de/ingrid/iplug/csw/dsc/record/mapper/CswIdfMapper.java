/**
 * 
 */
package de.ingrid.iplug.csw.dsc.record.mapper;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;
import org.springframework.core.io.Resource;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import de.ingrid.iplug.csw.dsc.cswclient.CSWRecord;
import de.ingrid.iplug.csw.dsc.om.CswCacheSourceRecord;
import de.ingrid.iplug.csw.dsc.om.SourceRecord;
import de.ingrid.iplug.csw.dsc.tools.DocumentStyler;
import de.ingrid.utils.xml.IDFNamespaceContext;
import de.ingrid.utils.xml.XPathUtils;

/**
 * Creates a base InGrid Detail data Format (IDF) skeleton.
 * 
 * @author joachim@wemove.com
 * 
 */
public class CswIdfMapper implements IIdfMapper {

    protected static final Logger log = Logger.getLogger(CswIdfMapper.class);

    private Resource styleSheetResource;
    
    @Override
    public void map(SourceRecord record, Document doc) throws Exception {

        if (!(record instanceof CswCacheSourceRecord)) {
            log.error("Source Record is not a CswCacheSourceRecord!");
            throw new IllegalArgumentException(
                    "Source Record is not a CswCacheSourceRecord!");
        }

        CSWRecord cswRecord = (CSWRecord) record
                .get(CswCacheSourceRecord.CSW_RECORD);

        XPathUtils.getXPathInstance().setNamespaceContext(
                new IDFNamespaceContext());
        Node body = XPathUtils.getNode(doc, "/idf:html/idf:body");
        Node originalResponse = cswRecord.getOriginalResponse();
        Source style = new StreamSource(styleSheetResource.getInputStream());
        DocumentStyler ds = new DocumentStyler(style);
        
        Document idfResponse = ds.transform(originalResponse.getOwnerDocument());
        Node csw = doc.importNode(idfResponse.getDocumentElement(), true);

        body.appendChild(csw);
    }
    
    public Resource getStyleSheetResource() {
        return styleSheetResource;
    }

    public void setStyleSheetResource(Resource styleSheetResource) {
        this.styleSheetResource = styleSheetResource;
    }

}
