/*
 * Copyright (c) 1997-2005 by media style GmbH
 * 
 * $Source: /cvs/asp-search/src/java/com/ms/aspsearch/PermissionDeniedException.java,v $
 */

package de.ingrid.iplug.dsc.index;

import java.io.File;

import junit.framework.TestCase;
import de.ingrid.iplug.dsc.schema.Construct;
import de.ingrid.iplug.dsc.schema.RecordReaderTest;
import de.ingrid.utils.xml.XMLSerializer;

public class LoadConstructTest extends TestCase {

	public void testLoadConstruct() throws Exception {
		Construct simpleConstruct = RecordReaderTest.getSimpleConstruct();
		XMLSerializer serializer = new XMLSerializer();
		serializer.aliasClass("construct", Construct.class);
		File file = new File("./test" + System.currentTimeMillis());

		// serialisieren...
		serializer.serialize(simpleConstruct, file);

		// load file...
		Construct construct = (Construct) serializer.deSerialize(file);
		assertNotNull(construct);

		// now you can use the construct with reader etc.

	}

}
