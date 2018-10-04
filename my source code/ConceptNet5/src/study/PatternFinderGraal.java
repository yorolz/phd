package study;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

import org.apache.commons.math3.random.Well44497a;

import fr.lirmm.graphik.graal.api.core.Atom;
import fr.lirmm.graphik.graal.api.core.AtomSetException;
import fr.lirmm.graphik.graal.api.core.ConjunctiveQuery;
import fr.lirmm.graphik.graal.api.core.Constant;
import fr.lirmm.graphik.graal.api.core.Predicate;
import fr.lirmm.graphik.graal.api.core.RuleSet;
import fr.lirmm.graphik.graal.api.core.Substitution;
import fr.lirmm.graphik.graal.api.io.ParseException;
import fr.lirmm.graphik.graal.api.kb.KnowledgeBase;
import fr.lirmm.graphik.graal.api.kb.KnowledgeBaseException;
import fr.lirmm.graphik.graal.core.DefaultAtom;
import fr.lirmm.graphik.graal.core.atomset.graph.DefaultInMemoryGraphStore;
import fr.lirmm.graphik.graal.core.ruleset.LinkedListRuleSet;
import fr.lirmm.graphik.graal.core.term.DefaultTermFactory;
import fr.lirmm.graphik.graal.io.dlp.DlgpParser;
import fr.lirmm.graphik.graal.io.dlp.DlgpWriter;
import fr.lirmm.graphik.graal.kb.DefaultKnowledgeBase;
import fr.lirmm.graphik.graal.kb.KBBuilder;
import fr.lirmm.graphik.graal.kb.KBBuilderException;
import fr.lirmm.graphik.util.stream.CloseableIterator;
import fr.lirmm.graphik.util.stream.IteratorException;
import graph.GraphAlgorithms;
import graph.StringEdge;
import graph.StringGraph;
import structures.ObjectIndex;
import structures.Ticker;

public class PatternFinderGraal {
	public static void findPatterns(StringGraph graph) throws KBBuilderException, KnowledgeBaseException, IOException, AtomSetException {
		ObjectIndex<String> concepts = new ObjectIndex<>();
		KnowledgeBase kb = createKnowledgeBase(graph, concepts);

		Well44497a random = new Well44497a(1); // fixed seed for experimentation
		StringGraph pattern = new StringGraph();

		// generate a graph pattern
		// do {
		mutatePattern(graph, random, pattern);
		mutatePattern(graph, random, pattern);
		mutatePattern(graph, random, pattern);
		// match the pattern in the graph
		int count = countPatternMatches(pattern, kb);
		System.out.println(pattern.toString() + Character.LINE_SEPARATOR + " -> " + count);
		// } while (true);

		System.lineSeparator();
	}

	private static void initializeGraal() throws KBBuilderException, KnowledgeBaseException, IOException {
		// 0 - Create a KBBuilder
		KBBuilder kbb = new KBBuilder();
		// 1 - Add a rule
		kbb.add(DlgpParser.parseRule("mortal(X) :- human(X)."));
		// 2 - Add a fact
		kbb.add(DlgpParser.parseAtom("human(socrate)."));
		// 3 - Generate the KB
		KnowledgeBase kb = kbb.build();
		// 4 - Create a DLGP writer to print data
		DlgpWriter writer = new DlgpWriter();
		// 5 - Parse a query from a Java String
		ConjunctiveQuery query = DlgpParser.parseQuery("?(X) :- mortal(X).");
		// 6 - Query the KB
		CloseableIterator<Substitution> resultIterator = kb.query(query);
		// 7 - Iterate and print results
		writer.write("\n= Answers =\n");
		if (resultIterator.hasNext()) {
			do {
				writer.write(resultIterator.next());
				writer.write("\n");
			} while (resultIterator.hasNext());
		} else {
			writer.write("No answers.\n");
		}
		// 8 - Close resources
		kb.close();
		writer.close();
	}

	private static KnowledgeBase createKnowledgeBase(StringGraph graph, ObjectIndex<String> concepts) throws AtomSetException, IteratorException {
		Ticker t = new Ticker();
		t.getTimeDeltaLastCall();
		System.out.println("creating graal KB...");
		DefaultInMemoryGraphStore store = new DefaultInMemoryGraphStore();
		RuleSet ruleset = new LinkedListRuleSet();
		KnowledgeBase kb = new DefaultKnowledgeBase(store, ruleset);

		for (StringEdge edge : graph.edgeSet()) {
			String source = edge.getSource();
			String relation = edge.getLabel();
			String target = edge.getTarget();

			int si = concepts.addObject(source);
			int ti = concepts.addObject(target);

			Constant t1 = DefaultTermFactory.instance().createConstant(si);
			Constant t2 = DefaultTermFactory.instance().createConstant(ti);
			Predicate p = new Predicate(relation, 2);
			Atom atom1 = new DefaultAtom(p, t1, t2);

			store.add(atom1);
		}
		System.out.println("graal KB creation took " + t.getTimeDeltaLastCall() + " s");
		return kb;
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

	private static int countPatternMatches(StringGraph pattern, KnowledgeBase kb) throws ParseException {
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
		ConjunctiveQuery cq = DlgpParser.parseQuery("?(X) :- mortal(X).");
		int matches = queryPattern(query, 1 << 16, false, 1 << 28);
		return matches;
	}

	private static int queryPattern(final String query, final int loopUnrollSize, final boolean nextSolutionCheck, final int solutionLimit) {
		System.out.println("querying " + query + " ...");
		// Query q = new Query(query);
		int matches = 0;
		// try all answers
		// Ticker t = new Ticker();
		// while (q.hasMoreSolutions()) {
		// for (int i = 0; i < loopUnrollSize; i++) {
		// q.nextSolution();
		// if (nextSolutionCheck && !q.hasMoreSolutions())
		// break;
		// }
		// matches += loopUnrollSize;
		// if (matches > solutionLimit)
		// break;
		// if (t.getElapsedTime() > 60)
		// break;
		// }
		// System.out.println("query took " + t.getElapsedTime() + " with " + matches + " matches");
		return matches;
	}

	public static void waitForEnter() {
		System.out.println("press ENTER to continue...");
		Scanner scanner = new Scanner(System.in);
		scanner.nextLine();
		scanner.close();
	}

}
