package jcfgonc.bridging;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.random.Well44497a;

import graph.GraphReadWrite;
import graph.StringGraph;
import graph.StringReaderUnsynchronized;
import jcfgonc.bridging.genetic.GeneticAlgorithm;
import jcfgonc.bridging.genetic.GeneticAlgorithmConfig;
import jcfgonc.bridging.genetic.operators.FitnessEvaluator;
import jcfgonc.bridging.genetic.operators.GeneCrossover;
import jcfgonc.bridging.genetic.operators.GeneInitializer;
import jcfgonc.bridging.genetic.operators.GeneMutation;
import jcfgonc.bridging.genetic.operators.implementation.PassthroughCrossover;
import stream.StreamProcessor;
import stream.StreamService;

public class DomainSpotterLauncher {

	private static BridgingGene invokeGeneticAlgorithm(StringGraph bgraph, int gaMaximumGenerations, int gaPopulationSize) throws InterruptedException {
		GeneMutation<BridgingGene> geneMutator = new GraphMutation(bgraph);
		FitnessEvaluator<BridgingGene> fitnessEvaluator = new BridgeSplitterFitness(bgraph);
		GeneCrossover<BridgingGene> geneCrossover = new PassthroughCrossover<>();
		GeneInitializer<BridgingGene> geneInitializer = new GraphGeneInitializer<BridgingGene>(bgraph);
		int numberOfGenesPerChromosomes = 1;

		GeneticAlgorithm<BridgingGene> ga = new GeneticAlgorithm<>(BridgingGene.class, geneMutator, fitnessEvaluator, geneCrossover, geneInitializer, numberOfGenesPerChromosomes,
				gaMaximumGenerations, gaPopulationSize);
		ga.execute();

		double[] stats = ga.getPopulationStatistics();

		double worstFitness = stats[0];
		double bestFitness = stats[1];
		double meanFitness = stats[2];
		double stdFitness = stats[3];

		System.out.println("Fitness statistics: " + "worst:" + worstFitness + " mean:" + meanFitness + "+-" + stdFitness + " best:" + bestFitness);

		BridgingGene best = ga.getBestGenes()[0];
		return best;
	}

	public static void main(String[] args) throws Exception {
		String filename = "..\\ConceptNet5\\conceptnet5v5.csv";
		// filename = "..\\ConceptNet5\\conceptnet5v5_no_isa.csv";

		{
//			partitionFromFileCSV(filename, GeneticAlgorithmConfig.GA_MAXIMUM_GENERATIONS, GeneticAlgorithmConfig.GA_POPULATION_SIZE);
//			System.exit(0);
		}
		
		{
			StringGraph sourceGraph = new StringGraph(1 << 24, 1 << 24, 1 << 24, 1 << 24);
			GraphReadWrite.readCSV_highPerformance(filename, sourceGraph);
			sourceGraph.showStructureSizes();
			List<String> bridges = GraphReadWrite.loadConceptsFromFile("bridges.txt");

			StreamService streamService = new StreamService();
			streamService.invoke(bridges.size(), new StreamProcessor() {

				@Override
				public void run(int processorId, int rangeL, int rangeH, int streamSize) {
					for (int i = rangeL; i <= rangeH; i++) {
						String bridge = bridges.get(i);
						try {
							partitionFromConcept(bridge, sourceGraph);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			});
			streamService.shutdown();
		}
	}

	public static ArrayList<String> partitionFromString(String graphAsDT, int gaMaximumGenerations, int gaPopulationSize) throws IOException, InterruptedException {

		StringGraph sourceGraph = new StringGraph();
		GraphReadWrite.readDT(new BufferedReader(new StringReaderUnsynchronized(graphAsDT)), sourceGraph);

		BridgingGene best = invokeGeneticAlgorithm(sourceGraph, gaMaximumGenerations, gaPopulationSize);
		System.out.println(best);

		StringGraph output0 = BridgingAlgorithms.intersectGraphWithVertexSet(sourceGraph, best.set0);
		StringGraph output1 = BridgingAlgorithms.intersectGraphWithVertexSet(sourceGraph, best.set1);

		ArrayList<String> inputSpaces = new ArrayList<>();
		{
			StringWriter sw = new StringWriter();
			BufferedWriter bw = new BufferedWriter(sw);
			GraphReadWrite.writeDT(bw, output0, "is0");
			bw.flush();
			String dt = sw.toString();
			inputSpaces.add(dt);
		}
		{
			StringWriter sw = new StringWriter();
			BufferedWriter bw = new BufferedWriter(sw);
			GraphReadWrite.writeDT(bw, output1, "is1");
			bw.flush();
			String dt = sw.toString();
			inputSpaces.add(dt);
		}
		return inputSpaces;
	}

	public static void partitionFromFileCSV(String filename, int gaMaximumGenerations, int gaPopulationSize) throws IOException, InterruptedException {

		StringGraph sourceGraph = new StringGraph(1 << 24, 1 << 24, 1 << 24, 1 << 24);
		GraphReadWrite.readCSV_highPerformance(filename, sourceGraph);
		sourceGraph.showStructureSizes();

		BridgingGene best = invokeGeneticAlgorithm(sourceGraph, gaMaximumGenerations, gaPopulationSize);
		System.out.println(best);

		saveInputSpacesFromBridgeGene(sourceGraph, best);
	}

	public static void partitionFromConcept(String bridge, StringGraph graph) throws IOException {
		System.out.println("partitioning " + bridge + "...");
		Well44497a random = new Well44497a();
		BridgingGene bag = new BridgingGene();
		BridgingAlgorithms.expandFromBridgeUntilIntersect(random, bridge, bag, graph);
		System.out.println(bag);
		saveInputSpacesFromBridgeGene(graph, bag);
	}

	private static void saveInputSpacesFromBridgeGene(StringGraph sourceGraph, BridgingGene best) throws IOException {
		StringGraph output0 = BridgingAlgorithms.intersectGraphWithVertexSet(sourceGraph, best.set0);
		StringGraph output1 = BridgingAlgorithms.intersectGraphWithVertexSet(sourceGraph, best.set1);
		String bridge = best.bridge;
		String preFilename = "tau" + GeneticAlgorithmConfig.MAX_INTERSECTION_RATIO + "_" + bridge;
		System.out.println("saving inputspaces as " + preFilename);
		GraphReadWrite.writeCSV(preFilename + "_is0.csv", output0);
		GraphReadWrite.writeCSV(preFilename + "_is1.csv", output1);
	}

}
