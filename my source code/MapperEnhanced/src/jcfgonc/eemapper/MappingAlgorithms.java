package jcfgonc.eemapper;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import graph.DirectedMultiGraph;
import graph.GraphAlgorithms;
import graph.GraphEdge;
import graph.StringEdge;
import graph.StringGraph;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import mapper.OrderedPair;
import structures.MapOfSet;

public class MappingAlgorithms {
	/**
	 * Returns true if both concepts have the same labels in their incoming/outgoing edges. Returns false otherwise.
	 * 
	 * @param inputSpace
	 * @param leftConcept
	 * @param rightConcept
	 * @param out
	 * @return
	 */
	public static boolean containsCommonEdgeLabels(StringGraph inputSpace, String leftConcept, String rightConcept, boolean out) {
		Set<StringEdge> leftEdges;
		Set<StringEdge> rightEdges;
		if (out) {
			leftEdges = inputSpace.outgoingEdgesOf(leftConcept);
			rightEdges = inputSpace.outgoingEdgesOf(rightConcept);
		} else {
			leftEdges = inputSpace.incomingEdgesOf(leftConcept);
			rightEdges = inputSpace.incomingEdgesOf(rightConcept);
		}
		HashSet<String> leftLabels = GraphAlgorithms.getEdgesLabelsAsSet(leftEdges);
		HashSet<String> rightLabels = GraphAlgorithms.getEdgesLabelsAsSet(rightEdges);
		boolean commonLabels = GraphAlgorithms.intersects(leftLabels, rightLabels);
		return commonLabels;
	}

	private static <V, E> boolean containsCommonEdgeLabels(DirectedMultiGraph<V, E> inputSpace, V leftConcept, V rightConcept, boolean out) {
		Set<GraphEdge<V, E>> leftEdges;
		Set<GraphEdge<V, E>> rightEdges;
		if (out) {
			leftEdges = inputSpace.outgoingEdgesOf(leftConcept);
			rightEdges = inputSpace.outgoingEdgesOf(rightConcept);
		} else {
			leftEdges = inputSpace.incomingEdgesOf(leftConcept);
			rightEdges = inputSpace.incomingEdgesOf(rightConcept);
		}
		HashSet<E> leftLabels = GraphAlgorithms.getEdgesLabelsAsSet(leftEdges);
		HashSet<E> rightLabels = GraphAlgorithms.getEdgesLabelsAsSet(rightEdges);
		boolean commonLabels = GraphAlgorithms.intersects(leftLabels, rightLabels);
		return commonLabels;
	}

	public static HashMap<String, OrderedPair<String>> expandConceptPair(StringGraph inputSpace, DirectedMultiGraph<OrderedPair<String>, String> pairGraph, RandomGenerator random,
			OrderedPair<String> refPair, HashSet<String> usedConcepts) {
		// do the expansion
		HashMap<String, OrderedPair<String>> pairs;
		{
			// get left/right edges
			String left = refPair.getLeftElement();
			String right = refPair.getRightElement();

			// left and right are expected to be different
			assert !left.equals(right) : "left and right concepts of a pair are expected to be different";

			usedConcepts.add(left);
			usedConcepts.add(right);
			// create left/right edge maps according to labels+direction
			MapOfSet<String, String> leftmap = mapEgdeLabelsDirToNeighbors(left, inputSpace, usedConcepts);
			MapOfSet<String, String> rightmap = mapEgdeLabelsDirToNeighbors(right, inputSpace, usedConcepts);
			// intersect maps' keys randomly
			pairs = extractPairsFromMaps(leftmap, rightmap, random, usedConcepts);
		}
		// update the pair/mapping graph
		for (String dirLabel : pairs.keySet()) {
			OrderedPair<String> nextPair = pairs.get(dirLabel);
			if (dirLabel.charAt(0) == '-') {
				pairGraph.addEdge(nextPair, refPair, dirLabel.substring(1));
			} else {
				pairGraph.addEdge(refPair, nextPair, dirLabel);
			}
		}
		return pairs;
	}

	/**
	 * For each labeldir in common from both left/right maps (labeldir around each pair), select a random orderedpair of touching concepts.
	 * 
	 * @param leftmap
	 * @param rightmap
	 * @param random
	 * @param usedConcepts
	 * @return
	 */
	private static HashMap<String, OrderedPair<String>> extractPairsFromMaps(MapOfSet<String, String> leftmap, MapOfSet<String, String> rightmap, RandomGenerator random,
			HashSet<String> usedConcepts) {
		HashMap<String, OrderedPair<String>> relationToPair = new HashMap<>();
		Set<String> leftDirLabels = leftmap.keySet();
		Set<String> rightDirLabels = rightmap.keySet();
		for (String label : leftDirLabels) {
			if (rightDirLabels.contains(label)) {
				Set<String> leftInputSet = leftmap.get(label);
				Set<String> rightInputSet = rightmap.get(label);

				ArrayList<String> leftNeighbors = setToListExcluding(leftInputSet, usedConcepts);
				ArrayList<String> rightNeighbors = setToListExcluding(rightInputSet, leftInputSet, usedConcepts); // what's on the left can't be on the right

				if (leftNeighbors.isEmpty() || rightNeighbors.isEmpty())
					continue;

				// any of the left concepts can be paired with any of the right concepts
				String leftNeighbor = GraphAlgorithms.getRandomElementFromCollection(leftNeighbors, random);
				String rightNeighbor = GraphAlgorithms.getRandomElementFromCollection(rightNeighbors, random);

				usedConcepts.add(leftNeighbor);
				usedConcepts.add(rightNeighbor);

				OrderedPair<String> pair = new OrderedPair<String>(leftNeighbor, rightNeighbor);
				relationToPair.put(label, pair);
			}
		}
		return relationToPair;
	}

