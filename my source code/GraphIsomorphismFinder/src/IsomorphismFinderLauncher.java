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
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import structures.CSVReader;
import structures.MapOfList;
import structures.MapOfSet;
import structures.Ticker;

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
		System.out.format("csv file %s loaded...\n", file);
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
		ArrayList<GraphData> graphs = createGraphsFromCSV("\t", new File("mergedResultsBig.csv"), true);
	//	MapOfList<Integer, StringGraph> graphsPerVertices = new MapOfList<>();
		MapOfList<Object2IntOpenHashMap<String>, StringGraph> graphGroups=new MapOfList<Object2IntOpenHashMap<String>, StringGraph>();

		// organize graphs in groups
		for (GraphData gd : graphs) {
			StringGraph graph = gd.getGraph();
			Object2IntOpenHashMap<String> relations = GraphAlgorithms.countRelations(graph);
			graphGroups.put(relations, graph);
		//	int vert = graph.numberOfVertices();
			//graphsPerVertices.put(vert, graph);
		}
		// check for isomorphisms within groups
		//TODO perhaps it is better to parallelize here
		HashSet<StringGraph> repeated = new HashSet<StringGraph>();
		for (Object2IntOpenHashMap<String> key : graphGroups.keySet()) {
			List<StringGraph> localGroup = graphGroups.get(key);
	//		System.out.printf("%s\t%d\n", key, localGroup.size());
			Set<StringGraph> r = findIsomorphisms(localGroup);
			repeated.addAll(r);
		}
//		System.out.println(graphsPerVertices.keySet());

		System.lineSeparator();
	}

	private static Set<StringGraph> findIsomorphisms(List<StringGraph> graphs) {
	//	HashSet<StringGraph> isomorphic = new HashSet<>();
		//repeated (isomorphic to some) graphs
		Set<StringGraph> isomorphic = Collections.newSetFromMap(new ConcurrentHashMap<StringGraph, Boolean>());
		for (int i = 0; i < graphs.size()-1; i++) {
			StringGraph graph0 = graphs.get(i);
			if(isomorphic.contains(graph0))
				continue;
			KnowledgeBase kb = buildKnowledgeBase(graph0);
			IntStream.range(i+1, graphs.size()).parallel().forEach(j -> {
				// find graph1 in graph0
				StringGraph graph1 = graphs.get(j);
				if (!isomorphic.contains(graph1)) {
					HashMap<String, String> conceptToVariable = createConceptToVariableMapping(graph1);
					ArrayList<Conjunct> conjunctList = createConjunctionFromStringGraph(graph1, conceptToVariable);
					Query q = Query.make(conjunctList);
					BigInteger matches = kb.count(q, 256, 4, null, true, (long) (5 * 60));
					//graph1 contained in graph0
					if (matches.compareTo(BigInteger.ZERO) > 0) {
						isomorphic.add(graph1);
						System.out.printf("%s \t ~- \t %s\n", graph0, graph1);
					} else {
						// System.out.println("NAY\t" + graph1);
					}
//					System.lineSeparator();	
				}
			});
		}
		return isomorphic;
	}
}