/*
 * **************************************************-
 * ingrid-iplug-csw-dsc:war
 * ==================================================
 * Copyright (C) 2014 - 2023 wemove digital solutions GmbH
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
/**
 * 
 */
package de.ingrid.iplug.csw.dsc.index.producer;

import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.ingrid.iplug.csw.dsc.cache.Cache;
import de.ingrid.iplug.csw.dsc.cswclient.constants.ElementSetName;
import de.ingrid.iplug.csw.dsc.om.CswCacheSourceRecord;
import de.ingrid.iplug.csw.dsc.om.SourceRecord;

/**
 * Takes care of selecting all source record Ids from a database. The SQL
 * statement is configurable via Spring.
 * 
 * The database connection is configured via the PlugDescription.
 * 
 * 
 * @author joachim@wemove.com
 * 
 */
public class CswRecordSetProducer implements ICswCacheRecordSetProducer {

    Cache cache;

    Iterator<String> recordIdIterator = null;

    final private static Log log = LogFactory
            .getLog(CswRecordSetProducer.class);

    public CswRecordSetProducer() {
        log.info("CswRecordSetProducer started.");
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.ingrid.iplug.dsc.index.IRecordProducer#hasNext()
     */
    @Override
    public boolean hasNext() {
        try {
            if (recordIdIterator == null) {
                recordIdIterator = cache.getCachedRecordIds().iterator();
            }
            if (recordIdIterator.hasNext()) {
                return true;
            }
        } catch (Exception e) {
            log.error("Error obtaining record from cache:" + cache, e);
        }
        recordIdIterator = null;
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.ingrid.iplug.dsc.index.IRecordProducer#next()
     */
    @Override
    public SourceRecord next() {
        String recordId = null;
        try {
            recordId = recordIdIterator.next();
            return new CswCacheSourceRecord(cache.getRecord(recordId,
                    ElementSetName.FULL));
        } catch (Exception e) {
            log.error("Error reading record '" + recordId + "' from cache '"
                    + cache + "' -> WE RETURN NULL !");
        }
        return null;
    }

    public void setCache(Cache cache) {
        this.cache = cache;
    }
    
}
