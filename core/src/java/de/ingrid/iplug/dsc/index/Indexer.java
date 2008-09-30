/*
 * Copyright (c) 1997-2005 by media style GmbH
 * 
 * $Source: /cvs/asp-search/src/java/com/ms/aspsearch/PermissionDeniedException.java,v $
 */

package de.ingrid.iplug.dsc.index;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;

import de.ingrid.iplug.PlugServer;
import de.ingrid.iplug.dsc.schema.Construct;
import de.ingrid.iplug.dsc.schema.RecordReader;
import de.ingrid.utils.PlugDescription;
import de.ingrid.utils.dsc.Column;
import de.ingrid.utils.dsc.Record;

/**
 * Iterates over a recordreader's records and indexes them to a given folder.
 */
public class Indexer {

    private File fTargetFolder;

    private static Log log = LogFactory.getLog(Indexer.class);

    private static HashMap fInstances = new HashMap();

    private RecordReader fReader;

    private double fX1;

    private double fY1;

    private double fX2;

    private double fY2;

    /**
     * May you better use the get instance since the indexer is already running.
     * 
     * @param targetFolder
     *            Where the index should be stored on the hd.
     * @param reader
     */
    public Indexer(File targetFolder, RecordReader reader) {
        this.fTargetFolder = targetFolder;
        this.fReader = reader;
        fInstances.put(targetFolder.getAbsolutePath(), this);
    }

    /**
     * Indexes a datasource into a given target folder.
     * 
     * @throws Exception
     */
    public void index() throws Exception {
        File indexFolder = new File(this.fTargetFolder, "newIndex");
        IndexWriter writer = new IndexWriter(indexFolder, new StandardAnalyzer(new String[0]), true);

        int count = 0;
        Record record;
        while ((record = this.fReader.nextRecord()) != null) {
            writer.addDocument(recordToDocument(new Document(), record));
            if (count++ % 100 == 0) {
                log.info("indexing record nr: " + count);
            }
        }
        if (log.isDebugEnabled()) {
        	log.debug("Finish indexing after " + count + " records.");
        }
        writer.optimize();
        writer.close();
        AbstractSearcher instance = AbstractSearcher.getInstance();
        if (instance != null) {
            // block
            instance.stop();
        }
        File oldIndex = new File(this.fTargetFolder, "index");
        delete(oldIndex);
        indexFolder.renameTo(oldIndex);
        if (instance != null) {
            // unblock
            instance.start();
        }
    }

