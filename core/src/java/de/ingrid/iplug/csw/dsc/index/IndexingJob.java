package de.ingrid.iplug.csw.dsc.index;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;

import de.ingrid.iplug.PlugServer;
import de.ingrid.iplug.csw.dsc.schema.Construct;
import de.ingrid.iplug.csw.dsc.schema.RecordReader;
import de.ingrid.utils.PlugDescription;

/**
 * Quarz job that runs the indexing
 */
public class IndexingJob implements StatefulJob {

    private static final long MINUTE = 1000 * 60;

    private static Log log = LogFactory.getLog(IndexingJob.class);

    public void execute(JobExecutionContext context) throws JobExecutionException {
        long startTime = System.currentTimeMillis();
        log.info("start indexing job...");
        try {
            Construct construct;
            PlugDescription plugDescription;
            try {
                plugDescription = PlugServer.getPlugDescription();
                construct = AbstractSearcher.getConstruct(plugDescription);
            } catch (Exception e) {
                throw new JobExecutionException("unable to load the configuration values", e, false);
            }

            File file = plugDescription.getWorkinDirectory();
            DatabaseConnection dsConnection = (DatabaseConnection) plugDescription.getConnection();

            Class.forName(dsConnection.getDataBaseDriver());
            String url = dsConnection.getConnectionURL();
            String user = dsConnection.getUser();
            String password = dsConnection.getPassword();
            Connection connection = DriverManager.getConnection(url, user, password);

            RecordReader reader = new RecordReader(construct, connection, dsConnection.getSchema(), url, user, password);
            Indexer indexer = null;

            while ((indexer = Indexer.getInstance(file, reader)) == null) {
                log.warn("old indexing job still running, check your scheduler settings...");
                Thread.sleep(MINUTE);
            }

            try {
                indexer.index();
            } catch (Exception e) {
                throw e;
            } finally {
                indexer.close();
            }
        } catch (Exception e) {
            throw new JobExecutionException(e);
        }
        log.info("indexing job done in: " + (System.currentTimeMillis() - startTime) + " ms");
    }
}
