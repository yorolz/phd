package study;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.math3.random.Well44497a;

import abcdatalog.ast.Clause;
import abcdatalog.ast.Constant;
import abcdatalog.ast.PositiveAtom;
import abcdatalog.ast.PredicateSym;
import abcdatalog.ast.Premise;
import abcdatalog.ast.validation.DatalogValidationException;
import abcdatalog.engine.DatalogEngine;
import abcdatalog.engine.bottomup.sequential.SemiNaiveEngine;
import alice.tuprolog.InvalidTheoryException;
import amzi.ls.LSException;
import graph.GraphAlgorithms;
import graph.StringEdge;
import graph.StringGraph;
import structures.Ticker;

public class PatternFinderAbcDatalog {

	public static void findPatterns(StringGraph graph) throws DatalogValidationException, InvalidTheoryException, LSException {
		DatalogEngine datalog = new SemiNaiveEngine();
		createAbcDatalogEDB(graph, datalog);

		Well44497a random = new Well44497a(3); // fixed seed for experimentation
		StringGraph pattern = new StringGraph();

		// generate a graph pattern
		// do {
		mutatePattern(graph, random, pattern);
		mutatePattern(graph, random, pattern);
		mutatePattern(graph, random, pattern);
		// match the pattern in the graph
		Ticker t = new Ticker();
		int count = countPatternMatches(pattern, datalog);
		t.showTimeDeltaLastCall();
		System.out.println(pattern.toString() + Character.LINE_SEPARATOR + " -> " + count);
		// } while (true);

		// System.lineSeparator();
	}

	private static void createAbcDatalogEDB(StringGraph graph, DatalogEngine datalog) throws DatalogValidationException {
		// EDB = extensional data base, contains only facts
		List<Premise> emptyBody = new ArrayList<>(); // TODO ver se null melhora performance
		Set<Clause> clauses = new HashSet<>();
		Ticker t = new Ticker();
		t.getTimeDeltaLastCall();
		System.out.println("creating AbcDatalog EDB...");
		for (StringEdge edge : graph.edgeSet()) {
			String source = edge.getSource();
			String relation = edge.getLabel();
			String target = edge.getTarget();

			Constant[] constants = new Constant[2];
			constants[0] = Constant.create(source);
			constants[1] = Constant.create(target);
			PositiveAtom head = PositiveAtom.create(PredicateSym.create(relation, 2), constants);
			clauses.add(new Clause(head, emptyBody));
		}
		datalog.init(clauses);
		System.out.println("Datalog EDB creation took " + t.getTimeDeltaLastCall() + " s");
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

	@SuppressWarnings("unused")
	private static int countPatternMatches(StringGraph pattern, DatalogEngine datalog) {
		HashMap<String, String> conceptToVariable = new HashMap<>();
		// replace each concept in the pattern to a variable
		int varCounter = 0;
		for (String concept : pattern.getVertexSet()) {
			String varName = "X" + varCounter;
			conceptToVariable.put(concept, varName);
			varCounter++;
		}
		// ArrayList<Expr> query = new ArrayList<>();
		// convert each edge to a predicate
		for (StringEdge edge : pattern.edgeSet()) {
			String relation = edge.getLabel();
			String sourceVar = conceptToVariable.get(edge.getSource());
			String targetVar = conceptToVariable.get(edge.getTarget());
			// Term[] constants;
			// PositiveAtom query = PositiveAtom.create(PredicateSym.create(relation, 2), constants);
			// datalog.query(query);
		}
		int matches = 0;
		return matches;
	}

}
