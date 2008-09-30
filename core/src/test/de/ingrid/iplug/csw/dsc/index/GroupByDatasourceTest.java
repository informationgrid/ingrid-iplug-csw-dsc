package de.ingrid.iplug.csw.dsc.index;

import java.io.File;

import junit.framework.TestCase;
import de.ingrid.utils.IngridHits;
import de.ingrid.utils.query.IngridQuery;
import de.ingrid.utils.queryparser.QueryStringParser;

public class GroupByDatasourceTest extends TestCase {

    public void testSearch() throws Exception {
        IngridQuery ingridQuery = QueryStringParser.parse("wasser grouped:" + IngridQuery.GROUPED_BY_DATASOURCE);

        DSCSearcher searcher = new DSCSearcher(new File("resources/index"), "search-plug");
        IngridHits hits = searcher.search(ingridQuery, 0, 10);

        assertEquals(10, hits.getHits().length);
        assertEquals(138, hits.length());
        for (int i = 0; i < hits.getHits().length; i++) {
            assertEquals(138, hits.getHits()[i].getGroupTotalHitLength());
        }

    }
}
