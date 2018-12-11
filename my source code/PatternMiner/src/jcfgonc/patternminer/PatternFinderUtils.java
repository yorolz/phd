package jcfgonc.patternminer;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.FastMath;

import com.githhub.aaronbembenek.querykb.Conjunct;
import com.githhub.aaronbembenek.querykb.KnowledgeBase;
import com.githhub.aaronbembenek.querykb.Query;

import graph.GraphAlgorithms;
import graph.StringEdge;
import graph.StringGraph;
import structures.ListOfSet;
import structures.Ticker;

public class PatternFinderUtils {
	public static StringGraph mutatePattern(final StringGraph kbGraph, final RandomGenerator random, StringGraph pattern, final boolean forceAdd) {
		// TODO: detect if pattern has not changed

		// decide if adding an edge or removing existing
		boolean empty = pattern.isEmpty();
		if (random.nextBoolean() || empty || forceAdd) { // add
			if (empty) {
				// add a random edge
				StringEdge edge = GraphAlgorithms.getRandomElementFromCollection(kbGraph.edgeSet(), random);
				pattern.addEdge(edge);
			} else {
				// get an existing edge and add a random connected edge
				StringEdge existingEdge = GraphAlgorithms.getRandomElementFromCollection(pattern.edgeSet(), random);
				// add a new edge to an existing source
				if (random.nextBoolean()) {
					String source = existingEdge.getSource();
					Set<StringEdge> edgesOf = kbGraph.edgesOf(source);

					StringEdge edge = GraphAlgorithms.getRandomElementFromCollection(edgesOf, random);
					pattern.addEdge(edge);
				} else {
					// add a new edge to an existing target
					String target = existingEdge.getTarget();
					Set<StringEdge> edgesOf = kbGraph.edgesOf(target);

					StringEdge edge = GraphAlgorithms.getRandomElementFromCollection(edgesOf, random);
					pattern.addEdge(edge);
				}
			}
		} else { // remove
			Set<StringEdge> patternEdgeSet = pattern.edgeSet();
			StringEdge toRemove = GraphAlgorithms.getRandomElementFromCollection(patternEdgeSet, random);
			pattern.removeEdge(toRemove);
		}
		return pattern;
	}

	/**
	 * check for components and leave only one
	 * 
	 * @param random
	 * @param pattern
	 * @return
	 */
	public static StringGraph removeAdditionalComponents(final RandomGenerator random, final StringGraph pattern) {
		ListOfSet<String> components = GraphAlgorithms.extractGraphComponents(pattern);
		// TODO: check impact of choosing largest component, smallest, random, etc.
		// HashSet<String> largestComponent = components.getSetAt(0);
		HashSet<String> largestComponent;
		if (random == null) {
			largestComponent = components.getSetAt(0);
		} else {
			largestComponent = components.getRandomSet(random);
		}
		// filter pattern with mask
		return new StringGraph(pattern, largestComponent);
	}

	/**
	 * This function calculates base 2 log because finding the number of occupied bits is trivial.
	 * 
	 * @param val
	 * @return
	 */
	public static double log2(BigInteger val) {
		// from https://stackoverflow.com/a/9125512 by Mark Jeronimus
		// ---
		// Get the minimum number of bits necessary to hold this value.
		int n = val.bitLength();

		// Calculate the double-precision fraction of this number; as if the
		// binary point was left of the most significant '1' bit.
		// (Get the most significant 53 bits and divide by 2^53)
		long mask = 1L << 52; // mantissa is 53 bits (including hidden bit)
		long mantissa = 0;
		int j = 0;
		for (int i = 1; i < 54; i++) {
			j = n - i;
			if (j < 0)
				break;

			if (val.testBit(j))
				mantissa |= mask;
			mask >>>= 1;
		}
		// Round up if next bit is 1.
		if (j > 0 && val.testBit(j - 1))
			mantissa++;

		double f = mantissa / (double) (1L << 52);

		// Add the logarithm to the number of bits, and subtract 1 because the
		// number of bits is always higher than necessary for a number
		// (ie. log2(val)<n for every val).
		return (n - 1 + Math.log(f) * 1.44269504088896340735992468100189213742664595415298D);
		// Magic number converts from base e to base 2 before adding. For other
		// bases, correct the result, NOT this number!
	}

