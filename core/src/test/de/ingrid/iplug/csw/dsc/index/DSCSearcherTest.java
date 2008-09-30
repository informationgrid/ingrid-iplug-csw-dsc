package de.ingrid.iplug.csw.dsc.index;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import junit.framework.TestCase;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.search.Query;

import de.ingrid.utils.query.FieldQuery;
import de.ingrid.utils.query.IngridQuery;
import de.ingrid.utils.query.RangeQuery;
import de.ingrid.utils.query.WildCardFieldQuery;
import de.ingrid.utils.queryparser.ParseException;
import de.ingrid.utils.queryparser.QueryStringParser;

/**
 * TODO comment for DSCSearcherTest
 * 
 * <p/>created on 26.05.2006
 * 
 * @version $Revision: $
 * @author jz
 * @author $Author: ${lastedit}
 * 
 */
public class DSCSearcherTest extends TestCase {

    /**
     * @throws Exception
     */
    public void testConstructDetailUrl() throws Exception {
        String url = "heute{foo}sch�nes{bar}in{bazz}";
        Document document = new Document();
        document.add(Field.Keyword("foo", "ist"));
        document.add(Field.Keyword("bar", "wetter"));
        document.add(Field.Keyword("bazz", "halle"));
        String result = "heuteistsch�neswetterinhalle";

        assertEquals(result, DSCSearcher.constructDetailUrl(url, document));

    }

    /**
     * @throws Exception
     */
    public void testBuildLuceneQuery() throws Exception {
        IngridQuery ingridQuery = new IngridQuery();
        ingridQuery.addField(new FieldQuery(true, true, "foo", "bar"));
        ingridQuery.addRangeQuery(new RangeQuery(true, false, "foo", "x", "y", true));
        ingridQuery.addWildCardFieldQuery(new WildCardFieldQuery(true, false, "foo", "b*r"));
        System.out.println(ingridQuery.toString());
        assertNotNull(ingridQuery.toString());
    }

    /**
     * @throws Exception
     */
    public void testInsideBoundingBoxes() throws Exception {
        IngridQuery ingridQuery = new IngridQuery();
        ingridQuery.addField(new FieldQuery(true, true, "x1", "1"));
        ingridQuery.addField(new FieldQuery(true, true, "x2", "2"));
        ingridQuery.addField(new FieldQuery(true, true, "y1", "1"));
        ingridQuery.addField(new FieldQuery(true, true, "y2", "2"));
        ingridQuery.addField(new FieldQuery(true, true, "coord", "inside"));
        System.out.println("IngridQuery: " + ingridQuery.toString());
        Query query = AbstractSearcher.buildLuceneQuery(ingridQuery, false);
        assertNotNull(query.toString());
        System.out.println("luceneQuery: " + query.toString());
    }

    /**
     * @throws Exception
     */
    public void testIntersectBoundingBoxes() throws Exception {
        IngridQuery ingridQuery = new IngridQuery();
        ingridQuery.addField(new FieldQuery(true, true, "x1", "1"));
        ingridQuery.addField(new FieldQuery(true, true, "x2", "2"));
        ingridQuery.addField(new FieldQuery(true, true, "y1", "1"));
        ingridQuery.addField(new FieldQuery(true, true, "y2", "2"));
        ingridQuery.addField(new FieldQuery(true, true, "coord", "intersect"));
        System.out.println("IngridQuery: " + ingridQuery.toString());
        Query query = AbstractSearcher.buildLuceneQuery(ingridQuery, false);
        assertNotNull(query.toString());
        System.out.println("luceneQuery: " + query.toString());
    }

