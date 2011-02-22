/**
 * 
 */
package de.ingrid.iplug.csw.dsc.record.mapper;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import de.ingrid.iplug.csw.dsc.cswclient.CSWRecord;
import de.ingrid.iplug.csw.dsc.om.CswCacheSourceRecord;
import de.ingrid.iplug.csw.dsc.om.SourceRecord;
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
        Node csw = doc.importNode(cswRecord.getOriginalResponse(), true);

        body.appendChild(csw);
    }

}
