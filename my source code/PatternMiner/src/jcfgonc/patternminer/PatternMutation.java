package jcfgonc.patternminer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;

import graph.GraphAlgorithms;
import graph.StringEdge;
import graph.StringGraph;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

public class PatternMutation {

	/**
	 * mutates the pattern IN-PLACE
	 * 
	 * @param kbGraph
	 * @param random
	 * @param pattern
	 * @param forceAdd
	 */
	public static void mutation(final StringGraph kbGraph, final RandomGenerator random, StringGraph pattern, final boolean forceAdd) {
		// decide if adding an edge or removing existing
		boolean isEmpty = pattern.isEmpty();
		boolean addEdge = pattern.numberOfEdges() < 8 && (random.nextBoolean() || isEmpty || forceAdd);
		if (addEdge) { // add
			if (isEmpty) {
				// add a random edge
				mutationAddRandomEdge(kbGraph, random, pattern);
			} else {
				boolean cycleAdded = false;
				if (PatternMinerConfig.FORCE_CYCLES) {
					// add an edge which makes a cycle
					cycleAdded = mutationMakeCycle(kbGraph, random, pattern);
				}
				// did not make a cycle, add then an edge
				if (!cycleAdded) {
					// try to keep generalizing the pattern by adding an edge with a different relation
					// get all KB edges touching the pattern's vertices and filter edges having the the same existing relations
					addRelationWithRarestLabel(random, pattern, GraphAlgorithms.countRelations(pattern), kbGraph.edgesOf(pattern.getVertexSet()));
				}
			}
		} else { // remove
			// try to remove a terminal edge first, otherwise remove any edge
			Set<StringEdge> edges;
			HashSet<StringEdge> terminalEdges = getTerminalEdges(pattern);
			if (!terminalEdges.isEmpty()) {
				edges = terminalEdges;
			} else {
				edges = pattern.edgeSet();
			}
			// try to remove an edge with a common label
			HashSet<StringEdge> edgesFrequent = filterEdges(edges, getMostFrequentLabels(GraphAlgorithms.countRelations(edges)));
			pattern.removeEdge(GraphAlgorithms.getRandomElementFromCollection(edgesFrequent, random));
		}

		// pattern could be empty, reinitialize it
		if (pattern.isEmpty()) {
			pattern.clear();
			pattern.addEdges(PatternFinderUtils.initializePattern(kbGraph, new StringGraph(), random));
		}
	}

