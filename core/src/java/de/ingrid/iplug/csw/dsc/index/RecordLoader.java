/*
 * Copyright (c) 1997-2005 by media style GmbH
 * 
 * $Source: /cvs/asp-search/src/java/com/ms/aspsearch/PermissionDeniedException.java,v $
 */

package de.ingrid.iplug.csw.dsc.index;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import de.ingrid.iplug.csw.dsc.schema.Construct;
import de.ingrid.iplug.csw.dsc.schema.RecordReader;
import de.ingrid.iplug.csw.dsc.schema.Util;
import de.ingrid.utils.dsc.Column;
import de.ingrid.utils.dsc.Filter;
import de.ingrid.utils.dsc.Record;

/**
 * Returns the details of a document
 */
public class RecordLoader {

    protected static Log log = LogFactory.getLog(RecordLoader.class);

    private Construct fConstruct;

    private Connection fConnection;

    private String fUrl;

    private String fUser;

    private String fPassword;

    private String fSchema;

    /**
     * Initializes the RecordLoader.
     * @param construct A database contstruct.
     * @param schema A database schema.
     * @param url a database connection url.
     * @param user A database user.
     * @param password The password for the database user.
     * @throws SQLException
     */
    public RecordLoader(Construct construct, String schema, String url, String user, String password)
            throws SQLException {
        this.fUrl = url;
        this.fUser = user;
        this.fPassword = password;
        this.fSchema = schema;

        this.fConnection = createConnection();
        this.fConstruct = construct;
    }

    private Connection createConnection() throws SQLException {
        return DriverManager.getConnection(this.fUrl, this.fUser, this.fPassword);
    }

    /**
     * Returns details to a document.
     * @param document The document which provides the column names.
     * @return The complete data of data entry as record. The record is strcutured as the mapping provides it.
     * @throws Exception
     */
    public Record getDetails(Document document) throws Exception {
        Column key = this.fConstruct.getKey();
        Field field = document.getField(key.getTargetName());
        Construct construct = (Construct) Util.deepClone(this.fConstruct);
        if (field != null) {
            String value = field.stringValue();
            Column column = construct.findColumn(key);
            column.addFilter(new Filter(Filter.EQUALS, value));
        }

        try {
            return new RecordReader(construct, this.fConnection, this.fSchema, this.fUrl, this.fUser, this.fPassword)
                    .nextRecord();
        } catch (Exception e) {
            log.error(e);
            close();
            this.fConnection = createConnection();
            return new RecordReader(construct, this.fConnection, this.fSchema, this.fUrl, this.fUser, this.fPassword)
                    .nextRecord();

        }
    }

    /**
     * Closes a connection if it isn't closed.
     * 
     * @throws Exception
     */
    public void close() throws Exception {
        if (!this.fConnection.isClosed()) {
            this.fConnection.close();
        }
    }

}
