package matcher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.math3.random.Well44497a;

import graph.GraphAlgorithms;
import graph.GraphReadWrite;
import graph.StringGraph;
import structures.Combinatorics;
import structures.Ticker;

public class MapperLauncher {
	public final static boolean debug = Config.DEBUG;

	private static <T> void launchAnalogyDetectingThreads(ArrayList<Mapping<String>> rootMappings, AnalogySet[] analogies, StringGraph graph, Ticker ticker)
			throws InterruptedException {
		int amountThreads = Config.THREADS;
		int populationSize = analogies.length;
		ExecutorService es = Executors.newFixedThreadPool(amountThreads);
		Thread.currentThread().setName("analogyEvaluatingBlock");

		ArrayList<AnalogyEvaluatingThread> tasks = new ArrayList<>();

		final int range_size = populationSize / amountThreads;
		for (int thread_id = 0; thread_id < amountThreads; thread_id++) {
			final int range_l = range_size * thread_id;
			final int range_h;
			if (thread_id == amountThreads - 1)
				range_h = populationSize - 1;
			else
				range_h = range_size * (thread_id + 1) - 1;
			// System.out.printf("range:\t%d\t%d\n", range_l,range_h);
			AnalogyEvaluatingThread command = new AnalogyEvaluatingThread(thread_id, range_l, range_h, rootMappings, graph, analogies);
			tasks.add(command);
		}

		StaticTimer.initializeTicker();
		StaticTimer.setTimeout(Config.TIMEOUT_SECONDS);
		es.invokeAll(tasks);
		es.shutdown();
	}

	/**
	 * Given a graph, the root mapping (left & right concepts), get the largest analogy.
	 *
	 * @param graph
	 * @param leftConcept
	 * @param rightConcept
	 * @return
	 * @throws IOException
	 */
	public static Set<Mapping<String>> getAnalogy(StringGraph graph, String leftConcept, String rightConcept, boolean writeCombinationsFile) throws IOException {
		// execute an isomorphish detection again, given the best root mapping
		Set<Mapping<String>> analogy = MatchingDepthStep.execute(graph, leftConcept, rightConcept, writeCombinationsFile);
		return analogy;
	}

	public static void mainOld(String[] args) throws Exception {
		StringGraph graph = new StringGraph();

		final int method = 2;

		switch (method) {
		case 0: { // this method checks for mappings between all the namespaces (ie pig-angel, pig-bird, angel-bird, etc.)
			// merge all the given files
			for (String argument : args) {
				GraphReadWrite.readAutoDetect(argument, graph);
			}
			ArrayList<String> namespaces = new ArrayList<>(GraphAlgorithms.getNameSpaces(graph));
			namespaces.remove("generic"); // generic namespace is not to be used in the combinations
			Iterator<int[]> combIt = Combinatorics.combinations(namespaces.size(), 2);
			while (combIt.hasNext()) {
				int[] comb = combIt.next();
				ArrayList<String> namespacePair = new ArrayList<>();
				namespacePair.add(namespaces.get(comb[0]));
				namespacePair.add(namespaces.get(comb[1]));
				System.out.printf("namespace combination: %s\n", namespacePair);
				StringGraph filteredGraph = GraphAlgorithms.filterNamespaces(graph, namespacePair);
				findAllAnalogies(filteredGraph, null, null);
				System.out.println();
			}
		}
			break;

		case 1: {
			GraphReadWrite.readAutoDetect("C:\\Desktop\\dropbox\\trabalho com o jcunha\\pig-angel-eagle.tgf", graph);
			HashSet<String> namespaces = new HashSet<>();
			namespaces.add("eagle");
			namespaces.add("angel");
			namespaces.add("generic");
			StringGraph filteredGraph = GraphAlgorithms.filterNamespaces(graph, namespaces);
			findAllAnalogies(filteredGraph, null, null);
		}
			break;

		case 2: {
			// merge all the given files
			for (String argument : args) {
				GraphReadWrite.readAutoDetect(argument, graph);
			}
			findAllAnalogies(graph, null, null);
		}
			break;

		default: {
			System.out.println("you forgot specifying the file id in the switch");
			break;
		}

		}

		// Set<Mapping<String>> analogy = MatchingDepthStep.execute(graph, "2", "22");
	}

