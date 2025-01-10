/*
 * **************************************************-
 * ingrid-iplug-csw-dsc:war
 * ==================================================
 * Copyright (C) 2014 - 2025 wemove digital solutions GmbH
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
/*
 * Copyright (c) 2008 wemove digital solutions. All rights reserved.
 */

package de.ingrid.iplug.csw.dsc.tools;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.axiom.soap.SOAPMessage;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

public class StringUtils {

    public static String join(Object[] parts, String separator) {
        StringBuilder str = new StringBuilder();
        for (Object part : parts) {
            str.append(part).append(separator);
        }
        if (str.length() > 0)
            return str.substring(0, str.length() - separator.length());

        return str.toString();
    }

    public static String nodeToString(Node node) {
        try {
            Source source = new DOMSource(node);
            StringWriter stringWriter = new StringWriter();
            Result result = new StreamResult(stringWriter);
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();
            // just set this to get literally equal results
            transformer.setOutputProperty(OutputKeys.STANDALONE, "no");
            transformer.transform(source, result);
            return stringWriter.getBuffer().toString();
        } catch (Exception e) {
            return e.getMessage();
        }
    }
    
    public static String soapMessageTostring(SOAPMessage message) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            message.serialize(out);
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
        return out.toString();
    }

    public static Document stringToDocument(String string) throws Exception {
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setNamespaceAware(true);
        DocumentBuilder builder = domFactory.newDocumentBuilder();
        InputSource inStream = new InputSource();
        inStream.setCharacterStream(new StringReader(string));
        return builder.parse(inStream);
    }

    
    
    public static String generateUuid() {
        UUID uuid = UUID.randomUUID();
        StringBuilder idcUuid = new StringBuilder(uuid.toString().toUpperCase());
        while (idcUuid.length() < 36) {
            idcUuid.append("0");
        }
        return idcUuid.toString();
    }

}
