/*
 * Copyright (c) 1997-2005 by media style GmbH
 * 
 * $Source: /cvs/asp-search/src/java/com/ms/aspsearch/PermissionDeniedException.java,v $
 */

package de.ingrid.iplug.dsc.index;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import org.apache.lucene.document.Document;

import junit.framework.TestCase;
import de.ingrid.iplug.dsc.schema.DBSchemaControllerTest;
import de.ingrid.iplug.dsc.schema.RecordReaderTest;

public class RecordLoaderTest extends TestCase {

    private Connection fConnection;

    protected void setUp() throws Exception {
        Class.forName("org.hsqldb.jdbcDriver");
        this.fConnection = DriverManager.getConnection("jdbc:hsqldb:file:testdb", "sa", "");
        Statement statement = this.fConnection.createStatement();
        statement.addBatch(DBSchemaControllerTest.getStatement("/sql2.txt"));
        statement.executeBatch();
    }

    protected void tearDown() throws Exception {
        Statement statement = this.fConnection.createStatement();
        statement.execute("SHUTDOWN");
        DBSchemaControllerTest.removeDB("testdb");
    }

    public void testDetailer() throws Exception {
        RecordLoader detailer = new RecordLoader(RecordReaderTest.getSimpleConstruct(), null,  "jdbc:hsqldb:file:testdb",
                "sa", "");
        Document document = new Document();
        detailer.getDetails(document);
        assertNotNull(document);
    }
}
