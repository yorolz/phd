package dk.aaue.sna.alg.centrality;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Ranks a centrality result. Takes into account that several nodes may have the same
 * calculated value.
 *
 * @author Soren <soren@tanesha.net>
 */
public class CentralityRanking<V> {

    public static final double THRESHOLD = 0.001;

    public static <V> CentralityResult<V> makeRanks(CentralityResult<V> result) {

        double last = Double.NEGATIVE_INFINITY;
        double rank = 0.0;

        Map<V, Double> ranks = new HashMap();

        for (Map.Entry<V, Double> e : result.getSorted()) {
            double delta = Math.abs(e.getValue() - last);
            if (last == Double.NEGATIVE_INFINITY) {
                last = e.getValue();
            }
            else if (delta > THRESHOLD) { // different
                rank++;
                last = e.getValue();
            }
            ranks.put(e.getKey(), rank);
    }

        return new CentralityResult<V>(ranks, false);
    }

    public static <V> double[] makeIndexedRanks(List<V> indexed, Map<V, Double> map) {
        double[] r = new double[indexed.size()];

        for (int i = 0; i < indexed.size(); i++) {
             r[i] = map.containsKey(indexed.get(i)) ? map.get(indexed.get(i)) : 0.0;
        }

        return r;
    }

    public static <V> List<V> makeIndex(CentralityResult<V> result) {
        return new ArrayList<V>(result.getRaw().keySet());
    }

}