	public static Set<AnalogySet> findAllAnalogies(StringGraph graph, MutableInt numberConceptsM, MutableInt allocationSizeM) throws IOException, InterruptedException {
		Ticker ticker = new Ticker();
		// build the list of mappings to be analyzed
		ArrayList<String> conceptList = new ArrayList<String>(graph.getVertexSet());
		int numberConcepts = conceptList.size();

		// if (numberConcepts > Math.sqrt(Integer.MAX_VALUE)) {
		double probabilityRate = 1;
		boolean useDithering = false;
		final int conceptLimit = 32;
		if (numberConcepts > conceptLimit) {
			probabilityRate = (double) conceptLimit / numberConcepts;
			useDithering = true;
			System.out.printf("[!] too many concepts (%d) to combine... using dithering of %f\n", numberConcepts, probabilityRate);
			// return null;
		}

		int allocationSize = getRootMappingArrayAllocationSize(probabilityRate * numberConcepts);
		System.out.printf("processing %d root mappings from a total of %d concepts\n", allocationSize, numberConcepts);

		if (numberConceptsM != null) {
			numberConceptsM.setValue(numberConcepts);
		}
		if (allocationSizeM != null) {
			allocationSizeM.setValue(allocationSize);
		}

		ArrayList<Mapping<String>> rootMappings = new ArrayList<>(allocationSize);

		if (useDithering) {
			Well44497a random = new Well44497a();
			HashSet<Mapping<String>> rmSet = new HashSet<>();
			while (rmSet.size() < allocationSize) {
				String leftConcept = conceptList.get(random.nextInt(numberConcepts));
				String rightConcept = conceptList.get(random.nextInt(numberConcepts));
				Mapping<String> rootMapping = new Mapping<>(leftConcept, rightConcept);
				if (leftConcept.equals(rightConcept) || rmSet.contains(rootMapping))
					continue; // you never know...
				rootMappings.add(rootMapping);
				rmSet.add(rootMapping);
			}
		} else {
			for (int i = 0; i < numberConcepts; i++) {
				String leftConcept = conceptList.get(i);
				for (int j = i + 1; j < numberConcepts; j++) {
					String rightConcept = conceptList.get(j);
					if (leftConcept.equals(rightConcept))
						continue; // you never know...
					Mapping<String> rootMapping = new Mapping<>(leftConcept, rightConcept);
					rootMappings.add(rootMapping);
				}
			}
		}

		rootMappings.trimToSize();
		conceptList = null;

		Collections.shuffle(rootMappings); // shuffle because of the timeout, so that other runs can give different results

		AnalogySet[] analogies = new AnalogySet[rootMappings.size()];

		launchAnalogyDetectingThreads(rootMappings, analogies, graph, ticker);

		Set<AnalogySet> bestAnalogies = rateAnalogies(graph, rootMappings, analogies);
		System.out.printf("execution took %f seconds\n", ticker.getTimeDeltaLastCall());
		return bestAnalogies;
	}

	private static int getRootMappingArrayAllocationSize(double numberConcepts) {
		double allocationSize = numberConcepts * (numberConcepts - 1) / 2;
		return (int) Math.ceil(allocationSize);
	}

	private static Set<AnalogySet> rateAnalogies(StringGraph graph, List<Mapping<String>> rootMappings, AnalogySet[] analogies) throws IOException {
		// here we scan for the best score from all the analogies
		int bestScore = -Integer.MAX_VALUE;
		for (int i = 0; i < analogies.length; i++) {
			AnalogySet analogySet = analogies[i];
			if (analogySet == null)
				continue;
			Set<Mapping<String>> mappings = analogySet.getMappings();
			if (mappings != null) {
				int score = mappings.size();
				if (score > bestScore) {
					bestScore = score;
				}
			}
		}

		// stupid way to get all the analogies with the same best score
		// makes use of equals() and hashCode() in AnalogySet
		HashSet<AnalogySet> bestAnalogies = new HashSet<>();
		for (int i = 0; i < analogies.length; i++) {
			AnalogySet analogySet = analogies[i];
			if (analogySet == null)
				continue;
			Set<Mapping<String>> mappings = analogySet.getMappings();
			if (mappings != null) {
				int score = mappings.size();
				if (score == bestScore) {
					bestAnalogies.add(analogySet);
				}
			}
		}
		System.out.printf("got %d analogies with %d mappings each\n", bestAnalogies.size(), bestScore);

		for (AnalogySet analogy : bestAnalogies) {
			System.out.printf("analogy is %s\n", analogy);
		}
		return bestAnalogies;
	}
}
