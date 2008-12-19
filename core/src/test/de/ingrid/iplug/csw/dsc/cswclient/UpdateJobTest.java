/*
 * Copyright (c) 2008 wemove digital solutions. All rights reserved.
 */

package de.ingrid.iplug.csw.dsc.cswclient;

import java.io.File;

import junit.framework.TestCase;
import de.ingrid.iplug.csw.dsc.cache.UpdateJob;
import de.ingrid.iplug.csw.dsc.cswclient.constants.OutputFormat;
import de.ingrid.utils.PlugDescription;
import de.ingrid.utils.xml.XMLSerializer;

public class UpdateJobTest extends TestCase {

    private File descFile = new File("src/conf/plugdescription.xml");
	
	public void testExecute() throws Exception {
		
		// read the PlugDescription
		XMLSerializer serializer = new XMLSerializer();
		serializer.aliasClass(PlugDescription.class.getName(), PlugDescription.class);
		PlugDescription desc = (PlugDescription)serializer.deSerialize(this.descFile);

		CSWClientFactory f = (CSWClientFactory)desc.get("cswFactory");
		f.setQueryTemplate((CSWQuery)desc.get("cswQueryTemplate"));
		CSWQuery q = f.createQuery();

        // check query configuration
		OutputFormat l = OutputFormat.valueOf("TEXT_XML");
		assertTrue("output format is application/xml", l.equals(q.getOutputFormat()));
        
		UpdateJob job = UpdateJob.getInstance();
		job.execute(f, 30, 2000);
	}
}
