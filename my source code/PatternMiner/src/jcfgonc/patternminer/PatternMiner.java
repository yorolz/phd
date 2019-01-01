package jcfgonc.patternminer;

import java.io.IOException;
import java.io.StringReader;
import java.math.BigInteger;
import java.nio.file.NoSuchFileException;

import javax.swing.UIManager;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import com.githhub.aaronbembenek.querykb.KnowledgeBase;
import com.githhub.aaronbembenek.querykb.Query;

import graph.GraphReadWrite;
import graph.StringGraph;
import jcfgonc.genetic.GeneticAlgorithm;
import jcfgonc.genetic.operators.GeneticOperations;
import structures.CSVWriter;
import structures.Ticker;

public class PatternMiner {

	public static void main(String[] args) throws Exception {

		// Thread.sleep(30 * 1000);

		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

		String path = PatternMinerConfig.FILE_PATH;

		System.out.println("loading... " + path);
		StringGraph inputSpace = new StringGraph(1 << 24, 1 << 24, 1 << 24, 1 << 24);
		Ticker ticker = new Ticker();
		GraphReadWrite.readCSV(path, inputSpace);
		inputSpace.showStructureSizes();
		System.out.println("loading took " + ticker.getTimeDeltaLastCall() + " s");

		System.out.println("vertices\t" + inputSpace.getVertexSet().size());
		System.out.println("edges   \t" + inputSpace.edgeSet().size());
		System.out.println("-------");

		// removeEdges(inputSpace);

		KnowledgeBaseBuilder kbb = new KnowledgeBaseBuilder();
		ticker.resetTicker();
		System.out.println("kbb.addFacts(inputSpace)");
		kbb.addFacts(inputSpace);
		System.out.println("kbb.build()");
		KnowledgeBase kb = kbb.build();
		System.out.println("build took " + ticker.getElapsedTime() + " s");

		// --------------------
		StringGraph graph = createStringGraphFromString("X9,atlocation,X0; X8,atlocation,X0; X7,atlocation,X0; X10,atlocation,X0; X6,atlocation,X0; X10,atlocation,X3; X11,atlocation,X5; X10,atlocation,X5; X2,atlocation,X0; X1,atlocation,X0; X4,atlocation,X3; ");
		benchmarkQuery(kb, graph);
//		Query q = Query.make(PatternFinderUtils.createConjunctionFromStringGraph(graph, null, null, null));
//		testQuery(kb, q);
		System.exit(0);
		// ---------------

		GeneticOperations<PatternChromosome> mgo = new PatternGeneticOperations(inputSpace, kb);
		CSVWriter csvw = null;// new CSVWriter();
		GeneticAlgorithm<PatternChromosome> ga = new GeneticAlgorithm<>(mgo, csvw);
		ga.execute();
		@SuppressWarnings("unused")
		PatternChromosome best = ga.getBestGenes();
		System.exit(0);
	}

	private static void testQuery(KnowledgeBase kb, Query q) throws IOException, NoSuchFileException {
		kb.count(q, PatternMinerConfig.BLOCK_SIZE, PatternMinerConfig.PARALLEL_LIMIT, PatternMinerConfig.QUERY_TIMEOUT_MS);

		System.exit(0);
	}

	private static StringGraph createStringGraphFromString(String pattern) throws IOException, NoSuchFileException {
		String graphCSV = pattern.replace("; ", "\r\n");
		StringGraph graph = new StringGraph();
		StringReader sr = new StringReader(graphCSV);
		GraphReadWrite.readCSV(sr, graph);
		sr.close();
		return graph;
	}

	private static void benchmarkQuery(KnowledgeBase kb, StringGraph graph) {
		// for (int blockSize = 1; blockSize < 8192; blockSize *= 2) {
		// for (int parallelLimit = 0; parallelLimit < 8; parallelLimit++) {
		for (int timeLimit = 1; timeLimit <= 32; timeLimit *= 2) {
			DescriptiveStatistics ds = new DescriptiveStatistics();
			for (int i = 0; i < 5; i++) {
				PatternChromosome genes = new PatternChromosome(graph);
				PatternMinerConfig.QUERY_TIMEOUT_MS = timeLimit * 1000 * 60;
				double matches = PatternFinderUtils.countPatternMatchesBI(genes, kb);
				ds.addValue(matches);

				System.out.println("\ttime\t" + genes.countingTime + //
						"\tmatches\t" + genes.matches + //
						"\tpattern\t" + genes.patternAsString);
			}
			System.out.println(ds.toString());
		}
	}

	@SuppressWarnings("unused")
	private static void removeEdges(StringGraph inputSpace) throws IOException {
		inputSpace.removeEdges("isa");
		inputSpace.removeEdges("derivedfrom");
		inputSpace.removeEdges("synonym");
		inputSpace.removeEdges("similarto");

		GraphReadWrite.writeCSV("conceptnet5v5.csv", inputSpace);

		System.exit(0);
	}
}