	private static <T> ArrayList<T> setToListExcluding(Set<T> inputSet, Set<T> exclusionSet) {
		ArrayList<T> asList = new ArrayList<>();
		for (T element : inputSet) {
			if (exclusionSet.contains(element))
				continue;
			asList.add(element);
		}
		return asList;
	}

	private static <T> ArrayList<T> setToListExcluding(Set<T> inputSet, Set<T> exclusionSet0, Set<T> exclusionSet1) {
		ArrayList<T> asList = new ArrayList<>();
		for (T element : inputSet) {
			if (exclusionSet0.contains(element) || exclusionSet1.contains(element))
				continue;
			asList.add(element);
		}
		return asList;
	}

	/**
	 * Selects randomly from the inputspace an ordered pair of distinct concepts which have at least one relation (of a given label) in common.
	 * 
	 * @param inputSpace
	 * @param random
	 * @return
	 */
	public static OrderedPair<String> getRandomConceptPair(StringGraph inputSpace, RandomGenerator random) {
		Set<String> concepts = inputSpace.getVertexSet();

		// create a new concept match which has at least one relation with the same label in common
		String leftConcept;
		String rightConcept;
		do {
			leftConcept = GraphAlgorithms.getRandomElementFromCollection(concepts, random);
			do {
				rightConcept = GraphAlgorithms.getRandomElementFromCollection(concepts, random);
			} while (rightConcept.equals(leftConcept));
			// try matching labels from edges OUT/IN
		} while (!(containsCommonEdgeLabels(inputSpace, leftConcept, rightConcept, false) || // ---
				containsCommonEdgeLabels(inputSpace, leftConcept, rightConcept, true)));

		OrderedPair<String> initial = new OrderedPair<String>(leftConcept, rightConcept);
		return initial;
	}

	public static <V, E> OrderedPair<V> getRandomConceptPair(DirectedMultiGraph<V, E> inputSpace, RandomGenerator random) {
		Set<V> concepts = inputSpace.vertexSet();

		// create a new concept match which has at least one relation with the same label in common
		V leftConcept;
		V rightConcept;
		do {
			leftConcept = GraphAlgorithms.getRandomElementFromCollection(concepts, random);
			do {
				rightConcept = GraphAlgorithms.getRandomElementFromCollection(concepts, random);
			} while (rightConcept.equals(leftConcept));
			// try matching labels from edges OUT/IN
		} while (!(containsCommonEdgeLabels(inputSpace, leftConcept, rightConcept, false) || containsCommonEdgeLabels(inputSpace, leftConcept, rightConcept, true)));

		OrderedPair<V> initial = new OrderedPair<V>(leftConcept, rightConcept);
		return initial;
	}

	/**
	 * maps edges according to their label/direction around a reference concept
	 * 
	 * @param reference
	 * @param inputSpace
	 * @param usedConcepts
	 * @return
	 */
	private static MapOfSet<String, String> mapEgdeLabelsDirToNeighbors(String reference, StringGraph inputSpace, HashSet<String> usedConcepts) {
		MapOfSet<String, String> dirLabelMap = new MapOfSet<>();
		Set<StringEdge> edges = inputSpace.edgesOf(reference);
		for (StringEdge edge : edges) {
			String oppositeConcept = edge.getOppositeOf(reference);
			if (usedConcepts.contains(oppositeConcept))
				continue;
			String label = edge.getLabel();
			String dirLabel;
			// check for undirected relations
			if (label.equals("synonym") || //
					label.equals("antonym") || //
					label.equals("relatedto") || //
					label.equals("similarto")) {
				dirLabel = label;
			} else { // directed relations
				if (edge.incomesTo(reference)) {
					dirLabel = "-" + label;
				} else {
					dirLabel = "+" + label;
				}
			}
			dirLabelMap.put(dirLabel, oppositeConcept);
		}
		return dirLabelMap;
	}

