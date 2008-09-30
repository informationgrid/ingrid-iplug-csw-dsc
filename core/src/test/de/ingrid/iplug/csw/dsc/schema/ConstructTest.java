package de.ingrid.iplug.csw.dsc.schema;

import de.ingrid.utils.dsc.Column;
import junit.framework.TestCase;

public class ConstructTest extends TestCase {

	public void testGetTables() throws Exception {
		Column col1 = new Column("A", "a", "bla", false);
		Column col2 = new Column("b", "a", "bla", false);

		Column[] columns = new Column[] { col1, col2 };
		Table table1 = new Table("tets1", columns);
		Table table2 = new Table("tets2", columns);
		Table table3 = new Table("tets2", columns);

		table1.addRelation(new Relation(col1, table2, col2,Relation.ONE_TO_ONE));
		table2.addRelation(new Relation(col1, table3, col2,Relation.ONE_TO_ONE));
		Construct construct = new Construct(col1, table1);
		assertEquals(3, construct.getTables().length);
	}
}
