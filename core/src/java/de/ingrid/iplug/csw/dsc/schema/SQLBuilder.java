/*
 * Copyright (c) 1997-2005 by media style GmbH
 * 
 * $Source: /cvs/asp-search/src/java/com/ms/aspsearch/PermissionDeniedException.java,v $
 */

package de.ingrid.iplug.csw.dsc.schema;

import java.util.HashSet;
import java.util.Iterator;

import de.ingrid.utils.dsc.Column;
import de.ingrid.utils.dsc.Filter;

/**
 * Builds sql statements from <code>Construct</code>s.
 * 
 * created on 09.08.2005
 * 
 * @author sg
 * @version $Revision: 1.3 $
 */
public class SQLBuilder {

	/**
	 * we only can use constructs with 1 to 1 relations, so split the relations
	 * before you are generating sql for a construct
	 * 
	 * @param construct
	 * @param schema 
	 * @return sql statement
	 */
	public static String generateSQL(Construct construct, String schema) {

		StringBuffer buffer = new StringBuffer();
		//System.out.println("!!! SCHEMA: " +schema);
		buffer.append("SELECT ");
		Column[] columns = construct.getColumns();
		if (columns.length < 1) {
			// we take just all columns
			buffer.append(" * ");
		}

		HashSet tables = new HashSet();
		for (int i = 0; i < columns.length; i++) {
			Column column = columns[i];
			buffer.append(column);
			buffer.append(" AS \"" + createColumnName(column)+"\"");
			if (columns.length > i + 1) {
				buffer.append(", \n");
			}
			tables.add(column.getTableName());
		}

		// construtc where clause since we need to know the tables as well.
		StringBuffer whereClause = constructWhereClause(construct, tables);

		// from
		buffer.append(" \nFROM ");
		Iterator iterator = tables.iterator();
		while (iterator.hasNext()) {
			if(schema != null) {
				buffer.append(schema+".");
			}
			buffer.append((String) iterator.next());
			if (iterator.hasNext()) {
				buffer.append(", \n");
			}

		}
		// connect where clause
		buffer.append(whereClause);

		buffer.append(" \nORDER BY " + construct.getKey());

		// // Limit Clause
		// buffer.append(" LIMIT "+start+" "+lenght);
//		System.out.println("SQL: "+buffer.toString());
		return buffer.toString();
	}

	/**
	 * @param construct
	 * @param tables
	 * @return the where clause of the statement
	 */
	private static StringBuffer constructWhereClause(Construct construct,
			HashSet tables) {
		StringBuffer whereClause = new StringBuffer();
		Relation[] relations = construct.getRelations();
		Column[] columnsWithFilter = construct.getColumnWithFilter();
		if (relations.length > 0 || columnsWithFilter.length > 0) {

			whereClause.append(" \nWHERE ");

			for (int i = 0; i < relations.length; i++) {
				Relation relation = relations[i];
				if (relation.getRelationType() != Relation.ONE_TO_ONE) {
					throw new IllegalArgumentException(
							"sql can only generated from constructs that has one to one relations, split larger constructs before generating sql before!");
				}
				tables.add(relation.getLeftColumn().getTableName());
				tables.add(relation.getRightColumn().getTableName());

				whereClause.append(relation.getLeftColumn() + " = "
						+ relation.getRightColumn());
				if (relations.length > i + 1 || columnsWithFilter.length >0 ) {
					whereClause.append(" AND \n");
				}
			}
			for (int j = 0; j < columnsWithFilter.length; j++) {
				Column filter = columnsWithFilter[j];
				tables.add(filter.getTableName());
				whereClause.append(buildFilterString(filter));
				if (columnsWithFilter.length > j + 1  ) {
					whereClause.append(" AND \n");
				}

			}

		}
		return whereClause;
	}

	
	private static String buildFilterString(Column column) {
		StringBuffer filterStr = new StringBuffer();
		Filter[] filters = column.getFilters();
		if (filters.length > 0) {
			filterStr.append("( ");
			for (int i = 0; i < filters.length; i++) {
				Filter filter = filters[i];
				filterStr.append(column.toString());
				filterStr.append(" ");
				filterStr.append(filter.getCompareSymbol());
				filterStr.append(" ");
				filterStr.append(column.getQuoteChar());
				filterStr.append(filter.getFilterValue());
				filterStr.append(column.getQuoteChar());
				if (i + 1 < filters.length) {
					if (column.filterIsRequired()) {
						filterStr.append(" AND ");
					} else {
						filterStr.append(" OR ");
					}
				}
			}
			filterStr.append(" )");
		}
		return filterStr.toString();
	}

	/**
	 * @param column
	 * @return a unique name build from table and columnName
	 */
	public static String createColumnName(Column column) {
		return Math.abs((column.getTableName() + column.getColumnName()).hashCode())+"";
	}

}
