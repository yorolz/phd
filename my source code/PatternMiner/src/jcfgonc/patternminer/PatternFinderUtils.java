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
	private static final double log2_10 = FastMath.log(2, 10);

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

	public static double countPatternMatches(final StringGraph pattern, final KnowledgeBase kb) {
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

		Ticker t = new Ticker();
		Query q = Query.make(conjunctList);
		final int blockSize = 256;
		final int parallelLimit = 3;
		final long timeLimit_ms = 3 * 60 * 1000;
		// this is possibly too big to fit in 64-bit so lets log it
		double matches = log2(kb.count(q, blockSize, parallelLimit, timeLimit_ms)) / log2_10;
		double time = t.getElapsedTime();
		System.out
				.println("pattern edges\t" + patternWithVars.numberOfEdges() + "\tpattern vars\t" + patternWithVars.numberOfVertices() + "\ttime\t" + time + "\tmatches\t" + matches
				// + "\tsolutions/s\t" + (matches / time)
						+ "\tpattern\t" + patternWithVars.toString(64, Integer.MAX_VALUE));
		return matches;
	}

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
