package study;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

import org.apache.commons.math3.random.Well44497a;

import graph.GraphAlgorithms;
import graph.StringEdge;
import graph.StringGraph;
import se.sics.jasper.SICStus;
import se.sics.jasper.SPException;
import se.sics.jasper.SPQuery;
import structures.ObjectIndex;
import structures.Ticker;

public class PatternFinderSICStus {

	public static void findPatterns(StringGraph graph) throws SPException {
		System.out.println("using PatternFinderSICStus");

		SICStus sp = new SICStus();
		ObjectIndex<String> concepts = new ObjectIndex<>();
		createKnowledgeBase(graph, concepts, sp);

		Well44497a random = new Well44497a(1); // fixed seed for experimentation
		StringGraph pattern = new StringGraph();

		// generate a graph pattern
		// do {
		mutatePattern(graph, random, pattern);
		mutatePattern(graph, random, pattern);
		mutatePattern(graph, random, pattern);
		// match the pattern in the graph
		int count = countPatternMatches(pattern, sp);
		System.out.println(pattern.toString() + Character.LINE_SEPARATOR + " -> " + count);
		// } while (true);

		// System.lineSeparator();
	}

	private static void createKnowledgeBase(StringGraph graph, ObjectIndex<String> concepts, SICStus sp) throws SPException {
		Ticker t = new Ticker();
		t.getTimeDeltaLastCall();
		System.out.println("creating KB...");

		for (StringEdge edge : graph.edgeSet()) {
			String source = edge.getSource();
			String relation = edge.getLabel();
			String target = edge.getTarget();

			int si = concepts.addObject(source);
			int ti = concepts.addObject(target);

			String fact = String.format("assertz(%s(%s,%s)).", relation, si, ti);

			@SuppressWarnings("unused")
			boolean query = sp.query(fact, null);
		}
		System.out.println("KB creation took " + t.getTimeDeltaLastCall() + " s");
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

	private static int countPatternMatches(StringGraph pattern, SICStus sp) throws SPException {
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

			query += edgeLabel + "(" + sourceVar + "," + targetVar + ")";

			if (edgeIterator.hasNext())
				query += ",";
		}
		query += ".";
		int matches = queryPattern(query, sp, 1 << 16, 1 << 28);
		return matches;
	}

	private static int queryPattern(final String query, SICStus sp, final int loopUnrollSize, final int solutionLimit) throws SPException {
		System.out.println("querying " + query + " ...");
		SPQuery spQuery = sp.openQuery(query, null);
		int matches = 0;
		// try all answers
		Ticker t = new Ticker();
		while (true) {
			// try all answers
			for (int i = 0; i < loopUnrollSize; i++) {
				// next answer
				spQuery.nextSolution();
			}
			matches += loopUnrollSize;
			if (matches > solutionLimit)
				break;
			if (t.getElapsedTime() > 60)
				break;
		}
		System.out.println("query took " + t.getElapsedTime() + " with " + matches + " matches");
		return matches;
	}

	public static void waitForEnter() {
		System.out.println("press ENTER to continue...");
		Scanner scanner = new Scanner(System.in);
		scanner.nextLine();
		scanner.close();
	}

}
