/*
 * Copyright (c) 1997-2005 by media style GmbH
 */

package de.ingrid.iplug.csw.dsc.index;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.IndexSearcher;

import de.ingrid.iplug.csw.dsc.schema.Construct;
import de.ingrid.iplug.scheduler.SchedulingService;
import de.ingrid.utils.IngridHit;
import de.ingrid.utils.IngridHits;
import de.ingrid.utils.PlugDescription;
import de.ingrid.utils.dsc.Record;
import de.ingrid.utils.query.IngridQuery;
import de.ingrid.utils.queryparser.QueryStringParser;

/**
 * Searcher for the local index. 
 */
public class DSCSearcher extends AbstractSearcher {

    private RecordLoader fDetailer;

    private SchedulingService fScheduler;

    private PlugDescription fPlugDescription;

    /**
     * Initilaizes the DSC searcher variant.
     */
    public DSCSearcher() {
        // nothing to do here
    }

    /**
     * @param file
     * @param string
     * @throws IOException
     */
    public DSCSearcher(File file, String string) throws IOException {
        super(file, string);
    }

    public void configure(PlugDescription plugDescription) throws Exception {
        this.fPlugDescription = plugDescription;
        this.fPlugId = plugDescription.getPlugId();
        this.fUrl = (String) plugDescription.get("detailUrl");
        this.fSearcher = new IndexSearcher(new File(plugDescription.getWorkinDirectory(), "index").getAbsolutePath());
        DatabaseConnection dsConnection = (DatabaseConnection) plugDescription.getConnection();

        Class.forName(dsConnection.getDataBaseDriver());
        String url = dsConnection.getConnectionURL();
        String user = dsConnection.getUser();
        String password = dsConnection.getPassword();
        Construct construct = getConstruct(plugDescription);
        this.fDetailer = new RecordLoader(construct, dsConnection.getSchema(), url, user, password);
        this.fScheduler = new SchedulingService(new File(plugDescription.getWorkinDirectory(), "jobstore"));
    }

    public IngridHits search(IngridQuery query, int start, int length) throws Exception {
        return search(query, false, start, length);
    }

    public Record getRecord(IngridHit hit) throws Exception {
        Document document = this.fSearcher.doc(hit.getDocumentId());
        Record record = this.fDetailer.getDetails(document);
        
        return record;
    }

    public void close() throws Exception {
      if (this.fDetailer != null) {
        this.fDetailer.close();
      }
      if (this.fSearcher != null) {
        this.fSearcher.close();
      }
      if (this.fScheduler != null) {
        this.fScheduler.shutdown();
      }
    }


  /**
     * Main method for testing from commandline.
     * @param args Two arguments are needed a index folder and the query.
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        String usage = "<indexFolder> queryString";
        if (args.length < 2) {
            System.err.println(usage);
            return;
        }
        DSCSearcher searcher = new DSCSearcher(new File(args[0]), "atestId");
        IngridQuery query = QueryStringParser.parse(args[1]);
        System.out.println("search for: " + query.toString());

        IngridHits hits = searcher.search(query, 0, Integer.MAX_VALUE);
        System.out.println("hits: " + hits.length());
        IngridHit[] results = hits.getHits();
        for (int i = 0; i < results.length; i++) {
            IngridHit hit = results[i];
            System.out.println(hit.toString());
        }

    }

    public PlugDescription getPlugDescription() {
        return this.fPlugDescription;
    }
}
