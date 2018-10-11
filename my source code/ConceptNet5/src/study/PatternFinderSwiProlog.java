package study;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.math3.random.Well44497a;
import org.jpl7.Compound;
import org.jpl7.JPL;
import org.jpl7.Query;
import org.jpl7.Term;
import org.jpl7.Variable;

import graph.GraphAlgorithms;
import graph.StringEdge;
import graph.StringGraph;
import structures.ObjectIndex;
import structures.Ticker;

public class PatternFinderSwiProlog {

	public static void findPatterns(StringGraph graph) {
		// Various.waitForEnter();
		ObjectIndex<String> concepts = new ObjectIndex<>();
		createKnowledgeBase(graph, concepts);
		// System.exit(0);
		Well44497a random = new Well44497a(1); // fixed seed for experimentation
		StringGraph pattern = new StringGraph();

		// generate a graph pattern
		// do {
		for (int i = 0; i < 3; i++) {
			mutatePattern(graph, random, pattern);
		}
		// match the pattern in the graph
		long count = countPatternMatches(pattern);
		System.out.println(pattern.toString() + Character.LINE_SEPARATOR + " -> " + count);
		// } while (true);

		// System.lineSeparator();
	}

	private static void createKnowledgeBase(StringGraph graph, ObjectIndex<String> concepts) {
		JPL.init();
		Ticker t = new Ticker();
		t.getTimeDeltaLastCall();
		System.out.println("creating SWI KB...");
		for (StringEdge edge : graph.edgeSet()) {
			String source = edge.getSource();
			String relation = edge.getLabel();
			String target = edge.getTarget();

			int si = concepts.addObject(source);
			int ti = concepts.addObject(target);

			Compound relationCompound = new Compound(relation, new Term[] { new org.jpl7.Integer(si), new org.jpl7.Integer(ti) });
			Compound factCompound = new Compound("assertz", new Term[] { relationCompound });
			Query fact = new Query(factCompound);
			fact.putQuery_jcfgonc();
		}
		System.out.println("SWI KB creation took " + t.getTimeDeltaLastCall() + " s");
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

	private static long countPatternMatches(StringGraph pattern) {
		int numberOfEdges = pattern.numberOfEdges();

		HashMap<String, String> conceptToVariable = new HashMap<>();
		// replace each concept in the pattern to a variable
		int varCounter = 0;
		for (String concept : pattern.getVertexSet()) {
			String varName = "X" + varCounter;
			conceptToVariable.put(concept, varName);
			varCounter++;
		}

		// create the query as a conjunction of terms
		Iterator<StringEdge> edgeIterator = pattern.edgeSet().iterator();
		int i = 0;
		Compound lastCompound = null;
		Compound rootCompound = null;
		while (edgeIterator.hasNext()) {
			StringEdge edge = edgeIterator.next();

			String edgeLabel = edge.getLabel();
			String sourceVar = conceptToVariable.get(edge.getSource());
			String targetVar = conceptToVariable.get(edge.getTarget());

			Compound currentTerm = new Compound(edgeLabel, new Term[] { new Variable(sourceVar), new Variable(targetVar) });

			if (i == 0) { // first term
				if (edgeIterator.hasNext()) {
					// multiple terms
					rootCompound = new Compound(",", new Term[2]);
					lastCompound = rootCompound;
					lastCompound.setArg(1, currentTerm);
				} else {
					// only one term
					rootCompound = currentTerm;
				}
			} else { // following terms
				// not last term
				if (i != numberOfEdges - 1) {
					Compound nextAnd = new Compound(",", new Term[2]);
					lastCompound.setArg(2, nextAnd);
					lastCompound = nextAnd;
					lastCompound.setArg(1, currentTerm);
				} else { // last term
					lastCompound.setArg(2, currentTerm);
				}
			}
			i++;
		}
		Query q = new Query(rootCompound);
		// Query qtest = new Query("isa(X3,X0),isa(X3,X1),isa(X2,X1)."); //test query
		long matches = queryPattern(q, 400000000);
		return matches;
	}

	private static long queryPattern(final Query query, final int solutionLimit) {
		System.out.println("querying..."); // do not show query, jpl/prolog explodes
		// try all answers
		Ticker t = new Ticker();
		long matches = query.countSolutions_jcfgonc(solutionLimit);
		double time = t.getElapsedTime();
		System.out.println("time\t" + time + "\tmatches\t" + matches + "\tsolutions/s\t" + (matches / time));
		return matches;
	}

}
