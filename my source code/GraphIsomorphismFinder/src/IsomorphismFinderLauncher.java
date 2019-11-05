import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import structures.CSVReader;
import structures.GlobalFileWriter;
import structures.MapOfList;

public class IsomorphismFinderLauncher {
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

	public static ArrayList<GraphData> createGraphsFromCSV(String columnSeparator, File file, boolean fileHasHeader) throws IOException {
		CSVReader csvData = CSVReader.readCSV(columnSeparator, file, fileHasHeader);
		System.out.printf("%s loaded.\n", file);
		int nGraphs = csvData.getNumberOfRows();
		GraphData[] graphs = new GraphData[nGraphs];

		ArrayList<ArrayList<String>> rows = csvData.getRows();
		IntStream.range(0, nGraphs).parallel().forEach(id -> {
			try {
				ArrayList<String> row = rows.get(id);
				StringGraph g = GraphReadWrite.readCSVFromString(row.get(8));
				GraphData gd = new GraphData(Integer.toString(id), g);
				graphs[id] = gd;
			} catch (Exception e) {
				e.printStackTrace();
			}
		});

		return GraphAlgorithms.arrayToArrayList(graphs);
	}

	public static void main(String[] args) throws IOException {
		// read graphs
		ArrayList<GraphData> graphs = createGraphsFromCSV("\t", new File("..\\PatternMiner\\mergedResultsBigV2.csv"), true);
		// MapOfList<Integer, StringGraph> graphsPerVertices = new MapOfList<>();
		MapOfList<Object2IntOpenHashMap<String>, StringGraph> graphGroups = new MapOfList<Object2IntOpenHashMap<String>, StringGraph>();

		// organize graphs in groups according to the relations' histograms
		for (GraphData gd : graphs) {
			StringGraph graph = gd.getGraph();
			Object2IntOpenHashMap<String> relations = GraphAlgorithms.countRelations(graph);
			graphGroups.put(relations, graph);
			// int vert = graph.numberOfVertices();
			// graphsPerVertices.put(vert, graph);
		}

		// check for isomorphisms within groups
		HashSet<StringGraph> duplicatedGraphs = new HashSet<StringGraph>();
		Set<Object2IntOpenHashMap<String>> keySet = graphGroups.keySet();
		for (Object2IntOpenHashMap<String> key : keySet) {
			List<StringGraph> localGroup = graphGroups.get(key);
			// System.out.printf("%s\t%d\n", key, localGroup.size());
			duplicatedGraphs.addAll(findIsomorphisms(localGroup));
		}

		System.out.printf("got %d duplicated graphs", duplicatedGraphs.size());
		GlobalFileWriter.createNewFile("useless graphs.txt");
		duplicatedGraphs.stream().forEach(u -> {
			GlobalFileWriter.writeLineUnsync(u.toString());
		});
		GlobalFileWriter.close();

		System.lineSeparator();
	}

	/**
	 * finds isomorphic graphs inside structural groups (based on their relations' histograms)
	 * 
	 * @param graphs
	 * @return
	 */
	private static Set<StringGraph> findIsomorphisms(List<StringGraph> graphs) {
		// repeated (isomorphic to some) graphs
		Set<StringGraph> uselessGraphs = Collections.newSetFromMap(new ConcurrentHashMap<StringGraph, Boolean>());
		if (graphs.size() > 1) {
			// parallelize bigger (outer) for
			IntStream.range(0, graphs.size() - 1).parallel().forEach(i -> {
				StringGraph graph0 = graphs.get(i);
				if (!uselessGraphs.contains(graph0)) {
					KnowledgeBase kb = buildKnowledgeBase(graph0);
					for (int j = i + 1; j < graphs.size(); j++) {
						// find graph1 in graph0
						StringGraph graph1 = graphs.get(j);
						if (!uselessGraphs.contains(graph1)) {
							if (isGraphIsomorphicToKnowledgeBase(kb, graph1)) {
								uselessGraphs.add(graph1);
							}
						}
					} // inner j for - variant graph1
				}
			});// i parallel for - variant knowledge base (graph0)
		}
		return uselessGraphs;
	}

	private static boolean isGraphIsomorphicToKnowledgeBase(KnowledgeBase kb, StringGraph graph1) {
		HashMap<String, String> conceptToVariable = createConceptToVariableMapping(graph1);
		ArrayList<Conjunct> conjunctList = createConjunctionFromStringGraph(graph1, conceptToVariable);
		Query q = Query.make(conjunctList);
		BigInteger matches = kb.count(q, 256, 1, BigInteger.ONE, true, null);
		// graph1 contained in graph0
		boolean isIsomorphic = matches.compareTo(BigInteger.ZERO) > 0;
		return isIsomorphic;
	}
}
