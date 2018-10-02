package study;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.math3.random.Well44497a;

import amzi.ls.LSException;
import amzi.ls.LogicServer;
import graph.GraphAlgorithms;
import graph.StringEdge;
import graph.StringGraph;
import structures.ObjectIndex;
import structures.Ticker;

public class PatternFinderAmziProlog {

	public static void findPatterns(StringGraph graph) throws LSException {
		LogicServer ls = initializeAmziProlog();
		// DatalogEngine datalog = new SemiNaiveEngine();

		// createDatalogEDB(graph, datalog);
		ObjectIndex<String> concepts = new ObjectIndex<>();
		createAmziKB(graph, ls, concepts);

		Well44497a random = new Well44497a(1); // fixed seed for experimentation
		StringGraph pattern = new StringGraph();

		// generate a graph pattern
		// do {
		mutatePattern(graph, random, pattern);
		mutatePattern(graph, random, pattern);
		mutatePattern(graph, random, pattern);
		// match the pattern in the graph
		Ticker t = new Ticker();
		int count = countPatternMatchesAmziKB(pattern, ls);
		t.showTimeDeltaLastCall();
		System.out.println(pattern.toString() + Character.LINE_SEPARATOR + " -> " + count);
		// } while (true);

		// System.lineSeparator();
	}

	private static LogicServer initializeAmziProlog() throws LSException {
		LogicServer ls = new LogicServer();
		ls.Init("");
		ls.Load("acmp.xpl");
		return ls;
	}

	private static void createAmziKB(StringGraph graph, LogicServer ls, ObjectIndex<String> concepts) throws LSException {
		Ticker t = new Ticker();
		t.getTimeDeltaLastCall();
		System.out.println("creating Amzi KB...");
		for (StringEdge edge : graph.edgeSet()) {
			String source = edge.getSource();
			String relation = edge.getLabel();
			String target = edge.getTarget();

			int si = concepts.addObject(source);
			int ti = concepts.addObject(target);

			String term = relation + "(" + si + "," + ti + ")";
			ls.AssertzStr(term);
		}
		System.out.println("Amzi KB creation took " + t.getTimeDeltaLastCall() + " s");
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

	private static int countPatternMatchesAmziKB(StringGraph pattern, LogicServer ls) throws LSException {
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
		// int matches = queryPattern(ls, query, 1 << 1);
		queryPattern(ls, query, 1 << 1);
		queryPattern(ls, query, 1 << 2);
		queryPattern(ls, query, 1 << 4);
		queryPattern(ls, query, 1 << 8);
		queryPattern(ls, query, 1 << 12);
		queryPattern(ls, query, 1 << 16);
		queryPattern(ls, query, 1 << 20);
		queryPattern(ls, query, 1 << 24);
		return 0;
	}

	private static int queryPattern(LogicServer ls, String query, final int loopUnrollSize) throws LSException {
		System.out.println("querying " + query + " ...");
		long resultTerm = ls.CallStr(query);
		int matches = 0;
		Ticker t = new Ticker();
		if (resultTerm == 0) {
			System.out.println("CallStr(query) Failed");
		} else {
			while (true) {
				// try all answers
				for (int i = 0; i < loopUnrollSize; i++) {
					// next answer
					ls.Redo();
				}
				matches += loopUnrollSize;
				if (t.getElapsedTime() > 60)
					break;
			}
			// do {
			// // String result = ls.TermToStr(resultTerm, 1 << 16);
			// matches += 1;
			// if (t.getElapsedTime() > 60)
			// break;
			// } while (ls.Redo());
		}
		System.out.println("query took " + t.getElapsedTime() + " with " + matches + " matches");
		return matches;
	}

}
