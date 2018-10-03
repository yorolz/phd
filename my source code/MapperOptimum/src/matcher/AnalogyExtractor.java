package matcher;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;

import graph.GraphReadWrite;
import graph.StringGraph;
import structures.MapOfSet;

public class AnalogyExtractor {

	public final static boolean debug = Config.DEBUG;

	/**
	 * process starts here
	 *
	 * @param baseMapping
	 *
	 * @param vertexDeepness
	 * @param leftToRightCorrespondences
	 * @param breadth
	 * @param writeCombinationsFile
	 * @param ancestor
	 * @return
	 * @throws IOException
	 */
	public static Set<Mapping<String>> execute(ArrayList<Mapping<String>> root, HashMap<String, Integer> vertexDeepness, MapOfSet<String, String> leftToRightCorrespondences,
			HashMap<String, String> leftAncestor, MatchingBreadthStep breadth, boolean writeCombinationsFile) throws IOException {
		if (debug)
			System.out.println(new Throwable().getStackTrace()[0].toString());

		MapOfSet<String, String> leftSucessors = getSucessorMap(leftAncestor); // map of vertex to set of sucessors
		SimpleDirectedGraph<ArrayList<Mapping<String>>, DefaultEdge> analogyGraph = new SimpleDirectedGraph<>(DefaultEdge.class);

		ArrayDeque<ArrayList<Mapping<String>>> queue = new ArrayDeque<>(); // openset contains the set of mapping combinations to be expanded
		queue.addLast(root);

		while (!queue.isEmpty()) {
			ArrayList<Mapping<String>> currentCombination = queue.pollFirst();

			// "baseMappings" contains a set of mappings (combination of mappings) which correspond to a vertex in the combination graph
			ArrayList<ArrayList<Mapping<String>>> newLevelCombinations = getLevelCombinations(currentCombination, leftToRightCorrespondences, leftSucessors, breadth);

			if (debug)
				System.out.printf("%s\n", currentCombination);

			for (ArrayList<Mapping<String>> newMappingCombo : newLevelCombinations) {

				// must be ordered so that it matches in multiple passages (because it's a label in the graph)
				ArrayList<Mapping<String>> sortedMappingCombo = sortCombination(newMappingCombo);
				if (debug)
					System.out.printf("\t%s\n", sortedMappingCombo);

				// create the combo edge in the combo graph
				analogyGraph.addVertex(currentCombination);
				analogyGraph.addVertex(sortedMappingCombo);
				analogyGraph.addEdge(currentCombination, sortedMappingCombo);
				// new combos are to be expanded further
				queue.addLast(sortedMappingCombo);
				
				if (StaticTimer.timedOut())
					break;
			}
			
			if (StaticTimer.timedOut())
				break;
			
		}

		if (writeCombinationsFile)
			writeTGF("combinations.tgf", analogyGraph);

		// combo and related combo ancestor
		HashMap<ArrayList<Mapping<String>>, ArrayList<Mapping<String>>> ancestorUpdated = new HashMap<>();
		// leaf is a final combination, or a leaf in the combo graph
		HashSet<ArrayList<Mapping<String>>> leafs = new HashSet<>();
		expandDFS(root, analogyGraph, leafs, ancestorUpdated);
		Set<Mapping<String>> bestAnalogy = getBestAnalogy(leafs, ancestorUpdated);

		// HashSet<Mapping<String>> analogy = LargestAnalogyExtractor.execute(analogyGraph, root);
		// from here just merge all and write to a file
		// HashSet<String> analogyConcepts = createSetConcepts(analogy);
		// StringGraph analogyGraph = createAnalogyGraph(graph, analogyConcepts);
		// GraphReadWrite.writeTGF("analogy.tgf", analogyGraph);
		return bestAnalogy;
	}

	private static Set<Mapping<String>> getBestAnalogy(HashSet<ArrayList<Mapping<String>>> leafs, HashMap<ArrayList<Mapping<String>>, ArrayList<Mapping<String>>> ancestorUpdated) {
		// leaf is a final combination, or a leaf in the combo graph
		MapOfSet<ArrayList<Mapping<String>>, Mapping<String>> analogiesByLeaf = createAllAnalogies(leafs, ancestorUpdated);
		ArrayList<Mapping<String>> largestLeaf = null;
		int largestLeafSize = -1;

		for (ArrayList<Mapping<String>> leaf : leafs) {
			Set<Mapping<String>> set = analogiesByLeaf.get(leaf);
			if (set != null && set.size() > largestLeafSize) {
				largestLeaf = leaf;
				largestLeafSize = set.size();
			}
		}
		return analogiesByLeaf.get(largestLeaf);
	}

