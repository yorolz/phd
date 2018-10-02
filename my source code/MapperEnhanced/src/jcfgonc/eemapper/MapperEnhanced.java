package jcfgonc.eemapper;

import java.io.IOException;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.apache.commons.math3.random.Well44497a;

import graph.DirectedMultiGraph;
import graph.GraphReadWrite;
import graph.StringGraph;
import jcfgonc.genetic.GeneticAlgorithm;
import mapper.OrderedPair;
import structures.CSVWriter;
import structures.Ticker;

public class MapperEnhanced {
	public static void main(String[] args) throws Exception {
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

		String filename = "..\\ConceptNet5\\conceptnet5v5.csv";
		// filename = "C:\\Desktop\\bitbucket\\semantic graphs\\horse bird from francisco (original)\\horse_bird_from_book_with_namespaces.csv";

		System.out.println("loading... " + filename);
		StringGraph inputSpace = new StringGraph(1 << 24, 1 << 24, 1 << 24, 1 << 24);
		GraphReadWrite.readCSV_highPerformance(filename, inputSpace);
		inputSpace.showStructureSizes();

		MapperGeneticOperations mgo = new MapperGeneticOperations(inputSpace);
		CSVWriter csvw = new CSVWriter();
		GeneticAlgorithm<MappingStructure<String, String>> ga = new GeneticAlgorithm<>(mgo,csvw);
		ga.execute();
		MappingStructure<String, String> best = ga.getBestGenes();

		System.out.println("mapping done: " + best.getMapping().size() + " pairs");

		// MapperEnhanced blr = new MapperEnhanced();
		// blr.executeSingle();
		// MappingStructure<String, String>[] population = getGeneticAlgorithmPopulation(ga, 4);
		// System.gc();
		// visualizePopulation(population);
	}

	@SuppressWarnings({ "unused", "unchecked" })
	private static MappingStructure<String, String>[] getGeneticAlgorithmPopulation(GeneticAlgorithm<MappingStructure<String, String>> ga, int amount) {
		int populationSize = ga.getPopulationSize();
		MappingStructure<String, String>[] population = new MappingStructure[amount];
		for (int i = 0; i < amount; i++) {
			// ga population is in ascending order, revert so that the first element is the highest
			population[i] = ga.getGenes(populationSize - i - 1);
		}
		return population;
	}

	public void executeSingle(String filename) throws Exception {// one mappingset per chromosome
		Ticker ticker = new Ticker();

		System.out.println("loading... " + filename);
		ticker.getTimeDeltaLastCall();
		StringGraph inputSpace = new StringGraph(1 << 24, 1 << 24, 1 << 24, 1 << 24);
		GraphReadWrite.readCSV_highPerformance(filename, inputSpace);
		System.out.println(ticker.getTimeDeltaLastCall());
		inputSpace.showStructureSizes();
		System.out.println("-------");

		DirectedMultiGraph<OrderedPair<String>, String> pairGraph = new DirectedMultiGraph<>();
		Well44497a random = new Well44497a();
		OrderedPair<String> refPair = MappingAlgorithms.getRandomConceptPair(inputSpace, random);
		MappingAlgorithms.createIsomorphism(inputSpace, pairGraph, random, refPair, Integer.MAX_VALUE);

		MappingStructure<String, String> mapStruct = new MappingStructure<>();
		mapStruct.setPairGraph(pairGraph);
		// mapStruct.setRandom(recordingRandom);
		mapStruct.setRefPair(refPair);
		System.out.println("mapping done: " + pairGraph.vertexSet().size() + " pairs");
		mapStruct.writeTGF("mapping.tgf");

		System.lineSeparator();
	}
	public static MappingStructure<String, String> executeGeneticAlgorithm(StringGraph inputSpace, CSVWriter csvw)
			throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException, IOException, InterruptedException {
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

		MapperGeneticOperations mgo = new MapperGeneticOperations(inputSpace);
		GeneticAlgorithm<MappingStructure<String, String>> ga = new GeneticAlgorithm<>(mgo, csvw);
		ga.execute();
		MappingStructure<String, String> best = ga.getBestGenes();

		System.out.println("mapping done: " + best.getMapping().size() + " pairs");
		return best;
	}

}
