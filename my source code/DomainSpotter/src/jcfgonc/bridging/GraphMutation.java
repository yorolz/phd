package jcfgonc.bridging;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.math3.random.RandomGenerator;

import graph.GraphAlgorithms;
import graph.StringEdge;
import graph.StringGraph;
import jcfgonc.bridging.genetic.GeneticAlgorithmConfig;
import jcfgonc.bridging.genetic.operators.GeneMutation;

public class GraphMutation implements GeneMutation<BridgingGene> {

	private StringGraph graph;
	private ArrayList<String> vertexSetAsList;
	private static HashSet<String> bridgeSet = new HashSet<>();
	private static ReentrantLock globalLock = new ReentrantLock();

	public GraphMutation(StringGraph graph) {
		this.graph = graph;
		this.vertexSetAsList = new ArrayList<>(graph.getVertexSet());
	}

	@Override
	public void mutateGenes(BridgingGene[] genes, RandomGenerator random) {
		BridgingGene bag = genes[0];
		String oldBridge = bag.bridge;
		String nextBridge = null;
		boolean gotBridge = false;
		int attempt = 1;
		boolean doNearbySearch = true;
		int minHops = 1;
		do {
			if (random.nextDouble() <= GeneticAlgorithmConfig.NEARBY_BRIDGE_JUMP_PROB && doNearbySearch) {
				double r = Math.pow(random.nextDouble(), GeneticAlgorithmConfig.MUTATION_JUMP_PROBABILITY_POWER);
				int hops = (int) Math.floor(r * GeneticAlgorithmConfig.BRIDGE_JUMPING_RANGE) + minHops;
				nextBridge = GraphAlgorithms.getVertexFromRandomWalk(random, oldBridge, graph, hops);
			} else {
				nextBridge = GraphAlgorithms.getRandomElementFromCollection(vertexSetAsList, random);
			}

			globalLock.lock();
			try {
				boolean contains = bridgeSet.contains(nextBridge);
				if (!contains) {
					bridgeSet.add(nextBridge);
					globalLock.unlock();
					gotBridge = true;
				} else {
					globalLock.unlock();
					Set<StringEdge> inc = graph.incomingEdgesOf(oldBridge);
					Set<StringEdge> out = graph.outgoingEdgesOf(oldBridge);
					if (inc.size() + out.size() <= 1) { // no available neighbors, better jump far
						doNearbySearch = false;
					} else if (attempt >= 3) { // time to jump far
						doNearbySearch = false;
					} else if (attempt >= 5) { // we are not going to stay here forever...
						gotBridge = true;
					} else {
					}
					minHops++;// prevent looping around old bridge
					attempt++;
				}
			} finally { // to prevent exceptions and unexpected situations
				if (globalLock.isHeldByCurrentThread()) {
					globalLock.unlock();
				}
			}

		} while (!gotBridge);

		bag.bridge = nextBridge;
		// recreate subgraphs from new bridge
		BridgingAlgorithms.expandFromBridgeUntilIntersect(random, nextBridge, bag, graph);
	}

}
