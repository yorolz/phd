package jcfgonc.patternminer;

import java.io.StringReader;
import java.util.Arrays;

import javax.swing.UIManager;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import com.githhub.aaronbembenek.querykb.Conjunct;
import com.githhub.aaronbembenek.querykb.KnowledgeBase;
import com.githhub.aaronbembenek.querykb.Query;
import com.githhub.aaronbembenek.querykb.parse.Parser;
import com.githhub.aaronbembenek.querykb.parse.Tokenizer;

import graph.GraphReadWrite;
import graph.StringGraph;
import jcfgonc.genetic.GeneticAlgorithm;
import jcfgonc.genetic.operators.GeneticOperations;
import structures.CSVWriter;
import structures.Ticker;

public class PatternMiner {

	public static void main(String[] args) throws Exception {
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

		Tokenizer t = new Tokenizer(new StringReader(":- isa(X2,X3),isa(X1,X0),synonym(X1,X3)."));
		// Tokenizer t = new Tokenizer(new StringReader(":- isa(X3,X0),isa(X3,X1),isa(X2,X1)."));
		Query q = Parser.parseQuery(t);
		for (int parallelLimit = 0; parallelLimit < 8; parallelLimit++) {
			DescriptiveStatistics ds = new DescriptiveStatistics();
			for (int i = 0; i < 16; i++) {
				ticker.resetTicker();
				// System.out.println("Making query: " + q);
				long count = kb.count(q, 4096, parallelLimit, 8000000);
				// System.out.println("Found " + count + " solution(s).");
				double time = ticker.getElapsedTime();
				ds.addValue(time);
				// System.out.println("query took " + time + " s");
			}
			System.out.println(parallelLimit + "\t" + ds.getMin());
		}
		System.exit(0);

		GeneticOperations<PatternChromosome> mgo = new PatternGeneticOperations(inputSpace);
		CSVWriter csvw = null;// new CSVWriter();
		GeneticAlgorithm<PatternChromosome> ga = new GeneticAlgorithm<>(mgo, csvw);
		ga.execute();
		@SuppressWarnings("unused")
		PatternChromosome best = ga.getBestGenes();

		// System.out.println("mapping done: " + best.getMapping().size() + " pairs");
	}

}
