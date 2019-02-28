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
	private static ReentrantLock lock = new ReentrantLock();

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
		boolean isEmpty = pattern.edgeSet().isEmpty();
		if (random.nextBoolean() || isEmpty || forceAdd) { // add
			if (isEmpty) {
				// add a random edge
				StringEdge edge = GraphAlgorithms.getRandomElementFromCollection(kbGraph.edgeSet(), random);
				pattern.addEdge(edge);
			} else {
				// make a cycle
				if (pattern.numberOfVertices() >= 3 && random.nextFloat() > 0.5) {
					// System.err.println("### trying to add a cycle");
					ArrayList<String> vertices = new ArrayList<>(pattern.getVertexSet());
					GraphAlgorithms.shuffleArrayList(vertices, random);
					// try to connect randomly a pair of vertices
					// boolean cycleAdded = false;
					outerfor: for (int i = 0; i < vertices.size(); i++) {
						String v0 = vertices.get(i);
						for (int j = i + 1; j < vertices.size(); j++) {
							String v1 = vertices.get(j);
							// make sure the pair is not yet connected
							Set<StringEdge> pedges = pattern.getBidirectedEdges(v0, v1);
							if (pedges.isEmpty()) {
								// and make sure the pair can be connected
								Set<StringEdge> kbEdges = kbGraph.getBidirectedEdges(v0, v1);
								if (!kbEdges.isEmpty()) { // can be connected with knowledge from the kb
									Object2IntOpenHashMap<String> relationCount = GraphAlgorithms.countRelations(pattern);
									tryAddingRareLabelEdge(random, pattern, relationCount, kbEdges);
									// cycleAdded = true;
									break outerfor;
								}
							}
						}
					}
//					if (cycleAdded) {
//						System.err.println("### cycle added: " + cycleAdded);
//					}
				} else {
					// try to keep generalizing the pattern by adding an edge with a different relation
					// get all KB edges touching the pattern's vertices
					HashSet<StringEdge> kbEdges = kbGraph.edgesOf(pattern.getVertexSet());
					// and filter edges having the the same existing relations
					Object2IntOpenHashMap<String> relationCount = GraphAlgorithms.countRelations(pattern);
					tryAddingRareLabelEdge(random, pattern, relationCount, kbEdges);
				}
			}
		} else { // remove
			// try to remove a terminal edge first
			Set<StringEdge> edges;
			HashSet<StringEdge> terminalEdges = getTerminalEdges(pattern);
			if (!terminalEdges.isEmpty()) {
				edges = terminalEdges;
			} else {
				edges = pattern.edgeSet();
			}
			// try to remove an edge with a common label first
			Object2IntOpenHashMap<String> relationCount = GraphAlgorithms.countRelations(edges);
			HashSet<String> frequentLabels = getMostFrequentLabels(relationCount);
			// get the edges with the most frequent labels
			HashSet<StringEdge> edgesFrequent = filterEdges(edges, frequentLabels);
			// remove one of those edges
			StringEdge byeEdge = GraphAlgorithms.getRandomElementFromCollection(edgesFrequent, random);
			pattern.removeEdge(byeEdge);
		}

		// pattern could be empty, reinitialize it
		if (pattern.isEmpty()) {
			pattern.clear();
			pattern.addEdges(PatternFinderUtils.initializePattern(kbGraph, new StringGraph(), random));
		}
	}

	private static HashSet<StringEdge> getTerminalEdges(StringGraph pattern) {
		HashSet<StringEdge> terminalEdges = new HashSet<StringEdge>();
		for (StringEdge edge : pattern.edgeSet()) {
			int degreeSource = pattern.degreeOf(edge.getSource());
			int degreeTarget = pattern.degreeOf(edge.getTarget());
			if (degreeSource == 1 || degreeTarget == 1) {
				terminalEdges.add(edge);
			}
		}
		return terminalEdges;
	}

	private static void tryAddingRareLabelEdge(final RandomGenerator random, StringGraph pattern, Object2IntOpenHashMap<String> patternRelationCount, Set<StringEdge> kbEdges) {
		HashSet<StringEdge> edgesWithDifferentLabels = filterEdgesExcludingLabel(kbEdges, patternRelationCount.keySet());
		if (!edgesWithDifferentLabels.isEmpty())
		// we can add an edge with a different label, so add a random one
		{
			StringEdge edge = GraphAlgorithms.getRandomElementFromCollection(edgesWithDifferentLabels, random);
			pattern.addEdge(edge);
		} else
		// unable to add an edge with a different label than the ones existing
		// add the label with the lowest presence in the pattern
		{
			// get the kb's edge labels
			HashSet<String> kbLabels = GraphAlgorithms.getEdgesLabelsAsSet(kbEdges);
			// make sure the histogram only contains the labels present in kbEdges
			Object2IntOpenHashMap<String> patternRelationCountFiltered = filterMapKeys(patternRelationCount, kbLabels);
			HashSet<String> rarestLabels = getLeastFrequentLabels(patternRelationCountFiltered);
			HashSet<StringEdge> rarestLabelEdges = filterEdges(kbEdges, rarestLabels);

			if (rarestLabelEdges.isEmpty()) {
				lock.lock();
				System.out.println(kbEdges);// [ca_wouters,notableidea,habitus]
				System.out.println(pattern);// ca_wouters,influencedby,norbert_elia;pierre_bourdieu,notableidea,habitus;pierre_bourdieu,maininterest,power;pierre_bourdieu,influencedby,norbert_elia;norbert_elia,notableidea,habitus;norbert_elia,influencedby,karl_mannheim;
				System.out.println(patternRelationCountFiltered); // {influencedby=>3, maininterest=>1, notableidea=>2}
				System.out.println(rarestLabels); // [maininterest]
				System.out.println(rarestLabelEdges); // []
				lock.unlock();
				System.exit(-1);
			}

			StringEdge edge = GraphAlgorithms.getRandomElementFromCollection(rarestLabelEdges, random);
			pattern.addEdge(edge);
		}
	}

	/**
	 * returns a map with only the keys present in the given set
	 * 
	 * @param relationCount
	 * @param labels
	 * @return
	 */
	private static Object2IntOpenHashMap<String> filterMapKeys(Object2IntOpenHashMap<String> relationCount, Set<String> labels) {
		Object2IntOpenHashMap<String> patternRelationCountFiltered = new Object2IntOpenHashMap<String>();
		for (String label : relationCount.keySet()) {
			if (labels.contains(label)) {
				patternRelationCountFiltered.put(label, relationCount.getInt(label));
			}
		}
		return patternRelationCountFiltered;
	}

	/**
	 * returns the edges whose label is not in the given label set
	 * 
	 * @param edges
	 * @param labels
	 * @return
	 */
	private static HashSet<StringEdge> filterEdgesExcludingLabel(Set<StringEdge> edges, Set<String> labels) {
		HashSet<StringEdge> filtered = new HashSet<StringEdge>();
		for (StringEdge edge : edges) {
			if (!labels.contains(edge.getLabel())) {
				filtered.add(edge);
			}
		}
		return filtered;
	}

	/**
	 * returns the edges whose label is in the given label set
	 * 
	 * @param edges
	 * @param labels
	 * @return
	 */
	private static HashSet<StringEdge> filterEdges(Set<StringEdge> edges, Set<String> labels) {
		HashSet<StringEdge> filtered = new HashSet<>(edges.size());
		for (StringEdge edge : edges) {
			if (labels.contains(edge.getLabel()))
				filtered.add(edge);
		}
		return filtered;
	}

	private static HashSet<String> getLeastFrequentLabels(Object2IntOpenHashMap<String> relationCount) {
		int smallest = Integer.MAX_VALUE;
		// because there can be multiple labels with the same (smallest) frequency, two scans are required
		// first scan for the smallest
		for (String curLabel : relationCount.keySet()) {
			int count = relationCount.getInt(curLabel);
			if (count < smallest) {
				smallest = count;
			}
		}
		HashSet<String> labels = new HashSet<>();
		// now scan for all the labels containing that count
		for (String curLabel : relationCount.keySet()) {
			int count = relationCount.getInt(curLabel);
			if (count == smallest) {
				labels.add(curLabel);
			}
		}
		return labels;
	}

	private static HashSet<String> getMostFrequentLabels(Object2IntOpenHashMap<String> relationCount) {
		int largest = -Integer.MAX_VALUE;
		// because there can be multiple labels with the same (largest) frequency, two scans are required
		// first scan for the largest
		for (String curLabel : relationCount.keySet()) {
			int count = relationCount.getInt(curLabel);
			if (count > largest) {
				largest = count;
			}
		}
		HashSet<String> labels = new HashSet<>();
		// now scan for all the labels containing that count
		for (String curLabel : relationCount.keySet()) {
			int count = relationCount.getInt(curLabel);
			if (count == largest) {
				labels.add(curLabel);
			}
		}
		return labels;
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

	public static StringGraph initializePattern(StringGraph kbGraph, StringGraph pattern, RandomGenerator random) {
		// randomly add edges to an empty graph
		for (int i = 0; i < 3; i++) { // add N edges
			PatternFinderUtils.mutatePattern(kbGraph, random, pattern, true);
		}
		return pattern;
	}

	public static StringGraph initializePattern(StringGraph kbGraph, RandomGenerator random) {
		return initializePattern(kbGraph, new StringGraph(), random);
	}

}
