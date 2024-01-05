/*
 * **************************************************-
 * ingrid-iplug-csw-dsc:war
 * ==================================================
 * Copyright (C) 2014 - 2024 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.2 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
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
import de.ingrid.utils.xpath.XPathUtils;

/**
 * Creates an IDF Document out of a CSW Record (SourceRecord) !
 * 
 * @author joachim@wemove.com
 * 
 */
public class CswIdfMapper implements IIdfMapper {

    final private XPathUtils xPathUtils = new XPathUtils(new IDFNamespaceContext());

    protected static final Logger log = Logger.getLogger(CswIdfMapper.class);

    private Resource styleSheetResource;

    @Override
    public void map(SourceRecord record, Document doc) throws Exception {

        if (!(record instanceof CswCacheSourceRecord)) {
            log.error("Source Record is not a CswCacheSourceRecord!");
            throw new IllegalArgumentException("Source Record is not a CswCacheSourceRecord!");
        }

        CSWRecord cswRecord = (CSWRecord) record.get(CswCacheSourceRecord.CSW_RECORD);

        Node body = xPathUtils.getNode(doc, "/idf:html/idf:body");
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
