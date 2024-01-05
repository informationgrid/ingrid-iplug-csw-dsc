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
package de.ingrid.iplug.csw.dsc.record;

import java.util.List;
import java.util.zip.GZIPOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;

import de.ingrid.iplug.csw.dsc.index.mapper.IdfProducerDocumentMapper;
import de.ingrid.iplug.csw.dsc.om.SourceRecord;
import de.ingrid.iplug.csw.dsc.record.mapper.IIdfMapper;
import de.ingrid.iplug.csw.dsc.record.producer.IRecordProducer;
import de.ingrid.utils.ElasticDocument;
import de.ingrid.utils.dsc.Record;
import de.ingrid.utils.idf.IdfTool;
import de.ingrid.utils.xml.XMLUtils;

/**
 * This class manages to get a {@link Record} from a data source based on data
 * from a lucene document.
 * <p/>
 * The Class can be configured with a data source specific record producer
 * implementing the {@link IRecordProducer} interface.
 * <p/>
 * The IDF data can optionally be compressed using a {@link GZIPOutputStream} by
 * setting the property {@link compressed} to true.
 * 
 * @author joachim@wemove.com
 * 
 */
public class IdfRecordCreator {

    protected static final Logger log = Logger.getLogger( IdfRecordCreator.class );

    private IRecordProducer recordProducer = null;

    private boolean compressed = false;

    private List<IIdfMapper> record2IdfMapperList = null;

    /**
     * Retrieves a record with an IDF document in property "data". The property
     * "compressed" is set to "true" if the IDF document is compressed, "false"
     * if the IDF document is not compressed.
     * 
     * @param luceneDoc
     * @param record
     *            The SourceRecord
     * @return
     * @throws Exception
     */
    public Record getRecord(ElasticDocument luceneDoc, SourceRecord record) throws Exception {

        String data;

        if (luceneDoc.containsKey( IdfProducerDocumentMapper.DOCUMENT_FIELD_IDF ) && luceneDoc.get( IdfProducerDocumentMapper.DOCUMENT_FIELD_IDF ) != null) {
            if (log.isDebugEnabled()) {
                log.debug( "Use content of index field 'idf'." );
            }
            data = (String) luceneDoc.get( IdfProducerDocumentMapper.DOCUMENT_FIELD_IDF );
        } else {
            try {

                if (record == null) {
                    throw new RuntimeException( "IDF could neither be obtained from elastic document field 'idf' nor from the source record." );
                }

                SourceRecord sourceRecord = record;

                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder docBuilder = dbf.newDocumentBuilder();
                org.w3c.dom.Document idfDoc = docBuilder.newDocument();
                for (IIdfMapper record2IdfMapper : this.record2IdfMapperList) {
                    long start = 0;
                    if (log.isDebugEnabled()) {
                        start = System.currentTimeMillis();
                    }
                    record2IdfMapper.map( sourceRecord, idfDoc );
                    if (log.isDebugEnabled()) {
                        log.debug( "Mapping of source record with " + record2IdfMapper + " took: " + (System.currentTimeMillis() - start) + " ms." );
                    }
                }
                data = XMLUtils.toString( idfDoc );
            } catch (Exception e) {
                log.error( "Error creating IDF document.", e );
                throw e;
            } finally {
                this.recordProducer.closeDatasource();
            }
        }
        if (log.isDebugEnabled()) {
            log.debug( "Resulting IDF document:\n" + data );
        }
        return IdfTool.createIdfRecord( data, this.compressed );
    }

    public Record getRecord(ElasticDocument luceneDoc) throws Exception {
        return this.getRecord( luceneDoc, null );
    }

    public IRecordProducer getRecordProducer() {
        return recordProducer;
    }

    public void setRecordProducer(IRecordProducer recordProducer) {
        this.recordProducer = recordProducer;
    }

    public boolean isCompressed() {
        return compressed;
    }

    public void setCompressed(boolean compressed) {
        this.compressed = compressed;
    }

    public List<IIdfMapper> getRecord2IdfMapperList() {
        return record2IdfMapperList;
    }

    public void setRecord2IdfMapperList(List<IIdfMapper> record2IdfMapperList) {
        this.record2IdfMapperList = record2IdfMapperList;
    }

}
