/*
 * **************************************************-
 * ingrid-iplug-csw-dsc:war
 * ==================================================
 * Copyright (C) 2014 - 2020 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.1 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl5
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
package de.ingrid.iplug.csw.dsc.record;

import java.io.File;

import junit.framework.TestCase;
import de.ingrid.iplug.csw.dsc.TestUtil;
import de.ingrid.iplug.csw.dsc.cache.Cache;
import de.ingrid.iplug.csw.dsc.cache.impl.DefaultFileCache;
import de.ingrid.iplug.csw.dsc.cswclient.CSWFactory;
import de.ingrid.iplug.csw.dsc.cswclient.CSWRecord;
import de.ingrid.iplug.csw.dsc.cswclient.constants.ElementSetName;
import de.ingrid.iplug.csw.dsc.cswclient.impl.GenericRecord;
import de.ingrid.iplug.csw.dsc.index.mapper.IdfProducerDocumentMapper;
import de.ingrid.iplug.csw.dsc.record.producer.CswRecordProducer;
import de.ingrid.iplug.csw.dsc.tools.StringUtils;
import de.ingrid.utils.ElasticDocument;
import de.ingrid.utils.idf.IdfTool;
import de.ingrid.utils.xml.IDFNamespaceContext;
import de.ingrid.utils.xml.XMLUtils;
import de.ingrid.utils.xpath.XPathUtils;

public class IdfRecordCreatorTest extends TestCase {

    final private XPathUtils xPathUtils = new XPathUtils( new IDFNamespaceContext() );

    private final String cachePath = "./test_case_cache";
    private Cache cache = null;
    private CSWFactory factory = null;

    @Override
    protected void tearDown() throws Exception {
        // delete cache
        TestUtil.deleteDirectory( new File( cachePath ) );
    }

    public void testGetRecord() throws Exception {

        String[] ids = new String[] { "33462e89-e5ab-11c3-737d-b3a61366d028", "0C12204F-5626-4A2E-94F4-514424F093A1", "486d9622-c29d-44e5-b878-44389740011",
                "77793F43-707A-4346-9A24-9F4E22213F54", "CF902C59-D50B-42F6-ADE4-F3CEC39A3259", "CFA384AB-028F-476B-AC95-EB75CCEFB296" };

        for (String id : ids) {
            this.putRecord( id, ElementSetName.FULL );
        }

        Cache cache = this.setupCache();

        CswRecordProducer cswRecordProducer = new CswRecordProducer();

        cswRecordProducer.setCache( cache );
        cswRecordProducer.setFactory( factory );

        IdfRecordCreator idfRecordCreator = new IdfRecordCreator();
        idfRecordCreator.setRecordProducer( cswRecordProducer );

        ElasticDocument idxDoc = new ElasticDocument();

        idxDoc.put( "t01_object.obj_id", "33462e89-e5ab-11c3-737d-b3a61366d028" );
        idxDoc.put( IdfProducerDocumentMapper.DOCUMENT_FIELD_IDF, XMLUtils.toString(cache.getRecord( "33462e89-e5ab-11c3-737d-b3a61366d028", ElementSetName.FULL ).getOriginalResponse().getOwnerDocument()) );
        org.w3c.dom.Node idfDoc = StringUtils.stringToDocument( IdfTool.getIdfDataFromRecord( idfRecordCreator.getRecord( idxDoc ) ) );
        assertEquals( "IDF Record 33462e89-e5ab-11c3-737d-b3a61366d028 exists.", "33462e89-e5ab-11c3-737d-b3a61366d028",
                xPathUtils.getString( idfDoc, "//gmd:fileIdentifier/gco:CharacterString" ) );

        idxDoc = new ElasticDocument();
        idxDoc.put( "t01_object.obj_id", "486d9622-c29d-44e5-b878-44389740011" );
        idxDoc.put( IdfProducerDocumentMapper.DOCUMENT_FIELD_IDF, XMLUtils.toString(cache.getRecord( "486d9622-c29d-44e5-b878-44389740011", ElementSetName.FULL ).getOriginalResponse().getOwnerDocument()) );
        idfDoc = StringUtils.stringToDocument( IdfTool.getIdfDataFromRecord( idfRecordCreator.getRecord( idxDoc ) ) );
        assertEquals( "IDF Record 486d9622-c29d-44e5-b878-44389740011 exists.", "486d9622-c29d-44e5-b878-44389740011",
                xPathUtils.getString( idfDoc, "//gmd:fileIdentifier/gco:CharacterString" ) );

    }

    private Cache setupCache() {
        if (this.cache == null) {
            factory = new CSWFactory();
            factory.setRecordImpl( "de.ingrid.iplug.csw.dsc.cswclient.impl.GenericRecord" );
            DefaultFileCache cache = new DefaultFileCache();
            cache.configure( factory );
            cache.setCachePath( cachePath );
            this.cache = cache;
        }
        return this.cache;
    }

    private void putRecord(String id, ElementSetName elementSetName) throws Exception {
        Cache cache = this.setupCache();
        CSWRecord record = TestUtil.getRecord( id, elementSetName, new GenericRecord() );
        cache.putRecord( record );
    }

}
