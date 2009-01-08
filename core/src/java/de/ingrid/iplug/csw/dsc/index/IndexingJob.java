package de.ingrid.iplug.csw.dsc.index;

import java.io.File;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;

import de.ingrid.iplug.PlugServer;
import de.ingrid.utils.PlugDescription;

public class IndexingJob implements StatefulJob {

	private static Log LOGGER = LogFactory.getLog(IndexingJob.class);

	public void execute(JobExecutionContext context)
			throws JobExecutionException {
		long startTime = System.currentTimeMillis();
		LOGGER.info("start indexing job...");
		try {
			PlugDescription plugDescription = PlugServer.getPlugDescription();
			File file = plugDescription.getWorkinDirectory();
			IIndexer indexer = new Indexer();
			indexer.open(file);

			List<IDocumentReader> collection = DocumentReaderFactory.getDocumentReaderCollection(plugDescription);
			for (IDocumentReader documentReader : collection) {
				indexer.index(documentReader);
			}

			indexer.close();
		} catch (Exception e) {
			throw new JobExecutionException(e);
		}
		LOGGER.info("indexing job done in: "
				+ (System.currentTimeMillis() - startTime) + " ms");
	}
}
