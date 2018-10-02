package jcfgonc.bridging;

import org.apache.commons.math3.random.RandomGenerator;

import graph.StringGraph;
import jcfgonc.bridging.genetic.operators.GeneInitializer;

public class GraphGeneInitializer<T extends BridgingGene> implements GeneInitializer<T> {

	private final StringGraph graph;

	public GraphGeneInitializer(StringGraph graph) {
		this.graph = graph;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void initializeGenes(T[] genes, RandomGenerator random) {
		// random spot (bridge node) to start expansion
		String bridge = BridgingAlgorithms.getRandomElementFromSet(graph.getVertexSet(), random);

		// initialize the gene wich contains the two subgraphs splitting the main graph
		BridgingGene bag = new BridgingGene();
		bag.bridge = bridge;
		genes[0] = (T) bag;

		BridgingAlgorithms.expandFromBridgeUntilIntersect(random, bridge, bag, graph);
	}

}
