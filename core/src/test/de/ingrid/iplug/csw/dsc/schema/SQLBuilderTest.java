/*
 * Copyright (c) 1997-2005 by media style GmbH
 * 
 * $Source: /cvs/asp-search/src/java/com/ms/aspsearch/PermissionDeniedException.java,v $
 */

package de.ingrid.iplug.csw.dsc.schema;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import de.ingrid.utils.dsc.Column;

import junit.framework.TestCase;

public class SQLBuilderTest extends TestCase {

	public void testGenerateSql() throws Exception {
		String fileName = "testdb" + System.currentTimeMillis();
		Class.forName("org.hsqldb.jdbcDriver");
		Connection connection = DriverManager.getConnection("jdbc:hsqldb:file:"
				+ fileName, "sa", "");

		Statement stm = connection.createStatement();
		stm.addBatch(DBSchemaControllerTest.getStatement("/sql2.txt"));
		stm.executeBatch();
		Construct construct = RecordReaderTest.getSimpleConstruct();
		Table table = construct.getRootTable();

		Util.removeNRelations(table);
		construct = new Construct(construct.getKey(), table);
		String sql = SQLBuilder.generateSQL(construct, null);
		System.out.println(sql);

		stm.execute(sql);
		ResultSet resultSet = stm.getResultSet();

		Column[] columns = construct.getColumnsToIndex();

		while (resultSet.next()) {
			String row = "";
			for (int i = 0; i < columns.length; i++) {
				String name = SQLBuilder.createColumnName(columns[i]);
				row += name + ": ";
				row += resultSet.getString(name) + " ";
			}
			System.out.println(row);
		}

		// int count = construct.getColumns().length;
		// while (resultSet.next()) {
		// String row = "";
		// for (int i = 1; i < count; i++) {
		// row += resultSet.getString(i) + " ";
		// }
		// System.out.println(row);
		//
		// }

		resultSet.close();
		stm.execute("SHUTDOWN");
		stm.close();
		connection.commit();
		connection.close();
		DBSchemaControllerTest.removeDB(fileName);
	}
}
