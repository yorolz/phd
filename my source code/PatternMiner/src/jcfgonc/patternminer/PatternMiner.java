package jcfgonc.patternminer;

import javax.swing.UIManager;

import com.githhub.aaronbembenek.querykb.KnowledgeBase;

import graph.GraphReadWrite;
import graph.StringGraph;
import jcfgonc.genetic.GeneticAlgorithm;
import jcfgonc.genetic.operators.GeneticOperations;
import structures.CSVWriter;
import structures.Ticker;

public class PatternMiner {

	public static void main(String[] args) throws Exception {

		//Thread.sleep(30 * 1000);

		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

		String path = "../ConceptNet5/kb/conceptnet5v43_no_invalid_chars.csv";

		System.out.println("loading... " + path);
		StringGraph inputSpace = new StringGraph(1 << 24, 1 << 24, 1 << 24, 1 << 24);
		Ticker ticker = new Ticker();
		GraphReadWrite.readCSV(path, inputSpace);
		inputSpace.showStructureSizes();
		System.out.println("loading took " + ticker.getTimeDeltaLastCall() + " s");

		System.out.println("vertices\t" + inputSpace.getVertexSet().size());
		System.out.println("edges   \t" + inputSpace.edgeSet().size());
		System.out.println("-------");

		KnowledgeBaseBuilder kbb = new KnowledgeBaseBuilder();
		ticker.resetTicker();
		System.out.println("kbb.addFacts(inputSpace)");
		kbb.addFacts(inputSpace);
		System.out.println("kbb.build()");
		KnowledgeBase kb = kbb.build();
		System.out.println("build took " + ticker.getElapsedTime() + " s");

		GeneticOperations<PatternChromosome> mgo = new PatternGeneticOperations(inputSpace, kb);
		CSVWriter csvw = null;// new CSVWriter();
		GeneticAlgorithm<PatternChromosome> ga = new GeneticAlgorithm<>(mgo, csvw);
		ga.execute();
		@SuppressWarnings("unused")
		PatternChromosome best = ga.getBestGenes();
		System.exit(0);
	}
}
