/*
 * Copyright (c) 1997-2005 by media style GmbH
 * 
 * $Source: /cvs/asp-search/src/java/com/ms/aspsearch/PermissionDeniedException.java,v $
 */

package de.ingrid.iplug.csw.dsc.schema;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.ingrid.utils.dsc.Column;
import de.ingrid.utils.dsc.Filter;
import de.ingrid.utils.dsc.Record;

/**
 * A reader that reads records from a database by using the construct as representation of the structure.
 */
public class RecordReader {

    private static Log log = LogFactory.getLog(RecordReader.class);

    private Construct fConstruct;

    private ResultSet fResults;

    private Construct fConstructForKey;

    private Connection fConnection;

    private Statement fStatement;

    private String fSchema;

    private int fConnectionRetriesStatement = 10;

    private int fConnectionRetriesResultSet = 100;

    private String fPassword;

    private String fUser;

    private String fUrl;

    /**
     * @param construct
     * @param connection
     * @param schema
     * @param password
     * @param user
     * @param url
     * @throws Exception
     */
    public RecordReader(Construct construct, Connection connection, String schema, String url, String user,
            String password) throws Exception {
        this.fUrl = url;
        this.fUser = user;
        this.fPassword = password;
        this.fConnection = connection;
        this.fConstruct = construct;
        this.fSchema = schema;
        Table table = (Table) Util.deepClone(this.fConstruct.getRootTable());

        Util.removeNRelations(table);
        this.fConstructForKey = new Construct((Column) Util.deepClone(this.fConstruct.getKey()), table);

        this.fStatement = connection.createStatement();
        String sql = SQLBuilder.generateSQL(this.fConstructForKey, schema);

        this.fStatement.execute(sql);
        this.fResults = this.fStatement.getResultSet();
    }

    /**
     * @return the next available record with its subrrecords or null.
     * @throws Exception
     */
    public Record nextRecord() throws Exception {
        int retryStatement = 100;
        boolean againStatement = true;
        while (againStatement) {
            try {
                if (!this.fResults.next()) {
                    return null;
                }
                againStatement = false;
            } catch (SQLException e) {
                log.error("Connection problem 3. " + retryStatement + " retries left. Next in 60 seconds");
                Thread.sleep(60000);
                retryStatement--;
                if (retryStatement < 1) {
                    if (log.isErrorEnabled()) {
                        log.error("To much retries. Stop indexing job.");
                    }
                    throw e;
                }
                this.fConnection = createNewConnection();
                this.fStatement = this.fConnection.createStatement();
                String sql = SQLBuilder.generateSQL(this.fConstructForKey, this.fSchema);
                this.fStatement.execute(sql);
                this.fResults = this.fStatement.getResultSet();
            }
        }

        Record record = getRecordFromRow(this.fConstructForKey, this.fResults);
        addSubRecord(record, this.fConstruct.getRootTable());

        return record;
    }

    /**
     * Add sub records.
     * 
     * @param record
     * @param table
     * @throws Exception
     */
    private void addSubRecord(Record record, Table table) throws Exception {
        Relation[] relations = table.getRelations();
        for (int j = 0; j < relations.length; j++) {
            Relation relation = relations[j];
            if (relation.getRelationType() == Relation.ONE_TO_MANY) {
                Column leftColumn = relation.getLeftColumn();
                Column rightColumn = relation.getRightColumn();
                Construct constructForKey = Util.build1To1ConstructFromRelation(relation);
                Column column = constructForKey.findColumn(rightColumn);
                column.addFilter(new Filter(Filter.EQUALS, record.getValueForColumn(leftColumn)));

                String sql = SQLBuilder.generateSQL(constructForKey, this.fSchema);
                Statement statement = null;
                int retryResultSet = this.fConnectionRetriesResultSet;
                boolean againResultSet = true;
                while (againResultSet) {
                    try {
                        int retryStatement = this.fConnectionRetriesStatement;
                        boolean againStatement = true;
                        while (againStatement) {
                            try {
                                statement = this.fConnection.createStatement();
                                statement.execute(sql);
                                againStatement = false;
                            } catch (SQLException e) {
                                if (log.isErrorEnabled()) {
                                    log.error("Connection problem 1. " + retryStatement
                                            + " retries left. Next in 60 seconds");
                                    log.error("Exception message: " + e.getMessage());
                                    log.error("Table name: " + table.getTableName());
                                    log.error("SQL: " + sql);
                                }
                                retryStatement--;
                                Thread.sleep(60000);

                                if (retryStatement < 1) {
                                    if (log.isErrorEnabled()) {
                                        log.error("To much retries. Stop indexing job.");
                                    }
                                    throw e;
                                }
                                this.fConnection = createNewConnection();
                            }
                        }

                        ResultSet resultSet = statement.getResultSet();
                        ArrayList subRecords = new ArrayList();
                        while (resultSet.next()) {
                            Record recordFromRow = getRecordFromRow(constructForKey, resultSet);
                            addSubRecord(recordFromRow, relation.getRightTable());
                            subRecords.add(recordFromRow);
                        }
                        againResultSet = false;

                        try {
                            resultSet.close();
                            statement.close();
                        } catch (SQLException e) {
                            if (log.isErrorEnabled()) {
                                log.error("Problem on closing resultSet and statement. (We ignore this.)");
                            }
                        }

                        record.addSubRecords((Record[]) subRecords.toArray(new Record[subRecords.size()]));
                    } catch (SQLException e) {
                        if (log.isErrorEnabled()) {
                            log.error("Connection problem 2. " + retryResultSet + " retries left. Next in 60 seconds");
                        }
                        retryResultSet--;
                        Thread.sleep(60000);

                        if (retryResultSet < 1) {
                            throw e;
                        }
                        this.fConnection = createNewConnection();
                    }
                }
            } else {
                addSubRecord(record, relation.getRightTable());
            }
        }
    }

    private Connection createNewConnection() throws SQLException {
        return DriverManager.getConnection(this.fUrl, this.fUser, this.fPassword);
    }

    private Record getRecordFromRow(Construct construct, ResultSet resultSet) throws SQLException {
        Record record = new Record(Record.DB);
        Column[] columns = construct.getColumns();
        for (int i = 0; i < columns.length; i++) {
            String columnName = SQLBuilder.createColumnName(columns[i]);
            String value = resultSet.getString(columnName);
            record.addColumn(columns[i], value);
        }
        return record;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#finalize()
     */
    protected void finalize() throws Throwable {
        close();
    }

    /**
     * @throws SQLException
     */
    public void close() throws SQLException {
        this.fStatement.close();
        this.fResults.close();
    }
}
