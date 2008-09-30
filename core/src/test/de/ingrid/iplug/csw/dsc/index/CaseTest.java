package de.ingrid.iplug.csw.dsc.index;

import junit.framework.TestCase;

import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;

/**
 * 
 */
public class CaseTest extends TestCase {

    /**
     * @throws Exception
     */
    public void testCasesAndTest() throws Exception {
        String path = "./test_case_index";
        StandardAnalyzer analyzer = new StandardAnalyzer();
        Document document = new Document();
        document.add(Field.Text("test", "Test 23"));
        document.add(Field.Keyword("key", "T23t"));
        IndexWriter writer = new IndexWriter(path, analyzer, true);
        writer.addDocument(document);
        writer.close();
        IndexSearcher searcher = new IndexSearcher(path);
        
        Hits hits = searcher.search(QueryParser.parse("Test", "test", analyzer));
        assertEquals(1, hits.length());
        
        hits = searcher.search(QueryParser.parse("23", "test", analyzer));
        assertEquals(1, hits.length());
        
        hits = searcher.search(QueryParser.parse("23", "key", analyzer));
        assertEquals(0, hits.length());

        hits = searcher.search(QueryParser.parse("T23t", "key", new WhitespaceAnalyzer()));
        assertEquals(1, hits.length());
        
        Document document2 = searcher.doc(0);
        assertNotNull(document2);
        assertEquals("Test 23", document2.get("test"));
    }
}
