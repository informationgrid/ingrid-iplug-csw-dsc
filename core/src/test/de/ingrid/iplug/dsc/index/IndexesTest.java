/*
 * Copyright (c) 1997-2005 by media style GmbH
 * 
 * $Source: /cvs/asp-search/src/java/com/ms/aspsearch/PermissionDeniedException.java,v $
 */

package de.ingrid.iplug.dsc.index;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import junit.framework.TestCase;
import de.ingrid.iplug.dsc.schema.DBSchemaControllerTest;
import de.ingrid.iplug.dsc.schema.RecordReader;
import de.ingrid.iplug.dsc.schema.RecordReaderTest;
import de.ingrid.utils.IngridHits;
import de.ingrid.utils.query.FieldQuery;
import de.ingrid.utils.query.IngridQuery;
import de.ingrid.utils.queryparser.QueryStringParser;

/**
 * TODO comment for IndexesTest
 * 
 * <p/>created on 30.05.2006
 * 
 * @version $Revision: $
 * @author jz
 * @author $Author: ${lastedit}
 * 
 */
public class IndexesTest extends TestCase {

    private Connection fConnection;

    protected void setUp() throws Exception {
        Class.forName("org.hsqldb.jdbcDriver");
        this.fConnection = DriverManager.getConnection("jdbc:hsqldb:mem:testdb", "sa", "");
        Statement statement = this.fConnection.createStatement();
        statement.addBatch(DBSchemaControllerTest.getStatement("/sql2.txt"));
        statement.executeBatch();

    }

    protected void tearDown() throws Exception {
        Statement statement = this.fConnection.createStatement();
        statement.execute("SHUTDOWN");
        DBSchemaControllerTest.removeDB("testdb");
    }

    /**
     * @throws Exception
     */
    public void testIndexer() throws Exception {
        File file = new File("./testIndex");
        RecordReader recordReader = new RecordReader(RecordReaderTest.getSimpleConstruct(), this.fConnection, null, "jdbc:hsqldb:mem:testdb", "sa", "");
        Indexer indexer = new Indexer(file, recordReader);
        indexer.index();
        indexer.close();
        DSCSearcher searcher = new DSCSearcher(new File(file, "index"), "myProviderId");
        IngridHits hits = searcher.search(QueryStringParser.parse("av"), 0, 100);
        assertNotNull(hits);
        assertTrue(hits.getHits().length > 0);
    }

    /**
     * @throws Exception
     */
    public void testSearch() throws Exception {
        File file = new File("./testIndex/index");
        DSCSearcher searcher = new DSCSearcher(file, "bla");
        IngridHits hits = searcher.search(QueryStringParser.parse("av"), 0, 100);
        assertNotNull(hits.getHits());
        assertTrue(hits.getHits().length > 0);
    }

    /**
     * @throws Exception
     */
    public void testUpperCaseSearch() throws Exception {
        File file = new File("./testIndex/index");
        DSCSearcher searcher = new DSCSearcher(file, "bla");
        IngridHits hits = searcher.search(QueryStringParser.parse("AV"), 0, 100);
        assertNotNull(hits.getHits());
        assertTrue(hits.getHits().length > 0);
    }

    /**
     * @throws Exception
     */
    public void testFieldSearch() throws Exception {
        File file = new File("./testIndex/index");
        DSCSearcher searcher = new DSCSearcher(file, "bla");
        IngridQuery query = new IngridQuery();
        query.addField(new FieldQuery(true, false, "customer.street", "av"));
        IngridHits hits = searcher.search(query, 0, 100);
        assertNotNull(hits.getHits());
        assertTrue(hits.getHits().length > 0);
    }

    /**
     * @throws Exception
     */
    public void testFieldSearchNumber() throws Exception {
        File file = new File("./testIndex/index");
        DSCSearcher searcher = new DSCSearcher(file, "bla");
        IngridQuery query = new IngridQuery();
        query.addField(new FieldQuery(true, false, "item.item", "12"));
        IngridHits hits = searcher.search(query, 0, 100);
        assertNotNull(hits.getHits());
        assertTrue(hits.getHits().length > 0);
    }

    /**
     * @throws Exception
     */
    public void testPhrase() throws Exception {
        File file = new File("./testIndex");
        DSCSearcher searcher = new DSCSearcher(new File(file, "index"), "myProviderId");
        IngridHits hits = searcher.search(QueryStringParser.parse("\"julia\""), 0, 100);
        assertNotNull(hits);
        assertTrue(hits.getHits().length > 0);

        hits = searcher.search(QueryStringParser.parse("\"college av\""), 0, 100);
        assertNotNull(hits);
        assertTrue(hits.getHits().length > 0);
    }

    /**
     * @throws Exception
     */
    public void testPrefixTerm() throws Exception {
        File file = new File("./testIndex");
        DSCSearcher searcher = new DSCSearcher(new File(file, "index"), "myProviderId");
        IngridHits hits = searcher.search(QueryStringParser.parse("juli*"), 0, 100);
        assertNotNull(hits);
        assertTrue(hits.getHits().length > 0);
        
        hits = searcher.search(QueryStringParser.parse("content:juli*"), 0, 100);
        assertNotNull(hits);
        assertTrue(hits.getHits().length > 0);
    }

    /**
     * @throws Exception
     */
    public void testWildcardFields() throws Exception {
        File file = new File("./testIndex");
        DSCSearcher searcher = new DSCSearcher(new File(file, "index"), "myProviderId");
        IngridHits hits = searcher.search(QueryStringParser.parse("content:ju*a"), 0, 100);
        assertNotNull(hits);
        assertTrue(hits.getHits().length > 0);

        hits = searcher.search(QueryStringParser.parse("content:ju?ia"), 0, 100);
        assertNotNull(hits);
        assertTrue(hits.getHits().length > 0);
    }

    /**
     * @throws Exception
     */
    public void testWildcardTerms() throws Exception {
        File file = new File("./testIndex");
        DSCSearcher searcher = new DSCSearcher(new File(file, "index"), "myProviderId");
        IngridHits hits = searcher.search(QueryStringParser.parse("jul*a"), 0, 100);
        assertNotNull(hits);
        assertTrue(hits.getHits().length > 0);

        hits = searcher.search(QueryStringParser.parse("ju?ia"), 0, 100);
        assertNotNull(hits);
        assertTrue(hits.getHits().length > 0);
    }

    /**
     * @throws Exception
     */
    public void testFuzzyFields() throws Exception {
        File file = new File("./testIndex");
        DSCSearcher searcher = new DSCSearcher(new File(file, "index"), "myProviderId");
        IngridQuery query = QueryStringParser.parse("content:jolia~");
        assertEquals(1, query.getFuzzyFieldQueries().length);
        IngridHits hits = searcher.search(query, 0, 100);

        assertNotNull(hits);
        assertTrue(hits.getHits().length > 0);
    }

    /**
     * @throws Exception
     */
    public void testFuzzyTerms() throws Exception {
        File file = new File("./testIndex");
        DSCSearcher searcher = new DSCSearcher(new File(file, "index"), "myProviderId");
        IngridQuery query = QueryStringParser.parse("jolia~");
        assertEquals(1, query.getFuzzyTermQueries().length);
        IngridHits hits = searcher.search(query, 0, 100);

        assertNotNull(hits);
        assertTrue(hits.getHits().length > 0);
    }
}
