package jcfgonc.patternminer;

import java.math.BigInteger;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

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
import structures.UnorderedPair;

public class PatternFinderUtils {

	/**
	 * generates a graph with custom variables instead of the original concepts
	 * 
	 * @param pattern
	 * @param conceptToVariable
	 * @return
	 */
	public static StringGraph createPatternWithVars(final StringGraph pattern, final HashMap<String, String> conceptToVariable) {
		StringGraph patternWithVars = new StringGraph();

		// create the query as a conjunction of terms
		for (StringEdge edge : pattern.edgeSet()) {

			String edgeLabel = edge.getLabel();
			String sourceVar = edge.getSource();
			String targetVar = edge.getTarget();
			if (conceptToVariable != null) {
				sourceVar = conceptToVariable.get(sourceVar);
				targetVar = conceptToVariable.get(targetVar);
			}
			patternWithVars.addEdge(sourceVar, targetVar, edgeLabel);
		}

		assert patternWithVars.numberOfEdges() == pattern.numberOfEdges(); // bug check
		return patternWithVars;
	}

	/**
	 * creates a querykb conjunction to be used as a query using the given variable<=>variable mapping
	 * 
	 * @param pattern
	 * @param conceptToVariable
	 * @return
	 */
	public static ArrayList<Conjunct> createConjunctionFromStringGraph(final StringGraph pattern, final HashMap<String, String> conceptToVariable) {

		ArrayList<Conjunct> conjunctList = new ArrayList<>();

		// create the query as a conjunction of terms
		for (StringEdge edge : pattern.edgeSet()) {

			String edgeLabel = edge.getLabel();
			String sourceVar = edge.getSource();
			String targetVar = edge.getTarget();
			if (conceptToVariable != null) {
				sourceVar = conceptToVariable.get(sourceVar);
				targetVar = conceptToVariable.get(targetVar);
			}
			conjunctList.add(Conjunct.make(edgeLabel, sourceVar, targetVar));
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

	public static void countPatternMatchesBI(final PatternChromosome patternChromosome, final KnowledgeBase kb) {
		StringGraph pattern = patternChromosome.pattern;
		if (pattern.numberOfEdges() > 0) {
			HashMap<String, String> conceptToVariable = createConceptToVariableMapping(pattern);
			ArrayList<Conjunct> conjunctList = createConjunctionFromStringGraph(pattern, conceptToVariable);
			StringGraph patternWithVars = createPatternWithVars(pattern, conceptToVariable);
			String patternAsString = patternWithVars.toString(Integer.MAX_VALUE, Integer.MAX_VALUE);

			ReentrantLock timeOutLock = new ReentrantLock(); // used as the waiting object
			timeOutLock.lock(); // prevent timeout of occurring now
			Thread timeOutThread = new Thread() {
				public void run() {
					try {
						int timeout = PatternMinerConfig.QUERY_TIMEOUT_MS * 3 / 2;
						boolean lockAcquired = timeOutLock.tryLock(timeout, TimeUnit.MILLISECONDS);
						if (lockAcquired) {
							timeOutLock.unlock();
						} else {
							System.out.println("[hanged on] " + patternAsString);
						}
					} catch (InterruptedException e) {
					}
				}
			};
			timeOutThread.start();
			Ticker t = new Ticker();
			BigInteger matches = kb.count(Query.make(conjunctList), PatternMinerConfig.BLOCK_SIZE, PatternMinerConfig.PARALLEL_LIMIT, PatternMinerConfig.QUERY_TIMEOUT_MS);
			timeOutLock.unlock(); // warn timeout thread

			patternChromosome.matches = 0;
			if (matches.signum() != 0) { // to prevent log(0)
				patternChromosome.matches = log2(matches) / FastMath.log(2, 10);
			}
			patternChromosome.countingTime = t.getElapsedTime();
			patternChromosome.patternWithVars = patternWithVars;
		} else {
			patternChromosome.countingTime = 0;
			patternChromosome.patternWithVars = new StringGraph();
		}

	}

	private static HashMap<String, String> createConceptToVariableMapping(StringGraph pattern) {
		Set<String> vertexSet = pattern.getVertexSet();
		HashMap<String, String> conceptToVariable = new HashMap<>(vertexSet.size() * 2);
		int varCounter = 0;
		for (String concept : vertexSet) {
			String varName = "X" + varCounter;
			conceptToVariable.put(concept, varName);
			varCounter++;
		}
		return conceptToVariable;
	}

	public static double calculateFitness(PatternChromosome patternChromosome, KnowledgeBase kb) {
		getRelationVariance(patternChromosome);
		countPatternMatchesBI(patternChromosome, kb);
		countLoops(patternChromosome);

//		double[] fitness = new double[2];
//		fitness[0] = matches;
//		fitness[1] = pattern.numberOfEdges();
//		return fitness;

		double f = patternChromosome.matches * 0.00//
				+ patternChromosome.pattern.numberOfEdges() * 0.0 //
				+ patternChromosome.loops * 1.0 //
				+ patternChromosome.relations.size() * 1.0 //
				- patternChromosome.relationStd * 10.0;
		return f;
	}

	public static void countLoops(PatternChromosome patternChromosome) {
		// this only works if the graph has one component
		int numComponents = patternChromosome.components.size();
		patternChromosome.loops = 0;
		if (numComponents == 0)
			return;
		StringGraph pattern = patternChromosome.pattern;

		ArrayDeque<StringEdge> edgesToVisit = new ArrayDeque<>();
		HashSet<UnorderedPair<String>> edgesVisited = new HashSet<>();
		HashSet<String> verticesVisited = new HashSet<>();

		StringEdge startingEdge = pattern.edgeSet().iterator().next();
		edgesToVisit.add(startingEdge);
		int loops = 0;

		while (true) {
			StringEdge edge = edgesToVisit.pollLast();
			if (edge == null)
				break;
			String source = edge.getSource();
			String target = edge.getTarget();
			// check if the vertex pair has been already visited
			UnorderedPair<String> edgeUndirected = new UnorderedPair<String>(source, target);
			if (edgesVisited.contains(edgeUndirected))
				continue;
			edgesVisited.add(edgeUndirected);

			boolean sourceVisited = verticesVisited.contains(source);
			boolean targetVisited = verticesVisited.contains(target);
			if (sourceVisited && targetVisited) {
				loops++;
			}

			verticesVisited.add(source);
			verticesVisited.add(target);

			// do not add coincident edges, i.e., guarantee unique vertex pairs
			HashSet<StringEdge> edgesTouching = pattern.getTouchingEdges(edge);
			for (StringEdge newEdge : edgesTouching) {
				UnorderedPair<String> newEdgeConcepts = new UnorderedPair<String>(newEdge.getSource(), newEdge.getTarget());
				if (!edgesVisited.contains(newEdgeConcepts))
					edgesToVisit.add(newEdge);
			}
		}
		patternChromosome.loops = loops;
//		System.out.println(loops);
		if (loops > 0) {
			System.lineSeparator();
		}
//		System.lineSeparator();
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
