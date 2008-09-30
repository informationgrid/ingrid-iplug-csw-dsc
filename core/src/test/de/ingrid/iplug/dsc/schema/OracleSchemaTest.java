/*
 * Copyright (c) 1997-2005 by media style GmbH
 * 
 * $Source: /cvs/asp-search/src/java/com/ms/aspsearch/PermissionDeniedException.java,v $
 */

package de.ingrid.iplug.dsc.schema;

import java.sql.Connection;
import java.sql.DriverManager;

import de.ingrid.utils.dsc.Column;

import junit.framework.TestCase;

public class OracleSchemaTest extends TestCase {
	private Connection fConnection;

	protected void setUp() throws Exception {
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
            // set timeout to 10 seconds to reduce the time for build waiting
            DriverManager.setLoginTimeout(10);
			this.fConnection = DriverManager.getConnection(
					"jdbc:oracle:thin:@192.168.200.38:1521:oradb", "scott",
					"tiger");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("this fails since no orcale is available");
		}
		// Statement statement = this.fConnection.createStatement();
	}

	public void testGetTables() throws Exception {
		try {

			DBSchemaController controller = new DBSchemaController(fConnection);
			Table[] tables = controller.getTables();
			assertNotNull(tables);
			for (int i = 0; i < tables.length; i++) {
				Table table = tables[i];
				System.out.println("table is: " + table);

				Column[] columns = controller.getColumns(table.getTableName());
				for (int j = 0; j < columns.length; j++) {
					Column column = columns[j];
					System.out.println("column: " + column);
					String columnType = controller.getColumnType(table
							.getTableName(), column.getColumnName());
					System.out.println("columnType: " + columnType);

				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out
					.println("this fails since there is no oracle connection");
		}
	}
}
