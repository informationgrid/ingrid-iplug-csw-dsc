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
import de.ingrid.iplug.csw.dsc.cache.UpdateJob;
import de.ingrid.iplug.csw.dsc.cswclient.CSWFactory;
import de.ingrid.iplug.csw.dsc.cswclient.CSWQuery;
import de.ingrid.iplug.csw.dsc.mapping.DocumentMapper;
import de.ingrid.iplug.csw.dsc.tools.SimpleSpringBeanFactory;
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

			// get instances from spring configuration
			CSWFactory factory = SimpleSpringBeanFactory.INSTANCE.getBean(ConfigurationKeys.CSW_FACTORY, CSWFactory.class);
			factory.setQueryTemplate(SimpleSpringBeanFactory.INSTANCE.getBean(ConfigurationKeys.CSW_QUERY_TEMPLATE, CSWQuery.class));
			DocumentMapper mapper = SimpleSpringBeanFactory.INSTANCE.getBean(ConfigurationKeys.CSW_MAPPER, DocumentMapper.class);
			Cache cache = SimpleSpringBeanFactory.INSTANCE.getBean(ConfigurationKeys.CSW_CACHE, Cache.class);
			cache.configure(factory);
			
			// start transaction
			tmpCache = cache.startTransaction();
			tmpCache.removeAllRecords();

			// run the update job
			UpdateJob job = new UpdateJob(factory, tmpCache);
			job.execute(10, 2000);
			
			// start indexing
			IIndexer indexer = new Indexer();
			indexer.open(file);

			List<IDocumentReader> collection = DocumentReaderFactory.getDocumentReaderCollection(tmpCache, mapper);
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
	
    /**
     * This method starts the indexing job from the commandline.
     * @param args No arguments needed.
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        
    	IndexingJob job = new IndexingJob();
    	job.execute(null);
    }	
	
}
