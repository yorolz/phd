package dk.aaue.sna.alg;

import org.jgrapht.Graph;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Soren <soren@tanesha.net>
 */
public class CachedShortestDistances<V, E> {

    private Map<V, Map<V, Double>> distances = new HashMap();

    private Graph<V, E> graph;
    private DijkstraForClosuresFactory<V, E> factory;

    public CachedShortestDistances(Graph<V, E> graph, DijkstraForClosuresFactory<V, E> factory) {
        this.graph = graph;
        this.factory = factory;
    }

    public Map<V, Double> distancesFrom(V node) {

        if (!distances.containsKey(node)) {

            DijkstraForClosures<V, E> forNode =
                    factory.create(graph, node);

            Map<V, Double> nodeDistances = new HashMap();
            for (V other : graph.vertexSet())
                if (other != node)
                    nodeDistances.put(other, forNode.get(other));

            distances.put(node, nodeDistances);
        }

        return distances.get(node);
    }


}
