package dk.aaue.sna.alg.centrality;

import dk.aaue.sna.alg.centrality.CentralityRanking;
import dk.aaue.sna.alg.centrality.CentralityResult;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

/**
 * @author Soren <soren@tanesha.net>
 */
public class CentralityRankingTest {

    CentralityResult<String> result;

    @Test
    public void testRanks_01() throws Exception {

        Map<String, Double> map = new HashMap();
        map.put("A", 1.0);
        map.put("B", 1.5);
        map.put("C", 2.5);
        map.put("D", -1.0);

        result = new CentralityResult<String>(map, false);

        List<String> index = Arrays.asList("A","B","C","D");

        CentralityResult<String> ranked = CentralityRanking.makeRanks(result);

        double[] ranks = CentralityRanking.makeIndexedRanks(index, ranked.getRaw());

        for (int i = 0; i < ranks.length; i++) {
            System.out.println("ranks[" + i + "] = " + ranks[i]);
        }

        assertEquals(4, ranks.length);

        assertEquals(1.0, ranks[0]);
        assertEquals(2.0, ranks[1]);
        assertEquals(3.0, ranks[2]);
        assertEquals(0.0, ranks[3]);

    }

    @Test
    public void testRanks_02() throws Exception {

        Map<String, Double> map = new HashMap();
        map.put("A", 1.0);
        map.put("B", 1.0);
        map.put("C", 2.0);
        map.put("D", 3.0);
        map.put("E", 3.0);

        List<String> index = Arrays.asList("A","B","C","D","E");

        result = new CentralityResult<String>(map, false);

        CentralityResult<String> ranked = CentralityRanking.makeRanks(result);

        double[] ranks = CentralityRanking.makeIndexedRanks(index, ranked.getRaw());

        for (int i = 0; i < ranks.length; i++) {
            System.out.println("ranks[" + i + "] = " + ranks[i]);
        }

        assertEquals(5, ranks.length);

        assertEquals(0.0, ranks[0]);
        assertEquals(0.0, ranks[1]);
        assertEquals(1.0, ranks[2]);
        assertEquals(2.0, ranks[3]);
        assertEquals(2.0, ranks[4]);

    }

    @Test
    public void testMakeIndex() throws Exception {

        Map<String, Double> map = new HashMap();
        map.put("A", 1.0);
        map.put("B", 1.5);
        map.put("C", 2.5);
        map.put("D", -1.0);

        result = new CentralityResult<String>(map, true);

        List<String> index = CentralityRanking.makeIndex(result);

        assertEquals(4, index.size());

        assertTrue(index.contains("A"));
        assertTrue(index.contains("B"));
        assertTrue(index.contains("C"));
        assertTrue(index.contains("D"));
        assertFalse(index.contains("E"));

    }
}
