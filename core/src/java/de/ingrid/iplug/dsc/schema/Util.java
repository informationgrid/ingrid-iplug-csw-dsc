/*
 * Copyright (c) 1997-2005 by media style GmbH
 * 
 * $Source: /cvs/asp-search/src/java/com/ms/aspsearch/PermissionDeniedException.java,v $
 */

package de.ingrid.iplug.dsc.schema;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import de.ingrid.utils.dsc.Column;

public class Util {

	/**
	 * @param serializable
	 * @return a deep cloned object
	 * @throws Exception
	 */
	public static Serializable deepClone(Serializable serializable)
			throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		new ObjectOutputStream(baos).writeObject(serializable);
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		return (Serializable) new ObjectInputStream(bais).readObject();
	}

	/**
	 * removes one to n relations from a construct
	 * 
	 * @param table
	 */
	public static void removeNRelations(Table table) {
		Relation[] relations = table.getRelations();
		for (int i = 0; i < relations.length; i++) {
			Relation relation = relations[i];
			if (relation.getRelationType() == Relation.ONE_TO_MANY) {
				table.removeRelation(relation);
			} else {
				removeNRelations(relation.getRightTable());
			}
		}

	}

	public static Construct build1To1ConstructFromRelation(Relation relation) throws Exception {
		Column rightColumn = (Column) Util.deepClone(relation.getRightColumn());
		Table rightTable = (Table) Util.deepClone(relation.getRightTable());
		Util.removeNRelations(rightTable);
		return new Construct(rightColumn, rightTable);
	}
}
