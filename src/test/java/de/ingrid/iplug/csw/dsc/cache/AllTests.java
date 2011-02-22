/*
 * Copyright (c) 2008 wemove digital solutions. All rights reserved.
 */

package de.ingrid.iplug.csw.dsc.cache;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for de.ingrid.iplug.csw.dsc.cache");
		//$JUnit-BEGIN$
		suite.addTestSuite(CacheTest.class);
		suite.addTestSuite(UpdateJobTestLocal.class);
		//$JUnit-END$
		return suite;
	}

}
