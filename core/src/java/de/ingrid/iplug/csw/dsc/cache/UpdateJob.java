/*
 * Copyright (c) 2008 wemove digital solutions. All rights reserved.
 */

package de.ingrid.iplug.csw.dsc.cache;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.ingrid.iplug.csw.dsc.ConfigurationKeys;
import de.ingrid.iplug.csw.dsc.cswclient.CSWFactory;
import de.ingrid.iplug.csw.dsc.tools.SimpleSpringBeanFactory;
import de.ingrid.utils.PlugDescription;

/**
 * The update job.
 * @author ingo herwig <ingo@wemove.com>
 */
public class UpdateJob {

	final protected static Log log = LogFactory.getLog(UpdateJob.class);
	
	private CSWFactory factory;
	private Cache cache;
	private Set<String> filterStrSet;
	private UpdateStrategy updateStrategy;

	/**
	 * Constructor
	 * @param factory The CSWFactory instance
	 * @param cache The Cache instance (UpdateJob assumes that the Cache is an transaction already)
	 */
	@SuppressWarnings({"unchecked"})
	public UpdateJob(CSWFactory factory, Cache cache) {

		// get filter set from configuration 
		Set<String> filterStrSet = (Set<String>)SimpleSpringBeanFactory.INSTANCE.getBean(
				ConfigurationKeys.CSW_HARVEST_FILTER, Set.class);

		// get strategy set from configuration 
		PlugDescription plugDescription = SimpleSpringBeanFactory.INSTANCE.getBean(ConfigurationKeys.PLUGDESCRIPTION, 
				PlugDescription.class);
		Map strategies = SimpleSpringBeanFactory.INSTANCE.getBean(ConfigurationKeys.CSW_UPDATE_STRATEGIES, Map.class);
		String updateStrategy = plugDescription.getString("updateStrategy");
		UpdateStrategy strategy = SimpleSpringBeanFactory.INSTANCE.getBean((String)strategies.get(updateStrategy), 
				UpdateStrategy.class);

		this.factory = factory;
		this.cache = cache;
		this.filterStrSet = filterStrSet;
		this.updateStrategy = strategy;
	}
	
	/**
	 * Execute the update job.
	 * @param recordsPerCall The maximum number of records to fetch with each server call
	 * @param requestPause The time between two requests in milliseconds
	 * @throws Exception
	 */
	public void execute(int recordsPerCall, int requestPause) throws Exception {
		Date start = new Date();

		// get cached record ids (for later removal of records that do not exist anymore)
		Set<String> cachedRecordIds = this.cache.getCachedRecordIds();
		
		// delegate execution to the strategy
		List<String> allRecordIds = updateStrategy.execute(this.factory, this.cache, this.filterStrSet, 
				recordsPerCall, requestPause);

		// remove deprecated records
		for (String cachedRecordId : cachedRecordIds) {
			if (!allRecordIds.contains(cachedRecordId))
				this.cache.removeRecord(cachedRecordId);
		}

		// duplicates are filtered out automatically by the cache, so there is no need for action here
		int duplicates = allRecordIds.size() - new HashSet<String>(allRecordIds).size();

		// summary
		Date end = new Date();
		long diff = end.getTime()-start.getTime();
		log.info("Fetched "+allRecordIds.size()+" records of "+allRecordIds.size()+". Duplicates: "+duplicates);
		log.info("Job executed in "+diff+" milliseconds.");
	}
}