    /**
     * @throws Exception
     */
    public void testIcludeBoundingBoxes() throws Exception {
        IngridQuery ingridQuery = new IngridQuery();
        ingridQuery.addField(new FieldQuery(true, true, "x1", "1"));
        ingridQuery.addField(new FieldQuery(true, true, "x2", "2"));
        ingridQuery.addField(new FieldQuery(true, true, "y1", "1"));
        ingridQuery.addField(new FieldQuery(true, true, "y2", "2"));
        ingridQuery.addField(new FieldQuery(true, true, "coord", "include"));
        System.out.println("IngridQuery: " + ingridQuery.toString());
        Query query = AbstractSearcher.buildLuceneQuery(ingridQuery, false);
        assertNotNull(query.toString());
        System.out.println("luceneQuery: " + query.toString());
    }

    /**
     * @throws Exception
     */
    public void testTime() throws Exception {
        IngridQuery ingridQuery = new IngridQuery();
        ingridQuery.addField(new FieldQuery(true, true, "t1", "1"));
        ingridQuery.addField(new FieldQuery(true, true, "t2", "2"));
        System.out.println("IngridQuery: " + ingridQuery.toString());
        Query query = AbstractSearcher.buildLuceneQuery(ingridQuery, false);
        assertNotNull(query.toString());
        System.out.println("luceneQuery: " + query.toString());
    }

    /**
     * @throws ParseException
     * @throws IOException
     */
    public void testEmptySubClause() throws ParseException, IOException {
        IngridQuery ingridQuery = QueryStringParser
                .parse("boden t1:1970-01-01 t2:1990-01-01 (time:include OR time:intersect)");
        Query query = AbstractSearcher.buildLuceneQuery(ingridQuery, false);
        assertFalse("Query is empty: " + query, query.toString().trim().equals(""));
        assertFalse("Query contains empty sub clauses: " + query, query.toString().matches(".*\\(\\).*"));

        ingridQuery = QueryStringParser.parse("(boden t1:1970-01-01 t2:1990-01-01 time:intersect)");
        query = AbstractSearcher.buildLuceneQuery(ingridQuery, false);
        assertFalse("Query is empty: " + query, query.toString().equals(""));
        assertFalse("Query contains empty sub clauses: " + query, query.toString().trim().matches(".*\\(\\).*"));
    }

    /**
     * @throws ParseException
     * @throws IOException
     */
    public void testPhraseFieldQuery() throws ParseException, IOException {
        IngridQuery ingridQuery = QueryStringParser.parse("boden:a b");
        Query query = AbstractSearcher.buildLuceneQuery(ingridQuery, false);
        assertFalse("Query is empty: " + query, query.toString().trim().equals(""));
        assertEquals("+content:b title:b^-1.0 +boden:a", query.toString());

        ingridQuery = QueryStringParser.parse("boden:\"a b\"");
        query = AbstractSearcher.buildLuceneQuery(ingridQuery, false);
        assertFalse("Query is empty: " + query, query.toString().equals(""));
        assertEquals(query.toString(), "+boden:\"a b\"");
    }

    /**
     * @throws ParseException
     * @throws IOException
     */
    public void testFieldQueryInClauses() throws ParseException, IOException {
        IngridQuery ingridQuery = QueryStringParser
                .parse("klima x1:1 x2:2 y1:1 y2:2 (coord:include OR coord:intersect)");
        Query query = AbstractSearcher.buildLuceneQuery(ingridQuery, false);
        assertFalse("Query is empty: " + query, query.toString().trim().equals(""));
        assertEquals("+content:klima title:klima^-1.0 +x1:0000000001 +x2:0000000002 +y1:0000000001 +y2:0000000002",
                query.toString());

        ingridQuery = QueryStringParser.parse("klima x1:1 x2:2 y1:1 y2:2");
        query = AbstractSearcher.buildLuceneQuery(ingridQuery, false);
        assertFalse("Query is empty: " + query, query.toString().trim().equals(""));
        assertEquals("+content:klima title:klima^-1.0 +x1:0000000001 +x2:0000000002 +y1:0000000001 +y2:0000000002",
                query.toString());
    }

