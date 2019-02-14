package jcfgonc.patternminer;

import java.io.IOException;
import java.io.StringReader;
import java.math.BigInteger;
import java.nio.file.NoSuchFileException;
import java.util.HashSet;

import javax.swing.UIManager;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import com.githhub.aaronbembenek.querykb.KnowledgeBase;
import com.githhub.aaronbembenek.querykb.Query;

import graph.GraphReadWrite;
import graph.StringGraph;
import jcfgonc.genetic.GeneticAlgorithm;
import jcfgonc.genetic.operators.GeneticOperations;
import structures.CSVWriter;
import structures.ListOfSet;
import structures.Ticker;

public class PatternMiner {

	public static void main(String[] args) throws Exception {

		// testLoopDetector();

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
		String s = "X12,receivesaction,X9;X7,partof,X11;X10,hasprerequisite,X0;X2,haslastsubevent,X13;X11,capableof,X3;X12,atlocation,X11;X6,hasfirstsubevent,X0;X6,motivatedbygoal,X5;X12,hasproperty,X6;X10,usedfor,X6;X6,causes,X1;X6,hassubevent,X13;X4,locatednear,X11;X8,causesdesire,X10;";
		testQuery(s, kb);
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

	@SuppressWarnings("unused")
	private static void testLoopDetector() throws IOException {
		StringGraph pattern = new StringGraph();
		GraphReadWrite.readAutoDetect("0.tgf", pattern);

		PatternChromosome pc = new PatternChromosome(pattern);
		pc.components = new ListOfSet<>();
		pc.components.add(new HashSet<>());

		PatternFinderUtils.countLoops(pc);
		System.out.println(pc.loops);

		System.exit(0);
	}

	@SuppressWarnings("unused")
	private static void testQuery(String query, KnowledgeBase kb) throws IOException, NoSuchFileException {
		query = query.replaceAll(";", "\r\n");
		StringGraph graph = new StringGraph();
		StringReader sr = new StringReader(query);
		GraphReadWrite.readCSV(sr, graph);
		sr.close();
		System.out.println("query has " + graph.edgeSet().size() + " edges");

		Query q = Query.make(PatternFinderUtils.createConjunctionFromStringGraph(graph, null));
		Ticker t = new Ticker();
		BigInteger count = kb.count(q, PatternMinerConfig.BLOCK_SIZE, PatternMinerConfig.PARALLEL_LIMIT, PatternMinerConfig.QUERY_TIMEOUT_MS);
		System.out.printf("time\t%f\tcount\t%s\n", t.getElapsedTime(), count.toString());
	}

	@SuppressWarnings("unused")
	private static void benchmarkQuery(KnowledgeBase kb, StringGraph graph) {
		// for (int blockSize = 1; blockSize < 8192; blockSize *= 2) {
		// for (int parallelLimit = 0; parallelLimit < 8; parallelLimit++) {
		for (int timeLimit = 1; timeLimit <= 32; timeLimit *= 2) {
			DescriptiveStatistics ds = new DescriptiveStatistics();
			for (int i = 0; i < 5; i++) {
				PatternChromosome genes = new PatternChromosome(graph);
				PatternMinerConfig.QUERY_TIMEOUT_MS = timeLimit * 1000 * 60;
				PatternFinderUtils.countPatternMatchesBI(genes, kb);
				ds.addValue(genes.matches);

				System.out.println("\ttime\t" + genes.countingTime + //
						"\tmatches\t" + genes.matches + //
						"\tpattern\t" + genes.patternWithVars);
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
