package jcfgonc.blender;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.apache.commons.math3.random.Well44497b;

import com.githhub.aaronbembenek.querykb.Query;

import graph.GraphAlgorithms;
import graph.GraphReadWrite;
import graph.StringGraph;
import jcfgonc.blender.logic.LogicUtils;
import jcfgonc.blender.structures.Blend;
import jcfgonc.blender.structures.Mapping;
import structures.Ticker;
import visual.GraphData;

public class BlenderMoLauncher {
	public static void main(String[] args) throws NoSuchFileException, IOException, ClassNotFoundException, InstantiationException,
			IllegalAccessException, UnsupportedLookAndFeelException, InterruptedException {
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

		String inputSpacePath = "../ConceptNet5/kb/conceptnet5v5.csv";
		String mappingPath = "../EEmapper/2020-04-29_21-23-37_mappings.csv";
		String framesPath = "../PatternMiner/results/resultsV21.csv";

		// read input space
		System.out.println("loading input space from " + inputSpacePath);
		StringGraph kbGraph = new StringGraph(1 << 24, 1 << 24, 1 << 24, 1 << 24);
		Ticker ticker = new Ticker();
		GraphReadWrite.readCSV(inputSpacePath, kbGraph);
		kbGraph.showStructureSizes();
		System.out.println("loading took " + ticker.getTimeDeltaLastCall() + " s");
		System.out.println("-------");

		// read mappings file (contains multiple mappings)
		System.out.println("loading mappings from " + mappingPath);
		ticker.resetTicker();
		List<Mapping<String>> mappingsOriginal = Mapping.readMultipleMappingsCSV(new File(mappingPath));
		List<Mapping<String>> mappings = mappingsOriginal.stream().filter(mapping -> mapping.getSize() > 4 && mapping.size() < 100)
				.collect(Collectors.toList());
		System.out.printf("using %d from %d mappings\n", mappings.size(), mappingsOriginal.size());
		System.out.println("loading took " + ticker.getTimeDeltaLastCall() + " s");
		System.out.println("-------");

		// read frames file
		System.out.println("loading frames from " + framesPath);
		ticker.resetTicker();
		List<StringGraph> framesOriginal = LogicUtils.readPatternResultsDelimitedFile(new File(framesPath), " \t", true, 7);
		// create querykb queries from the frames
		List<Query> frames = framesOriginal.stream().filter(frame -> frame.numberOfEdges() > 2 && frame.numberOfEdges() < 100)
				.map(frame -> LogicUtils.createQueryFromStringGraph(frame)).collect(Collectors.toList());
		System.out.printf("using %d from %d frames\n", frames.size(), framesOriginal.size());
		System.out.println("loading took " + ticker.getTimeDeltaLastCall() + " s");
		System.out.println("-------");

//		KnowledgeBase kb = PatternFinderUtils.buildKnowledgeBase(kbGraph);

//		PatternChromosome.kb = kb;
//		PatternChromosome.kbGraph = kbGraph;
//		PatternChromosome.random = new Well44497a();

		// -----------------
//		registerPatternChromosomeMutation();
//		Properties properties = new Properties();
//		properties.setProperty("operator", "PatternMutation");
//		properties.setProperty("PatternMutation.Rate", Double.toString(PatternMinerConfig.MUTATION_RATE));
//		properties.setProperty("populationSize", Integer.toString(PatternMinerConfig.POPULATION_SIZE));
//		PatternMinerProblem problem = new PatternMinerProblem(kb);
//		InteractiveExecutor ie = new InteractiveExecutor(problem, "NSGAII", properties, Integer.MAX_VALUE);
//		@SuppressWarnings("unused")
//		NondominatedPopulation result = ie.execute();	}

		Semaphore stepSem = new Semaphore(1);
		Well44497b random = new Well44497b(0);
		Mapping<String> mapping = mappings.get(random.nextInt(mappings.size()));
		ArrayList<GraphData> arrGD = new ArrayList<GraphData>();
		for (int i = 0; i < 16; i++) {
			arrGD.add(new GraphData(Integer.toString(i), new StringGraph()));
		}

		BlenderStepperGUI bs = new BlenderStepperGUI();
		bs.setup(arrGD, stepSem);
		List<StringGraph> graphs = arrGD.stream().map(gd -> gd.getStringGraph()).collect(Collectors.toList());
		arrGD = null;

		while (true) {
			// stepSem.acquire();
			Thread.sleep(1000);

			graphs.stream().forEach(graph -> {
				changeBlendSpace(graph, random);
			});

			// BlendMutation.mutateBlend(random, b, kbGraph);
			// System.out.println("step: " + blend.getBlendSpace());
			bs.updateBlendGraph(graphs);
		}

		// System.lineSeparator();
	}

	private static void changeBlendSpace(StringGraph blendSpace, Well44497b random) {
		if (blendSpace.isEmpty()) {
			String v0 = Integer.toString(random.nextInt(8));
			String v1 = Integer.toString(random.nextInt(8));
			String rel = Integer.toString(random.nextInt(8));

			blendSpace.addEdge(v0, v1, rel);
		} else {
			String v0 = GraphAlgorithms.getRandomElementFromCollection(blendSpace.getVertexSet(), random);

			String v1 = Integer.toString(random.nextInt(8));
			String rel = Integer.toString(random.nextInt(8));

			blendSpace.addEdge(v0, v1, rel);
		}
	}

//	private static void registerPatternChromosomeMutation() {
//		OperatorFactory.getInstance().addProvider(new OperatorProvider() {
//			public String getMutationHint(Problem problem) {
//				return null;
//			}
//
//			public String getVariationHint(Problem problem) {
//				return null;
//			}
//
//			public Variation getVariation(String name, Properties properties, Problem problem) {
//				TypedProperties typedProperties = new TypedProperties(properties);
//
//				if (name.equalsIgnoreCase("PatternMutation")) {
//					double probability = typedProperties.getDouble("PatternMutation.Rate", 1.0);
//					PatternMutation pm = new PatternMutation(probability);
//					return pm;
//				}
//
//				// No match, return null
//				return null;
//			}
//		});
//	}
}
