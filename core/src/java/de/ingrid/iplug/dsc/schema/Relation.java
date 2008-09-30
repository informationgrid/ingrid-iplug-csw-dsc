/*
 * Copyright (c) 1997-2005 by media style GmbH
 * 
 * $Source: /cvs/asp-search/src/java/com/ms/aspsearch/PermissionDeniedException.java,v $
 */

package de.ingrid.iplug.dsc.schema;

import java.io.Serializable;

import de.ingrid.utils.dsc.Column;
import de.ingrid.utils.dsc.UniqueObject;

/**
 * Container representing a relation in a database sql querly, a relation can be
 * one to one or one to many, we do not have many to many relations since all
 * our hirachically constructs are centralized around a key column
 * 
 * created on 09.08.2005
 * 
 * @author sg
 * @version $Revision: 1.3 $
 */
public class Relation extends UniqueObject implements Serializable {

    private static final long serialVersionUID = Relation.class.getName().hashCode();
    
	public final static int ONE_TO_ONE = 0;

	public final static int ONE_TO_MANY = 1;

	private Column fLeftColumn;

	private Column fRightColumn;


	private Table fRightTable;

	private int fRelationType;

	/**
	 * @param leftColumn
	 * @param rightTable
	 * @param rightColumn
	 * @param relationType
	 */
	public Relation(Column leftColumn, Table rightTable,
			Column rightColumn, int relationType) {
		fLeftColumn = leftColumn;
		fRightTable = rightTable;
		fRightColumn = rightColumn;
		fRelationType = relationType;
	}

	/**
	 * @param leftColumn
	 */
	public void setLeftColumn(Column leftColumn) {
		fLeftColumn = leftColumn;
	}

	/**
	 * @param relationType
	 */
	public void setRelationType(int relationType) {
		fRelationType = relationType;
	}

	/**
	 * @param rightColumn
	 */
	public void setRightColumn(Column rightColumn) {
		fRightColumn = rightColumn;
	}

	/**
	 * @return column
	 */
	public Column getLeftColumn() {
		return fLeftColumn;
	}

	/**
	 * @return relation type
	 */
	public int getRelationType() {
		return fRelationType;
	}

	/**
	 * @return column
	 */
	public Column getRightColumn() {
		return fRightColumn;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return getLeftColumn() + ": " + getRightColumn() + " type: "
				+ getRelationType();
	}

	/**
	 * @return the right table
	 */
	public Table getRightTable() {
		return fRightTable;
	}



}