	private static MapOfSet<ArrayList<Mapping<String>>, Mapping<String>> createAllAnalogies(HashSet<ArrayList<Mapping<String>>> leafs,
			HashMap<ArrayList<Mapping<String>>, ArrayList<Mapping<String>>> ancestorUpdated) {

		MapOfSet<ArrayList<Mapping<String>>, Mapping<String>> analogiesByLeaf = new MapOfSet<>();

		for (ArrayList<Mapping<String>> leaf : leafs) {
			ArrayList<Mapping<String>> currentCombination = leaf;
			ArrayList<Mapping<String>> ancestorCombination;
			do {
				// System.out.println(currentCombination);
				analogiesByLeaf.put(leaf, currentCombination);
				ancestorCombination = ancestorUpdated.get(currentCombination);
				currentCombination = ancestorCombination;
			} while (ancestorCombination != null);
			// System.out.println("-----------");
		}
		return analogiesByLeaf;
	}

	private static void expandDFS(ArrayList<Mapping<String>> root, SimpleDirectedGraph<ArrayList<Mapping<String>>, DefaultEdge> graph, HashSet<ArrayList<Mapping<String>>> leafs,
			HashMap<ArrayList<Mapping<String>>, ArrayList<Mapping<String>>> ancestorUpdated) {

		ArrayDeque<ArrayList<Mapping<String>>> queue = new ArrayDeque<>();
		HashSet<ArrayList<Mapping<String>>> closedSet = new HashSet<>();
		queue.push(root);

		// ---------------- left queue loop
		while (!queue.isEmpty()) {
//			if (StaticTimer.timedOut())
//				break;

			ArrayList<Mapping<String>> currentVertex = queue.pop();

			if (!closedSet.contains(currentVertex)) {

				closedSet.add(currentVertex);
				// expand relation edge in left
				Set<DefaultEdge> outgoingEdgesOf = null;
				if (graph.containsVertex(currentVertex))
					outgoingEdgesOf = graph.outgoingEdgesOf(currentVertex);
				if (outgoingEdgesOf != null && !outgoingEdgesOf.isEmpty()) {

					for (DefaultEdge outgoingEdge : outgoingEdgesOf) {

						ArrayList<Mapping<String>> expandedNeighbor = graph.getEdgeTarget(outgoingEdge);
						if (!closedSet.contains(expandedNeighbor)) {

							// store neighbor level and precedence
							ancestorUpdated.put(expandedNeighbor, currentVertex);

							queue.push(expandedNeighbor);
							// queue.addFirst(expandedNeighbor);
						}
					}
				} else {
					leafs.add(currentVertex);
				}
			}

		}
	}

