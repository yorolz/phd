package dk.aaue.sna.ext.csv;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.junit.Test;

import java.io.StringReader;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

/**
 * @author Soren <soren@tanesha.net>
 */
public class CSVImporterTest {

    CSVImporter<String, DefaultWeightedEdge> impl;

    @Test
    public void testCreateImporter_01() throws Exception {

        String graph =
                "A,B,3.0\n" +
                "A,B,2.0\n" +
                "B,C\n";

        SimpleDirectedWeightedGraph<String, DefaultWeightedEdge> g = new SimpleDirectedWeightedGraph<String, DefaultWeightedEdge>(DefaultWeightedEdge.class);

        impl = CSVImporter.createImporter(new StringReader(graph), 0, 1, 2);

        impl.generateGraph(g, null, null);

        assertTrue(g.containsVertex("A"));
        assertTrue(g.containsVertex("B"));
        assertTrue(g.containsVertex("C"));
        assertEquals(3, g.vertexSet().size());

        assertTrue(g.containsEdge("A", "B"));
        assertTrue(g.containsEdge("B", "C"));
        assertEquals(2, g.edgeSet().size());

        DefaultWeightedEdge e = g.getEdge("A", "B");
        assertEquals(3.0, g.getEdgeWeight(e));
        assertEquals(1.0, g.getEdgeWeight(g.getEdge("B", "C")));


    }
}
