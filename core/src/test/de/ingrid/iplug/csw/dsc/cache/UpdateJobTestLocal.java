/*
 * Copyright (c) 2008 wemove digital solutions. All rights reserved.
 */

package de.ingrid.iplug.csw.dsc.cache;

import java.io.File;

import junit.framework.TestCase;
import de.ingrid.iplug.csw.dsc.cache.Cache;
import de.ingrid.iplug.csw.dsc.cache.UpdateJob;
import de.ingrid.iplug.csw.dsc.cache.impl.DefaultFileCache;
import de.ingrid.iplug.csw.dsc.cswclient.CSWClientFactory;
import de.ingrid.iplug.csw.dsc.cswclient.CSWQuery;
import de.ingrid.iplug.csw.dsc.cswclient.CSWRecord;
import de.ingrid.iplug.csw.dsc.cswclient.constants.ElementSetName;
import de.ingrid.iplug.csw.dsc.cswclient.constants.OutputFormat;
import de.ingrid.utils.PlugDescription;
import de.ingrid.utils.xml.XMLSerializer;

public class UpdateJobTestLocal extends TestCase {

    private File descFile = new File("src/conf/plugdescription.xml");
	
	public void testExecute() throws Exception {
		
		// read the PlugDescription
		XMLSerializer serializer = new XMLSerializer();
		serializer.aliasClass(PlugDescription.class.getName(), PlugDescription.class);
		PlugDescription desc = (PlugDescription)serializer.deSerialize(this.descFile);

		// get instances from plugdescription
		CSWClientFactory factory = (CSWClientFactory)desc.get("cswFactory");
		factory.setQueryTemplate((CSWQuery)desc.get("cswQueryTemplate"));
		Cache cache = (DefaultFileCache)desc.get("cswCache");
		
        // check query configuration
		CSWQuery q = factory.createQuery();
		OutputFormat l = OutputFormat.valueOf("TEXT_XML");
		assertTrue("output format is application/xml", l.equals(q.getOutputFormat()));

		// run the update job for all elementset names
		UpdateJob job = new UpdateJob();
		job.configure(factory, cache, (String)desc.get("cswHarvestFilter"));
		
		ElementSetName[] names = ElementSetName.values();
		for (int i=0; i<names.length; i++)
			job.execute(names[i], 20, 2000);
		
		// check for a cached record
		String id = "1AFDCB03-3818-40F1-9560-9FB082956357";
		CSWRecord recordBrief = cache.getRecord(id, ElementSetName.BRIEF, factory.createRecord());
		assertTrue("record "+id+" was cached", recordBrief.getId().equals(id));
		CSWRecord recordSummary = cache.getRecord(id, ElementSetName.SUMMARY, factory.createRecord());
		assertTrue("record "+id+" was cached", recordSummary.getId().equals(id));
		CSWRecord recordFull = cache.getRecord(id, ElementSetName.FULL, factory.createRecord());
		assertTrue("record "+id+" was cached", recordFull.getId().equals(id));
	}
}
