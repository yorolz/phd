package jcfgonc.blender.logic;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.util.FastMath;

import com.githhub.aaronbembenek.querykb.Conjunct;
import com.githhub.aaronbembenek.querykb.KnowledgeBase;
import com.githhub.aaronbembenek.querykb.KnowledgeBase.KnowledgeBaseBuilder;
import com.githhub.aaronbembenek.querykb.Query;

import graph.GraphAlgorithms;
import graph.GraphReadWrite;
import graph.StringEdge;
import graph.StringGraph;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import jcfgonc.blender.BlenderMoConfig;
import jcfgonc.blender.structures.ConceptPair;
import jcfgonc.blender.structures.Mapping;
import structures.CSVReader;
import structures.ListOfSet;
import structures.Ticker;

public class LogicUtils {

	/**
	 * the stringraph's vertices must be of the form text"separator"text
	 * 
	 * @param sg
	 * @param mapping
	 */
	public static void addStringGraphVerticesToMapping(StringGraph sg, Mapping<String> mapping, String separator) {
		for (String vertice : sg.getVertexSet()) {
			String[] tokens = vertice.split(separator);
			if (tokens.length == 2) {
				mapping.add(tokens[0], tokens[1]);
			} else {
				System.out.format("vertice %s can't be tokenized using separator %s\n", vertice, separator);
			}
		}
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

	/**
	 * Calculates and returns the relation histogram
	 * 
	 * @param relationToCountMap a map of string (the relation) to integer (relation count), i.e, the absolute frequency table
	 * @return
	 */
	public static DescriptiveStatistics calculateRelationHistogram(Object2IntOpenHashMap<String> relationToCountMap) {
		int size = relationToCountMap.size();
		if (size == 0) {
			return null;
		}
		double[] count_d = new double[size];
		int i = 0;
		for (String key : relationToCountMap.keySet()) {
			count_d[i++] = relationToCountMap.getInt(key);
		}
		DescriptiveStatistics ds = new DescriptiveStatistics(count_d);
		return ds;
	}

	/**
	 * given a blend in KB form and a frame as a query, counts the occurrences of the frame in the blend using aaron's querykb tool
	 * 
	 * @param blend
	 * @param frame
	 */
	public static double countFrameMatchesBI(KnowledgeBase blendKB, Query frameQuery) {
		// 3. apply query to KnowledgeBase
		BigInteger matches = blendKB.count(frameQuery, BlenderMoConfig.BLOCK_SIZE, BlenderMoConfig.PARALLEL_LIMIT, BlenderMoConfig.SOLUTION_LIMIT,
				true, Long.valueOf(BlenderMoConfig.QUERY_TIMEOUT_SECONDS));

		assert matches.compareTo(BigInteger.ZERO) >= 0; // matches will always be >= 0

		if (matches.compareTo(BigInteger.ONE) < 0) { // zero matches
			return -1.0d;
		} else { // more than than one match
			return log2(matches) / FastMath.log(2, 10);
		}
	}

	/**
	 * given a blend in KB form and a frame as a query, counts the occurrences of the frame in the blend using aaron's querykb tool
	 * 
	 * @param blend
	 * @param frame
	 */
	public static double countFrameMatchesBI(StringGraph blend, Query frameQuery) {
		// TODO: optimization: convert blend to KB outside this function (to cache between multiple blend calls)

		// assume the blend has edges as well as the frame
		assert (blend.numberOfEdges() > 0);
		// assert (frame.numberOfEdges() > 0);

		// 1. convert blend to querykb's KB
		KnowledgeBase blendKB = buildKnowledgeBase(blend);

		// 2. convert frame to querykb's query
		// TODO: optimization: convert frame to query outside this function (to cache during the execution of the whole program)
		// List<Conjunct> conjunctList = createConjunctionFromStringGraph(frame, null);
		// Query frameQuery = Query.make(conjunctList);

		return countFrameMatchesBI(blendKB, frameQuery);
	}

	/**
	 * creates a mapping from each element of the given concept set to a unique and consecutive variable named Xi, i in 0...set size.
	 * 
	 * @param pattern
	 * @return
	 */
	public static HashMap<String, String> createConceptToVariableMapping(Set<String> vertexSet) {
		HashMap<String, String> conceptToVariable = new HashMap<>(vertexSet.size() * 2);
		int varCounter = 0;
		for (String concept : vertexSet) {
			String varName = "X" + varCounter;
			conceptToVariable.put(concept, varName);
			varCounter++;
		}
		return conceptToVariable;
	}

	/**
	 * creates a querykb query
	 * 
	 * @param graph
	 * @return
	 */
	public static Query createQueryFromStringGraph(final StringGraph graph) {
		return Query.make(createConjunctionFromStringGraph(graph, null));
	}

	/**
	 * creates a querykb conjunction to be used as a query using the given variable<=>variable mapping. If conceptToVariable is null then the edge's
	 * concepts names are used as variables instead of the mapping.
	 * 
	 * @param pattern
	 * @param conceptToVariable
	 * @return
	 */
	public static List<Conjunct> createConjunctionFromStringGraph(final StringGraph pattern, final HashMap<String, String> conceptToVariable) {
		ArrayList<Conjunct> conjunctList = new ArrayList<>();
		if (conceptToVariable != null) {
			// create the query as a conjunction of terms
			for (StringEdge edge : pattern.edgeSet()) {
				String edgeLabel = edge.getLabel();
				String source = conceptToVariable.get(edge.getSource());
				String target = conceptToVariable.get(edge.getTarget());
				conjunctList.add(Conjunct.make(edgeLabel, source, target));
			}
		} else {
			// create the query as a conjunction of terms
			for (StringEdge edge : pattern.edgeSet()) {
				String edgeLabel = edge.getLabel();
				String sourceVar = edge.getSource();
				String targetVar = edge.getTarget();
				conjunctList.add(Conjunct.make(edgeLabel, sourceVar, targetVar));
			}
		}
		return conjunctList;
	}

	/**
	 * generates a graph with using the variables' map instead of the original concepts. Used to convert a specific graph to a frame.
	 * 
	 * @param graph
	 * @param conceptToVariable
	 * @return
	 */
	public static StringGraph createPatternWithVars(StringGraph graph, HashMap<String, String> conceptToVariable) {
		StringGraph patternWithVars = new StringGraph();

		// create the query as a conjunction of terms
		for (StringEdge edge : graph.edgeSet()) {

			String edgeLabel = edge.getLabel();
			String sourceVar = edge.getSource();
			String targetVar = edge.getTarget();
			if (conceptToVariable != null) {
				sourceVar = conceptToVariable.get(sourceVar);
				targetVar = conceptToVariable.get(targetVar);
			}
			patternWithVars.addEdge(sourceVar, targetVar, edgeLabel);
		}

		assert patternWithVars.numberOfEdges() == graph.numberOfEdges(); // bug check
		return patternWithVars;
	}

	public static String generateNewConcept(ConceptPair<String> mapping, RandomGenerator randomGenerator) {
		String leftConcept = mapping.getLeftConcept();
		String rightConcept = mapping.getRightConcept();
		// put none, one, or all concepts in a new concept
		int mergeControl = randomGenerator.nextInt(3);
		switch (mergeControl) {
		case 0:
			return leftConcept;
		case 1:
			return rightConcept;
		case 2:
			return leftConcept + "|" + rightConcept;
		}
		return null; // because of compiler error, should not ever be executed
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

	/**
	 * Check for components and leave only one, done IN-PLACE. If random is not null, select a random component to be the whole graph. Otherwise
	 * select the largest component.
	 * 
	 * @param random
	 * @param genes
	 * @return
	 * @return the components
	 */
	public static StringGraph removeAdditionalComponents(StringGraph pattern, RandomGenerator random) {
		// calculate components (sets of vertices)
		ListOfSet<String> components = GraphAlgorithms.extractGraphComponents(pattern);

		if (components.size() == 1) {
			HashSet<String> component0 = components.getSetAt(0);
			if (component0.isEmpty()) {
				System.err.println("### got an empty component");
			}
			return pattern; // break, no need to filter the pattern (ie remove additional components)
		}

		System.err.format("### got a pattern with %d components: %s\n", components.size(), components.toString());

		HashSet<String> largestComponent;
		if (random == null) {
			largestComponent = components.getSetAt(0);
		} else {
			largestComponent = components.getRandomSet(random);
		}

		// filter pattern with mask and store it back
		return new StringGraph(pattern, largestComponent);
	}

	public static List<StringGraph> readPatternResultsDelimitedFile(File file, String columnSeparator, boolean fileHasHeader, int graphColumnID)
			throws IOException {
		System.out.println("loading StringGraph CSV File " + file.getName());
		CSVReader csvData = CSVReader.readCSV(columnSeparator, file, fileHasHeader);
		int nFrames = csvData.getNumberOfRows();
		StringGraph[] frames = new StringGraph[nFrames];
		ArrayList<ArrayList<String>> csvRows = csvData.getRows();

		IntStream.range(0, nFrames).parallel().forEach(id -> { // parallelize conversion
			try {
				ArrayList<String> row = csvRows.get(id);
				String graphAsString = row.get(graphColumnID);
				frames[id] = GraphReadWrite.readCSVFromString(graphAsString);
			} catch (NoSuchFileException e) {
			} catch (IOException e) {
			}
		});
		return Arrays.asList(frames);
	}

}
