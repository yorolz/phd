package jcfgonc.eemapper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.apache.commons.math3.random.Well44497a;

import graph.DirectedMultiGraph;
import graph.GraphReadWrite;
import graph.StringGraph;
import jcfgonc.eemapper.genetic.MapperGeneticOperations;
import jcfgonc.eemapper.structures.MappingStructure;
import jcfgonc.eemapper.structures.OrderedPair;
import jcfgonc.genetic.GeneticAlgorithm;
import structures.CSVWriter;
import structures.Ticker;
import utils.Various;

public class MapperEnhanced {
	public static void main(String[] args) throws Exception {
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

		String filename = "..\\ConceptNet5\\kb\\conceptnet5v5.csv";
		String mappingsFilename = Various.generateCurrentDateAndTimeStamp() + "_mappings.csv";

		System.out.println("loading... " + filename);
		StringGraph inputSpace = new StringGraph(1 << 24, 1 << 24, 1 << 24, 1 << 24);
		GraphReadWrite.readCSV(filename, inputSpace);
		inputSpace.showStructureSizes();

		MapperGeneticOperations mgo = new MapperGeneticOperations(inputSpace);
		CSVWriter csvw = new CSVWriter();
		GeneticAlgorithm<MappingStructure<String, String>> ga = new GeneticAlgorithm<>(mgo, csvw);
		ga.execute();
		
		MappingStructure<String, String> best = ga.getBestGenes();
		System.out.println("mapping done, largest map has " + best.getMapping().size() + " pairs");
		System.out.println("saving mappings to file " + mappingsFilename);
		saveMappings(ga, new File(mappingsFilename));
		
		System.out.println("done.");
		System.exit(0);
	}

	public static void saveMappings(GeneticAlgorithm<MappingStructure<String, String>> ga, File f) throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter(f), 1 << 16);
		for (int i = 0; i < ga.getPopulationSize(); i++) {
			MappingStructure<String, String> ms = ga.getGenes(i);
			ms.toString(bw);
			bw.newLine();
		}
		bw.close();
	}

	public static MappingStructure<String, String>[] getGeneticAlgorithmPopulation(GeneticAlgorithm<MappingStructure<String, String>> ga, int amount) {
		int populationSize = ga.getPopulationSize();
		@SuppressWarnings("unchecked")
		MappingStructure<String, String>[] population = new MappingStructure[amount];
		for (int i = 0; i < amount; i++) {
			// ga population is in ascending order, revert so that the first element is the highest
			population[i] = ga.getGenes(populationSize - i - 1);
		}
		return population;
	}

	/**
	 * runs a single stochastic execution of the EEmapping algorithm
	 * 
	 * @param filename
	 * @throws Exception
	 */
	public void executeSingle(String filename) throws Exception {// one mappingset per chromosome
		Ticker ticker = new Ticker();

		System.out.println("loading... " + filename);
		ticker.getTimeDeltaLastCall();
		StringGraph inputSpace = new StringGraph(1 << 24, 1 << 24, 1 << 24, 1 << 24);
		GraphReadWrite.readCSV(filename, inputSpace);
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
		mapStruct.writeTGF(Various.generateCurrentDateAndTimeStamp() + "_mapping.tgf");
	}

	/**
	 * executes the GA version of the EEmapper, ready to be invoked as a Web Service.
	 * 
	 * @param inputSpace textual description (source,relation,target) of the semantic graph to find mappings in
	 * @param csvw       if null no execution log is created
	 * @return
	 * @throws ClassNotFoundException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws UnsupportedLookAndFeelException
	 * @throws IOException
	 * @throws InterruptedException
	 */
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