	/**
	 * TODO: here it is where it takes a while
	 *
	 * @param mappingSet
	 * @return
	 */
	private static <T> ArrayList<ArrayList<Mapping<T>>> createGroupsOfCombinations(ArrayList<Mapping<T>> mappingSet) {
		if (mappingSet.isEmpty())
			return new ArrayList<>();
		// long t0 = System.nanoTime();
		ArrayList<ArrayList<Mapping<T>>> conceptGroups = new ArrayList<>();
		{
			MapOfSet<T, Mapping<T>> leftIndexing = new MapOfSet<>();
			MapOfSet<T, Mapping<T>> rightIndexing = new MapOfSet<>();

			// create two maps relating either left or right concepts to corresponding mapping
			for (Mapping<T> mapping : mappingSet) {
				T leftConcept = mapping.getLeftConcept();
				T rightConcept = mapping.getRightConcept();

				leftIndexing.put(leftConcept, mapping);
				rightIndexing.put(rightConcept, mapping);
			}

			// group combinations according to l/r concepts in either left or right map indexes
			for (Mapping<T> mapping : mappingSet) {
				T leftConcept = mapping.getLeftConcept();
				T rightConcept = mapping.getRightConcept();

				Set<Mapping<T>> lSet = leftIndexing.get(leftConcept);
				Set<Mapping<T>> rSet = rightIndexing.get(rightConcept);

				if (lSet.size() > rSet.size()) {
					rSet.remove(mapping);
				} else {
					lSet.remove(mapping);
				}
				// now the sets may be empty, remove them from indexing
				if (lSet.size() == 0) {
					leftIndexing.removeKey(leftConcept);
				}
				if (rSet.size() == 0) {
					rightIndexing.removeKey(rightConcept);
				}
			}

			{
				Collection<Set<Mapping<T>>> lSet = leftIndexing.values();
				Collection<Set<Mapping<T>>> rSet = rightIndexing.values();

				for (Set<Mapping<T>> mappingGroup : lSet) {
					ArrayList<Mapping<T>> block = new ArrayList<>();
					for (Mapping<T> mapping : mappingGroup) {
						block.add(mapping);
					}
					conceptGroups.add(block);
				}
				for (Set<Mapping<T>> mappingGroup : rSet) {
					ArrayList<Mapping<T>> block = new ArrayList<>();
					for (Mapping<T> mapping : mappingGroup) {
						block.add(mapping);
					}
					conceptGroups.add(block);
				}
			}
		}

		// conceptGroups now has groups of combinations grouped consistently by left or right concepts (but possible incorrect combinations)
		// ie [(a,1), (a,2)], [(b,2), (b,1)], [(c,2), (c,1)], [(d,1), (d,2)], [(e,1), (e,2)], [(f,1), (f,2)], [(g,1), (g,2)]

		int numberGroups = conceptGroups.size();
		int[] indices = new int[numberGroups];
		int[] possibilitiesPerGroup = new int[numberGroups];
		for (int i = 0; i < numberGroups; i++) {
			possibilitiesPerGroup[i] = conceptGroups.get(i).size();
		}

		// System.out.println("numberGroups = " + numberGroups);

		// possibilitiesPerGroup contains the number of possibilites (mappings) per group in the conceptGroups list
		// ie [2, 2, 2, 2, 2, 2, 2] for the above example or [2, 1 ,2] for your revealertest
		boolean combinationsToBeDone = true;
		ArrayList<ArrayList<Mapping<T>>> allCombinations = new ArrayList<>();
		while (combinationsToBeDone) {
			boolean carry = false;

			ArrayList<Mapping<T>> combination = new ArrayList<>();
			boolean validCombination = true;
			HashSet<T> existingConcepts = new HashSet<>();

			for (int curGroup = 0; curGroup < numberGroups; curGroup++) {
				// to start increasing from zero
				if (curGroup == 0)
					carry = true;

				int curIndex = indices[curGroup];
				Mapping<T> mapping = conceptGroups.get(curGroup).get(curIndex);
				// increase group index if there is a carry
				if (carry) {
					curIndex++;
					carry = false;
					if (curIndex >= possibilitiesPerGroup[curGroup]) {
						// overflow in current group, reset and carry to next group
						curIndex = 0;
						carry = true;
					}
					indices[curGroup] = curIndex;
				}

				if (!validCombination)
					continue;

				T leftConcept = mapping.getLeftConcept();
				T rightConcept = mapping.getRightConcept();

				if (existingConcepts.contains(leftConcept) || existingConcepts.contains(rightConcept)) {
					validCombination = false;
				} else {
					existingConcepts.add(leftConcept);
					existingConcepts.add(rightConcept);
					combination.add(mapping);
				}
				
				if (StaticTimer.timedOut())
					break;
				
			}
			if (validCombination) {
				allCombinations.add(combination);
				// System.out.println(combination);
			}
			if (carry)
				combinationsToBeDone = false;
			
			if (StaticTimer.timedOut())
				break;
			
		}
		// long t1 = System.nanoTime();
		// double dt = (double) (t1 - t0) / (1000000000.0);
		// System.out.printf("createGroupsOfCombinations() took %f seconds\n", dt);
		return allCombinations;
	}

	/**
	 * Given a set of (a,b) returns a set of combinations of mappings descended from (a,b) (at the next level from (a,b).
	 * <p>
	 * Example: given {(2,22)} returns {(3,28), (8,23), (1,21)} U {(1,23), (3,28), (8,21)}
	 *
	 * @param currentLevelMappings
	 * @param leftToRightCorrespondences
	 * @param leftSucessors
	 * @param breadt
	 * @return
	 */
	private static ArrayList<ArrayList<Mapping<String>>> getLevelCombinations(ArrayList<Mapping<String>> currentLevelMappings, MapOfSet<String, String> leftToRightCorrespondences,
			MapOfSet<String, String> leftSucessors, MatchingBreadthStep breadth) {
		// System.out.println(new Throwable().getStackTrace()[0].toString());
		// {(,)} ->
		// {(,)...(,)} ;...; {(,)...(,)} ie {(2,22)} for level 0
		ArrayList<Mapping<String>> levelCombinations = new ArrayList<>();
		for (Mapping<String> curmap : currentLevelMappings) {
			// curmap = (2, 22)
			ArrayList<Mapping<String>> expanded = getMappings(curmap, leftToRightCorrespondences, leftSucessors, breadth);
			if (expanded.isEmpty())
				continue;
			levelCombinations.addAll(expanded);
		}

		// generate combinations from the all the extracted mappings in this level
		ArrayList<ArrayList<Mapping<String>>> combinations = createGroupsOfCombinations(levelCombinations);
		return combinations;
	}

