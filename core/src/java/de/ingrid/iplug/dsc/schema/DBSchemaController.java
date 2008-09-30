/*
 * Copyright (c) 1997-2005 by media style GmbH
 * 
 * $Source: /cvs/asp-search/src/java/com/ms/aspsearch/PermissionDeniedException.java,v $
 */

package de.ingrid.iplug.dsc.schema;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import de.ingrid.utils.dsc.Column;

/**
 * Get database metadata via jdbc connection.
 */
public class DBSchemaController {

    private DatabaseMetaData fMetaData;

    /**
     * Initialises the database schema controller.
     * 
     * @param connection
     *            Connection to database.
     * @throws SQLException
     */
    public DBSchemaController(Connection connection) throws SQLException {
        this.fMetaData = connection.getMetaData();

    }

    /**
     * Return all known schemas of database
     * 
     * @return All known schemas of database.
     * @throws SQLException
     */
    public String[] getSchemas() throws SQLException {
        ArrayList schemas = new ArrayList();
        ResultSet schemasOfDB = this.fMetaData.getSchemas();
        while (schemasOfDB.next()) {
            String schemaName = schemasOfDB.getString(1);
            schemas.add(schemaName);
        }
        return (String[]) schemas.toArray(new String[schemas.size()]);
    }

    /**
     * Return all known tables of a given schema.
     * 
     * @param schemaName
     *            Schema name.
     * @return All known tables of a given schema.
     * @throws SQLException
     */
    public Table[] getTablesFromSchema(String schemaName) throws SQLException {
        ArrayList tables = new ArrayList();
        ResultSet resultSet = this.fMetaData.getTables(null, schemaName, "%", null);
        while (resultSet.next()) {
            String tableName = resultSet.getString(3);
            System.out.println("...loading " + schemaName + " : " + tableName);
            Column[] columns = getColumns(tableName, schemaName);
            Table table = new Table(tableName, columns);
            tables.add(table);
        }
        resultSet.close();
        return (Table[]) tables.toArray(new Table[tables.size()]);
    }

    /**
     * Return all known tables of this database.
     * 
     * @return All known tables of this database.
     * @throws SQLException
     */
    public Table[] getTables() throws SQLException {
        // TODO may we should use VIEWS as well.
        // String[] tableTypes = { "TABLE" };
        ResultSet resultSet = this.fMetaData.getTables(null, null, null, null);
        ArrayList tables = new ArrayList();
        while (resultSet.next()) {
            String tableName = resultSet.getString("TABLE_NAME");
            Column[] columns = getColumns(tableName);
            Table table = new Table(tableName, columns);
            tables.add(table);
        }
        resultSet.close();
        return (Table[]) tables.toArray(new Table[tables.size()]);
    }

    /**
     * Returns all columns to a table.
     * 
     * @param table
     *            A table were to get the colmuns from.
     * @return An array of columns.
     * @throws SQLException
     */
    public Column[] getColumns(String table) throws SQLException {
        return getColumns(table, "%");
    }

    /**
     * Returns all columns to a schema pattern.
     * 
     * @param table
     *            A table were to get the colmuns from.
     * @param schemaPattern
     *            A schema pattern.
     * @return An array of columns.
     * @throws SQLException
     */
    public Column[] getColumns(String table, String schemaPattern) throws SQLException {
        ArrayList arrayList = new ArrayList();
        ResultSet columns = this.fMetaData.getColumns(null, schemaPattern, table, "%");

        while (columns.next()) {
            String columnName = columns.getString("COLUMN_NAME");
            Column column = new Column(table, columnName, Column.TEXT, false);
            arrayList.add(column);

        }
        columns.close();
        return (Column[]) arrayList.toArray(new Column[arrayList.size()]);

    }

    /**
     * Return the type to a column and a table.
     * 
     * @param table
     *            A table were the colmun is from.
     * @param column
     *            The column name were to get the type from.
     * @return The type of a column or null in case we do not find the column name.
     * @throws SQLException
     */
    public String getColumnType(String table, String column) throws SQLException {
        ResultSet columns = this.fMetaData.getColumns(null, "%", table, "%");
        while (columns.next()) {
            String columnName = columns.getString("COLUMN_NAME");
            if (columnName.equals(column)) {

                String type = columns.getString("TYPE_NAME");
                columns.close();
                return type;
            }
        }
        columns.close();
        return null;
    }
}
