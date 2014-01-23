/**
 * 
 */
package de.ingrid.iplug.csw.dsc.record;

import java.util.zip.GZIPOutputStream;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.w3c.dom.Node;

import de.ingrid.iplug.csw.dsc.cswclient.CSWRecord;
import de.ingrid.iplug.csw.dsc.om.CswCacheSourceRecord;
import de.ingrid.iplug.csw.dsc.om.SourceRecord;
import de.ingrid.iplug.csw.dsc.record.producer.IRecordProducer;
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

    protected static final Logger log = Logger.getLogger(IdfRecordCreator.class);

    private IRecordProducer recordProducer = null;

    private boolean compressed = false;

    /**
     * Retrieves a record with an IDF document in property "data". The property
     * "compressed" is set to "true" if the IDF document is compressed, "false"
     * if the IDF document is not compressed.
     * 
     * @param idxDoc
     * @return
     * @throws Exception
     */
    public Record getRecord(Document idxDoc) throws Exception {
        try {
            recordProducer.openDatasource();
            SourceRecord sourceRecord = recordProducer.getRecord(idxDoc);
            CSWRecord record = (CSWRecord) sourceRecord.get(CswCacheSourceRecord.CSW_RECORD);

            Node idfDoc = record.getOriginalResponse();

            String data = XMLUtils.toString(idfDoc.getOwnerDocument());
            if (log.isDebugEnabled()) {
                log.debug("Resulting IDF document:\n" + data);
            }
            return IdfTool.createIdfRecord(data, compressed);
        } catch (Exception e) {
            log.error("Error creating IDF document.", e);
            throw e;
        } finally {
            recordProducer.closeDatasource();
        }
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

}
