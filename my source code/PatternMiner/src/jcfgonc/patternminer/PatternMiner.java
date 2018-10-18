package jcfgonc.patternminer;

import javax.swing.UIManager;

import graph.GraphAlgorithms;
import graph.GraphReadWrite;
import graph.StringGraph;
import jcfgonc.genetic.GeneticAlgorithm;
import jcfgonc.genetic.operators.GeneticOperations;
import structures.CSVWriter;
import structures.ObjectIndex;
import structures.Ticker;

public class PatternMiner {

	public static void main(String[] args) throws Exception {
		Ticker ticker = new Ticker();
		String path = "kb/conceptnet5v43_no_invalid_chars.csv";

		System.out.println("loading... " + path);
		StringGraph graph = new StringGraph(1 << 24, 1 << 24, 1 << 24, 1 << 24);
		ticker.getTimeDeltaLastCall();
		GraphReadWrite.readCSV(path, graph);

		ObjectIndex<String> vertexLabels = new ObjectIndex<>();
		ObjectIndex<String> relationLabels = new ObjectIndex<>();
		GraphAlgorithms.convertStringGraph2IntDirectedMultiGraph(graph, vertexLabels, relationLabels);
		// graph.showStructureSizes();
		System.out.println(ticker.getTimeDeltaLastCall());

		System.out.println("vertices\t" + graph.getVertexSet().size());
		System.out.println("edges   \t" + graph.edgeSet().size());
		System.out.println("-------");

		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

		GeneticOperations<PatternChromosome> mgo = new PatternGeneticOperations(graph);
		CSVWriter csvw = new CSVWriter();
		GeneticAlgorithm<PatternChromosome> ga = new GeneticAlgorithm<>(mgo, csvw);
		ga.execute();
		PatternChromosome best = ga.getBestGenes();

//		System.out.println("mapping done: " + best.getMapping().size() + " pairs");
	}

}
