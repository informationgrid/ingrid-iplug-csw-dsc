package de.ingrid.iplug.csw.dsc.index;

import java.io.File;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;

import de.ingrid.iplug.PlugServer;
import de.ingrid.iplug.csw.dsc.ConfigurationKeys;
import de.ingrid.iplug.csw.dsc.cache.Cache;
import de.ingrid.iplug.csw.dsc.cswclient.CSWFactory;
import de.ingrid.iplug.csw.dsc.mapping.DocumentMapper;
import de.ingrid.utils.PlugDescription;

public class IndexingJob implements StatefulJob {

	private static Log LOGGER = LogFactory.getLog(IndexingJob.class);

	public void execute(JobExecutionContext context)
			throws JobExecutionException {
		long startTime = System.currentTimeMillis();
		LOGGER.info("start indexing job...");
		
		Cache tmpCache = null;
		try {
			PlugDescription plugDescription = PlugServer.getPlugDescription();
			File file = plugDescription.getWorkinDirectory();
			
			CSWFactory factory = (CSWFactory)plugDescription.get(ConfigurationKeys.CSW_FACTORY);
			DocumentMapper mapper = (DocumentMapper)plugDescription.get(ConfigurationKeys.CSW_MAPPER);
			Cache cache = (Cache)plugDescription.get(ConfigurationKeys.CSW_CACHE);
			
			// start transaction
			tmpCache = cache.startTransaction();
			tmpCache.removeAllRecords();

			// start indexing
			IIndexer indexer = new Indexer();
			indexer.open(file);

			List<IDocumentReader> collection = DocumentReaderFactory.getDocumentReaderCollection(tmpCache, mapper, factory);
			for (IDocumentReader documentReader : collection) {
				indexer.index(documentReader);
			}

			indexer.close();
			
			// commit transaction
			tmpCache.commitTransaction();
			
		} catch (Exception e) {
			
			tmpCache.rollbackTransaction();
			throw new JobExecutionException(e);
		}
		LOGGER.info("indexing job done in: "
				+ (System.currentTimeMillis() - startTime) + " ms");
	}
}
