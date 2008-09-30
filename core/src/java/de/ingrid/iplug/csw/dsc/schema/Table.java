/*
 * Copyright (c) 1997-2005 by media style GmbH
 * 
 * $Source: /cvs/asp-search/src/java/com/ms/aspsearch/PermissionDeniedException.java,v $
 */

package de.ingrid.iplug.csw.dsc.schema;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

import de.ingrid.utils.dsc.Column;
import de.ingrid.utils.dsc.UniqueObject;

public class Table extends UniqueObject implements Serializable {
    
    private static final long serialVersionUID = Table.class.getName().hashCode();

	private String fTableName;

	private ArrayList fColumns = new ArrayList();

	private ArrayList fRelations = new ArrayList();

	public Table(String tableName, Column[] columns) {
		this.fTableName = tableName;
		if (columns != null) {
			this.fColumns.addAll(Arrays.asList(columns));
		}

	}

	public Column[] getColumns() {
		return (Column[]) fColumns.toArray(new Column[fColumns.size()]);
	}

	public void setColumns(Column[] columns) {
		if (columns != null) {
			this.fColumns.addAll(Arrays.asList(columns));
		}
	}

	public String getTableName() {
		return fTableName;
	}

	public void setTableName(String tableName) {
		fTableName = tableName;
	}

	public String toString() {
		return getTableName();
	}

	/**
	 * @return Returns the relations.
	 */
	public Relation[] getRelations() {
		return (Relation[]) this.fRelations.toArray(new Relation[fRelations
				.size()]);
	}

	/**
	 * @param relations
	 *            The relations to set.
	 */
	public void setRelations(Relation[] relations) {
		this.fRelations = (ArrayList) Arrays.asList(relations);
	}

	/**
	 * @param relation
	 */
	public void removeRelation(Relation relation) {
		fRelations.remove(relation);
	}

	/**
	 * @param relation
	 */
	public void addRelation(Relation relation) {
		fRelations.add(relation);
	}

	public void addColumn(Column column) {
		fColumns.add(column);
	}

	/**
	 * @param string
	 * @return the column matching the name or null if not found
	 */
	public Column getColumnByName(String string) {
		Column[] columns = getColumns();
		for (int i = 0; i < columns.length; i++) {
			Column column = columns[i];
			if (column.getColumnName().equals(string)) {
				return column;
			}

		}
		return null;
	}

	/**
	 * @param key
	 * @return true in case the given column occures in this table
	 */
	public boolean hasColumn(Column key) {
		if (getTableName().equals(key.getTableName())) {
			if (getColumnByName(key.getColumnName()) != null) {
				return true;
			}
		}
		return false;
	}

	public Relation[] getNRelations() {
		ArrayList arrayList = new ArrayList();
		Relation[] relations = getRelations();
		for (int i = 0; i < relations.length; i++) {
			Relation relation = relations[i];
			if (relation.getRelationType() == Relation.ONE_TO_MANY) {
				arrayList.add(relation);
			}
		}
		return (Relation[]) arrayList.toArray(new Relation[arrayList.size()]);
	}

	public Table[] getRelatedTables() {
		ArrayList arrayList = new ArrayList();
		collectRelatedTables(arrayList, this);
		return (Table[]) arrayList.toArray(new Table[arrayList.size()]);
	}

	private void collectRelatedTables(ArrayList arrayList, Table table) {
		Relation[] relations = table.getRelations();
		for (int i = 0; i < relations.length; i++) {
			Relation relation = relations[i];
			Table rightTable = relation.getRightTable();
			arrayList.add(rightTable);
			collectRelatedTables(arrayList, rightTable);
		}
	}
}
