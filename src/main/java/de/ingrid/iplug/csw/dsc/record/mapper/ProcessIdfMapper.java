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

import de.ingrid.iplug.csw.dsc.om.SourceRecord;
import de.ingrid.iplug.csw.dsc.tools.DocumentStyler;

/**
 * Processes the IDF Record ! SourceRecord (CSW) not needed but passed because of interface.
 */
public class ProcessIdfMapper implements IIdfMapper {

    protected static final Logger log = Logger.getLogger(ProcessIdfMapper.class);

    private Resource styleSheetResource;

    @Override
    public void map(SourceRecord record, Document doc) throws Exception {

    	// Transform incoming IDF !
        Source style = new StreamSource(styleSheetResource.getInputStream());
        DocumentStyler ds = new DocumentStyler(style);

        Document idfResponse = ds.transform(doc);
        Node newIdf = doc.importNode(idfResponse.getDocumentElement(), true);

        doc.replaceChild(newIdf, doc.getFirstChild());
    }

    public Resource getStyleSheetResource() {
        return styleSheetResource;
    }

    public void setStyleSheetResource(Resource styleSheetResource) {
        this.styleSheetResource = styleSheetResource;
    }

}
