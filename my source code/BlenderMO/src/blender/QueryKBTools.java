package blender;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

import com.githhub.aaronbembenek.querykb.Conjunct;
import com.githhub.aaronbembenek.querykb.KnowledgeBase;
import com.githhub.aaronbembenek.querykb.KnowledgeBase.KnowledgeBaseBuilder;
import com.githhub.aaronbembenek.querykb.Query;

import graph.GraphAlgorithms;
import graph.GraphReadWrite;
import graph.StringEdge;
import graph.StringGraph;
import structures.CSVReader;

public class QueryKBTools {
	/**
	 * number of threads in querykb tool
	 */
	private static final int parallelLimit = 1;
	private static final int blockSize = 256;

	/**
	 * converts all concepts/vertices to variables in Prolog
	 * 
	 * @param pattern
	 * @return unique map of concepts to variables
	 */
	public static HashMap<String, String> createConceptToVariableMapping(StringGraph pattern) {
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

	/**
	 * Creates a querykb conjunction to be used as a query using the given variable<=>variable mapping. If conceptToVariable is null, graph's vertices are not converted to variables.
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

	public static KnowledgeBase buildKnowledgeBase(StringGraph kbGraph) {
		KnowledgeBaseBuilder kbb = new KnowledgeBaseBuilder();
		// Ticker ticker = new Ticker();

		for (StringEdge edge : kbGraph.edgeSet()) {
			String label = edge.getLabel();
			String source = edge.getSource();
			String target = edge.getTarget();
			kbb.addFact(label, source, target);
		}

		KnowledgeBase kb = kbb.build();
		// System.out.println("build took " + ticker.getElapsedTime() + " s");
		return kb;
	}

	public static ArrayList<StringGraph> createPatternFrameListFromCSV(String columnSeparator, File file, boolean fileHasHeader) throws IOException {
		CSVReader csvData = CSVReader.readCSV(columnSeparator, file, fileHasHeader);
		System.out.printf("%s loaded.\n", file);
		int nGraphs = csvData.getNumberOfRows();
		StringGraph[] graphs = new StringGraph[nGraphs];

		ArrayList<ArrayList<String>> rows = csvData.getRows();
		IntStream.range(0, nGraphs).parallel().forEach(id -> {
			try {
				ArrayList<String> row = rows.get(id);
				StringGraph g = GraphReadWrite.readCSVFromString(row.get(8));
				graphs[id] = g;
			} catch (Exception e) {
				e.printStackTrace();
			}
		});

		return GraphAlgorithms.arrayToArrayList(graphs);
	}

	/**
	 * graph1 is searched for/counted in kb with graph's vertices converted to variables.
	 * 
	 * @param kb
	 * @param graphPattern
	 * @return
	 */
	public static BigInteger countPatternMatchesInKBWithPatternVars(KnowledgeBase kb, StringGraph graphPattern) {
		HashMap<String, String> conceptToVariable = createConceptToVariableMapping(graphPattern);
		ArrayList<Conjunct> conjunctList = createConjunctionFromStringGraph(graphPattern, conceptToVariable);
		// TODO: you may want to cache this
		Query q = Query.make(conjunctList);
		BigInteger matches = kb.count(q, QueryKBTools.blockSize, QueryKBTools.parallelLimit, BigInteger.ONE, true, null);
		return matches;
	}

	/**
	 * graph1 is searched for/counted in kb.
	 * 
	 * @param kb
	 * @param graphPattern
	 * @return
	 */
	public static BigInteger countPatternMatchesInKBNoPatternVars(KnowledgeBase kb, StringGraph graphPattern) {
		ArrayList<Conjunct> conjunctList = createConjunctionFromStringGraph(graphPattern, null);
		// TODO: you may want to cache this
		Query q = Query.make(conjunctList);
		BigInteger matches = kb.count(q, QueryKBTools.blockSize, QueryKBTools.parallelLimit, BigInteger.ONE, true, null);
		return matches;
	}

}
