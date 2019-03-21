package jcfgonc.patternminer;

import java.math.BigInteger;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.util.FastMath;

import com.githhub.aaronbembenek.querykb.Conjunct;
import com.githhub.aaronbembenek.querykb.KnowledgeBase;
import com.githhub.aaronbembenek.querykb.KnowledgeBase.KnowledgeBaseBuilder;
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
	private static StringGraph createPatternWithVars(final StringGraph pattern, final HashMap<String, String> conceptToVariable) {
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
	 * Check for components and leave only one, done IN-PLACE. If random is not null, select a random component as the only one. Otherwise select the largest component.
	 * 
	 * @param random
	 * @param genes
	 * @return the components
	 */
	public static void removeAdditionalComponents(final PatternChromosome genes, final RandomGenerator random) {
		StringGraph pattern = genes.pattern;
		ListOfSet<String> components = GraphAlgorithms.extractGraphComponents(pattern);
		genes.components = components;

		if (components.size() == 1) {
			HashSet<String> component0 = components.getSetAt(0);
			if (component0.isEmpty()) {
				System.err.println("### got an empty component");
			}
			return; // break, no need to filter the pattern (ie remove additional components)
		}

		System.err.format("### got a pattern with %d components: %s\n", components.size(), components.toString());

		HashSet<String> largestComponent;
		if (random == null) {
			largestComponent = components.getSetAt(0);
		} else {
			largestComponent = components.getRandomSet(random);
		}

		// filter pattern with mask and store it back
		genes.pattern = new StringGraph(pattern, largestComponent);
	}

	/**
	 * This function calculates base 2 log because finding the number of occupied bits is trivial.
	 * 
	 * @param val
	 * @return
	 */
	private static double log2(BigInteger val) {
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

//			ReentrantLock timeOutLock = new ReentrantLock(); // used as the waiting object
//			timeOutLock.lock(); // prevent timeout of occurring now
//			Thread timeOutThread = new Thread() {
//				public void run() {
//					try {
//						int timeout = PatternMinerConfig.QUERY_TIMEOUT_MS * 3 / 2;
//						boolean lockAcquired = timeOutLock.tryLock(timeout, TimeUnit.MILLISECONDS);
//						if (lockAcquired) {
//							timeOutLock.unlock();
//						} else {
//							String patternAsString = patternWithVars.toString(Integer.MAX_VALUE, Integer.MAX_VALUE);
//							System.out.println("[hanged on] " + patternAsString);
//						}
//					} catch (InterruptedException e) {
//					}
//				}
//			};
//			timeOutThread.start();
			Ticker t = new Ticker();
			Query q = Query.make(conjunctList);
			BigInteger solutionLimit = BigInteger.valueOf(10).pow(pattern.numberOfVertices()); // limit solution count to 10^vertices
			BigInteger matches = kb.count(q, PatternMinerConfig.BLOCK_SIZE, PatternMinerConfig.PARALLEL_LIMIT, solutionLimit, true,
					Long.valueOf(PatternMinerConfig.QUERY_TIMEOUT_SECONDS));
//			timeOutLock.unlock(); // warn timeout thread

			if (matches.compareTo(BigInteger.ZERO) == -1) { // less than zero (should not happen)
				patternChromosome.matches = -1;
			} else if (matches.compareTo(BigInteger.ZERO) == 0) { // zero matches
				patternChromosome.matches = 0.0;
			} else if (matches.compareTo(BigInteger.ONE) == 0) { // one match
				patternChromosome.matches = 0.1;
			} else { // more than than one match
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

	public static void countCycles(PatternChromosome patternChromosome) {
		// this only works if the graph has one component
		ListOfSet<String> components = patternChromosome.components;
		if (components == null || components.size() == 0) {
			patternChromosome.cycles = 0;
			return;
		}

		// TODO: adapt this to support multiple components
		ArrayDeque<StringEdge> edgesToVisit = new ArrayDeque<>();
		HashSet<UnorderedPair<String>> edgesVisited = new HashSet<>();
		HashSet<String> verticesVisited = new HashSet<>();

		StringGraph pattern = patternChromosome.pattern;
		StringEdge startingEdge = pattern.edgeSet().iterator().next();
		edgesToVisit.add(startingEdge);
		int cycles = 0;

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
				cycles++;
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
		patternChromosome.cycles = cycles;
//		System.out.println(cycles);
		if (cycles > 0) {
			System.lineSeparator();
		}
//		System.lineSeparator();
	}

	/**
	 * calculates the relation histogram and variance
	 * 
	 * @param patternChromosome
	 * @return
	 */
	public static double calculateRelationHistogram(PatternChromosome patternChromosome) {
		Object2IntOpenHashMap<String> count = GraphAlgorithms.countRelations(patternChromosome.pattern);
		patternChromosome.relations = count;
		int size = count.size();
		if (size == 0) {
			patternChromosome.relationStd = 0;
			return 0;
		}
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

	public static StringGraph initializePattern(StringGraph kbGraph, StringGraph pattern, RandomGenerator random) {
		// randomly add edges to an empty graph
		for (int i = 0; i < 3; i++) { // add N edges
			PatternMutation.mutation(kbGraph, random, pattern, true);
		}
		return pattern;
	}

	public static KnowledgeBase buildKnowledgeBase(StringGraph kbGraph) {
		KnowledgeBaseBuilder kbb = new KnowledgeBaseBuilder();
		Ticker ticker = new Ticker();

		for (StringEdge edge : kbGraph.edgeSet()) {
			String label = edge.getLabel();
			String source = edge.getSource();
			String target = edge.getTarget();
			kbb.addFact(label, source, target);
		}

		KnowledgeBase kb = kbb.build();
		System.out.println("build took " + ticker.getElapsedTime() + " s");
		return kb;
	}

}
