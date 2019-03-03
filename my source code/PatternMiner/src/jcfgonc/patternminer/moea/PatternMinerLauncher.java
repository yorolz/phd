package jcfgonc.patternminer.moea;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.math.BigInteger;
import java.nio.file.NoSuchFileException;
import java.util.Properties;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.apache.commons.math3.random.Well44497a;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Problem;
import org.moeaframework.core.Variation;
import org.moeaframework.core.spi.OperatorFactory;
import org.moeaframework.core.spi.OperatorProvider;
import org.moeaframework.util.TypedProperties;

import com.githhub.aaronbembenek.querykb.KnowledgeBase;
import com.githhub.aaronbembenek.querykb.Query;

import graph.GraphReadWrite;
import graph.StringGraph;
import jcfgonc.patternminer.KnowledgeBaseBuilder;
import jcfgonc.patternminer.PatternChromosome;
import jcfgonc.patternminer.PatternFinderUtils;
import jcfgonc.patternminer.PatternMinerConfig;
import structures.Ticker;

/**
 * Demonstrates how a new problem is defined and used within the MOEA Framework.
 */
public class PatternMinerLauncher {

	public static void main(String[] args)
			throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException, NoSuchFileException, IOException {
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

		String path = PatternMinerConfig.FILE_PATH;

		System.out.println("loading... " + path);
		StringGraph kbGraph = new StringGraph(1 << 24, 1 << 24, 1 << 24, 1 << 24);
		Ticker ticker = new Ticker();
		GraphReadWrite.readCSV(path, kbGraph);
		kbGraph.showStructureSizes();
		System.out.println("loading took " + ticker.getTimeDeltaLastCall() + " s");
		System.out.println("-------");

		KnowledgeBaseBuilder kbb = new KnowledgeBaseBuilder();
		ticker.resetTicker();
		System.out.println("kbb.addFacts(inputSpace)");
		kbb.addFacts(kbGraph);
		System.out.println("kbb.build()");
		KnowledgeBase kb = kbb.build();
		System.out.println("build took " + ticker.getElapsedTime() + " s");

		// test query
//		for (int i = 0; i < 16; i++) {
//			// testQuery("X3,definedas,X6;X1,antonym,X4;X2,influencedby,X0;X0,field,X3;X2,knownfor,X5;X1,hascontext,X3;X2,field,X3;X0,knownfor,X5;X4,hascontext,X3;\r\n", kb);
//			testQuery("X2,influencedby,X1;X1,field,X0;X2,knownfor,X3;X2,field,X0;X1,knownfor,X3;\r\n", kb);
//		}
//		System.exit(0);

		PatternChromosome.kb = kb;
		PatternChromosome.kbGraph = kbGraph;
		PatternChromosome.random = new Well44497a();

		// -----------------
		registerPatternChromosomeMutation();
		Properties properties = new Properties();
		properties.setProperty("operator", "PatternMutation");
		properties.setProperty("PatternMutation.Rate", Double.toString(PatternMinerConfig.MUTATION_RATE));
		properties.setProperty("populationSize", Integer.toString(PatternMinerConfig.POPULATION_SIZE));
		PatternMinerProblem problem = new PatternMinerProblem(kb);
		InteractiveExecutor ie = new InteractiveExecutor(problem, "NSGAII", properties, Integer.MAX_VALUE);
		@SuppressWarnings("unused")
		NondominatedPopulation result = ie.execute();
	}

	private static void registerPatternChromosomeMutation() {
		OperatorFactory.getInstance().addProvider(new OperatorProvider() {
			public String getMutationHint(Problem problem) {
				return null;
			}

			public String getVariationHint(Problem problem) {
				return null;
			}

			public Variation getVariation(String name, Properties properties, Problem problem) {
				TypedProperties typedProperties = new TypedProperties(properties);

				if (name.equalsIgnoreCase("PatternMutation")) {
					double probability = typedProperties.getDouble("PatternMutation.Rate", 1.0);
					PatternMutation pm = new PatternMutation(probability);
					return pm;
				}

				// No match, return null
				return null;
			}
		});
	}

	@SuppressWarnings("unused")
	private static void testCycleDetector() throws IOException {
		StringGraph pattern = new StringGraph();
		GraphReadWrite.readAutoDetect("0.tgf", pattern);

		PatternChromosome pc = new PatternChromosome(pattern);
		PatternFinderUtils.countCycles(pc);
		System.out.println(pc.cycles);
	}

	@SuppressWarnings("unused")
	private static void testQuery(String query, KnowledgeBase kb) throws IOException, NoSuchFileException {
		query = query.replaceAll("\r\n", ""); // remove lines
		query = query.replaceAll(";", "\r\n"); // convert ; to lines
		// System.out.format("query is:\n%s\n", query);
		StringGraph graph = new StringGraph();
		StringReader sr = new StringReader(query);
		GraphReadWrite.readCSV(sr, graph);
		sr.close();
		// System.out.println("query has " + graph.edgeSet().size() + " edges");

		Query q = Query.make(PatternFinderUtils.createConjunctionFromStringGraph(graph, null));
		Ticker t = new Ticker();
		BigInteger count = kb.count(q, PatternMinerConfig.BLOCK_SIZE, PatternMinerConfig.PARALLEL_LIMIT, Long.MAX_VALUE);
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
	}
}
