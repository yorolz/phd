package jcfgonc.patternminer;

import java.util.HashMap;
import java.util.Iterator;

import org.apache.commons.math3.random.Well44497a;
import org.jpl7.Compound;
import org.jpl7.JPL;
import org.jpl7.Query;
import org.jpl7.Term;
import org.jpl7.Variable;

import graph.StringEdge;
import graph.StringGraph;
import structures.ObjectIndex;
import structures.Ticker;

public class PatternFinderSwiProlog {

	/**
	 * called from ConceptNetStudy.java
	 * 
	 * @param graph
	 */
	public static void findPatterns(final StringGraph graph) {
		// Various.waitForEnter();
		ObjectIndex<String> concepts = new ObjectIndex<>();
		createKnowledgeBase(graph, concepts);
		// System.exit(0);
		Well44497a random = new Well44497a(1); // fixed seed for experimentation
		StringGraph pattern = new StringGraph();

		// generate a graph pattern
		// do {
		for (int i = 0; i < 3; i++) {
			PatternFinderUtils.mutatePattern(graph, random, pattern, true);
		}
		// match the pattern in the graph
		long count = countPatternMatches(pattern, 200000000);
		System.out.println(pattern.toString() + Character.LINE_SEPARATOR + " -> " + count);
		// } while (true);

		// System.lineSeparator();
	}

	/**
	 * 
	 * @param graph
	 * @param concepts
	 *            if null concepts are stored as text, else stored as unique integers
	 */
	public static void createKnowledgeBase(final StringGraph graph, final ObjectIndex<String> concepts) {
		JPL.init();
		Ticker t = new Ticker();
		System.out.println("creating SWI KB...");
		for (StringEdge edge : graph.edgeSet()) {
			String source = edge.getSource();
			String relation = edge.getLabel();
			String target = edge.getTarget();

			Compound relationCompound;
			if (concepts == null) {
				relationCompound = new Compound(relation, new Term[] { new org.jpl7.Atom(source), new org.jpl7.Atom(target) });
			} else {
				int si = concepts.addObject(source);
				int ti = concepts.addObject(target);
				relationCompound = new Compound(relation, new Term[] { new org.jpl7.Integer(si), new org.jpl7.Integer(ti) });
			}

			Compound factCompound = new Compound("assertz", new Term[] { relationCompound });
			Query fact = new Query(factCompound);
			fact.putQuery_jcfgonc();
		}
		System.out.println("SWI KB creation took " + t.getTimeDeltaLastCall() + " s");
	}

	public static long countPatternMatches(final StringGraph pattern, final int solutionLimit) {
		int numberOfEdges = pattern.numberOfEdges();

		if (numberOfEdges == 0)
			return 0;

		HashMap<String, String> conceptToVariable = new HashMap<>();
		// replace each concept in the pattern to a variable
		int varCounter = 0;
		for (String concept : pattern.getVertexSet()) {
			String varName = "X" + varCounter;
			conceptToVariable.put(concept, varName);
			varCounter++;
		}

		// generate pattern graph with variables instead of original concepts
		StringGraph patternWithVars = new StringGraph(pattern, true);

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

			if (sourceVar == null || targetVar == null) {
				System.err.println(pattern);
			}

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

			patternWithVars.addEdge(sourceVar, targetVar, edgeLabel);

			i++;
		}
		Query q = new Query(rootCompound);
		// Query qtest = new Query("isa(X3,X0),isa(X3,X1),isa(X2,X1)."); //test query
		Ticker t = new Ticker();
		long matches = q.countSolutions_jcfgonc(solutionLimit);
		double time = t.getElapsedTime();
		System.out.println("pattern edges\t" + patternWithVars.numberOfEdges() + "\tpattern vars\t" + patternWithVars.numberOfVertices() + "\ttime\t" + time + "\tmatches\t"
				+ matches + "\tsolutions/s\t" + (matches / time) + "\tpattern\t" + patternWithVars.toString(64, Integer.MAX_VALUE));
		return matches;
	}
}
