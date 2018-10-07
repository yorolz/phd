package study;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

import org.apache.commons.math3.random.Well44497a;
import org.jpl7.Query;

import com.declarativa.interprolog.PrologEngine;
import com.xsb.interprolog.NativeEngine;

import graph.GraphAlgorithms;
import graph.StringEdge;
import graph.StringGraph;
import structures.ObjectIndex;
import structures.Ticker;

public class PatternFinderXSBProlog {

	public static void findPatterns(StringGraph graph) {
		PrologEngine engine = new NativeEngine();
		ObjectIndex<String> concepts = new ObjectIndex<>();
		createKnowledgeBase(graph, concepts, engine);

		System.exit(0);

		Well44497a random = new Well44497a(1); // fixed seed for experimentation
		StringGraph pattern = new StringGraph();

		// generate a graph pattern
		// do {
		mutatePattern(graph, random, pattern);
		mutatePattern(graph, random, pattern);
		mutatePattern(graph, random, pattern);
		// match the pattern in the graph
		int count = countPatternMatches(pattern);
		System.out.println(pattern.toString() + Character.LINE_SEPARATOR + " -> " + count);
		// } while (true);

		// System.lineSeparator();
	}

	private static void createKnowledgeBase(StringGraph graph, ObjectIndex<String> concepts, PrologEngine engine) {
		Ticker t = new Ticker();
		t.getTimeDeltaLastCall();
		System.out.println("creating XSB KB...");
		int counter = 0;
		for (StringEdge edge : graph.edgeSet()) {
			String source = edge.getSource();
			String relation = edge.getLabel();
			String target = edge.getTarget();

			int si = concepts.addObject(source);
			int ti = concepts.addObject(target);

			String fact = String.format("assertz(%s(%s,%s))", relation, si, ti);
			boolean command = engine.deterministicGoal(fact);
			System.lineSeparator();
			counter++;
			if (counter > 1 << 14)
				break;
		}
		System.out.println("XSB KB creation took " + t.getTimeDeltaLastCall() + " s");
	}

	private static void mutatePattern(StringGraph graph, Well44497a random, StringGraph pattern) {
		// TODO: decide if adding an edge or removing existing
		// if deleting, after removing check for components and leave the biggest
		if (pattern.getVertexSet().isEmpty()) {
			// add a random edge
			StringEdge edge = GraphAlgorithms.getRandomElementFromCollection(graph.edgeSet(), random);
			pattern.addEdge(edge);
		} else {
			// get an existing edge and add a random connected edge
			StringEdge existingedge = GraphAlgorithms.getRandomElementFromCollection(pattern.edgeSet(), random);
			// add a new edge to existing source
			if (random.nextBoolean()) {
				String source = existingedge.getSource();
				Set<StringEdge> edgesOf = graph.edgesOf(source);

				StringEdge edge = GraphAlgorithms.getRandomElementFromCollection(edgesOf, random);
				pattern.addEdge(edge);
			} else {
				// add a new edge to existing targets
				String target = existingedge.getTarget();
				Set<StringEdge> edgesOf = graph.edgesOf(target);

				StringEdge edge = GraphAlgorithms.getRandomElementFromCollection(edgesOf, random);
				pattern.addEdge(edge);
			}
		}
	}

	private static int countPatternMatches(StringGraph pattern) {
		HashMap<String, String> conceptToVariable = new HashMap<>();
		// replace each concept in the pattern to a variable
		int varCounter = 0;
		for (String concept : pattern.getVertexSet()) {
			String varName = "X" + varCounter;
			conceptToVariable.put(concept, varName);
			varCounter++;
		}
		// convert each edge to a predicate
		String query = "";
		Iterator<StringEdge> edgeIterator = pattern.edgeSet().iterator();
		while (edgeIterator.hasNext()) {
			StringEdge edge = edgeIterator.next();

			String edgeLabel = edge.getLabel();
			String sourceVar = conceptToVariable.get(edge.getSource());
			String targetVar = conceptToVariable.get(edge.getTarget());

			query += String.format("%s(%s,%s)", edgeLabel, sourceVar, targetVar);

			if (edgeIterator.hasNext())
				query += ",";
		}
		int matches = queryPattern(query, 1 << 16, false, 1 << 28);
		return matches;
	}

	private static int queryPattern(final String query, final int loopUnrollSize, final boolean nextSolutionCheck, final int solutionLimit) {
		System.out.println("querying " + query + " ...");
		Query q = new Query(query);
		int matches = 0;
		// try all answers
		// Ticker t = new Ticker();
		while (q.hasMoreSolutions()) {
			for (int i = 0; i < loopUnrollSize; i++) {
				q.nextSolution();
				if (nextSolutionCheck && !q.hasMoreSolutions())
					break;
			}
			matches += loopUnrollSize;
			if (matches > solutionLimit)
				break;
			// if (t.getElapsedTime() > 60)
			// break;
		}
		// System.out.println("query took " + t.getElapsedTime() + " with " + matches + " matches");
		return matches;
	}

}