	private static ArrayList<Mapping<String>> getMappings(Mapping<String> baseAnalogy, MapOfSet<String, String> leftToRightCorrespondences,
			MapOfSet<String, String> leftSucessorsMap, MatchingBreadthStep breadth) {

		ArrayList<Mapping<String>> levelMappings = new ArrayList<>();
		String leftConcept = baseAnalogy.getLeftConcept(); // 2
		String rightConcept = baseAnalogy.getRightConcept(); // 22

		Set<String> successors = leftSucessorsMap.get(leftConcept);
		// the left concept may not have a sucessor/sucessors
		if (successors != null && !successors.isEmpty()) {
			for (String leftSuccessor : successors) { // 2, {1, 3, 8}

				Set<String> rightMatches = leftToRightCorrespondences.get(leftSuccessor); // 1, {21, 23}

				// the left sucessor concept may not have a match to the right concept tree
				if (rightMatches != null && !rightMatches.isEmpty()) {

					for (String rightMatch : rightMatches) {
						// ver se do lado direito (rightConcept), algum dos matches tem como pai um no que mapeia para o leftConcept
						HashMap<String, String> rightAncestorMap = breadth.getAncestor();
						String rightAncestor = rightAncestorMap.get(rightMatch);
						if (rightAncestor.equals(rightConcept)) {
							Mapping<String> mapping = new Mapping<String>(leftSuccessor, rightMatch);
							levelMappings.add(mapping);
						}
					}
				}
			}
		}
		return levelMappings;
	}

	/**
	 * Returns a mapping of ancestor concept to set of sucessor concepts
	 *
	 * @param leftAncestor
	 * @return
	 */
	private static MapOfSet<String, String> getSucessorMap(HashMap<String, String> leftAncestor) {
		// System.out.println(new Throwable().getStackTrace()[0].toString());
		MapOfSet<String, String> sucessors = new MapOfSet<>();
		for (String vertex : leftAncestor.keySet()) {
			String ancestor = leftAncestor.get(vertex);
			sucessors.put(ancestor, vertex);
		}
		return sucessors;
	}

	static Double parseDouble(String s) {
		try {
			double d = Double.parseDouble(s);
			return Double.valueOf(d);
		} catch (NumberFormatException e) {
		}
		return null;
	}

	/**
	 * Sorts the given list of mappings (a combination of) into ascending order (left concept first).
	 *
	 * @param comb
	 *            the given list of mappings
	 * @return a new sorted list of mappings
	 */
	public static ArrayList<Mapping<String>> sortCombination(ArrayList<Mapping<String>> comb) {
		ArrayList<Mapping<String>> asList = new ArrayList<>(comb);

		asList.sort(new Comparator<Mapping<String>>() {

			@Override
			public int compare(Mapping<String> o1, Mapping<String> o2) {

				// try both as number
				Double o1ld = parseDouble(o1.getLeftConcept());
				Double o1rd = parseDouble(o1.getRightConcept());
				Double o2ld = parseDouble(o2.getLeftConcept());
				Double o2rd = parseDouble(o2.getRightConcept());

				int compL;
				int compR;

				// number
				if (o1ld != null && o1rd != null && o2ld != null && o2rd != null) {
					compL = o1ld.compareTo(o2ld);
					compR = o1rd.compareTo(o2rd);
				} else { // or string
					compL = o1.getLeftConcept().compareTo(o2.getLeftConcept());
					compR = o1.getRightConcept().compareTo(o2.getRightConcept());
				}

				if (compL != 0) { // left concept not equal
					return compL;
				}
				return compR; // compare right concepts
			}
		});
		return asList;
	}

	public static <V, E> void writeTGF(String filename, SimpleDirectedGraph<ArrayList<Mapping<String>>, DefaultEdge> graph) throws IOException {
		// store vertices
		// store edges
		// ---------- example
		// 1 bird
		// 2 solid
		// 3 mobile
		// #
		// 1 2 property
		// 1 3 property
		Set<DefaultEdge> edgeSet = graph.edgeSet();
		StringGraph graphAsString = new StringGraph();
		for (DefaultEdge edge : edgeSet) {
			String edgeSource = graph.getEdgeSource(edge).toString();
			String edgeTarget = graph.getEdgeTarget(edge).toString();
			graphAsString.addEdge(edgeSource, edgeTarget, "");
		}

		GraphReadWrite.writeTGF(filename, graphAsString);
	}

}
