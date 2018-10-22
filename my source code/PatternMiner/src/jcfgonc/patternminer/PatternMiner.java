package jcfgonc.patternminer;

import javax.swing.UIManager;

import graph.GraphReadWrite;
import graph.StringGraph;
import jcfgonc.genetic.GeneticAlgorithm;
import jcfgonc.genetic.operators.GeneticOperations;
import structures.CSVWriter;
import structures.Ticker;

public class PatternMiner {

	public static void main(String[] args) throws Exception {
		String path = "../ConceptNet5/kb/conceptnet5v43_no_invalid_chars.csv";

		System.out.println("loading... " + path);
		StringGraph kb = new StringGraph(1 << 24, 1 << 24, 1 << 24, 1 << 24);
		Ticker ticker = new Ticker();
		GraphReadWrite.readCSV(path, kb);
		kb.showStructureSizes();
		System.out.println("loading took " + ticker.getTimeDeltaLastCall() + " s");

		System.out.println("vertices\t" + kb.getVertexSet().size());
		System.out.println("edges   \t" + kb.edgeSet().size());
		System.out.println("-------");

		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

		GeneticOperations<PatternChromosome> mgo = new PatternGeneticOperations(kb);
		CSVWriter csvw = null;// new CSVWriter();
		GeneticAlgorithm<PatternChromosome> ga = new GeneticAlgorithm<>(mgo, csvw);
		ga.execute();
		@SuppressWarnings("unused")
		PatternChromosome best = ga.getBestGenes();

		// System.out.println("mapping done: " + best.getMapping().size() + " pairs");
	}

}
