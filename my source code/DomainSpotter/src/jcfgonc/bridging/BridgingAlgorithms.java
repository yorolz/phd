package jcfgonc.bridging;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.FastMath;

import graph.StringEdge;
import graph.StringGraph;
import jcfgonc.bridging.genetic.GeneticAlgorithmConfig;

public class BridgingAlgorithms {

	public static StringGraph intersectGraphWithVertexSet(StringGraph graph, Set<String> maskingVertexSet) {
		StringGraph newGraph = new StringGraph(graph, true);
		for (StringEdge edge : graph.edgeSet()) {
			if (maskingVertexSet.contains(edge.getSource()) && maskingVertexSet.contains(edge.getTarget())) {
				newGraph.addEdge(edge);
			}
		}
		return newGraph;
	}

	public static void expandFromBridgeUntilIntersect(RandomGenerator random, String bridge, BridgingGene bag, StringGraph graph) {
		// set of visited and to visit vertices for the expansion process
		Set<String> openSet0 = new HashSet<String>();
		Set<String> openSet1 = new HashSet<String>();
		Set<String> closedSet0 = new HashSet<String>();
		Set<String> closedSet1 = new HashSet<String>();

		// bridge vertex will not be explored
		closedSet0.add(bridge);
		closedSet1.add(bridge);

		Set<String> set0 = new HashSet<String>();
		Set<String> set1 = new HashSet<String>();

		// split nearest bridge vertices into two sets
		splitBridge(bridge, set0, set1, random, graph);

		// now we have two subgraphs with only the neighbors of bridge vertex
		// expand subgraph from openset
		openSet0.addAll(set0);
		openSet1.addAll(set1);
		openSet0.remove(bridge);
		openSet1.remove(bridge);

		int iterations = 1;
		Set<String> intersection = null;
		Set<String> intersectionOld;
		while (true) {
			// backup before next expansion
			HashSet<String> oldSet0 = new HashSet<String>(set0);
			HashSet<String> oldSet1 = new HashSet<String>(set1);

			expandFromOpenSetOneLevel(set0, openSet0, closedSet0, graph);
			expandFromOpenSetOneLevel(set1, openSet1, closedSet1, graph);

			intersectionOld = intersection;
			intersection = intersection(set0, set1);
			intersection.remove(bridge);
			int minSize = FastMath.min(set0.size(), set1.size());
			double intersectionRatio = (double) intersection.size() / minSize;
			// double intersectionRatio = (double) intersection.size() / (double) (set0.size() + set1.size());
			// expand if there is something to expand (and below intersection threshold)
			if (intersectionRatio > GeneticAlgorithmConfig.MAX_INTERSECTION_RATIO || openSet0.isEmpty() || openSet1.isEmpty()) {
				// subgraphs intersect now, return copy before intersection
				set0 = oldSet0;
				set1 = oldSet1;
				if (intersectionOld == null) {
					intersection = new HashSet<>();
				} else {
					intersection = intersectionOld;
				}
				break;
			}
			iterations++;
		}
		// possibly reduce by 1
		bag.deepness = iterations;
		bag.set0 = set0;
		bag.set1 = set1;
		bag.intersection = intersection;
		bag.bridgeDegree = graph.getDegree(bridge);
		bag.bridge = bridge;
	}

	private static void expandFromOpenSetOneLevel(Set<String> accumulatingVertexSet, Set<String> openSet, Set<String> closedSet, StringGraph graph) {
		Set<String> openSetAddition = new HashSet<String>();
		Set<String> openSetRemoval = new HashSet<String>();
		for (String vertexId : openSet) {
			if (closedSet.contains(vertexId))
				continue;
			accumulatingVertexSet.add(vertexId);
			Set<String> neighbors = graph.getNeighborVertices(vertexId);
			for (String neighborId : neighbors) {
				if (closedSet.contains(neighborId))
					continue;
				accumulatingVertexSet.add(neighborId);
				openSetAddition.add(neighborId);
			}
			openSetRemoval.add(vertexId);
			closedSet.add(vertexId);
		}
		openSet.addAll(openSetAddition);
		openSet.removeAll(openSetRemoval);
	}

	public static String getRandomElementFromSet(Set<String> vertexSet, RandomGenerator random) {
		int size = vertexSet.size();
		int luckyShot = random.nextInt(size);
		int i = 0;
		for (String vertexId : vertexSet) {
			if (i == luckyShot)
				return vertexId;
			i++;
		}
		return null;
	}

	/**
	 * returns the intersection of the given sets
	 *
	 * @param vertexSet0
	 * @param vertexSet1
	 * @return
	 */
	public static Set<String> intersection(Set<String> vertexSet0, Set<String> vertexSet1) {
		HashSet<String> intersection = new HashSet<String>();
		for (String vertex0 : vertexSet0) {
			if (vertexSet1.contains(vertex0)) {
				intersection.add(vertex0);
			}
		}
		return intersection;
	}

	/**
	 * splits randomly half the neighbors of bridge vertex into subGraph0 and the remaining half into subGraph1
	 *
	 * @param bridge
	 * @param set0
	 * @param set1
	 */
	private static void splitBridge(String bridge, Set<String> set0, Set<String> set1, RandomGenerator random, StringGraph graph) {
		HashSet<String> localSet0 = new HashSet<>();
		HashSet<String> localSet1 = new HashSet<>();

		Set<String> neighborVertices = graph.getNeighborVertices(bridge);
		boolean[] booleanArray = randomBooleanArray(neighborVertices.size(), random);
		int i = 0;
		for (String neighborVerticeId : neighborVertices) {
			boolean addToSubGraph0 = booleanArray[i];
			if (addToSubGraph0) {
				localSet0.add(neighborVerticeId);
			} else {
				localSet1.add(neighborVerticeId);
			}
			i++;
		}
		set0.addAll(localSet0);
		set1.addAll(localSet1);
		set0.add(bridge);
		set1.add(bridge);
	}

	private static boolean[] randomBooleanArray(int size, RandomGenerator random) {
		boolean[] array = new boolean[size];
		Arrays.fill(array, 0, size / 2, true);
		shuffleBooleanArray(array, random);
		return array;
	}

	// Implementing Fisher–Yates shuffle
	private static void shuffleBooleanArray(boolean[] ar, RandomGenerator random) {
		// If running on Java 6 or older, use `new Random()` on RHS here
		for (int i = ar.length - 1; i > 0; i--) {
			int index = random.nextInt(i + 1);
			// Simple swap
			boolean a = ar[index];
			ar[index] = ar[i];
			ar[i] = a;
		}
	}

}
