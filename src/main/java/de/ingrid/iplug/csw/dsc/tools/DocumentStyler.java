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
package de.ingrid.iplug.csw.dsc.tools;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;

import org.w3c.dom.Document;

/**
 * @author joachim
 * 
 */
public class DocumentStyler {
    private Transformer transformer;

    public DocumentStyler(Source aStyleSheet) throws Exception {
        // create transformer
        TransformerFactory factory = TransformerFactory.newInstance();
        transformer = factory.newTransformer(aStyleSheet);
    }

    public Document transform(Document aDocument) throws Exception {

        // perform transformation
        DOMSource source = new DOMSource(aDocument);
        DOMResult result = new DOMResult();
        transformer.transform(source, result);

        // return resulting document
        return (Document) result.getNode();
    }
}
