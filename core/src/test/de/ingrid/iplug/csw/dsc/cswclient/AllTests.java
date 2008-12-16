/*
 * Copyright (c) 2008 wemove digital solutions. All rights reserved.
 */

package de.ingrid.iplug.csw.dsc.cswclient;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for de.ingrid.iplug.csw.dsc.cswclient");
		//$JUnit-BEGIN$
		suite.addTestSuite(CSWClientFactoryTest.class);
		suite.addTestSuite(CSWClientTestLocal.class);
		//$JUnit-END$
		return suite;
	}

}