	/**
	 * tries to make a cycle, i.e., add an edge which will close a path in the pattern and thus making a cycle
	 * 
	 * @param kbGraph
	 * @param random
	 * @param pattern
	 * @return
	 */
	private static boolean mutationMakeCycle(final StringGraph kbGraph, final RandomGenerator random, StringGraph pattern) {
		if (pattern.numberOfVertices() >= 3 /* && random.nextFloat() > 0.5 */) {
			// System.err.println("### trying to add a cycle");
			ArrayList<String> vertices = new ArrayList<>(pattern.getVertexSet());
			GraphAlgorithms.shuffleArrayList(vertices, random);
			// try to connect randomly a pair of vertices
			for (int i = 0; i < vertices.size(); i++) {
				String v0 = vertices.get(i);
				for (int j = i + 1; j < vertices.size(); j++) {
					String v1 = vertices.get(j);
					// make sure the pair is not yet connected
					Set<StringEdge> pedges = pattern.getUndirectedEdgesConnecting(v0, v1);
					if (pedges.isEmpty()) {
						// and make sure the pair can be connected
						Set<StringEdge> kbEdges = kbGraph.getUndirectedEdgesConnecting(v0, v1);
						if (!kbEdges.isEmpty()) { // can be connected with knowledge from the kb
							Object2IntOpenHashMap<String> relationCount = GraphAlgorithms.countRelations(pattern);
							addRelationWithRarestLabel(random, pattern, relationCount, kbEdges);
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	/**
	 * picks a random edge from the kbGraph and adds it to the pattern
	 * 
	 * @param kbGraph
	 * @param random
	 * @param pattern
	 */
	private static void mutationAddRandomEdge(final StringGraph kbGraph, final RandomGenerator random, StringGraph pattern) {
		pattern.addEdge(GraphAlgorithms.getRandomElementFromCollection(kbGraph.edgeSet(), random));
	}

	/**
	 * returns the edges with terminal vertices (with at least one vertex with degree=1)
	 * 
	 * @param pattern
	 * @return
	 */
	private static HashSet<StringEdge> getTerminalEdges(StringGraph pattern) {
		HashSet<StringEdge> terminalEdges = new HashSet<StringEdge>();
		for (StringEdge edge : pattern.edgeSet()) {
			int degreeSource = pattern.degreeOf(edge.getSource());
			int degreeTarget = pattern.degreeOf(edge.getTarget());
			if (degreeSource == 1 || degreeTarget == 1) {
				terminalEdges.add(edge);
			}
		}
		return terminalEdges;
	}

	/**
	 * returns a map with only the keys present in the given set
	 * 
	 * @param relationCount
	 * @param labels
	 * @return
	 */
	private static Object2IntOpenHashMap<String> filterMapKeys(Object2IntOpenHashMap<String> relationCount, Set<String> labels) {
		Object2IntOpenHashMap<String> patternRelationCountFiltered = new Object2IntOpenHashMap<String>();
		for (String label : relationCount.keySet()) {
			if (labels.contains(label)) {
				patternRelationCountFiltered.put(label, relationCount.getInt(label));
			}
		}
		return patternRelationCountFiltered;
	}

	/**
	 * returns the edges whose label is not in the given label set
	 * 
	 * @param edges
	 * @param labels
	 * @return
	 */
	private static HashSet<StringEdge> filterEdgesExcludingLabel(Set<StringEdge> edges, Set<String> labels) {
		HashSet<StringEdge> filtered = new HashSet<StringEdge>();
		for (StringEdge edge : edges) {
			if (!labels.contains(edge.getLabel())) {
				filtered.add(edge);
			}
		}
		return filtered;
	}

	/**
	 * returns the edges whose label is in the given label set
	 * 
	 * @param edges
	 * @param labels
	 * @return
	 */
	private static HashSet<StringEdge> filterEdges(Set<StringEdge> edges, Set<String> labels) {
		HashSet<StringEdge> filtered = new HashSet<>(edges.size());
		for (StringEdge edge : edges) {
			if (labels.contains(edge.getLabel()))
				filtered.add(edge);
		}
		return filtered;
	}

	/**
	 * returns the labels with the lowest count (could be more than one with the same low count)
	 * 
	 * @param relationCount
	 * @return
	 */
	private static HashSet<String> getLeastFrequentLabels(Object2IntOpenHashMap<String> relationCount) {
		int smallest = Integer.MAX_VALUE;
		// because there can be multiple labels with the same (smallest) frequency, two scans are required
		// first scan for the smallest
		for (String curLabel : relationCount.keySet()) {
			int count = relationCount.getInt(curLabel);
			if (count < smallest) {
				smallest = count;
			}
		}
		HashSet<String> labels = new HashSet<>();
		// now scan for all the labels containing that count
		for (String curLabel : relationCount.keySet()) {
			int count = relationCount.getInt(curLabel);
			if (count == smallest) {
				labels.add(curLabel);
			}
		}
		return labels;
	}

	/**
	 * returns the labels with the highest count (could be more than one with the same high count)
	 * 
	 * @param relationCount
	 * @return
	 */
	private static HashSet<String> getMostFrequentLabels(Object2IntOpenHashMap<String> relationCount) {
		int largest = -Integer.MAX_VALUE;
		// because there can be multiple labels with the same (largest) frequency, two scans are required
		// first scan for the largest
		for (String curLabel : relationCount.keySet()) {
			int count = relationCount.getInt(curLabel);
			if (count > largest) {
				largest = count;
			}
		}
		HashSet<String> labels = new HashSet<>();
		// now scan for all the labels containing that count
		for (String curLabel : relationCount.keySet()) {
			int count = relationCount.getInt(curLabel);
			if (count == largest) {
				labels.add(curLabel);
			}
		}
		return labels;
	}

	private static void addRelationWithRarestLabel(final RandomGenerator random, StringGraph pattern, Object2IntOpenHashMap<String> patternRelationCount, Set<StringEdge> kbEdges) {
		HashSet<StringEdge> edgesWithDifferentLabels = filterEdgesExcludingLabel(kbEdges, patternRelationCount.keySet());
		if (!edgesWithDifferentLabels.isEmpty())
		// we can add an edge with a different label, so add a random one
		{
			StringEdge edge = GraphAlgorithms.getRandomElementFromCollection(edgesWithDifferentLabels, random);
			pattern.addEdge(edge);
		} else
		// unable to add an edge with a different label than the ones existing
		// add the label with the lowest presence in the pattern
		{
			// get the kb's edge labels
			HashSet<String> kbLabels = GraphAlgorithms.getEdgesLabelsAsSet(kbEdges);
			// make sure the histogram only contains the labels present in kbEdges
			Object2IntOpenHashMap<String> patternRelationCountFiltered = filterMapKeys(patternRelationCount, kbLabels);
			HashSet<String> rarestLabels = getLeastFrequentLabels(patternRelationCountFiltered);
			HashSet<StringEdge> rarestLabelEdges = filterEdges(kbEdges, rarestLabels);

			if (rarestLabelEdges.isEmpty()) {
				System.out.println(kbEdges + "\t" + rarestLabels);
//				System.out.println(kbEdges);// [ca_wouters,notableidea,habitus]
//				System.out.println(pattern);// ca_wouters,influencedby,norbert_elia;pierre_bourdieu,notableidea,habitus;pierre_bourdieu,maininterest,power;pierre_bourdieu,influencedby,norbert_elia;norbert_elia,notableidea,habitus;norbert_elia,influencedby,karl_mannheim;
//				System.out.println(patternRelationCountFiltered); // {influencedby=>3, maininterest=>1, notableidea=>2}
//				System.out.println(rarestLabels); // [maininterest]
//				System.out.println(rarestLabelEdges); // []
				System.exit(-1);
			}

			StringEdge edge = GraphAlgorithms.getRandomElementFromCollection(rarestLabelEdges, random);
			pattern.addEdge(edge);
		}
	}
}
