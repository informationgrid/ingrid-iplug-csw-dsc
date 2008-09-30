/*
 * Copyright (c) 1997-2005 by media style GmbH
 * 
 * $Source: /cvs/asp-search/src/java/com/ms/aspsearch/PermissionDeniedException.java,v $
 */

package de.ingrid.iplug.csw.dsc.schema;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import de.ingrid.utils.dsc.Column;
import de.ingrid.utils.dsc.Record;

import junit.framework.TestCase;

public class RecordReaderTest extends TestCase {

    private Connection fConnection;

    private Statement fStm;

    private String fFileName;

    protected void setUp() throws Exception {
        fFileName = "testdb" + System.currentTimeMillis();
        Class.forName("org.hsqldb.jdbcDriver");
        fConnection = DriverManager.getConnection("jdbc:hsqldb:file:" + fFileName, "sa", "");

        fStm = this.fConnection.createStatement();
        fStm.addBatch(DBSchemaControllerTest.getStatement("/sql2.txt"));
        fStm.executeBatch();

    }

    public void testGetCustomer() throws Exception {
        Construct construct = getSimpleConstruct();
        RecordReader reader = new RecordReader(construct, this.fConnection, null, "jdbc:hsqldb:file:" + fFileName,
                "sa", "");

        Record record = null;
        int i = 0;
        while ((record = reader.nextRecord()) != null) {
            assertNotNull(record);
            System.out.println(i++ + " : " + record);
        }
        assertEquals(50, i);

    }

    public static Construct getSimpleConstruct() {
        Table item = new Table("item", null);
        Table invoice = new Table("invoice", null);
        Table customer = new Table("CUSTOMER", null);

        item.addColumn(new Column("item", "invoiceid", Column.TEXT, true));
        item.addColumn(new Column("item", "item", Column.TEXT, true));
        item.addColumn(new Column("item", "quantity", Column.TEXT, true));
        item.addColumn(new Column("item", "cost", Column.TEXT, true));

        invoice.addColumn(new Column("invoice", "id", Column.TEXT, true));
        invoice.addColumn(new Column("invoice", "customerid", Column.TEXT, true));
        invoice.addColumn(new Column("invoice", "total", Column.TEXT, true));
        invoice.addColumn(new Column("invoice", "id", Column.TEXT, true));

        customer.addColumn(new Column("CUSTOMER", "id", Column.TEXT, true));
        customer.addColumn(new Column("CUSTOMER", "firstname", Column.TEXT, true));
        customer.addColumn(new Column("CUSTOMER", "lastname", Column.TEXT, true));
        customer.addColumn(new Column("CUSTOMER", "street", Column.TEXT, true));
        customer.addColumn(new Column("CUSTOMER", "city", Column.TEXT, true));

        Relation relation1 = new Relation(invoice.getColumnByName("id"), item, item.getColumnByName("invoiceid"),
                Relation.ONE_TO_MANY);
        invoice.addRelation(relation1);

        Relation relation2 = new Relation(invoice.getColumnByName("customerid"), customer, customer
                .getColumnByName("id"), Relation.ONE_TO_ONE);
        invoice.addRelation(relation2);

        Column key = new Column("invoice", "id", Column.TEXT, false);

        return new Construct(key, invoice, new Table[] { item, invoice, customer });
    }

    protected void tearDown() throws Exception {
        fConnection.commit();
        fConnection.close();
        DBSchemaControllerTest.removeDB(fFileName);
    }

}
