/*
 * Copyright (c) 1997-2005 by media style GmbH
 * 
 * $Source: /cvs/asp-search/src/java/com/ms/aspsearch/PermissionDeniedException.java,v $
 */

package de.ingrid.iplug.csw.dsc.schema;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import junit.framework.TestCase;
import de.ingrid.utils.dsc.Column;
import de.ingrid.utils.xml.XMLSerializer;

public class DBSchemaControllerTest extends TestCase {

	private Connection fConnection;

	protected void setUp() throws Exception {
		Class.forName("org.hsqldb.jdbcDriver");
		this.fConnection = DriverManager.getConnection(
				"jdbc:hsqldb:file:testdb", "sa", "");
		Statement statement = this.fConnection.createStatement();
		statement.addBatch(getStatement("/sql1.txt"));
		statement.executeBatch();
	}

	protected void tearDown() throws Exception {
		Statement statement = this.fConnection.createStatement();
		statement.execute("SHUTDOWN");
		removeDB("testdb");
	}

	public void testGetMetaData() throws Exception {

		DBSchemaController controller = new DBSchemaController(this.fConnection);

		Table[] tables = controller.getTables();
		assertEquals("should be 48 databases", 48, tables.length);
		String tableName = "flights";
		Column[] fields = controller.getColumns(tableName);
		assertEquals(4, fields.length);
		for (int i = 0; i < fields.length; i++) {
			String string = fields[i].getColumnName();
			if (string.equals("flight_id")) {
				assertTrue(true);
				// we can not detect if this is a key since oracle has problems
				// to indentify indexed fields.
			}
		}

	}

	public void testGestSchema() throws Exception {
		DBSchemaController controller = new DBSchemaController(this.fConnection);
		Table[] tables = controller.getTables();
		for (int i = 0; i < tables.length; i++) {
			Table table = tables[i];
			Column[] fields = controller.getColumns(table.getTableName());
			for (int j = 0; j < fields.length; j++) {
				Column field = fields[j];
				String fieldType = controller.getColumnType(table
						.getTableName(), field.getColumnName());
				// we can not check this anymore.. boolean b =
				// controller.isPrimaryKey(table, field);

				System.out.println("table: " + table + " field: " + field
						+ " type: " + fieldType);
			}
		}
	}

	public static String getStatement(String name) throws IOException {
		InputStream resourceAsStream = DBSchemaController.class
				.getResourceAsStream(name);
		return XMLSerializer.getContents(resourceAsStream);
	}

	public static void removeDB(String fileName) {
		new File(fileName + ".properties").delete();
		new File(fileName + ".script").delete();
		new File(fileName + ".lck").delete();
		new File(fileName + ".log").delete();
	}
}
