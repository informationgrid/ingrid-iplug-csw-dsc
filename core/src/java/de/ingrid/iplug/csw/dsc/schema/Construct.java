/*
 * Copyright (c) 1997-2005 by media style GmbH
 * 
 * $Source: /cvs/asp-search/src/java/com/ms/aspsearch/PermissionDeniedException.java,v $
 */

package de.ingrid.iplug.csw.dsc.schema;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.ingrid.utils.dsc.Column;

/**
 * Object to represent a hirachically mapping of databases structures.
 */
public class Construct implements Serializable {

    private static final long serialVersionUID = Construct.class.getName().hashCode();

    private Column fKey;

    private List fTables;

    private Table fRootTable;

    /**
     * Default constructor for de-/serialization.
     */
    public Construct() {
        // for deserialization
    }

    /**
     * Initializes a Construct with a column and a table.
     * @param key A column.
     * @param rootTable A table.
     */
    public Construct(Column key, Table rootTable) {
        this.fKey = key;
        this.fRootTable = rootTable;
        this.fTables = new ArrayList();
    }

    /**
     * Initializes a Construct with a column, a table and an array of tables.
     * @param key A column.
     * @param rootTable A table.
     * @param tables An array of tables.
     */
    public Construct(Column key, Table rootTable, Table[] tables) {
        this.fKey = key;
        this.fRootTable = rootTable;
        this.fTables = new ArrayList();
        if (tables != null) {
            this.fTables.addAll(Arrays.asList(tables));
        }
    }

    /**
     * Returns all tables of this construct also the tables that are may part of relations.
     * @return All tables of this construct also the tables that are may part of relations.
     */
    public Table[] getTables() {
        ArrayList list = new ArrayList();
        Table rootTable = getRootTable();
        list.add(rootTable);
        Table[] relatedTables = rootTable.getRelatedTables();
        for (int i = 0; i < relatedTables.length; i++) {
            Table table = relatedTables[i];
            if (!list.contains(table)) {
                list.add(table);
            }
        }

        int count = this.fTables.size();
        for (int i = 0; i < count; i++) {
            Table table = (Table) this.fTables.get(i);
            if (!list.contains(table)) {
                list.add(table);
            }
            Table[] tables = table.getRelatedTables();
            for (int j = 0; j < tables.length; j++) {
                Table relatedTable = tables[j];
                if (!list.contains(relatedTable)) {
                    list.add(relatedTable);
                }
            }

        }
        return (Table[]) list.toArray(new Table[list.size()]);
    }

    /**
     * Sets an array of tables.
     * @param tables
     *            The tables to set.
     */
    public void setTables(Table[] tables) {
        this.fTables = (ArrayList) Arrays.asList(tables);
    }

    /**
     * Adds a table.
     * @param table The table to add.
     */
    public void addTable(Table table) {
        this.fTables.add(table);
    }

    /**
     * Return the key column.
     * @return Returns the key.
     */
    public Column getKey() {
        return this.fKey;
    }

    /**
     * Sets the key column.
     * @param key
     *            The key to set.
     */
    public void setKey(Column key) {
        this.fKey = key;
    }

    /**
     * Returns all columns also of tables that are may part of relations.
     * @return All columns also of tables that are may part of relations.
     */
    public Column[] getColumns() {
        ArrayList arrayList = new ArrayList();
        Table[] tables = getTables();
        for (int i = 0; i < tables.length; i++) {
            Table table = tables[i];
            Column[] columns = table.getColumns();
            if (columns != null) {
                arrayList.addAll(Arrays.asList(columns));
            }
        }
        return (Column[]) arrayList.toArray(new Column[arrayList.size()]);

    }

    /**
     * Returns the relations to the tables.
     * @return relations The relations to the tables.
     */
    public Relation[] getRelations() {
        ArrayList arrayList = new ArrayList();
        Table[] tables = getTables();
        for (int i = 0; i < tables.length; i++) {
            Table table = tables[i];
            collectRelations(arrayList, table);
        }
        return (Relation[]) arrayList.toArray(new Relation[arrayList.size()]);
    }

    private void collectRelations(ArrayList arrayList, Table table) {
        Relation[] relations = table.getRelations();
        if (relations != null) {

            for (int i = 0; i < relations.length; i++) {
                Relation relation = relations[i];

                if (!arrayList.contains(relation)) {
                    arrayList.add(relation);
                }

                collectRelations(arrayList, relation.getRightTable());
            }
        }
    }

    /**
     * Returns all columns that have filters defined.
     * @return All columns that have filters defined.
     */
    public Column[] getColumnWithFilter() {
        ArrayList arrayList = new ArrayList();
        Table[] tables = getTables();
        for (int i = 0; i < tables.length; i++) {
            Table table = tables[i];
            Column[] columns = table.getColumns();
            if (columns != null) {
                for (int j = 0; j < columns.length; j++) {
                    Column column = columns[j];
                    if (column.getFilters().length > 0) {
                        arrayList.add(column);
                    }
                }

            }
        }
        return (Column[]) arrayList.toArray(new Column[arrayList.size()]);
    }

    /**
     * Removes a table from the list of tables.
     * @param table The table to remove.
     */
    public void removeTable(Table table) {
        this.fTables.remove(table);
    }

    /**
     * Returns all columns that should be added to the index.
     * @return All columns that should be added to the index.
     */
    public Column[] getColumnsToIndex() {
        ArrayList arrayList = new ArrayList();
        Table[] tables = getTables();
        for (int i = 0; i < tables.length; i++) {
            Table table = tables[i];
            Column[] columns = table.getColumns();
            if (columns != null) {
                for (int j = 0; j < columns.length; j++) {
                    Column column = columns[j];
                    if (column.toIndex()) {
                        arrayList.add(column);
                    }
                }

            }
        }
        return (Column[]) arrayList.toArray(new Column[arrayList.size()]);
    }

    /**
     * We may need to find a column by its name since the column object itself is a clone.
     * @param rightColumn The column to find.
     * @return The column as a clone.
     */
    public Column findColumn(Column rightColumn) {
        Column[] columns = getColumns();
        for (int i = 0; i < columns.length; i++) {
            Column column = columns[i];
            if (column.toString().equals(rightColumn.toString())) {
                return column;
            }
        }
        return null;
    }

    /**
     * Returns the root table the mother of all relations
     * @return The root table the mother of all relations.
     */
    public Table getRootTable() {
        return this.fRootTable;
    }

    /**
     * Sets a new root table.
     * @param table The new root table.
     */
    public void setRootTable(Table table) {
        this.fRootTable = table;
    }
}
