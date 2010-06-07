/*
 * Copyright (c) 2008 wemove digital solutions. All rights reserved.
 */

package de.ingrid.iplug.csw.dsc.index;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for de.ingrid.iplug.csw.dsc.index");
		//$JUnit-BEGIN$
		suite.addTestSuite(CaseTestLocal.class);
		suite.addTestSuite(DSCSearcherTest.class);
		suite.addTestSuite(IndexesTestLocal.class);
		//$JUnit-END$
		return suite;
	}

}
