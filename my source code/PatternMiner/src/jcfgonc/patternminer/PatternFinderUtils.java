package jcfgonc.patternminer;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;

import graph.GraphAlgorithms;
import graph.StringEdge;
import graph.StringGraph;
import structures.ListOfSet;

public class PatternFinderUtils {
	public static StringGraph mutatePattern(final StringGraph kbGraph, final RandomGenerator random, StringGraph pattern, final boolean forceAdd) {
		// TODO: detect if pattern has not changed

		// decide if adding an edge or removing existing
		boolean patternEmpty = pattern.getVertexSet().isEmpty();
		if (random.nextBoolean() || patternEmpty || forceAdd) { // add
			if (patternEmpty) {
				// add a random edge
				StringEdge edge = GraphAlgorithms.getRandomElementFromCollection(kbGraph.edgeSet(), random);
				pattern.addEdge(edge);
			} else {
				// get an existing edge and add a random connected edge
				StringEdge existingedge = GraphAlgorithms.getRandomElementFromCollection(pattern.edgeSet(), random);
				// add a new edge to existing source
				if (random.nextBoolean()) {
					String source = existingedge.getSource();
					Set<StringEdge> edgesOf = kbGraph.edgesOf(source);

					StringEdge edge = GraphAlgorithms.getRandomElementFromCollection(edgesOf, random);
					pattern.addEdge(edge);
				} else {
					// add a new edge to existing targets
					String target = existingedge.getTarget();
					Set<StringEdge> edgesOf = kbGraph.edgesOf(target);

					StringEdge edge = GraphAlgorithms.getRandomElementFromCollection(edgesOf, random);
					pattern.addEdge(edge);
				}
			}
		} else { // remove
			Set<StringEdge> patternEdgeSet = pattern.edgeSet();
			StringEdge toRemove = GraphAlgorithms.getRandomElementFromCollection(patternEdgeSet, random);
			pattern.removeEdge(toRemove);
			if (pattern.numberOfEdges() > 1) {
				// after removing check for components and leave the biggest
				ListOfSet<String> components = GraphAlgorithms.extractGraphComponents(pattern);
				// TODO: check impact of choosing largest component, smallest, random, etc.
				// HashSet<String> largestComponent = components.getSetAt(0);
				HashSet<String> largestComponent = components.getRandomSet(random);
				// filter pattern with mask
				pattern = new StringGraph(pattern, largestComponent);
			}
		}
		return pattern;
	}

}