	public static BigInteger countPatternMatchesBI(final PatternChromosome patternChromosome, final KnowledgeBase kb) {
		StringGraph pattern = patternChromosome.getPattern();
		int numberOfEdges = pattern.numberOfEdges();

		if (numberOfEdges == 0)
			return BigInteger.ZERO;

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
		ArrayList<Conjunct> conjunctList = new ArrayList<>();

		// create the query as a conjunction of terms
		Iterator<StringEdge> edgeIterator = pattern.edgeSet().iterator();
		while (edgeIterator.hasNext()) {
			StringEdge edge = edgeIterator.next();

			String edgeLabel = edge.getLabel();
			String sourceVar = conceptToVariable.get(edge.getSource());
			String targetVar = conceptToVariable.get(edge.getTarget());

			if (sourceVar == null || targetVar == null) {
				System.err.println(pattern);
			}

			patternWithVars.addEdge(sourceVar, targetVar, edgeLabel);
			conjunctList.add(Conjunct.make(edgeLabel, sourceVar, targetVar));
		}
		assert patternWithVars.numberOfVertices() == pattern.numberOfVertices();
		assert patternWithVars.numberOfEdges() == pattern.numberOfEdges();

		String patternAsString = patternWithVars.toString(64, Integer.MAX_VALUE);

		Query q = Query.make(conjunctList);
		final int blockSize = 256;
		final int parallelLimit = 16;
		final int minute = 60 * 1000;
		final long timeLimit_ms = 2 * minute;
		// System.out.println("counting matches for " + patternAsString);
		Ticker t = new Ticker();
		BigInteger matches = kb.count(q, blockSize, parallelLimit, timeLimit_ms);
		double time = t.getElapsedTime();
		patternChromosome.countingTime = time;
		patternChromosome.matches = matches;
		patternChromosome.patternAsString = patternAsString;

		if (patternChromosome.matches == null) {
			System.err.println("WTFllklklk");
		}

		return matches;
	}

	public static double calculateFitness(PatternChromosome patternChromosome, KnowledgeBase kb) {
		StringGraph pattern = patternChromosome.getPattern();

		// TODO: think about the distribution of labels in the relations
		if (pattern.numberOfEdges("isa") > 3 || pattern.numberOfEdges("derivedfrom") > 3 || pattern.numberOfEdges("synonym") > 3 || pattern.numberOfEdges("hasprerequisite") > 3) {
			return 0;
		}

		// int isaCounter = pattern.edgeSet("isa").size();

		// double isaRatio = (double) isaCounter / pattern.edgeSet().size();
		// if (isaRatio > 0.55) {
		// matches = BigInteger.ZERO;
		// } else {
		BigInteger matches = countPatternMatchesBI(patternChromosome, kb);
		// }

//		double[] fitness = new double[2];
//		fitness[0] = matches;
//		fitness[1] = pattern.numberOfEdges();
//		return fitness;

		double matches_d = 0;
		if (matches.signum() != 0) { // to prevent log(0)
			matches_d = log2(matches);
		}
		return matches_d / FastMath.log(2, 10) * 0.1 + pattern.numberOfEdges() * 1.0;
	}

	public static StringGraph initializePattern(StringGraph inputSpace, RandomGenerator random) {
		final int mode = random.nextInt(2);
		if (mode == 0) { // pick a random concept and add its neighborhood
			String rootConcept = GraphAlgorithms.getRandomElementFromCollection(inputSpace.getVertexSet(), random);
			StringGraph pattern = new StringGraph();
			Set<StringEdge> edges = inputSpace.edgesOf(rootConcept);
			if (edges.size() > 48) {
				edges = GraphAlgorithms.randomSubSet(edges, 32, random);
			}
			pattern.addEdges(edges);
			return pattern;
		} else { // randomly add edges to an empty graph
			StringGraph pattern = new StringGraph();
			for (int i = 0; i < 3; i++) {
				PatternFinderUtils.mutatePattern(inputSpace, random, pattern, true);
			}
			return pattern;
		}
	}

}