    private void delete(File folder) {
        File[] files = folder.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                if (file.isDirectory()) {
                    delete(file);
                }
                file.delete();
            }
        }
        folder.delete();
    }

    private Document recordToDocument(Document document, Record record) throws Exception {
        int count = record.numberOfColumns();
        // info container for postprocessing record
        HashMap recordInfo = new HashMap();
        float boost = 1f;// column.getBoost();
        
        for (int i = 0; i < count; i++) {
            Column column = record.getColumn(i);
            if (column.toIndex()) {
                Field field = null;

                // padd the coordinates
                String value = record.getValueAsString(column);
                final String targetName = column.getTargetName().toLowerCase();
                if (targetName.equals("x1")) {
                    try {
						double valueD = Double.parseDouble(value);
						this.fX1 = Math.min(this.fX1, valueD);
						value = DoublePadding.padding(valueD);
					} catch (Exception e) {}
                } else if (targetName.equals("x2")) {
                    try {
	                    double valueD = Double.parseDouble(value);
	                    this.fX2 = Math.max(this.fX2, valueD);
	                    value = DoublePadding.padding(valueD);
					} catch (Exception e) {}
                } else if (targetName.equals("y1")) {
                    try {
	                    double valueD = Double.parseDouble(value);
	                    this.fY1 = Math.min(this.fY1, valueD);
	                    value = DoublePadding.padding(valueD);
					} catch (Exception e) {}
                } else if (targetName.equals("y2")) {
                    try {
	                    double valueD = Double.parseDouble(value);
	                    this.fY2 = Math.max(this.fY2, valueD);
	                    value = DoublePadding.padding(valueD);
					} catch (Exception e) {}
                } else if (targetName.equals("t0") || targetName.equals("t1") || targetName.equals("t2")) {
                    // cut time expressions
                    int lastPos = 8;
                    if (value.length() < lastPos) {
                        lastPos = value.length();
                    }
                    value = value.substring(0, lastPos);
                    // record time value for later postprocessing
                    if (value.length() > 0) {
                    	recordInfo.put(targetName, value);
                    }
                }

                if (column.getType().equals(Column.KEYWORD)) {
                    field = Field.Keyword(targetName, value);
                } else if (column.getType().equals(Column.TEXT)) {
                    field = Field.Text(targetName, value);
                //} else if (column.getType().equals(Column.DATE)) {
                    // field = Field.Keyword(column.getTargetName(), record
                    // .getValueAsDate(i));
                    // FIXME add date support
                } else {
                    field = Field.Text(targetName, AbstractSearcher.filterTerm(value));
                }
                field.setBoost(boost);

                if (targetName.equals("x1") || targetName.equals("x2") || targetName.equals("y1")
                        || targetName.equals("y2")) {
                    document.removeField(targetName);
                    document.add(field);
                } else {
                    document.add(field);
                }
                document.add(Field.Text("content", record.getValueAsString(column)));
                document.add(Field.Text("content", AbstractSearcher.filterTerm(record.getValueAsString(column))));
            }
        }
        
        /*
         * Set the boundaries of dates to values that can be compared with lucene. The
         * value of inifinite pas is '00000000' and the value for inifinit future is '99999999'.
         * 
         * Makes sure that the fields are only set, if we have a UDK date type of 'seit' or 'bis'. 
         * We can do this because the mapping filters and maps the dates to t0 in case of date type
         * 'am' and to t1 in case of 'seit', even if the database fields are the same. Thus we do not 
         * need to look at the DB field time_type which controls the date 
         * type ('am', 'seit', 'bis', 'von (von-bis)')   
         * 
         */
        if (recordInfo.get("t1") != null && recordInfo.get("t2") == null && recordInfo.get("t0") == null) {
        	if (log.isDebugEnabled()) {
        		log.debug("t1 is set, t2 and t0 not set: set t2 to '99999999'!");
        	}
        	Field field = Field.Text("t2", "99999999");
        	field.setBoost(boost);
        	document.removeField("t2");
            document.add(field);
        } else if (recordInfo.get("t1") == null && recordInfo.get("t2") != null && recordInfo.get("t0") == null) {
        	if (log.isDebugEnabled()) {
        		log.debug("t2 is set, t1 and t0 not set: set t1 to '00000000'!");
        	}
        	Field field = Field.Text("t1", "00000000");
        	field.setBoost(boost);
        	document.removeField("t1");
            document.add(field);
        }
        
        Record[] subRecords = record.getSubRecords();
        if (subRecords != null) {
            for (int i = 0; i < subRecords.length; i++) {
                recordToDocument(document, subRecords[i]);
            }
        }
        return document;
    }

    /**
     * @param targetFile
     * @param reader
     * @return a instance of the indexer incase there is no instance of this indexer locket to a given targetfile. The
     *         lock will be removed as soon the closed method is called.
     */
    public static Indexer getInstance(File targetFile, RecordReader reader) {
        if (fInstances.get(targetFile.getAbsolutePath()) == null) {
            Indexer indexer = new Indexer(targetFile, reader);
            return indexer;
        }
        return null;
    }

    /**
     * Closes an indexing job.
     */
    public void close() {
        fInstances.remove(this.fTargetFolder.getAbsolutePath());
    }

    /**
     * This method starts the indexing job from the commandline.
     * @param args No arguments needed.
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        long startTime = System.currentTimeMillis();
        log.info("start indexing job...");
        Construct construct;
        PlugDescription plugDescription;
        try {
            plugDescription = PlugServer.getPlugDescription();
            construct = AbstractSearcher.getConstruct(plugDescription);
        } catch (Exception e) {
        	log.error(e.getMessage(), e);
            throw new IllegalArgumentException("unable to load the configuration values");
        }

        File file = plugDescription.getWorkinDirectory();
        DatabaseConnection dsConnection = (DatabaseConnection) plugDescription.getConnection();

        Class.forName(dsConnection.getDataBaseDriver());
        String url = dsConnection.getConnectionURL();
        String user = dsConnection.getUser();
        String password = dsConnection.getPassword();
        Connection connection = DriverManager.getConnection(url, user, password);

        RecordReader reader = new RecordReader(construct, connection, dsConnection.getSchema(), url, user, password);
        Indexer indexer = Indexer.getInstance(file, reader);
        indexer.index();
        indexer.close();

        log.info("indexing job done in: " + (System.currentTimeMillis() - startTime) + " ms");
    }

}
