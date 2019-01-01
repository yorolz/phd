package jcfgonc.patternminer;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang3.mutable.MutableObject;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.util.FastMath;

import com.githhub.aaronbembenek.querykb.Conjunct;
import com.githhub.aaronbembenek.querykb.KnowledgeBase;
import com.githhub.aaronbembenek.querykb.Query;

import graph.GraphAlgorithms;
import graph.StringEdge;
import graph.StringGraph;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import structures.ListOfSet;
import structures.Ticker;

public class PatternFinderUtils {

	public static ArrayList<Conjunct> createConjunctionFromStringGraph(final StringGraph pattern, //
			final HashMap<String, String> conceptToVariable, //
			final MutableObject<StringGraph> patternWithVarsMO, //
			final MutableObject<String> patternAsStringMO) {

		// generate pattern graph with variables instead of original concepts
		if (patternWithVarsMO != null) {
			patternWithVarsMO.setValue(new StringGraph(pattern, true));
		}
		ArrayList<Conjunct> conjunctList = new ArrayList<>();

		// create the query as a conjunction of terms
		Iterator<StringEdge> edgeIterator = pattern.edgeSet().iterator();
		while (edgeIterator.hasNext()) {
			StringEdge edge = edgeIterator.next();

			String edgeLabel = edge.getLabel();
			String sourceVar = edge.getSource();
			String targetVar = edge.getTarget();
			if (conceptToVariable != null) {
				sourceVar = conceptToVariable.get(sourceVar);
				targetVar = conceptToVariable.get(targetVar);
			}

			if (patternWithVarsMO != null) {
				patternWithVarsMO.getValue().addEdge(sourceVar, targetVar, edgeLabel);
			}
			conjunctList.add(Conjunct.make(edgeLabel, sourceVar, targetVar));
		}

		if (patternAsStringMO != null) {
			patternAsStringMO.setValue(patternWithVarsMO.getValue().toString(64, Integer.MAX_VALUE));
		}
		return conjunctList;
	}

	/**
	 * mutates the pattern IN-PLACE
	 * 
	 * @param kbGraph
	 * @param random
	 * @param pattern
	 * @param forceAdd
	 */
	public static void mutatePattern(final StringGraph kbGraph, final RandomGenerator random, StringGraph pattern, final boolean forceAdd) {
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
	}

	/**
	 * check for components and leave only one
	 * 
	 * @param random
	 * @param genes
	 * @return
	 */
	public static void removeAdditionalComponents(final RandomGenerator random, final PatternChromosome genes) {
		StringGraph pattern = genes.pattern;
		ListOfSet<String> components = GraphAlgorithms.extractGraphComponents(pattern);
		genes.components = components;

		if (components.isEmpty() || components.size() == 1) {
			return;
		}

		assert components.size() > 1;

		// TODO: check impact of choosing largest component, smallest, random, etc.
		HashSet<String> largestComponent;
		if (random == null) {
			largestComponent = components.getSetAt(0);
		} else {
			largestComponent = components.getRandomSet(random);
		}

		// filter pattern with mask
		genes.pattern = new StringGraph(pattern, largestComponent);
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

	public static double countPatternMatchesBI(final PatternChromosome patternChromosome, final KnowledgeBase kb) {
		StringGraph pattern = patternChromosome.pattern;
		int numberOfEdges = pattern.numberOfEdges();

		if (numberOfEdges == 0) {
			patternChromosome.countingTime = 0;
			patternChromosome.matches = 0;
			patternChromosome.patternAsString = "";
			return 0;
		}

		HashMap<String, String> conceptToVariable = new HashMap<>();
		// replace each concept in the pattern to a variable
		int varCounter = 0;
		for (String concept : pattern.getVertexSet()) {
			String varName = "X" + varCounter;
			conceptToVariable.put(concept, varName);
			varCounter++;
		}

		MutableObject<StringGraph> patternWithVarsMO = new MutableObject<StringGraph>(new StringGraph());
		MutableObject<String> patternAsStringMO = new MutableObject<>();
		ArrayList<Conjunct> conjunctList = createConjunctionFromStringGraph(pattern, conceptToVariable, patternWithVarsMO, patternAsStringMO);

		Query q = Query.make(conjunctList);
		// System.out.println("counting matches for " + patternAsString);
		Ticker t = new Ticker();

		ReentrantLock timeOutLock = new ReentrantLock();
		timeOutLock.lock();
		Thread timeOutThread = new Thread() {
			public void run() {
				try {
					int timeout = PatternMinerConfig.QUERY_TIMEOUT_MS * 3 / 2;
					boolean lockAcquired = timeOutLock.tryLock(timeout, TimeUnit.MILLISECONDS);
					if (lockAcquired) {
						timeOutLock.unlock();
					} else {
						System.out.println("[hanged on] " + patternAsStringMO.getValue());
					}
				} catch (InterruptedException e) {
				}
			}
		};
		timeOutThread.start();
		BigInteger matches = kb.count(q, PatternMinerConfig.BLOCK_SIZE, PatternMinerConfig.PARALLEL_LIMIT, PatternMinerConfig.QUERY_TIMEOUT_MS);
		timeOutLock.unlock();

		double time = t.getElapsedTime();
		patternChromosome.countingTime = time;
		patternChromosome.patternAsString = patternAsStringMO.getValue();

		double matches_d = 0;

		if (matches.signum() != 0) { // to prevent log(0)
			matches_d = log2(matches) / FastMath.log(2, 10);
		}
		patternChromosome.matches = matches_d;
		return matches_d;
	}

	public static double calculateFitness(PatternChromosome patternChromosome, KnowledgeBase kb) {
		// TODO: think about the distribution of labels in the relations
		getRelationVariance(patternChromosome);

		double matches = countPatternMatchesBI(patternChromosome, kb);

//		double[] fitness = new double[2];
//		fitness[0] = matches;
//		fitness[1] = pattern.numberOfEdges();
//		return fitness;

		double f = matches * 0.1 //
				+ patternChromosome.pattern.numberOfEdges() * 0.1 //
				+ patternChromosome.relations.size() * 1.0 //
				- patternChromosome.relationStd * 1.0;
		return f;
	}

	private static double getRelationVariance(PatternChromosome patternChromosome) {
		Object2IntOpenHashMap<String> count = GraphAlgorithms.countRelations(patternChromosome.pattern);
		patternChromosome.relations = count;
		int size = count.size();
		if (size == 0)
			return 0;
		double[] count_d = new double[size];
		int i = 0;
		for (String key : count.keySet()) {
			count_d[i++] = count.getInt(key);
		}
		DescriptiveStatistics ds = new DescriptiveStatistics(count_d);
		double std = ds.getStandardDeviation();
		patternChromosome.relationStd = std;
		return std;
	}

	public static StringGraph initializePattern(StringGraph inputSpace, RandomGenerator random) {
		float r = random.nextFloat();
		if (r < 0.15) { // pick a random concept and add its neighborhood
			String rootConcept = GraphAlgorithms.getRandomElementFromCollection(inputSpace.getVertexSet(), random);
			StringGraph pattern = new StringGraph();
			Set<StringEdge> edges = inputSpace.edgesOf(rootConcept);
			if (edges.size() > 8) {
				edges = GraphAlgorithms.randomSubSet(edges, 8, random);
			}
			pattern.addEdges(edges);
			return pattern;
		} else { // randomly add edges to an empty graph
			StringGraph pattern = new StringGraph();
			for (int i = 0; i < 2; i++) { // add two edges
				PatternFinderUtils.mutatePattern(inputSpace, random, pattern, true);
			}
			return pattern;
		}
	}

}
