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
package de.ingrid.iplug.csw.dsc.record.producer;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.ingrid.iplug.csw.dsc.cache.Cache;
import de.ingrid.iplug.csw.dsc.cswclient.CSWFactory;
import de.ingrid.iplug.csw.dsc.cswclient.constants.ElementSetName;
import de.ingrid.iplug.csw.dsc.om.CswCacheSourceRecord;
import de.ingrid.iplug.csw.dsc.om.SourceRecord;
import de.ingrid.utils.ElasticDocument;
import de.ingrid.utils.PlugDescription;

/**
 * This class retrieves a record from a database data source. It retrieves an
 * database id from a lucene document ({@link getRecord}) and creates a
 * {@link DatabaseSourceRecord} containing the database ID that identifies the
 * database record and the open connection to the database.
 * 
 * The database connection is configured via the {@link PlugDescription}.
 * 
 * 
 * @author joachim@wemove.com
 * 
 */
public class CswRecordProducer implements IRecordProducer {

    final private static Log log = LogFactory.getLog(CswRecordProducer.class);

    Cache cache;
    
    CSWFactory factory;

    /*
     * (non-Javadoc)
     * 
     * @see
     * de.ingrid.iplug.dsc.record.IRecordProducer#getRecord(org.apache.lucene
     * .document.Document)
     */
    @Override
    public SourceRecord getRecord(ElasticDocument doc) {
        // TODO make the field configurable
        String field = (String) doc.get("t01_object.obj_id");
        try {
            return new CswCacheSourceRecord(cache.getRecord(field, ElementSetName.FULL));
        } catch (IOException e) {
            log.error("Error reading record '" + field + "' from cache '" + cache.toString() + "'.");
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.ingrid.iplug.dsc.record.IRecordProducer#closeDatasource()
     */
    @Override
    public void closeDatasource() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.ingrid.iplug.dsc.record.IRecordProducer#openDatasource()
     */
    @Override
    public void openDatasource() {
        cache.configure(factory);
    }

    public Cache getCache() {
        return cache;
    }

    public void setCache(Cache cache) {
        this.cache = cache;
    }
    
    public CSWFactory getFactory() {
        return factory;
    }

    public void setFactory(CSWFactory factory) {
        this.factory = factory;
    }
    

}