    public void testMoreTimeQueriesInClauses() throws ParseException, IOException {
        IngridQuery ingridQuery1 = QueryStringParser.parse("klima t1:2006-01-01 t2:2006-02-01");
        IngridQuery ingridQuery2 = QueryStringParser.parse("klima t1:2006-01-01 t2:2006-02-01 (time:inside)");
        Query query1 = AbstractSearcher.buildLuceneQuery(ingridQuery1, false);
        Query query2 = AbstractSearcher.buildLuceneQuery(ingridQuery2, false);
        assertEquals(query1.toString(), query2.toString());

        IngridQuery ingridQuery = QueryStringParser.parse("klima (t1:2006-01-01 t2:2006-02-01 time:include)");
        Query query = AbstractSearcher.buildLuceneQuery(ingridQuery, false);
        assertFalse("Query is empty: " + query, query.toString().trim().equals(""));
        assertEquals("+content:klima title:klima^-1.0 +(+((+t1:[00000000 TO 20060101] +t2:[20060201 TO 99999999]) (+((+t1:[20060101 TO 20060201] +t2:[20060101 TO 20060201]) t0:[20060101 TO 20060201]))))", query
                .toString());

        ingridQuery = QueryStringParser
                .parse("klima (t1:2006-01-01 t2:2006-02-01 time:include) OR (t1:2006-01-01 t2:2006-02-01 coord:intersect)");
        query = AbstractSearcher.buildLuceneQuery(ingridQuery, false);
        assertFalse("Query is empty: " + query, query.toString().trim().equals(""));
        assertEquals(
                "+content:klima title:klima^-1.0 (+((+t1:[00000000 TO 20060101] +t2:[20060201 TO 99999999]) (+((+t1:[20060101 TO 20060201] +t2:[20060101 TO 20060201]) t0:[20060101 TO 20060201])))) (+((+t1:[20060101 TO 20060201] +t2:[20060101 TO 20060201]) t0:[20060101 TO 20060201]))",
                query.toString());

        ingridQuery = QueryStringParser.parse("klima t0:2006-01-01 (time:include OR coord:intersect)");
        query = AbstractSearcher.buildLuceneQuery(ingridQuery, false);
        assertFalse("Query is empty: " + query, query.toString().trim().equals(""));
        assertEquals("+content:klima title:klima^-1.0 +t0:20060101", query.toString());

        ingridQuery = QueryStringParser.parse("klima t0:2006-01-01");
        query = AbstractSearcher.buildLuceneQuery(ingridQuery, false);
        assertFalse("Query is empty: " + query, query.toString().trim().equals(""));
        assertEquals("+content:klima title:klima^-1.0 +t0:20060101", query.toString());

        ingridQuery = QueryStringParser.parse("klima t0:2006-01-01 time:inside");
        query = AbstractSearcher.buildLuceneQuery(ingridQuery, false);
        assertFalse("Query is empty: " + query, query.toString().trim().equals(""));
        assertEquals("+content:klima title:klima^-1.0 +t0:20060101", query.toString());

        ingridQuery = QueryStringParser.parse("klima t0:2006-01-01 time:intersect");
        query = AbstractSearcher.buildLuceneQuery(ingridQuery, false);
        assertFalse("Query is empty: " + query, query.toString().trim().equals(""));
        assertEquals("+content:klima title:klima^-1.0 +((+t0:20060101) (+(t0:20060101 t1:20060101 t2:20060101)))", query.toString());

        ingridQuery = QueryStringParser.parse("klima t0:2006-01-01 time:include");
        query = AbstractSearcher.buildLuceneQuery(ingridQuery, false);
        assertFalse("Query is empty: " + query, query.toString().trim().equals(""));
        final SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
        assertEquals("+content:klima title:klima^-1.0 +((+t1:{00000000 TO 20060101} +t2:{20060101 TO 99999999}) (+t0:20060101))", query.toString());
    }
}