	public static void createIsomorphism(StringGraph inputSpace, DirectedMultiGraph<OrderedPair<String>, String> pairGraph, RandomGenerator random, OrderedPair<String> refPair,
			int deepnessLimit) {
		Object2IntOpenHashMap<OrderedPair<String>> pairDeepness = null;
		if (deepnessLimit >= 0) {
			pairDeepness = new Object2IntOpenHashMap<>();
		}
		HashSet<String> closedSet = new HashSet<>();
		HashSet<String> usedConcepts = new HashSet<>();
		ArrayDeque<OrderedPair<String>> openSet = new ArrayDeque<>();
		openSet.addLast(refPair);
		if (deepnessLimit >= 0) {
			pairDeepness.put(refPair, 0);
		}
		// ---------init
		while (!openSet.isEmpty()) {
			OrderedPair<String> currentPair = openSet.removeFirst();
			int nextDeepness = -Integer.MAX_VALUE;
			if (deepnessLimit >= 0) {
				int deepness = pairDeepness.getInt(currentPair);
				if (deepness >= deepnessLimit)
					continue;
				nextDeepness = deepness + 1;
			}
			// expand a vertex not in the closed set
			if (closedSet.contains(currentPair.getLeftElement()) || closedSet.contains(currentPair.getRightElement()))
				continue;
			// get the vertex neighbors not in the closed set
			HashMap<String, OrderedPair<String>> expansion = MappingAlgorithms.expandConceptPair(inputSpace, pairGraph, random, currentPair, usedConcepts);
			for (OrderedPair<String> nextPair : expansion.values()) {
				if (closedSet.contains(currentPair.getLeftElement()) || closedSet.contains(currentPair.getRightElement()))
					continue;
				// put the neighbors in the open set
				openSet.addLast(nextPair);
				if (deepnessLimit >= 0) {
					pairDeepness.put(nextPair, nextDeepness);
				}
			}
			// vertex from the open set explored, remove it from further exploration
			closedSet.add(currentPair.getLeftElement());
			closedSet.add(currentPair.getRightElement());
		}
	}

	public static void updateMappingGraph(StringGraph inputSpace, MappingStructure<String, String> mappingStruct, int deepnessLimit, RandomGenerator random) {
		DirectedMultiGraph<OrderedPair<String>, String> pairGraph = new DirectedMultiGraph<>();
		// create a random mapping using the reference pair
		MappingAlgorithms.createIsomorphism(inputSpace, pairGraph, random, mappingStruct.getRefPair(), deepnessLimit);
		// store results in the chromosome
		mappingStruct.setPairGraph(pairGraph);
	}

	public static HashMap<String, Integer> countRelations(Set<StringEdge> edges) {
		HashMap<String, Integer> counter = new HashMap<>();
		for (StringEdge edge : edges) {
			String relation = edge.getLabel();
			Integer relationCount = counter.get(relation);
			if (relationCount == null) {
				relationCount = Integer.valueOf(1);
				counter.put(relation, relationCount);
			} else {
				relationCount = Integer.valueOf(relationCount.intValue() + 1);
				counter.put(relation, relationCount);
			}
		}
		return counter;
	}

	public static DoubleArrayList compareRelations(HashMap<String, Integer> numerator, HashMap<String, Integer> denominator) {
		DoubleArrayList ratios = new DoubleArrayList();
		for (String dKey : denominator.keySet()) {
			double deCount = denominator.get(dKey).intValue();
			Integer numCount = numerator.get(dKey);
			double ratio;
			if (numCount != null) {
				int nv = numCount.intValue();
				if (nv < deCount)
					ratio = nv / deCount;
				else
					ratio = 1;
			} else
				ratio = 0;
			ratios.add(ratio);
		}
		return ratios;
	}

	public static DoubleArrayList compareEdgesOf(StringGraph inputSpace, StringGraph outputSpace, String concept, boolean incoming) {
		// get its edges
		Set<StringEdge> bcEdges;
		if (incoming)
			bcEdges = outputSpace.incomingEdgesOf(concept);
		else
			bcEdges = outputSpace.outgoingEdgesOf(concept);

		HashMap<String, Integer> osRelations = countRelations(bcEdges);
		HashMap<String, Integer> isRelations = new HashMap<>();

		// get matching node(s) in the input space
		if (incoming) {
			isRelations.putAll(countRelations(inputSpace.incomingEdgesOf(concept)));
		} else {
			isRelations.putAll(countRelations(inputSpace.outgoingEdgesOf(concept)));
		}
		// count edges / relations and compare between the blend and the inputspace
		DoubleArrayList currentRatios = compareRelations(osRelations, isRelations);
		return currentRatios;
	}

	public static void scoreTopology(StringGraph inputSpace, StringGraph outputSpace) {
		Set<String> outputSpaceConcepts = outputSpace.getVertexSet();
		DoubleArrayList ratios = new DoubleArrayList();
		if (outputSpaceConcepts.isEmpty()) {
			ratios.add(0);
		} else {
			// for each concept in the blend
			for (String concept : outputSpaceConcepts) {
				DoubleArrayList incomingRatios = compareEdgesOf(inputSpace, outputSpace, concept, true);
				DoubleArrayList outgoingRatios = compareEdgesOf(inputSpace, outputSpace, concept, false);
				ratios.addAll(incomingRatios);
				ratios.addAll(outgoingRatios);
			}
		}

		DescriptiveStatistics ds = new DescriptiveStatistics(ratios.toDoubleArray());
		double topologyMean = ds.getMean();
		double topologyStdDev = ds.getStandardDeviation();
		// scoreMap.put("topologyMean", Double.toString(topologyMean));
		// scoreMap.put("topologyStdDev", Double.toString(topologyStdDev));
	}

}
