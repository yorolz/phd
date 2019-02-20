package jcfgonc.patternminer.moea;

import java.io.IOException;
import java.io.StringReader;
import java.math.BigInteger;
import java.nio.file.NoSuchFileException;
import java.util.HashSet;
import java.util.Properties;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.apache.commons.math3.random.Well44497a;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.moeaframework.Executor;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Problem;
import org.moeaframework.core.Solution;
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
import structures.ListOfSet;
import structures.Ticker;

/**
 * Demonstrates how a new problem is defined and used within the MOEA Framework.
 */
public class PatternMinerMOEALauncher {

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

		System.out.println("vertices\t" + kbGraph.getVertexSet().size());
		System.out.println("edges   \t" + kbGraph.edgeSet().size());
		System.out.println("-------");

		KnowledgeBaseBuilder kbb = new KnowledgeBaseBuilder();
		ticker.resetTicker();
		System.out.println("kbb.addFacts(inputSpace)");
		kbb.addFacts(kbGraph);
		System.out.println("kbb.build()");
		KnowledgeBase kb = kbb.build();
		System.out.println("build took " + ticker.getElapsedTime() + " s");

		PatternChromosome.kb = kb;
		PatternChromosome.kbGraph = kbGraph;
		PatternChromosome.random = new Well44497a();

		// -----------------
		registerPatternChromosomeMutation();
		NondominatedPopulation result = new Executor()// ---------
				.withProblemClass(PatternMinerProblem.class)// ---
				.withAlgorithm("NSGAII")// -----------------------
				.withProperty("operator", "PatternMutation") // --
				.withProperty("PatternMutation.Rate", 1.0) // ----
				.withProperty("populationSize", 96) // -----------
				.withMaxEvaluations(96 * 10)// -------------------
				.run(); // ---------------------------------------

		for (Solution solution : result) {
			for (int i = 0; i < solution.getNumberOfObjectives(); i++) {
				System.out.format("%.7f\t", solution.getObjective(0));
			}
			System.out.println();
		}
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
		pc.components = new ListOfSet<>();
		pc.components.add(new HashSet<>());

		PatternFinderUtils.countCycles(pc);
		System.out.println(pc.cycles);

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
